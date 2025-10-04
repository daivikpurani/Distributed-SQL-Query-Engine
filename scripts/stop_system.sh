#!/bin/bash

# Distributed SQL Engine - System Shutdown Script
# This script stops all components of the distributed SQL engine

set -e

echo "🛑 Stopping Distributed SQL Engine (Rust)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to stop a process
stop_process() {
    local name=$1
    local pid_file="logs/$name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            echo -e "${BLUE}Stopping $name (PID: $pid)...${NC}"
            kill $pid
            
            # Wait for graceful shutdown
            local count=0
            while kill -0 $pid 2>/dev/null && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done
            
            # Force kill if still running
            if kill -0 $pid 2>/dev/null; then
                echo -e "${YELLOW}Force killing $name...${NC}"
                kill -9 $pid
            fi
            
            echo -e "${GREEN}✅ $name stopped${NC}"
        else
            echo -e "${YELLOW}⚠️  $name was not running${NC}"
        fi
        rm -f "$pid_file"
    else
        echo -e "${YELLOW}⚠️  No PID file found for $name${NC}"
    fi
}

# Function to stop all processes
stop_all() {
    echo -e "${YELLOW}Stopping all system components...${NC}"
    
    # Stop workers first
    stop_process "worker3"
    stop_process "worker2"
    stop_process "worker1"
    
    # Stop coordinator last
    stop_process "coordinator"
    
    echo -e "${GREEN}🎉 All components stopped successfully!${NC}"
}

# Function to force kill all processes
force_kill_all() {
    echo -e "${RED}Force killing all processes...${NC}"
    
    # Kill all coordinator processes
    pkill -f "coordinator" || true
    
    # Kill all worker processes
    pkill -f "worker" || true
    
    # Clean up PID files
    rm -f logs/*.pid
    
    echo -e "${GREEN}All processes force killed${NC}"
}

# Function to clean up logs
cleanup_logs() {
    echo -e "${YELLOW}Cleaning up logs...${NC}"
    rm -f logs/*.log
    rm -f logs/*.pid
    echo -e "${GREEN}Logs cleaned up${NC}"
}

# Main execution
main() {
    case "${1:-}" in
        --force|-f)
            force_kill_all
            ;;
        --clean|-c)
            stop_all
            cleanup_logs
            ;;
        --help|-h)
            echo "Usage: $0 [--force|--clean|--help]"
            echo "  --force, -f    Force kill all processes"
            echo "  --clean, -c    Stop all processes and clean logs"
            echo "  --help, -h      Show this help message"
            exit 0
            ;;
        *)
            stop_all
            ;;
    esac
}

# Run main function
main "$@"
