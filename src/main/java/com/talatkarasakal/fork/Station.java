package com.talatkarasakal.fork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Station {
    private String stationID;
    private int maxCapacity;
    private int currentCapacity;
    private boolean multiFlag;
    private boolean fifoFlag;
    private Map<String, List<Job>> taskQueues;
    private double stationSpeed;
    private double speedVariation;
    private int totalProcessTime;
    private Map<String, Double> taskSpeeds;

    public Station(String stationID, int maxCapacity, boolean multiFlag, boolean fifoFlag) {
        this.stationID = stationID;
        this.maxCapacity = maxCapacity;
        this.multiFlag = multiFlag;
        this.fifoFlag = fifoFlag;
        this.taskQueues = new HashMap<>();
        this.taskSpeeds = new HashMap<>();
        this.stationSpeed = 1.0;
        this.speedVariation = 0.0;
        this.totalProcessTime = 0;
    }

    public Station(String stationID, int maxCapacity, boolean multiFlag, boolean fifoFlag,
                   double stationSpeed, double speedVariation) {
        this.stationID = stationID;
        this.maxCapacity = maxCapacity;
        this.multiFlag = multiFlag;
        this.fifoFlag = fifoFlag;
        this.taskQueues = new HashMap<>();
        this.taskSpeeds = new HashMap<>();
        this.stationSpeed = stationSpeed;
        this.speedVariation = speedVariation;
        this.totalProcessTime = 0;
    }

    public void addTaskType(String taskTypeID, double speed) {
        taskQueues.put(taskTypeID, new ArrayList<>());
        taskSpeeds.put(taskTypeID, speed);
    }

    public void addTask(TaskType task, Job job) {
        if (!taskQueues.containsKey(task.getTaskTypeID())) {
            System.out.println("Task type cannot be executed here: " + task.getTaskTypeID());
            return;
        }
        taskQueues.get(task.getTaskTypeID()).add(job);
        System.out.println("Job " + job.getJobID() + " is added to station " + stationID
                + " with task " + task.getTaskTypeID());
        printTaskQueue();
    }

    public boolean canHandleTaskType(String taskTypeID) {
        return taskQueues.containsKey(taskTypeID);
    }

    public int getQueueLengthForTask(String taskTypeID) {
        List<Job> queue = taskQueues.get(taskTypeID);
        return queue != null ? queue.size() : 0;
    }

    public double getSpeedForTask(String taskTypeID) {
        double baseSpeed = taskSpeeds.getOrDefault(taskTypeID, stationSpeed);
        if (speedVariation > 0) {
            Random r = new Random();
            double variationFactor = 1 + (r.nextDouble() * 2 - 1) * speedVariation;
            return baseSpeed * variationFactor;
        } else {
            return baseSpeed;
        }
    }

    public double getSpeedVariation() {
        return speedVariation;
    }

    public void setSpeedVariation(double speedVariation) {
        this.speedVariation = speedVariation;
    }

    public void processTasks() {
        if (multiFlag) {
            for (String taskTypeID : taskQueues.keySet()) {
                processTasksForType(taskTypeID);
            }
        } else {
            for (String taskTypeID : taskQueues.keySet()) {
                processTasksForType(taskTypeID);
                break;
            }
        }
    }

    private void processTasksForType(String taskTypeID) {
        List<Job> queue = taskQueues.get(taskTypeID);
        if (fifoFlag) {
            processTasksFIFO(queue);
        } else {
            processTasksEDD(queue);
        }
    }

    private void processTasksFIFO(List<Job> queue) {
        if (!queue.isEmpty() && currentCapacity < maxCapacity) {
            Job job = queue.remove(0);
            currentCapacity++;
            System.out.println("Process started: Job ID: " + job.getJobID()
                    + ", Task ID: " + job.getJobType().getJobTypeID() + " at station " + stationID);
        }
        printTaskQueue();
    }

    private void processTasksEDD(List<Job> queue) {
        queue.sort(Comparator.comparingInt(j -> j.getStartTime() + j.getDuration()));

        if (!queue.isEmpty() && currentCapacity < maxCapacity) {
            Job job = queue.remove(0);
            currentCapacity++;
            System.out.println("Process started: Job ID: " + job.getJobID()
                    + ", Task ID: " + job.getJobType().getJobTypeID() + " at station " + stationID);
        }
        printTaskQueue();
    }

    public void incrementProcessTime(int time) {
        this.totalProcessTime += time;
    }

    public int getTotalProcessTime() {
        return totalProcessTime;
    }

    public void decrementCapacity() {
        currentCapacity--;
    }

    public boolean containsTaskOfJob(String taskTypeID, String jobID) {
        List<Job> queue = taskQueues.get(taskTypeID);
        if (queue != null) {
            for (Job job : queue) {
                if (job.getJobID().equals(jobID)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getStationID() {
        return stationID;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isMultiFlag() {
        return multiFlag;
    }

    public boolean isFifoFlag() {
        return fifoFlag;
    }

    public Map<String, List<Job>> getTaskQueues() {
        return taskQueues;
    }

    public Map<String, Double> getTaskSpeeds() {
        return taskSpeeds;
    }

    private void printTaskQueue() {
        System.out.println("--------------------------------------");
        System.out.println("Station " + stationID + " current task queues:");

        for (String taskTypeID : taskQueues.keySet()) {
            List<Job> queue = taskQueues.get(taskTypeID);
            System.out.print("Task Type: " + taskTypeID + " Queue: ");
            for (Job job : queue) {
                System.out.print(job.getJobID() + " ");
            }
            System.out.println();
        }
        printCurrentTasks();
    }

    private void printCurrentTasks() {
        System.out.println("Station " + stationID + " currently processing tasks:");

        for (String taskTypeID : taskQueues.keySet()) {
            List<Job> queue = taskQueues.get(taskTypeID);
            if (!queue.isEmpty()) {
                Job job = queue.get(0);
                System.out.println("Currently processing: Job ID: " + job.getJobID()
                        + ", Task Type: " + taskTypeID);
            }
        }
        System.out.println("--------------------------------------");
    }
}
