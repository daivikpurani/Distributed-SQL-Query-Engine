//! Shard key data model for data distribution

use serde::{Deserialize, Serialize};

/// Represents a shard key used for data distribution
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct ShardKey {
    /// Column name
    pub column_name: String,
    /// Value of the shard key
    pub value: String,
    /// Data type of the value
    pub data_type: DataType,
    /// Whether this is a primary key
    pub is_primary_key: bool,
}

/// Data types for shard key values
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum DataType {
    String,
    Integer,
    Long,
    Double,
    Boolean,
    Date,
}

impl ShardKey {
    /// Create a new shard key
    pub fn new(column_name: String, value: String, data_type: DataType) -> Self {
        Self {
            column_name,
            value,
            data_type,
            is_primary_key: false,
        }
    }

    /// Create a new shard key with primary key flag
    pub fn with_primary_key(
        column_name: String,
        value: String,
        data_type: DataType,
        is_primary_key: bool,
    ) -> Self {
        Self {
            column_name,
            value,
            data_type,
            is_primary_key,
        }
    }

    /// Calculate hash for this shard key
    pub fn calculate_hash(&self) -> u32 {
        if self.value.is_empty() {
            return 0;
        }
        use std::collections::hash_map::DefaultHasher;
        use std::hash::{Hash, Hasher};
        
        let mut hasher = DefaultHasher::new();
        self.value.hash(&mut hasher);
        hasher.finish() as u32
    }

    /// Calculate hash for consistent sharding
    pub fn calculate_consistent_hash(&self, num_shards: u32) -> u32 {
        if self.value.is_empty() {
            return 0;
        }
        self.calculate_hash() % num_shards
    }

    /// Parse the value according to its data type
    pub fn parse_value(&self) -> Result<ParsedValue, String> {
        match self.data_type {
            DataType::String => Ok(ParsedValue::String(self.value.clone())),
            DataType::Integer => {
                let parsed = self.value.parse::<i32>()
                    .map_err(|_| format!("Cannot parse '{}' as integer", self.value))?;
                Ok(ParsedValue::Integer(parsed))
            },
            DataType::Long => {
                let parsed = self.value.parse::<i64>()
                    .map_err(|_| format!("Cannot parse '{}' as long", self.value))?;
                Ok(ParsedValue::Long(parsed))
            },
            DataType::Double => {
                let parsed = self.value.parse::<f64>()
                    .map_err(|_| format!("Cannot parse '{}' as double", self.value))?;
                Ok(ParsedValue::Double(parsed))
            },
            DataType::Boolean => {
                let parsed = self.value.parse::<bool>()
                    .map_err(|_| format!("Cannot parse '{}' as boolean", self.value))?;
                Ok(ParsedValue::Boolean(parsed))
            },
            DataType::Date => {
                // For simplicity, we'll treat dates as strings
                // In a real implementation, you'd parse this as a proper date
                Ok(ParsedValue::String(self.value.clone()))
            },
        }
    }

    /// Check if this shard key is valid
    pub fn is_valid(&self) -> bool {
        !self.column_name.is_empty() && !self.value.is_empty()
    }
}

/// Parsed values for shard keys
#[derive(Debug, Clone, PartialEq)]
pub enum ParsedValue {
    String(String),
    Integer(i32),
    Long(i64),
    Double(f64),
    Boolean(bool),
}

impl std::fmt::Display for ShardKey {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "ShardKey{{column={}, value={}, type={}, primary={}}}",
            self.column_name,
            self.value,
            self.data_type,
            self.is_primary_key
        )
    }
}

impl std::fmt::Display for DataType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            DataType::String => write!(f, "STRING"),
            DataType::Integer => write!(f, "INTEGER"),
            DataType::Long => write!(f, "LONG"),
            DataType::Double => write!(f, "DOUBLE"),
            DataType::Boolean => write!(f, "BOOLEAN"),
            DataType::Date => write!(f, "DATE"),
        }
    }
}
