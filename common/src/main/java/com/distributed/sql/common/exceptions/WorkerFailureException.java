package com.distributed.sql.common.exceptions;

/**
 * Exception thrown when a worker fails during query execution.
 * Used for implementing fault tolerance and retry logic.
 */
public class WorkerFailureException extends RuntimeException {

    private final String workerId;
    private final String failureReason;

    public WorkerFailureException(String workerId, String failureReason) {
        super("Worker " + workerId + " failed: " + failureReason);
        this.workerId = workerId;
        this.failureReason = failureReason;
    }

    public WorkerFailureException(String workerId, String failureReason, Throwable cause) {
        super("Worker " + workerId + " failed: " + failureReason, cause);
        this.workerId = workerId;
        this.failureReason = failureReason;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
