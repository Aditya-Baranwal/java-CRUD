package com.aditya.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class LMSApplication {

    private static final Logger log = LoggerFactory.getLogger(LMSApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(LMSApplication.class, args);
        String appName = context.getEnvironment().getProperty("application.name", "application");
        log.info("{} started successfully", appName);
    }
}
