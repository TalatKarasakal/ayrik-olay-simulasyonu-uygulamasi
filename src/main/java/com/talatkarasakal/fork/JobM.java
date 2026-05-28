package com.talatkarasakal.fork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobM {
    private Map<String, Job> jobs = new HashMap<>();
    private EventSimulation eventSimulation;
    private StationM stationM;

    public JobM(StationM stationManager) {
        this.stationM = stationManager;
        this.eventSimulation = new EventSimulation();
    }

    public void addJob(Job job) {
        System.out.println("Job is added: " + job.getJobID());
        List<TaskType> tasks = job.getJobType().getTasks();
        if (tasks.isEmpty()) {
            System.out.println("JobType for Job ID " + job.getJobID() + " has no tasks defined.");
            return;
        }

        TaskType firstTask = tasks.get(0);
        Event startEvent = new Event(job.getStartTime(), Event.Type.StartJob, job, firstTask);
        eventSimulation.addEvent(startEvent);
        jobs.put(job.getJobID(), job);
    }

    public void eventProcess() {
        while (!eventSimulation.isEmpty()) {
            nextEventProcess();
            for (Station station : stationM.getStations().values()) {
                station.processTasks();
            }
        }
    }

    public void nextEventProcess() {
        Event event = eventSimulation.getNextEvent();
        if (event != null) {
            System.out.println("Event time: " + event.getTime() + ", Event type: " + event.getType()
                    + ", Job ID: " + event.getJob().getJobID());
            if (event.getType() == Event.Type.StartJob) {
                jobStart(event);
            } else if (event.getType() == Event.Type.CompleteJob) {
                taskComplete(event);
            }
            printJobSituation(event.getJob());
        }
    }

    private void printJobSituation(Job job) {
        System.out.println("Job status: ");
        System.out.println("  -Job ID: " + job.getJobID());
        System.out.println("  -Starting Time: " + job.getStartTime());
        System.out.println("  -Complete Time: " + (job.getCompletionTime() > 0 ? job.getCompletionTime() : "Not Completed"));
        System.out.println("  -Tasks: ");
        for (TaskType task : job.getJobType().getTasks()) {
            System.out.println("    -Task ID: " + task.getTaskTypeID() + ", -Default Size: " + task.getDefaultSize());
        }
        System.out.println("--------------------------------------");
    }

    private void jobStart(Event event) {
        Job job = event.getJob();
        TaskType taskType = event.getTaskType();
        Station station = stationM.getBestStationForTask(taskType, event.getTime());
        if (station != null) {
            job.setStartTimeforTask(taskType.getTaskTypeID(), event.getTime());
            station.addTask(taskType, job);
            System.out.println("Job " + job.getJobID() + " started at station " + station.getStationID()
                    + " for task " + taskType.getTaskTypeID());
            int completionTime = event.getTime()
                    + (int) (taskType.getDefaultSize() / station.getSpeedForTask(taskType.getTaskTypeID()) * 60);
            Event completeEvent = new Event(completionTime, Event.Type.CompleteJob, job, taskType);
            eventSimulation.addEvent(completeEvent);
        } else {
            System.out.println("No suitable station found for starting job " + job.getJobID());
        }
    }

    private void taskComplete(Event event) {
        Job job = event.getJob();
        TaskType completedTask = event.getTaskType();
        List<TaskType> tasks = job.getJobType().getTasks();
        int taskIndex = tasks.indexOf(completedTask);

        Station station = stationM.getStationByTaskAndJob(completedTask.getTaskTypeID(), job.getJobID());
        if (station != null) {
            int processTime = event.getTime() - job.getStartTimeforTask(completedTask.getTaskTypeID());
            station.incrementProcessTime(processTime);
            station.decrementCapacity();
        }

        if (taskIndex < tasks.size() - 1) {
            TaskType nextTask = tasks.get(taskIndex + 1);
            Station nextStation = stationM.getBestStationForTask(nextTask, event.getTime());
            if (nextStation != null) {
                job.setStartTimeforTask(nextTask.getTaskTypeID(), event.getTime());
                nextStation.addTask(nextTask, job);
                System.out.println("Task " + completedTask.getTaskTypeID() + " completed for job " + job.getJobID()
                        + ". Next task " + nextTask.getTaskTypeID() + " started at station "
                        + nextStation.getStationID());
                int completionTime = event.getTime()
                        + (int) (nextTask.getDefaultSize() / nextStation.getSpeedForTask(nextTask.getTaskTypeID()) * 60);
                Event completeEvent = new Event(completionTime, Event.Type.CompleteJob, job, nextTask);
                eventSimulation.addEvent(completeEvent);
            } else {
                System.out.println("No suitable station found for next task " + nextTask.getTaskTypeID()
                        + " of job " + job.getJobID());
            }
        } else {
            System.out.println("Job " + job.getJobID() + " completed.");
            job.setCompletionTime(event.getTime());
        }
    }

    public boolean isEventSimulationEmpty() {
        return eventSimulation.isEmpty();
    }
}
