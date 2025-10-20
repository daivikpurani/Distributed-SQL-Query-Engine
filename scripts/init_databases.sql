-- Initialize databases for distributed SQL engine workers
-- Run this script to create separate databases for each worker

-- Create databases for workers
CREATE DATABASE worker1_db;
CREATE DATABASE worker2_db;
CREATE DATABASE worker3_db;

-- Create a coordinator database for metadata
CREATE DATABASE coordinator_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE worker1_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE worker2_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE worker3_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE coordinator_db TO postgres;
