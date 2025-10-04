use std::collections::HashMap;
use tracing::{info, debug};

#[derive(Debug, Clone)]
pub struct Table {
    pub name: String,
    pub columns: Vec<String>,
    pub rows: Vec<Vec<String>>,
}

#[derive(Debug)]
pub struct DataStore {
    tables: HashMap<String, Table>,
}

impl DataStore {
    pub fn new() -> Self {
        let mut tables = HashMap::new();
        
        // Initialize with demo data
        tables.insert("users".to_string(), Table {
            name: "users".to_string(),
            columns: vec!["id".to_string(), "name".to_string(), "age".to_string(), "email".to_string()],
            rows: vec![
                vec!["1".to_string(), "John Doe".to_string(), "30".to_string(), "john@example.com".to_string()],
                vec!["2".to_string(), "Jane Smith".to_string(), "25".to_string(), "jane@example.com".to_string()],
                vec!["3".to_string(), "Bob Johnson".to_string(), "35".to_string(), "bob@example.com".to_string()],
            ],
        });

        tables.insert("orders".to_string(), Table {
            name: "orders".to_string(),
            columns: vec!["id".to_string(), "user_id".to_string(), "product".to_string(), "amount".to_string(), "date".to_string()],
            rows: vec![
                vec!["1".to_string(), "1".to_string(), "Laptop".to_string(), "999.99".to_string(), "2024-01-15".to_string()],
                vec!["2".to_string(), "2".to_string(), "Mouse".to_string(), "29.99".to_string(), "2024-01-16".to_string()],
                vec!["3".to_string(), "1".to_string(), "Keyboard".to_string(), "79.99".to_string(), "2024-01-17".to_string()],
            ],
        });

        tables.insert("products".to_string(), Table {
            name: "products".to_string(),
            columns: vec!["id".to_string(), "name".to_string(), "price".to_string(), "category".to_string()],
            rows: vec![
                vec!["1".to_string(), "Laptop".to_string(), "999.99".to_string(), "Electronics".to_string()],
                vec!["2".to_string(), "Mouse".to_string(), "29.99".to_string(), "Accessories".to_string()],
                vec!["3".to_string(), "Keyboard".to_string(), "79.99".to_string(), "Accessories".to_string()],
            ],
        });

        Self { tables }
    }

    pub fn get_table(&self, table_name: &str) -> Option<&Table> {
        self.tables.get(table_name)
    }

    pub fn get_all_tables(&self) -> Vec<&Table> {
        self.tables.values().collect()
    }

    pub fn get_table_count(&self) -> usize {
        self.tables.len()
    }

    pub fn get_total_rows(&self) -> usize {
        self.tables.values().map(|table| table.rows.len()).sum()
    }

    pub fn add_table(&mut self, table: Table) {
        self.tables.insert(table.name.clone(), table);
    }

    pub fn get_table_names(&self) -> Vec<String> {
        self.tables.keys().cloned().collect()
    }
}
