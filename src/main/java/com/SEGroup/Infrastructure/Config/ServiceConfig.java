package com.SEGroup.Infrastructure.Config;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Infrastructure.Repositories.GuestRepository;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.SecurityAdapter;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class ServiceConfig {

    @Bean
    public Security security() {
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        return security;
    }

    @Bean
    public IAuthenticationService authenticationService(Security security) {
        return new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
    }

    @Bean
    public IGuestRepository guestRepository() {
        return new GuestRepository();
    }

    @Bean
    public GuestService guestService(IGuestRepository guestRepository, IAuthenticationService authService) {
        return new GuestService(guestRepository, authService);
    }
} 