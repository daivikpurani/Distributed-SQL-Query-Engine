package com.distributed.sql.coordinator;

import com.distributed.sql.common.proto.WorkerServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * gRPC client for communicating with worker nodes
 */
public class WorkerClient {

    private final ManagedChannel channel;
    private final WorkerServiceGrpc.WorkerServiceBlockingStub blockingStub;
    private final String address;
    private final int port;

    public WorkerClient(String address, int port) {
        this.address = address;
        this.port = port;

        this.channel = ManagedChannelBuilder.forAddress(address, port)
                .usePlaintext()
                .build();

        this.blockingStub = WorkerServiceGrpc.newBlockingStub(channel);

        AppLogger.info("Created worker client for {}:{}", address, port);
    }

    public QueryResult executeQuery(String sqlQuery) {
        try {
            ExecuteQueryRequest request = ExecuteQueryRequest.newBuilder()
                    .setSqlQuery(sqlQuery)
                    .setQueryId("query_" + System.currentTimeMillis())
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build();

            ExecuteQueryResponse response = blockingStub.executeQuery(request);

            if (response.getSuccess()) {
                return response.getResult();
            } else {
                AppLogger.warn("Worker query execution failed: {}", response.getMessage());
                return QueryResult.newBuilder()
                        .setQueryId(request.getQueryId())
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

        } catch (Exception e) {
            AppLogger.error("Error executing query on worker {}:{}", address, port, e);
            return QueryResult.newBuilder()
                    .setQueryId("error_" + System.currentTimeMillis())
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
    }

    public WorkerStatus getWorkerStatus() {
        try {
            GetWorkerStatusRequest request = GetWorkerStatusRequest.newBuilder().build();
            GetWorkerStatusResponse response = blockingStub.getWorkerStatus(request);

            if (response.getSuccess()) {
                return response.getStatus();
            } else {
                AppLogger.warn("Failed to get worker status: {}", response.getMessage());
                return null;
            }

        } catch (Exception e) {
            AppLogger.error("Error getting worker status from {}:{}", address, port, e);
            return null;
        }
    }

    public boolean healthCheck() {
        try {
            HealthRequest request = HealthRequest.newBuilder()
                    .setWorkerId(address + ":" + port)
                    .build();

            HealthResponse response = blockingStub.healthCheck(request);
            return response.getHealthy();

        } catch (Exception e) {
            AppLogger.error("Health check failed for worker {}:{}", address, port, e);
            return false;
        }
    }

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            AppLogger.info("Worker client for {}:{} shutdown", address, port);
        } catch (InterruptedException e) {
            AppLogger.error("Error shutting down worker client for {}:{}", address, port, e);
            Thread.currentThread().interrupt();
        }
    }
}
