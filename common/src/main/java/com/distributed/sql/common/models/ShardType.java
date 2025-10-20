package com.distributed.sql.common.models;

/**
 * Types of sharding strategies
 */
public enum ShardType {
    HASH,
    RANGE,
    ROUND_ROBIN
}
