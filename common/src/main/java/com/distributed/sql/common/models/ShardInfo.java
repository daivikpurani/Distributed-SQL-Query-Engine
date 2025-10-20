package com.distributed.sql.common.models;

/**
 * Represents shard information for data distribution
 */
public class ShardInfo {
    private String shardId;
    private String workerId;
    private String tableName;
    private String keyRangeStart;
    private String keyRangeEnd;
    private long rowCount;

    public ShardInfo() {
    }

    public ShardInfo(String shardId, String workerId, String tableName, String keyRangeStart, String keyRangeEnd,
            long rowCount) {
        this.shardId = shardId;
        this.workerId = workerId;
        this.tableName = tableName;
        this.keyRangeStart = keyRangeStart;
        this.keyRangeEnd = keyRangeEnd;
        this.rowCount = rowCount;
    }

    // Getters and Setters
    public String getShardId() {
        return shardId;
    }

    public void setShardId(String shardId) {
        this.shardId = shardId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getKeyRangeStart() {
        return keyRangeStart;
    }

    public void setKeyRangeStart(String keyRangeStart) {
        this.keyRangeStart = keyRangeStart;
    }

    public String getKeyRangeEnd() {
        return keyRangeEnd;
    }

    public void setKeyRangeEnd(String keyRangeEnd) {
        this.keyRangeEnd = keyRangeEnd;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }
}
