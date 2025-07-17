package org.grants.harvester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Grant Search Spring Boot application.
 * 
 * This class bootstraps the Spring application context and starts the embedded server.
 */
@SpringBootApplication // Marks this class as a Spring Boot application
@EnableScheduling   
public class GrantSearchApp {
    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GrantSearchApp.class, args);
    }
}
