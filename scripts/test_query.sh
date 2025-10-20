#!/bin/bash

echo "Testing Distributed SQL Query Engine..."

# Test query execution
echo "SELECT * FROM users WHERE age > 25" | cd client && mvn exec:java -Dexec.mainClass="com.distributed.sql.client.SQLClient" -Dexec.args="localhost 50051"

echo "Test completed."
