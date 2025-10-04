//! Join data model for JOIN clauses

use serde::{Deserialize, Serialize};

/// Represents a JOIN clause
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct Join {
    /// Left table name
    pub left_table: String,
    /// Right table name
    pub right_table: String,
    /// Left column for join condition
    pub left_column: String,
    /// Right column for join condition
    pub right_column: String,
    /// Type of join
    pub join_type: JoinType,
}

/// Types of JOINs supported
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum JoinType {
    Inner,
    Left,
    Right,
    Full,
}

impl Join {
    /// Create a new JOIN
    pub fn new(
        left_table: String,
        right_table: String,
        left_column: String,
        right_column: String,
        join_type: JoinType,
    ) -> Self {
        Self {
            left_table,
            right_table,
            left_column,
            right_column,
            join_type,
        }
    }

    /// Create an INNER JOIN
    pub fn inner(
        left_table: String,
        right_table: String,
        left_column: String,
        right_column: String,
    ) -> Self {
        Self::new(left_table, right_table, left_column, right_column, JoinType::Inner)
    }

    /// Create a LEFT JOIN
    pub fn left(
        left_table: String,
        right_table: String,
        left_column: String,
        right_column: String,
    ) -> Self {
        Self::new(left_table, right_table, left_column, right_column, JoinType::Left)
    }

    /// Create a RIGHT JOIN
    pub fn right(
        left_table: String,
        right_table: String,
        left_column: String,
        right_column: String,
    ) -> Self {
        Self::new(left_table, right_table, left_column, right_column, JoinType::Right)
    }

    /// Create a FULL JOIN
    pub fn full(
        left_table: String,
        right_table: String,
        left_column: String,
        right_column: String,
    ) -> Self {
        Self::new(left_table, right_table, left_column, right_column, JoinType::Full)
    }

    /// Check if this is an INNER JOIN
    pub fn is_inner(&self) -> bool {
        matches!(self.join_type, JoinType::Inner)
    }

    /// Check if this is a LEFT JOIN
    pub fn is_left(&self) -> bool {
        matches!(self.join_type, JoinType::Left)
    }

    /// Check if this is a RIGHT JOIN
    pub fn is_right(&self) -> bool {
        matches!(self.join_type, JoinType::Right)
    }

    /// Check if this is a FULL JOIN
    pub fn is_full(&self) -> bool {
        matches!(self.join_type, JoinType::Full)
    }
}

impl std::fmt::Display for Join {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "{} {} JOIN {} ON {}.{} = {}.{}",
            self.left_table,
            self.join_type,
            self.right_table,
            self.left_table,
            self.left_column,
            self.right_table,
            self.right_column
        )
    }
}

impl std::fmt::Display for JoinType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            JoinType::Inner => write!(f, "INNER"),
            JoinType::Left => write!(f, "LEFT"),
            JoinType::Right => write!(f, "RIGHT"),
            JoinType::Full => write!(f, "FULL"),
        }
    }
}
