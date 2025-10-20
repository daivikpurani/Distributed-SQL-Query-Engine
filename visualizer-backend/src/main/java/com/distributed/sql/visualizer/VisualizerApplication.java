package com.distributed.sql.visualizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application for the visualizer backend
 */
@SpringBootApplication
@EnableScheduling
public class VisualizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualizerApplication.class, args);
    }
}
