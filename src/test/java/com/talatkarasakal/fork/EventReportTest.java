package com.talatkarasakal.fork;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventReportTest {

    private Job makeJob(String id, int startTime, int duration, int completionTime) {
        TaskType t = new TaskType("T1", 1.0);
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(t);
        JobType jt = new JobType("JT", tasks);
        Job job = new Job(id, jt, startTime, duration);
        job.setCompletionTime(completionTime);
        return job;
    }

    private Station makeStation(String id, int totalProcessTime) {
        Station s = new Station(id, 1, false, true);
        s.incrementProcessTime(totalProcessTime);
        return s;
    }

    // --- Average Tardiness happy paths ---

    @Test
    void averageTardiness_onTimeJob_nothingPrinted() {
        // completionTime=60, startTime=0, duration=1 → tardiness = 60 - (0+1*60) = 0 → not counted
        Job job = makeJob("J1", 0, 1, 60);
        Map<String, List<Job>> jobsByType = new HashMap<>();
        jobsByType.put("JT", List.of(job));

        ByteArrayOutputStream out = captureOut();
        new EventReport(jobsByType, new HashMap<>()).calcAverageTardiness();
        String output = out.toString();

        // No "Average Late:" line for this job type since no tardy jobs
        assertFalse(output.contains("Job Type: JT,"));
    }

    @Test
    void averageTardiness_singleLateJob_computedCorrectly() {
        // startTime=0, duration=1 → deadline=60 sec; completionTime=100 → tardiness=40
        Job job = makeJob("J1", 0, 1, 100);
        Map<String, List<Job>> jobsByType = new HashMap<>();
        jobsByType.put("JT", List.of(job));

        ByteArrayOutputStream out = captureOut();
        new EventReport(jobsByType, new HashMap<>()).calcAverageTardiness();
        String output = out.toString();

        assertTrue(output.contains("Job Type: JT"), "Should report tardiness for JT");
        assertTrue(output.contains("40.0"), "Average tardiness should be 40.0");
    }

    @Test
    void averageTardiness_multipleLatejobs_averageIsCorrect() {
        // J1: tardiness=40, J2: tardiness=60 → average=50
        Job j1 = makeJob("J1", 0, 1, 100); // tardiness=100-60=40
        Job j2 = makeJob("J2", 0, 1, 120); // tardiness=120-60=60

        Map<String, List<Job>> jobsByType = new HashMap<>();
        jobsByType.put("JT", List.of(j1, j2));

        ByteArrayOutputStream out = captureOut();
        new EventReport(jobsByType, new HashMap<>()).calcAverageTardiness();
        String output = out.toString();

        assertTrue(output.contains("50.0"), "Average of 40 and 60 should be 50.0");
    }

    @Test
    void averageTardiness_incompletedJob_ignored() {
        // completionTime=0 means not completed → should be ignored
        Job incompleted = makeJob("J1", 0, 1, 0);
        Map<String, List<Job>> jobsByType = new HashMap<>();
        jobsByType.put("JT", List.of(incompleted));

        ByteArrayOutputStream out = captureOut();
        new EventReport(jobsByType, new HashMap<>()).calcAverageTardiness();
        String output = out.toString();

        assertFalse(output.contains("Job Type: JT,"), "Incompleted jobs must not produce tardiness output");
    }

    @Test
    void averageTardiness_earlyCompletion_notCounted() {
        // completionTime=30 < deadline 60 → tardiness negative → not counted
        Job earlyJob = makeJob("J1", 0, 1, 30);
        Map<String, List<Job>> jobsByType = new HashMap<>();
        jobsByType.put("JT", List.of(earlyJob));

        ByteArrayOutputStream out = captureOut();
        new EventReport(jobsByType, new HashMap<>()).calcAverageTardiness();
        String output = out.toString();

        assertFalse(output.contains("Job Type: JT,"));
    }

    // --- Station Utilization happy paths ---

    @Test
    void stationUtilization_fullUtilization() {
        Station s = makeStation("S1", 100);
        Map<String, Station> stations = new HashMap<>();
        stations.put("S1", s);

        ByteArrayOutputStream out = captureOut();
        new EventReport(new HashMap<>(), stations).calcStationUtilization(100);
        String output = out.toString();

        assertTrue(output.contains("100.0%"), "Full utilization should be 100.0%");
    }

    @Test
    void stationUtilization_fiftyPercent() {
        Station s = makeStation("S1", 50);
        Map<String, Station> stations = new HashMap<>();
        stations.put("S1", s);

        ByteArrayOutputStream out = captureOut();
        new EventReport(new HashMap<>(), stations).calcStationUtilization(100);
        String output = out.toString();

        assertTrue(output.contains("50.0%"), "Half utilization should be 50.0%");
    }

    @Test
    void stationUtilization_zeroProcessTime() {
        Station s = makeStation("S_IDLE", 0);
        Map<String, Station> stations = new HashMap<>();
        stations.put("S_IDLE", s);

        ByteArrayOutputStream out = captureOut();
        new EventReport(new HashMap<>(), stations).calcStationUtilization(100);
        String output = out.toString();

        assertTrue(output.contains("0.0%"), "Idle station should report 0.0%");
    }

    @Test
    void stationUtilization_multipleStations_allReported() {
        Map<String, Station> stations = new HashMap<>();
        stations.put("S1", makeStation("S1", 80));
        stations.put("S2", makeStation("S2", 40));

        ByteArrayOutputStream out = captureOut();
        new EventReport(new HashMap<>(), stations).calcStationUtilization(100);
        String output = out.toString();

        assertTrue(output.contains("S1"));
        assertTrue(output.contains("S2"));
    }

    // helper: capture stdout
    private ByteArrayOutputStream captureOut() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        return bos;
    }
}
