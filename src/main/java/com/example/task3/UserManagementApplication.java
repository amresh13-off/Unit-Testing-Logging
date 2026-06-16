package com.example.task3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserManagementApplication {

    private static final Logger log = LoggerFactory.getLogger(UserManagementApplication.class);

    public static void main(String[] args) {
        log.info("Starting User Management Application...");
        SpringApplication.run(UserManagementApplication.class, args);
        log.info("User Management Application started successfully");
    }
}
