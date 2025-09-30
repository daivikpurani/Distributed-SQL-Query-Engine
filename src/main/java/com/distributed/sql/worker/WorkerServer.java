package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.common.proto.WorkerServiceGrpc;
import com.distributed.sql.utils.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Worker server that executes query tasks
 */
public class WorkerServer {
    private final int port;
    private final String workerId;
    private final String dataDirectory;
    private Server server;
    private final ExecutionEngine executionEngine;
    private final FaultToleranceManager faultToleranceManager;

    public WorkerServer(int port, String workerId, String dataDirectory) {
        this.port = port;
        this.workerId = workerId;
        this.dataDirectory = dataDirectory;
        this.executionEngine = new ExecutionEngine(workerId, dataDirectory);
        this.faultToleranceManager = new FaultToleranceManager(workerId);
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
            .addService(new WorkerServiceImpl())
            .build()
            .start();
        
        Logger.workerStart(workerId, "localhost:" + port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Shutting down worker server: {}", workerId);
            WorkerServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Implementation of the WorkerService gRPC service
     */
    private class WorkerServiceImpl extends WorkerServiceGrpc.WorkerServiceImplBase {
        
        @Override
        public void executeTask(TaskRequest request, StreamObserver<TaskResponse> responseObserver) {
            String taskId = request.getTaskId();
            String queryId = request.getQueryId();
            
            Logger.workerTaskStart(workerId, taskId);
            long startTime = System.currentTimeMillis();
            
            try {
                // Simulate network delay
                faultToleranceManager.simulateNetworkDelay();
                
                // Simulate potential failure
                if (faultToleranceManager.simulateFailure()) {
                    TaskResponse failureResponse = TaskResponse.newBuilder()
                        .setTaskId(taskId)
                        .setQueryId(queryId)
                        .setStatus(TaskStatus.TASK_FAILED)
                        .setErrorMessage("Simulated worker failure")
                        .setExecutionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
                    
                    responseObserver.onNext(failureResponse);
                    responseObserver.onCompleted();
                    return;
                }
                
                // Convert protobuf PlanNode to model
                com.distributed.sql.common.models.PlanNode planNode = convertProtoPlanNode(request.getPlanNode());
                
                // Execute the plan node
                List<com.distributed.sql.common.models.Row> results = executionEngine.executePlanNode(planNode);
                
                // Create checkpoint for partial results
                FaultToleranceManager.CheckpointInfo checkpoint = 
                    faultToleranceManager.createCheckpoint(taskId, results, "COMPLETED");
                
                // Convert results to protobuf
                List<com.distributed.sql.common.proto.QueryProto.Row> protoRows = 
                    convertRowsToProto(results);
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                TaskResponse response = TaskResponse.newBuilder()
                    .setTaskId(taskId)
                    .setQueryId(queryId)
                    .setStatus(TaskStatus.TASK_COMPLETED)
                    .addAllRows(protoRows)
                    .setExecutionTimeMs(executionTime)
                    .setCheckpoint(faultToleranceManager.toProtoCheckpoint(checkpoint))
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                Logger.workerTaskComplete(workerId, taskId, executionTime);
                
            } catch (ExecutionEngine.ExecutionException e) {
                Logger.error("Task execution failed: {}", taskId, e);
                
                TaskResponse errorResponse = TaskResponse.newBuilder()
                    .setTaskId(taskId)
                    .setQueryId(queryId)
                    .setStatus(TaskStatus.TASK_FAILED)
                    .setErrorMessage("Execution error: " + e.getMessage())
                    .setExecutionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
                
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
            } catch (Exception e) {
                Logger.error("Unexpected error in task execution: {}", taskId, e);
                
                TaskResponse errorResponse = TaskResponse.newBuilder()
                    .setTaskId(taskId)
                    .setQueryId(queryId)
                    .setStatus(TaskStatus.TASK_FAILED)
                    .setErrorMessage("Unexpected error: " + e.getMessage())
                    .setExecutionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
                
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
            }
        }
        
        @Override
        public void checkpoint(CheckpointRequest request, StreamObserver<CheckpointResponse> responseObserver) {
            try {
                FaultToleranceManager.CheckpointInfo checkpoint = 
                    faultToleranceManager.fromProtoCheckpoint(request.getCheckpointData());
                
                // Store checkpoint (in a real system, this would be persisted)
                faultToleranceManager.createCheckpoint(
                    request.getCheckpointId(), 
                    checkpoint.getPartialResults(), 
                    checkpoint.getState()
                );
                
                CheckpointResponse response = CheckpointResponse.newBuilder()
                    .setCheckpointId(request.getCheckpointId())
                    .setSuccess(true)
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
            } catch (Exception e) {
                Logger.error("Checkpoint operation failed: {}", request.getCheckpointId(), e);
                
                CheckpointResponse response = CheckpointResponse.newBuilder()
                    .setCheckpointId(request.getCheckpointId())
                    .setSuccess(false)
                    .setErrorMessage("Checkpoint failed: " + e.getMessage())
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
        
        @Override
        public void healthCheck(HealthRequest request, StreamObserver<HealthResponse> responseObserver) {
            HealthResponse response = HealthResponse.newBuilder()
                .setWorkerId(workerId)
                .setHealthy(true)
                .setTimestamp(System.currentTimeMillis())
                .setStatusMessage("Worker is healthy and ready")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        
        private com.distributed.sql.common.models.PlanNode convertProtoPlanNode(com.distributed.sql.common.proto.QueryProto.PlanNode protoNode) {
            com.distributed.sql.common.models.PlanNode node = new com.distributed.sql.common.models.PlanNode(protoNode.getNodeId(), convertNodeType(protoNode.getType()));
            node.setTableName(protoNode.getTableName());
            node.setColumns(new ArrayList<>(protoNode.getColumnsList()));
            node.setEstimatedRows(protoNode.getEstimatedRows());
            
            // Convert conditions
            List<com.distributed.sql.common.models.Condition> conditions = new ArrayList<>();
            for (com.distributed.sql.common.proto.QueryProto.Condition protoCondition : protoNode.getConditionsList()) {
                com.distributed.sql.common.models.Condition condition = new com.distributed.sql.common.models.Condition(
                    protoCondition.getColumn(),
                    convertOperator(protoCondition.getOperator()),
                    protoCondition.getValue(),
                    convertDataType(protoCondition.getDataType())
                );
                conditions.add(condition);
            }
            node.setConditions(conditions);
            
            // Convert children recursively
            for (com.distributed.sql.common.proto.QueryProto.PlanNode protoChild : protoNode.getChildrenList()) {
                node.addChild(convertProtoPlanNode(protoChild));
            }
            
            return node;
        }
        
        private com.distributed.sql.common.models.PlanNode.NodeType convertNodeType(com.distributed.sql.common.proto.QueryProto.NodeType protoType) {
            switch (protoType) {
                case SCAN: return com.distributed.sql.common.models.PlanNode.NodeType.SCAN;
                case FILTER: return com.distributed.sql.common.models.PlanNode.NodeType.FILTER;
                case JOIN: return com.distributed.sql.common.models.PlanNode.NodeType.JOIN;
                case PROJECT: return com.distributed.sql.common.models.PlanNode.NodeType.PROJECT;
                case AGGREGATE: return com.distributed.sql.common.models.PlanNode.NodeType.AGGREGATE;
                default: return com.distributed.sql.common.models.PlanNode.NodeType.SCAN;
            }
        }
        
        private com.distributed.sql.common.models.Condition.Operator convertOperator(com.distributed.sql.common.proto.QueryProto.Operator protoOperator) {
            switch (protoOperator) {
                case EQUALS: return com.distributed.sql.common.models.Condition.Operator.EQUALS;
                case NOT_EQUALS: return com.distributed.sql.common.models.Condition.Operator.NOT_EQUALS;
                case GREATER_THAN: return com.distributed.sql.common.models.Condition.Operator.GREATER_THAN;
                case LESS_THAN: return com.distributed.sql.common.models.Condition.Operator.LESS_THAN;
                case GREATER_THAN_EQUALS: return com.distributed.sql.common.models.Condition.Operator.GREATER_THAN_EQUALS;
                case LESS_THAN_EQUALS: return com.distributed.sql.common.models.Condition.Operator.LESS_THAN_EQUALS;
                case LIKE: return com.distributed.sql.common.models.Condition.Operator.LIKE;
                case IN: return com.distributed.sql.common.models.Condition.Operator.IN;
                default: return com.distributed.sql.common.models.Condition.Operator.EQUALS;
            }
        }
        
        private com.distributed.sql.common.models.Condition.DataType convertDataType(com.distributed.sql.common.proto.QueryProto.DataType protoDataType) {
            switch (protoDataType) {
                case STRING: return com.distributed.sql.common.models.Condition.DataType.STRING;
                case INTEGER: return com.distributed.sql.common.models.Condition.DataType.INTEGER;
                case DOUBLE: return com.distributed.sql.common.models.Condition.DataType.DOUBLE;
                case BOOLEAN: return com.distributed.sql.common.models.Condition.DataType.BOOLEAN;
                case DATE: return com.distributed.sql.common.models.Condition.DataType.DATE;
                default: return com.distributed.sql.common.models.Condition.DataType.STRING;
            }
        }
        
        private List<com.distributed.sql.common.proto.QueryProto.Row> convertRowsToProto(List<com.distributed.sql.common.models.Row> rows) {
            List<com.distributed.sql.common.proto.QueryProto.Row> protoRows = new ArrayList<>();
            
            for (com.distributed.sql.common.models.Row row : rows) {
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
                
                protoRows.add(rowBuilder.build());
            }
            
            return protoRows;
        }
    }
}
