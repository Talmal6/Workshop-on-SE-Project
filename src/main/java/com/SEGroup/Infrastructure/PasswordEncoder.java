package com.SEGroup.Infrastructure;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoder {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String SECRET_KEY = "secret";

    // Method to encrypt a given password
    public static String encrypt(String password) {
        return passwordEncoder.encode(password);
    }

    // Method to check if a password matches its hashed version
    public static boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
