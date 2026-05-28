package com.talatkarasakal.fork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationSchedulingTest {

    private TaskType taskT1;
    private JobType jobTypeSingle;

    @BeforeEach
    void setUp() {
        taskT1 = new TaskType("T1", 2.0);
        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskT1);
        jobTypeSingle = new JobType("JT_SINGLE", tasks);
    }

    private Job makeJob(String id, int startTime, int duration) {
        return new Job(id, jobTypeSingle, startTime, duration);
    }

    private Station fifoStation(int capacity) {
        Station s = new Station("S_FIFO", capacity, false, true);
        s.addTaskType("T1", 1.0);
        return s;
    }

    private Station eddStation(int capacity) {
        Station s = new Station("S_EDD", capacity, false, false);
        s.addTaskType("T1", 1.0);
        return s;
    }

    // --- Queue management happy paths ---

    @Test
    void addTask_incrementsQueueLength() {
        Station station = fifoStation(2);
        assertEquals(0, station.getQueueLengthForTask("T1"));
        station.addTask(taskT1, makeJob("J1", 0, 1));
        assertEquals(1, station.getQueueLengthForTask("T1"));
    }

    @Test
    void canHandleRegisteredTaskType_returnsTrue() {
        Station station = fifoStation(1);
        assertTrue(station.canHandleTaskType("T1"));
    }

    @Test
    void canHandleUnregisteredTaskType_returnsFalse() {
        Station station = fifoStation(1);
        assertFalse(station.canHandleTaskType("T_UNKNOWN"));
    }

    @Test
    void unknownTaskTypeQueue_returnsZeroLength() {
        Station station = fifoStation(1);
        assertEquals(0, station.getQueueLengthForTask("T_MISSING"));
    }

    // --- FIFO scheduling happy paths ---

    @Test
    void fifo_firstAddedJobIsProcessedFirst() {
        Station station = fifoStation(2);
        Job j1 = makeJob("J1", 0, 1);
        Job j2 = makeJob("J2", 0, 1);
        station.addTask(taskT1, j1);
        station.addTask(taskT1, j2);

        // Both in queue; processTasks removes the first one (FIFO order)
        station.processTasks();
        // j1 was first — queue now has only j2
        assertEquals(1, station.getQueueLengthForTask("T1"));
    }

    @Test
    void fifo_processTasks_decreasesQueueByOne() {
        Station station = fifoStation(3);
        station.addTask(taskT1, makeJob("J1", 0, 1));
        station.addTask(taskT1, makeJob("J2", 0, 1));
        station.addTask(taskT1, makeJob("J3", 0, 1));

        station.processTasks();
        assertEquals(2, station.getQueueLengthForTask("T1"));
    }

    // --- Capacity limit edge cases ---

    @Test
    void fifo_capacityLimitPreventsProcessing() {
        Station station = fifoStation(1);
        station.addTask(taskT1, makeJob("J1", 0, 1));
        station.processTasks(); // fills capacity to 1

        station.addTask(taskT1, makeJob("J2", 0, 1));
        station.processTasks(); // capacity full — J2 stays in queue

        assertEquals(1, station.getQueueLengthForTask("T1"));
    }

    @Test
    void decrementCapacity_allowsNextProcessing() {
        Station station = fifoStation(1);
        station.addTask(taskT1, makeJob("J1", 0, 1));
        station.processTasks(); // fills to capacity

        station.addTask(taskT1, makeJob("J2", 0, 1));
        station.decrementCapacity(); // simulate task completion
        station.processTasks(); // now J2 can be processed

        assertEquals(0, station.getQueueLengthForTask("T1"));
    }

    // --- EDD scheduling happy paths ---

    @Test
    void edd_jobWithEarliestDueDate_processedFirst() {
        Station station = eddStation(2);
        // J_LATE due at startTime + duration*60 = 0 + 10*60 = 600
        Job jLate = makeJob("J_LATE", 0, 10);
        // J_EARLY due at 0 + 3*60 = 180
        Job jEarly = makeJob("J_EARLY", 0, 3);

        station.addTask(taskT1, jLate);  // added first
        station.addTask(taskT1, jEarly); // added second

        assertEquals(2, station.getQueueLengthForTask("T1"));
        station.processTasks(); // EDD should pick J_EARLY (due 180 < 600)
        // J_EARLY removed, J_LATE remains
        assertEquals(1, station.getQueueLengthForTask("T1"));
    }

    @Test
    void edd_twoJobsWithSameDueDate_eitherCanBeFirst() {
        Station station = eddStation(1);
        Job j1 = makeJob("J1", 0, 5);
        Job j2 = makeJob("J2", 0, 5); // same due date as j1
        station.addTask(taskT1, j1);
        station.addTask(taskT1, j2);

        station.processTasks();
        assertEquals(1, station.getQueueLengthForTask("T1")); // one was processed
    }

    // --- Speed model happy paths ---

    @Test
    void speedWithZeroVariation_returnsExactRegisteredSpeed() {
        Station station = new Station("S_SPD", 1, false, true, 1.0, 0.0);
        station.addTaskType("T1", 2.5);
        assertEquals(2.5, station.getSpeedForTask("T1"), 1e-9);
    }

    @Test
    void speedWithVariation_staysWithinExpectedBounds() {
        double baseSpeed = 2.0;
        double variation = 0.1;
        Station station = new Station("S_VAR", 1, false, true, 1.0, variation);
        station.addTaskType("T1", baseSpeed);

        for (int i = 0; i < 50; i++) {
            double speed = station.getSpeedForTask("T1");
            assertTrue(speed >= baseSpeed * (1 - variation),
                    "Speed " + speed + " below minimum " + baseSpeed * (1 - variation));
            assertTrue(speed <= baseSpeed * (1 + variation),
                    "Speed " + speed + " above maximum " + baseSpeed * (1 + variation));
        }
    }

    @Test
    void speed_fallsBackToStationBaseSpeed_forUnregisteredTask() {
        Station station = new Station("S_BASE", 1, false, true, 3.0, 0.0);
        station.addTaskType("T1", 2.0);
        // T99 not registered — should fall back to stationSpeed=3.0
        assertEquals(3.0, station.getSpeedForTask("T99"), 1e-9);
    }

    // --- MultiFlag happy paths ---

    @Test
    void multiFlag_processesAllTaskTypeQueues() {
        Station station = new Station("S_MULTI", 4, true, true);
        TaskType t2 = new TaskType("T2", 1.0);
        station.addTaskType("T1", 1.0);
        station.addTaskType("T2", 1.0);

        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskT1);
        tasks.add(t2);
        JobType jt = new JobType("JT2TASKS", tasks);
        Job job1 = new Job("JA", jt, 0, 1);
        Job job2 = new Job("JB", jt, 0, 1);

        station.addTask(taskT1, job1);
        station.addTask(t2, job2);
        assertEquals(1, station.getQueueLengthForTask("T1"));
        assertEquals(1, station.getQueueLengthForTask("T2"));

        station.processTasks(); // multiFlag: processes both queues
        assertEquals(0, station.getQueueLengthForTask("T1"));
        assertEquals(0, station.getQueueLengthForTask("T2"));
    }

    @Test
    void noMultiFlag_processesOnlyOneTaskTypeQueue() {
        Station station = new Station("S_SINGLE", 4, false, true);
        TaskType t2 = new TaskType("T2", 1.0);
        station.addTaskType("T1", 1.0);
        station.addTaskType("T2", 1.0);

        List<TaskType> tasks = new ArrayList<>();
        tasks.add(taskT1);
        tasks.add(t2);
        JobType jt = new JobType("JT2TASKS", tasks);
        Job job1 = new Job("JA", jt, 0, 1);
        Job job2 = new Job("JB", jt, 0, 1);

        station.addTask(taskT1, job1);
        station.addTask(t2, job2);

        station.processTasks(); // no multiFlag: breaks after first task type
        // One queue was cleared, one remains — total non-empty queues = 1
        int remaining = station.getQueueLengthForTask("T1") + station.getQueueLengthForTask("T2");
        assertEquals(1, remaining);
    }

    // --- Process time tracking ---

    @Test
    void incrementProcessTime_accumulatesCorrectly() {
        Station station = fifoStation(1);
        assertEquals(0, station.getTotalProcessTime());
        station.incrementProcessTime(30);
        station.incrementProcessTime(20);
        assertEquals(50, station.getTotalProcessTime());
    }

    // --- containsTaskOfJob ---

    @Test
    void containsTaskOfJob_returnsTrueWhenJobInQueue() {
        Station station = fifoStation(2);
        Job job = makeJob("J_CHECK", 0, 1);
        station.addTask(taskT1, job);
        assertTrue(station.containsTaskOfJob("T1", "J_CHECK"));
    }

    @Test
    void containsTaskOfJob_returnsFalseWhenJobNotInQueue() {
        Station station = fifoStation(1);
        assertFalse(station.containsTaskOfJob("T1", "J_ABSENT"));
    }
}
