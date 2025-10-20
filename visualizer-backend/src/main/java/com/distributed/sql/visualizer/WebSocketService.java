package com.distributed.sql.visualizer;

import com.distributed.sql.common.proto.QueryProto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket service for real-time data streaming
 */
@Service
public class WebSocketService {

    @Autowired
    private CoordinatorClient coordinatorClient;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Send system status updates every 2 seconds
     */
    @Scheduled(fixedRate = 2000)
    public void sendSystemStatusUpdate() {
        try {
            SystemStatus status = coordinatorClient.getSystemStatus();

            if (status != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("type", "system_status");
                update.put("timestamp", System.currentTimeMillis());
                update.put("data", status);

                messagingTemplate.convertAndSend("/topic/system-status", update);
            }

        } catch (Exception e) {
            // Log error but don't interrupt the scheduled task
            System.err.println("Error sending system status update: " + e.getMessage());
        }
    }

    /**
     * Send performance metrics updates every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void sendMetricsUpdate() {
        try {
            SystemStatus status = coordinatorClient.getSystemStatus();

            if (status != null) {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("totalQueries", status.getTotalQueries());
                metrics.put("activeQueries", status.getActiveQueries());
                metrics.put("systemUptime", status.getSystemUptime().getSeconds());

                // Calculate performance metrics
                Map<String, Object> performanceMetrics = new HashMap<>();
                performanceMetrics.put("totalQueries", status.getTotalQueries());
                performanceMetrics.put("averageLatencyMs", 95.5 + Math.random() * 20); // Mock data with variation
                performanceMetrics.put("queriesPerSecond", 12.3 + Math.random() * 5); // Mock data with variation
                performanceMetrics.put("errorRate", 0.02 + Math.random() * 0.01); // Mock data with variation

                // Worker utilization
                Map<String, Object> workerUtilization = new HashMap<>();
                for (var entry : status.getComponentsMap().entrySet()) {
                    ComponentStatus component = entry.getValue();
                    if (!component.getId().equals("coordinator")) {
                        Map<String, Object> workerMetrics = new HashMap<>();
                        workerMetrics.put("cpuUsage", component.getCpuUsage());
                        workerMetrics.put("memoryUsage", component.getMemoryUsage());
                        workerMetrics.put("activeConnections", component.getActiveConnections());
                        workerUtilization.put(component.getId(), workerMetrics);
                    }
                }
                performanceMetrics.put("workerUtilization", workerUtilization);

                Map<String, Object> update = new HashMap<>();
                update.put("type", "performance_metrics");
                update.put("timestamp", System.currentTimeMillis());
                update.put("data", performanceMetrics);

                messagingTemplate.convertAndSend("/topic/metrics", update);
            }

        } catch (Exception e) {
            // Log error but don't interrupt the scheduled task
            System.err.println("Error sending metrics update: " + e.getMessage());
        }
    }

    /**
     * Send query execution updates
     */
    public void sendQueryExecutionUpdate(String queryId, String status, Object data) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("type", "query_execution");
            update.put("queryId", queryId);
            update.put("status", status);
            update.put("timestamp", System.currentTimeMillis());
            update.put("data", data);

            messagingTemplate.convertAndSend("/topic/query-execution", update);

        } catch (Exception e) {
            System.err.println("Error sending query execution update: " + e.getMessage());
        }
    }
}
