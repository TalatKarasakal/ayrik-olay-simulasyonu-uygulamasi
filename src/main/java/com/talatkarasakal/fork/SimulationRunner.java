package com.talatkarasakal.fork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Drives a complete simulation and returns a structured {@link SimulationResult}.
 *
 * <p>The simulation classes report their progress by writing to {@code System.out}. To make that
 * narration available to a UI, {@link #run(File, File)} temporarily redirects standard output for
 * the duration of the run and hands the captured text back in the result. Runs are serialised on a
 * lock so that a concurrent run cannot steal another's output.
 */
public final class SimulationRunner {

    private static final Object CONSOLE_LOCK = new Object();

    private SimulationRunner() {
    }

    /**
     * A workflow file is recognised by its parenthesised section syntax; a job file is a plain
     * whitespace-separated table. This mirrors the detection the command line entry point has
     * always used, so file order never matters.
     */
    public static boolean isWorkflowFile(File file) throws FileNotFoundException {
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("(") || line.endsWith(")")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Parses both files (in either order), runs the simulation to completion and collects the
     * metrics. Parser problems are collected as warnings rather than terminating the JVM.
     *
     * @throws FileNotFoundException     if either file cannot be read
     * @throws IllegalArgumentException  if neither file looks like a workflow definition
     */
    public static SimulationResult run(File fileA, File fileB) throws FileNotFoundException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long startedNanos = System.nanoTime();
        SimulationResult result;

        synchronized (CONSOLE_LOCK) {
            PrintStream original = System.out;
            PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
            try {
                System.setOut(capture);
                result = execute(fileA, fileB);
            } finally {
                System.setOut(original);
                capture.flush();
                capture.close();
            }
        }

        long millis = (System.nanoTime() - startedNanos) / 1_000_000L;
        return result.withLog(buffer.toString(StandardCharsets.UTF_8), millis);
    }

    private static SimulationResult execute(File fileA, File fileB) throws FileNotFoundException {
        WorkflowFileParser workflowParser = new WorkflowFileParser();
        workflowParser.setStrict(false);

        File workflowFile;
        File jobFile;
        if (isWorkflowFile(fileA)) {
            workflowFile = fileA;
            jobFile = fileB;
        } else if (isWorkflowFile(fileB)) {
            workflowFile = fileB;
            jobFile = fileA;
        } else {
            throw new IllegalArgumentException("No valid workflow file found. One of the two files "
                    + "must contain the parenthesised TASKTYPES / JOBTYPES / STATIONS sections.");
        }

        Map<String, Station> stations = workflowParser.parse(workflowFile);
        Map<String, JobType> jobTypes = workflowParser.getJobTypes();

        JobFileParser jobParser = new JobFileParser(jobTypes);
        jobParser.setStrict(false);
        Map<String, Job> jobs = jobParser.parse(jobFile);

        List<String> warnings = new ArrayList<>(workflowParser.getErrorMessages());
        warnings.addAll(jobParser.getErrorMessages());
        if (jobTypes.isEmpty()) {
            warnings.add("No job types found after parsing. Please check the workflow file.");
        }
        if (jobs.isEmpty()) {
            warnings.add("No jobs were parsed. Please check the job file.");
        }

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);
        jobM.eventProcess();

        int simulationEndTime = jobs.values().stream()
                .mapToInt(Job::getCompletionTime)
                .max()
                .orElse(0);

        // A sorted map keeps the reported job-type order stable from run to run, which a HashMap
        // would not guarantee.
        Map<String, List<Job>> jobsByType = new TreeMap<>();
        for (Job job : jobs.values()) {
            jobsByType.computeIfAbsent(job.getJobType().getJobTypeID(), k -> new ArrayList<>()).add(job);
        }

        EventReport report = new EventReport(jobsByType, stationM.getStations());
        report.calcAverageTardiness();
        report.calcStationUtilization(simulationEndTime);

        Map<String, Double> utilization = report.stationUtilization(simulationEndTime);

        List<JobResult> jobResults = jobs.values().stream()
                .map(JobResult::of)
                .sorted(Comparator.comparing(JobResult::jobId))
                .toList();

        List<StationResult> stationResults = stationM.getStations().values().stream()
                .map(s -> StationResult.of(s, utilization.getOrDefault(s.getStationID(), 0.0)))
                .sorted(Comparator.comparing(StationResult::stationId))
                .toList();

        return new SimulationResult(jobResults, stationResults, report.averageTardinessByType(),
                simulationEndTime, warnings, "", 0L);
    }
}
