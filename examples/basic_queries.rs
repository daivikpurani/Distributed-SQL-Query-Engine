//! Basic query examples for the distributed SQL engine
//! 
//! This example demonstrates basic SQL query operations

use common::{Query, QueryType, Condition, Operator, DataType};
use std::collections::HashMap;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🔍 Basic Query Examples for Distributed SQL Engine");
    println!("==================================================");

    // Example 1: Simple SELECT query
    println!("\n1. Simple SELECT Query:");
    let mut query1 = Query::new(
        "SELECT name, age FROM users".to_string(),
        QueryType::Select,
    );
    query1.select_columns = vec!["name".to_string(), "age".to_string()];
    query1.from_tables = vec!["users".to_string()];
    
    println!("Query: {}", query1.sql);
    println!("Columns: {:?}", query1.select_columns);
    println!("Tables: {:?}", query1.from_tables);

    // Example 2: SELECT with WHERE condition
    println!("\n2. SELECT with WHERE Condition:");
    let mut query2 = Query::new(
        "SELECT name FROM users WHERE age > 25".to_string(),
        QueryType::Select,
    );
    query2.select_columns = vec!["name".to_string()];
    query2.from_tables = vec!["users".to_string()];
    
    let condition = Condition::new(
        "age".to_string(),
        Operator::GreaterThan,
        "25".to_string(),
        DataType::Integer,
    );
    query2.add_condition(condition);
    
    println!("Query: {}", query2.sql);
    println!("Has conditions: {}", query2.has_conditions());

    // Example 3: SELECT with multiple conditions
    println!("\n3. SELECT with Multiple Conditions:");
    let mut query3 = Query::new(
        "SELECT * FROM users WHERE age > 25 AND city = 'New York'".to_string(),
        QueryType::Select,
    );
    query3.select_columns = vec!["*".to_string()];
    query3.from_tables = vec!["users".to_string()];
    
    let condition1 = Condition::new(
        "age".to_string(),
        Operator::GreaterThan,
        "25".to_string(),
        DataType::Integer,
    );
    let condition2 = Condition::new(
        "city".to_string(),
        Operator::Equals,
        "New York".to_string(),
        DataType::String,
    );
    query3.add_condition(condition1);
    query3.add_condition(condition2);
    
    println!("Query: {}", query3.sql);
    println!("Conditions count: {}", query3.where_conditions.as_ref().unwrap().len());

    // Example 4: SELECT with LIKE operator
    println!("\n4. SELECT with LIKE Operator:");
    let mut query4 = Query::new(
        "SELECT name FROM users WHERE name LIKE 'John%'".to_string(),
        QueryType::Select,
    );
    query4.select_columns = vec!["name".to_string()];
    query4.from_tables = vec!["users".to_string()];
    
    let like_condition = Condition::new(
        "name".to_string(),
        Operator::Like,
        "John%".to_string(),
        DataType::String,
    );
    query4.add_condition(like_condition);
    
    println!("Query: {}", query4.sql);

    // Example 5: SELECT with IN operator
    println!("\n5. SELECT with IN Operator:");
    let mut query5 = Query::new(
        "SELECT name FROM users WHERE city IN ('New York', 'Los Angeles')".to_string(),
        QueryType::Select,
    );
    query5.select_columns = vec!["name".to_string()];
    query5.from_tables = vec!["users".to_string()];
    
    let in_condition = Condition::new(
        "city".to_string(),
        Operator::In,
        "New York, Los Angeles".to_string(),
        DataType::String,
    );
    query5.add_condition(in_condition);
    
    println!("Query: {}", query5.sql);

    println!("\n✅ All basic query examples created successfully!");
    println!("\nThese queries demonstrate:");
    println!("- Simple column selection");
    println!("- WHERE clause filtering");
    println!("- Multiple conditions");
    println!("- Pattern matching with LIKE");
    println!("- Set membership with IN");

    Ok(())
}
