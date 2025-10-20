package com.distributed.sql.common.models;

import java.util.*;

/**
 * Represents a query execution plan node
 */
public class PlanNode {
    private String nodeId;
    private NodeType type;
    private String tableName;
    private List<String> columns;
    private List<Condition> conditions;
    private List<PlanNode> children;
    private int estimatedRows;

    public PlanNode() {
        this.columns = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public PlanNode(String nodeId, NodeType type) {
        this();
        this.nodeId = nodeId;
        this.type = type;
    }

    public void addChild(PlanNode child) {
        this.children.add(child);
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    // Getters and Setters
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<PlanNode> getChildren() {
        return children;
    }

    public void setChildren(List<PlanNode> children) {
        this.children = children;
    }

    public int getEstimatedRows() {
        return estimatedRows;
    }

    public void setEstimatedRows(int estimatedRows) {
        this.estimatedRows = estimatedRows;
    }
}
