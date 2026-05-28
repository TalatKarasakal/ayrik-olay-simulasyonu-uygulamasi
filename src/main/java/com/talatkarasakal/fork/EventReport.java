package com.talatkarasakal.fork;

import java.util.List;
import java.util.Map;

public class EventReport {
    private Map<String, List<Job>> jobsByType;
    private Map<String, Station> stations;

    public EventReport(Map<String, List<Job>> jobsByType, Map<String, Station> stations) {
        this.jobsByType = jobsByType;
        this.stations = stations;
    }

    public void calcAverageTardiness() {
        System.out.println("Average Late Time:");

        for (String jobType : jobsByType.keySet()) {
            List<Job> jobs = jobsByType.get(jobType);

            double allTardiness = 0.0;
            int jobsCompleted = 0;

            for (Job job : jobs) {
                if (job.getCompletionTime() > 0) {
                    int tardiness = job.getCompletionTime() - (job.getStartTime() + job.getDuration() * 60);
                    if (tardiness > 0) {
                        allTardiness += tardiness;
                        jobsCompleted++;
                    }
                }
            }

            if (jobsCompleted > 0) {
                double averageTardiness = allTardiness / jobsCompleted;
                System.out.println("Job Type: " + jobType + ", Average Late: " + averageTardiness + " seconds");
            }
        }
    }

    public void calcStationUtilization(int simulationEnd) {
        System.out.println("Station Usage Rates:");

        for (String stationId : stations.keySet()) {
            Station station = stations.get(stationId);
            double utilization = (double) station.getTotalProcessTime() / simulationEnd * 100;
            System.out.println("Station ID: " + stationId + ", Usage Rate: " + utilization + "%");
        }
    }
}
