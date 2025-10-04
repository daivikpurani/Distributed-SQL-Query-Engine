//! Shard distribution strategy data model

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Represents a shard distribution strategy for a table
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct ShardDistribution {
    /// Table name
    pub table_name: String,
    /// Type of sharding strategy
    pub shard_type: ShardType,
    /// Columns used for sharding
    pub shard_columns: Vec<String>,
    /// Number of shards
    pub num_shards: u32,
    /// Additional distribution parameters
    pub distribution_params: HashMap<String, String>,
    /// Whether this distribution is replicated
    pub is_replicated: bool,
    /// Replication factor
    pub replication_factor: u32,
}

/// Types of sharding strategies
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum ShardType {
    HashBased,   // Data distributed by hash of shard key
    RangeBased,  // Data distributed by range of shard key
    RoundRobin,  // Data distributed in round-robin fashion
    Custom,      // Custom distribution logic
}

impl ShardDistribution {
    /// Create a new shard distribution
    pub fn new(
        table_name: String,
        shard_type: ShardType,
        shard_columns: Vec<String>,
        num_shards: u32,
    ) -> Self {
        Self {
            table_name,
            shard_type,
            shard_columns,
            num_shards,
            distribution_params: HashMap::new(),
            is_replicated: false,
            replication_factor: 1,
        }
    }

    /// Create a hash-based distribution
    pub fn hash_based(table_name: String, shard_columns: Vec<String>, num_shards: u32) -> Self {
        Self::new(table_name, ShardType::HashBased, shard_columns, num_shards)
    }

    /// Create a range-based distribution
    pub fn range_based(table_name: String, shard_columns: Vec<String>, num_shards: u32) -> Self {
        Self::new(table_name, ShardType::RangeBased, shard_columns, num_shards)
    }

    /// Create a round-robin distribution
    pub fn round_robin(table_name: String, shard_columns: Vec<String>, num_shards: u32) -> Self {
        Self::new(table_name, ShardType::RoundRobin, shard_columns, num_shards)
    }

    /// Set replication parameters
    pub fn with_replication(&mut self, replication_factor: u32) {
        self.is_replicated = replication_factor > 1;
        self.replication_factor = replication_factor;
    }

    /// Add a distribution parameter
    pub fn add_parameter(&mut self, key: String, value: String) {
        self.distribution_params.insert(key, value);
    }

    /// Get a distribution parameter
    pub fn get_parameter(&self, key: &str) -> Option<&String> {
        self.distribution_params.get(key)
    }

    /// Determine which shard a row should belong to based on shard key values
    pub fn determine_shard_id(&self, row_data: &HashMap<String, String>) -> Result<String, String> {
        match self.shard_type {
            ShardType::HashBased => self.determine_hash_based_shard(row_data),
            ShardType::RangeBased => self.determine_range_based_shard(row_data),
            ShardType::RoundRobin => self.determine_round_robin_shard(row_data),
            ShardType::Custom => Err("Custom sharding not implemented".to_string()),
        }
    }

    /// Determine shard using hash-based distribution
    fn determine_hash_based_shard(&self, row_data: &HashMap<String, String>) -> Result<String, String> {
        let mut hash = 0u32;
        
        for column in &self.shard_columns {
            if let Some(value) = row_data.get(column) {
                hash = hash.wrapping_add(value.len() as u32);
                for byte in value.bytes() {
                    hash = hash.wrapping_add(byte as u32);
                }
            }
        }
        
        let shard_index = hash % self.num_shards;
        Ok(format!("{}_shard_{}", self.table_name, shard_index))
    }

    /// Determine shard using range-based distribution
    fn determine_range_based_shard(&self, row_data: &HashMap<String, String>) -> Result<String, String> {
        // For range-based sharding, we need range parameters
        if let Some(ranges_str) = self.get_parameter("ranges") {
            // This is a simplified implementation
            // In practice, you'd have predefined ranges
            if let Some(first_column) = self.shard_columns.first() {
                if let Some(value) = row_data.get(first_column) {
                    if let Ok(num_value) = value.parse::<i64>() {
                        let shard_index = (num_value.abs() as u32) % self.num_shards;
                        return Ok(format!("{}_shard_{}", self.table_name, shard_index));
                    }
                }
            }
        }
        
        // Fallback to hash-based
        self.determine_hash_based_shard(row_data)
    }

    /// Determine shard using round-robin distribution
    fn determine_round_robin_shard(&self, row_data: &HashMap<String, String>) -> Result<String, String> {
        // For round-robin, we can use any consistent value from the row
        let mut hash = 0u32;
        
        for column in &self.shard_columns {
            if let Some(value) = row_data.get(column) {
                hash = hash.wrapping_add(value.len() as u32);
                break; // Use first available column for round-robin
            }
        }
        
        let shard_index = hash % self.num_shards;
        Ok(format!("{}_shard_{}", self.table_name, shard_index))
    }

    /// Check if this distribution is valid
    pub fn is_valid(&self) -> bool {
        !self.table_name.is_empty() 
            && !self.shard_columns.is_empty() 
            && self.num_shards > 0
            && self.replication_factor > 0
    }

    /// Get the total number of shards including replicas
    pub fn total_shards(&self) -> u32 {
        self.num_shards * self.replication_factor
    }
}

impl std::fmt::Display for ShardDistribution {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "ShardDistribution{{table={}, type={}, columns={:?}, shards={}, replicated={}}}",
            self.table_name,
            self.shard_type,
            self.shard_columns,
            self.num_shards,
            self.is_replicated
        )
    }
}

impl std::fmt::Display for ShardType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            ShardType::HashBased => write!(f, "HASH_BASED"),
            ShardType::RangeBased => write!(f, "RANGE_BASED"),
            ShardType::RoundRobin => write!(f, "ROUND_ROBIN"),
            ShardType::Custom => write!(f, "CUSTOM"),
        }
    }
}
