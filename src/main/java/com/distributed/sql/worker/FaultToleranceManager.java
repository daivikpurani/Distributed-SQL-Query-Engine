package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.proto.QueryProto.*;
import com.distributed.sql.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages fault tolerance features including checkpointing and retry logic
 */
public class FaultToleranceManager {
    private final String workerId;
    private final Map<String, CheckpointInfo> checkpoints;
    private final Random random;
    private final double failureProbability;

    public FaultToleranceManager(String workerId) {
        this.workerId = workerId;
        this.checkpoints = new ConcurrentHashMap<>();
        this.random = new Random();
        this.failureProbability = 0.1; // 10% chance of failure for simulation
    }

    /**
     * Create a checkpoint for a task
     */
    public CheckpointInfo createCheckpoint(String taskId, List<com.distributed.sql.common.models.Row> partialResults, String state) {
        String checkpointId = "checkpoint_" + taskId + "_" + System.currentTimeMillis();
        
        CheckpointInfo checkpoint = new CheckpointInfo();
        checkpoint.setCheckpointId(checkpointId);
        checkpoint.setTimestamp(System.currentTimeMillis());
        checkpoint.setPartialResults(new ArrayList<>(partialResults));
        checkpoint.setState(state);
        
        checkpoints.put(taskId, checkpoint);
        
        Logger.checkpointCreated(checkpointId, workerId);
        return checkpoint;
    }

    /**
     * Restore from a checkpoint
     */
    public CheckpointInfo restoreCheckpoint(String taskId) {
        CheckpointInfo checkpoint = checkpoints.get(taskId);
        if (checkpoint != null) {
            Logger.checkpointRestored(checkpoint.getCheckpointId(), workerId);
        }
        return checkpoint;
    }

    /**
     * Simulate a potential failure during task execution
     */
    public boolean simulateFailure() {
        boolean shouldFail = random.nextDouble() < failureProbability;
        if (shouldFail) {
            Logger.workerFailure(workerId, "Simulated random failure");
        }
        return shouldFail;
    }

    /**
     * Simulate network delay
     */
    public void simulateNetworkDelay() {
        try {
            int delayMs = ThreadLocalRandom.current().nextInt(10, 100);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Convert CheckpointInfo to protobuf
     */
    public com.distributed.sql.common.proto.QueryProto.CheckpointInfo toProtoCheckpoint(CheckpointInfo checkpoint) {
        com.distributed.sql.common.proto.QueryProto.CheckpointInfo.Builder builder = 
            com.distributed.sql.common.proto.QueryProto.CheckpointInfo.newBuilder()
                .setCheckpointId(checkpoint.getCheckpointId())
                .setTimestamp(checkpoint.getTimestamp())
                .setState(checkpoint.getState());
        
        for (com.distributed.sql.common.models.Row row : checkpoint.getPartialResults()) {
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
            
            builder.addPartialResults(rowBuilder.build());
        }
        
        return builder.build();
    }

    /**
     * Convert protobuf CheckpointInfo to model
     */
    public CheckpointInfo fromProtoCheckpoint(com.distributed.sql.common.proto.QueryProto.CheckpointInfo protoCheckpoint) {
        CheckpointInfo checkpoint = new CheckpointInfo();
        checkpoint.setCheckpointId(protoCheckpoint.getCheckpointId());
        checkpoint.setTimestamp(protoCheckpoint.getTimestamp());
        checkpoint.setState(protoCheckpoint.getState());
        
        List<com.distributed.sql.common.models.Row> partialResults = new ArrayList<>();
        for (com.distributed.sql.common.proto.QueryProto.Row protoRow : protoCheckpoint.getPartialResultsList()) {
            com.distributed.sql.common.models.Row row = new com.distributed.sql.common.models.Row();
            row.getValues().addAll(protoRow.getValuesList());
            
            if (protoRow.getMetadataMap().containsKey("source_table")) {
                row.setSourceTable(protoRow.getMetadataMap().get("source_table"));
            }
            if (protoRow.getMetadataMap().containsKey("worker_id")) {
                row.setWorkerId(protoRow.getMetadataMap().get("worker_id"));
            }
            
            partialResults.add(row);
        }
        
        checkpoint.setPartialResults(partialResults);
        return checkpoint;
    }

    /**
     * Clean up old checkpoints
     */
    public void cleanupOldCheckpoints(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        checkpoints.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > maxAgeMs);
    }

    /**
     * Get checkpoint statistics
     */
    public Map<String, Object> getCheckpointStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckpoints", checkpoints.size());
        stats.put("workerId", workerId);
        stats.put("failureProbability", failureProbability);
        
        long totalRows = checkpoints.values().stream()
            .mapToInt(cp -> cp.getPartialResults().size())
            .sum();
        stats.put("totalCheckpointedRows", totalRows);
        
        return stats;
    }

    /**
     * Represents checkpoint information
     */
    public static class CheckpointInfo {
        private String checkpointId;
        private long timestamp;
        private List<com.distributed.sql.common.models.Row> partialResults;
        private String state;

        public CheckpointInfo() {
            this.partialResults = new ArrayList<>();
        }

        // Getters and setters
        public String getCheckpointId() { return checkpointId; }
        public void setCheckpointId(String checkpointId) { this.checkpointId = checkpointId; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public List<com.distributed.sql.common.models.Row> getPartialResults() { return partialResults; }
        public void setPartialResults(List<com.distributed.sql.common.models.Row> partialResults) { this.partialResults = partialResults; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        @Override
        public String toString() {
            return String.format("CheckpointInfo{id='%s', timestamp=%d, rows=%d, state='%s'}", 
                               checkpointId, timestamp, partialResults.size(), state);
        }
    }
}
