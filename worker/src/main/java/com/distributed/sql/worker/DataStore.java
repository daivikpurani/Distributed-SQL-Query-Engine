package com.distributed.sql.worker;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

/**
 * DataStore implementation with PostgreSQL integration and connection pooling
 */
public class DataStore {

    private final HikariDataSource dataSource;
    private final String workerId;

    public DataStore(String workerId, String databaseUrl, String username, String password) {
        this.workerId = workerId;

        // Configure HikariCP connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        this.dataSource = new HikariDataSource(config);

        AppLogger.info("Initialized DataStore for worker {} with database: {}", workerId, databaseUrl);
    }

    public java.sql.ResultSet executeQuery(String sqlQuery) {
        String traceId = Tracer.startTrace("execute_query");

        try {
            AppLogger.info("Executing query on worker {}: {}", workerId, sqlQuery);

            // For demo purposes, return mock data based on query content
            return convertToResultSet(generateMockResults(sqlQuery));

        } finally {
            Tracer.endTrace("execute_query");
        }
    }

    private com.distributed.sql.common.models.ResultSet generateMockResults(String sqlQuery) {
        com.distributed.sql.common.models.ResultSet resultSet = new com.distributed.sql.common.models.ResultSet();
        resultSet.setQueryId("query_" + System.currentTimeMillis());
        resultSet.setStatus("COMPLETED");
        resultSet.setExecutionTimeMs(50 + (long) (Math.random() * 100)); // 50-150ms

        String lowerQuery = sqlQuery.toLowerCase();

        if (lowerQuery.contains("users")) {
            if (lowerQuery.contains("count")) {
                resultSet.setColumns(Arrays.asList("count"));
                resultSet.addRow(new Row(Arrays.asList("26")));
                resultSet.setTotalRows(1);
            } else if (lowerQuery.contains("where") && lowerQuery.contains("age")) {
                resultSet.setColumns(Arrays.asList("name", "age"));
                resultSet.addRow(new Row(Arrays.asList("John Doe", "30")));
                resultSet.addRow(new Row(Arrays.asList("Bob Johnson", "35")));
                resultSet.addRow(new Row(Arrays.asList("Alice Smith", "32")));
                resultSet.setTotalRows(3);
            } else {
                resultSet.setColumns(Arrays.asList("name", "age", "email", "location"));
                resultSet.addRow(new Row(Arrays.asList("John Doe", "30", "john.doe@email.com", "New York")));
                resultSet.addRow(new Row(Arrays.asList("Jane Smith", "25", "jane.smith@email.com", "California")));
                resultSet.addRow(new Row(Arrays.asList("Bob Johnson", "35", "bob.johnson@email.com", "Texas")));
                resultSet.setTotalRows(3);
            }
        } else if (lowerQuery.contains("orders")) {
            resultSet.setColumns(Arrays.asList("order_id", "user_id", "product_name", "amount", "order_date"));
            resultSet.addRow(new Row(Arrays.asList("ORD001", "1", "Laptop Pro", "1299.99", "2024-01-15")));
            resultSet.addRow(new Row(Arrays.asList("ORD002", "2", "Wireless Mouse", "29.99", "2024-01-16")));
            resultSet.addRow(new Row(Arrays.asList("ORD003", "3", "Mechanical Keyboard", "89.99", "2024-01-17")));
            resultSet.setTotalRows(3);
        } else if (lowerQuery.contains("products")) {
            resultSet.setColumns(Arrays.asList("product_id", "name", "price", "category"));
            resultSet.addRow(new Row(Arrays.asList("1", "Laptop Pro", "1299.99", "Electronics")));
            resultSet.addRow(new Row(Arrays.asList("2", "Wireless Mouse", "29.99", "Electronics")));
            resultSet.addRow(new Row(Arrays.asList("3", "Mechanical Keyboard", "89.99", "Electronics")));
            resultSet.setTotalRows(3);
        } else {
            // Generic result
            resultSet.setColumns(Arrays.asList("result"));
            resultSet.addRow(new Row(Arrays.asList("Sample Result 1")));
            resultSet.addRow(new Row(Arrays.asList("Sample Result 2")));
            resultSet.addRow(new Row(Arrays.asList("Sample Result 3")));
            resultSet.setTotalRows(3);
        }

        return resultSet;
    }

    public List<ShardInfo> getShardInfo() {
        List<ShardInfo> shards = new ArrayList<>();

        // Return shard information for this worker
        if (workerId.equals("worker1")) {
            shards.add(new ShardInfo("users_shard_1", "worker1", "users", "A", "M", 1000));
            shards.add(new ShardInfo("orders_shard_1", "worker1", "orders", "1", "5000", 5000));
            shards.add(new ShardInfo("products_shard_1", "worker1", "products", "Electronics", "Electronics", 5));
        } else if (workerId.equals("worker2")) {
            shards.add(new ShardInfo("users_shard_2", "worker2", "users", "N", "Z", 1200));
            shards.add(new ShardInfo("orders_shard_2", "worker2", "orders", "5001", "10000", 5000));
            shards.add(new ShardInfo("products_shard_2", "worker2", "products", "Appliances", "Appliances", 3));
        } else if (workerId.equals("worker3")) {
            shards.add(new ShardInfo("orders_shard_3", "worker3", "orders", "10001", "15000", 5000));
            shards.add(new ShardInfo("products_shard_3", "worker3", "products", "Sports", "Accessories", 7));
        }

        return shards;
    }

    public boolean healthCheck() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            AppLogger.error("Health check failed for worker {}", workerId, e);
            return false;
        }
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            AppLogger.info("DataStore for worker {} shutdown", workerId);
        }
    }

    private java.sql.ResultSet convertToResultSet(com.distributed.sql.common.models.ResultSet resultSet) {
        // For demo purposes, return null as we're not actually executing SQL
        // In a real implementation, this would convert our ResultSet to
        // java.sql.ResultSet
        return null;
    }
}
