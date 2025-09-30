package com.distributed.sql.common.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a single row of data in a result set
 */
public class Row {
    private List<String> values;
    private Map<String, String> metadata;
    private String sourceTable;
    private String workerId;

    public Row() {
        this.values = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public Row(List<String> values) {
        this();
        this.values = new ArrayList<>(values);
    }

    public Row(List<String> values, String sourceTable) {
        this(values);
        this.sourceTable = sourceTable;
    }

    // Getters and setters
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getValue(int index) {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    public void setValue(int index, String value) {
        if (index >= 0 && index < values.size()) {
            values.set(index, value);
        }
    }

    public void addValue(String value) {
        values.add(value);
    }

    public int getColumnCount() {
        return values.size();
    }

    @Override
    public String toString() {
        return String.format("Row{values=%s, source='%s', worker='%s'}", 
                           values, sourceTable, workerId);
    }
}
