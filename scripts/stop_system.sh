#!/bin/bash

# Distributed SQL Engine - System Shutdown Script
# This script stops all components of the distributed SQL engine

set -e

echo "🛑 Stopping Distributed SQL Engine"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="logs/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            echo -e "${BLUE}Stopping $service_name (PID: $pid)...${NC}"
            kill $pid
            
            # Wait for graceful shutdown
            local count=0
            while kill -0 $pid 2>/dev/null && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done
            
            # Force kill if still running
            if kill -0 $pid 2>/dev/null; then
                echo -e "${YELLOW}Force killing $service_name...${NC}"
                kill -9 $pid
            fi
            
            echo -e "${GREEN}✅ $service_name stopped${NC}"
        else
            echo -e "${YELLOW}⚠️  $service_name was not running${NC}"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠️  No PID file found for $service_name${NC}"
    fi
}

# Main execution
main() {
    echo -e "${YELLOW}Stopping system components...${NC}"
    
    # Stop workers first
    stop_service "worker3"
    stop_service "worker2"
    stop_service "worker1"
    
    # Stop coordinator last
    stop_service "coordinator"
    
    echo -e "${GREEN}🎉 All services stopped successfully!${NC}"
}

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [--help]"
        echo "Stop all components of the distributed SQL engine"
        exit 0
        ;;
    *)
        main
        ;;
esac