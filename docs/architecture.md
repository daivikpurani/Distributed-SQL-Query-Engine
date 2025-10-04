# Architecture Documentation

## System Overview

The Distributed SQL Query Engine is designed as a distributed system that can process SQL queries across multiple worker nodes. The architecture follows a coordinator-worker pattern with sharding support, built entirely in Rust for high performance and memory safety.

## Component Architecture

### Coordinator Node

The coordinator is the central component that manages the entire system:

```
┌─────────────────────────────────────────────────────────────┐
│                        Coordinator                           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ SQL Parser  │  │Query Planner│  │   Query Executor    │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │Shard Manager│  │Worker Client│  │   Fault Tolerance   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### Components:

1. **SQL Parser** (`parser.rs`)

   - Parses SQL queries into internal AST representation using nom parser combinators
   - Validates query syntax and semantics
   - Extracts query components (SELECT, FROM, WHERE, JOIN)

2. **Query Planner** (`planner.rs`)

   - Creates optimal execution plans
   - Implements shard-aware planning
   - Optimizes query execution across workers

3. **Shard Manager** (`shard_manager.rs`)

   - Manages data distribution across workers
   - Handles shard creation, migration, and rebalancing
   - Tracks shard ownership and metadata

4. **Query Executor** (`executor.rs`)

   - Coordinates query execution across workers using Tokio async runtime
   - Manages result aggregation
   - Handles fault tolerance and recovery

5. **Worker Client** (`client.rs`)
   - gRPC client for worker communication using Tonic
   - Manages worker connections and health checks

### Worker Nodes

Workers execute query plan nodes on their local data:

```
┌─────────────────────────────────────────────────────────────┐
│                        Worker Node                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │Execution    │  │Shard        │  │   Fault Tolerance   │  │
│  │Engine       │  │Management   │  │   Manager           │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                Local Data Storage                      │  │
│  │         (Sharded Tables and Indexes)                  │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### Components:

1. **Execution Engine** (`execution_engine.rs`)

   - Executes query plan nodes locally using Rust's zero-cost abstractions
   - Handles data scanning, filtering, and joining
   - Manages local shard data with memory safety

2. **Shard Management**

   - Tracks owned shards and their metadata
   - Handles shard data loading and caching
   - Implements shard-specific operations

3. **Fault Tolerance Manager** (`fault_tolerance.rs`)
   - Implements checkpointing mechanisms
   - Handles recovery from failures
   - Manages state persistence

## Data Flow

### Query Execution Flow

```
Client Query
     ↓
SQL Parser → Query AST
     ↓
Query Planner → Execution Plan
     ↓
Shard Manager → Shard Assignment
     ↓
Query Executor → Worker Coordination
     ↓
Workers → Local Execution
     ↓
Result Aggregation → Client Response
```

### Sharding Flow

```
Table Data
     ↓
Shard Key Extraction
     ↓
Hash Calculation
     ↓
Shard Assignment
     ↓
Worker Distribution
     ↓
Local Storage
```

## Communication Protocol

### gRPC Services

The system uses gRPC for inter-node communication with Tonic:

1. **WorkerService**: Worker-to-coordinator communication

   - Query execution requests
   - Health checks
   - Status reporting

2. **CoordinatorService**: Coordinator-to-worker communication
   - Query plan distribution
   - Shard management commands
   - Fault tolerance coordination

### Message Types

- **QueryRequest**: Query execution requests
- **QueryResponse**: Query results and metadata
- **ShardInfo**: Shard metadata and status
- **WorkerStatus**: Worker health and capacity information

## Sharding Strategy

### Shard Types

1. **Hash-based Sharding**

   - Distributes data using hash of shard key
   - Ensures even distribution
   - Supports consistent hashing

2. **Range-based Sharding**

   - Distributes data based on value ranges
   - Optimized for range queries
   - Supports ordered data access

3. **Round-robin Sharding**
   - Distributes data in round-robin fashion
   - Simple and predictable
   - Good for uniform workloads

### Shard Management

- **Shard Creation**: Automatic shard creation based on table schema
- **Shard Assignment**: Workers assigned specific shards based on capacity
- **Shard Migration**: Dynamic shard movement for load balancing
- **Shard Recovery**: Automatic recovery from worker failures

## Fault Tolerance

### Failure Detection

- **Health Checks**: Regular worker health monitoring
- **Heartbeat Mechanism**: Worker status reporting
- **Timeout Handling**: Request timeout and retry logic

### Recovery Mechanisms

- **Checkpointing**: Periodic state persistence
- **Shard Replication**: Multiple copies of critical shards
- **Automatic Failover**: Worker replacement and shard migration

## Performance Considerations

### Optimization Strategies

1. **Query Optimization**

   - Predicate pushdown
   - Join order optimization
   - Column pruning

2. **Shard Optimization**

   - Load balancing
   - Data locality
   - Parallel processing

3. **Network Optimization**
   - Connection pooling
   - Batch processing
   - Compression

### Monitoring and Metrics

- Query execution time
- Worker utilization
- Shard distribution
- Network latency
- Error rates

## Scalability

### Horizontal Scaling

- Add more worker nodes
- Redistribute shards
- Load balance queries

### Vertical Scaling

- Increase worker capacity
- Optimize query plans
- Improve network performance

## Security Considerations

- Authentication and authorization
- Data encryption in transit
- Secure worker communication
- Access control and auditing
