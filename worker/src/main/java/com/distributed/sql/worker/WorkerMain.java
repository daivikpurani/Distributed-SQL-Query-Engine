package com.distributed.sql.worker;

import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Main class to start a worker node
 */
public class WorkerMain {

    private static final String DEFAULT_WORKER_ID = "worker1";
    private static final int DEFAULT_PORT = 50052;
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/worker1_db";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";

    private Server server;
    private WorkerServiceImpl workerService;
    private DataStore dataStore;
    private QueryExecutor queryExecutor;
    private ManagedChannel coordinatorChannel;

    public static void main(String[] args) {
        String workerId = DEFAULT_WORKER_ID;
        int port = DEFAULT_PORT;
        String dbUrl = DEFAULT_DB_URL;
        String dbUser = DEFAULT_DB_USER;
        String dbPassword = DEFAULT_DB_PASSWORD;

        // Parse command line arguments
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                switch (args[i]) {
                    case "--worker-id":
                        workerId = args[i + 1];
                        break;
                    case "--port":
                        port = Integer.parseInt(args[i + 1]);
                        break;
                    case "--db-url":
                        dbUrl = args[i + 1];
                        break;
                    case "--db-user":
                        dbUser = args[i + 1];
                        break;
                    case "--db-password":
                        dbPassword = args[i + 1];
                        break;
                }
            }
        }

        AppLogger.info("Starting worker {} on port: {}", workerId, port);
        AppLogger.info("Database URL: {}", dbUrl);

        try {
            WorkerMain worker = new WorkerMain();
            worker.start(workerId, port, dbUrl, dbUser, dbPassword);
            worker.blockUntilShutdown();
        } catch (Exception e) {
            AppLogger.error("Failed to start worker server", e);
            System.exit(1);
        }
    }

    private void start(String workerId, int port, String dbUrl, String dbUser, String dbPassword) throws IOException {
        // Initialize DataStore
        dataStore = new DataStore(workerId, dbUrl, dbUser, dbPassword);

        // Initialize QueryExecutor
        queryExecutor = new QueryExecutor(workerId, dataStore);

        // Initialize WorkerService
        workerService = new WorkerServiceImpl(workerId, queryExecutor, dataStore);

        // Create and start gRPC server
        server = ServerBuilder.forPort(port)
                .addService(workerService)
                .build()
                .start();

        AppLogger.info("Worker {} server started on port: {}", workerId, port);

        // Register with coordinator
        registerWithCoordinator(workerId, port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AppLogger.info("Shutting down worker {}...", workerId);
            try {
                WorkerMain.this.stop();
            } catch (InterruptedException e) {
                AppLogger.error("Error shutting down worker server", e);
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void registerWithCoordinator(String workerId, int port) {
        try {
            // Connect to coordinator
            coordinatorChannel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();

            CoordinatorServiceGrpc.CoordinatorServiceBlockingStub coordinatorStub = CoordinatorServiceGrpc
                    .newBlockingStub(coordinatorChannel);

            // Register worker
            RegisterWorkerRequest request = RegisterWorkerRequest.newBuilder()
                    .setWorkerId(workerId)
                    .setAddress("localhost")
                    .setPort(port)
                    .build();

            RegisterWorkerResponse response = coordinatorStub.registerWorker(request);

            if (response.getSuccess()) {
                AppLogger.info("Successfully registered worker {} with coordinator", workerId);
            } else {
                AppLogger.warn("Failed to register worker {} with coordinator: {}", workerId, response.getMessage());
            }

        } catch (Exception e) {
            AppLogger.error("Error registering with coordinator", e);
        }
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown();
            if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                AppLogger.warn("Worker server did not terminate gracefully");
                server.shutdownNow();
                if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                    AppLogger.error("Worker server did not terminate");
                }
            }
        }

        if (coordinatorChannel != null) {
            coordinatorChannel.shutdown();
            if (!coordinatorChannel.awaitTermination(5, TimeUnit.SECONDS)) {
                AppLogger.warn("Coordinator channel did not terminate gracefully");
                coordinatorChannel.shutdownNow();
            }
        }

        if (workerService != null) {
            workerService.shutdown();
        }

        if (dataStore != null) {
            dataStore.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
