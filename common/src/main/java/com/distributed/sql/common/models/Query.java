package com.distributed.sql.common.models;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a SQL query with all its components
 */
public class Query {
    private String queryId;
    private String sql;
    private QueryType queryType;
    private List<String> selectColumns;
    private List<String> fromTables;
    private List<Condition> whereConditions;
    private List<Join> joins;
    private Map<String, String> metadata;

    public Query() {
        this.selectColumns = new ArrayList<>();
        this.fromTables = new ArrayList<>();
        this.whereConditions = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public Query(String sql, QueryType queryType) {
        this();
        this.queryId = "query_" + System.currentTimeMillis();
        this.sql = sql;
        this.queryType = queryType;
    }

    public Query(String queryId, String sql, QueryType queryType) {
        this();
        this.queryId = queryId;
        this.sql = sql;
        this.queryType = queryType;
    }

    public void addCondition(Condition condition) {
        this.whereConditions.add(condition);
    }

    public void addJoin(Join join) {
        this.joins.add(join);
    }

    public boolean isSelect() {
        return queryType == QueryType.SELECT;
    }

    public boolean hasConditions() {
        return !whereConditions.isEmpty();
    }

    public boolean hasJoins() {
        return !joins.isEmpty();
    }

    // Getters and Setters
    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public List<String> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<String> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public List<String> getFromTables() {
        return fromTables;
    }

    public void setFromTables(List<String> fromTables) {
        this.fromTables = fromTables;
    }

    public List<Condition> getWhereConditions() {
        return whereConditions;
    }

    public void setWhereConditions(List<Condition> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
