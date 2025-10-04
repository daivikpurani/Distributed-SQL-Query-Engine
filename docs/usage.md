# Usage Guide - Distributed SQL Query Engine

## Quick Start

### 1. Build the Project

```bash
# Compile the project
cargo build --release

# Generate protobuf classes
cargo run --bin build
```

### 2. Start the System

**Option A: Use the startup script (recommended)**

```bash
./scripts/start_system.sh
```

**Option B: Manual startup**

```bash
# Terminal 1 - Worker 1
cargo run --bin worker -- --worker-id worker1 --port 50052

# Terminal 2 - Worker 2
cargo run --bin worker -- --worker-id worker2 --port 50053

# Terminal 3 - Worker 3
cargo run --bin worker -- --worker-id worker3 --port 50054

# Terminal 4 - Coordinator
cargo run --bin coordinator -- --port 50051
```

### 3. Start the Client

```bash
cargo run --bin client -- --query "SELECT name FROM users"
```

## Example Queries

Once the client is running, try these example queries:

### Basic Queries

```sql
-- Show all users
SELECT * FROM users;

-- Select specific columns
SELECT name, age, city FROM users;

-- Filter by age
SELECT name, age FROM users WHERE age > 30;

-- Multiple conditions
SELECT name, salary FROM users WHERE age > 25 AND salary > 70000;
```

### JOIN Queries

```sql
-- Join users with orders
SELECT u.name, o.product_name, o.price
FROM users u
JOIN orders o ON u.user_id = o.user_id;

-- Join with filtering
SELECT u.name, o.product_name
FROM users u
JOIN orders o ON u.user_id = o.user_id
WHERE u.age > 30;
```

## Client Commands

- `help` - Show available commands
- `status` - Show worker node status
- `quit` or `exit` - Exit the client

## Stopping the System

```bash
./scripts/stop_system.sh
```

## Troubleshooting

### Common Issues

1. **Port already in use**

   - Check if ports 50051-50054 are available
   - Use `lsof -i :50051` to check port usage

2. **Rust not found**

   - Install Rust using rustup: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`
   - Ensure Rust 1.70+ is installed

3. **Protobuf compilation errors**
   - Install protoc: `brew install protobuf` (macOS) or `apt install protobuf-compiler` (Ubuntu)
   - Run `cargo run --bin build` to regenerate protobuf code

### Logs

- System logs are written to the `logs/` directory
- Each component has its own log file
- Check logs for detailed error information

## Architecture Overview

```
Client (CLI)
    ↓ SQL Query
Coordinator (Port 50051)
    ↓ Task Distribution
Worker 1 (Port 50052) ←→ Worker 2 (Port 50053) ←→ Worker 3 (Port 50054)
    ↓ Results Aggregation
Coordinator
    ↓ Query Response
Client
```

## Features Demonstrated

- **SQL Parsing**: Converts SQL to structured queries using nom parser combinators
- **Query Planning**: Creates execution plans with cost estimation
- **Distributed Execution**: Parallel processing across workers using Tokio
- **Data Partitioning**: Simulated data distribution with sharding
- **Fault Tolerance**: Checkpointing and retry mechanisms
- **Instrumentation**: Query tracing and performance monitoring
- **gRPC Communication**: High-performance RPC between components

## Sample Data

The system includes sample datasets:

- `data/users.csv` - 20 user records with id, name, age, email, city, salary
- `data/orders.csv` - 20 order records with order_id, user_id, product, price, etc.

Each worker processes a partition of this data, simulating a real distributed database.
