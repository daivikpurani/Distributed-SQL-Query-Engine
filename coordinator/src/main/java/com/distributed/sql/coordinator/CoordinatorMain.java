package com.distributed.sql.coordinator;

import com.distributed.sql.common.utils.AppLogger;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Main class to start the coordinator server
 */
public class CoordinatorMain {

    private static final int DEFAULT_PORT = 50051;
    private Server server;
    private CoordinatorServiceImpl coordinatorService;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                AppLogger.error("Invalid port number: {}", args[0]);
                System.exit(1);
            }
        }

        AppLogger.info("Starting coordinator server on port: {}", port);

        try {
            CoordinatorMain coordinator = new CoordinatorMain();
            coordinator.start(port);
            coordinator.blockUntilShutdown();
        } catch (Exception e) {
            AppLogger.error("Failed to start coordinator server", e);
            System.exit(1);
        }
    }

    private void start(int port) throws IOException {
        // Initialize shard manager
        ShardManager shardManager = new ShardManager();

        // Initialize coordinator service
        coordinatorService = new CoordinatorServiceImpl(shardManager);

        // Create and start gRPC server
        server = ServerBuilder.forPort(port)
                .addService(coordinatorService)
                .build()
                .start();

        AppLogger.info("Coordinator server started on port: {}", port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AppLogger.info("Shutting down coordinator server...");
            try {
                CoordinatorMain.this.stop();
            } catch (InterruptedException e) {
                AppLogger.error("Error shutting down coordinator server", e);
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown();
            if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                AppLogger.warn("Coordinator server did not terminate gracefully");
                server.shutdownNow();
                if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                    AppLogger.error("Coordinator server did not terminate");
                }
            }
        }

        if (coordinatorService != null) {
            coordinatorService.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
