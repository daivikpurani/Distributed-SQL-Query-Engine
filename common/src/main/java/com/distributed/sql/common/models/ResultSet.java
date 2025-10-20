package com.distributed.sql.common.models;

import java.util.*;

/**
 * Represents a result set from query execution
 */
public class ResultSet {
    private String queryId;
    private List<String> columns;
    private List<Row> rows;
    private long executionTimeMs;
    private int totalRows;
    private String status;

    public ResultSet() {
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public ResultSet(String queryId, List<String> columns) {
        this.queryId = queryId;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public void addRow(Row row) {
        this.rows.add(row);
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    // Getters and Setters
    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
