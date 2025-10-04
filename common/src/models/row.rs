//! Row data model representing a single row of data

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Represents a single row of data from a table
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct Row {
    /// Values in this row (ordered by column)
    pub values: Vec<String>,
    /// Additional metadata for this row
    pub metadata: HashMap<String, String>,
    /// Name of the table this row belongs to
    pub table_name: String,
    /// Worker ID that owns this row
    pub worker_id: Option<String>,
    /// Shard ID this row belongs to
    pub shard_id: Option<String>,
}

impl Row {
    /// Create a new row
    pub fn new(values: Vec<String>, table_name: String) -> Self {
        Self {
            values,
            metadata: HashMap::new(),
            table_name,
            worker_id: None,
            shard_id: None,
        }
    }

    /// Create a new row with metadata
    pub fn with_metadata(
        values: Vec<String>,
        table_name: String,
        metadata: HashMap<String, String>,
    ) -> Self {
        Self {
            values,
            metadata,
            table_name,
            worker_id: None,
            shard_id: None,
        }
    }

    /// Get a value by index
    pub fn get_value(&self, index: usize) -> Option<&String> {
        self.values.get(index)
    }

    /// Set a value by index
    pub fn set_value(&mut self, index: usize, value: String) -> Result<(), String> {
        if index >= self.values.len() {
            return Err(format!("Index {} out of bounds for row with {} columns", index, self.values.len()));
        }
        self.values[index] = value;
        Ok(())
    }

    /// Add a value to the end of the row
    pub fn add_value(&mut self, value: String) {
        self.values.push(value);
    }

    /// Get the number of columns in this row
    pub fn column_count(&self) -> usize {
        self.values.len()
    }

    /// Check if this row is empty
    pub fn is_empty(&self) -> bool {
        self.values.is_empty()
    }

    /// Set metadata for this row
    pub fn set_metadata(&mut self, key: String, value: String) {
        self.metadata.insert(key, value);
    }

    /// Get metadata for this row
    pub fn get_metadata(&self, key: &str) -> Option<&String> {
        self.metadata.get(key)
    }

    /// Set the worker ID for this row
    pub fn set_worker_id(&mut self, worker_id: String) {
        self.worker_id = Some(worker_id);
    }

    /// Set the shard ID for this row
    pub fn set_shard_id(&mut self, shard_id: String) {
        self.shard_id = Some(shard_id);
    }

    /// Project specific columns from this row
    pub fn project_columns(&self, column_indices: &[usize]) -> Result<Row, String> {
        let mut projected_values = Vec::new();
        
        for &index in column_indices {
            let value = self.get_value(index)
                .ok_or_else(|| format!("Column index {} not found", index))?;
            projected_values.push(value.clone());
        }

        Ok(Row {
            values: projected_values,
            metadata: self.metadata.clone(),
            table_name: self.table_name.clone(),
            worker_id: self.worker_id.clone(),
            shard_id: self.shard_id.clone(),
        })
    }

    /// Convert this row to a string representation
    pub fn to_string(&self) -> String {
        self.values.join(",")
    }

    /// Convert this row to a JSON string
    pub fn to_json(&self) -> Result<String, serde_json::Error> {
        serde_json::to_string(self)
    }
}

impl std::fmt::Display for Row {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.to_string())
    }
}

impl From<Vec<String>> for Row {
    fn from(values: Vec<String>) -> Self {
        Self::new(values, "unknown".to_string())
    }
}

impl From<(&[String], &str)> for Row {
    fn from((values, table_name): (&[String], &str)) -> Self {
        Self::new(values.to_vec(), table_name.to_string())
    }
}
