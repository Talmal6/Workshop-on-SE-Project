package com.SEGroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation to make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */


@SpringBootApplication
// Remove the AppShellConfigurator implementation as it's already implemented in AppShellConfig
public class MarketplaceApplication extends SpringBootServletInitializer {

    /**
     * Main method, used to run the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApplication.class, args);
    }

}