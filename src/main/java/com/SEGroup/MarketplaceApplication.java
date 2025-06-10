// src/main/java/com/SEGroup/MarketplaceApplication.java
package com.SEGroup;

import com.SEGroup.Domain.*;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@SpringBootApplication
@EnableVaadin
//@EnableJpaRepositories("com.SEGroup.Infrastructure.Repositories.DataBaseRepositories")
public class MarketplaceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {

		// בדיקה מוקדמת של חיבור מסד נתונים
		if (!isDatabaseAvailable()) {
			System.err.println("❌ Database connection failed - Application will not start");
			System.exit(1);
		}
		System.out.println("✅ Database connection validated successfully");
		System.out.println("📚 Initializing Spring Boot application...");
		SpringApplication.run(MarketplaceApplication.class, args);
	}
	// הוסף מתוד לבדיקת חיבור
	private static boolean isDatabaseAvailable() {
		try {
			String url = "jdbc:h2:tcp://192.168.56.1:9092/~/data/prod-db";
			Connection conn = DriverManager.getConnection(url, "sa", "");
			conn.close();
			return true;
		} catch (SQLException e) {
			System.err.println("===========================================");
			System.err.println("DATABASE CONNECTION FAILED");
			System.err.println("Unable to connect to H2 database at 192.168.56.1:9092");
			System.err.println("Error: " + e.getMessage());
			System.err.println("Please ensure the H2 server is running and accessible");
			System.err.println("===========================================");
			return false;
		}
	}


}
