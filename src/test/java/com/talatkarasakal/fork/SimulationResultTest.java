package com.talatkarasakal.fork;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulationResultTest {

    private static JobResult job(String id, int tardiness, boolean completed) {
        return new JobResult(id, "JT", 0, 1, completed ? 100 : 0, tardiness, completed);
    }

    private static StationResult station(String id, double utilization) {
        return new StationResult(id, 1, false, true, List.of("T1"), 50, utilization);
    }

    private static SimulationResult result(List<JobResult> jobs, List<StationResult> stations) {
        return new SimulationResult(jobs, stations, Map.of(), 100, List.of(), "", 0L);
    }

    // --- job counts ----------------------------------------------------------------------

    @Test
    void completedJobCount_countsOnlyFinishedJobs() {
        SimulationResult r = result(
                List.of(job("J1", 0, true), job("J2", 0, true), job("J3", 0, false)),
                List.of());
        assertEquals(2, r.completedJobCount());
    }

    @Test
    void lateJobCount_ignoresUnfinishedJobs() {
        // An unfinished job carries no tardiness, so it must not be counted as late.
        SimulationResult r = result(
                List.of(job("J1", 40, true), job("J2", 0, true), job("J3", 500, false)),
                List.of());
        assertEquals(1, r.lateJobCount());
    }

    @Test
    void overallAverageTardiness_averagesOnlyLateJobs() {
        SimulationResult r = result(
                List.of(job("J1", 40, true), job("J2", 60, true), job("J3", 0, true)),
                List.of());
        assertEquals(50.0, r.overallAverageTardiness(), 1e-9);
    }

    @Test
    void overallAverageTardiness_isZeroWhenNothingIsLate() {
        SimulationResult r = result(List.of(job("J1", 0, true)), List.of());
        assertEquals(0.0, r.overallAverageTardiness(), 1e-9);
    }

    // --- station metrics -----------------------------------------------------------------

    @Test
    void averageUtilization_averagesAcrossStations() {
        SimulationResult r = result(List.of(),
                List.of(station("S1", 80), station("S2", 40)));
        assertEquals(60.0, r.averageUtilization(), 1e-9);
    }

    @Test
    void averageUtilization_skipsNonFiniteValues() {
        // A zero-length run divides by zero and yields infinity; it must not poison the average.
        SimulationResult r = result(List.of(),
                List.of(station("S1", 80), station("S2", Double.POSITIVE_INFINITY)));
        assertEquals(80.0, r.averageUtilization(), 1e-9);
    }

    @Test
    void averageUtilization_isZeroWithoutStations() {
        assertEquals(0.0, result(List.of(), List.of()).averageUtilization(), 1e-9);
    }

    @Test
    void busiestStation_isTheMostLoadedOne() {
        SimulationResult r = result(List.of(),
                List.of(station("S1", 30), station("S2", 91), station("S3", 55)));
        assertEquals("S2", r.busiestStation().stationId());
    }

    @Test
    void busiestStation_isNullWithoutStations() {
        assertNull(result(List.of(), List.of()).busiestStation());
    }

    // --- defensive copying ---------------------------------------------------------------

    @Test
    void jobsList_isImmutable() {
        SimulationResult r = result(List.of(job("J1", 0, true)), List.of());
        assertThrows(UnsupportedOperationException.class, () -> r.jobs().add(job("J2", 0, true)));
    }

    // --- JobResult -----------------------------------------------------------------------

    @Test
    void jobResult_isLate_requiresCompletionAndPositiveTardiness() {
        assertTrue(job("J1", 10, true).isLate());
        assertFalse(job("J2", 0, true).isLate());
        assertFalse(job("J3", 10, false).isLate());
    }

    @Test
    void jobResult_turnaround_isMinusOneWhenUnfinished() {
        assertEquals(-1, job("J1", 0, false).turnaround());
        assertEquals(100, job("J2", 0, true).turnaround());
    }

    @Test
    void jobResult_of_derivesTardinessFromTheJob() {
        TaskType task = new TaskType("T1", 1.0);
        JobType type = new JobType("JT1", List.of(task));
        Job job = new Job("J1", type, 0, 1);
        job.setCompletionTime(100);

        JobResult converted = JobResult.of(job);
        assertTrue(converted.completed());
        assertEquals(40, converted.tardiness(), "100 - (0 + 1*60)");
        assertEquals("JT1", converted.jobTypeId());
    }

    @Test
    void jobResult_of_marksUnfinishedJobs() {
        JobType type = new JobType("JT1", List.of(new TaskType("T1", 1.0)));
        JobResult converted = JobResult.of(new Job("J1", type, 0, 1));

        assertFalse(converted.completed());
        assertEquals(0, converted.tardiness());
    }

    // --- StationResult -------------------------------------------------------------------

    @Test
    void stationResult_schedulingPolicyReflectsTheFifoFlag() {
        assertEquals("FIFO", station("S1", 0).schedulingPolicy());
        assertEquals("EDD",
                new StationResult("S2", 1, false, false, List.of(), 0, 0).schedulingPolicy());
    }

    @Test
    void stationResult_of_sortsTaskTypes() {
        Station station = new Station("S1", 2, true, true);
        station.addTaskType("T2", 1.0);
        station.addTaskType("T1", 2.0);

        assertEquals(List.of("T1", "T2"), StationResult.of(station, 0).taskTypes());
    }
}
