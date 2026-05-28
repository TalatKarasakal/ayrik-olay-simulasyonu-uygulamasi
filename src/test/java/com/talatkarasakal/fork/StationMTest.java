package com.talatkarasakal.fork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationMTest {

    private StationM stationM;
    private TaskType taskT1;
    private TaskType taskT2;

    @BeforeEach
    void setUp() {
        stationM = new StationM();
        taskT1 = new TaskType("T1", 1.0);
        taskT2 = new TaskType("T2", 1.0);

        Station s1 = new Station("S1", 2, true, true);
        s1.addTaskType("T1", 1.0);

        Station s2 = new Station("S2", 1, false, true);
        s2.addTaskType("T2", 2.0);

        stationM.addStation(s1);
        stationM.addStation(s2);
    }

    // --- addStation happy paths ---

    @Test
    void addStation_stationIsRetrievable() {
        assertNotNull(stationM.getStations().get("S1"));
        assertNotNull(stationM.getStations().get("S2"));
    }

    @Test
    void addStation_countIsCorrect() {
        assertEquals(2, stationM.getStations().size());
    }

    @Test
    void addStation_nullDoesNothing() {
        int before = stationM.getStations().size();
        stationM.addStation(null);
        assertEquals(before, stationM.getStations().size());
    }

    // --- getBestStationForTask happy paths ---

    @Test
    void getBestStation_returnsStationThatHandlesTask() {
        Station best = stationM.getBestStationForTask(taskT1, 0);
        assertNotNull(best);
        assertTrue(best.canHandleTaskType("T1"));
    }

    @Test
    void getBestStation_noEligibleStation_returnsNull() {
        TaskType unknownTask = new TaskType("T_UNKNOWN", 1.0);
        assertNull(stationM.getBestStationForTask(unknownTask, 0));
    }

    @Test
    void getBestStation_prefersShortestQueue() {
        // S1 handles T1; add a job to S1's T1 queue so it's longer
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskT1);
        JobType jt = new JobType("JT", tasks);
        Job job = new Job("J1", jt, 0, 1);

        Station s1b = new Station("S1B", 3, false, true);
        s1b.addTaskType("T1", 1.0);
        stationM.addStation(s1b);

        // S1 has queue=1, S1B has queue=0 → S1B should win
        stationM.getStations().get("S1").addTask(taskT1, job);

        Station best = stationM.getBestStationForTask(taskT1, 0);
        assertNotNull(best);
        assertEquals(0, best.getQueueLengthForTask("T1"));
    }

    // --- getStationByTaskAndJob ---

    @Test
    void getStationByTaskAndJob_findsCorrectStation() {
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskT1);
        JobType jt = new JobType("JT", tasks);
        Job job = new Job("J_TARGET", jt, 0, 1);

        stationM.getStations().get("S1").addTask(taskT1, job);

        Station found = stationM.getStationByTaskAndJob("T1", "J_TARGET");
        assertNotNull(found);
        assertEquals("S1", found.getStationID());
    }

    @Test
    void getStationByTaskAndJob_returnNullWhenJobAbsent() {
        assertNull(stationM.getStationByTaskAndJob("T1", "J_NOT_THERE"));
    }

    // --- Edge cases ---

    @Test
    void emptyStationM_bestStation_returnsNull() {
        StationM empty = new StationM();
        assertNull(empty.getBestStationForTask(taskT1, 0));
    }
}
