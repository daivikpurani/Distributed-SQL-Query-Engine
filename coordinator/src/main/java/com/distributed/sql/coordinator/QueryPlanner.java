package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;

import java.util.*;

/**
 * Query planner that creates execution plans with shard-aware optimization
 */
public class QueryPlanner {

    private final ShardManager shardManager;

    public QueryPlanner(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public QueryPlan createExecutionPlan(Query query) {
        String traceId = Tracer.startTrace("create_execution_plan");

        try {
            QueryPlan plan = new QueryPlan();
            plan.setQueryId(query.getQueryId());
            plan.setSqlQuery(query.getSql());

            // Create root plan node
            PlanNode rootNode = createPlanNode(query);
            plan.setRootNode(rootNode);

            // Determine which workers to involve
            List<String> workerIds = determineWorkers(query);
            plan.setWorkerIds(workerIds);

            // Estimate execution cost
            double estimatedCost = estimateCost(query, rootNode);
            plan.setEstimatedCost(estimatedCost);

            Tracer.addTimestamp("plan_created");
            AppLogger.info("Created execution plan for query: {} with {} workers",
                    query.getQueryId(), workerIds.size());

            return plan;

        } finally {
            Tracer.endTrace("create_execution_plan");
        }
    }

    private PlanNode createPlanNode(Query query) {
        PlanNode rootNode = new PlanNode("root", NodeType.SCAN);

        // Add table scan nodes for each table
        for (String tableName : query.getFromTables()) {
            PlanNode scanNode = new PlanNode("scan_" + tableName, NodeType.SCAN);
            scanNode.setTableName(tableName);
            scanNode.setColumns(new ArrayList<>(query.getSelectColumns()));

            // Add filter node if there are WHERE conditions
            if (query.hasConditions()) {
                PlanNode filterNode = new PlanNode("filter_" + tableName, NodeType.FILTER);
                filterNode.setConditions(new ArrayList<>(query.getWhereConditions()));
                filterNode.addChild(scanNode);
                rootNode.addChild(filterNode);
            } else {
                rootNode.addChild(scanNode);
            }
        }

        // Add JOIN nodes if there are joins
        if (query.hasJoins()) {
            for (Join join : query.getJoins()) {
                PlanNode joinNode = new PlanNode("join_" + join.getRightTable(), NodeType.JOIN);
                joinNode.setTableName(join.getRightTable());
                rootNode.addChild(joinNode);
            }
        }

        // Add projection node
        PlanNode projectNode = new PlanNode("project", NodeType.PROJECT);
        projectNode.setColumns(new ArrayList<>(query.getSelectColumns()));
        projectNode.addChild(rootNode);

        return projectNode;
    }

    private List<String> determineWorkers(Query query) {
        Set<String> workers = new HashSet<>();

        // For each table in the query, find which workers have shards
        for (String tableName : query.getFromTables()) {
            List<ShardInfo> shards = shardManager.getShardsForTable(tableName);
            for (ShardInfo shard : shards) {
                workers.add(shard.getWorkerId());
            }
        }

        return new ArrayList<>(workers);
    }

    private double estimateCost(Query query, PlanNode rootNode) {
        double cost = 0.0;

        // Base cost for query parsing and planning
        cost += 10.0;

        // Cost for each table scan
        for (String tableName : query.getFromTables()) {
            List<ShardInfo> shards = shardManager.getShardsForTable(tableName);
            for (ShardInfo shard : shards) {
                cost += shard.getRowCount() * 0.1; // 0.1 cost per row
            }
        }

        // Additional cost for WHERE conditions
        if (query.hasConditions()) {
            cost += query.getWhereConditions().size() * 5.0;
        }

        // Additional cost for JOINs
        if (query.hasJoins()) {
            cost += query.getJoins().size() * 20.0;
        }

        return cost;
    }
}
