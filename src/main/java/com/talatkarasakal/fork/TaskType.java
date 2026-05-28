package com.talatkarasakal.fork;

public class TaskType {
    private String taskTypeID;
    private double defaultSize;

    public TaskType(String taskTypeID, double defaultSize) {
        this.taskTypeID = taskTypeID;
        this.defaultSize = defaultSize;
    }

    public TaskType(String taskTypeID) {
        this.taskTypeID = taskTypeID;
    }

    public String getTaskTypeID() {
        return taskTypeID;
    }

    public double getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(double defaultSize) {
        this.defaultSize = defaultSize;
    }
}
