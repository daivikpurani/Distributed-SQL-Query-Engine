//! Shard information data model

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Represents information about a data shard
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct ShardInfo {
    /// Unique identifier for this shard
    pub shard_id: String,
    /// Worker ID that owns this shard
    pub worker_id: String,
    /// Table name this shard belongs to
    pub table_name: String,
    /// Type of sharding strategy used
    pub shard_type: ShardType,
    /// Shard key values for this shard
    pub shard_key: HashMap<String, String>,
    /// Number of rows in this shard
    pub row_count: u64,
    /// Size of this shard in bytes
    pub size_bytes: u64,
    /// Timestamp when this shard was created
    pub created_at: u64,
    /// Timestamp when this shard was last updated
    pub last_updated: u64,
    /// Current status of this shard
    pub status: ShardStatus,
}

/// Types of sharding strategies
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum ShardType {
    HashBased,   // Data distributed by hash of shard key
    RangeBased,  // Data distributed by range of shard key
    RoundRobin,  // Data distributed in round-robin fashion
    Custom,      // Custom distribution logic
}

/// Status of a shard
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum ShardStatus {
    Active,     // Shard is active and serving requests
    Migrating,  // Shard is being migrated to another worker
    Inactive,   // Shard is inactive (maintenance, etc.)
    Failed,     // Shard has failed and needs recovery
}

impl ShardInfo {
    /// Create a new shard info
    pub fn new(shard_id: String, worker_id: String, table_name: String, shard_type: ShardType) -> Self {
        let now = chrono::Utc::now().timestamp_millis() as u64;
        Self {
            shard_id,
            worker_id,
            table_name,
            shard_type,
            shard_key: HashMap::new(),
            row_count: 0,
            size_bytes: 0,
            created_at: now,
            last_updated: now,
            status: ShardStatus::Active,
        }
    }

    /// Update the row count
    pub fn update_row_count(&mut self, count: u64) {
        self.row_count = count;
        self.last_updated = chrono::Utc::now().timestamp_millis() as u64;
    }

    /// Update the size in bytes
    pub fn update_size_bytes(&mut self, size: u64) {
        self.size_bytes = size;
        self.last_updated = chrono::Utc::now().timestamp_millis() as u64;
    }

    /// Set the shard status
    pub fn set_status(&mut self, status: ShardStatus) {
        self.status = status;
        self.last_updated = chrono::Utc::now().timestamp_millis() as u64;
    }

    /// Add a shard key value
    pub fn add_shard_key(&mut self, key: String, value: String) {
        self.shard_key.insert(key, value);
        self.last_updated = chrono::Utc::now().timestamp_millis() as u64;
    }

    /// Check if this shard is active
    pub fn is_active(&self) -> bool {
        matches!(self.status, ShardStatus::Active)
    }

    /// Check if this shard is migrating
    pub fn is_migrating(&self) -> bool {
        matches!(self.status, ShardStatus::Migrating)
    }

    /// Check if this shard has failed
    pub fn has_failed(&self) -> bool {
        matches!(self.status, ShardStatus::Failed)
    }

    /// Get the age of this shard in milliseconds
    pub fn age_ms(&self) -> u64 {
        chrono::Utc::now().timestamp_millis() as u64 - self.created_at
    }

    /// Get the time since last update in milliseconds
    pub fn time_since_update_ms(&self) -> u64 {
        chrono::Utc::now().timestamp_millis() as u64 - self.last_updated
    }
}

impl std::fmt::Display for ShardInfo {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "ShardInfo{{id={}, worker={}, table={}, type={}, rows={}, status={}}}",
            self.shard_id,
            self.worker_id,
            self.table_name,
            self.shard_type,
            self.row_count,
            self.status
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

impl std::fmt::Display for ShardStatus {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            ShardStatus::Active => write!(f, "ACTIVE"),
            ShardStatus::Migrating => write!(f, "MIGRATING"),
            ShardStatus::Inactive => write!(f, "INACTIVE"),
            ShardStatus::Failed => write!(f, "FAILED"),
        }
    }
}
