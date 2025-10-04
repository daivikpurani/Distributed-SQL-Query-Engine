#!/bin/bash

# Distributed SQL Engine Stop Script

echo "Stopping Distributed SQL Query Engine..."
echo "======================================="

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="logs/${service_name}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 1
            if ps -p $pid > /dev/null 2>&1; then
                echo "Force killing $service_name..."
                kill -9 $pid
            fi
        else
            echo "$service_name is not running"
        fi
        rm -f "$pid_file"
    else
        echo "No PID file found for $service_name"
    fi
}

# Stop all services
stop_service "coordinator"
stop_service "worker1"
stop_service "worker2"
stop_service "worker3"

echo ""
echo "System stopped successfully!"
echo "==========================="
