use std::sync::Arc;
use tonic::{Request, Response, Status};
use tracing::{info, error};

use crate::{WorkerState, QueryResult};

use common::proto::{
    worker_service_server::WorkerService,
    ExecuteQueryRequest, ExecuteQueryResponse,
    GetWorkerStatusRequest, GetWorkerStatusResponse,
    HealthRequest, HealthResponse,
    WorkerStatus,
};

pub struct WorkerServiceImpl {
    state: Arc<WorkerState>,
}

impl WorkerServiceImpl {
    pub fn new(state: Arc<WorkerState>) -> Self {
        Self { state }
    }
}

#[tonic::async_trait]
impl WorkerService for WorkerServiceImpl {
    async fn execute_query(
        &self,
        request: Request<ExecuteQueryRequest>,
    ) -> Result<Response<ExecuteQueryResponse>, Status> {
        let req = request.into_inner();
        let sql_query = req.sql_query;

        info!("Worker {} received query: {}", self.state.worker_id, sql_query);

        match self.state.execute_query(&sql_query).await {
            Ok(query_result) => {
                let proto_result = common::proto::QueryResult {
                    query_id: query_result.query_id,
                    sql_query: query_result.sql_query,
                    execution_time_ms: query_result.execution_time_ms,
                    rows_returned: query_result.rows_returned,
                    results: query_result.results.into_iter().map(|row| common::proto::Row {
                        values: row,
                        metadata: std::collections::HashMap::new(),
                    }).collect(),
                    status: query_result.status,
                    timestamp: query_result.timestamp,
                };

                Ok(Response::new(ExecuteQueryResponse {
                    success: true,
                    result: Some(proto_result),
                    message: "Query executed successfully".to_string(),
                }))
            }
            Err(e) => {
                error!("Worker {} failed to execute query: {}", self.state.worker_id, e);
                Ok(Response::new(ExecuteQueryResponse {
                    success: false,
                    result: None,
                    message: format!("Query execution failed: {}", e),
                }))
            }
        }
    }

    async fn get_worker_status(
        &self,
        _request: Request<GetWorkerStatusRequest>,
    ) -> Result<Response<GetWorkerStatusResponse>, Status> {
        let metrics = self.state.get_metrics().await;
        
        let status = WorkerStatus {
            worker_id: self.state.worker_id.clone(),
            status: "healthy".to_string(),
            cpu_usage: metrics.cpu_usage,
            memory_usage: metrics.memory_usage,
            active_queries: metrics.active_queries,
            total_queries: metrics.total_queries,
            uptime: Some(prost_types::Duration {
                seconds: (chrono::Utc::now() - self.state.start_time).num_seconds(),
                nanos: 0,
            }),
            last_heartbeat: Some(prost_types::Timestamp {
                seconds: metrics.last_heartbeat.timestamp(),
                nanos: 0,
            }),
        };

        Ok(Response::new(GetWorkerStatusResponse {
            success: true,
            status: Some(status),
            message: "Worker status retrieved successfully".to_string(),
        }))
    }

    async fn health_check(
        &self,
        request: Request<HealthRequest>,
    ) -> Result<Response<HealthResponse>, Status> {
        let req = request.into_inner();
        let worker_id = req.worker_id;
        
        let metrics = self.state.get_metrics().await;
        
        Ok(Response::new(HealthResponse {
            worker_id: worker_id.clone(),
            healthy: true,
            timestamp: chrono::Utc::now().timestamp(),
            status_message: format!("Worker {} is healthy", worker_id),
        }))
    }
}
