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





@SpringBootApplication
@EnableVaadin
//@EnableJpaRepositories("com.SEGroup.Infrastructure.Repositories.DataBaseRepositories")
public class MarketplaceApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {

		SpringApplication.run(MarketplaceApplication.class, args);
	}
}
