package com.distributed.sql.visualizer;

import com.distributed.sql.common.proto.QueryProto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for the visualizer backend
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VisualizerController {

    @Autowired
    private CoordinatorClient coordinatorClient;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            SystemStatus status = coordinatorClient.getSystemStatus();

            if (status != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("status", status);
                response.put("message", "System status retrieved successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to retrieve system status");

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody Map<String, String> request) {
        try {
            String sqlQuery = request.get("query");

            if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Query is required");

                return ResponseEntity.badRequest().body(response);
            }

            QueryResult result = coordinatorClient.executeQuery(sqlQuery);

            if (result != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("result", result);
                response.put("message", "Query executed successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Query execution failed");

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
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
                performanceMetrics.put("averageLatencyMs", 95.5); // Mock data
                performanceMetrics.put("queriesPerSecond", 12.3); // Mock data
                performanceMetrics.put("errorRate", 0.02); // Mock data

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

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("metrics", performanceMetrics);
                response.put("message", "Metrics retrieved successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to retrieve metrics");

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/workers")
    public ResponseEntity<Map<String, Object>> getWorkers() {
        try {
            SystemStatus status = coordinatorClient.getSystemStatus();

            if (status != null) {
                Map<String, Object> workers = new HashMap<>();

                for (var entry : status.getComponentsMap().entrySet()) {
                    ComponentStatus component = entry.getValue();
                    if (!component.getId().equals("coordinator")) {
                        Map<String, Object> workerInfo = new HashMap<>();
                        workerInfo.put("id", component.getId());
                        workerInfo.put("status", component.getStatus());
                        workerInfo.put("cpuUsage", component.getCpuUsage());
                        workerInfo.put("memoryUsage", component.getMemoryUsage());
                        workerInfo.put("activeConnections", component.getActiveConnections());
                        workerInfo.put("lastHeartbeat", component.getLastHeartbeat().getSeconds());

                        workers.put(component.getId(), workerInfo);
                    }
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("workers", workers);
                response.put("message", "Worker information retrieved successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to retrieve worker information");

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }
}
