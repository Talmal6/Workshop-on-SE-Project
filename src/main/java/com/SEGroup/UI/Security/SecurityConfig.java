package com.SEGroup.UI.Security;

import com.SEGroup.UI.Views.SignInView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/images/**", "/frontend/**", "/VAADIN/**", "/favicon.ico").permitAll()
                        .anyRequest().permitAll() // for now
                );
        return http.build();
    }
}
