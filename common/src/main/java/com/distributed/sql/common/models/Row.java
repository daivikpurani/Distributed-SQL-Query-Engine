package com.distributed.sql.common.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a row of data in query results
 */
public class Row {
    private List<String> values;
    private Map<String, String> metadata;

    public Row() {
        this.values = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public Row(List<String> values) {
        this.values = new ArrayList<>(values);
        this.metadata = new HashMap<>();
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    // Getters and Setters
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
