package com.english12smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ========== SPRING BOOT APPLICATION ==========
 * Main entry point cho English 12 Smart Application
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class English12SmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(English12SmartApplication.class, args);
    }
}
