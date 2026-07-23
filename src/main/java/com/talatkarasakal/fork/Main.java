package com.talatkarasakal.fork;

import com.talatkarasakal.fork.ui.SimulatorApp;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for both interfaces.
 *
 * <ul>
 *   <li>no arguments — opens the graphical interface</li>
 *   <li>{@code --gui [workflow] [job]} — opens the graphical interface, files preselected</li>
 *   <li>{@code <workflow>.txt <job>.txt} — runs the simulation on the command line</li>
 * </ul>
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            launchGui(null, null);
            return;
        }

        if ("--gui".equals(args[0]) || "-g".equals(args[0])) {
            launchGui(args.length > 1 ? new File(args[1]) : null,
                    args.length > 2 ? new File(args[2]) : null);
            return;
        }

        if (args.length < 2) {
            printUsage();
            return;
        }

        runCommandLine(args[0], args[1]);
    }

    private static void launchGui(File first, File second) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No display available, so the graphical interface cannot start.");
            printUsage();
            return;
        }
        SimulatorApp.launch(first, second);
    }

    private static void printUsage() {
        System.out.println("Please enter two file names from the command line.");
        System.out.println("Usage: java -jar ayrik-olay-simulasyonu-1.0.0-enhanced.jar "
                + "<workflow>.txt <job>.txt");
        System.out.println("       java -jar ayrik-olay-simulasyonu-1.0.0-enhanced.jar "
                + "            (opens the graphical interface)");
    }

    private static void runCommandLine(String fileName1, String fileName2) {
        File file1 = new File(fileName1);
        File file2 = new File(fileName2);

        try {
            WorkflowFileParser workflowParser = new WorkflowFileParser();
            JobFileParser jobParser;

            Map<String, Station> stations;
            Map<String, JobType> jobTypes;
            Map<String, Job> jobs;

            if (SimulationRunner.isWorkflowFile(file1)) {
                stations = workflowParser.parse(file1);
                jobTypes = workflowParser.getJobTypes();
                jobParser = new JobFileParser(jobTypes);
                jobs = jobParser.parse(file2);
            } else if (SimulationRunner.isWorkflowFile(file2)) {
                stations = workflowParser.parse(file2);
                jobTypes = workflowParser.getJobTypes();
                jobParser = new JobFileParser(jobTypes);
                jobs = jobParser.parse(file1);
            } else {
                System.out.println("No valid workflow file found.");
                return;
            }

            if (jobTypes.isEmpty()) {
                System.out.println("No job types found after parsing. Please check the workflow file.");
                return;
            }

            StationM stationM = new StationM();
            for (String id : stations.keySet()) {
                stationM.addStation(stations.get(id));
            }

            JobM jobM = new JobM(stationM);
            for (String id : jobs.keySet()) {
                jobM.addJob(jobs.get(id));
            }

            jobM.eventProcess();

            int simulationEndTime = 0;
            for (String i : jobs.keySet()) {
                Job job = jobs.get(i);
                if (job.getCompletionTime() > simulationEndTime) {
                    simulationEndTime = job.getCompletionTime();
                }
            }

            System.out.println("Event Simulation completed at time: " + simulationEndTime);

            Map<String, List<Job>> jobsByType = new HashMap<>();
            for (String j : jobs.keySet()) {
                Job job = jobs.get(j);
                String jobTypeID = job.getJobType().getJobTypeID();
                if (!jobsByType.containsKey(jobTypeID)) {
                    jobsByType.put(jobTypeID, new ArrayList<>());
                }
                jobsByType.get(jobTypeID).add(job);
            }

            EventReport eventReport = new EventReport(jobsByType, stationM.getStations());
            eventReport.calcAverageTardiness();
            eventReport.calcStationUtilization(simulationEndTime);

        } catch (FileNotFoundException e) {
            System.out.println("File can not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
