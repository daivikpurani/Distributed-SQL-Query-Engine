package com.distributed.sql.utils;

import org.slf4j.LoggerFactory;

/**
 * Centralized logging utility for the distributed SQL engine
 */
public class Logger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("DistributedSQLEngine");

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String format, Object... args) {
        logger.info(format, args);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String format, Object... args) {
        logger.debug(format, args);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String format, Object... args) {
        logger.warn(format, args);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void error(String format, Object... args) {
        logger.error(format, args);
    }

    public static void trace(String message) {
        logger.trace(message);
    }

    public static void trace(String format, Object... args) {
        logger.trace(format, args);
    }

    // Query lifecycle logging
    public static void queryStart(String queryId, String sql) {
        info("QUERY_START: queryId={}, sql='{}'", queryId, sql);
    }

    public static void queryPlan(String queryId, String plan) {
        info("QUERY_PLAN: queryId={}, plan={}", queryId, plan);
    }

    public static void queryDispatch(String queryId, String workerId) {
        info("QUERY_DISPATCH: queryId={}, workerId={}", queryId, workerId);
    }

    public static void queryComplete(String queryId, long executionTimeMs, int resultRows) {
        info("QUERY_COMPLETE: queryId={}, executionTime={}ms, resultRows={}", 
             queryId, executionTimeMs, resultRows);
    }

    public static void queryError(String queryId, String error) {
        error("QUERY_ERROR: queryId={}, error={}", queryId, error);
    }

    // Worker lifecycle logging
    public static void workerStart(String workerId, String address) {
        info("WORKER_START: workerId={}, address={}", workerId, address);
    }

    public static void workerTaskStart(String workerId, String taskId) {
        info("WORKER_TASK_START: workerId={}, taskId={}", workerId, taskId);
    }

    public static void workerTaskComplete(String workerId, String taskId, long executionTimeMs) {
        info("WORKER_TASK_COMPLETE: workerId={}, taskId={}, executionTime={}ms", 
             workerId, taskId, executionTimeMs);
    }

    public static void workerFailure(String workerId, String reason) {
        warn("WORKER_FAILURE: workerId={}, reason={}", workerId, reason);
    }

    // Fault tolerance logging
    public static void checkpointCreated(String checkpointId, String workerId) {
        info("CHECKPOINT_CREATED: checkpointId={}, workerId={}", checkpointId, workerId);
    }

    public static void checkpointRestored(String checkpointId, String workerId) {
        info("CHECKPOINT_RESTORED: checkpointId={}, workerId={}", checkpointId, workerId);
    }

    public static void retryAttempt(String taskId, String workerId, int attemptNumber) {
        warn("RETRY_ATTEMPT: taskId={}, workerId={}, attempt={}", taskId, workerId, attemptNumber);
    }
}
