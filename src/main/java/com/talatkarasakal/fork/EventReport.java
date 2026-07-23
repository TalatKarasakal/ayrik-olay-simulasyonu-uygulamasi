package com.talatkarasakal.fork;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventReport {
    private Map<String, List<Job>> jobsByType;
    private Map<String, Station> stations;

    public EventReport(Map<String, List<Job>> jobsByType, Map<String, Station> stations) {
        this.jobsByType = jobsByType;
        this.stations = stations;
    }

    /**
     * Average tardiness per job type, keyed by job type ID. Only job types that actually have
     * late jobs appear in the map — matching what {@link #calcAverageTardiness()} prints.
     */
    public Map<String, Double> averageTardinessByType() {
        Map<String, Double> result = new LinkedHashMap<>();

        for (String jobType : jobsByType.keySet()) {
            List<Job> jobs = jobsByType.get(jobType);

            double allTardiness = 0.0;
            int jobsCompleted = 0;

            for (Job job : jobs) {
                if (job.getCompletionTime() > 0) {
                    int tardiness = tardinessOf(job);
                    if (tardiness > 0) {
                        allTardiness += tardiness;
                        jobsCompleted++;
                    }
                }
            }

            if (jobsCompleted > 0) {
                result.put(jobType, allTardiness / jobsCompleted);
            }
        }
        return result;
    }

    /** Utilization percentage per station ID, using the same formula as the printed report. */
    public Map<String, Double> stationUtilization(int simulationEnd) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String stationId : stations.keySet()) {
            Station station = stations.get(stationId);
            result.put(stationId, (double) station.getTotalProcessTime() / simulationEnd * 100);
        }
        return result;
    }

    /** How many seconds past its deadline a job finished. Negative means it finished early. */
    public static int tardinessOf(Job job) {
        return job.getCompletionTime() - (job.getStartTime() + job.getDuration() * 60);
    }

    public void calcAverageTardiness() {
        System.out.println("Average Late Time:");

        for (Map.Entry<String, Double> entry : averageTardinessByType().entrySet()) {
            System.out.println("Job Type: " + entry.getKey() + ", Average Late: " + entry.getValue() + " seconds");
        }
    }

    public void calcStationUtilization(int simulationEnd) {
        System.out.println("Station Usage Rates:");

        for (Map.Entry<String, Double> entry : stationUtilization(simulationEnd).entrySet()) {
            System.out.println("Station ID: " + entry.getKey() + ", Usage Rate: " + entry.getValue() + "%");
        }
    }
}
