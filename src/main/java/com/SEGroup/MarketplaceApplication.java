// src/main/java/com/SEGroup/MarketplaceApplication.java
package com.SEGroup;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableVaadin
public class MarketplaceApplication {

	public static void main(String[] args) {

		SpringApplication.run(MarketplaceApplication.class, args);
	}
}
