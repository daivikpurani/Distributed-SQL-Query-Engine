package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.proto.CoordinatorServiceGrpc;
import com.distributed.sql.utils.Logger;
import com.distributed.sql.utils.Tracer;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinator server that handles query requests and coordinates execution
 */
public class CoordinatorServer {
    private final int port;
    private Server server;
    private final QueryPlanner queryPlanner;
    private final QueryExecutor queryExecutor;
    private final Map<String, WorkerClient> workerClients;
    private final Map<String, String> workerAddresses;

    public CoordinatorServer(int port, Map<String, String> workerAddresses) {
        this.port = port;
        this.workerAddresses = workerAddresses;
        this.workerClients = new ConcurrentHashMap<>();
        this.queryPlanner = new QueryPlanner(new ArrayList<>(workerAddresses.keySet()));
        this.queryExecutor = new QueryExecutor(workerClients);
        
        initializeWorkerClients();
    }

    private void initializeWorkerClients() {
        for (Map.Entry<String, String> entry : workerAddresses.entrySet()) {
            String workerId = entry.getKey();
            String address = entry.getValue();
            
            try {
                WorkerClient client = new WorkerClient(address);
                workerClients.put(workerId, client);
                Logger.info("Connected to worker: {} at {}", workerId, address);
            } catch (Exception e) {
                Logger.error("Failed to connect to worker: {} at {}", workerId, address, e);
            }
        }
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
            .addService(new CoordinatorServiceImpl())
            .build()
            .start();
        
        Logger.info("Coordinator server started on port: {}", port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Shutting down coordinator server...");
            CoordinatorServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
            queryExecutor.shutdown();
            
            // Close worker connections
            for (WorkerClient client : workerClients.values()) {
                client.shutdown();
            }
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Implementation of the CoordinatorService gRPC service
     */
    private class CoordinatorServiceImpl extends CoordinatorServiceGrpc.CoordinatorServiceImplBase {
        
        @Override
        public void executeQuery(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
            String queryId = request.getQueryId();
            String sql = request.getSqlQuery();
            
            Logger.queryStart(queryId, sql);
            Tracer.startQueryTrace(queryId, sql);
            
            try {
                // Parse the SQL query
                Query query = SQLParser.parse(sql);
                query.setQueryId(queryId);
                
                Tracer.addEvent(queryId, "PARSE_COMPLETE", "SQL query parsed successfully");
                
                // Create execution plan
                QueryPlanner.QueryPlan plan = queryPlanner.createPlan(query);
                
                Tracer.addEvent(queryId, "PLAN_CREATED", "Execution plan created");
                
                // Execute the plan
                ResultSet resultSet = queryExecutor.executePlan(query, plan);
                
                // Convert result set to protobuf response
                QueryResponse.Builder responseBuilder = QueryResponse.newBuilder()
                    .setQueryId(queryId)
                    .setStatus(QueryStatus.COMPLETED)
                    .setExecutionTimeMs(resultSet.getExecutionTimeMs());
                
                // Add rows to response
                for (com.distributed.sql.common.models.Row row : resultSet.getRows()) {
                    com.distributed.sql.common.proto.QueryProto.Row.Builder rowBuilder = 
                        com.distributed.sql.common.proto.QueryProto.Row.newBuilder();
                    
                    for (String value : row.getValues()) {
                        rowBuilder.addValues(value);
                    }
                    
                    if (row.getSourceTable() != null) {
                        rowBuilder.putMetadata("source_table", row.getSourceTable());
                    }
                    if (row.getWorkerId() != null) {
                        rowBuilder.putMetadata("worker_id", row.getWorkerId());
                    }
                    
                    responseBuilder.addRows(rowBuilder.build());
                }
                
                // Add query plan to response
                QueryPlan.Builder planBuilder = QueryPlan.newBuilder()
                    .setQueryId(queryId)
                    .setPlanTimeMs(plan.getPlanTimeMs());
                
                // Convert plan node to protobuf
                com.distributed.sql.common.proto.QueryProto.PlanNode protoNode = 
                    convertPlanNodeToProto(plan.getRootNode());
                planBuilder.setRootNode(protoNode);
                
                planBuilder.addAllWorkerIds(plan.getWorkerIds());
                responseBuilder.setPlan(planBuilder.build());
                
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
                
                // Complete the trace
                Tracer.completeTrace(queryId);
                
            } catch (SQLParser.SQLParseException e) {
                Logger.queryError(queryId, "SQL parse error: " + e.getMessage());
                sendErrorResponse(responseObserver, queryId, "SQL parse error: " + e.getMessage());
            } catch (QueryPlanner.PlanningException e) {
                Logger.queryError(queryId, "Planning error: " + e.getMessage());
                sendErrorResponse(responseObserver, queryId, "Planning error: " + e.getMessage());
            } catch (QueryExecutor.ExecutionException e) {
                Logger.queryError(queryId, "Execution error: " + e.getMessage());
                sendErrorResponse(responseObserver, queryId, "Execution error: " + e.getMessage());
            } catch (Exception e) {
                Logger.queryError(queryId, "Unexpected error: " + e.getMessage());
                sendErrorResponse(responseObserver, queryId, "Unexpected error: " + e.getMessage());
            }
        }
        
        @Override
        public void getWorkerStatus(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
            StatusResponse.Builder responseBuilder = StatusResponse.newBuilder()
                .setTimestamp(System.currentTimeMillis());
            
            for (Map.Entry<String, String> entry : workerAddresses.entrySet()) {
                String workerId = entry.getKey();
                String address = entry.getValue();
                
                WorkerInfo.Builder workerInfoBuilder = WorkerInfo.newBuilder()
                    .setWorkerId(workerId)
                    .setAddress(address)
                    .setLastHeartbeat(System.currentTimeMillis());
                
                // Check if worker is healthy
                WorkerClient client = workerClients.get(workerId);
                if (client != null && client.isHealthy()) {
                    workerInfoBuilder.setStatus(WorkerStatus.WORKER_IDLE);
                } else {
                    workerInfoBuilder.setStatus(WorkerStatus.WORKER_FAILED);
                }
                
                responseBuilder.addWorkers(workerInfoBuilder.build());
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
        
        private void sendErrorResponse(StreamObserver<QueryResponse> responseObserver, 
                                      String queryId, String errorMessage) {
            QueryResponse errorResponse = QueryResponse.newBuilder()
                .setQueryId(queryId)
                .setStatus(QueryStatus.FAILED)
                .setErrorMessage(errorMessage)
                .setExecutionTimeMs(0)
                .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
        
        private com.distributed.sql.common.proto.QueryProto.PlanNode convertPlanNodeToProto(com.distributed.sql.common.models.PlanNode node) {
            com.distributed.sql.common.proto.QueryProto.PlanNode.Builder builder = 
                com.distributed.sql.common.proto.QueryProto.PlanNode.newBuilder()
                    .setNodeId(node.getNodeId())
                    .setType(convertNodeType(node.getType()))
                    .setEstimatedRows(node.getEstimatedRows());
            
            if (node.getTableName() != null) {
                builder.setTableName(node.getTableName());
            }
            
            if (node.getColumns() != null) {
                builder.addAllColumns(node.getColumns());
            }
            
            if (node.getConditions() != null) {
                for (com.distributed.sql.common.models.Condition condition : node.getConditions()) {
                    com.distributed.sql.common.proto.QueryProto.Condition.Builder conditionBuilder = 
                        com.distributed.sql.common.proto.QueryProto.Condition.newBuilder()
                            .setColumn(condition.getColumn())
                            .setOperator(convertOperator(condition.getOperator()))
                            .setValue(condition.getValue())
                            .setDataType(convertDataType(condition.getDataType()));
                    builder.addConditions(conditionBuilder.build());
                }
            }
            
            // Add children recursively
            for (com.distributed.sql.common.models.PlanNode child : node.getChildren()) {
                builder.addChildren(convertPlanNodeToProto(child));
            }
            
            return builder.build();
        }
        
        private com.distributed.sql.common.proto.QueryProto.NodeType convertNodeType(com.distributed.sql.common.models.PlanNode.NodeType type) {
            switch (type) {
                case SCAN: return com.distributed.sql.common.proto.QueryProto.NodeType.SCAN;
                case FILTER: return com.distributed.sql.common.proto.QueryProto.NodeType.FILTER;
                case JOIN: return com.distributed.sql.common.proto.QueryProto.NodeType.JOIN;
                case PROJECT: return com.distributed.sql.common.proto.QueryProto.NodeType.PROJECT;
                case AGGREGATE: return com.distributed.sql.common.proto.QueryProto.NodeType.AGGREGATE;
                default: return com.distributed.sql.common.proto.QueryProto.NodeType.SCAN;
            }
        }
        
        private com.distributed.sql.common.proto.QueryProto.Operator convertOperator(com.distributed.sql.common.models.Condition.Operator operator) {
            switch (operator) {
                case EQUALS: return com.distributed.sql.common.proto.QueryProto.Operator.EQUALS;
                case NOT_EQUALS: return com.distributed.sql.common.proto.QueryProto.Operator.NOT_EQUALS;
                case GREATER_THAN: return com.distributed.sql.common.proto.QueryProto.Operator.GREATER_THAN;
                case LESS_THAN: return com.distributed.sql.common.proto.QueryProto.Operator.LESS_THAN;
                case GREATER_THAN_EQUALS: return com.distributed.sql.common.proto.QueryProto.Operator.GREATER_THAN_EQUALS;
                case LESS_THAN_EQUALS: return com.distributed.sql.common.proto.QueryProto.Operator.LESS_THAN_EQUALS;
                case LIKE: return com.distributed.sql.common.proto.QueryProto.Operator.LIKE;
                case IN: return com.distributed.sql.common.proto.QueryProto.Operator.IN;
                default: return com.distributed.sql.common.proto.QueryProto.Operator.EQUALS;
            }
        }
        
        private com.distributed.sql.common.proto.QueryProto.DataType convertDataType(com.distributed.sql.common.models.Condition.DataType dataType) {
            switch (dataType) {
                case STRING: return com.distributed.sql.common.proto.QueryProto.DataType.STRING;
                case INTEGER: return com.distributed.sql.common.proto.QueryProto.DataType.INTEGER;
                case DOUBLE: return com.distributed.sql.common.proto.QueryProto.DataType.DOUBLE;
                case BOOLEAN: return com.distributed.sql.common.proto.QueryProto.DataType.BOOLEAN;
                case DATE: return com.distributed.sql.common.proto.QueryProto.DataType.DATE;
                default: return com.distributed.sql.common.proto.QueryProto.DataType.STRING;
            }
        }
    }
}
