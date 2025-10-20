package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;

import java.util.List;

/**
 * Query executor that executes plan nodes on local PostgreSQL shard
 */
public class QueryExecutor {

    private final DataStore dataStore;
    private final String workerId;

    public QueryExecutor(String workerId, DataStore dataStore) {
        this.workerId = workerId;
        this.dataStore = dataStore;
    }

    public ResultSet executeQuery(String sqlQuery) {
        String traceId = Tracer.startTrace("execute_query");

        try {
            AppLogger.info("Executing query on worker {}: {}", workerId, sqlQuery);

            // Execute the query using DataStore
            java.sql.ResultSet sqlResultSet = dataStore.executeQuery(sqlQuery);
            ResultSet resultSet = convertFromSqlResultSet(sqlResultSet);

            Tracer.addTimestamp("query_executed");
            AppLogger.info("Query executed successfully on worker {} in {}ms",
                    workerId, resultSet.getExecutionTimeMs());

            return resultSet;

        } catch (Exception e) {
            AppLogger.error("Error executing query on worker {}", workerId, e);

            ResultSet errorResult = new ResultSet();
            errorResult.setQueryId("error_" + System.currentTimeMillis());
            errorResult.setStatus("FAILED");
            errorResult.setExecutionTimeMs(0);
            errorResult.setTotalRows(0);

            return errorResult;

        } finally {
            Tracer.endTrace("execute_query");
        }
    }

    public ResultSet executePlanNode(PlanNode planNode) {
        String traceId = Tracer.startTrace("execute_plan_node");

        try {
            AppLogger.info("Executing plan node {} on worker {}", planNode.getNodeId(), workerId);

            switch (planNode.getType()) {
                case SCAN:
                    return executeScanNode(planNode);
                case FILTER:
                    return executeFilterNode(planNode);
                case PROJECT:
                    return executeProjectNode(planNode);
                case JOIN:
                    return executeJoinNode(planNode);
                case AGGREGATE:
                    return executeAggregateNode(planNode);
                default:
                    AppLogger.warn("Unknown plan node type: {}", planNode.getType());
                    return createEmptyResult();
            }

        } catch (Exception e) {
            AppLogger.error("Error executing plan node {} on worker {}", planNode.getNodeId(), workerId, e);
            return createEmptyResult();

        } finally {
            Tracer.endTrace("execute_plan_node");
        }
    }

    private ResultSet executeScanNode(PlanNode planNode) {
        String tableName = planNode.getTableName();
        String sqlQuery = String.format("SELECT * FROM %s", tableName);

        ResultSet resultSet = convertFromSqlResultSet(dataStore.executeQuery(sqlQuery));
        resultSet.setQueryId("scan_" + planNode.getNodeId());

        return resultSet;
    }

    private ResultSet executeFilterNode(PlanNode planNode) {
        // For simplicity, execute the filter as part of the scan
        String tableName = planNode.getTableName();
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ").append(tableName);

        if (!planNode.getConditions().isEmpty()) {
            sqlQuery.append(" WHERE ");
            for (int i = 0; i < planNode.getConditions().size(); i++) {
                if (i > 0)
                    sqlQuery.append(" AND ");

                Condition condition = planNode.getConditions().get(i);
                sqlQuery.append(condition.getColumn())
                        .append(" ")
                        .append(mapOperatorToString(condition.getOperator()))
                        .append(" '")
                        .append(condition.getValue())
                        .append("'");
            }
        }

        ResultSet resultSet = convertFromSqlResultSet(dataStore.executeQuery(sqlQuery.toString()));
        resultSet.setQueryId("filter_" + planNode.getNodeId());

        return resultSet;
    }

    private ResultSet executeProjectNode(PlanNode planNode) {
        // Projection is handled by modifying the SELECT clause
        String tableName = planNode.getTableName();
        StringBuilder sqlQuery = new StringBuilder("SELECT ");

        if (planNode.getColumns().isEmpty() || planNode.getColumns().contains("*")) {
            sqlQuery.append("*");
        } else {
            for (int i = 0; i < planNode.getColumns().size(); i++) {
                if (i > 0)
                    sqlQuery.append(", ");
                sqlQuery.append(planNode.getColumns().get(i));
            }
        }

        sqlQuery.append(" FROM ").append(tableName);

        ResultSet resultSet = convertFromSqlResultSet(dataStore.executeQuery(sqlQuery.toString()));
        resultSet.setQueryId("project_" + planNode.getNodeId());

        return resultSet;
    }

    private ResultSet executeJoinNode(PlanNode planNode) {
        // For simplicity, return mock join results
        ResultSet resultSet = new ResultSet();
        resultSet.setQueryId("join_" + planNode.getNodeId());
        resultSet.setColumns(List.of("user_name", "order_id", "product_name"));
        resultSet.addRow(new Row(List.of("John Doe", "ORD001", "Laptop Pro")));
        resultSet.addRow(new Row(List.of("Jane Smith", "ORD002", "Wireless Mouse")));
        resultSet.setTotalRows(2);
        resultSet.setExecutionTimeMs(75);
        resultSet.setStatus("COMPLETED");

        return resultSet;
    }

    private ResultSet executeAggregateNode(PlanNode planNode) {
        // For simplicity, return mock aggregate results
        ResultSet resultSet = new ResultSet();
        resultSet.setQueryId("aggregate_" + planNode.getNodeId());
        resultSet.setColumns(List.of("count"));
        resultSet.addRow(new Row(List.of("26")));
        resultSet.setTotalRows(1);
        resultSet.setExecutionTimeMs(25);
        resultSet.setStatus("COMPLETED");

        return resultSet;
    }

    private String mapOperatorToString(Operator operator) {
        switch (operator) {
            case EQUALS:
                return "=";
            case NOT_EQUALS:
                return "!=";
            case GREATER_THAN:
                return ">";
            case LESS_THAN:
                return "<";
            case GREATER_THAN_EQUALS:
                return ">=";
            case LESS_THAN_EQUALS:
                return "<=";
            case LIKE:
                return "LIKE";
            case IN:
                return "IN";
            default:
                return "=";
        }
    }

    private ResultSet createEmptyResult() {
        ResultSet resultSet = new ResultSet();
        resultSet.setQueryId("empty_" + System.currentTimeMillis());
        resultSet.setStatus("FAILED");
        resultSet.setExecutionTimeMs(0);
        resultSet.setTotalRows(0);
        return resultSet;
    }

    private ResultSet convertFromSqlResultSet(java.sql.ResultSet sqlResultSet) {
        // For demo purposes, return mock data since we're not actually executing SQL
        // In a real implementation, this would convert java.sql.ResultSet to our
        // ResultSet
        ResultSet resultSet = new ResultSet();
        resultSet.setQueryId("query_" + System.currentTimeMillis());
        resultSet.setStatus("SUCCESS");
        resultSet.setExecutionTimeMs(150);
        resultSet.setTotalRows(3);

        // Add mock columns and rows
        resultSet.setColumns(List.of("id", "name", "value"));
        resultSet.addRow(new Row(List.of("1", "Sample Data 1", "100")));
        resultSet.addRow(new Row(List.of("2", "Sample Data 2", "200")));
        resultSet.addRow(new Row(List.of("3", "Sample Data 3", "300")));

        return resultSet;
    }
}
