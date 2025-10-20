package com.distributed.sql.client;

import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.utils.AppLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * CLI client for testing query execution
 */
public class SQLClient {

    private static final String DEFAULT_COORDINATOR_HOST = "localhost";
    private static final int DEFAULT_COORDINATOR_PORT = 50051;

    private ManagedChannel channel;
    private CoordinatorServiceGrpc.CoordinatorServiceBlockingStub coordinatorStub;

    public static void main(String[] args) {
        String host = DEFAULT_COORDINATOR_HOST;
        int port = DEFAULT_COORDINATOR_PORT;

        // Parse command line arguments
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                switch (args[i]) {
                    case "--host":
                        host = args[i + 1];
                        break;
                    case "--port":
                        port = Integer.parseInt(args[i + 1]);
                        break;
                }
            }
        }

        AppLogger.info("Starting SQL client connecting to {}:{}", host, port);

        try {
            SQLClient client = new SQLClient();
            client.connect(host, port);
            client.runInteractiveMode();
        } catch (Exception e) {
            AppLogger.error("Error running SQL client", e);
            System.exit(1);
        }
    }

    public void connect(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        coordinatorStub = CoordinatorServiceGrpc.newBlockingStub(channel);

        AppLogger.info("Connected to coordinator at {}:{}", host, port);
    }

    public void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Distributed SQL Query Engine Client ===");
        System.out.println("Type 'help' for available commands, 'exit' to quit");
        System.out.println();

        while (true) {
            System.out.print("sql> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                break;
            }

            if (input.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }

            if (input.equalsIgnoreCase("status")) {
                getSystemStatus();
                continue;
            }

            // Execute SQL query
            executeQuery(input);
        }

        scanner.close();
        shutdown();
    }

    private void executeQuery(String sqlQuery) {
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

            ExecuteQueryResponse response = coordinatorStub.executeQuery(request);

            if (response.getSuccess()) {
                displayQueryResult(response.getResult());
            } else {
                System.err.println("Query failed: " + response.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            AppLogger.error("Error executing query", e);
        }
    }

    private void displayQueryResult(QueryResult result) {
        System.out.println("Query ID: " + result.getQueryId());
        System.out.println("Execution Time: " + result.getExecutionTimeMs() + "ms");
        System.out.println("Rows Returned: " + result.getRowsReturned());
        System.out.println("Status: " + result.getStatus());
        System.out.println();

        if (result.getRowsReturned() > 0) {
            System.out.println("Results:");
            System.out.println("--------");

            for (int i = 0; i < result.getResultsCount(); i++) {
                com.distributed.sql.common.proto.QueryProto.Row row = result.getResults(i);
                System.out.print("| ");
                for (int j = 0; j < row.getValuesCount(); j++) {
                    System.out.print(row.getValues(j));
                    if (j < row.getValuesCount() - 1) {
                        System.out.print(" | ");
                    }
                }
                System.out.println(" |");
            }

            System.out.println("--------");
        }

        System.out.println();
    }

    private void getSystemStatus() {
        try {
            GetSystemStatusRequest request = GetSystemStatusRequest.newBuilder().build();
            GetSystemStatusResponse response = coordinatorStub.getSystemStatus(request);

            if (response.getSuccess()) {
                SystemStatus status = response.getStatus();
                System.out.println("=== System Status ===");
                System.out.println("Total Queries: " + status.getTotalQueries());
                System.out.println("Active Queries: " + status.getActiveQueries());
                System.out.println("System Uptime: " + status.getSystemUptime().getSeconds() + " seconds");
                System.out.println();

                System.out.println("Components:");
                for (var entry : status.getComponentsMap().entrySet()) {
                    ComponentStatus component = entry.getValue();
                    System.out.println("  " + component.getId() + ": " + component.getStatus() +
                            " (CPU: " + String.format("%.1f", component.getCpuUsage()) + "%, " +
                            "Memory: " + String.format("%.1f", component.getMemoryUsage()) + "%, " +
                            "Active: " + component.getActiveConnections() + ")");
                }
                System.out.println();
            } else {
                System.err.println("Failed to get system status: " + response.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error getting system status: " + e.getMessage());
            AppLogger.error("Error getting system status", e);
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help     - Show this help message");
        System.out.println("  status   - Show system status");
        System.out.println("  exit     - Exit the client");
        System.out.println();
        System.out.println("SQL Queries:");
        System.out.println("  SELECT * FROM users");
        System.out.println("  SELECT name, age FROM users WHERE age > 30");
        System.out.println("  SELECT COUNT(*) FROM users");
        System.out.println("  SELECT u.name, o.order_id FROM users u JOIN orders o ON u.user_id = o.user_id");
        System.out.println();
    }

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

        AppLogger.info("SQL client shutdown");
    }
}
