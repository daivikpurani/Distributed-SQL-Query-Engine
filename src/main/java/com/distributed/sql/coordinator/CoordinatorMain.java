package com.distributed.sql.coordinator;

import com.distributed.sql.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Main class to start the coordinator server
 */
public class CoordinatorMain {
    public static void main(String[] args) {
        int port = 50051;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.exit(1);
            }
        }
        
        // Define worker addresses
        Map<String, String> workerAddresses = new HashMap<>();
        workerAddresses.put("worker1", "localhost:50052");
        workerAddresses.put("worker2", "localhost:50053");
        workerAddresses.put("worker3", "localhost:50054");
        
        Logger.info("Starting coordinator server on port: {}", port);
        Logger.info("Configured workers: {}", workerAddresses.keySet());
        
        try {
            CoordinatorServer server = new CoordinatorServer(port, workerAddresses);
            server.start();
            server.blockUntilShutdown();
        } catch (Exception e) {
            Logger.error("Failed to start coordinator server", e);
            System.exit(1);
        }
    }
}
