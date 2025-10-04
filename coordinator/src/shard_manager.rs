use std::collections::HashMap;
use tracing::{info, debug};

#[derive(Debug, Clone)]
pub struct ShardInfo {
    pub shard_id: String,
    pub worker_id: String,
    pub table_name: String,
    pub key_range: Option<(String, String)>,
    pub row_count: u64,
}

#[derive(Debug, Clone)]
pub enum ShardType {
    Hash,
    Range,
    RoundRobin,
}

#[derive(Debug)]
pub struct ShardManager {
    shards: HashMap<String, Vec<ShardInfo>>,
    shard_type: ShardType,
}

impl ShardManager {
    pub fn new() -> Self {
        let mut shards = HashMap::new();
        
        // Initialize with some demo shards
        shards.insert("users".to_string(), vec![
            ShardInfo {
                shard_id: "users_shard_1".to_string(),
                worker_id: "worker1".to_string(),
                table_name: "users".to_string(),
                key_range: Some(("A".to_string(), "M".to_string())),
                row_count: 1000,
            },
            ShardInfo {
                shard_id: "users_shard_2".to_string(),
                worker_id: "worker2".to_string(),
                table_name: "users".to_string(),
                key_range: Some(("N".to_string(), "Z".to_string())),
                row_count: 1200,
            },
        ]);

        shards.insert("orders".to_string(), vec![
            ShardInfo {
                shard_id: "orders_shard_1".to_string(),
                worker_id: "worker1".to_string(),
                table_name: "orders".to_string(),
                key_range: Some(("1".to_string(), "5000".to_string())),
                row_count: 5000,
            },
            ShardInfo {
                shard_id: "orders_shard_2".to_string(),
                worker_id: "worker2".to_string(),
                table_name: "orders".to_string(),
                key_range: Some(("5001".to_string(), "10000".to_string())),
                row_count: 5000,
            },
            ShardInfo {
                shard_id: "orders_shard_3".to_string(),
                worker_id: "worker3".to_string(),
                table_name: "orders".to_string(),
                key_range: Some(("10001".to_string(), "15000".to_string())),
                row_count: 5000,
            },
        ]);

        Self {
            shards,
            shard_type: ShardType::Hash,
        }
    }

    pub fn get_shards_for_table(&self, table_name: &str) -> Vec<ShardInfo> {
        self.shards.get(table_name)
            .cloned()
            .unwrap_or_else(|| {
                debug!("No shards found for table: {}", table_name);
                vec![]
            })
    }

    pub fn get_worker_shards(&self, worker_id: &str) -> Vec<ShardInfo> {
        let mut worker_shards = Vec::new();
        
        for shards in self.shards.values() {
            for shard in shards {
                if shard.worker_id == worker_id {
                    worker_shards.push(shard.clone());
                }
            }
        }
        
        worker_shards
    }

    pub fn add_shard(&mut self, table_name: String, shard: ShardInfo) {
        self.shards.entry(table_name)
            .or_insert_with(Vec::new)
            .push(shard);
    }

    pub fn get_shard_distribution(&self) -> HashMap<String, Vec<String>> {
        let mut distribution = HashMap::new();
        
        for (table_name, shards) in &self.shards {
            let mut workers = Vec::new();
            for shard in shards {
                if !workers.contains(&shard.worker_id) {
                    workers.push(shard.worker_id.clone());
                }
            }
            distribution.insert(table_name.clone(), workers);
        }
        
        distribution
    }

    pub fn get_total_rows(&self) -> u64 {
        self.shards.values()
            .flatten()
            .map(|shard| shard.row_count)
            .sum()
    }
}
