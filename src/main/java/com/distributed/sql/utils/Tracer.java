package com.distributed.sql.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instrumentation and tracing utility for query lifecycle tracking
 */
public class Tracer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, QueryTrace> activeTraces = new ConcurrentHashMap<>();

    /**
     * Start tracing a query
     */
    public static String startQueryTrace(String queryId, String sql) {
        QueryTrace trace = new QueryTrace(queryId, sql);
        activeTraces.put(queryId, trace);
        trace.addEvent("QUERY_START", "Query submitted", System.currentTimeMillis());
        return queryId;
    }

    /**
     * Add an event to the trace
     */
    public static void addEvent(String queryId, String eventType, String description) {
        QueryTrace trace = activeTraces.get(queryId);
        if (trace != null) {
            trace.addEvent(eventType, description, System.currentTimeMillis());
        }
    }

    /**
     * Add an event with timing information
     */
    public static void addEvent(String queryId, String eventType, String description, long timestamp) {
        QueryTrace trace = activeTraces.get(queryId);
        if (trace != null) {
            trace.addEvent(eventType, description, timestamp);
        }
    }

    /**
     * Record timing for a specific operation
     */
    public static void recordTiming(String queryId, String operation, long startTime, long endTime) {
        QueryTrace trace = activeTraces.get(queryId);
        if (trace != null) {
            trace.addTiming(operation, startTime, endTime);
        }
    }

    /**
     * Complete the trace and return the trace data
     */
    public static QueryTrace completeTrace(String queryId) {
        QueryTrace trace = activeTraces.remove(queryId);
        if (trace != null) {
            trace.addEvent("QUERY_COMPLETE", "Query execution completed", System.currentTimeMillis());
            trace.setEndTime(System.currentTimeMillis());
        }
        return trace;
    }

    /**
     * Get the current trace for a query
     */
    public static QueryTrace getTrace(String queryId) {
        return activeTraces.get(queryId);
    }

    /**
     * Generate a JSON representation of the trace
     */
    public static String toJson(String queryId) {
        QueryTrace trace = activeTraces.get(queryId);
        if (trace == null) {
            return "{}";
        }
        return trace.toJson();
    }

    /**
     * Generate a human-readable trace summary
     */
    public static String toSummary(String queryId) {
        QueryTrace trace = activeTraces.get(queryId);
        if (trace == null) {
            return "No trace found for query: " + queryId;
        }
        return trace.toSummary();
    }

    /**
     * Represents a complete query trace
     */
    public static class QueryTrace {
        private String queryId;
        private String sql;
        private long startTime;
        private long endTime;
        private List<TraceEvent> events;
        private Map<String, TimingInfo> timings;

        public QueryTrace(String queryId, String sql) {
            this.queryId = queryId;
            this.sql = sql;
            this.startTime = System.currentTimeMillis();
            this.events = new ArrayList<>();
            this.timings = new HashMap<>();
        }

        public void addEvent(String eventType, String description, long timestamp) {
            events.add(new TraceEvent(eventType, description, timestamp));
        }

        public void addTiming(String operation, long startTime, long endTime) {
            timings.put(operation, new TimingInfo(startTime, endTime));
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public String toJson() {
            try {
                ObjectNode root = objectMapper.createObjectNode();
                root.put("queryId", queryId);
                root.put("sql", sql);
                root.put("startTime", startTime);
                root.put("endTime", endTime);
                root.put("totalDuration", endTime - startTime);

                ArrayNode eventsArray = root.putArray("events");
                for (TraceEvent event : events) {
                    ObjectNode eventNode = eventsArray.addObject();
                    eventNode.put("type", event.eventType);
                    eventNode.put("description", event.description);
                    eventNode.put("timestamp", event.timestamp);
                    eventNode.put("relativeTime", event.timestamp - startTime);
                }

                ObjectNode timingsNode = root.putObject("timings");
                for (Map.Entry<String, TimingInfo> entry : timings.entrySet()) {
                    ObjectNode timingNode = timingsNode.putObject(entry.getKey());
                    timingNode.put("startTime", entry.getValue().startTime);
                    timingNode.put("endTime", entry.getValue().endTime);
                    timingNode.put("duration", entry.getValue().endTime - entry.getValue().startTime);
                }

                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            } catch (Exception e) {
                return "{\"error\": \"Failed to serialize trace: " + e.getMessage() + "\"}";
            }
        }

        public String toSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Query Trace Summary\n");
            sb.append("==================\n");
            sb.append("Query ID: ").append(queryId).append("\n");
            sb.append("SQL: ").append(sql).append("\n");
            sb.append("Total Duration: ").append(endTime - startTime).append("ms\n\n");

            sb.append("Events:\n");
            for (TraceEvent event : events) {
                sb.append(String.format("  [%dms] %s: %s\n", 
                    event.timestamp - startTime, event.eventType, event.description));
            }

            if (!timings.isEmpty()) {
                sb.append("\nTimings:\n");
                for (Map.Entry<String, TimingInfo> entry : timings.entrySet()) {
                    TimingInfo timing = entry.getValue();
                    sb.append(String.format("  %s: %dms\n", entry.getKey(), 
                        timing.endTime - timing.startTime));
                }
            }

            return sb.toString();
        }

        // Getters
        public String getQueryId() { return queryId; }
        public String getSql() { return sql; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public List<TraceEvent> getEvents() { return events; }
        public Map<String, TimingInfo> getTimings() { return timings; }
    }

    /**
     * Represents a single trace event
     */
    public static class TraceEvent {
        public final String eventType;
        public final String description;
        public final long timestamp;

        public TraceEvent(String eventType, String description, long timestamp) {
            this.eventType = eventType;
            this.description = description;
            this.timestamp = timestamp;
        }
    }

    /**
     * Represents timing information for an operation
     */
    public static class TimingInfo {
        public final long startTime;
        public final long endTime;

        public TimingInfo(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
