# Usage Guide - Distributed SQL Query Engine

## Quick Start

### 1. Build the Project

```bash
# Compile the project
mvn clean compile

# Generate protobuf classes
mvn protobuf:compile protobuf:compile-custom
```

### 2. Start the System

**Option A: Use the startup script (recommended)**
```bash
./start_system.sh
```

**Option B: Manual startup**
```bash
# Terminal 1 - Worker 1
mvn exec:java -Dexec.mainClass="com.distributed.sql.worker.WorkerMain" -Dexec.args="worker1 50052"

# Terminal 2 - Worker 2  
mvn exec:java -Dexec.mainClass="com.distributed.sql.worker.WorkerMain" -Dexec.args="worker2 50053"

# Terminal 3 - Worker 3
mvn exec:java -Dexec.mainClass="com.distributed.sql.worker.WorkerMain" -Dexec.args="worker3 50054"

# Terminal 4 - Coordinator
mvn exec:java -Dexec.mainClass="com.distributed.sql.coordinator.CoordinatorMain" -Dexec.args="50051"
```

### 3. Start the Client

```bash
mvn exec:java -Dexec.mainClass="com.distributed.sql.client.SQLClient"
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
./stop_system.sh
```

## Troubleshooting

### Common Issues

1. **Port already in use**
   - Check if ports 50051-50054 are available
   - Use `lsof -i :50051` to check port usage

2. **Maven not found**
   - Install Maven or use the system package manager
   - Ensure Java 17+ is installed

3. **Protobuf compilation errors**
   - Run `mvn protobuf:compile protobuf:compile-custom`
   - Check that protobuf plugin is properly configured

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

- **SQL Parsing**: Converts SQL to structured queries
- **Query Planning**: Creates execution plans with cost estimation
- **Distributed Execution**: Parallel processing across workers
- **Data Partitioning**: Simulated data distribution
- **Fault Tolerance**: Checkpointing and retry mechanisms
- **Instrumentation**: Query tracing and performance monitoring
- **gRPC Communication**: High-performance RPC between components

## Sample Data

The system includes sample datasets:
- `data/users.csv` - 20 user records with id, name, age, email, city, salary
- `data/orders.csv` - 20 order records with order_id, user_id, product, price, etc.

Each worker processes a partition of this data, simulating a real distributed database.
