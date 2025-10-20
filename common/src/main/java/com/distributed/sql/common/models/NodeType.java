package com.distributed.sql.common.models;

/**
 * Types of plan nodes in query execution
 */
public enum NodeType {
    SCAN,
    FILTER,
    JOIN,
    PROJECT,
    AGGREGATE
}
