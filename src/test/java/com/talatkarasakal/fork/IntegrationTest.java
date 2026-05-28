package com.talatkarasakal.fork;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    private File resource(String name) throws Exception {
        URL url = getClass().getClassLoader().getResource(name);
        assertNotNull(url, name + " must exist in test/resources");
        return new File(url.toURI());
    }

    // --- Full simulation end-to-end ---

    @Test
    void fullSimulation_allJobsReceiveCompletionTime() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        Map<String, Station> stations = wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);

        jobM.eventProcess();

        for (Job job : jobs.values()) {
            assertTrue(job.getCompletionTime() > 0,
                    "Job " + job.getJobID() + " should have a positive completion time");
        }
    }

    @Test
    void fullSimulation_endTimeIsPositive() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        Map<String, Station> stations = wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);
        jobM.eventProcess();

        int simulationEndTime = jobs.values().stream()
                .mapToInt(Job::getCompletionTime)
                .max()
                .orElse(0);

        assertTrue(simulationEndTime > 0, "Simulation end time must be positive");
    }

    @Test
    void fullSimulation_eventQueueEmptyAfterRun() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        Map<String, Station> stations = wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);
        jobM.eventProcess();

        assertTrue(jobM.isEventSimulationEmpty());
    }

    @Test
    void fullSimulation_jt2Job_completesWithSingleTask() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        Map<String, Station> stations = wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);
        jobM.eventProcess();

        // J2 is JT2 type — single T2 task; must complete
        Job j2 = jobs.get("J2");
        assertNotNull(j2);
        assertTrue(j2.getCompletionTime() > 0);
    }

    // --- Metrics after full simulation ---

    @Test
    void eventReport_runsWithoutException_afterSimulation() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        Map<String, Station> stations = wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        StationM stationM = new StationM();
        stations.values().forEach(stationM::addStation);

        JobM jobM = new JobM(stationM);
        jobs.values().forEach(jobM::addJob);
        jobM.eventProcess();

        int simulationEndTime = jobs.values().stream()
                .mapToInt(Job::getCompletionTime).max().orElse(1);

        Map<String, List<Job>> jobsByType = new HashMap<>();
        for (Job job : jobs.values()) {
            String typeId = job.getJobType().getJobTypeID();
            jobsByType.computeIfAbsent(typeId, k -> new ArrayList<>()).add(job);
        }

        assertDoesNotThrow(() -> {
            EventReport report = new EventReport(jobsByType, stationM.getStations());
            report.calcAverageTardiness();
            report.calcStationUtilization(simulationEndTime);
        });
    }

    // --- Job file parsing ---

    @Test
    void jobFileParsing_returnsThreeJobs() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        assertEquals(3, jobs.size());
        assertTrue(jobs.containsKey("J1"));
        assertTrue(jobs.containsKey("J2"));
        assertTrue(jobs.containsKey("J3"));
    }

    @Test
    void jobFileParsing_jobStartTimesAreCorrect() throws Exception {
        WorkflowFileParser wfp = new WorkflowFileParser();
        wfp.parse(resource("sample_workflow.txt"));
        Map<String, JobType> jobTypes = wfp.getJobTypes();

        JobFileParser jfp = new JobFileParser(jobTypes);
        Map<String, Job> jobs = jfp.parse(resource("sample_job.txt"));

        assertEquals(0, jobs.get("J1").getStartTime());
        assertEquals(0, jobs.get("J2").getStartTime());
        assertEquals(10, jobs.get("J3").getStartTime());
    }

    @Test
    void jobFileParsing_fileNotFound_throwsException() {
        JobFileParser jfp = new JobFileParser(new HashMap<>());
        assertThrows(Exception.class, () -> jfp.parse(new File("nonexistent_job_file.txt")));
    }
}
