use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tonic::transport::Channel;
use std::io::{self, Write};
use tracing::info;
use anyhow::Result;
use clap::{Parser, Subcommand};
use serde_json;

use common::proto::{
    coordinator_service_client::CoordinatorServiceClient,
    ExecuteQueryRequest,
    GetSystemStatusRequest,
};

#[derive(Parser)]
#[command(name = "sql-client")]
#[command(about = "Distributed SQL Query Engine Client")]
#[command(version = "1.0")]
struct Cli {
    #[command(subcommand)]
    command: Commands,
    
    /// Coordinator address
    #[arg(long, default_value = "127.0.0.1:50051")]
    coordinator: String,
    
    /// Enable verbose logging
    #[arg(short, long)]
    verbose: bool,
}

#[derive(Subcommand)]
enum Commands {
    /// Execute a SQL query
    Query {
        /// SQL query to execute
        sql: String,
        
        /// Output format (json, table, csv)
        #[arg(short, long, default_value = "table")]
        format: String,
        
        /// Show execution statistics
        #[arg(short, long)]
        stats: bool,
    },
    
    /// Get system status
    Status {
        /// Output format (json, table)
        #[arg(short, long, default_value = "table")]
        format: String,
    },
    
    /// Interactive SQL shell
    Shell,
    
    /// Run queries from a file
    File {
        /// Path to SQL file
        path: String,
        
        /// Output format (json, table, csv)
        #[arg(short, long, default_value = "table")]
        format: String,
        
        /// Show execution statistics
        #[arg(short, long)]
        stats: bool,
    },
    
    /// Benchmark queries
    Benchmark {
        /// Number of iterations
        #[arg(short, long, default_value = "10")]
        iterations: u32,
        
        /// SQL query to benchmark
        sql: String,
    },
}

#[derive(Debug, Clone)]
pub struct QueryStats {
    pub execution_time_ms: u64,
    pub rows_returned: usize,
    pub query_id: String,
    pub timestamp: chrono::DateTime<chrono::Utc>,
}

#[derive(Debug, serde::Serialize)]
pub struct SystemStats {
    pub total_queries: u64,
    pub active_queries: u32,
    pub components: HashMap<String, ComponentInfo>,
    pub system_uptime: chrono::Duration,
}

#[derive(Debug, serde::Serialize)]
pub struct ComponentInfo {
    pub id: String,
    pub status: String,
    pub cpu_usage: f64,
    pub memory_usage: f64,
    pub active_connections: u32,
}

pub struct SqlClient {
    coordinator_client: CoordinatorServiceClient<Channel>,
    stats_history: Arc<RwLock<Vec<QueryStats>>>,
}

impl SqlClient {
    pub async fn new(coordinator_address: &str) -> Result<Self> {
        let addr = format!("http://{}", coordinator_address);
        let channel = Channel::from_shared(addr)?
            .connect()
            .await?;
        
        let client = CoordinatorServiceClient::new(channel);
        
        Ok(Self {
            coordinator_client: client,
            stats_history: Arc::new(RwLock::new(Vec::new())),
        })
    }

    pub async fn execute_query(&self, sql: &str) -> Result<(Vec<Vec<String>>, QueryStats)> {
        let start_time = std::time::Instant::now();
        
        info!("Executing query: {}", sql);
        
        let mut client = self.coordinator_client.clone();
        let request = tonic::Request::new(ExecuteQueryRequest {
            sql_query: sql.to_string(),
        });

        let response = client.execute_query(request).await?;
        let response_inner = response.into_inner();
        
        if !response_inner.success {
            return Err(anyhow::anyhow!("Query execution failed: {}", response_inner.message));
        }

        let result = response_inner.result.unwrap();
        let execution_time = start_time.elapsed();
        
        // Convert proto results to Vec<Vec<String>>
        let results: Vec<Vec<String>> = result.results
            .into_iter()
            .map(|row| row.values)
            .collect();

        let stats = QueryStats {
            execution_time_ms: result.execution_time_ms,
            rows_returned: results.len(),
            query_id: result.query_id,
            timestamp: chrono::DateTime::from_timestamp(
                result.timestamp.unwrap().seconds,
                0
            ).unwrap_or_else(|| chrono::Utc::now()),
        };

        // Store stats for history
        {
            let mut history = self.stats_history.write().await;
            history.push(stats.clone());
            if history.len() > 100 {
                history.remove(0);
            }
        }

        info!("Query completed in {}ms, returned {} rows", 
              stats.execution_time_ms, stats.rows_returned);

        Ok((results, stats))
    }

    pub async fn get_system_status(&self) -> Result<SystemStats> {
        let mut client = self.coordinator_client.clone();
        let request = tonic::Request::new(GetSystemStatusRequest {});

        let response = client.get_system_status(request).await?;
        let response_inner = response.into_inner();
        
        if !response_inner.success {
            return Err(anyhow::anyhow!("Failed to get system status: {}", response_inner.message));
        }

        let status = response_inner.status.unwrap();
        let mut components = HashMap::new();
        
        for (id, comp) in status.components {
            components.insert(id, ComponentInfo {
                id: comp.id,
                status: comp.status,
                cpu_usage: comp.cpu_usage,
                memory_usage: comp.memory_usage,
                active_connections: comp.active_connections,
            });
        }

        Ok(SystemStats {
            total_queries: status.total_queries,
            active_queries: status.active_queries,
            components,
            system_uptime: chrono::Duration::seconds(status.system_uptime.unwrap().seconds),
        })
    }

    pub async fn get_stats_history(&self) -> Vec<QueryStats> {
        let history = self.stats_history.read().await;
        history.clone()
    }

    fn format_results(&self, results: &[Vec<String>], format: &str) -> String {
        match format {
            "json" => {
                serde_json::to_string_pretty(results).unwrap_or_else(|_| "[]".to_string())
            }
            "csv" => {
                if results.is_empty() {
                    return String::new();
                }
                
                let mut csv = String::new();
                for row in results {
                    csv.push_str(&row.join(","));
                    csv.push('\n');
                }
                csv
            }
            "table" => {
                if results.is_empty() {
                    return "No results".to_string();
                }
                
                let mut table = String::new();
                
                // Calculate column widths
                let num_cols = results[0].len();
                let mut col_widths = vec![0; num_cols];
                
                for row in results {
                    for (i, cell) in row.iter().enumerate() {
                        col_widths[i] = col_widths[i].max(cell.len());
                    }
                }
                
                // Print header
                for (i, width) in col_widths.iter().enumerate() {
                    table.push_str(&format!("{:width$} ", results[0][i], width = width));
                }
                table.push('\n');
                
                // Print separator
                for width in &col_widths {
                    table.push_str(&format!("{:-<width$} ", "", width = width));
                }
                table.push('\n');
                
                // Print data rows
                for row in results.iter().skip(1) {
                    for (i, cell) in row.iter().enumerate() {
                        table.push_str(&format!("{:width$} ", cell, width = col_widths[i]));
                    }
                    table.push('\n');
                }
                
                table
            }
            _ => {
                format!("Unsupported format: {}", format)
            }
        }
    }

    fn format_stats(&self, stats: &QueryStats) -> String {
        format!(
            "Query ID: {}\nExecution Time: {}ms\nRows Returned: {}\nTimestamp: {}",
            stats.query_id,
            stats.execution_time_ms,
            stats.rows_returned,
            stats.timestamp.format("%Y-%m-%d %H:%M:%S UTC")
        )
    }

    fn format_system_stats(&self, stats: &SystemStats) -> String {
        let mut output = String::new();
        
        output.push_str(&format!("System Status:\n"));
        output.push_str(&format!("Total Queries: {}\n", stats.total_queries));
        output.push_str(&format!("Active Queries: {}\n", stats.active_queries));
        output.push_str(&format!("System Uptime: {}s\n\n", stats.system_uptime.num_seconds()));
        
        output.push_str("Components:\n");
        output.push_str("ID         Status    CPU%   Memory(MB) Connections\n");
        output.push_str("---------- -------- ------ ---------- -----------\n");
        
        for (id, comp) in &stats.components {
            output.push_str(&format!(
                "{:<10} {:<8} {:<6.1} {:<10.1} {:<11}\n",
                id, comp.status, comp.cpu_usage, comp.memory_usage, comp.active_connections
            ));
        }
        
        output
    }

    async fn interactive_shell(&self, coordinator_address: &str) -> Result<()> {
        println!("Distributed SQL Query Engine - Interactive Shell");
        println!("Type 'help' for commands, 'exit' to quit");
        println!("Connected to coordinator at: {}", coordinator_address);
        
        loop {
            print!("sql> ");
            io::stdout().flush()?;
            
            let mut input = String::new();
            io::stdin().read_line(&mut input)?;
            let input = input.trim();
            
            if input.is_empty() {
                continue;
            }
            
            match input {
                "exit" | "quit" => break,
                "help" => {
                    println!("Available commands:");
                    println!("  help     - Show this help");
                    println!("  status   - Show system status");
                    println!("  stats    - Show query statistics");
                    println!("  exit     - Exit the shell");
                    println!("  <sql>    - Execute SQL query");
                }
                "status" => {
                    match self.get_system_status().await {
                        Ok(stats) => println!("{}", self.format_system_stats(&stats)),
                        Err(e) => eprintln!("Error getting status: {}", e),
                    }
                }
                "stats" => {
                    let history = self.get_stats_history().await;
                    if history.is_empty() {
                        println!("No queries executed yet");
                    } else {
                        println!("Query Statistics:");
                        println!("Total Queries: {}", history.len());
                        let avg_time: f64 = history.iter().map(|s| s.execution_time_ms as f64).sum::<f64>() / history.len() as f64;
                        println!("Average Execution Time: {:.2}ms", avg_time);
                        let total_rows: usize = history.iter().map(|s| s.rows_returned).sum();
                        println!("Total Rows Returned: {}", total_rows);
                    }
                }
                _ => {
                    match self.execute_query(input).await {
                        Ok((results, stats)) => {
                            println!("{}", self.format_results(&results, "table"));
                            println!("\n{}", self.format_stats(&stats));
                        }
                        Err(e) => eprintln!("Query error: {}", e),
                    }
                }
            }
        }
        
        Ok(())
    }

    async fn benchmark_query(&self, sql: &str, iterations: u32) -> Result<()> {
        println!("Benchmarking query: {}", sql);
        println!("Iterations: {}", iterations);
        
        let mut times = Vec::new();
        let mut total_rows = 0;
        
        for i in 1..=iterations {
            print!("Iteration {}/{}... ", i, iterations);
            io::stdout().flush()?;
            
            match self.execute_query(sql).await {
                Ok((results, stats)) => {
                    times.push(stats.execution_time_ms);
                    total_rows += stats.rows_returned;
                    println!("{}ms", stats.execution_time_ms);
                }
                Err(e) => {
                    println!("Error: {}", e);
                    return Err(e);
                }
            }
        }
        
        // Calculate statistics
        times.sort();
        let min = times[0];
        let max = times[times.len() - 1];
        let avg: f64 = times.iter().sum::<u64>() as f64 / times.len() as f64;
        let median = if times.len() % 2 == 0 {
            (times[times.len() / 2 - 1] + times[times.len() / 2]) as f64 / 2.0
        } else {
            times[times.len() / 2] as f64
        };
        
        println!("\nBenchmark Results:");
        println!("==================");
        println!("Query: {}", sql);
        println!("Iterations: {}", iterations);
        println!("Total Rows: {}", total_rows);
        println!("Min Time: {}ms", min);
        println!("Max Time: {}ms", max);
        println!("Avg Time: {:.2}ms", avg);
        println!("Median Time: {:.2}ms", median);
        
        Ok(())
    }
}

#[tokio::main]
async fn main() -> Result<()> {
    let cli = Cli::parse();
    
    // Initialize logging
    if cli.verbose {
        tracing_subscriber::fmt()
            .with_env_filter("sql_client=debug,tonic=info")
            .init();
    } else {
        tracing_subscriber::fmt()
            .with_env_filter("sql_client=info")
            .init();
    }

    let client = SqlClient::new(&cli.coordinator).await?;
    
    match cli.command {
        Commands::Query { sql, format, stats } => {
            match client.execute_query(&sql).await {
                Ok((results, query_stats)) => {
                    println!("{}", client.format_results(&results, &format));
                    if stats {
                        println!("\n{}", client.format_stats(&query_stats));
                    }
                }
                Err(e) => {
                    eprintln!("Query execution failed: {}", e);
                    std::process::exit(1);
                }
            }
        }
        
        Commands::Status { format } => {
            match client.get_system_status().await {
                Ok(system_stats) => {
                    if format == "json" {
                        println!("{}", serde_json::to_string_pretty(&system_stats)?);
                    } else {
                        println!("{}", client.format_system_stats(&system_stats));
                    }
                }
                Err(e) => {
                    eprintln!("Failed to get system status: {}", e);
                    std::process::exit(1);
                }
            }
        }
        
        Commands::Shell => {
            if let Err(e) = client.interactive_shell(&cli.coordinator).await {
                eprintln!("Shell error: {}", e);
                std::process::exit(1);
            }
        }
        
        Commands::File { path, format, stats } => {
            let content = std::fs::read_to_string(&path)?;
            let queries: Vec<&str> = content.split(';').map(|q| q.trim()).filter(|q| !q.is_empty()).collect();
            
            for (i, query) in queries.iter().enumerate() {
                println!("Executing query {}: {}", i + 1, query);
                match client.execute_query(query).await {
                    Ok((results, query_stats)) => {
                        println!("{}", client.format_results(&results, &format));
                        if stats {
                            println!("\n{}", client.format_stats(&query_stats));
                        }
                    }
                    Err(e) => {
                        eprintln!("Query {} failed: {}", i + 1, e);
                    }
                }
                println!();
            }
        }
        
        Commands::Benchmark { iterations, sql } => {
            if let Err(e) = client.benchmark_query(&sql, iterations).await {
                eprintln!("Benchmark failed: {}", e);
                std::process::exit(1);
            }
        }
    }

    Ok(())
}