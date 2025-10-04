//! Tracing utilities for performance monitoring

use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::{info, debug};

/// Tracer for query execution monitoring
pub struct Tracer {
    query_traces: Arc<RwLock<HashMap<String, QueryTrace>>>,
}

/// Individual query trace
#[derive(Debug, Clone)]
pub struct QueryTrace {
    pub query_id: String,
    pub sql: String,
    pub start_time: u64,
    pub events: Vec<TraceEvent>,
}

/// Individual trace event
#[derive(Debug, Clone)]
pub struct TraceEvent {
    pub timestamp: u64,
    pub event_type: String,
    pub description: String,
    pub metadata: HashMap<String, String>,
}

impl Tracer {
    /// Create a new tracer
    pub fn new() -> Self {
        Self {
            query_traces: Arc::new(RwLock::new(HashMap::new())),
        }
    }

    /// Start tracing a query
    pub async fn start_query_trace(&self, query_id: String, sql: String) {
        let trace = QueryTrace {
            query_id: query_id.to_string(),
            sql: sql.to_string(),
            start_time: chrono::Utc::now().timestamp_millis() as u64,
            events: Vec::new(),
        };

        self.query_traces.write().await.insert(query_id.to_string(), trace);
        info!("Started tracing query: {}", query_id);
    }

    /// Add an event to a query trace
    pub async fn add_event(&self, query_id: String, event_type: String, description: String) {
        let event = TraceEvent {
            timestamp: chrono::Utc::now().timestamp_millis() as u64,
            event_type: event_type.to_string(),
            description: description.to_string(),
            metadata: HashMap::new(),
        };

        if let Some(trace) = self.query_traces.write().await.get_mut(&query_id) {
            trace.events.push(event);
            debug!("Added event to query {}: {} - {}", query_id, event_type, description);
        }
    }

    /// Add an event with metadata
    pub async fn add_event_with_metadata(
        &self,
        query_id: String,
        event_type: String,
        description: String,
        metadata: HashMap<String, String>,
    ) {
        let event = TraceEvent {
            timestamp: chrono::Utc::now().timestamp_millis() as u64,
            event_type: event_type.to_string(),
            description: description.to_string(),
            metadata,
        };

        if let Some(trace) = self.query_traces.write().await.get_mut(&query_id) {
            trace.events.push(event);
            debug!("Added event with metadata to query {}: {} - {}", query_id, event_type, description);
        }
    }

    /// Get the trace for a query
    pub async fn get_trace(&self, query_id: String) -> Option<QueryTrace> {
        self.query_traces.read().await.get(&query_id).cloned()
    }

    /// Complete tracing for a query
    pub async fn complete_query_trace(&self, query_id: String) -> Option<QueryTrace> {
        let trace = self.query_traces.write().await.remove(&query_id);
        if let Some(ref trace) = trace {
            let duration = chrono::Utc::now().timestamp_millis() as u64 - trace.start_time;
            info!("Completed tracing query {}: {}ms", query_id, duration);
        }
        trace
    }

    /// Get all active traces
    pub async fn get_all_traces(&self) -> HashMap<String, QueryTrace> {
        self.query_traces.read().await.clone()
    }

    /// Clear all traces
    pub async fn clear_traces(&self) {
        self.query_traces.write().await.clear();
        info!("Cleared all query traces");
    }
}

impl Default for Tracer {
    fn default() -> Self {
        Self::new()
    }
}

/// Global tracer instance
lazy_static::lazy_static! {
    pub static ref TRACER: Tracer = Tracer::new();
}

/// Convenience functions for global tracer
pub fn start_query_trace(query_id: String, sql: String) {
    tokio::spawn(async move {
        TRACER.start_query_trace(query_id.to_string(), sql.to_string()).await;
    });
}

pub fn add_event(query_id: String, event_type: String, description: String) {
    tokio::spawn(async move {
        TRACER.add_event(query_id.to_string(), event_type.to_string(), description.to_string()).await;
    });
}

pub fn add_event_with_metadata(
    query_id: String,
    event_type: String,
    description: String,
    metadata: HashMap<String, String>,
) {
    tokio::spawn(async move {
        TRACER.add_event_with_metadata(query_id.to_string(), event_type.to_string(), description.to_string(), metadata).await;
    });
}

pub async fn get_trace(query_id: String) -> Option<QueryTrace> {
    TRACER.get_trace(query_id).await
}

pub async fn complete_query_trace(query_id: String) -> Option<QueryTrace> {
    TRACER.complete_query_trace(query_id).await
}
