#!/bin/bash

# Distributed SQL Engine Startup Script

echo "Starting Distributed SQL Query Engine..."
echo "======================================="

# Create logs directory
mkdir -p logs

# Function to start a service in background
start_service() {
    local service_name=$1
    local main_class=$2
    local args=$3
    
    echo "Starting $service_name..."
    mvn exec:java -Dexec.mainClass="$main_class" -Dexec.args="$args" > logs/${service_name}.log 2>&1 &
    echo $! > logs/${service_name}.pid
    echo "$service_name started with PID $(cat logs/${service_name}.pid)"
}

# Start workers
start_service "worker1" "com.distributed.sql.worker.WorkerMain" "worker1 50052"
sleep 2

start_service "worker2" "com.distributed.sql.worker.WorkerMain" "worker2 50053"
sleep 2

start_service "worker3" "com.distributed.sql.worker.WorkerMain" "worker3 50054"
sleep 2

# Start coordinator
start_service "coordinator" "com.distributed.sql.coordinator.CoordinatorMain" "50051"
sleep 3

echo ""
echo "System started successfully!"
echo "============================"
echo "Workers:"
echo "  Worker 1: PID $(cat logs/worker1.pid)"
echo "  Worker 2: PID $(cat logs/worker2.pid)"
echo "  Worker 3: PID $(cat logs/worker3.pid)"
echo ""
echo "Coordinator: PID $(cat logs/coordinator.pid)"
echo ""
echo "To start the client:"
echo "  mvn exec:java -Dexec.mainClass=\"com.distributed.sql.client.SQLClient\""
echo ""
echo "To stop the system:"
echo "  ./stop_system.sh"
echo ""
echo "Logs are available in the logs/ directory"
