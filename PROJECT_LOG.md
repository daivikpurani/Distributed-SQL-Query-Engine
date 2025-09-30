# Distributed SQL Query Engine - Comprehensive Project Log

## Project Overview

This is a **Distributed SQL Query Engine** implemented in Java that demonstrates the internal workings of distributed database systems like Presto, SparkSQL, or Snowflake. The project simulates how queries are processed across multiple worker nodes with fault tolerance, instrumentation, and gRPC-based communication.

### Key Characteristics
- **Language**: Java 17+
- **Build Tool**: Maven 3.6+
- **Communication**: gRPC with Protocol Buffers
- **Architecture**: Microservices-based distributed system
- **Purpose**: Educational demonstration of distributed SQL processing concepts

## System Architecture

### High-Level Architecture
```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│    Client   │───▶│ Coordinator  │───▶│   Workers   │
│   (CLI)     │    │   (Port     │    │ (Ports      │
│             │    │   50051)    │    │ 50052-54)   │
└─────────────┘    └──────────────┘    └─────────────┘
```

### Component Breakdown

#### 1. Coordinator (Central Orchestrator)
- **Port**: 50051
- **Role**: Query parsing, planning, and execution coordination
- **Key Classes**:
  - `CoordinatorMain`: Entry point and configuration
  - `CoordinatorServer`: gRPC server implementation
  - `QueryPlanner`: Rule-based query planning
  - `QueryExecutor`: Task distribution and result aggregation
  - `SQLParser`: Regex-based SQL parsing
  - `WorkerClient`: Communication with worker nodes

#### 2. Worker Nodes (Data Processing Units)
- **Ports**: 50052, 50053, 50054
- **Role**: Execute query plan nodes on partitioned data
- **Key Classes**:
  - `WorkerMain`: Entry point for worker processes
  - `WorkerServer`: gRPC server for task execution
  - `ExecutionEngine`: Query plan node execution
  - `FaultToleranceManager`: Checkpointing and failure simulation

#### 3. Client (User Interface)
- **Role**: CLI interface for query submission
- **Key Classes**:
  - `SQLClient`: Interactive command-line client

## Data Models and Protocol Definitions

### Core Data Models

#### Query Model (`Query.java`)
```java
public class Query {
    private String queryId;
    private String originalSql;
    private QueryType type;
    private List<String> selectColumns;
    private List<String> fromTables;
    private List<Condition> whereConditions;
    private List<Join> joins;
    private Map<String, Object> metadata;
    private long timestamp;
}
```

#### Plan Node Model (`PlanNode.java`)
```java
public class PlanNode {
    private String nodeId;
    private NodeType type;  // SCAN, FILTER, JOIN, PROJECT, AGGREGATE
    private String tableName;
    private List<String> columns;
    private List<Condition> conditions;
    private List<PlanNode> children;
    private int estimatedRows;
    private String workerId;
}
```

#### Result Set Model (`ResultSet.java`)
```java
public class ResultSet {
    private String queryId;
    private List<String> columnNames;
    private List<Row> rows;
    private long executionTimeMs;
    private int totalRows;
}
```

### Protocol Buffer Definitions (`query.proto`)

#### Services
- **CoordinatorService**: `ExecuteQuery`, `GetWorkerStatus`
- **WorkerService**: `ExecuteTask`, `Checkpoint`, `HealthCheck`

#### Key Messages
- `QueryRequest/QueryResponse`: Client ↔ Coordinator communication
- `TaskRequest/TaskResponse`: Coordinator ↔ Worker communication
- `PlanNode`: Query execution plan tree structure
- `Row`: Data row with values and metadata
- `CheckpointInfo`: Fault tolerance checkpoint data

#### Enums
- `QueryStatus`: PENDING, PLANNING, EXECUTING, COMPLETED, FAILED, CANCELLED
- `TaskStatus`: TASK_PENDING, TASK_RUNNING, TASK_COMPLETED, TASK_FAILED, TASK_CANCELLED
- `NodeType`: SCAN, FILTER, JOIN, PROJECT, AGGREGATE
- `Operator`: EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, etc.
- `DataType`: STRING, INTEGER, DOUBLE, BOOLEAN, DATE

## Query Execution Flow

### 1. Query Submission
```
Client → Coordinator: SQL Query via gRPC
```

### 2. Query Parsing (`SQLParser.java`)
- **Method**: Regex-based parsing using `SELECT_PATTERN`
- **Supported Operations**:
  - SELECT with column projection
  - FROM with table specification
  - WHERE with basic operators (=, !=, >, <, >=, <=)
  - JOIN with ON conditions
- **Output**: Structured `Query` object

### 3. Query Planning (`QueryPlanner.java`)
- **Strategy**: Rule-based planning (not cost-based)
- **Plan Construction**: Bottom-up tree building
- **Node Types**:
  - **SCAN**: Table access operations
  - **FILTER**: WHERE condition application
  - **PROJECT**: Column selection
  - **JOIN**: Table join operations
- **Worker Assignment**: Random assignment to available workers
- **Output**: `QueryPlan` with execution tree

### 4. Task Distribution (`QueryExecutor.java`)
- **Strategy**: Create subplans for each worker
- **Execution**: Parallel task execution using `CompletableFuture`
- **Timeout**: 30-second timeout for task completion
- **Result Aggregation**: Collect and merge results from all workers

### 5. Worker Execution (`ExecutionEngine.java`)
- **Data Loading**: CSV files loaded into memory cache
- **Partitioning**: Simulated data distribution across workers
- **Node Execution**:
  - **SCAN**: Read partitioned table data
  - **FILTER**: Apply WHERE conditions
  - **PROJECT**: Select specified columns
  - **JOIN**: Simple nested loop join implementation

### 6. Result Processing
- **Aggregation**: Coordinator merges partial results
- **Response**: Final result set returned to client

## Fault Tolerance Mechanisms

### Checkpointing (`FaultToleranceManager.java`)
- **Purpose**: Save partial execution state for recovery
- **Implementation**: In-memory checkpoint storage
- **Checkpoint Data**:
  - Partial results
  - Execution state
  - Timestamp information
- **Recovery**: Restore from checkpoint on failure

### Failure Simulation
- **Probability**: 10% chance of simulated failure
- **Types**: Random worker failures for testing
- **Network Delay**: 10-100ms random delays
- **Health Monitoring**: Worker health status tracking

### Retry Logic
- **Strategy**: Failed tasks retried on different workers
- **Error Handling**: Graceful degradation when workers unavailable
- **Status Tracking**: Worker health monitoring

## Data Management

### Sample Datasets
- **Users Table** (`data/users.csv`): 20 records with user information
  - Columns: user_id, name, age, email, city, salary
- **Orders Table** (`data/orders.csv`): 20 records with order information
  - Columns: order_id, user_id, product_name, quantity, price, order_date, status

### Data Partitioning
- **Strategy**: Round-robin partitioning simulation
- **Implementation**: Each worker gets subset of table data
- **Partition Size**: Total rows divided by number of workers (3)
- **Worker Assignment**: Based on worker ID (worker1=0, worker2=1, worker3=2)

## Instrumentation and Monitoring

### Query Tracing (`Tracer.java`)
- **Lifecycle Tracking**: Complete query execution timeline
- **Events**: START → PARSE → PLAN → EXECUTE → COMPLETE
- **Timing**: Performance metrics for each phase
- **Output**: JSON-formatted trace data

### Logging (`Logger.java`)
- **Framework**: SLF4J with Logback
- **Structured Logging**: Query lifecycle events
- **Log Files**: Rolling file appender with daily rotation
- **Log Levels**: INFO, DEBUG, WARN, ERROR

### Performance Metrics
- **Execution Time**: Per-worker task duration
- **Planning Time**: Query plan creation time
- **Network Timing**: Communication latency
- **Data Statistics**: Rows processed per worker

## Build and Deployment

### Maven Configuration (`pom.xml`)
- **Java Version**: 17
- **Dependencies**:
  - gRPC (1.58.0)
  - Protocol Buffers (3.25.1)
  - Jackson (2.15.2)
  - SLF4J/Logback (2.0.7/1.4.11)
- **Plugins**:
  - Protobuf Maven Plugin
  - Maven Compiler Plugin
  - Exec Maven Plugin

### Startup Scripts
- **`start_system.sh`**: Automated system startup
  - Starts 3 worker nodes (ports 50052-50054)
  - Starts coordinator (port 50051)
  - Creates PID files for process management
- **`stop_system.sh`**: Graceful system shutdown
  - Kills all processes using PID files
  - Force kill if graceful shutdown fails

### Configuration
- **Logging**: `logback.xml` configuration
- **Ports**: Hardcoded port assignments
- **Data Directory**: Configurable via command line args

## Supported SQL Features

### SELECT Queries
- **Column Projection**: `SELECT col1, col2 FROM table`
- **Wildcard Selection**: `SELECT * FROM table`
- **Table Aliases**: `SELECT u.name FROM users u`

### WHERE Conditions
- **Operators**: =, !=, >, <, >=, <=
- **Multiple Conditions**: AND/OR support
- **Data Types**: String, Integer, Double comparison

### JOIN Operations
- **Type**: INNER JOIN only
- **Syntax**: `FROM table1 JOIN table2 ON table1.col = table2.col`
- **Implementation**: Simple nested loop join

### Limitations
- **Multi-table Joins**: Only 2 tables supported
- **Aggregations**: GROUP BY not implemented
- **Sorting**: ORDER BY not implemented
- **Subqueries**: Not supported
- **Complex Expressions**: Limited expression support

## Example Queries

### Basic Queries
```sql
-- Select all users
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

## Technical Implementation Details

### gRPC Communication
- **Service Definition**: Protocol buffer schemas
- **Client-Server Pattern**: Blocking stubs for synchronous communication
- **Error Handling**: gRPC status codes and exceptions
- **Connection Management**: Managed channels with lifecycle management

### Concurrency
- **Thread Pool**: Cached thread pool for task execution
- **Parallel Execution**: CompletableFuture for concurrent task processing
- **Synchronization**: ConcurrentHashMap for thread-safe data structures

### Memory Management
- **Data Caching**: In-memory table cache per worker
- **Checkpoint Storage**: In-memory checkpoint storage
- **Garbage Collection**: Automatic cleanup of old checkpoints

## Performance Characteristics

### Scalability
- **Horizontal Scaling**: Add more worker nodes
- **Data Partitioning**: Automatic data distribution
- **Load Balancing**: Round-robin worker assignment

### Performance Metrics
- **Query Latency**: End-to-end query execution time
- **Throughput**: Queries per second capacity
- **Resource Usage**: Memory and CPU utilization per worker

### Bottlenecks
- **Single Coordinator**: Centralized planning and coordination
- **Memory Constraints**: In-memory data storage
- **Network Latency**: gRPC communication overhead

## Error Handling

### Exception Hierarchy
- **`SQLParseException`**: SQL parsing errors
- **`PlanningException`**: Query planning errors
- **`ExecutionException`**: Task execution errors

### Error Recovery
- **Graceful Degradation**: Continue with available workers
- **Retry Logic**: Automatic retry on failure
- **Error Propagation**: Detailed error messages to client

## Testing and Validation

### Manual Testing
- **Query Execution**: Test various SQL queries
- **Failure Simulation**: Test fault tolerance mechanisms
- **Performance Testing**: Measure execution times

### Test Scenarios
- **Single Table Queries**: Basic SELECT operations
- **Multi-table Joeries**: JOIN operations
- **Filtering**: WHERE condition testing
- **Failure Injection**: Worker failure simulation

## Future Enhancements

### Query Optimization
- **Cost-Based Optimizer**: Statistics-driven planning
- **Join Ordering**: Optimal join sequence selection
- **Predicate Pushdown**: Move filters closer to data
- **Index Usage**: Leverage indexes for performance

### Advanced Features
- **GROUP BY**: Aggregation operations
- **ORDER BY**: Sorting and ranking
- **LIMIT**: Result set size limiting
- **Subqueries**: Nested query support
- **Advanced JOINs**: Hash joins and sort-merge joins

### Scalability Improvements
- **Dynamic Worker Registration**: Add/remove workers at runtime
- **Load Balancing**: Distribute work based on worker capacity
- **Data Shuffling**: Implement shuffle operations for joins
- **Caching**: Query result caching and materialized views

### Production Readiness
- **Persistence**: Disk-based checkpointing
- **Security**: Authentication and authorization
- **Configuration**: External configuration management
- **Monitoring**: Metrics collection and alerting
- **Containerization**: Docker deployment support

## Educational Value

### Distributed Systems Concepts
- **Query Planning**: Converting SQL to distributed execution plans
- **Task Distribution**: Breaking work across multiple nodes
- **Result Aggregation**: Combining partial results
- **Fault Tolerance**: Handling failures gracefully

### gRPC and RPC Patterns
- **Service Definition**: Protocol buffer schemas
- **Client-Server Communication**: Blocking and async patterns
- **Error Handling**: gRPC status codes and exceptions
- **Connection Management**: Channel lifecycle

### Data Processing
- **Data Partitioning**: Distributing data across workers
- **Parallel Execution**: Concurrent task processing
- **Join Algorithms**: Simple nested loop joins
- **Filtering**: Condition evaluation

### Monitoring and Observability
- **Tracing**: Request lifecycle tracking
- **Metrics**: Performance measurement
- **Logging**: Structured event logging
- **Health Checks**: Service availability monitoring

## Project Structure Summary

```
distributed-sql-engine/
├── src/main/java/com/distributed/sql/
│   ├── client/
│   │   └── SQLClient.java                 # CLI client
│   ├── coordinator/
│   │   ├── CoordinatorMain.java          # Coordinator entry point
│   │   ├── CoordinatorServer.java       # gRPC server
│   │   ├── QueryPlanner.java            # Query planning
│   │   ├── QueryExecutor.java           # Task execution
│   │   ├── SQLParser.java               # SQL parsing
│   │   └── WorkerClient.java            # Worker communication
│   ├── worker/
│   │   ├── WorkerMain.java              # Worker entry point
│   │   ├── WorkerServer.java           # gRPC server
│   │   ├── ExecutionEngine.java         # Query execution
│   │   └── FaultToleranceManager.java  # Fault tolerance
│   ├── common/
│   │   ├── models/                     # Data models
│   │   │   ├── Query.java
│   │   │   ├── PlanNode.java
│   │   │   ├── ResultSet.java
│   │   │   ├── Row.java
│   │   │   ├── Condition.java
│   │   │   └── Join.java
│   │   └── proto/                      # Generated protobuf classes
│   └── utils/
│       ├── Logger.java                 # Logging utility
│       └── Tracer.java                 # Tracing utility
├── src/main/proto/
│   └── query.proto                     # Protocol buffer definitions
├── src/main/resources/
│   └── logback.xml                     # Logging configuration
├── data/
│   ├── users.csv                       # Sample user data
│   └── orders.csv                      # Sample order data
├── pom.xml                             # Maven configuration
├── start_system.sh                     # Startup script
├── stop_system.sh                      # Shutdown script
├── README.md                           # Project documentation
├── USAGE.md                           # Usage guide
└── project_notes.txt                  # Design notes
```

## Key Learning Outcomes

This project demonstrates:

1. **Distributed Query Processing**: How modern distributed databases process SQL queries across multiple nodes
2. **gRPC Communication**: High-performance RPC patterns for microservices architecture
3. **Fault Tolerance**: Checkpointing, retries, and failure handling in distributed systems
4. **Query Planning**: Converting SQL to executable distributed plans
5. **Data Partitioning**: Strategies for distributing data across workers
6. **Instrumentation**: Monitoring and tracing in distributed systems
7. **Concurrency**: Parallel task execution and result aggregation

## Conclusion

This Distributed SQL Query Engine serves as a comprehensive educational demonstration of distributed database concepts. While simplified compared to production systems, it effectively illustrates the core challenges and solutions in building scalable, fault-tolerant data processing systems. The project provides hands-on experience with modern distributed systems technologies and patterns, making it valuable for understanding how systems like Presto, SparkSQL, and Snowflake operate internally.

The codebase is well-structured, documented, and includes comprehensive logging and tracing capabilities, making it an excellent learning resource for distributed systems concepts and implementation patterns.
