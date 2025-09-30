package com.distributed.sql.common.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the result set from a query execution
 */
public class ResultSet {
    private String queryId;
    private List<String> columnNames;
    private List<Row> rows;
    private Map<String, Object> metadata;
    private long executionTimeMs;
    private int totalRows;

    public ResultSet() {
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.executionTimeMs = 0;
        this.totalRows = 0;
    }

    public ResultSet(String queryId) {
        this();
        this.queryId = queryId;
    }

    // Getters and setters
    public String getQueryId() { return queryId; }
    public void setQueryId(String queryId) { this.queryId = queryId; }

    public List<String> getColumnNames() { return columnNames; }
    public void setColumnNames(List<String> columnNames) { this.columnNames = columnNames; }

    public List<Row> getRows() { return rows; }
    public void setRows(List<Row> rows) { this.rows = rows; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public void addRow(Row row) {
        this.rows.add(row);
        this.totalRows++;
    }

    public void addRows(List<Row> rows) {
        this.rows.addAll(rows);
        this.totalRows += rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("ResultSet{queryId='%s', rows=%d, executionTime=%dms}", 
                           queryId, totalRows, executionTimeMs);
    }
}
