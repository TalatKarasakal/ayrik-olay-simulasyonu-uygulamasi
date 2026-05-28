package com.talatkarasakal.fork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {

    private TaskType taskType;
    private JobType jobType;
    private Job job;
    private Station station;

    @BeforeEach
    void setUp() {
        taskType = new TaskType("T1", 2.0);
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskType);
        jobType = new JobType("JT1", tasks);
        job = new Job("J1", jobType, 0, 60);
        station = new Station("S1", 2, true, true);
        station.addTaskType("T1", 1.0);
    }

    // --- TaskType ---

    @Test
    void taskTypeHoldsCorrectValues() {
        assertEquals("T1", taskType.getTaskTypeID());
        assertEquals(2.0, taskType.getDefaultSize(), 1e-9);
    }

    @Test
    void taskType_setDefaultSize_updatesValue() {
        taskType.setDefaultSize(5.0);
        assertEquals(5.0, taskType.getDefaultSize(), 1e-9);
    }

    @Test
    void taskType_constructorWithoutSize_defaultSizeIsZero() {
        TaskType t = new TaskType("TX");
        assertEquals("TX", t.getTaskTypeID());
        assertEquals(0.0, t.getDefaultSize(), 1e-9);
    }

    // --- JobType ---

    @Test
    void jobTypeContainsTasks() {
        assertEquals(1, jobType.getTasks().size());
        assertEquals("JT1", jobType.getJobTypeID());
    }

    @Test
    void jobType_getTaskSize_returnsDefaultWhenNotSet() {
        assertEquals(1.0, jobType.getTaskSize("T_MISSING"), 1e-9);
    }

    @Test
    void jobType_setTaskSize_updatesValue() {
        jobType.setTaskSize("T1", 7.5);
        assertEquals(7.5, jobType.getTaskSize("T1"), 1e-9);
    }

    @Test
    void jobType_threeArgConstructor_preservesTaskSizes() {
        java.util.Map<String, Double> sizes = new java.util.HashMap<>();
        sizes.put("T1", 4.0);
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(new TaskType("T1", 4.0));
        JobType jt = new JobType("JT_SIZED", tasks, sizes);
        assertEquals(4.0, jt.getTaskSize("T1"), 1e-9);
        assertEquals("JT_SIZED", jt.getJobTypeID());
    }

    // --- Job ---

    @Test
    void jobInitialCompletionTimeIsZero() {
        assertEquals(0, job.getCompletionTime());
    }

    @Test
    void jobCompletionTimeUpdates() {
        job.setCompletionTime(120);
        assertEquals(120, job.getCompletionTime());
    }

    @Test
    void jobStartTimeSavedPerTask() {
        job.setStartTimeforTask("T1", 42);
        assertEquals(42, job.getStartTimeforTask("T1"));
        assertEquals(-1, job.getStartTimeforTask("T_MISSING"));
    }

    @Test
    void jobDelegatesTaskListToJobType() {
        assertEquals(jobType.getTasks(), job.getTasks());
    }

    @Test
    void jobDelegatesTaskSizeToJobType() {
        assertEquals(jobType.getTaskSize("T1"), job.getTaskSize("T1"), 1e-9);
    }

    // --- Station ---

    @Test
    void stationCanHandleRegisteredTaskType() {
        assertTrue(station.canHandleTaskType("T1"));
        assertFalse(station.canHandleTaskType("T_UNKNOWN"));
    }

    @Test
    void stationQueueStartsEmpty() {
        assertEquals(0, station.getQueueLengthForTask("T1"));
    }

    // --- StationM ---

    @Test
    void stationMAddsAndRetrievesStation() {
        StationM stationM = new StationM();
        stationM.addStation(station);
        assertNotNull(stationM.getStations().get("S1"));
        assertEquals(1, stationM.getStations().size());
    }

    // --- EventSimulation ---

    @Test
    void eventSimulationProcessesInTimeOrder() {
        EventSimulation sim = new EventSimulation();
        Event e1 = new Event(10, Event.Type.StartJob, job, taskType);
        Event e2 = new Event(5, Event.Type.StartJob, job, taskType);
        sim.addEvent(e1);
        sim.addEvent(e2);
        assertEquals(5, sim.getNextEvent().getTime());
        assertEquals(10, sim.getNextEvent().getTime());
        assertTrue(sim.isEmpty());
    }

    @Test
    void eventSimulation_isEmpty_trueWhenEmpty() {
        EventSimulation sim = new EventSimulation();
        assertTrue(sim.isEmpty());
    }

    @Test
    void eventSimulation_getNextEvent_returnsNullWhenEmpty() {
        EventSimulation sim = new EventSimulation();
        assertNull(sim.getNextEvent());
    }

    @Test
    void eventSimulation_threeEvents_sortedByTime() {
        EventSimulation sim = new EventSimulation();
        sim.addEvent(new Event(30, Event.Type.CompleteJob, job, taskType));
        sim.addEvent(new Event(10, Event.Type.StartJob, job, taskType));
        sim.addEvent(new Event(20, Event.Type.StartJob, job, taskType));

        assertEquals(10, sim.getNextEvent().getTime());
        assertEquals(20, sim.getNextEvent().getTime());
        assertEquals(30, sim.getNextEvent().getTime());
        assertTrue(sim.isEmpty());
    }

    @Test
    void event_typeFieldIsCorrect() {
        Event e = new Event(5, Event.Type.CompleteJob, job, taskType);
        assertEquals(Event.Type.CompleteJob, e.getType());
        assertEquals(5, e.getTime());
        assertSame(job, e.getJob());
        assertSame(taskType, e.getTaskType());
    }
}
