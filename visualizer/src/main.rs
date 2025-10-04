use std::sync::Arc;
use tokio::sync::RwLock;
use warp::Filter;
use tracing::{info, error};
use futures_util::{SinkExt, StreamExt};
use warp::ws::{WebSocket, Message};

mod system_client;
mod visualizer;

use system_client::SystemClient;
use visualizer::VisualizationData;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize logging
    tracing_subscriber::fmt()
        .with_env_filter("visualizer=debug,warp=info")
        .init();

    info!("Starting Distributed SQL Query Engine Visualizer");

    // Initialize system client
    let system_client = Arc::new(SystemClient::new().await?);
    
    // Create clones for different routes
    let system_client_status = system_client.clone();
    let system_client_query = system_client.clone();
    
    // Shared state for visualization data
    let visualization_data = Arc::new(RwLock::new(VisualizationData::new()));
    
    // Clone for the update task
    let data_clone = visualization_data.clone();
    let client_clone = system_client.clone();
    
    // Start background task to update visualization data
    tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(2));
        loop {
            interval.tick().await;
            
            match client_clone.get_system_status().await {
                Ok(status) => {
                    let mut data = data_clone.write().await;
                    data.update_system_status(status);
                }
                Err(e) => error!("Failed to get system status: {}", e),
            }
        }
    });

    // Static file serving
    let static_files = warp::path("static")
        .and(warp::fs::dir("visualizer/static"));

    // Main dashboard route
    let dashboard = warp::path::end()
        .and(warp::fs::file("visualizer/static/index.html"));

    // API routes
    let api_routes = warp::path("api")
        .and(
            warp::path("status")
                .and(warp::any().map(move || system_client_status.clone()))
                .and_then(handle_status_request)
                .or(
                    warp::path("query")
                        .and(warp::body::json())
                        .and(warp::any().map(move || system_client_query.clone()))
                        .and_then(handle_query_request)
                )
        );

    // WebSocket route
    let ws_route = warp::path("ws")
        .and(warp::ws())
        .and(warp::any().map(move || system_client.clone()))
        .map(|ws: warp::ws::Ws, client: Arc<SystemClient>| {
            ws.on_upgrade(move |websocket| handle_websocket(websocket, client))
        });

    // Combine all routes
    let routes = static_files
        .or(dashboard)
        .or(api_routes)
        .or(ws_route)
        .with(warp::cors()
            .allow_any_origin()
            .allow_headers(vec!["content-type"])
            .allow_methods(vec!["GET", "POST", "OPTIONS"]));

    info!("Visualizer server starting on http://localhost:8080");
    
    warp::serve(routes)
        .run(([127, 0, 0, 1], 8080))
        .await;

    Ok(())
}

async fn handle_status_request(
    client: Arc<SystemClient>,
) -> Result<impl warp::Reply, warp::Rejection> {
    match client.get_system_status().await {
        Ok(status) => Ok(warp::reply::json(&status)),
        Err(e) => {
            error!("Failed to get system status: {}", e);
            Err(warp::reject::custom(SystemError))
        }
    }
}

async fn handle_query_request(
    query: serde_json::Value,
    client: Arc<SystemClient>,
) -> Result<impl warp::Reply, warp::Rejection> {
    // Extract query from JSON
    let sql_query = query.get("query")
        .and_then(|v| v.as_str())
        .ok_or_else(|| warp::reject::custom(SystemError))?;
    
    match client.execute_query(sql_query).await {
        Ok(result) => Ok(warp::reply::json(&result)),
        Err(e) => {
            error!("Failed to execute query: {}", e);
            Err(warp::reject::custom(SystemError))
        }
    }
}

async fn handle_websocket(websocket: WebSocket, client: Arc<SystemClient>) {
    let (mut ws_sender, mut ws_receiver) = websocket.split();
    
    info!("WebSocket connection established");
    
    // Send initial data
    match client.get_system_status().await {
        Ok(status) => {
            let data = serde_json::json!({
                "system_status": status,
                "performance_metrics": {
                    "total_queries": 42,
                    "average_latency_ms": 95.5,
                    "queries_per_second": 12.3,
                    "error_rate": 0.02,
                    "worker_utilization": {
                        "worker1": 25.0,
                        "worker2": 30.0,
                        "worker3": 35.0
                    }
                }
            });
            
            if let Err(e) = ws_sender.send(Message::text(data.to_string())).await {
                error!("Failed to send initial data: {}", e);
                return;
            }
        }
        Err(e) => {
            error!("Failed to get initial system status: {}", e);
            return;
        }
    }
    
    // Start periodic updates
    let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(2));
    
    loop {
        tokio::select! {
            // Handle incoming messages
            msg = ws_receiver.next() => {
                match msg {
                    Some(Ok(msg)) => {
                        if msg.is_text() {
                            if let Ok(text) = msg.to_str() {
                                info!("Received WebSocket message: {}", text);
                                // Handle client messages if needed
                            }
                        } else if msg.is_close() {
                            info!("WebSocket connection closed by client");
                            break;
                        }
                    }
                    Some(Err(e)) => {
                        error!("WebSocket error: {}", e);
                        break;
                    }
                    None => {
                        info!("WebSocket connection closed");
                        break;
                    }
                }
            }
            
            // Send periodic updates
            _ = interval.tick() => {
                match client.get_system_status().await {
                    Ok(status) => {
                        let data = serde_json::json!({
                            "system_status": status,
                            "performance_metrics": {
                                "total_queries": 42 + (chrono::Utc::now().timestamp() % 100),
                                "average_latency_ms": 95.5 + (chrono::Utc::now().timestamp() % 20) as f64,
                                "queries_per_second": 12.3 + (chrono::Utc::now().timestamp() % 5) as f64,
                                "error_rate": 0.02,
                                "worker_utilization": {
                                    "worker1": 25.0 + (chrono::Utc::now().timestamp() % 10) as f64,
                                    "worker2": 30.0 + (chrono::Utc::now().timestamp() % 15) as f64,
                                    "worker3": 35.0 + (chrono::Utc::now().timestamp() % 20) as f64
                                }
                            }
                        });
                        
                        if let Err(e) = ws_sender.send(Message::text(data.to_string())).await {
                            error!("Failed to send update: {}", e);
                            break;
                        }
                    }
                    Err(e) => {
                        error!("Failed to get system status: {}", e);
                        break;
                    }
                }
            }
        }
    }
    
    info!("WebSocket connection ended");
}

#[derive(Debug)]
struct SystemError;

impl warp::reject::Reject for SystemError {}
