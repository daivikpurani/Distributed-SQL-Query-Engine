# 🎯 Visualizer Demo Guide

## Quick Demo Setup

### 1. Start the System
```bash
# In the main project directory
./scripts/start_system.sh
```

### 2. Start the Visualizer
```bash
# In the visualizer directory
./start_visualizer.sh
```

### 3. Open Dashboard
Navigate to `http://localhost:8080` in your browser.

## Demo Scenarios

### Scenario 1: System Architecture (2 minutes)
1. **Show Architecture View**
   - Explain coordinator-worker pattern
   - Point out real-time health indicators
   - Demonstrate component status updates

2. **Interactive Exploration**
   - Click on different components
   - Show component details panel
   - Explain gRPC connections

### Scenario 2: Query Execution (3 minutes)
1. **Switch to Query Flow View**
   - Show the query input field
   - Execute: `SELECT name, age FROM users WHERE age > 25`
   - Watch step-by-step execution

2. **Explain Each Phase**
   - SQL Parser: Converts SQL to internal representation
   - Query Planner: Creates execution plan
   - Shard Assignment: Determines which workers handle data
   - Worker Execution: Parallel processing across workers
   - Result Aggregation: Combines results from all workers

3. **Show Results**
   - Display query results in table format
   - Show execution time and metrics

### Scenario 3: Performance Monitoring (2 minutes)
1. **Switch to Performance View**
   - Show real-time metrics dashboard
   - Explain each metric (queries/sec, latency, error rate)

2. **Worker Utilization**
   - Show individual worker performance
   - Explain load balancing
   - Demonstrate system health monitoring

### Scenario 4: Interactive Demo (3 minutes)
1. **Switch to Demo View**
   - Show sample queries
   - Execute different query types
   - Demonstrate JOIN operations

2. **Educational Content**
   - Explain distributed systems concepts
   - Show how data is partitioned
   - Demonstrate fault tolerance

## Key Talking Points

### Technical Highlights
- **Real-time Updates**: WebSocket connection for live data
- **Interactive Visualizations**: Canvas-based graphics
- **System Integration**: Direct gRPC connection to services
- **Performance**: Rust backend for high performance

### Educational Value
- **Distributed Systems**: Microservices architecture
- **Database Internals**: Query processing pipeline
- **Performance Monitoring**: Real-time metrics
- **Fault Tolerance**: System resilience

### Portfolio Impact
- **Full-Stack Development**: Rust + JavaScript
- **Real-time Systems**: WebSocket integration
- **Data Visualization**: Interactive charts and diagrams
- **Professional Quality**: Production-ready code

## Troubleshooting

### Common Issues
1. **"Connecting..." Status**
   - Check if system services are running
   - Verify ports 50051-50054 are available
   - Restart system services if needed

2. **No Data Updates**
   - Check WebSocket connection
   - Verify gRPC services are responding
   - Check browser console for errors

3. **Canvas Not Rendering**
   - Ensure JavaScript is enabled
   - Try different browser
   - Check browser compatibility

### Debug Mode
```bash
# Enable debug logging
RUST_LOG=debug cargo run --bin visualizer

# Check browser console
# Open Developer Tools → Console tab
```

## Presentation Tips

### For Technical Interviews
- **Start with Architecture**: Show system design understanding
- **Demonstrate Real-time**: Execute queries and show live updates
- **Explain Performance**: Show metrics and optimization
- **Highlight Rust**: Emphasize performance and safety benefits

### For Portfolio Presentations
- **Show Live Demo**: Execute queries in real-time
- **Explain Concepts**: Use visualizations to teach
- **Demonstrate Skills**: Full-stack development capabilities
- **Professional Quality**: Clean UI and smooth animations

### For Educational Purposes
- **Step-by-Step**: Walk through query execution phases
- **Interactive Learning**: Let audience try different queries
- **Visual Learning**: Use diagrams to explain concepts
- **Real-world Application**: Connect to actual distributed system

## Sample Queries for Demo

### Basic Queries
```sql
-- Simple selection
SELECT name FROM users;

-- Filtered query
SELECT name, age FROM users WHERE age > 30;

-- Multiple conditions
SELECT name, salary FROM users WHERE age > 25 AND salary > 70000;
```

### Complex Queries
```sql
-- JOIN operation
SELECT u.name, o.product_name 
FROM users u 
JOIN orders o ON u.user_id = o.user_id;

-- Aggregation
SELECT COUNT(*) FROM users;

-- Complex filtering
SELECT name FROM users WHERE city IN ('New York', 'Los Angeles');
```

## Success Metrics

### Technical Success
- ✅ Visualizer connects to running system
- ✅ Real-time updates work smoothly
- ✅ All visualizations render correctly
- ✅ Query execution produces results

### Presentation Success
- ✅ Audience understands distributed systems concepts
- ✅ Live demo impresses viewers
- ✅ Technical depth is clearly demonstrated
- ✅ Professional quality is evident

### Portfolio Success
- ✅ Stands out from other projects
- ✅ Demonstrates advanced technical skills
- ✅ Shows full-stack development capabilities
- ✅ Provides educational value

This visualizer is designed to be a standout piece in your portfolio, demonstrating advanced technical skills, system design understanding, and the ability to create impressive visualizations of complex distributed systems.
