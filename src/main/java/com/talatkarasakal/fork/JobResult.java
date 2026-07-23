package com.talatkarasakal.fork;

/**
 * A single job's outcome after a simulation run, flattened for display.
 *
 * @param tardiness seconds past the deadline; negative when the job finished early
 */
public record JobResult(String jobId,
                        String jobTypeId,
                        int startTime,
                        int duration,
                        int completionTime,
                        int tardiness,
                        boolean completed) {

    public boolean isLate() {
        return completed && tardiness > 0;
    }

    /** Wall-clock length of the job, or -1 when it never completed. */
    public int turnaround() {
        return completed ? completionTime - startTime : -1;
    }

    public static JobResult of(Job job) {
        boolean completed = job.getCompletionTime() > 0;
        return new JobResult(
                job.getJobID(),
                job.getJobType().getJobTypeID(),
                job.getStartTime(),
                job.getDuration(),
                job.getCompletionTime(),
                completed ? EventReport.tardinessOf(job) : 0,
                completed);
    }
}
