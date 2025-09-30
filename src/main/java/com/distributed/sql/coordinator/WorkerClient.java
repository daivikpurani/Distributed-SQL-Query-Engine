package com.distributed.sql.coordinator;

import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.proto.WorkerServiceGrpc;
import com.distributed.sql.utils.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * Client for communicating with worker nodes
 */
public class WorkerClient {
    private final ManagedChannel channel;
    private final WorkerServiceGrpc.WorkerServiceBlockingStub blockingStub;
    private final String address;
    private volatile boolean healthy = true;

    public WorkerClient(String address) {
        this.address = address;
        this.channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext()
            .build();
        this.blockingStub = WorkerServiceGrpc.newBlockingStub(channel);
        
        // Test connection
        testConnection();
    }

    private void testConnection() {
        try {
            HealthRequest request = HealthRequest.newBuilder()
                .setWorkerId("coordinator")
                .build();
            
            HealthResponse response = blockingStub.healthCheck(request);
            this.healthy = response.getHealthy();
            
            if (healthy) {
                Logger.info("Worker connection established: {}", address);
            } else {
                Logger.warn("Worker reported unhealthy: {}", address);
            }
        } catch (StatusRuntimeException e) {
            Logger.error("Failed to connect to worker: {}", address, e);
            this.healthy = false;
        }
    }

    public TaskResponse executeTask(TaskRequest request) throws Exception {
        try {
            Logger.debug("Sending task to worker: {}, taskId: {}", address, request.getTaskId());
            
            TaskResponse response = blockingStub.executeTask(request);
            
            Logger.debug("Received response from worker: {}, status: {}", 
                        address, response.getStatus());
            
            return response;
            
        } catch (StatusRuntimeException e) {
            Logger.error("Task execution failed on worker: {}", address, e);
            this.healthy = false;
            
            return TaskResponse.newBuilder()
                .setTaskId(request.getTaskId())
                .setQueryId(request.getQueryId())
                .setStatus(TaskStatus.TASK_FAILED)
                .setErrorMessage("Worker communication error: " + e.getMessage())
                .setExecutionTimeMs(0)
                .build();
        }
    }

    public CheckpointResponse checkpoint(CheckpointRequest request) throws Exception {
        try {
            return blockingStub.checkpoint(request);
        } catch (StatusRuntimeException e) {
            Logger.error("Checkpoint failed on worker: {}", address, e);
            throw e;
        }
    }

    public HealthResponse healthCheck() throws Exception {
        try {
            HealthRequest request = HealthRequest.newBuilder()
                .setWorkerId("coordinator")
                .build();
            
            HealthResponse response = blockingStub.healthCheck(request);
            this.healthy = response.getHealthy();
            
            return response;
        } catch (StatusRuntimeException e) {
            Logger.error("Health check failed on worker: {}", address, e);
            this.healthy = false;
            throw e;
        }
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getAddress() {
        return address;
    }

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            Logger.info("Worker client shutdown: {}", address);
        } catch (InterruptedException e) {
            Logger.error("Worker client shutdown interrupted: {}", address, e);
            Thread.currentThread().interrupt();
        }
    }
}
