//! Logging utilities

use tracing::{info, warn, error, debug, trace};

/// Logger utility functions for the distributed SQL engine
pub struct Logger;

impl Logger {
    /// Log query start
    pub fn query_start(query_id: &str, sql: &str) {
        info!("Query started: {} - {}", query_id, sql);
    }

    /// Log query completion
    pub fn query_complete(query_id: &str, execution_time_ms: u64) {
        info!("Query completed: {} - {}ms", query_id, execution_time_ms);
    }

    /// Log query dispatch to worker
    pub fn query_dispatch(query_id: &str, worker_id: &str) {
        debug!("Query dispatched: {} -> {}", query_id, worker_id);
    }

    /// Log worker task start
    pub fn worker_task_start(worker_id: &str, task_id: &str) {
        debug!("Worker task started: {} - {}", worker_id, task_id);
    }

    /// Log worker task completion
    pub fn worker_task_complete(worker_id: &str, task_id: &str, execution_time_ms: u64) {
        debug!("Worker task completed: {} - {} - {}ms", worker_id, task_id, execution_time_ms);
    }

    /// Log shard operation
    pub fn shard_operation(operation: &str, shard_id: &str, details: &str) {
        info!("Shard operation: {} - {} - {}", operation, shard_id, details);
    }

    /// Log system startup
    pub fn system_startup(component: &str, port: u16) {
        info!("System startup: {} on port {}", component, port);
    }

    /// Log system shutdown
    pub fn system_shutdown(component: &str) {
        info!("System shutdown: {}", component);
    }

    /// Log error with context
    pub fn error_with_context(context: &str, error: &str) {
        error!("{}: {}", context, error);
    }

    /// Log warning with context
    pub fn warning_with_context(context: &str, warning: &str) {
        warn!("{}: {}", context, warning);
    }

    /// Log debug information
    pub fn debug_info(message: &str) {
        debug!("{}", message);
    }

    /// Log trace information
    pub fn trace_info(message: &str) {
        trace!("{}", message);
    }
}
