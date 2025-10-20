package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.utils.AppLogger;
import com.distributed.sql.common.utils.Tracer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages data distribution and shard assignments across workers
 */
public class ShardManager {

    private final Map<String, List<ShardInfo>> shards;
    private final Map<String, WorkerInfo> workers;
    private final ShardType shardType;

    public ShardManager() {
        this.shards = new ConcurrentHashMap<>();
        this.workers = new ConcurrentHashMap<>();
        this.shardType = ShardType.HASH;

        initializeDefaultShards();
    }

    private void initializeDefaultShards() {
        // Initialize users table shards
        List<ShardInfo> userShards = Arrays.asList(
                new ShardInfo("users_shard_1", "worker1", "users", "A", "M", 1000),
                new ShardInfo("users_shard_2", "worker2", "users", "N", "Z", 1200));
        shards.put("users", userShards);

        // Initialize orders table shards
        List<ShardInfo> orderShards = Arrays.asList(
                new ShardInfo("orders_shard_1", "worker1", "orders", "1", "5000", 5000),
                new ShardInfo("orders_shard_2", "worker2", "orders", "5001", "10000", 5000),
                new ShardInfo("orders_shard_3", "worker3", "orders", "10001", "15000", 5000));
        shards.put("orders", orderShards);

        // Initialize products table shards
        List<ShardInfo> productShards = Arrays.asList(
                new ShardInfo("products_shard_1", "worker1", "products", "Electronics", "Electronics", 5),
                new ShardInfo("products_shard_2", "worker2", "products", "Appliances", "Appliances", 3),
                new ShardInfo("products_shard_3", "worker3", "products", "Sports", "Accessories", 7));
        shards.put("products", productShards);

        AppLogger.info("Initialized default shard distribution");
    }

    public List<ShardInfo> getShardsForTable(String tableName) {
        return shards.getOrDefault(tableName, new ArrayList<>());
    }

    public List<ShardInfo> getWorkerShards(String workerId) {
        List<ShardInfo> workerShards = new ArrayList<>();

        for (List<ShardInfo> tableShards : shards.values()) {
            for (ShardInfo shard : tableShards) {
                if (shard.getWorkerId().equals(workerId)) {
                    workerShards.add(shard);
                }
            }
        }

        return workerShards;
    }

    public void addShard(String tableName, ShardInfo shard) {
        shards.computeIfAbsent(tableName, k -> new ArrayList<>()).add(shard);
        AppLogger.info("Added shard {} for table {} to worker {}",
                shard.getShardId(), tableName, shard.getWorkerId());
    }

    public Map<String, List<String>> getShardDistribution() {
        Map<String, List<String>> distribution = new HashMap<>();

        for (Map.Entry<String, List<ShardInfo>> entry : shards.entrySet()) {
            String tableName = entry.getKey();
            List<String> workers = new ArrayList<>();

            for (ShardInfo shard : entry.getValue()) {
                if (!workers.contains(shard.getWorkerId())) {
                    workers.add(shard.getWorkerId());
                }
            }

            distribution.put(tableName, workers);
        }

        return distribution;
    }

    public long getTotalRows() {
        return shards.values().stream()
                .flatMap(List::stream)
                .mapToLong(ShardInfo::getRowCount)
                .sum();
    }

    public void registerWorker(String workerId, String address, int port) {
        WorkerInfo workerInfo = new WorkerInfo(workerId, address, port);
        workers.put(workerId, workerInfo);
        AppLogger.info("Registered worker: {} at {}:{}", workerId, address, port);
    }

    public void updateWorkerHeartbeat(String workerId, double cpuUsage, double memoryUsage, int activeQueries) {
        WorkerInfo workerInfo = workers.get(workerId);
        if (workerInfo != null) {
            workerInfo.updateHeartbeat(cpuUsage, memoryUsage, activeQueries);
            AppLogger.debug("Updated heartbeat for worker: {} - CPU: {}%, Memory: {}%, Active queries: {}",
                    workerId, cpuUsage, memoryUsage, activeQueries);
        }
    }

    public List<WorkerInfo> getActiveWorkers() {
        return new ArrayList<>(workers.values());
    }

    public WorkerInfo getWorker(String workerId) {
        return workers.get(workerId);
    }

    public boolean isWorkerHealthy(String workerId) {
        WorkerInfo workerInfo = workers.get(workerId);
        if (workerInfo == null) {
            return false;
        }

        // Consider worker healthy if last heartbeat was within 30 seconds
        long timeSinceLastHeartbeat = System.currentTimeMillis() - workerInfo.getLastHeartbeat();
        return timeSinceLastHeartbeat < 30000;
    }

    public void removeWorker(String workerId) {
        workers.remove(workerId);
        AppLogger.warn("Removed worker: {}", workerId);
    }

    /**
     * Worker information for tracking worker status
     */
    public static class WorkerInfo {
        private final String workerId;
        private final String address;
        private final int port;
        private double cpuUsage;
        private double memoryUsage;
        private int activeQueries;
        private long lastHeartbeat;

        public WorkerInfo(String workerId, String address, int port) {
            this.workerId = workerId;
            this.address = address;
            this.port = port;
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public void updateHeartbeat(double cpuUsage, double memoryUsage, int activeQueries) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.activeQueries = activeQueries;
            this.lastHeartbeat = System.currentTimeMillis();
        }

        // Getters
        public String getWorkerId() {
            return workerId;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public double getMemoryUsage() {
            return memoryUsage;
        }

        public int getActiveQueries() {
            return activeQueries;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }
    }
}
