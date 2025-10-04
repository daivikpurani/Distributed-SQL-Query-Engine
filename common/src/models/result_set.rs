//! Result set data model for query results

use serde::{Deserialize, Serialize};
use crate::models::Row;
use std::collections::HashMap;

/// Represents the result set of a query execution
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct ResultSet {
    /// Query ID this result set belongs to
    pub query_id: String,
    /// Rows in the result set
    pub rows: Vec<Row>,
    /// Column names (in order)
    pub column_names: Vec<String>,
    /// Total execution time in milliseconds
    pub execution_time_ms: u64,
    /// Additional metadata
    pub metadata: HashMap<String, String>,
}

impl ResultSet {
    /// Create a new empty result set
    pub fn new(query_id: String) -> Self {
        Self {
            query_id,
            rows: Vec::new(),
            column_names: Vec::new(),
            execution_time_ms: 0,
            metadata: HashMap::new(),
        }
    }

    /// Create a new result set with data
    pub fn with_data(
        query_id: String,
        rows: Vec<Row>,
        column_names: Vec<String>,
        execution_time_ms: u64,
    ) -> Self {
        Self {
            query_id,
            rows,
            column_names,
            execution_time_ms,
            metadata: HashMap::new(),
        }
    }

    /// Add a row to the result set
    pub fn add_row(&mut self, row: Row) {
        self.rows.push(row);
    }

    /// Add multiple rows to the result set
    pub fn add_rows(&mut self, rows: Vec<Row>) {
        self.rows.extend(rows);
    }

    /// Get the number of rows in the result set
    pub fn row_count(&self) -> usize {
        self.rows.len()
    }

    /// Get the number of columns in the result set
    pub fn column_count(&self) -> usize {
        self.column_names.len()
    }

    /// Check if the result set is empty
    pub fn is_empty(&self) -> bool {
        self.rows.is_empty()
    }

    /// Set execution time
    pub fn set_execution_time(&mut self, time_ms: u64) {
        self.execution_time_ms = time_ms;
    }

    /// Add metadata
    pub fn add_metadata(&mut self, key: String, value: String) {
        self.metadata.insert(key, value);
    }

    /// Get metadata
    pub fn get_metadata(&self, key: &str) -> Option<&String> {
        self.metadata.get(key)
    }

    /// Convert to a string representation (CSV format)
    pub fn to_csv(&self) -> String {
        let mut result = String::new();
        
        // Add header
        result.push_str(&self.column_names.join(","));
        result.push('\n');
        
        // Add rows
        for row in &self.rows {
            result.push_str(&row.to_string());
            result.push('\n');
        }
        
        result
    }

    /// Convert to JSON string
    pub fn to_json(&self) -> Result<String, serde_json::Error> {
        serde_json::to_string_pretty(self)
    }

    /// Get a specific row by index
    pub fn get_row(&self, index: usize) -> Option<&Row> {
        self.rows.get(index)
    }

    /// Get all rows
    pub fn get_rows(&self) -> &[Row] {
        &self.rows
    }

    /// Clear all rows
    pub fn clear(&mut self) {
        self.rows.clear();
    }

    /// Merge another result set into this one
    pub fn merge(&mut self, other: ResultSet) {
        self.rows.extend(other.rows);
        self.execution_time_ms += other.execution_time_ms;
        
        // Merge metadata
        for (key, value) in other.metadata {
            self.metadata.insert(key, value);
        }
    }
}

impl std::fmt::Display for ResultSet {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "ResultSet for query: {}", self.query_id)?;
        writeln!(f, "Rows: {}, Columns: {}", self.row_count(), self.column_count())?;
        writeln!(f, "Execution time: {}ms", self.execution_time_ms)?;
        
        if !self.rows.is_empty() {
            writeln!(f, "Data:")?;
            writeln!(f, "{}", self.to_csv())?;
        }
        
        Ok(())
    }
}
