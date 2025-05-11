package com.SEGroup.Infrastructure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoder {
    // Use fixed strength parameter for consistency
    private static final BCryptPasswordEncoder SHARED_ENCODER = new BCryptPasswordEncoder(10);

    /**
     * Encrypts a given password using the BCrypt hashing algorithm.
     *
     * @param password The plain-text password to be encrypted.
     * @return The encrypted password.
     */
    public String encrypt(String password) {
        return SHARED_ENCODER.encode(password);
    }

    /**
     * Checks if a given plain-text password matches the hashed version.
     *
     * @param password The plain-text password to check.
     * @param hashedPassword The hashed password to compare against.
     * @return true if the plain-text password matches the hashed password, false otherwise.
     */
    public boolean checkPassword(String password, String hashedPassword) {
        try {
            return SHARED_ENCODER.matches(password, hashedPassword);
        } catch (Exception e) {
            // Handle potential format errors
            return false;
        }
    }
}