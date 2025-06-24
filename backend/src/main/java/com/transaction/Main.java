package com.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Transaction Management System.
 * <p>
 * Spring Boot application entry point that configures and starts the application.
 * Uses component scanning to automatically detect and register Spring components.
 * 
 * @author Transaction Management System
 * @version 1.0
 */
@SpringBootApplication
public class Main {
    /**
     * Application entry point.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}