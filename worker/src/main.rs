use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tonic::{transport::Server, Request, Response, Status};
use tracing::{info, error, warn, debug};
use chrono::{DateTime, Utc};
use uuid::Uuid;

mod worker_service;
mod query_executor;
mod data_store;

use worker_service::WorkerServiceImpl;
use common::proto::worker_service_server::WorkerService;
use query_executor::QueryExecutor;
use data_store::DataStore;

#[derive(Debug, Clone)]
pub struct QueryMetrics {
    pub total_queries: u64,
    pub active_queries: u32,
    pub cpu_usage: f64,
    pub memory_usage: f64,
    pub last_heartbeat: DateTime<Utc>,
}

#[derive(Debug)]
pub struct WorkerState {
    pub worker_id: String,
    pub coordinator_address: String,
    pub coordinator_port: u16,
    pub query_executor: Arc<QueryExecutor>,
    pub data_store: Arc<DataStore>,
    pub metrics: Arc<RwLock<QueryMetrics>>,
    pub start_time: DateTime<Utc>,
}

impl WorkerState {
    pub fn new(worker_id: String, coordinator_address: String, coordinator_port: u16) -> Self {
        Self {
            worker_id: worker_id.clone(),
            coordinator_address,
            coordinator_port,
            query_executor: Arc::new(QueryExecutor::new()),
            data_store: Arc::new(DataStore::new()),
            metrics: Arc::new(RwLock::new(QueryMetrics {
                total_queries: 0,
                active_queries: 0,
                cpu_usage: 0.0,
                memory_usage: 0.0,
                last_heartbeat: Utc::now(),
            })),
            start_time: Utc::now(),
        }
    }

    pub async fn execute_query(&self, sql_query: &str) -> Result<QueryResult, String> {
        let query_id = Uuid::new_v4().to_string();
        let start_time = std::time::Instant::now();

        info!("Worker {} executing query {}: {}", self.worker_id, query_id, sql_query);

        // Increment active queries
        {
            let mut metrics = self.metrics.write().await;
            metrics.active_queries += 1;
        }

        // Execute query
        let results = self.query_executor.execute(sql_query).await?;
        let execution_time = start_time.elapsed();

        // Decrement active queries and increment total
        {
            let mut metrics = self.metrics.write().await;
            metrics.active_queries -= 1;
            metrics.total_queries += 1;
        }

        let query_result = QueryResult {
            query_id: query_id.clone(),
            sql_query: sql_query.to_string(),
            execution_time_ms: execution_time.as_millis() as u64,
            rows_returned: results.len() as u32,
            results,
            status: "completed".to_string(),
            timestamp: Some(prost_types::Timestamp {
                seconds: Utc::now().timestamp(),
                nanos: 0,
            }),
        };

        info!("Worker {} completed query {} in {}ms", self.worker_id, query_id, execution_time.as_millis());

        Ok(query_result)
    }

    pub async fn get_metrics(&self) -> QueryMetrics {
        let metrics = self.metrics.read().await;
        QueryMetrics {
            total_queries: metrics.total_queries,
            active_queries: metrics.active_queries,
            cpu_usage: metrics.cpu_usage,
            memory_usage: metrics.memory_usage,
            last_heartbeat: metrics.last_heartbeat,
        }
    }

    pub async fn update_system_metrics(&self, cpu_usage: f64, memory_usage: f64) {
        let mut metrics = self.metrics.write().await;
        metrics.cpu_usage = cpu_usage;
        metrics.memory_usage = memory_usage;
        metrics.last_heartbeat = Utc::now();
    }
}

#[derive(Debug, Clone)]
pub struct QueryResult {
    pub query_id: String,
    pub sql_query: String,
    pub execution_time_ms: u64,
    pub rows_returned: u32,
    pub results: Vec<Vec<String>>,
    pub status: String,
    pub timestamp: Option<prost_types::Timestamp>,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize logging
    tracing_subscriber::fmt()
        .with_env_filter("worker=debug,tonic=info")
        .init();

    let args: Vec<String> = std::env::args().collect();
    let mut worker_id = "worker1".to_string();
    let mut port = 50052u16;
    let mut data_dir = "data".to_string();
    
    // Parse command line arguments
    let mut i = 1;
    while i < args.len() {
        match args[i].as_str() {
            "--worker-id" => {
                if i + 1 < args.len() {
                    worker_id = args[i + 1].clone();
                    i += 2;
                } else {
                    i += 1;
                }
            }
            "--port" => {
                if i + 1 < args.len() {
                    port = args[i + 1].parse().unwrap_or(50052);
                    i += 2;
                } else {
                    i += 1;
                }
            }
            "--data-dir" => {
                if i + 1 < args.len() {
                    data_dir = args[i + 1].clone();
                    i += 2;
                } else {
                    i += 1;
                }
            }
            _ => i += 1,
        }
    }

    let coordinator_address = "127.0.0.1".to_string();
    let coordinator_port = 50051;

    info!("Starting Worker {} on port {}", worker_id, port);

    let state = Arc::new(WorkerState::new(worker_id.clone(), coordinator_address, coordinator_port));
    let service = WorkerServiceImpl::new(state.clone());

    // Start heartbeat to coordinator
    let state_clone = state.clone();
    let worker_id_clone = worker_id.clone();
    tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(10));
        loop {
            interval.tick().await;
            
            // Send heartbeat to coordinator
            let metrics = state_clone.get_metrics().await;
            
            // Simulate system metrics
            let cpu_usage = 20.0 + (Utc::now().timestamp() % 20) as f64;
            let memory_usage = 50.0 + (Utc::now().timestamp() % 30) as f64;
            
            state_clone.update_system_metrics(cpu_usage, memory_usage).await;
            
            debug!("Worker {} heartbeat: CPU={}%, Memory={}MB, Active={}", 
                   worker_id_clone, cpu_usage, memory_usage, metrics.active_queries);
        }
    });

    let addr = format!("127.0.0.1:{}", port).parse()?;
    
    info!("Worker {} server listening on {}", worker_id, addr);
    
    Server::builder()
        .add_service(common::proto::worker_service_server::WorkerServiceServer::new(service))
        .serve(addr)
        .await?;

    Ok(())
}