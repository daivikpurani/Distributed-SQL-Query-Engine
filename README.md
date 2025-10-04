# Distributed SQL Query Engine

A high-performance distributed SQL query engine with sharding support, built with **Rust** and gRPC.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Testing](#testing)
- [Contributing](#contributing)

## Overview

This project implements a distributed SQL query engine that can execute SQL queries across multiple worker nodes. The system features:

- **🔥 High Performance**: Leverages Rust's zero-cost abstractions and memory safety
- **🌐 Distributed Query Processing**: Queries are parsed, planned, and executed across multiple workers
- **📊 Data Sharding**: Automatic data distribution and sharding across worker nodes
- **🛡️ Fault Tolerance**: Worker failure detection and recovery mechanisms
- **⚡ gRPC Communication**: High-performance inter-node communication
- **🔧 SQL Support**: Basic SQL operations including SELECT, JOIN, WHERE, and aggregation

## Architecture

The system consists of three main components:

### 1. Coordinator

- **Query Parser**: Parses SQL queries into internal representations using nom parser combinators
- **Query Planner**: Creates execution plans with shard-aware optimization
- **Shard Manager**: Manages data distribution and shard assignments
- **Query Executor**: Coordinates query execution across workers

### 2. Workers

- **Execution Engine**: Executes query plan nodes on local data
- **Shard Management**: Handles local shard data and ownership
- **Fault Tolerance**: Implements checkpointing and recovery

### 3. Client

- **SQL Client**: Command-line interface for query execution
- **Result Processing**: Handles query results and formatting

## Features

### Core Features

- ✅ SQL Query Parsing (SELECT, JOIN, WHERE, GROUP BY)
- ✅ Distributed Query Planning
- ✅ Multi-worker Query Execution
- ✅ Data Sharding and Distribution
- ✅ Fault Tolerance and Recovery
- ✅ Performance Monitoring and Tracing

### Sharding Features

- ✅ Hash-based Data Sharding
- ✅ Range-based Data Distribution
- ✅ Round-robin Distribution
- ✅ Automatic Shard Rebalancing
- ✅ Shard Migration and Recovery
- ✅ Worker Registration and Management

### Query Operations

- ✅ Table Scans with Column Projection
- ✅ Filter Operations (WHERE clauses)
- ✅ Join Operations (INNER, LEFT, RIGHT, FULL)
- ✅ Aggregation Functions (COUNT, SUM, AVG, etc.)
- ✅ Complex Multi-table Queries

## Quick Start

### Prerequisites

- Rust 1.70+ (latest stable recommended)
- Cargo (comes with Rust)
- Protocol Buffers compiler (`protoc`)

### Installation

```bash
# Clone the repository
git clone <repository-url>
cd Distributed-SQL-Query-Engine

# Build the project
cargo build --release
```

### Running the System

1. **Start the Coordinator**:

```bash
cargo run --bin coordinator
```

2. **Start Workers** (in separate terminals):

```bash
# Worker 1
cargo run --bin worker -- --worker-id worker1 --port 50052

# Worker 2
cargo run --bin worker -- --worker-id worker2 --port 50053

# Worker 3
cargo run --bin worker -- --worker-id worker3 --port 50054
```

3. **Run Queries**:

```bash
cargo run --bin client -- "SELECT name FROM users WHERE age > 30"
```

### Using Scripts

```bash
# Quick start with interactive menu
./scripts/start_system.sh

# Run tests
./scripts/test_system.sh

# Stop all services
./scripts/stop_system.sh
```

## Documentation

- [Rust Migration Plan](RUST_MIGRATION_PLAN.md) - Comprehensive migration strategy
- [Rust Migration Summary](RUST_MIGRATION_SUMMARY.md) - Migration results and benefits
- [Rust README](README_RUST.md) - Detailed Rust implementation guide
- [Architecture Details](docs/architecture.md)
- [Sharding Implementation](docs/sharding.md)
- [API Reference](docs/api.md)
- [Configuration Guide](docs/configuration.md)
- [Performance Tuning](docs/performance.md)
- [Troubleshooting](docs/troubleshooting.md)

## Testing

### Running Tests

```bash
# Run all tests
cargo test

# Run specific test suites
cargo test --package common
cargo test --package coordinator
cargo test --package worker
cargo test --package client

# Run integration tests
cargo test --test integration_tests
```

### Test Coverage

- Unit tests for all core components
- Integration tests for end-to-end scenarios
- Sharding functionality tests
- Performance benchmarks

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with Rust and the amazing Rust ecosystem
- Inspired by distributed database systems like Presto, SparkSQL, and Snowflake
- Designed for educational and demonstration purposes
- Special thanks to the Rust community for excellent tooling and libraries

---

**Built with ❤️ in Rust** | **Performance meets Safety** | **Distributed Systems Excellence**
