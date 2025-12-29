package com.distributed.sql.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.distributed.sql.common.utils.AppLogger;

/**
 * Monitors connection pool metrics and logs statistics.
 * Polls HikariCP metrics every 30 seconds and alerts on pool exhaustion.
 */
public class ConnectionPoolMonitor {

    private final DataStore dataStore;
    private final String workerId;
    private final ScheduledExecutorService scheduler;
    private boolean monitoring = false;

    public ConnectionPoolMonitor(String workerId, DataStore dataStore) {
        this.workerId = workerId;
        this.dataStore = dataStore;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Start monitoring the connection pool.
     * Logs pool statistics every 30 seconds and alerts on high utilization.
     */
    public void start() {
        if (monitoring) {
            return;
        }

        monitoring = true;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Get pool metrics
                int active = dataStore.getActiveConnections();
                int idle = dataStore.getIdleConnections();
                int total = dataStore.getTotalConnections();

                // Log pool statistics
                AppLogger.info("Worker {} connection pool: active={}, idle={}, total={}, metrics={}",
                        workerId, active, idle, total, dataStore.getPoolMetrics());

                // Alert if pool is near exhaustion
                if (dataStore.isPoolNearExhaustion()) {
                    AppLogger.warn("Worker {} connection pool is near exhaustion! Utilization > 80%", workerId);
                }

                // Alert if no idle connections available
                if (idle == 0 && active > 0) {
                    AppLogger.warn("Worker {} has no idle connections available. All connections are active.",
                            workerId);
                }

            } catch (Exception e) {
                AppLogger.error("Error monitoring connection pool for worker {}", workerId, e);
            }
        }, 0, 30, TimeUnit.SECONDS);

        AppLogger.info("Connection pool monitoring started for worker {}", workerId);
    }

    /**
     * Stop monitoring the connection pool.
     */
    public void stop() {
        monitoring = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        AppLogger.info("Connection pool monitoring stopped for worker {}", workerId);
    }

    /**
     * Get current connection pool metrics as a formatted string.
     */
    public String getMetrics() {
        return dataStore.getPoolMetrics();
    }

    /**
     * Get current pool utilization percentage.
     */
    public double getPoolUtilization() {
        int active = dataStore.getActiveConnections();
        int total = dataStore.getTotalConnections();
        int max = 10; // From HikariCP config

        if (total == 0) {
            return 0.0;
        }

        return (double) active / max * 100;
    }
}

