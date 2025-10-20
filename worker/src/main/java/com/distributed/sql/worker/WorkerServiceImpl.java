package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.WorkerServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Worker gRPC service implementation
 */
public class WorkerServiceImpl extends WorkerServiceGrpc.WorkerServiceImplBase {

    private final String workerId;
    private final QueryExecutor queryExecutor;
    private final DataStore dataStore;
    private final ScheduledExecutorService scheduler;

    // Worker metrics
    private long totalQueries = 0;
    private int activeQueries = 0;
    private final Instant workerStartTime = Instant.now();
    private double cpuUsage = 0.0;
    private double memoryUsage = 0.0;

    public WorkerServiceImpl(String workerId, QueryExecutor queryExecutor, DataStore dataStore) {
        this.workerId = workerId;
        this.queryExecutor = queryExecutor;
        this.dataStore = dataStore;
        this.scheduler = Executors.newScheduledThreadPool(2);

        // Start periodic metrics update
        startMetricsUpdate();

        AppLogger.info("Worker service {} initialized", workerId);
    }

    private void startMetricsUpdate() {
        // Update CPU and memory usage every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            cpuUsage = Math.random() * 100; // Simulate CPU usage
            memoryUsage = Math.random() * 100; // Simulate memory usage
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void executeQuery(ExecuteQueryRequest request, StreamObserver<ExecuteQueryResponse> responseObserver) {
        String traceId = Tracer.startTrace("execute_query");
        activeQueries++;

        try {
            String sqlQuery = request.getSqlQuery();
            String queryId = request.getQueryId();

            AppLogger.info("Worker {} executing query: {} with ID: {}", workerId, sqlQuery, queryId);

            // Execute the query
            ResultSet resultSet = queryExecutor.executeQuery(sqlQuery);

            // Convert ResultSet to QueryResult
            QueryResult.Builder resultBuilder = QueryResult.newBuilder()
                    .setQueryId(queryId)
                    .setSqlQuery(sqlQuery)
                    .setExecutionTimeMs(resultSet.getExecutionTimeMs())
                    .setRowsReturned(resultSet.getTotalRows())
                    .setStatus(mapStatus(resultSet.getStatus()))
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build());

            // Add rows to result
            for (com.distributed.sql.common.models.Row row : resultSet.getRows()) {
                com.distributed.sql.common.proto.QueryProto.Row.Builder rowBuilder = com.distributed.sql.common.proto.QueryProto.Row
                        .newBuilder();

                for (String value : row.getValues()) {
                    rowBuilder.addValues(value);
                }

                // Add metadata
                for (var entry : row.getMetadata().entrySet()) {
                    rowBuilder.putMetadata(entry.getKey(), entry.getValue());
                }

                resultBuilder.addResults(rowBuilder.build());
            }

            QueryResult queryResult = resultBuilder.build();

            ExecuteQueryResponse response = ExecuteQueryResponse.newBuilder()
                    .setSuccess(true)
                    .setResult(queryResult)
                    .setMessage("Query executed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            totalQueries++;
            AppLogger.info("Worker {} completed query {} in {}ms",
                    workerId, queryId, resultSet.getExecutionTimeMs());

        } catch (Exception e) {
            AppLogger.error("Error executing query on worker {}", workerId, e);

            QueryResult errorResult = QueryResult.newBuilder()
                    .setQueryId(request.getQueryId())
                    .setSqlQuery(request.getSqlQuery())
                    .setExecutionTimeMs(0)
                    .setRowsReturned(0)
                    .setStatus(QueryStatus.FAILED)
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build();

            ExecuteQueryResponse response = ExecuteQueryResponse.newBuilder()
                    .setSuccess(false)
                    .setResult(errorResult)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } finally {
            activeQueries--;
            Tracer.endTrace("execute_query");
        }
    }

    @Override
    public void getWorkerStatus(GetWorkerStatusRequest request,
            StreamObserver<GetWorkerStatusResponse> responseObserver) {
        try {
            WorkerStatus status = WorkerStatus.newBuilder()
                    .setWorkerId(workerId)
                    .setStatus("HEALTHY")
                    .setCpuUsage(cpuUsage)
                    .setMemoryUsage(memoryUsage)
                    .setActiveQueries(activeQueries)
                    .setTotalQueries(totalQueries)
                    .setUptime(com.google.protobuf.Duration.newBuilder()
                            .setSeconds(Duration.between(workerStartTime, Instant.now()).getSeconds())
                            .build())
                    .setLastHeartbeat(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build();

            GetWorkerStatusResponse response = GetWorkerStatusResponse.newBuilder()
                    .setSuccess(true)
                    .setStatus(status)
                    .setMessage("Worker status retrieved successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            AppLogger.error("Error getting worker status", e);

            GetWorkerStatusResponse response = GetWorkerStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void healthCheck(HealthRequest request, StreamObserver<HealthResponse> responseObserver) {
        try {
            boolean healthy = dataStore.healthCheck();

            HealthResponse response = HealthResponse.newBuilder()
                    .setWorkerId(workerId)
                    .setHealthy(healthy)
                    .setTimestamp(System.currentTimeMillis())
                    .setStatusMessage(healthy ? "Worker is healthy" : "Worker is unhealthy")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            AppLogger.error("Error in health check", e);

            HealthResponse response = HealthResponse.newBuilder()
                    .setWorkerId(workerId)
                    .setHealthy(false)
                    .setTimestamp(System.currentTimeMillis())
                    .setStatusMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private QueryStatus mapStatus(String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED":
                return QueryStatus.COMPLETED;
            case "FAILED":
                return QueryStatus.FAILED;
            case "PENDING":
                return QueryStatus.PENDING;
            case "EXECUTING":
                return QueryStatus.EXECUTING;
            case "CANCELLED":
                return QueryStatus.CANCELLED;
            default:
                return QueryStatus.COMPLETED;
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        dataStore.shutdown();
        AppLogger.info("Worker service {} shutdown", workerId);
    }
}
