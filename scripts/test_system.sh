#!/bin/bash

# Distributed SQL Engine - System Testing Script
# This script runs comprehensive tests on the distributed SQL engine

set -e

echo "🧪 Testing Distributed SQL Engine (Rust)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COORDINATOR_HOST="localhost"
COORDINATOR_PORT=50051
TEST_TIMEOUT=30

# Function to run a test query
run_test_query() {
    local query="$1"
    local description="$2"
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo -e "${YELLOW}Query: $query${NC}"
    
    # Run the query with timeout
    if timeout $TEST_TIMEOUT cargo run --bin client -- --query "$query" --host $COORDINATOR_HOST --port $COORDINATOR_PORT; then
        echo -e "${GREEN}✅ Test passed: $description${NC}"
        return 0
    else
        echo -e "${RED}❌ Test failed: $description${NC}"
        return 1
    fi
}

# Function to check system health
check_system_health() {
    echo -e "${YELLOW}Checking system health...${NC}"
    
    # Check if coordinator is running
    if ! lsof -Pi :$COORDINATOR_PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}❌ Coordinator is not running on port $COORDINATOR_PORT${NC}"
        return 1
    fi
    
    # Check if workers are running
    for port in 50052 50053 50054; do
        if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo -e "${RED}❌ Worker on port $port is not running${NC}"
            return 1
        fi
    done
    
    echo -e "${GREEN}✅ System health check passed${NC}"
    return 0
}

# Function to run basic tests
run_basic_tests() {
    echo -e "${YELLOW}Running basic tests...${NC}"
    
    local failed_tests=0
    
    # Test 1: Simple SELECT
    if ! run_test_query "SELECT name FROM users" "Simple SELECT query"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Test 2: SELECT with WHERE
    if ! run_test_query "SELECT name, age FROM users WHERE age > 25" "SELECT with WHERE clause"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Test 3: SELECT with multiple conditions
    if ! run_test_query "SELECT * FROM users WHERE age > 25 AND city = 'New York'" "SELECT with multiple conditions"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Test 4: SELECT with LIKE
    if ! run_test_query "SELECT name FROM users WHERE name LIKE 'John%'" "SELECT with LIKE operator"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    return $failed_tests
}

# Function to run join tests
run_join_tests() {
    echo -e "${YELLOW}Running join tests...${NC}"
    
    local failed_tests=0
    
    # Test 1: INNER JOIN
    if ! run_test_query "SELECT u.name, o.order_id FROM users u JOIN orders o ON u.user_id = o.user_id" "INNER JOIN query"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Test 2: LEFT JOIN
    if ! run_test_query "SELECT u.name, o.order_id FROM users u LEFT JOIN orders o ON u.user_id = o.user_id" "LEFT JOIN query"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    return $failed_tests
}

# Function to run performance tests
run_performance_tests() {
    echo -e "${YELLOW}Running performance tests...${NC}"
    
    local failed_tests=0
    
    # Test 1: Large result set
    if ! run_test_query "SELECT * FROM users" "Large result set query"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Test 2: Complex query
    if ! run_test_query "SELECT u.name, o.order_id, p.product_name FROM users u JOIN orders o ON u.user_id = o.user_id JOIN products p ON o.product_id = p.product_id WHERE u.age > 30" "Complex multi-table join"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    return $failed_tests
}

# Function to run unit tests
run_unit_tests() {
    echo -e "${YELLOW}Running unit tests...${NC}"
    
    if cargo test; then
        echo -e "${GREEN}✅ Unit tests passed${NC}"
        return 0
    else
        echo -e "${RED}❌ Unit tests failed${NC}"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    echo -e "${YELLOW}Running integration tests...${NC}"
    
    if cargo test --test integration_tests; then
        echo -e "${GREEN}✅ Integration tests passed${NC}"
        return 0
    else
        echo -e "${RED}❌ Integration tests failed${NC}"
        return 1
    fi
}

# Function to generate test report
generate_test_report() {
    local total_tests=$1
    local failed_tests=$2
    
    echo -e "${YELLOW}Test Report${NC}"
    echo -e "${BLUE}===========${NC}"
    echo -e "Total tests: $total_tests"
    echo -e "Passed: $((total_tests - failed_tests))"
    echo -e "Failed: $failed_tests"
    
    if [ $failed_tests -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed!${NC}"
        return 0
    else
        echo -e "${RED}❌ $failed_tests tests failed${NC}"
        return 1
    fi
}

# Main execution
main() {
    local total_failed=0
    
    # Check system health first
    if ! check_system_health; then
        echo -e "${RED}❌ System health check failed. Please start the system first.${NC}"
        echo -e "${YELLOW}Run './scripts/start_system.sh' to start the system${NC}"
        exit 1
    fi
    
    # Run different test suites based on arguments
    case "${1:-all}" in
        basic)
            run_basic_tests
            total_failed=$?
            ;;
        joins)
            run_join_tests
            total_failed=$?
            ;;
        performance)
            run_performance_tests
            total_failed=$?
            ;;
        unit)
            run_unit_tests
            total_failed=$?
            ;;
        integration)
            run_integration_tests
            total_failed=$?
            ;;
        all)
            echo -e "${YELLOW}Running all test suites...${NC}"
            
            # Run unit tests
            if ! run_unit_tests; then
                total_failed=$((total_failed + 1))
            fi
            
            # Run integration tests
            if ! run_integration_tests; then
                total_failed=$((total_failed + 1))
            fi
            
            # Run basic tests
            local basic_failed
            run_basic_tests
            basic_failed=$?
            total_failed=$((total_failed + basic_failed))
            
            # Run join tests
            local join_failed
            run_join_tests
            join_failed=$?
            total_failed=$((total_failed + join_failed))
            
            # Run performance tests
            local perf_failed
            run_performance_tests
            perf_failed=$?
            total_failed=$((total_failed + perf_failed))
            ;;
        --help|-h)
            echo "Usage: $0 [basic|joins|performance|unit|integration|all]"
            echo "  basic        Run basic SQL tests"
            echo "  joins        Run join query tests"
            echo "  performance  Run performance tests"
            echo "  unit         Run unit tests"
            echo "  integration  Run integration tests"
            echo "  all          Run all tests (default)"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown test suite: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
    
    # Generate test report
    local total_tests=6  # Approximate number of tests
    generate_test_report $total_tests $total_failed
}

# Run main function
main "$@"
