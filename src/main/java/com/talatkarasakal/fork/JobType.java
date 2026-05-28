package com.talatkarasakal.fork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobType {
    private String jobTypeID;
    private List<TaskType> tasks;
    private Map<String, Double> taskSizes;

    public JobType(String jobTypeID, List<TaskType> tasks, Map<String, Double> taskSizes) {
        this.jobTypeID = jobTypeID;
        this.tasks = new ArrayList<>(tasks);
        this.taskSizes = new HashMap<>(taskSizes);
    }

    public JobType(String jobTypeID, List<TaskType> tasks) {
        this.jobTypeID = jobTypeID;
        this.tasks = new ArrayList<>(tasks);
        this.taskSizes = new HashMap<>();
    }

    public String getJobTypeID() {
        return jobTypeID;
    }

    public List<TaskType> getTasks() {
        return tasks;
    }

    public double getTaskSize(String taskTypeID) {
        return taskSizes.getOrDefault(taskTypeID, 1.0);
    }

    public void setTaskSize(String taskTypeID, double size) {
        taskSizes.put(taskTypeID, size);
    }
}
