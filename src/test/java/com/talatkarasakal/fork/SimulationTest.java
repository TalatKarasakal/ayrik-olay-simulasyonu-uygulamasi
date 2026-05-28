package com.talatkarasakal.fork;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void taskTypeHoldsCorrectValues() {
        assertEquals("T1", taskType.getTaskTypeID());
        assertEquals(2.0, taskType.getDefaultSize(), 1e-9);
    }

    @Test
    void jobTypeContainsTasks() {
        assertEquals(1, jobType.getTasks().size());
        assertEquals("JT1", jobType.getJobTypeID());
    }

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
    void stationCanHandleRegisteredTaskType() {
        assertTrue(station.canHandleTaskType("T1"));
        assertFalse(station.canHandleTaskType("T_UNKNOWN"));
    }

    @Test
    void stationQueueStartsEmpty() {
        assertEquals(0, station.getQueueLengthForTask("T1"));
    }

    @Test
    void stationMAddsAndRetrievesStation() {
        StationM stationM = new StationM();
        stationM.addStation(station);
        assertNotNull(stationM.getStations().get("S1"));
        assertEquals(1, stationM.getStations().size());
    }

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
    void jobStartTimeSavedPerTask() {
        job.setStartTimeforTask("T1", 42);
        assertEquals(42, job.getStartTimeforTask("T1"));
        assertEquals(-1, job.getStartTimeforTask("T_MISSING"));
    }
}
