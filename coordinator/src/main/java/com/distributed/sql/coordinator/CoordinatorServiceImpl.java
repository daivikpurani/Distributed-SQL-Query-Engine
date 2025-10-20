package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinator gRPC service implementation
 */
public class CoordinatorServiceImpl extends CoordinatorServiceGrpc.CoordinatorServiceImplBase {

    private final SQLParser sqlParser;
    private final QueryPlanner queryPlanner;
    private final ShardManager shardManager;
    private final ExecutorService executorService;
    private final Map<String, WorkerClient> workerClients;

    // System metrics
    private long totalQueries = 0;
    private int activeQueries = 0;
    private final Instant systemStartTime = Instant.now();

    public CoordinatorServiceImpl(ShardManager shardManager) {
        this.shardManager = shardManager;
        this.sqlParser = new SQLParser();
        this.queryPlanner = new QueryPlanner(shardManager);
        this.executorService = Executors.newFixedThreadPool(10);
        this.workerClients = new ConcurrentHashMap<>();

        // Initialize worker clients
        initializeWorkerClients();
    }

    private void initializeWorkerClients() {
        // Initialize clients for known workers
        workerClients.put("worker1", new WorkerClient("localhost", 50052));
        workerClients.put("worker2", new WorkerClient("localhost", 50053));
        workerClients.put("worker3", new WorkerClient("localhost", 50054));

        AppLogger.info("Initialized worker clients for {} workers", workerClients.size());
    }

    @Override
    public void executeQuery(ExecuteQueryRequest request, StreamObserver<ExecuteQueryResponse> responseObserver) {
        String traceId = Tracer.startTrace("execute_query");
        activeQueries++;

        try {
            String sqlQuery = request.getSqlQuery();
            String queryId = request.getQueryId();

            AppLogger.info("Executing query: {} with ID: {}", sqlQuery, queryId);

            // Parse the SQL query
            Query query = sqlParser.parse(sqlQuery);
            query.setQueryId(queryId);

            Tracer.addTimestamp("query_parsed");

            // Create execution plan
            QueryPlan plan = queryPlanner.createExecutionPlan(query);

            Tracer.addTimestamp("plan_created");

            // Execute query across workers
            QueryResult result = executeQueryAcrossWorkers(query, plan);

            Tracer.addTimestamp("execution_completed");

            // Create response
            ExecuteQueryResponse response = ExecuteQueryResponse.newBuilder()
                    .setSuccess(true)
                    .setResult(result)
                    .setMessage("Query executed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            totalQueries++;
            AppLogger.info("Query {} completed successfully in {}ms",
                    queryId, result.getExecutionTimeMs());

        } catch (Exception e) {
            AppLogger.error("Error executing query: " + request.getSqlQuery(), e);

            QueryResult errorResult = QueryResult.newBuilder()
                    .setQueryId(request.getQueryId())
                    .setSqlQuery(request.getSqlQuery())
                    .setExecutionTimeMs(0)
                    .setRowsReturned(0)
                    .setStatus(QueryStatus.FAILED)
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

    private QueryResult executeQueryAcrossWorkers(Query query, QueryPlan plan) {
        List<String> workerIds = plan.getWorkerIds();
        List<CompletableFuture<QueryResult>> futures = new ArrayList<>();

        // Execute query on each worker in parallel
        for (String workerId : workerIds) {
            CompletableFuture<QueryResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    WorkerClient client = workerClients.get(workerId);
                    if (client != null) {
                        return client.executeQuery(query.getSql());
                    } else {
                        AppLogger.warn("No client found for worker: {}", workerId);
                        return createEmptyResult(query.getQueryId(), query.getSql());
                    }
                } catch (Exception e) {
                    AppLogger.error("Error executing query on worker: " + workerId, e);
                    return createEmptyResult(query.getQueryId(), query.getSql());
                }
            }, executorService);

            futures.add(future);
        }

        // Wait for all workers to complete and aggregate results
        List<QueryResult> results = new ArrayList<>();
        for (CompletableFuture<QueryResult> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                AppLogger.error("Error getting result from worker", e);
            }
        }

        return aggregateResults(query.getQueryId(), query.getSql(), results);
    }

    private QueryResult aggregateResults(String queryId, String sqlQuery, List<QueryResult> results) {
        long totalExecutionTime = 0;
        int totalRows = 0;
        List<com.distributed.sql.common.proto.QueryProto.Row> allRows = new ArrayList<>();

        for (QueryResult result : results) {
            totalExecutionTime = Math.max(totalExecutionTime, result.getExecutionTimeMs());
            totalRows += result.getRowsReturned();
            allRows.addAll(result.getResultsList());
        }

        return QueryResult.newBuilder()
                .setQueryId(queryId)
                .setSqlQuery(sqlQuery)
                .setExecutionTimeMs(totalExecutionTime)
                .setRowsReturned(totalRows)
                .addAllResults(allRows)
                .setStatus(QueryStatus.COMPLETED)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                        .build())
                .build();
    }

    private QueryResult createEmptyResult(String queryId, String sqlQuery) {
        return QueryResult.newBuilder()
                .setQueryId(queryId)
                .setSqlQuery(sqlQuery)
                .setExecutionTimeMs(0)
                .setRowsReturned(0)
                .setStatus(QueryStatus.FAILED)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                        .build())
                .build();
    }

    @Override
    public void getSystemStatus(GetSystemStatusRequest request,
            StreamObserver<GetSystemStatusResponse> responseObserver) {
        try {
            SystemStatus.Builder statusBuilder = SystemStatus.newBuilder();

            // Add component statuses
            statusBuilder.putComponents("coordinator", ComponentStatus.newBuilder()
                    .setId("coordinator")
                    .setStatus("HEALTHY")
                    .setCpuUsage(getCpuUsage())
                    .setMemoryUsage(getMemoryUsage())
                    .setActiveConnections(activeQueries)
                    .setLastHeartbeat(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build());

            // Add worker statuses
            for (ShardManager.WorkerInfo worker : shardManager.getActiveWorkers()) {
                statusBuilder.putComponents(worker.getWorkerId(), ComponentStatus.newBuilder()
                        .setId(worker.getWorkerId())
                        .setStatus(shardManager.isWorkerHealthy(worker.getWorkerId()) ? "HEALTHY" : "UNHEALTHY")
                        .setCpuUsage(worker.getCpuUsage())
                        .setMemoryUsage(worker.getMemoryUsage())
                        .setActiveConnections(worker.getActiveQueries())
                        .setLastHeartbeat(com.google.protobuf.Timestamp.newBuilder()
                                .setSeconds(worker.getLastHeartbeat() / 1000)
                                .setNanos((int) ((worker.getLastHeartbeat() % 1000) * 1000000))
                                .build())
                        .build());
            }

            SystemStatus status = statusBuilder
                    .setTotalQueries(totalQueries)
                    .setActiveQueries(activeQueries)
                    .setSystemUptime(com.google.protobuf.Duration.newBuilder()
                            .setSeconds(Duration.between(systemStartTime, Instant.now()).getSeconds())
                            .build())
                    .setLastUpdated(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build();

            GetSystemStatusResponse response = GetSystemStatusResponse.newBuilder()
                    .setSuccess(true)
                    .setStatus(status)
                    .setMessage("System status retrieved successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            AppLogger.error("Error getting system status", e);

            GetSystemStatusResponse response = GetSystemStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void registerWorker(RegisterWorkerRequest request, StreamObserver<RegisterWorkerResponse> responseObserver) {
        try {
            String workerId = request.getWorkerId();
            String address = request.getAddress();
            int port = request.getPort();

            shardManager.registerWorker(workerId, address, port);

            // Add worker client
            workerClients.put(workerId, new WorkerClient(address, port));

            RegisterWorkerResponse response = RegisterWorkerResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Worker registered successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            AppLogger.info("Worker {} registered at {}:{}", workerId, address, port);

        } catch (Exception e) {
            AppLogger.error("Error registering worker", e);

            RegisterWorkerResponse response = RegisterWorkerResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        try {
            String workerId = request.getWorkerId();
            double cpuUsage = request.getCpuUsage();
            double memoryUsage = request.getMemoryUsage();
            int activeQueries = request.getActiveQueries();

            shardManager.updateWorkerHeartbeat(workerId, cpuUsage, memoryUsage, activeQueries);

            HeartbeatResponse response = HeartbeatResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Heartbeat received")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            AppLogger.error("Error processing heartbeat", e);

            HeartbeatResponse response = HeartbeatResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private double getCpuUsage() {
        // Simple CPU usage simulation
        return Math.random() * 100;
    }

    private double getMemoryUsage() {
        // Simple memory usage simulation
        return Math.random() * 100;
    }

    public void shutdown() {
        executorService.shutdown();
        for (WorkerClient client : workerClients.values()) {
            client.shutdown();
        }
    }
}
