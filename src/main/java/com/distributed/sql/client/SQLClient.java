package com.distributed.sql.client;

import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.utils.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * CLI client for interacting with the distributed SQL engine
 */
public class SQLClient {
    private final ManagedChannel channel;
    private final CoordinatorServiceGrpc.CoordinatorServiceBlockingStub blockingStub;
    private final Scanner scanner;

    public SQLClient(String coordinatorAddress) {
        this.channel = ManagedChannelBuilder.forTarget(coordinatorAddress)
            .usePlaintext()
            .build();
        this.blockingStub = CoordinatorServiceGrpc.newBlockingStub(channel);
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Distributed SQL Query Engine - CLI Client");
        System.out.println("==========================================");
        System.out.println("Type 'help' for available commands, 'quit' to exit");
        System.out.println();

        while (true) {
            System.out.print("sql> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if ("quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                break;
            }

            if ("help".equalsIgnoreCase(input)) {
                showHelp();
                continue;
            }

            if ("status".equalsIgnoreCase(input)) {
                showWorkerStatus();
                continue;
            }

            if (input.toLowerCase().startsWith("select")) {
                executeQuery(input);
            } else {
                System.out.println("Error: Only SELECT queries are supported");
                System.out.println("Type 'help' for examples");
            }
        }

        shutdown();
    }

    private void executeQuery(String sql) {
        String queryId = "query_" + System.currentTimeMillis();
        
        System.out.println("Executing query: " + sql);
        System.out.println("Query ID: " + queryId);
        System.out.println();

        try {
            QueryRequest request = QueryRequest.newBuilder()
                .setQueryId(queryId)
                .setSqlQuery(sql)
                .setTimestamp(System.currentTimeMillis())
                .build();

            QueryResponse response = blockingStub.executeQuery(request);

            if (response.getStatus() == QueryStatus.COMPLETED) {
                displayResults(response);
            } else {
                System.out.println("Query failed: " + response.getErrorMessage());
            }

        } catch (Exception e) {
            System.out.println("Error executing query: " + e.getMessage());
            Logger.error("Query execution error", e);
        }
    }

    private void displayResults(QueryResponse response) {
        System.out.println("Query Results:");
        System.out.println("===============");
        
        if (response.getRowsCount() == 0) {
            System.out.println("No results found.");
        } else {
            // Display results in a simple table format
            System.out.printf("%-10s | %-20s | %-10s | %-30s | %-15s | %-10s%n", 
                            "user_id", "name", "age", "email", "city", "salary");
            System.out.println("-----------|----------------------|------------|------------------------------|-----------------|----------");
            
            for (com.distributed.sql.common.proto.QueryProto.Row row : response.getRowsList()) {
                if (row.getValuesCount() >= 6) {
                    System.out.printf("%-10s | %-20s | %-10s | %-30s | %-15s | %-10s%n",
                        row.getValues(0), // user_id
                        row.getValues(1), // name
                        row.getValues(2), // age
                        row.getValues(3), // email
                        row.getValues(4), // city
                        row.getValues(5)  // salary
                    );
                }
            }
        }
        
        System.out.println();
        System.out.println("Execution Statistics:");
        System.out.println("  Total rows: " + response.getRowsCount());
        System.out.println("  Execution time: " + response.getExecutionTimeMs() + "ms");
        System.out.println("  Plan time: " + response.getPlan().getPlanTimeMs() + "ms");
        System.out.println("  Workers used: " + response.getPlan().getWorkerIdsCount());
        
        // Display query plan
        displayQueryPlan(response.getPlan());
    }

    private void displayQueryPlan(QueryPlan plan) {
        System.out.println();
        System.out.println("Query Execution Plan:");
        System.out.println("====================");
        System.out.println("Plan ID: " + plan.getQueryId());
        System.out.println("Workers: " + String.join(", ", plan.getWorkerIdsList()));
        System.out.println("Plan Tree:");
        displayPlanNode(plan.getRootNode(), 0);
    }

    private void displayPlanNode(com.distributed.sql.common.proto.QueryProto.PlanNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.printf("%s- %s (%s)%n", indent, node.getType(), node.getNodeId());
        
        if (!node.getTableName().isEmpty()) {
            System.out.printf("%s  Table: %s%n", indent, node.getTableName());
        }
        
        if (!node.getColumnsList().isEmpty()) {
            System.out.printf("%s  Columns: %s%n", indent, String.join(", ", node.getColumnsList()));
        }
        
        if (!node.getConditionsList().isEmpty()) {
            System.out.printf("%s  Conditions: %d%n", indent, node.getConditionsList().size());
        }
        
        System.out.printf("%s  Estimated rows: %d%n", indent, node.getEstimatedRows());
        
        for (com.distributed.sql.common.proto.QueryProto.PlanNode child : node.getChildrenList()) {
            displayPlanNode(child, depth + 1);
        }
    }

    private void showWorkerStatus() {
        try {
            StatusRequest request = StatusRequest.newBuilder()
                .setCoordinatorId("client")
                .build();

            StatusResponse response = blockingStub.getWorkerStatus(request);

            System.out.println("Worker Status:");
            System.out.println("==============");
            
            for (WorkerInfo worker : response.getWorkersList()) {
                System.out.printf("Worker %s: %s (%s)%n", 
                                worker.getWorkerId(), 
                                worker.getStatus(), 
                                worker.getAddress());
            }
            
        } catch (Exception e) {
            System.out.println("Error getting worker status: " + e.getMessage());
        }
    }

    private void showHelp() {
        System.out.println("Available Commands:");
        System.out.println("==================");
        System.out.println("help          - Show this help message");
        System.out.println("status        - Show worker node status");
        System.out.println("quit/exit     - Exit the client");
        System.out.println();
        System.out.println("SQL Query Examples:");
        System.out.println("===================");
        System.out.println("SELECT * FROM users");
        System.out.println("SELECT name, age FROM users WHERE age > 30");
        System.out.println("SELECT u.name, o.product_name FROM users u JOIN orders o ON u.user_id = o.user_id");
        System.out.println();
        System.out.println("Note: Only SELECT queries with basic WHERE and JOIN are supported");
    }

    private void shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            scanner.close();
            System.out.println("Client shutdown complete.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        String coordinatorAddress = "localhost:50051";
        
        if (args.length > 0) {
            coordinatorAddress = args[0];
        }
        
        System.out.println("Connecting to coordinator at: " + coordinatorAddress);
        
        SQLClient client = new SQLClient(coordinatorAddress);
        client.run();
    }
}
