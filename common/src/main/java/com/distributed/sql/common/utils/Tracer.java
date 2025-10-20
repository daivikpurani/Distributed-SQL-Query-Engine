package com.distributed.sql.common.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for distributed tracing across the SQL engine
 */
public class Tracer {
    private static final ThreadLocal<String> currentTraceId = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Instant>> traceTimestamps = new ThreadLocal<>();

    public static String startTrace(String operation) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        currentTraceId.set(traceId);

        Map<String, Instant> timestamps = new HashMap<>();
        timestamps.put(operation, Instant.now());
        traceTimestamps.set(timestamps);

        AppLogger.info("[TRACE:{}] Starting operation: {}", traceId, operation);
        return traceId;
    }

    public static void addTimestamp(String event) {
        Map<String, Instant> timestamps = traceTimestamps.get();
        if (timestamps != null) {
            timestamps.put(event, Instant.now());
            AppLogger.debug("[TRACE:{}] Event: {}", getCurrentTraceId(), event);
        }
    }

    public static void endTrace(String operation) {
        String traceId = getCurrentTraceId();
        Map<String, Instant> timestamps = traceTimestamps.get();

        if (timestamps != null && timestamps.containsKey(operation)) {
            Instant start = timestamps.get(operation);
            Duration duration = Duration.between(start, Instant.now());
            AppLogger.info("[TRACE:{}] Completed operation: {} in {}ms", traceId, operation, duration.toMillis());
        }

        currentTraceId.remove();
        traceTimestamps.remove();
    }

    public static String getCurrentTraceId() {
        return currentTraceId.get();
    }

    public static void logStep(String step) {
        AppLogger.debug("[TRACE:{}] Step: {}", getCurrentTraceId(), step);
    }
}
