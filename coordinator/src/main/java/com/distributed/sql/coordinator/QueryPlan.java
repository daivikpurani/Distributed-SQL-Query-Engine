package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.PlanNode;

import java.util.List;

/**
 * Represents a query execution plan
 */
public class QueryPlan {
    private String queryId;
    private String sqlQuery;
    private PlanNode rootNode;
    private List<String> workerIds;
    private double estimatedCost;
    private long planTimeMs;

    public QueryPlan() {
    }

    public QueryPlan(String queryId, String sqlQuery) {
        this.queryId = queryId;
        this.sqlQuery = sqlQuery;
        this.planTimeMs = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public PlanNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(PlanNode rootNode) {
        this.rootNode = rootNode;
    }

    public List<String> getWorkerIds() {
        return workerIds;
    }

    public void setWorkerIds(List<String> workerIds) {
        this.workerIds = workerIds;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public long getPlanTimeMs() {
        return planTimeMs;
    }

    public void setPlanTimeMs(long planTimeMs) {
        this.planTimeMs = planTimeMs;
    }
}
