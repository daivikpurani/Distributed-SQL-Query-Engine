//! Sharding demonstration for the distributed SQL engine
//! 
//! This example demonstrates how data is sharded across workers

use common::{
    ShardDistribution, ShardType, ShardInfo, ShardStatus,
    ShardKey, DataType as ShardDataType,
};
use std::collections::HashMap;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🔀 Sharding Demonstration for Distributed SQL Engine");
    println!("====================================================");

    // Example 1: Hash-based sharding
    println!("\n1. Hash-based Sharding:");
    let mut hash_distribution = ShardDistribution::hash_based(
        "users".to_string(),
        vec!["user_id".to_string()],
        3,
    );
    
    println!("Distribution: {}", hash_distribution);
    println!("Shard type: {}", hash_distribution.shard_type);
    println!("Number of shards: {}", hash_distribution.num_shards);
    
    // Demonstrate shard assignment for sample data
    let sample_users = vec![
        ("1", "John Doe"),
        ("2", "Jane Smith"),
        ("3", "Bob Johnson"),
        ("4", "Alice Brown"),
        ("5", "Charlie Wilson"),
    ];
    
    println!("\nShard assignments for sample users:");
    for (user_id, name) in sample_users {
        let mut row_data = HashMap::new();
        row_data.insert("user_id".to_string(), user_id.to_string());
        
        if let Ok(shard_id) = hash_distribution.determine_shard_id(&row_data) {
            println!("  User {} ({}) -> {}", user_id, name, shard_id);
        }
    }

    // Example 2: Range-based sharding
    println!("\n2. Range-based Sharding:");
    let mut range_distribution = ShardDistribution::range_based(
        "orders".to_string(),
        vec!["order_id".to_string()],
        3,
    );
    
    // Add range parameters
    range_distribution.add_parameter("ranges".to_string(), "0-10,11-20,21-30".to_string());
    
    println!("Distribution: {}", range_distribution);
    println!("Shard type: {}", range_distribution.shard_type);
    
    // Demonstrate shard assignment for sample orders
    let sample_orders = vec![
        ("1", "Order 1"),
        ("5", "Order 5"),
        ("10", "Order 10"),
        ("15", "Order 15"),
        ("25", "Order 25"),
    ];
    
    println!("\nShard assignments for sample orders:");
    for (order_id, description) in sample_orders {
        let mut row_data = HashMap::new();
        row_data.insert("order_id".to_string(), order_id.to_string());
        
        if let Ok(shard_id) = range_distribution.determine_shard_id(&row_data) {
            println!("  Order {} ({}) -> {}", order_id, description, shard_id);
        }
    }

    // Example 3: Round-robin sharding
    println!("\n3. Round-robin Sharding:");
    let round_robin_distribution = ShardDistribution::round_robin(
        "products".to_string(),
        vec!["product_id".to_string()],
        3,
    );
    
    println!("Distribution: {}", round_robin_distribution);
    println!("Shard type: {}", round_robin_distribution.shard_type);
    
    // Demonstrate shard assignment for sample products
    let sample_products = vec![
        ("101", "Laptop"),
        ("102", "Smartphone"),
        ("103", "Headphones"),
        ("104", "Console"),
        ("105", "Chair"),
    ];
    
    println!("\nShard assignments for sample products:");
    for (product_id, name) in sample_products {
        let mut row_data = HashMap::new();
        row_data.insert("product_id".to_string(), product_id.to_string());
        
        if let Ok(shard_id) = round_robin_distribution.determine_shard_id(&row_data) {
            println!("  Product {} ({}) -> {}", product_id, name, shard_id);
        }
    }

    // Example 4: Shard information
    println!("\n4. Shard Information:");
    let shard_info = ShardInfo::new(
        "users_shard_0".to_string(),
        "worker1".to_string(),
        "users".to_string(),
        ShardType::HashBased,
    );
    
    shard_info.update_row_count(1000);
    shard_info.update_size_bytes(1024 * 1024); // 1MB
    
    println!("Shard info: {}", shard_info);
    println!("Row count: {}", shard_info.row_count);
    println!("Size: {} bytes", shard_info.size_bytes);
    println!("Status: {}", shard_info.status);
    println!("Is active: {}", shard_info.is_active());

    // Example 5: Shard keys
    println!("\n5. Shard Keys:");
    let shard_key = ShardKey::new(
        "user_id".to_string(),
        "123".to_string(),
        ShardDataType::Integer,
    );
    
    println!("Shard key: {}", shard_key);
    println!("Hash: {}", shard_key.calculate_hash());
    println!("Consistent hash (3 shards): {}", shard_key.calculate_consistent_hash(3));
    
    if let Ok(parsed_value) = shard_key.parse_value() {
        println!("Parsed value: {:?}", parsed_value);
    }

    // Example 6: Replication
    println!("\n6. Replication:");
    let mut replicated_distribution = ShardDistribution::hash_based(
        "critical_data".to_string(),
        vec!["id".to_string()],
        2,
    );
    replicated_distribution.with_replication(3);
    
    println!("Replicated distribution: {}", replicated_distribution);
    println!("Is replicated: {}", replicated_distribution.is_replicated);
    println!("Replication factor: {}", replicated_distribution.replication_factor);
    println!("Total shards (including replicas): {}", replicated_distribution.total_shards());

    println!("\n✅ Sharding demonstration completed!");
    println!("\nThis demonstration shows:");
    println!("- Hash-based sharding for even distribution");
    println!("- Range-based sharding for ordered data");
    println!("- Round-robin sharding for simple distribution");
    println!("- Shard metadata and management");
    println!("- Shard key operations");
    println!("- Replication strategies");

    Ok(())
}
