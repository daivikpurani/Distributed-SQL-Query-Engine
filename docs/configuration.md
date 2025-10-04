# Configuration Guide

This guide covers configuration options for the Distributed SQL Query Engine built in Rust.

## Service Configuration

### Coordinator Configuration

#### Command Line Arguments

```bash
cargo run --bin coordinator -- --port 50051
```

- `port`: Coordinator server port (default: 50051)

#### Environment Variables

```bash
RUST_LOG=debug cargo run --bin coordinator
```

Available environment variables:

- `RUST_LOG`: Logging level (debug, info, warn, error)
- `COORDINATOR_PORT`: Coordinator server port

### Worker Configuration

#### Command Line Arguments

```bash
cargo run --bin worker -- --worker-id worker1 --port 50052 --data-dir data
```

Parameters:

- `worker-id`: Unique worker identifier
- `port`: Worker server port
- `data-dir`: Path to data files (default: "data")

#### Example

```bash
cargo run --bin worker -- --worker-id worker1 --port 50052 --data-dir data
```

### Client Configuration

#### Command Line Arguments

```bash
cargo run --bin client -- --query "SELECT name FROM users"
```

- `query`: SQL query to execute (optional)

## Sharding Configuration

The sharding system is configured through the `ShardManager` struct in Rust:

```rust
let mut shard_manager = ShardManager::new();

// Register workers
shard_manager.register_worker("worker1".to_string());
shard_manager.register_worker("worker2".to_string());
shard_manager.register_worker("worker3".to_string());

// Configure table distributions
shard_manager.create_table_distribution(
    "users".to_string(),
    ShardType::HashBased,
    vec!["user_id".to_string()],
    3
);
```

### Shard Types Configuration

#### Hash-based Sharding

```rust
// Even distribution using hash of shard key
shard_manager.create_table_distribution(
    "users".to_string(),
    ShardType::HashBased,
    vec!["user_id".to_string()],
    3
);
```

#### Range-based Sharding

```rust
// Range-based distribution
let range_params = HashMap::from([
    ("ranges".to_string(), vec!["0-1000".to_string(), "1001-2000".to_string(), "2001-3000".to_string()])
]);

let mut distribution = ShardDistribution::new(
    "orders".to_string(),
    ShardType::RangeBased,
    vec!["order_id".to_string()],
    3
);
distribution.set_distribution_params(range_params);
```

#### Round-robin Sharding

```rust
// Round-robin distribution
shard_manager.create_table_distribution(
    "logs".to_string(),
    ShardType::RoundRobin,
    vec!["id".to_string()],
    3
);
```

### Replication Configuration

```rust
// Configure replicated sharding
let mut distribution = ShardDistribution::new(
    "critical_table".to_string(),
    ShardType::HashBased,
    vec!["id".to_string()],
    3
);
distribution.set_replicated(true);
distribution.set_replication_factor(2);
```

## Data Configuration

### Data Directory Structure

```
data/
├── users.csv
├── orders.csv
└── products.csv
```

### CSV File Format

The engine expects CSV files with headers. Example:

```csv
user_id,name,age,city,salary
1,John Doe,30,New York,75000
2,Jane Smith,25,Los Angeles,65000
```

```csv
order_id,user_id,product_name,quantity,price,order_date,status
1001,1,Laptop,1,1200.00,2024-01-15,completed
1002,2,Mouse,2,25.50,2024-01-16,completed
```

### Data Loading Configuration

```rust
// Configure data loading in ExecutionEngine
impl ExecutionEngine {
    pub fn new(worker_id: String, data_directory: String) -> Self {
        let mut engine = Self {
            worker_id,
            data_directory,
            table_cache: HashMap::new(),
            shard_registry: HashMap::new(),
        };

        // Load tables with sharding
        engine.load_sharded_table_data("users".to_string());
        engine.load_sharded_table_data("orders".to_string());

        engine
    }
}
```

## Network Configuration

### gRPC Configuration

#### Server Configuration

```rust
// Configure gRPC server
let server = Server::builder()
    .add_service(WorkerServiceServer::new(WorkerServiceImpl::new()))
    .serve(addr)
    .await?;
```

#### Client Configuration

```rust
// Configure gRPC client
let channel = Channel::from_static("http://localhost:50051")
    .max_message_size(4 * 1024 * 1024)
    .connect()
    .await?;
```

### Connection Pooling

```rust
// Configure connection pool
let mut worker_channels: HashMap<String, Channel> = HashMap::new();

for worker_id in &["worker1", "worker2", "worker3"] {
    let channel = Channel::from_static(&format!("http://localhost:5005{}", worker_id.chars().last().unwrap()))
        .connect()
        .await?;
    worker_channels.insert(worker_id.to_string(), channel);
}
```

## Performance Configuration

### Memory Configuration

```rust
// Configure memory limits
const MAX_MEMORY_BYTES: usize = 1024 * 1024 * 1024; // 1GB
const CHUNK_SIZE: usize = 1024 * 1024; // 1MB chunks
```

### Query Timeout Configuration

```rust
// Configure query timeouts
pub struct QueryExecutor {
    timeout_duration: Duration,
}

impl QueryExecutor {
    pub fn new() -> Self {
        Self {
            timeout_duration: Duration::from_secs(30),
        }
    }
}
```

### Batch Size Configuration

```rust
// Configure batch processing
pub struct ExecutionEngine {
    batch_size: usize,
}

impl ExecutionEngine {
    pub fn new() -> Self {
        Self {
            batch_size: 1000,
        }
    }
}
```

## Logging Configuration

### Custom Logging

```rust
// Configure custom logging
use tracing_subscriber;

fn init_logging() {
    tracing_subscriber::fmt()
        .with_env_filter("debug")
        .init();
}
```

## Docker Configuration

### Dockerfile

```dockerfile
FROM rust:1.70-slim

WORKDIR /app
COPY . .

RUN cargo build --release

EXPOSE 50051

CMD ["./target/release/coordinator"]
```

### Docker Compose

```yaml
version: "3.8"
services:
  coordinator:
    build: .
    ports:
      - "50051:50051"
    command: ["./target/release/coordinator"]

  worker1:
    build: .
    command:
      ["./target/release/worker", "--worker-id", "worker1", "--port", "50052"]

  worker2:
    build: .
    command:
      ["./target/release/worker", "--worker-id", "worker2", "--port", "50053"]
```

## Environment Variables

### Development

```bash
export RUST_LOG=debug
export COORDINATOR_PORT=50051
export WORKER_COUNT=3
```

### Production

```bash
export RUST_LOG=info
export COORDINATOR_PORT=50051
export WORKER_COUNT=5
export MAX_MEMORY_BYTES=2147483648
```

## Configuration Validation

```rust
// Validate configuration
pub struct ConfigValidator;

impl ConfigValidator {
    pub fn validate_config(config: &Config) -> Result<(), ConfigError> {
        if config.port < 1024 {
            return Err(ConfigError::InvalidPort);
        }

        if config.worker_count == 0 {
            return Err(ConfigError::InvalidWorkerCount);
        }

        Ok(())
    }
}
```

## Configuration Testing

```rust
// Test configuration
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_config_validation() {
        let config = Config {
            port: 50051,
            worker_count: 3,
        };

        assert!(ConfigValidator::validate_config(&config).is_ok());
    }
}
```
