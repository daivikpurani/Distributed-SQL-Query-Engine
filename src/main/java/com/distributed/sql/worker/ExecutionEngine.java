package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.utils.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Executes query plan nodes on worker nodes
 */
public class ExecutionEngine {
    private final String workerId;
    private final String dataDirectory;
    private final Map<String, List<com.distributed.sql.common.models.Row>> tableCache;
    private final Random random;

    public ExecutionEngine(String workerId, String dataDirectory) {
        this.workerId = workerId;
        this.dataDirectory = dataDirectory;
        this.tableCache = new HashMap<>();
        this.random = new Random();
        
        // Load sample data
        loadTableData("users");
        loadTableData("orders");
    }

    /**
     * Execute a plan node and return results
     */
    public List<com.distributed.sql.common.models.Row> executePlanNode(com.distributed.sql.common.models.PlanNode planNode) throws ExecutionException {
        Logger.debug("Executing plan node: {} on worker: {}", planNode.getNodeId(), workerId);
        
        try {
            switch (planNode.getType()) {
                case SCAN:
                    return executeScan(planNode);
                case FILTER:
                    return executeFilter(planNode);
                case PROJECT:
                    return executeProject(planNode);
                case JOIN:
                    return executeJoin(planNode);
                default:
                    throw new ExecutionException("Unsupported node type: " + planNode.getType());
            }
        } catch (Exception e) {
            Logger.error("Execution failed for node: {} on worker: {}", 
                        planNode.getNodeId(), workerId, e);
            throw new ExecutionException("Execution failed: " + e.getMessage(), e);
        }
    }

    private List<com.distributed.sql.common.models.Row> executeScan(com.distributed.sql.common.models.PlanNode scanNode) throws ExecutionException {
        String tableName = scanNode.getTableName();
        List<String> columns = scanNode.getColumns();
        
        Logger.debug("Executing scan on table: {} for columns: {}", tableName, columns);
        
        List<com.distributed.sql.common.models.Row> tableData = tableCache.get(tableName);
        if (tableData == null) {
            throw new ExecutionException("Table not found: " + tableName);
        }
        
        // Simulate partial data on this worker (partitioning simulation)
        List<com.distributed.sql.common.models.Row> workerData = simulatePartitioning(tableData, tableName);
        
        // Apply column projection if specified
        if (columns != null && !columns.contains("*")) {
            return projectColumns(workerData, columns, tableName);
        }
        
        return workerData;
    }

    private List<com.distributed.sql.common.models.Row> executeFilter(com.distributed.sql.common.models.PlanNode filterNode) throws ExecutionException {
        List<com.distributed.sql.common.models.Condition> conditions = filterNode.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            throw new ExecutionException("Filter node has no conditions");
        }
        
        // Get data from child nodes
        List<com.distributed.sql.common.models.Row> inputData = new ArrayList<>();
        for (com.distributed.sql.common.models.PlanNode child : filterNode.getChildren()) {
            inputData.addAll(executePlanNode(child));
        }
        
        Logger.debug("Applying filter with {} conditions to {} rows", 
                    conditions.size(), inputData.size());
        
        // Apply filter conditions
        List<com.distributed.sql.common.models.Row> filteredData = inputData.stream()
            .filter(row -> evaluateConditions(row, conditions))
            .collect(Collectors.toList());
        
        Logger.debug("Filter result: {} rows", filteredData.size());
        return filteredData;
    }

    private List<com.distributed.sql.common.models.Row> executeProject(com.distributed.sql.common.models.PlanNode projectNode) throws ExecutionException {
        List<String> columns = projectNode.getColumns();
        if (columns == null || columns.isEmpty()) {
            throw new ExecutionException("Project node has no columns specified");
        }
        
        // Get data from child nodes
        List<com.distributed.sql.common.models.Row> inputData = new ArrayList<>();
        for (com.distributed.sql.common.models.PlanNode child : projectNode.getChildren()) {
            inputData.addAll(executePlanNode(child));
        }
        
        Logger.debug("Applying projection for columns: {} to {} rows", 
                    columns, inputData.size());
        
        return projectColumns(inputData, columns, null);
    }

    private List<com.distributed.sql.common.models.Row> executeJoin(com.distributed.sql.common.models.PlanNode joinNode) throws ExecutionException {
        if (joinNode.getChildren().size() != 2) {
            throw new ExecutionException("Join node must have exactly 2 children");
        }
        
        com.distributed.sql.common.models.PlanNode leftChild = joinNode.getChildren().get(0);
        com.distributed.sql.common.models.PlanNode rightChild = joinNode.getChildren().get(1);
        
        List<com.distributed.sql.common.models.Row> leftData = executePlanNode(leftChild);
        List<com.distributed.sql.common.models.Row> rightData = executePlanNode(rightChild);
        
        Logger.debug("Executing join: {} rows x {} rows", leftData.size(), rightData.size());
        
        // Simple nested loop join (for demonstration)
        List<com.distributed.sql.common.models.Row> joinResult = new ArrayList<>();
        for (com.distributed.sql.common.models.Row leftRow : leftData) {
            for (com.distributed.sql.common.models.Row rightRow : rightData) {
                // Simple join condition: assume user_id matches
                if (leftRow.getValue(0).equals(rightRow.getValue(1))) { // user_id = user_id
                    com.distributed.sql.common.models.Row joinedRow = new com.distributed.sql.common.models.Row();
                    joinedRow.getValues().addAll(leftRow.getValues());
                    joinedRow.getValues().addAll(rightRow.getValues());
                    joinedRow.setSourceTable(leftRow.getSourceTable() + "_" + rightRow.getSourceTable());
                    joinedRow.setWorkerId(workerId);
                    joinResult.add(joinedRow);
                }
            }
        }
        
        Logger.debug("Join result: {} rows", joinResult.size());
        return joinResult;
    }

    private List<com.distributed.sql.common.models.Row> projectColumns(List<com.distributed.sql.common.models.Row> data, List<String> columns, String sourceTable) {
        // This is simplified - in reality, column mapping would be more complex
        List<com.distributed.sql.common.models.Row> projectedData = new ArrayList<>();
        
        for (com.distributed.sql.common.models.Row row : data) {
            com.distributed.sql.common.models.Row projectedRow = new com.distributed.sql.common.models.Row();
            projectedRow.setSourceTable(sourceTable);
            projectedRow.setWorkerId(workerId);
            
            // For simplicity, just take the first N columns where N = columns.size()
            for (int i = 0; i < Math.min(columns.size(), row.getColumnCount()); i++) {
                projectedRow.addValue(row.getValue(i));
            }
            
            projectedData.add(projectedRow);
        }
        
        return projectedData;
    }

    private boolean evaluateConditions(com.distributed.sql.common.models.Row row, List<com.distributed.sql.common.models.Condition> conditions) {
        for (com.distributed.sql.common.models.Condition condition : conditions) {
            if (!evaluateCondition(row, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(com.distributed.sql.common.models.Row row, com.distributed.sql.common.models.Condition condition) {
        String columnValue = getColumnValue(row, condition.getColumn());
        if (columnValue == null) {
            return false;
        }
        
        String conditionValue = condition.getValue();
        
        switch (condition.getOperator()) {
            case EQUALS:
                return columnValue.equals(conditionValue);
            case NOT_EQUALS:
                return !columnValue.equals(conditionValue);
            case GREATER_THAN:
                return compareValues(columnValue, conditionValue) > 0;
            case LESS_THAN:
                return compareValues(columnValue, conditionValue) < 0;
            case GREATER_THAN_EQUALS:
                return compareValues(columnValue, conditionValue) >= 0;
            case LESS_THAN_EQUALS:
                return compareValues(columnValue, conditionValue) <= 0;
            default:
                return false;
        }
    }

    private String getColumnValue(com.distributed.sql.common.models.Row row, String columnName) {
        // Simplified column value extraction
        // In reality, this would use proper column mapping
        switch (columnName.toLowerCase()) {
            case "user_id":
            case "id":
                return row.getValue(0);
            case "name":
                return row.getValue(1);
            case "age":
                return row.getValue(2);
            case "email":
                return row.getValue(3);
            case "city":
                return row.getValue(4);
            case "salary":
                return row.getValue(5);
            case "order_id":
                return row.getValue(0);
            case "product_name":
                return row.getValue(2);
            case "quantity":
                return row.getValue(3);
            case "price":
                return row.getValue(4);
            case "order_date":
                return row.getValue(5);
            case "status":
                return row.getValue(6);
            default:
                return null;
        }
    }

    private int compareValues(String value1, String value2) {
        try {
            // Try numeric comparison first
            double num1 = Double.parseDouble(value1);
            double num2 = Double.parseDouble(value2);
            return Double.compare(num1, num2);
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return value1.compareTo(value2);
        }
    }

    private List<com.distributed.sql.common.models.Row> simulatePartitioning(List<com.distributed.sql.common.models.Row> fullData, String tableName) {
        // Simulate data partitioning across workers
        // Each worker gets a subset of the data
        int partitionSize = fullData.size() / 3; // Assume 3 workers
        int workerIndex = getWorkerIndex();
        
        int startIndex = workerIndex * partitionSize;
        int endIndex = Math.min(startIndex + partitionSize, fullData.size());
        
        if (workerIndex == 2) { // Last worker gets remaining data
            endIndex = fullData.size();
        }
        
        List<com.distributed.sql.common.models.Row> partition = fullData.subList(startIndex, endIndex);
        
        // Set worker ID for all rows in this partition
        for (com.distributed.sql.common.models.Row row : partition) {
            row.setWorkerId(workerId);
        }
        
        Logger.debug("Worker {} partition for table {}: {} rows (indices {}-{})", 
                    workerId, tableName, partition.size(), startIndex, endIndex - 1);
        
        return partition;
    }

    private int getWorkerIndex() {
        // Simple worker index calculation based on worker ID
        if (workerId.contains("1")) return 0;
        if (workerId.contains("2")) return 1;
        return 2; // Default to worker 3
    }

    private void loadTableData(String tableName) {
        try {
            String filePath = dataDirectory + "/" + tableName + ".csv";
            List<com.distributed.sql.common.models.Row> rows = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean firstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue; // Skip header
                    }
                    
                    String[] values = line.split(",");
                    com.distributed.sql.common.models.Row row = new com.distributed.sql.common.models.Row(Arrays.asList(values), tableName);
                    row.setWorkerId(workerId);
                    rows.add(row);
                }
            }
            
            tableCache.put(tableName, rows);
            Logger.info("Loaded {} rows for table: {} on worker: {}", 
                       rows.size(), tableName, workerId);
            
        } catch (IOException e) {
            Logger.error("Failed to load table data: {} on worker: {}", tableName, workerId, e);
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
}
