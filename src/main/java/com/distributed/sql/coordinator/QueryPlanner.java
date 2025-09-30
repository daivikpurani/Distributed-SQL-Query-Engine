package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.utils.Logger;
import com.distributed.sql.utils.Tracer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rule-based query planner that converts parsed queries into execution plans
 */
public class QueryPlanner {
    private final List<String> availableWorkers;
    private final Map<String, Integer> tableRowCounts;

    public QueryPlanner(List<String> availableWorkers) {
        this.availableWorkers = new ArrayList<>(availableWorkers);
        this.tableRowCounts = new HashMap<>();
        
        // Initialize with estimated row counts for our sample tables
        tableRowCounts.put("users", 20);
        tableRowCounts.put("orders", 20);
    }

    /**
     * Create an execution plan for a query
     */
    public QueryPlan createPlan(Query query) throws PlanningException {
        String planId = "plan_" + query.getQueryId();
        long startTime = System.currentTimeMillis();
        
        Logger.info("Creating execution plan for query: {}", query.getQueryId());
        Tracer.addEvent(query.getQueryId(), "PLANNING_START", "Query planning started");

        try {
            PlanNode rootNode = buildPlanTree(query);
            QueryPlan plan = new QueryPlan(planId, rootNode, availableWorkers);
            
            long endTime = System.currentTimeMillis();
            plan.setPlanTimeMs(endTime - startTime);
            
            Tracer.addEvent(query.getQueryId(), "PLANNING_COMPLETE", 
                          "Plan created with " + plan.getWorkerIds().size() + " workers");
            Tracer.recordTiming(query.getQueryId(), "PLANNING", startTime, endTime);
            
            Logger.queryPlan(query.getQueryId(), plan.toString());
            return plan;
            
        } catch (Exception e) {
            Logger.error("Failed to create plan for query: {}", query.getQueryId(), e);
            throw new PlanningException("Failed to create execution plan: " + e.getMessage(), e);
        }
    }

    private PlanNode buildPlanTree(Query query) throws PlanningException {
        PlanNode root = null;
        
        // Build the plan tree bottom-up
        List<String> tables = query.getFromTables();
        
        if (tables.size() == 1) {
            // Single table query
            root = createScanNode(tables.get(0), query.getSelectColumns());
        } else if (tables.size() == 2) {
            // Two table join
            root = createJoinPlan(query);
        } else {
            throw new PlanningException("Multi-table joins (>2 tables) not supported yet");
        }
        
        // Add filter node if WHERE conditions exist
        if (query.getWhereConditions() != null && !query.getWhereConditions().isEmpty()) {
            PlanNode filterNode = new PlanNode("filter_" + UUID.randomUUID().toString().substring(0, 8), 
                                             PlanNode.NodeType.FILTER);
            filterNode.setConditions(query.getWhereConditions());
            filterNode.addChild(root);
            root = filterNode;
        }
        
        // Add projection node if specific columns are selected
        if (query.getSelectColumns() != null && 
            !query.getSelectColumns().contains("*") && 
            !query.getSelectColumns().isEmpty()) {
            PlanNode projectNode = new PlanNode("project_" + UUID.randomUUID().toString().substring(0, 8), 
                                              PlanNode.NodeType.PROJECT);
            projectNode.setColumns(query.getSelectColumns());
            projectNode.addChild(root);
            root = projectNode;
        }
        
        return root;
    }

    private PlanNode createScanNode(String tableName, List<String> columns) {
        PlanNode scanNode = new PlanNode("scan_" + tableName, PlanNode.NodeType.SCAN);
        scanNode.setTableName(tableName);
        scanNode.setColumns(columns);
        scanNode.setEstimatedRows(tableRowCounts.getOrDefault(tableName, 100));
        
        // Assign to a random worker
        String workerId = availableWorkers.get(ThreadLocalRandom.current().nextInt(availableWorkers.size()));
        scanNode.setWorkerId(workerId);
        
        return scanNode;
    }

    private PlanNode createJoinPlan(Query query) throws PlanningException {
        if (query.getJoins() == null || query.getJoins().isEmpty()) {
            throw new PlanningException("Join query must specify JOIN conditions");
        }
        
        Join join = query.getJoins().get(0); // Support only first join for now
        
        // Create scan nodes for both tables
        PlanNode leftScan = createScanNode(join.getLeftTable(), query.getSelectColumns());
        PlanNode rightScan = createScanNode(join.getRightTable(), query.getSelectColumns());
        
        // Create join node
        PlanNode joinNode = new PlanNode("join_" + UUID.randomUUID().toString().substring(0, 8), 
                                       PlanNode.NodeType.JOIN);
        joinNode.setTableName(join.getLeftTable() + "_" + join.getRightTable());
        
        // Estimate join result size (simplified)
        int leftRows = tableRowCounts.getOrDefault(join.getLeftTable(), 100);
        int rightRows = tableRowCounts.getOrDefault(join.getRightTable(), 100);
        joinNode.setEstimatedRows(Math.min(leftRows, rightRows));
        
        joinNode.addChild(leftScan);
        joinNode.addChild(rightScan);
        
        return joinNode;
    }

    /**
     * Break down a plan into subplans for each worker
     */
    public Map<String, PlanNode> createSubplans(QueryPlan plan) {
        Map<String, PlanNode> subplans = new HashMap<>();
        
        // For simplicity, we'll create one subplan per worker
        // In a real system, this would be more sophisticated
        for (String workerId : plan.getWorkerIds()) {
            PlanNode subplan = clonePlanNode(plan.getRootNode());
            subplan.setWorkerId(workerId);
            subplans.put(workerId, subplan);
        }
        
        return subplans;
    }

    private PlanNode clonePlanNode(PlanNode original) {
        PlanNode clone = new PlanNode(original.getNodeId() + "_clone", original.getType());
        clone.setTableName(original.getTableName());
        clone.setColumns(original.getColumns());
        clone.setConditions(original.getConditions());
        clone.setEstimatedRows(original.getEstimatedRows());
        
        for (PlanNode child : original.getChildren()) {
            clone.addChild(clonePlanNode(child));
        }
        
        return clone;
    }

    public static class QueryPlan {
        private String planId;
        private PlanNode rootNode;
        private List<String> workerIds;
        private long planTimeMs;

        public QueryPlan(String planId, PlanNode rootNode, List<String> workerIds) {
            this.planId = planId;
            this.rootNode = rootNode;
            this.workerIds = workerIds;
        }

        // Getters and setters
        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }

        public PlanNode getRootNode() { return rootNode; }
        public void setRootNode(PlanNode rootNode) { this.rootNode = rootNode; }

        public List<String> getWorkerIds() { return workerIds; }
        public void setWorkerIds(List<String> workerIds) { this.workerIds = workerIds; }

        public long getPlanTimeMs() { return planTimeMs; }
        public void setPlanTimeMs(long planTimeMs) { this.planTimeMs = planTimeMs; }

        @Override
        public String toString() {
            return String.format("QueryPlan{id='%s', workers=%d, planTime=%dms}", 
                               planId, workerIds.size(), planTimeMs);
        }
    }

    public static class PlanningException extends Exception {
        public PlanningException(String message) {
            super(message);
        }

        public PlanningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
