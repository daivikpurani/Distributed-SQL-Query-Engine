//! Query data model representing SQL queries

use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use super::{Condition, Join};

/// Represents a SQL query with all its components
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct Query {
    /// Unique identifier for the query
    pub query_id: String,
    /// Original SQL string
    pub sql: String,
    /// Type of query (SELECT, INSERT, UPDATE, DELETE)
    pub query_type: QueryType,
    /// Columns to select (empty for SELECT *)
    pub select_columns: Vec<String>,
    /// Tables to query from
    pub from_tables: Vec<String>,
    /// WHERE conditions (optional)
    pub where_conditions: Option<Vec<Condition>>,
    /// JOIN clauses (optional)
    pub joins: Option<Vec<Join>>,
    /// Additional metadata
    pub metadata: HashMap<String, String>,
}

/// Types of SQL queries supported
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum QueryType {
    Select,
    Insert,
    Update,
    Delete,
}

impl Query {
    /// Create a new query with a generated ID
    pub fn new(sql: String, query_type: QueryType) -> Self {
        Self {
            query_id: format!("query_{}", chrono::Utc::now().timestamp_millis()),
            sql,
            query_type,
            select_columns: Vec::new(),
            from_tables: Vec::new(),
            where_conditions: None,
            joins: None,
            metadata: HashMap::new(),
        }
    }

    /// Create a new query with a specific ID
    pub fn with_id(query_id: String, sql: String, query_type: QueryType) -> Self {
        Self {
            query_id,
            sql,
            query_type,
            select_columns: Vec::new(),
            from_tables: Vec::new(),
            where_conditions: None,
            joins: None,
            metadata: HashMap::new(),
        }
    }

    /// Add a WHERE condition
    pub fn add_condition(&mut self, condition: Condition) {
        self.where_conditions
            .get_or_insert_with(Vec::new)
            .push(condition);
    }

    /// Add a JOIN clause
    pub fn add_join(&mut self, join: Join) {
        self.joins.get_or_insert_with(Vec::new).push(join);
    }

    /// Check if this is a SELECT query
    pub fn is_select(&self) -> bool {
        matches!(self.query_type, QueryType::Select)
    }

    /// Check if this query has WHERE conditions
    pub fn has_conditions(&self) -> bool {
        self.where_conditions.as_ref().map_or(false, |c| !c.is_empty())
    }

    /// Check if this query has JOINs
    pub fn has_joins(&self) -> bool {
        self.joins.as_ref().map_or(false, |j| !j.is_empty())
    }
}

