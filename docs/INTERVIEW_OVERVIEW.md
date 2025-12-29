### Distributed SQL Query Engine — Interview Overview

This document is a concise, interview-ready walkthrough of the project: what it is, why it exists, how it works, what you learned, and how to demo it live.

## Elevator Pitch

I built a distributed SQL query engine that executes real SQL across sharded PostgreSQL databases. A gRPC coordinator parses a SQL query, plans it, routes sub-queries to worker nodes, aggregates results, and returns a unified result set. It supports real SQL execution, worker fault tolerance with retries, connection pooling, and basic plan operators (scan/filter/project). It’s a learning lab inspired by systems like Presto/Trino.

## Why This Project

- Demonstrate end-to-end understanding of distributed systems and databases by building rather than just reading.
- Practice core concepts: data sharding, query planning, parallel execution, fault tolerance, pooling/observability.
- Produce a portfolio-quality system that can be demoed with real data and real trade-offs.

## High-Level Architecture

- **Coordinator (gRPC server)**
  - Parses SQL, builds a logical plan, selects workers, executes sub-queries in parallel, aggregates results, exposes status.
- **Workers (gRPC servers)**
  - Maintain a `DataStore` with HikariCP for PostgreSQL. Execute plan fragments (scan/filter/project), return rows and metadata.
- **Common module**
  - Protobuf definitions and shared models (e.g., `Row`, `ResultSet`), logging, tracing, and utils.
- **Communication**
  - gRPC with protobuf messages for requests/responses.
- **Storage**
  - Local Postgres per worker; sharded logical datasets across workers.

## End-to-End Query Flow

1. Client sends SQL to Coordinator via gRPC (`ExecuteQuery`).
2. Coordinator parses SQL → builds a plan → chooses target workers for each fragment.
3. Coordinator dispatches sub-queries in parallel to workers (with timeouts and retries).
4. Workers execute SQL against local Postgres via `DataStore` (real JDBC execution with pooling).
5. Workers return `QueryResult` rows + status; Coordinator aggregates results and metadata.
6. Coordinator responds to client with a unified `QueryResult` and execution stats.

## Key Implemented Features

- **Real SQL Execution (critical)**
  - Workers run actual SQL against Postgres using JDBC + HikariCP connection pooling.
  - Result conversion to internal `Row` model with metadata and null handling.

- **Fault Tolerance (Priority 1)**
  - Coordinator executes with retries, filters unhealthy workers, enforces 30s per-worker timeout, and supports partial aggregation when some workers fail.

- **Basic Planning and Operators**
  - SCAN, FILTER (as WHERE), PROJECT (column selection). Query text is constructed per node and executed at the worker.

- **Observability & Metrics (initial)**
  - Execution time tracking, structured logging, simple tracing timestamps, connection pool stats surfaced from workers.

## Important Classes (talking points)

- Coordinator: query orchestration, retries, aggregation
  - `coordinator/CoordinatorServiceImpl.java` — gRPC methods, retry logic, aggregation
- Worker: execution on shards
  - `worker/WorkerServiceImpl.java` — gRPC handlers, conversion to `QueryResult`
  - `worker/QueryExecutor.java` — executes SQL from plan fragments, builds internal `ResultSet`
  - `worker/DataStore.java` — JDBC + HikariCP, result conversion, pool metrics
- Common models
  - `common/models/Row.java` — result rows with flexible metadata

## What’s In vs. Out (Scope)

- In
  - Real SQL reads, simple operators (scan/filter/project)
  - Parallel execution on multiple workers
  - Basic fault tolerance (health check, retries, timeouts, partial results)
  - Connection pooling via HikariCP, pool metrics exposure (initial)

- Not Yet (Future Work / Roadmap)
  - Distributed JOIN strategies (broadcast/shuffle)
  - Cost-based optimization and smarter planning
  - Distributed aggregations and GROUP BY with reshuffle
  - Robust health monitoring/metrics surfacing and dashboards
  - Distributed transactions (2PC/Sagas)

## Design Decisions & Trade-offs

- **Start with real SQL over mocks**: Authenticity matters for interviews—actual JDBC execution shows a complete path from query to data.
- **Pushdown simple predicates**: Implemented WHERE pushdown so workers filter early to reduce data movement.
- **Retry + timeout defaults**: 3 retries, 30s per worker to balance resilience and responsiveness.
- **Row model is stringly-typed initially**: Simpler integration across modules; type fidelity can be added later.
- **Connection pooling**: HikariCP for performance; basic metrics exposed for operational visibility.

## Demo Script (5 minutes)

1. Start Postgres and load sample data (`scripts/` helper SQL files).
2. Start Coordinator and Workers (`./scripts/start_system.sh` or individual modules).
3. Run client and issue queries:
   - `SELECT * FROM users LIMIT 5;`
   - `SELECT name, age FROM users WHERE age > 30;`
4. Kill a worker process, re-run a query, point out retries and partial results in logs.
5. Show pool metrics in worker logs and simple tracing timestamps.

## Interview Talking Points (ready-to-say)

- Data Distribution
  - Started with simple sharding; next step is consistent hashing to minimize rebalancing.
- Predicate Pushdown
  - WHERE clauses are executed at the worker, reducing network transfer and coordinator aggregation work.
- Fault Tolerance Strategy
  - Health checks + retries + 30s timeout; Coordinator removes unhealthy nodes and aggregates partial results.
- Performance & Pooling
  - HikariCP pooling, minimal object allocation in hot paths, and log-based latency tracking.
- Limitations & Next Steps
  - JOINs and distributed aggregations are the next milestone; planning heuristics and metadata-aware execution are on the roadmap.

## What You Learned

- Crafting a minimal yet real execution path: client → coordinator → worker → Postgres → worker → coordinator → client.
- Managing concurrency, timeouts, and retries with CompletableFutures.
- The value of pushdown and early filtering in distributed settings.
- Operational concerns (pooling, basic health checks, metrics) matter even in a learning project.

## Setup & Run (quick reference)

1. Install/start PostgreSQL (e.g., `brew services start postgresql@15`).
2. Initialize databases and data: run SQL in `scripts/` (`init_databases.sql`, `create_schema.sql`, `load_sample_data.sql`).
3. Build: `mvn clean install`.
4. Start services: `./scripts/start_system.sh` (or run modules individually).
5. Run client: `mvn exec:java -pl client` and issue SQL statements.

## Metrics to Mention (once you run it)

- Cold vs warm query latencies on single-table scans.
- Impact of WHERE pushdown on bytes returned and latency.
- Connection pool utilization under modest concurrency.

## Roadmap (if asked “what next?”)

- Implement broadcast JOIN (small-right/left table heuristic), then shuffle JOIN for larger datasets.
- Introduce consistent hashing with virtual nodes for better rebalancing.
- Add cost-based planning using simple stats; predicate/column selectivity estimates.
- Surface metrics via a simple dashboard (worker/coordinator status, pool stats, latencies).
- Explore streaming results and backpressure for large result sets.

## One-Sentence Summary

I built a mini-Presto: a coordinator that parallelizes SQL across Postgres-backed workers with real execution, retries, and pooling; it’s intentionally scoped but production-flavored, and I can demo it live with real data while discussing the trade-offs and next steps.




