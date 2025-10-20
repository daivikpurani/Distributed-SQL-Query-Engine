-- Load sample data for distributed SQL engine
-- This script populates the tables with realistic test data

-- Insert users (distributed across workers based on name hash)
INSERT INTO users (name, age, email, location) VALUES
-- Worker 1 (names A-M)
('Alice Johnson', 28, 'alice.johnson@email.com', 'New York'),
('Bob Smith', 35, 'bob.smith@email.com', 'California'),
('Charlie Brown', 42, 'charlie.brown@email.com', 'Texas'),
('Diana Prince', 29, 'diana.prince@email.com', 'Florida'),
('Eve Wilson', 31, 'eve.wilson@email.com', 'Washington'),
('Frank Miller', 45, 'frank.miller@email.com', 'Oregon'),
('Grace Lee', 26, 'grace.lee@email.com', 'Nevada'),
('Henry Davis', 38, 'henry.davis@email.com', 'Arizona'),
('Ivy Chen', 33, 'ivy.chen@email.com', 'Colorado'),
('Jack Taylor', 41, 'jack.taylor@email.com', 'Utah'),
('Kate Anderson', 27, 'kate.anderson@email.com', 'Montana'),
('Liam O\'Brien', 36, 'liam.obrien@email.com', 'Wyoming'),
('Maria Garcia', 30, 'maria.garcia@email.com', 'Idaho'),

-- Worker 2 (names N-Z)
('Nathan Kim', 34, 'nathan.kim@email.com', 'Alaska'),
('Olivia White', 25, 'olivia.white@email.com', 'Hawaii'),
('Paul Rodriguez', 39, 'paul.rodriguez@email.com', 'Maine'),
('Quinn Thompson', 32, 'quinn.thompson@email.com', 'Vermont'),
('Rachel Green', 28, 'rachel.green@email.com', 'New Hampshire'),
('Samuel Jones', 44, 'samuel.jones@email.com', 'Massachusetts'),
('Tina Patel', 37, 'tina.patel@email.com', 'Connecticut'),
('Uma Sharma', 29, 'uma.sharma@email.com', 'Rhode Island'),
('Victor Nguyen', 43, 'victor.nguyen@email.com', 'New Jersey'),
('Wendy Zhang', 31, 'wendy.zhang@email.com', 'Delaware'),
('Xavier Lopez', 40, 'xavier.lopez@email.com', 'Maryland'),
('Yara Singh', 26, 'yara.singh@email.com', 'Virginia'),
('Zoe Martinez', 35, 'zoe.martinez@email.com', 'West Virginia');

-- Insert products
INSERT INTO products (name, price, category, stock_quantity) VALUES
('Laptop Pro', 1299.99, 'Electronics', 50),
('Wireless Mouse', 29.99, 'Electronics', 200),
('Mechanical Keyboard', 89.99, 'Electronics', 150),
('Monitor 24"', 299.99, 'Electronics', 75),
('Headphones', 149.99, 'Electronics', 100),
('Coffee Maker', 79.99, 'Appliances', 80),
('Blender', 59.99, 'Appliances', 60),
('Microwave', 199.99, 'Appliances', 40),
('Running Shoes', 89.99, 'Sports', 120),
('Yoga Mat', 24.99, 'Sports', 200),
('Water Bottle', 12.99, 'Sports', 300),
('Backpack', 49.99, 'Accessories', 150),
('Watch', 199.99, 'Accessories', 90),
('Sunglasses', 79.99, 'Accessories', 110),
('Phone Case', 19.99, 'Accessories', 250);

-- Insert orders (distributed across workers)
INSERT INTO orders (user_id, product_name, amount, order_date, status) VALUES
-- Orders for users 1-13 (Worker 1)
(1, 'Laptop Pro', 1299.99, '2024-01-15', 'COMPLETED'),
(2, 'Wireless Mouse', 29.99, '2024-01-16', 'COMPLETED'),
(3, 'Mechanical Keyboard', 89.99, '2024-01-17', 'PENDING'),
(4, 'Monitor 24"', 299.99, '2024-01-18', 'COMPLETED'),
(5, 'Headphones', 149.99, '2024-01-19', 'COMPLETED'),
(6, 'Coffee Maker', 79.99, '2024-01-20', 'PENDING'),
(7, 'Blender', 59.99, '2024-01-21', 'COMPLETED'),
(8, 'Microwave', 199.99, '2024-01-22', 'COMPLETED'),
(9, 'Running Shoes', 89.99, '2024-01-23', 'PENDING'),
(10, 'Yoga Mat', 24.99, '2024-01-24', 'COMPLETED'),
(11, 'Water Bottle', 12.99, '2024-01-25', 'COMPLETED'),
(12, 'Backpack', 49.99, '2024-01-26', 'PENDING'),
(13, 'Watch', 199.99, '2024-01-27', 'COMPLETED'),

-- Orders for users 14-26 (Worker 2)
(14, 'Sunglasses', 79.99, '2024-01-28', 'COMPLETED'),
(15, 'Phone Case', 19.99, '2024-01-29', 'COMPLETED'),
(16, 'Laptop Pro', 1299.99, '2024-01-30', 'PENDING'),
(17, 'Wireless Mouse', 29.99, '2024-02-01', 'COMPLETED'),
(18, 'Mechanical Keyboard', 89.99, '2024-02-02', 'COMPLETED'),
(19, 'Monitor 24"', 299.99, '2024-02-03', 'PENDING'),
(20, 'Headphones', 149.99, '2024-02-04', 'COMPLETED'),
(21, 'Coffee Maker', 79.99, '2024-02-05', 'COMPLETED'),
(22, 'Blender', 59.99, '2024-02-06', 'PENDING'),
(23, 'Microwave', 199.99, '2024-02-07', 'COMPLETED'),
(24, 'Running Shoes', 89.99, '2024-02-08', 'COMPLETED'),
(25, 'Yoga Mat', 24.99, '2024-02-09', 'PENDING'),
(26, 'Water Bottle', 12.99, '2024-02-10', 'COMPLETED');

-- Additional orders for testing joins and aggregations
INSERT INTO orders (user_id, product_name, amount, order_date, status) VALUES
(1, 'Wireless Mouse', 29.99, '2024-02-11', 'COMPLETED'),
(2, 'Mechanical Keyboard', 89.99, '2024-02-12', 'COMPLETED'),
(3, 'Monitor 24"', 299.99, '2024-02-13', 'COMPLETED'),
(14, 'Backpack', 49.99, '2024-02-14', 'COMPLETED'),
(15, 'Watch', 199.99, '2024-02-15', 'COMPLETED'),
(16, 'Sunglasses', 79.99, '2024-02-16', 'COMPLETED');
