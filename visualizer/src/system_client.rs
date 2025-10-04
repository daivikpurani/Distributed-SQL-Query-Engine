use std::collections::HashMap;
use anyhow::Result;
use serde::{Deserialize, Serialize};

use crate::visualizer::{SystemStatus, ComponentStatus, QueryResult, PerformanceMetrics};

pub struct SystemClient {
    demo_mode: bool,
}

impl SystemClient {
    pub async fn new() -> Result<Self> {
        Ok(Self {
            demo_mode: true, // Always run in demo mode for now
        })
    }
    
    pub async fn get_system_status(&self) -> Result<SystemStatus> {
        // Return demo data
        let mut components = HashMap::new();
        
        // Demo coordinator status
        components.insert("coordinator".to_string(), ComponentStatus {
            id: "coordinator".to_string(),
            status: "healthy".to_string(),
            cpu_usage: 45.0,
            memory_usage: 128.0,
            active_connections: 3,
            last_heartbeat: chrono::Utc::now(),
        });
        
        // Demo worker statuses
        for (worker_id, cpu, memory) in [
            ("worker1", 25.0, 64.0),
            ("worker2", 30.0, 72.0),
            ("worker3", 35.0, 80.0),
        ] {
            let status = ComponentStatus {
                id: worker_id.to_string(),
                status: "healthy".to_string(),
                cpu_usage: cpu,
                memory_usage: memory,
                active_connections: 1,
                last_heartbeat: chrono::Utc::now(),
            };
            components.insert(worker_id.to_string(), status);
        }
        
        Ok(SystemStatus {
            components,
            total_queries: 42,
            active_queries: 2,
            system_uptime: chrono::Duration::seconds(3600),
            last_updated: chrono::Utc::now(),
        })
    }
    
    pub async fn execute_query(&self, query: &str) -> Result<QueryResult> {
        // Mock query execution for demo purposes
        let start_time = std::time::Instant::now();
        
        // Simulate query processing time
        tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
        
        let execution_time = start_time.elapsed();
        
        // Generate mock results based on query
        let results = if query.contains("users") {
            vec![
                vec!["John Doe".to_string(), "30".to_string()],
                vec!["Jane Smith".to_string(), "25".to_string()],
                vec!["Bob Johnson".to_string(), "35".to_string()],
            ]
        } else {
            vec![
                vec!["Sample Result 1".to_string()],
                vec!["Sample Result 2".to_string()],
            ]
        };
        
        Ok(QueryResult {
            query_id: uuid::Uuid::new_v4().to_string(),
            sql_query: query.to_string(),
            execution_time_ms: execution_time.as_millis() as u64,
            rows_returned: results.len(),
            results,
            status: "completed".to_string(),
            timestamp: chrono::Utc::now(),
        })
    }
    
    pub async fn get_performance_metrics(&self) -> Result<PerformanceMetrics> {
        Ok(PerformanceMetrics {
            total_queries: 42,
            average_latency_ms: 150.0,
            queries_per_second: 2.5,
            error_rate: 0.02,
            worker_utilization: HashMap::from([
                ("worker1".to_string(), 75.0),
                ("worker2".to_string(), 60.0),
                ("worker3".to_string(), 80.0),
            ]),
            timestamp: chrono::Utc::now(),
        })
    }
}
