use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tonic::{transport::Server, Request, Response, Status};
use tracing::{info, error, warn};
use chrono::{DateTime, Utc};
use uuid::Uuid;

mod coordinator_service;
mod query_planner;
mod shard_manager;

use coordinator_service::CoordinatorServiceImpl;
use common::proto::coordinator_service_server::CoordinatorService;
use query_planner::QueryPlanner;
use shard_manager::ShardManager;

#[derive(Debug, Clone)]
pub struct WorkerInfo {
    pub id: String,
    pub address: String,
    pub port: u16,
    pub status: String,
    pub cpu_usage: f64,
    pub memory_usage: f64,
    pub active_queries: u32,
    pub last_heartbeat: DateTime<Utc>,
}

#[derive(Debug)]
pub struct CoordinatorState {
    pub workers: Arc<RwLock<HashMap<String, WorkerInfo>>>,
    pub query_planner: Arc<QueryPlanner>,
    pub shard_manager: Arc<ShardManager>,
    pub total_queries: Arc<RwLock<u64>>,
    pub active_queries: Arc<RwLock<u32>>,
    pub start_time: DateTime<Utc>,
}

impl CoordinatorState {
    pub fn new() -> Self {
        Self {
            workers: Arc::new(RwLock::new(HashMap::new())),
            query_planner: Arc::new(QueryPlanner::new()),
            shard_manager: Arc::new(ShardManager::new()),
            total_queries: Arc::new(RwLock::new(0)),
            active_queries: Arc::new(RwLock::new(0)),
            start_time: Utc::now(),
        }
    }

    pub async fn register_worker(&self, worker_id: String, address: String, port: u16) -> Result<(), String> {
        let mut workers = self.workers.write().await;
        let worker_info = WorkerInfo {
            id: worker_id.clone(),
            address,
            port,
            status: "healthy".to_string(),
            cpu_usage: 0.0,
            memory_usage: 0.0,
            active_queries: 0,
            last_heartbeat: Utc::now(),
        };
        
        workers.insert(worker_id.clone(), worker_info);
        info!("Registered worker: {}", worker_id);
        Ok(())
    }

    pub async fn update_worker_status(&self, worker_id: &str, cpu_usage: f64, memory_usage: f64, active_queries: u32) -> Result<(), String> {
        let mut workers = self.workers.write().await;
        if let Some(worker) = workers.get_mut(worker_id) {
            worker.cpu_usage = cpu_usage;
            worker.memory_usage = memory_usage;
            worker.active_queries = active_queries;
            worker.last_heartbeat = Utc::now();
            worker.status = "healthy".to_string();
        }
        Ok(())
    }

    pub async fn get_system_status(&self) -> Result<SystemStatus, String> {
        let workers = self.workers.read().await;
        let total_queries = *self.total_queries.read().await;
        let active_queries = *self.active_queries.read().await;
        
        let mut components = HashMap::new();
        
        // Add coordinator status
        components.insert("coordinator".to_string(), ComponentStatus {
            id: "coordinator".to_string(),
            status: "healthy".to_string(),
            cpu_usage: 45.0,
            memory_usage: 128.0,
            active_connections: workers.len() as u32,
            last_heartbeat: Utc::now(),
        });
        
        // Add worker statuses
        for (worker_id, worker_info) in workers.iter() {
            components.insert(worker_id.clone(), ComponentStatus {
                id: worker_id.clone(),
                status: worker_info.status.clone(),
                cpu_usage: worker_info.cpu_usage,
                memory_usage: worker_info.memory_usage,
                active_connections: worker_info.active_queries,
                last_heartbeat: worker_info.last_heartbeat,
            });
        }
        
        Ok(SystemStatus {
            components,
            total_queries,
            active_queries,
            system_uptime: Utc::now() - self.start_time,
            last_updated: Utc::now(),
        })
    }
}

#[derive(Debug, Clone)]
pub struct ComponentStatus {
    pub id: String,
    pub status: String,
    pub cpu_usage: f64,
    pub memory_usage: f64,
    pub active_connections: u32,
    pub last_heartbeat: DateTime<Utc>,
}

#[derive(Debug, Clone)]
pub struct SystemStatus {
    pub components: HashMap<String, ComponentStatus>,
    pub total_queries: u64,
    pub active_queries: u32,
    pub system_uptime: chrono::Duration,
    pub last_updated: DateTime<Utc>,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize logging
    tracing_subscriber::fmt()
        .with_env_filter("coordinator=debug,tonic=info")
        .init();

    let args: Vec<String> = std::env::args().collect();
    let port = args.get(1)
        .and_then(|s| s.parse::<u16>().ok())
        .unwrap_or(50051);

    info!("Starting Coordinator on port {}", port);

    let state = Arc::new(CoordinatorState::new());
    let service = CoordinatorServiceImpl::new(state.clone());

    // Start background tasks
    let state_clone = state.clone();
    tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(5));
        loop {
            interval.tick().await;
            
            // Check worker health
            let mut workers = state_clone.workers.write().await;
            let now = Utc::now();
            for (worker_id, worker_info) in workers.iter_mut() {
                if now.signed_duration_since(worker_info.last_heartbeat).num_seconds() > 30 {
                    worker_info.status = "unhealthy".to_string();
                    warn!("Worker {} is unhealthy - no heartbeat for 30+ seconds", worker_id);
                }
            }
        }
    });

    let addr = format!("127.0.0.1:{}", port).parse()?;
    
    info!("Coordinator server listening on {}", addr);
    
    Server::builder()
        .add_service(common::proto::coordinator_service_server::CoordinatorServiceServer::new(service))
        .serve(addr)
        .await?;

    Ok(())
}