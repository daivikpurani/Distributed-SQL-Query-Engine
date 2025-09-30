package com.distributed.sql.common.models;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a parsed SQL query with its components
 */
public class Query {
    private String queryId;
    private String originalSql;
    private QueryType type;
    private List<String> selectColumns;
    private List<String> fromTables;
    private List<Condition> whereConditions;
    private List<Join> joins;
    private Map<String, Object> metadata;
    private long timestamp;

    public Query() {
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public Query(String queryId, String originalSql) {
        this();
        this.queryId = queryId;
        this.originalSql = originalSql;
    }

    // Getters and setters
    public String getQueryId() { return queryId; }
    public void setQueryId(String queryId) { this.queryId = queryId; }

    public String getOriginalSql() { return originalSql; }
    public void setOriginalSql(String originalSql) { this.originalSql = originalSql; }

    public QueryType getType() { return type; }
    public void setType(QueryType type) { this.type = type; }

    public List<String> getSelectColumns() { return selectColumns; }
    public void setSelectColumns(List<String> selectColumns) { this.selectColumns = selectColumns; }

    public List<String> getFromTables() { return fromTables; }
    public void setFromTables(List<String> fromTables) { this.fromTables = fromTables; }

    public List<Condition> getWhereConditions() { return whereConditions; }
    public void setWhereConditions(List<Condition> whereConditions) { this.whereConditions = whereConditions; }

    public List<Join> getJoins() { return joins; }
    public void setJoins(List<Join> joins) { this.joins = joins; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("Query{id='%s', sql='%s', type=%s}", 
                           queryId, originalSql, type);
    }

    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE, CREATE, DROP
    }
}
