//! Condition data model for WHERE clauses

use serde::{Deserialize, Serialize};
use super::Row;

/// Represents a WHERE condition
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct Condition {
    /// Column name
    pub column: String,
    /// Comparison operator
    pub operator: Operator,
    /// Value to compare against
    pub value: String,
    /// Data type of the value
    pub data_type: DataType,
}

/// Comparison operators
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum Operator {
    Equals,
    NotEquals,
    GreaterThan,
    LessThan,
    GreaterThanEquals,
    LessThanEquals,
    Like,
    In,
}

/// Data types for values
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum DataType {
    String,
    Integer,
    Double,
    Boolean,
    Date,
}

impl Condition {
    /// Create a new condition
    pub fn new(column: String, operator: Operator, value: String, data_type: DataType) -> Self {
        Self {
            column,
            operator,
            value,
            data_type,
        }
    }

    /// Evaluate this condition against a row
    pub fn evaluate(&self, row: &crate::models::Row, column_index: usize) -> Result<bool, String> {
        let row_value = row.get_value(column_index)
            .ok_or_else(|| format!("Column index {} not found in row", column_index))?;

        match self.operator {
            Operator::Equals => Ok(row_value == &self.value),
            Operator::NotEquals => Ok(row_value != &self.value),
            Operator::GreaterThan => self.compare_numeric(row_value, &self.value, |a, b| a > b),
            Operator::LessThan => self.compare_numeric(row_value, &self.value, |a, b| a < b),
            Operator::GreaterThanEquals => self.compare_numeric(row_value, &self.value, |a, b| a >= b),
            Operator::LessThanEquals => self.compare_numeric(row_value, &self.value, |a, b| a <= b),
            Operator::Like => self.evaluate_like(row_value),
            Operator::In => self.evaluate_in(row_value),
        }
    }

    /// Compare numeric values
    fn compare_numeric<F>(&self, row_value: &str, condition_value: &str, compare_fn: F) -> Result<bool, String>
    where
        F: FnOnce(f64, f64) -> bool,
    {
        let row_num: f64 = row_value.parse()
            .map_err(|_| format!("Cannot parse '{}' as number", row_value))?;
        let condition_num: f64 = condition_value.parse()
            .map_err(|_| format!("Cannot parse '{}' as number", condition_value))?;
        
        Ok(compare_fn(row_num, condition_num))
    }

    /// Evaluate LIKE operator (simplified - only supports % wildcards)
    fn evaluate_like(&self, row_value: &str) -> Result<bool, String> {
        let pattern = &self.value;
        
        if pattern.starts_with('%') && pattern.ends_with('%') {
            let inner = &pattern[1..pattern.len()-1];
            Ok(row_value.contains(inner))
        } else if pattern.starts_with('%') {
            let suffix = &pattern[1..];
            Ok(row_value.ends_with(suffix))
        } else if pattern.ends_with('%') {
            let prefix = &pattern[..pattern.len()-1];
            Ok(row_value.starts_with(prefix))
        } else {
            Ok(row_value == pattern)
        }
    }

    /// Evaluate IN operator
    fn evaluate_in(&self, row_value: &str) -> Result<bool, String> {
        // Parse comma-separated values
        let values: Vec<&str> = self.value.split(',').map(|s| s.trim()).collect();
        Ok(values.contains(&row_value))
    }
}

impl std::fmt::Display for Condition {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{} {} {}", self.column, self.operator, self.value)
    }
}

impl std::fmt::Display for Operator {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Operator::Equals => write!(f, "="),
            Operator::NotEquals => write!(f, "!="),
            Operator::GreaterThan => write!(f, ">"),
            Operator::LessThan => write!(f, "<"),
            Operator::GreaterThanEquals => write!(f, ">="),
            Operator::LessThanEquals => write!(f, "<="),
            Operator::Like => write!(f, "LIKE"),
            Operator::In => write!(f, "IN"),
        }
    }
}
