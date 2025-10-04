use std::collections::HashMap;
use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc, Duration};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct VisualizationData {
    pub system_status: SystemStatus,
    pub performance_metrics: PerformanceMetrics,
    pub recent_queries: Vec<QueryResult>,
}

impl VisualizationData {
    pub fn new() -> Self {
        Self {
            system_status: SystemStatus::default(),
            performance_metrics: PerformanceMetrics::default(),
            recent_queries: Vec::new(),
        }
    }
    
    pub fn update_system_status(&mut self, status: SystemStatus) {
        self.system_status = status;
    }
    
    pub fn add_query_result(&mut self, result: QueryResult) {
        self.recent_queries.push(result);
        // Keep only last 10 queries
        if self.recent_queries.len() > 10 {
            self.recent_queries.remove(0);
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SystemStatus {
    pub components: HashMap<String, ComponentStatus>,
    pub total_queries: u64,
    pub active_queries: u64,
    pub system_uptime: Duration,
    pub last_updated: DateTime<Utc>,
}

impl Default for SystemStatus {
    fn default() -> Self {
        let mut components = HashMap::new();
        
        // Default coordinator status
        components.insert("coordinator".to_string(), ComponentStatus {
            id: "coordinator".to_string(),
            status: "unknown".to_string(),
            cpu_usage: 0.0,
            memory_usage: 0.0,
            active_connections: 0,
            last_heartbeat: Utc::now(),
        });
        
        // Default worker statuses
        for worker_id in ["worker1", "worker2", "worker3"] {
            components.insert(worker_id.to_string(), ComponentStatus {
                id: worker_id.to_string(),
                status: "unknown".to_string(),
                cpu_usage: 0.0,
                memory_usage: 0.0,
                active_connections: 0,
                last_heartbeat: Utc::now(),
            });
        }
        
        Self {
            components,
            total_queries: 0,
            active_queries: 0,
            system_uptime: Duration::zero(),
            last_updated: Utc::now(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ComponentStatus {
    pub id: String,
    pub status: String, // "healthy", "unhealthy", "unknown"
    pub cpu_usage: f64,
    pub memory_usage: f64, // MB
    pub active_connections: u32,
    pub last_heartbeat: DateTime<Utc>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct QueryResult {
    pub query_id: String,
    pub sql_query: String,
    pub execution_time_ms: u64,
    pub rows_returned: usize,
    pub results: Vec<Vec<String>>,
    pub status: String,
    pub timestamp: DateTime<Utc>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PerformanceMetrics {
    pub total_queries: u64,
    pub average_latency_ms: f64,
    pub queries_per_second: f64,
    pub error_rate: f64,
    pub worker_utilization: HashMap<String, f64>,
    pub timestamp: DateTime<Utc>,
}

impl Default for PerformanceMetrics {
    fn default() -> Self {
        Self {
            total_queries: 0,
            average_latency_ms: 0.0,
            queries_per_second: 0.0,
            error_rate: 0.0,
            worker_utilization: HashMap::new(),
            timestamp: Utc::now(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ShardInfo {
    pub shard_id: String,
    pub table_name: String,
    pub worker_id: String,
    pub row_count: u64,
    pub size_mb: f64,
    pub shard_type: String, // "hash", "range", "round_robin"
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct QueryExecutionStep {
    pub step_name: String,
    pub status: String, // "pending", "running", "completed", "failed"
    pub duration_ms: u64,
    pub details: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct QueryFlow {
    pub query_id: String,
    pub sql_query: String,
    pub steps: Vec<QueryExecutionStep>,
    pub total_duration_ms: u64,
    pub status: String,
}
