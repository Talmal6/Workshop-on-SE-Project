package com.SEGroup.Infrastructure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for encoding and verifying passwords using the BCrypt hashing algorithm.
 * It provides methods to encrypt passwords and check if a given password matches its hashed version.
 */
@Service
public class PasswordEncoder {

    // BCryptPasswordEncoder instance for hashing passwords
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Encrypts a given password using the BCrypt hashing algorithm.
     *
     * @param password The plain-text password to be encrypted.
     * @return The encrypted password.
     */
    public String encrypt(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Checks if a given plain-text password matches the hashed version.
     *
     * @param password The plain-text password to check.
     * @param hashedPassword The hashed password to compare against.
     * @return true if the plain-text password matches the hashed password, false otherwise.
     */
    public boolean checkPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }
}
