package com.talatkarasakal.fork;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationRunnerTest {

    private File resource(String name) throws Exception {
        URL url = getClass().getClassLoader().getResource(name);
        assertNotNull(url, name + " must exist in test/resources");
        return new File(url.toURI());
    }

    // --- file type detection -------------------------------------------------------------

    @Test
    void workflowFile_isDetected() throws Exception {
        assertTrue(SimulationRunner.isWorkflowFile(resource("sample_workflow.txt")));
    }

    @Test
    void jobFile_isNotDetectedAsWorkflow() throws Exception {
        assertFalse(SimulationRunner.isWorkflowFile(resource("sample_job.txt")));
    }

    @Test
    void missingFile_throwsFileNotFound() {
        assertThrows(FileNotFoundException.class,
                () -> SimulationRunner.isWorkflowFile(new File("no_such_file_xyz.txt")));
    }

    // --- running -------------------------------------------------------------------------

    @Test
    void run_producesAResultForEveryJob() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        assertEquals(3, result.jobs().size());
        assertEquals(List.of("J1", "J2", "J3"),
                result.jobs().stream().map(JobResult::jobId).toList());
    }

    @Test
    void run_argumentOrderDoesNotMatter() throws Exception {
        SimulationResult forward = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));
        SimulationResult reversed = SimulationRunner.run(
                resource("sample_job.txt"), resource("sample_workflow.txt"));

        assertEquals(forward.jobs(), reversed.jobs());
        assertEquals(forward.simulationEndTime(), reversed.simulationEndTime());
    }

    @Test
    void run_allSampleJobsComplete() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        assertEquals(result.jobs().size(), result.completedJobCount());
        assertTrue(result.jobs().stream().allMatch(JobResult::completed));
    }

    @Test
    void run_simulationEndTimeIsTheLatestCompletion() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        int latest = result.jobs().stream().mapToInt(JobResult::completionTime).max().orElse(0);
        assertEquals(latest, result.simulationEndTime());
        assertTrue(result.simulationEndTime() > 0);
    }

    @Test
    void run_stationsAreSortedAndCarryTheirConfiguration() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        assertEquals(List.of("S1", "S2"),
                result.stations().stream().map(StationResult::stationId).toList());

        StationResult s1 = result.stations().get(0);
        assertEquals(2, s1.maxCapacity());
        assertTrue(s1.multiFlag());
        assertEquals("FIFO", s1.schedulingPolicy());
        assertEquals(List.of("T1", "T2"), s1.taskTypes());
    }

    @Test
    void run_capturesTheSimulationLog() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        assertFalse(result.log().isBlank(), "the console narration should be captured");
        assertTrue(result.log().contains("Added station: S1"));
        assertTrue(result.log().contains("Station Usage Rates:"));
    }

    @Test
    void run_restoresStandardOutputAfterwards() throws Exception {
        PrintStream before = System.out;
        SimulationRunner.run(resource("sample_workflow.txt"), resource("sample_job.txt"));
        assertSame(before, System.out, "System.out must be put back after the run");
    }

    @Test
    void run_recordsHowLongItTook() throws Exception {
        SimulationResult result = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));
        assertTrue(result.durationMillis() >= 0);
    }

    // --- job type ordering ---------------------------------------------------------------

    @Test
    void run_reportsJobTypesInAStableOrder() throws Exception {
        SimulationResult first = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));
        SimulationResult second = SimulationRunner.run(
                resource("sample_workflow.txt"), resource("sample_job.txt"));

        assertEquals(first.tardinessByType().keySet(), second.tardinessByType().keySet());
    }

    // --- error handling ------------------------------------------------------------------

    @Test
    void run_withNoWorkflowFile_throwsIllegalArgument() throws Exception {
        File jobFile = resource("sample_job.txt");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> SimulationRunner.run(jobFile, jobFile));
        assertTrue(error.getMessage().contains("workflow"));
    }

    @Test
    void run_withMissingFile_throwsFileNotFound() throws Exception {
        File workflow = resource("sample_workflow.txt");
        assertThrows(FileNotFoundException.class,
                () -> SimulationRunner.run(workflow, new File("no_such_job_file.txt")));
    }

    @Test
    void run_collectsParserWarningsInsteadOfExiting() throws Exception {
        // warning_workflow.txt repeats a task type, uses an invalid ID and references an
        // undefined task type. On the command line each of these would call System.exit(1).
        SimulationResult result = SimulationRunner.run(
                resource("warning_workflow.txt"), resource("sample_job.txt"));

        assertFalse(result.warnings().isEmpty(), "parser problems should surface as warnings");
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("listed twice")));
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("Undefined task type")));
    }
}
