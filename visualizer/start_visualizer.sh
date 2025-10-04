#!/bin/bash

# Distributed SQL Query Engine Visualizer Startup Script

echo "🦀 Starting Distributed SQL Query Engine Visualizer"

# Check if Rust is installed
if ! command -v cargo &> /dev/null; then
    echo "❌ Rust/Cargo not found. Please install Rust first:"
    echo "   curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh"
    exit 1
fi

# Check if the main system is running
echo "🔍 Checking if Distributed SQL Query Engine is running..."

# Check coordinator port
if ! lsof -Pi :50051 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Coordinator not running on port 50051"
    echo "   Please start the system first: ./scripts/start_system.sh"
    echo "   Continuing anyway for demo purposes..."
fi

# Check worker ports
for port in 50052 50053 50054; do
    if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "⚠️  Worker not running on port $port"
    fi
done

# Build the visualizer
echo "🔨 Building visualizer..."
if cargo build --release; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi

# Start the visualizer
echo "🚀 Starting visualizer server on http://localhost:8080"
echo "   Press Ctrl+C to stop"
echo ""

cargo run --bin visualizer
