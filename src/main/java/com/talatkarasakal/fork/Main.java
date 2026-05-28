package com.talatkarasakal.fork;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please enter two file names from the command line.");
            System.out.println("Usage: java -jar ayrik-olay-simulasyonu-1.0.0-enhanced.jar <workflow>.txt <job>.txt");
            return;
        }

        String fileName1 = args[0];
        String fileName2 = args[1];

        File file1 = new File(fileName1);
        File file2 = new File(fileName2);

        try {
            WorkflowFileParser workflowParser = new WorkflowFileParser();
            JobFileParser jobParser;

            Map<String, Station> stations;
            Map<String, JobType> jobTypes;
            Map<String, Job> jobs;

            if (isWorkflowFile(file1)) {
                stations = workflowParser.parse(file1);
                jobTypes = workflowParser.getJobTypes();
                jobParser = new JobFileParser(jobTypes);
                jobs = jobParser.parse(file2);
            } else if (isWorkflowFile(file2)) {
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

    private static boolean isWorkflowFile(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.startsWith("(") || line.endsWith(")")) {
                sc.close();
                return true;
            }
        }
        sc.close();
        return false;
    }
}
