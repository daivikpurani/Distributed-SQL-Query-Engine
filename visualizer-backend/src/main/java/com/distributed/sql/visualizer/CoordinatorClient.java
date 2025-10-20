package com.distributed.sql.visualizer;

import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * gRPC client for communicating with the coordinator
 */
@Service
public class CoordinatorClient {

    private static final String COORDINATOR_HOST = "localhost";
    private static final int COORDINATOR_PORT = 50051;

    private ManagedChannel channel;
    private CoordinatorServiceGrpc.CoordinatorServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(COORDINATOR_HOST, COORDINATOR_PORT)
                .usePlaintext()
                .build();

        blockingStub = CoordinatorServiceGrpc.newBlockingStub(channel);

        AppLogger.info("Coordinator client initialized for {}:{}", COORDINATOR_HOST, COORDINATOR_PORT);
    }

    public SystemStatus getSystemStatus() {
        try {
            GetSystemStatusRequest request = GetSystemStatusRequest.newBuilder().build();
            GetSystemStatusResponse response = blockingStub.getSystemStatus(request);

            if (response.getSuccess()) {
                return response.getStatus();
            } else {
                AppLogger.warn("Failed to get system status: {}", response.getMessage());
                return null;
            }

        } catch (Exception e) {
            AppLogger.error("Error getting system status from coordinator", e);
            return null;
        }
    }

    public QueryResult executeQuery(String sqlQuery) {
        try {
            String queryId = "query_" + System.currentTimeMillis();

            ExecuteQueryRequest request = ExecuteQueryRequest.newBuilder()
                    .setSqlQuery(sqlQuery)
                    .setQueryId(queryId)
                    .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis() / 1000)
                            .setNanos((int) ((System.currentTimeMillis() % 1000) * 1000000))
                            .build())
                    .build();

            ExecuteQueryResponse response = blockingStub.executeQuery(request);

            if (response.getSuccess()) {
                return response.getResult();
            } else {
                AppLogger.warn("Query execution failed: {}", response.getMessage());
                return null;
            }

        } catch (Exception e) {
            AppLogger.error("Error executing query on coordinator", e);
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        AppLogger.info("Coordinator client shutdown");
    }
}
