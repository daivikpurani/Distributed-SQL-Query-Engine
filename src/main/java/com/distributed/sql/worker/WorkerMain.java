package com.distributed.sql.worker;

import com.distributed.sql.utils.Logger;

/**
 * Main class to start a worker server
 */
public class WorkerMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: WorkerMain <workerId> <port> [dataDirectory]");
            System.exit(1);
        }
        
        String workerId = args[0];
        int port = 0; // Default value
        String dataDirectory = "data";
        
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1]);
            System.exit(1);
        }
        
        if (args.length > 2) {
            dataDirectory = args[2];
        }
        
        Logger.info("Starting worker server: {} on port: {}", workerId, port);
        Logger.info("Data directory: {}", dataDirectory);
        
        try {
            WorkerServer server = new WorkerServer(port, workerId, dataDirectory);
            server.start();
            server.blockUntilShutdown();
        } catch (Exception e) {
            Logger.error("Failed to start worker server: {}", workerId, e);
            System.exit(1);
        }
    }
}
