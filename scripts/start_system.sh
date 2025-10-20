#!/bin/bash

# Distributed SQL Engine - System Startup Script (Java)
# This script starts all components of the distributed SQL engine

set -e

echo "🚀 Starting Distributed SQL Engine (Java)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COORDINATOR_PORT=50051
WORKER1_PORT=50052
WORKER2_PORT=50053
WORKER3_PORT=50054

# Create logs directory
mkdir -p logs

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}Port $port is already in use${NC}"
        return 1
    fi
    return 0
}

# Function to start coordinator
start_coordinator() {
    echo -e "${BLUE}Starting Coordinator on port $COORDINATOR_PORT...${NC}"
    
    if ! check_port $COORDINATOR_PORT; then
        echo -e "${RED}Coordinator port $COORDINATOR_PORT is busy${NC}"
        exit 1
    fi
    
    # Start coordinator in background
    mvn exec:java -pl coordinator -Dexec.mainClass="com.distributed.sql.coordinator.CoordinatorMain" -Dexec.args="$COORDINATOR_PORT" > logs/coordinator.log 2>&1 &
    COORDINATOR_PID=$!
    echo $COORDINATOR_PID > logs/coordinator.pid
    
    # Wait for coordinator to start
    sleep 5
    
    if kill -0 $COORDINATOR_PID 2>/dev/null; then
        echo -e "${GREEN}✅ Coordinator started (PID: $COORDINATOR_PID)${NC}"
    else
        echo -e "${RED}❌ Coordinator failed to start${NC}"
        cat logs/coordinator.log
        exit 1
    fi
}

# Function to start worker
start_worker() {
    local worker_id=$1
    local port=$2
    local db_name=$3
    
    echo -e "${BLUE}Starting Worker $worker_id on port $port...${NC}"
    
    if ! check_port $port; then
        echo -e "${RED}Worker port $port is busy${NC}"
        return 1
    fi
    
    # Start worker in background
    mvn exec:java -pl worker -Dexec.mainClass="com.distributed.sql.worker.WorkerMain" \
        -Dexec.args="--worker-id $worker_id --port $port --db-url jdbc:postgresql://localhost:5432/$db_name --db-user postgres --db-password postgres" \
        > logs/$worker_id.log 2>&1 &
    WORKER_PID=$!
    echo $WORKER_PID > logs/$worker_id.pid
    
    # Wait for worker to start
    sleep 3
    
    if kill -0 $WORKER_PID 2>/dev/null; then
        echo -e "${GREEN}✅ Worker $worker_id started (PID: $WORKER_PID)${NC}"
    else
        echo -e "${RED}❌ Worker $worker_id failed to start${NC}"
        cat logs/$worker_id.log
        return 1
    fi
}

# Function to check system health
check_health() {
    echo -e "${YELLOW}Checking system health...${NC}"
    
    # Check coordinator
    if [ -f logs/coordinator.pid ]; then
        COORDINATOR_PID=$(cat logs/coordinator.pid)
        if kill -0 $COORDINATOR_PID 2>/dev/null; then
            echo -e "${GREEN}✅ Coordinator is running${NC}"
        else
            echo -e "${RED}❌ Coordinator is not running${NC}"
            return 1
        fi
    fi
    
    # Check workers
    for worker in worker1 worker2 worker3; do
        if [ -f logs/$worker.pid ]; then
            WORKER_PID=$(cat logs/$worker.pid)
            if kill -0 $WORKER_PID 2>/dev/null; then
                echo -e "${GREEN}✅ $worker is running${NC}"
            else
                echo -e "${RED}❌ $worker is not running${NC}"
                return 1
            fi
        fi
    done
    
    return 0
}

# Main execution
main() {
    echo -e "${YELLOW}Building project...${NC}"
    mvn clean compile -q
    
    echo -e "${YELLOW}Starting system components...${NC}"
    
    # Start coordinator first
    start_coordinator
    
    # Start workers
    start_worker "worker1" $WORKER1_PORT "worker1_db"
    start_worker "worker2" $WORKER2_PORT "worker2_db"
    start_worker "worker3" $WORKER3_PORT "worker3_db"
    
    # Wait for all components to initialize
    echo -e "${YELLOW}Waiting for components to initialize...${NC}"
    sleep 8
    
    # Check system health
    if check_health; then
        echo -e "${GREEN}🎉 System started successfully!${NC}"
        echo -e "${BLUE}Coordinator: localhost:$COORDINATOR_PORT${NC}"
        echo -e "${BLUE}Workers: localhost:$WORKER1_PORT, localhost:$WORKER2_PORT, localhost:$WORKER3_PORT${NC}"
        echo -e "${YELLOW}Run 'mvn exec:java -pl client' to start the SQL client${NC}"
        echo -e "${YELLOW}Run './scripts/stop_system.sh' to stop the system${NC}"
    else
        echo -e "${RED}❌ System health check failed${NC}"
        exit 1
    fi
}

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [--help]"
        echo "Start all components of the distributed SQL engine"
        exit 0
        ;;
    *)
        main
        ;;
esac