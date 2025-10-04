use std::sync::Arc;
use tonic::{Request, Response, Status};
use tracing::{info, error, debug};
use uuid::Uuid;

use crate::{CoordinatorState, WorkerInfo};

use common::proto::{
    coordinator_service_server::CoordinatorService,
    RegisterWorkerRequest, RegisterWorkerResponse,
    HeartbeatRequest, HeartbeatResponse,
    ExecuteQueryRequest, ExecuteQueryResponse,
    GetSystemStatusRequest, GetSystemStatusResponse,
    QueryResult, SystemStatus as ProtoSystemStatus, ComponentStatus as ProtoComponentStatus,
};

pub struct CoordinatorServiceImpl {
    state: Arc<CoordinatorState>,
}

impl CoordinatorServiceImpl {
    pub fn new(state: Arc<CoordinatorState>) -> Self {
        Self { state }
    }
}

#[tonic::async_trait]
impl CoordinatorService for CoordinatorServiceImpl {
    async fn register_worker(
        &self,
        request: Request<RegisterWorkerRequest>,
    ) -> Result<Response<RegisterWorkerResponse>, Status> {
        let req = request.into_inner();
        let worker_id = req.worker_id;
        let address = req.address;
        let port = req.port as u16;

        info!("Registering worker: {} at {}:{}", worker_id, address, port);

        match self.state.register_worker(worker_id.clone(), address, port).await {
            Ok(_) => {
                info!("Successfully registered worker: {}", worker_id);
                Ok(Response::new(RegisterWorkerResponse {
                    success: true,
                    message: format!("Worker {} registered successfully", worker_id),
                }))
            }
            Err(e) => {
                error!("Failed to register worker {}: {}", worker_id, e);
                Ok(Response::new(RegisterWorkerResponse {
                    success: false,
                    message: format!("Failed to register worker: {}", e),
                }))
            }
        }
    }

    async fn heartbeat(
        &self,
        request: Request<HeartbeatRequest>,
    ) -> Result<Response<HeartbeatResponse>, Status> {
        let req = request.into_inner();
        let worker_id = req.worker_id;
        let cpu_usage = req.cpu_usage;
        let memory_usage = req.memory_usage;
        let active_queries = req.active_queries;

        debug!("Heartbeat from worker {}: CPU={}%, Memory={}MB, Active={}", 
               worker_id, cpu_usage, memory_usage, active_queries);

        match self.state.update_worker_status(&worker_id, cpu_usage, memory_usage, active_queries).await {
            Ok(_) => {
                Ok(Response::new(HeartbeatResponse {
                    success: true,
                    message: "Heartbeat received".to_string(),
                }))
            }
            Err(e) => {
                error!("Failed to update worker status for {}: {}", worker_id, e);
                Ok(Response::new(HeartbeatResponse {
                    success: false,
                    message: format!("Failed to update status: {}", e),
                }))
            }
        }
    }

    async fn execute_query(
        &self,
        request: Request<ExecuteQueryRequest>,
    ) -> Result<Response<ExecuteQueryResponse>, Status> {
        let req = request.into_inner();
        let sql_query = req.sql_query;
        let query_id = Uuid::new_v4().to_string();

        info!("Executing query {}: {}", query_id, sql_query);

        // Increment active queries
        {
            let mut active_queries = self.state.active_queries.write().await;
            *active_queries += 1;
        }

        // Simulate query planning and execution
        let start_time = std::time::Instant::now();
        
        // Generate mock results based on query content
        let results = if sql_query.to_lowercase().contains("users") {
            vec![
                vec!["John Doe".to_string(), "30".to_string()],
                vec!["Jane Smith".to_string(), "25".to_string()],
                vec!["Bob Johnson".to_string(), "35".to_string()],
            ]
        } else if sql_query.to_lowercase().contains("orders") {
            vec![
                vec!["ORD001".to_string(), "150.00".to_string()],
                vec!["ORD002".to_string(), "275.50".to_string()],
                vec!["ORD003".to_string(), "89.99".to_string()],
            ]
        } else {
            vec![
                vec!["Sample Result 1".to_string()],
                vec!["Sample Result 2".to_string()],
            ]
        };

        let execution_time = start_time.elapsed();

        // Decrement active queries
        {
            let mut active_queries = self.state.active_queries.write().await;
            *active_queries -= 1;
        }

        // Increment total queries
        {
            let mut total_queries = self.state.total_queries.write().await;
            *total_queries += 1;
        }

        let query_result = QueryResult {
            query_id: query_id.clone(),
            sql_query: sql_query.clone(),
            execution_time_ms: execution_time.as_millis() as u64,
            rows_returned: results.len() as u32,
            results: results.into_iter().map(|row| common::proto::Row {
                values: row,
                metadata: std::collections::HashMap::new(),
            }).collect(),
            status: "completed".to_string(),
            timestamp: Some(prost_types::Timestamp {
                seconds: chrono::Utc::now().timestamp(),
                nanos: 0,
            }),
        };

        info!("Query {} completed in {}ms", query_id, execution_time.as_millis());

        Ok(Response::new(ExecuteQueryResponse {
            success: true,
            result: Some(query_result),
            message: "Query executed successfully".to_string(),
        }))
    }

    async fn get_system_status(
        &self,
        _request: Request<GetSystemStatusRequest>,
    ) -> Result<Response<GetSystemStatusResponse>, Status> {
        match self.state.get_system_status().await {
            Ok(status) => {
                let mut components = std::collections::HashMap::new();
                
                for (id, comp) in status.components {
                    components.insert(id, ProtoComponentStatus {
                        id: comp.id,
                        status: comp.status,
                        cpu_usage: comp.cpu_usage,
                        memory_usage: comp.memory_usage,
                        active_connections: comp.active_connections,
                        last_heartbeat: Some(prost_types::Timestamp {
                            seconds: comp.last_heartbeat.timestamp(),
                            nanos: 0,
                        }),
                    });
                }

                let proto_status = ProtoSystemStatus {
                    components,
                    total_queries: status.total_queries,
                    active_queries: status.active_queries,
                    system_uptime: Some(prost_types::Duration {
                        seconds: status.system_uptime.num_seconds(),
                        nanos: 0,
                    }),
                    last_updated: Some(prost_types::Timestamp {
                        seconds: status.last_updated.timestamp(),
                        nanos: 0,
                    }),
                };

                Ok(Response::new(GetSystemStatusResponse {
                    success: true,
                    status: Some(proto_status),
                    message: "System status retrieved successfully".to_string(),
                }))
            }
            Err(e) => {
                error!("Failed to get system status: {}", e);
                Ok(Response::new(GetSystemStatusResponse {
                    success: false,
                    status: None,
                    message: format!("Failed to get system status: {}", e),
                }))
            }
        }
    }
}
