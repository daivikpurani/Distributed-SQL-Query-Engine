# Distributed SQL Query Engine

A distributed SQL query engine I've been building as a hands-on project to better understand how modern databases plan and execute queries across multiple nodes.

This project is intentionally not a full production database. The goal is to learn by building: take real design patterns from distributed query engines, implement them end to end, and understand where complexity actually comes from.

## Why I built this

Ahead of my internship on time-series databases, I wanted a deeper, practical understanding of:

- How SQL queries are parsed, planned, and broken into distributed execution units
- How coordinators and workers interact under partial failure
- How sharding choices affect correctness and performance
- Where bottlenecks emerge in real distributed execution paths

Rather than reading papers in isolation, I chose to build a working system and iterate on it.

## System Overview

The engine follows a coordinator–worker architecture.

- A coordinator node handles SQL parsing, planning, shard selection, and result aggregation
- Worker nodes execute query fragments against local PostgreSQL shards
- All inter-node communication uses gRPC

Each worker owns its own database shard, keeping execution simple and explicit.

```
Client
  |
  v
Coordinator
  |── gRPC ──> Worker 1 (Postgres shard)
  |── gRPC ──> Worker 2 (Postgres shard)
  |── gRPC ──> Worker 3 (Postgres shard)
```

## Query Execution Flow

- Client submits a SQL query to the coordinator
- The coordinator parses the query and builds a logical plan
- The planner identifies relevant shards and generates per-worker tasks
- Tasks are dispatched to workers over gRPC
- Workers execute queries locally and stream partial results back
- The coordinator aggregates results and returns the final response

This flow mirrors how many real distributed query engines separate control and execution planes.

## SQL Support

Supported query features are intentionally limited but practical:

- SELECT
- WHERE filters
- INNER JOINs
- GROUP BY and aggregations
- Basic ORDER BY

The focus is on execution mechanics rather than full SQL compliance.

## Sharding and Data Distribution

Each worker runs its own PostgreSQL instance.

Supported strategies:

- Hash-based sharding for uniform distribution
- Range-based sharding for range queries
- Simple round-robin for experimentation

Shard awareness lives in the coordinator, while workers remain stateless with respect to global metadata.

## Fault Handling

The system handles common failure scenarios encountered in distributed execution:

- Worker health tracking via heartbeats
- Retry of query fragments on transient worker failures
- Graceful handling when some shards are unavailable

This is not a fully fault-tolerant database, but it exposes real coordination and recovery challenges.

## Observability and Debugging

To better understand system behavior, I added lightweight observability:

- Live worker health and utilization metrics
- Query execution progress tracking
- Optional visualization UI for inspecting query flow

These tools were primarily used for learning and debugging, not as production monitoring.

## Tech Stack

Backend:

- Java 17
- Spring Boot
- gRPC + Protocol Buffers
- PostgreSQL (per-shard storage)
- HikariCP

Frontend (optional visualizer):

- React
- TypeScript
- WebSockets

## Running Locally

The system is designed to run locally with multiple worker processes.

High-level steps:

1. Start PostgreSQL and initialize shard databases
2. Build Java modules
3. Start coordinator and worker nodes
4. (Optional) Start the visualization UI

Scripts are provided under `scripts/` for convenience.

## Key Takeaways

Building this system helped me internalize:

- Why query planning and execution are tightly coupled in distributed systems
- How partial failures complicate otherwise simple execution flows
- Where coordination overhead dominates performance
- How much complexity is hidden behind "simple" SQL queries

## What's Intentionally Out of Scope

- Distributed transactions
- Strong cross-shard consistency guarantees
- Cost-based query optimization
- Multi-coordinator consensus

These were consciously excluded to keep the project focused and learnable.

## Future Work

If extended further:

- Partial aggregation pushdown
- Smarter shard pruning
- Backpressure-aware streaming
- More realistic benchmarking harness


