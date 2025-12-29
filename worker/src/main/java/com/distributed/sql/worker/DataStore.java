package com.distributed.sql.worker;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.distributed.sql.common.models.ShardInfo;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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

    /**
     * Execute SQL query against PostgreSQL database.
     * 
     * Migration from mock data to real execution (2024-12-27):
     * - Initially used mock data to focus on distributed architecture
     * - Replaced with real PostgreSQL execution to demonstrate actual capability
     * - Uses HikariCP connection pooling for performance
     * - Handles NULL values and extracts column metadata
     * 
     * Trade-off: Real execution adds latency and requires database setup,
     * but provides authentic demonstration of distributed SQL capabilities.
     * 
     * @param sqlQuery The SQL query to execute
     * @return List of Row objects with query results
     * @throws RuntimeException if SQL execution fails
     */
    public List<com.distributed.sql.common.models.Row> executeQuery(String sqlQuery) {
        String traceId = Tracer.startTrace("execute_query");

        try {
            AppLogger.info("Executing real SQL query on worker {}: {}", workerId, sqlQuery);

            long startTime = System.currentTimeMillis();

            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement();
                    java.sql.ResultSet rs = stmt.executeQuery(sqlQuery)) {

                // Get column metadata
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Extract column names
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }

                // Convert rows
                List<com.distributed.sql.common.models.Row> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> values = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        values.add(value != null ? value.toString() : "NULL");
                    }
                    rows.add(new com.distributed.sql.common.models.Row(values));
                }

                long executionTime = System.currentTimeMillis() - startTime;
                AppLogger.info("Query completed in {}ms, returned {} rows", executionTime, rows.size());

                return rows;

            } catch (SQLException e) {
                AppLogger.error("SQL execution failed for worker {}", workerId, e);
                throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
            }

        } finally {
            Tracer.endTrace("execute_query");
        }
    }

    /**
     * Returns column information for this worker's shards.
     * This is used by the coordinator to understand what columns are available.
     */
    public List<ShardInfo> getColumnMetadata(String tableName) {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(
                        "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = '" + tableName
                                + "'")) {

            List<ShardInfo> columnInfo = new ArrayList<>();
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                // Store column metadata in ShardInfo
            }
            return columnInfo;
        } catch (SQLException e) {
            AppLogger.error("Failed to get column metadata for table {}", tableName, e);
            return new ArrayList<>();
        }
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

    /**
     * Get connection pool metrics for monitoring.
     * 
     * Implemented for Priority 1: Connection Pool Monitoring.
     * This exposes HikariCP pool statistics for operational visibility.
     * Metrics are polled every 30 seconds by ConnectionPoolMonitor.
     * 
     * @return Pool metrics as a formatted string
     */
    public String getPoolMetrics() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        return String.format(
                "Pool[active=%d, idle=%d, total=%d, waiting=%d, activeConnections=%d, idleConnections=%d, totalConnections=%d, threadsAwaitingConnection=%d]",
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections(),
                ds.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections(),
                ds.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    /**
     * Get active connections count.
     */
    public int getActiveConnections() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        return ds.getHikariPoolMXBean().getActiveConnections();
    }

    /**
     * Get idle connections count.
     */
    public int getIdleConnections() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        return ds.getHikariPoolMXBean().getIdleConnections();
    }

    /**
     * Get total connections count.
     */
    public int getTotalConnections() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        return ds.getHikariPoolMXBean().getTotalConnections();
    }

    /**
     * Check if connection pool is near exhaustion.
     */
    public boolean isPoolNearExhaustion() {
        HikariDataSource ds = (HikariDataSource) dataSource;
        double utilization = (double) ds.getHikariPoolMXBean().getActiveConnections()
                / ds.getMaximumPoolSize();
        return utilization > 0.8; // Alert if > 80% utilization
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            AppLogger.info("DataStore for worker {} shutdown", workerId);
        }
    }

}
