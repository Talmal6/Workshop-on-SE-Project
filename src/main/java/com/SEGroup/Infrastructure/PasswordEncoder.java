package com.SEGroup.Infrastructure;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoder {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Method to encrypt a given password
    public String encrypt(String password) {
        return passwordEncoder.encode(password);
    }

    // Method to check if a password matches its hashed version
    public boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
