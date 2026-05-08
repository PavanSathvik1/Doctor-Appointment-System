package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Hospital Management System (HMS) application.
 * <p>
 * A monolithic modular Spring Boot application providing doctor appointment
 * booking, prescription management, and patient care workflows.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmsApplication.class, args);
    }
}
