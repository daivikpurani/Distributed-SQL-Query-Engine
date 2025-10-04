//! Query execution plan node data model

use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use super::Condition;

/// Represents a node in the query execution plan tree
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct PlanNode {
    /// Unique identifier for this node
    pub node_id: String,
    /// Type of operation this node performs
    pub node_type: NodeType,
    /// Table name (for SCAN nodes)
    pub table_name: Option<String>,
    /// Columns to process (for SCAN/PROJECT nodes)
    pub columns: Option<Vec<String>>,
    /// Conditions to apply (for FILTER nodes)
    pub conditions: Option<Vec<Condition>>,
    /// Child nodes in the execution tree
    pub children: Vec<PlanNode>,
    /// Estimated number of rows this node will produce
    pub estimated_rows: u32,
    /// Worker ID assigned to execute this node
    pub worker_id: Option<String>,
    /// Additional metadata
    pub metadata: HashMap<String, String>,
}

/// Types of plan nodes
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum NodeType {
    Scan,      // Table scan operation
    Filter,    // WHERE condition application
    Project,   // Column selection
    Join,      // Table join operation
    Aggregate, // Aggregation operations (GROUP BY, etc.)
}

impl PlanNode {
    /// Create a new plan node
    pub fn new(node_id: String, node_type: NodeType) -> Self {
        Self {
            node_id,
            node_type,
            table_name: None,
            columns: None,
            conditions: None,
            children: Vec::new(),
            estimated_rows: 0,
            worker_id: None,
            metadata: HashMap::new(),
        }
    }

    /// Create a scan node for a table
    pub fn scan(table_name: String, columns: Option<Vec<String>>) -> Self {
        Self {
            node_id: format!("scan_{}", table_name),
            node_type: NodeType::Scan,
            table_name: Some(table_name),
            columns,
            conditions: None,
            children: Vec::new(),
            estimated_rows: 0,
            worker_id: None,
            metadata: HashMap::new(),
        }
    }

    /// Create a filter node with conditions
    pub fn filter(conditions: Vec<Condition>) -> Self {
        Self {
            node_id: format!("filter_{}", uuid::Uuid::new_v4().to_string()[..8].to_string()),
            node_type: NodeType::Filter,
            table_name: None,
            columns: None,
            conditions: Some(conditions),
            children: Vec::new(),
            estimated_rows: 0,
            worker_id: None,
            metadata: HashMap::new(),
        }
    }

    /// Create a project node for column selection
    pub fn project(columns: Vec<String>) -> Self {
        Self {
            node_id: format!("project_{}", uuid::Uuid::new_v4().to_string()[..8].to_string()),
            node_type: NodeType::Project,
            table_name: None,
            columns: Some(columns),
            conditions: None,
            children: Vec::new(),
            estimated_rows: 0,
            worker_id: None,
            metadata: HashMap::new(),
        }
    }

    /// Create a join node
    pub fn join(left_table: String, right_table: String) -> Self {
        Self {
            node_id: format!("join_{}", uuid::Uuid::new_v4().to_string()[..8].to_string()),
            node_type: NodeType::Join,
            table_name: Some(format!("{}_{}", left_table, right_table)),
            columns: None,
            conditions: None,
            children: Vec::new(),
            estimated_rows: 0,
            worker_id: None,
            metadata: HashMap::new(),
        }
    }

    /// Add a child node
    pub fn add_child(&mut self, child: PlanNode) {
        self.children.push(child);
    }

    /// Set the worker ID for this node
    pub fn set_worker_id(&mut self, worker_id: String) {
        self.worker_id = Some(worker_id);
    }

    /// Set the estimated row count
    pub fn set_estimated_rows(&mut self, rows: u32) {
        self.estimated_rows = rows;
    }

    /// Check if this is a leaf node (no children)
    pub fn is_leaf(&self) -> bool {
        self.children.is_empty()
    }

    /// Get the total estimated cost of this subtree
    pub fn estimated_cost(&self) -> u32 {
        self.estimated_rows + self.children.iter().map(|c| c.estimated_cost()).sum::<u32>()
    }

    /// Clone this node with a new ID
    pub fn clone_with_new_id(&self) -> Self {
        Self {
            node_id: format!("{}_{}", self.node_id, uuid::Uuid::new_v4().to_string()[..8].to_string()),
            node_type: self.node_type.clone(),
            table_name: self.table_name.clone(),
            columns: self.columns.clone(),
            conditions: self.conditions.clone(),
            children: self.children.iter().map(|c| c.clone_with_new_id()).collect(),
            estimated_rows: self.estimated_rows,
            worker_id: self.worker_id.clone(),
            metadata: self.metadata.clone(),
        }
    }
}
