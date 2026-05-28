package com.talatkarasakal.fork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Job {
    private String jobID;
    private JobType jobType;
    private int startTime;
    private int duration;
    private int completionTime;
    private Map<String, Integer> taskStartTimes;

    public Job(String jobID, JobType jobType, int startTime, int duration) {
        this.jobID = jobID;
        this.jobType = jobType;
        this.startTime = startTime;
        this.duration = duration;
        this.completionTime = 0;
        this.taskStartTimes = new HashMap<>();
    }

    public String getJobID() {
        return jobID;
    }

    public JobType getJobType() {
        return jobType;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public List<TaskType> getTasks() {
        return jobType.getTasks();
    }

    public double getTaskSize(String taskTypeID) {
        return jobType.getTaskSize(taskTypeID);
    }

    public void setStartTimeforTask(String taskTypeID, int time) {
        taskStartTimes.put(taskTypeID, time);
    }

    public int getStartTimeforTask(String taskTypeID) {
        if (taskStartTimes.containsKey(taskTypeID)) {
            return taskStartTimes.get(taskTypeID);
        } else {
            return -1;
        }
    }
}
