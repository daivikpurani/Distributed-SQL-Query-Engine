package com.distributed.sql.common.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a node in the query execution plan tree
 */
public class PlanNode {
    private String nodeId;
    private NodeType type;
    private String tableName;
    private List<String> columns;
    private List<Condition> conditions;
    private List<PlanNode> children;
    private Map<String, Object> metadata;
    private int estimatedRows;
    private String workerId;

    public PlanNode() {
        this.children = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.estimatedRows = 0;
    }

    public PlanNode(String nodeId, NodeType type) {
        this();
        this.nodeId = nodeId;
        this.type = type;
    }

    // Getters and setters
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<Condition> getConditions() { return conditions; }
    public void setConditions(List<Condition> conditions) { this.conditions = conditions; }

    public List<PlanNode> getChildren() { return children; }
    public void setChildren(List<PlanNode> children) { this.children = children; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public int getEstimatedRows() { return estimatedRows; }
    public void setEstimatedRows(int estimatedRows) { this.estimatedRows = estimatedRows; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public void addChild(PlanNode child) {
        this.children.add(child);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("PlanNode{id='%s', type=%s, table='%s', estimatedRows=%d}", 
                           nodeId, type, tableName, estimatedRows);
    }

    public enum NodeType {
        SCAN,           // Table scan operation
        FILTER,         // WHERE condition filtering
        JOIN,           // Join operation
        PROJECT,        // Column selection
        AGGREGATE,      // GROUP BY aggregation
        SORT,           // ORDER BY sorting
        LIMIT           // LIMIT operation
    }
}
