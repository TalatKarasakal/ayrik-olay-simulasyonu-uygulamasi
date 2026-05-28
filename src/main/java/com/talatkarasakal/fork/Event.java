package com.talatkarasakal.fork;

public class Event {
    public enum Type {
        StartJob,
        CompleteJob
    }

    private int time;
    private Type type;
    private Job job;
    private TaskType taskType;

    public Event(int time, Type type, Job job, TaskType taskType) {
        this.time = time;
        this.type = type;
        this.job = job;
        this.taskType = taskType;
    }

    public int getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }

    public Job getJob() {
        return job;
    }

    public TaskType getTaskType() {
        return taskType;
    }
}
