package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.utils.Logger;
import com.distributed.sql.utils.Tracer;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Executes query plans by coordinating with worker nodes
 */
public class QueryExecutor {
    private final Map<String, WorkerClient> workerClients;
    private final ExecutorService executorService;
    private final int timeoutSeconds;

    public QueryExecutor(Map<String, WorkerClient> workerClients) {
        this.workerClients = workerClients;
        this.executorService = Executors.newCachedThreadPool();
        this.timeoutSeconds = 30;
    }

    /**
     * Execute a query plan across multiple workers
     */
    public ResultSet executePlan(Query query, QueryPlanner.QueryPlan plan) throws ExecutionException {
        String queryId = query.getQueryId();
        long startTime = System.currentTimeMillis();
        
        Logger.info("Executing query plan: {}", queryId);
        Tracer.addEvent(queryId, "EXECUTION_START", "Query execution started");

        try {
            // Create subplans for each worker
            Map<String, com.distributed.sql.common.models.PlanNode> subplans = createSubplans(plan);
            
            // Execute tasks in parallel
            List<CompletableFuture<TaskResult>> futures = new ArrayList<>();
            
            for (Map.Entry<String, com.distributed.sql.common.models.PlanNode> entry : subplans.entrySet()) {
                String workerId = entry.getKey();
                com.distributed.sql.common.models.PlanNode subplan = entry.getValue();
                
                CompletableFuture<TaskResult> future = CompletableFuture.supplyAsync(() -> {
                    return executeTaskOnWorker(queryId, workerId, subplan);
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all tasks to complete
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            try {
                allTasks.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                Logger.error("Query execution timeout: {}", queryId);
                throw new ExecutionException("Query execution timeout", e);
            }
            
            // Collect results
            ResultSet resultSet = new ResultSet(queryId);
            List<String> columnNames = new ArrayList<>();
            
            for (CompletableFuture<TaskResult> future : futures) {
                TaskResult result = future.get();
                
                if (result.isSuccess()) {
                    if (columnNames.isEmpty()) {
                        columnNames.addAll(result.getColumnNames());
                    }
                    resultSet.addRows(convertProtoRowsToModelRows(result.getRows()));
                } else {
                    Logger.error("Task failed on worker: {}, error: {}", 
                               result.getWorkerId(), result.getErrorMessage());
                }
            }
            
            resultSet.setColumnNames(columnNames);
            resultSet.setTotalRows(resultSet.getRows().size());
            
            long endTime = System.currentTimeMillis();
            resultSet.setExecutionTimeMs(endTime - startTime);
            
            Tracer.addEvent(queryId, "EXECUTION_COMPLETE", 
                          "Query completed with " + resultSet.getTotalRows() + " rows");
            Tracer.recordTiming(queryId, "EXECUTION", startTime, endTime);
            
            Logger.queryComplete(queryId, resultSet.getExecutionTimeMs(), resultSet.getTotalRows());
            return resultSet;
            
        } catch (Exception e) {
            Logger.queryError(queryId, e.getMessage());
            throw new ExecutionException("Query execution failed: " + e.getMessage(), e);
        }
    }

    private TaskResult executeTaskOnWorker(String queryId, String workerId, com.distributed.sql.common.models.PlanNode subplan) {
        try {
            Logger.queryDispatch(queryId, workerId);
            Tracer.addEvent(queryId, "TASK_DISPATCH", "Task dispatched to worker: " + workerId);
            
            WorkerClient client = workerClients.get(workerId);
            if (client == null) {
                return TaskResult.failure(workerId, "Worker client not found");
            }
            
            TaskRequest request = createTaskRequest(queryId, workerId, subplan);
            TaskResponse response = client.executeTask(request);
            
            if (response.getStatus() == TaskStatus.TASK_COMPLETED) {
                Tracer.addEvent(queryId, "TASK_COMPLETE", 
                              "Task completed on worker: " + workerId);
                return TaskResult.success(workerId, response.getRowsList(), 
                                        extractColumnNames(subplan));
            } else {
                return TaskResult.failure(workerId, response.getErrorMessage());
            }
            
        } catch (Exception e) {
            Logger.error("Task execution failed on worker: {}", workerId, e);
            return TaskResult.failure(workerId, e.getMessage());
        }
    }

    private Map<String, com.distributed.sql.common.models.PlanNode> createSubplans(QueryPlanner.QueryPlan plan) {
        Map<String, com.distributed.sql.common.models.PlanNode> subplans = new HashMap<>();
        
        // For simplicity, create one subplan per worker
        // In a real system, this would involve more sophisticated partitioning
        for (String workerId : plan.getWorkerIds()) {
            com.distributed.sql.common.models.PlanNode subplan = clonePlanNode(plan.getRootNode());
            subplan.setWorkerId(workerId);
            subplans.put(workerId, subplan);
        }
        
        return subplans;
    }

    private com.distributed.sql.common.models.PlanNode clonePlanNode(com.distributed.sql.common.models.PlanNode original) {
        com.distributed.sql.common.models.PlanNode clone = new com.distributed.sql.common.models.PlanNode(original.getNodeId() + "_" + original.getWorkerId(), 
                                    original.getType());
        clone.setTableName(original.getTableName());
        clone.setColumns(original.getColumns());
        clone.setConditions(original.getConditions());
        clone.setEstimatedRows(original.getEstimatedRows());
        
        for (com.distributed.sql.common.models.PlanNode child : original.getChildren()) {
            clone.addChild(clonePlanNode(child));
        }
        
        return clone;
    }

    private TaskRequest createTaskRequest(String queryId, String workerId, com.distributed.sql.common.models.PlanNode subplan) {
        // Convert PlanNode to protobuf PlanNode
        com.distributed.sql.common.proto.QueryProto.PlanNode protoNode = 
            com.distributed.sql.common.proto.QueryProto.PlanNode.newBuilder()
                .setNodeId(subplan.getNodeId())
                .setType(convertNodeType(subplan.getType()))
                .setTableName(subplan.getTableName() != null ? subplan.getTableName() : "")
                .addAllColumns(subplan.getColumns() != null ? subplan.getColumns() : new ArrayList<>())
                .setEstimatedRows(subplan.getEstimatedRows())
                .build();
        
        return com.distributed.sql.common.proto.QueryProto.TaskRequest.newBuilder()
            .setTaskId("task_" + queryId + "_" + workerId)
            .setQueryId(queryId)
            .setPlanNode(protoNode)
            .setWorkerId(workerId)
            .setTimestamp(System.currentTimeMillis())
            .build();
    }

    private com.distributed.sql.common.proto.QueryProto.NodeType convertNodeType(com.distributed.sql.common.models.PlanNode.NodeType type) {
        switch (type) {
            case SCAN: return com.distributed.sql.common.proto.QueryProto.NodeType.SCAN;
            case FILTER: return com.distributed.sql.common.proto.QueryProto.NodeType.FILTER;
            case JOIN: return com.distributed.sql.common.proto.QueryProto.NodeType.JOIN;
            case PROJECT: return com.distributed.sql.common.proto.QueryProto.NodeType.PROJECT;
            case AGGREGATE: return com.distributed.sql.common.proto.QueryProto.NodeType.AGGREGATE;
            default: return com.distributed.sql.common.proto.QueryProto.NodeType.SCAN;
        }
    }

    private List<String> extractColumnNames(com.distributed.sql.common.models.PlanNode subplan) {
        // Extract column names from the plan
        // This is simplified - in reality, this would be more complex
        if (subplan.getColumns() != null && !subplan.getColumns().contains("*")) {
            return subplan.getColumns();
        }
        
        // Default columns for our sample tables
        if ("users".equals(subplan.getTableName())) {
            return Arrays.asList("user_id", "name", "age", "email", "city", "salary");
        } else if ("orders".equals(subplan.getTableName())) {
            return Arrays.asList("order_id", "user_id", "product_name", "quantity", "price", "order_date", "status");
        }
        
        return new ArrayList<>();
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static class ExecutionException extends Exception {
        public ExecutionException(String message) {
            super(message);
        }

        public ExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Simple task result class
    public static class TaskResult {
        private final String workerId;
        private final boolean success;
        private final List<com.distributed.sql.common.proto.QueryProto.Row> rows;
        private final List<String> columnNames;
        private final String errorMessage;

        private TaskResult(String workerId, boolean success, List<com.distributed.sql.common.proto.QueryProto.Row> rows, 
                          List<String> columnNames, String errorMessage) {
            this.workerId = workerId;
            this.success = success;
            this.rows = rows;
            this.columnNames = columnNames;
            this.errorMessage = errorMessage;
        }

        public static TaskResult success(String workerId, List<com.distributed.sql.common.proto.QueryProto.Row> rows, List<String> columnNames) {
            return new TaskResult(workerId, true, rows, columnNames, null);
        }

        public static TaskResult failure(String workerId, String errorMessage) {
            return new TaskResult(workerId, false, new ArrayList<>(), new ArrayList<>(), errorMessage);
        }

        // Getters
        public String getWorkerId() { return workerId; }
        public boolean isSuccess() { return success; }
        public List<com.distributed.sql.common.proto.QueryProto.Row> getRows() { return rows; }
        public List<String> getColumnNames() { return columnNames; }
        public String getErrorMessage() { return errorMessage; }
    }

    private List<com.distributed.sql.common.models.Row> convertProtoRowsToModelRows(
            List<com.distributed.sql.common.proto.QueryProto.Row> protoRows) {
        List<com.distributed.sql.common.models.Row> modelRows = new ArrayList<>();
        
        for (com.distributed.sql.common.proto.QueryProto.Row protoRow : protoRows) {
            com.distributed.sql.common.models.Row modelRow = new com.distributed.sql.common.models.Row();
            modelRow.getValues().addAll(protoRow.getValuesList());
            
            if (protoRow.getMetadataMap().containsKey("source_table")) {
                modelRow.setSourceTable(protoRow.getMetadataMap().get("source_table"));
            }
            if (protoRow.getMetadataMap().containsKey("worker_id")) {
                modelRow.setWorkerId(protoRow.getMetadataMap().get("worker_id"));
            }
            
            modelRows.add(modelRow);
        }
        
        return modelRows;
    }
}
