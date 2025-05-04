package com.SEGroup.Infrastructure;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Service class for managing JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting information from JWTs.
 */
@Service
public class Security {

    @Value("${jwt.secret}")
    private String secret;  // The secret key for signing JWTs
    private SecretKey key;  // The SecretKey object derived from the secret
    private final long expirationTime = 1000 * 60 * 60 * 24; // 24 hours expiration time for JWTs

    /**
     * Initializes the SecretKey using the configured secret.
     * This method is called automatically after the bean is constructed.
     */
    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for a given username.
     *
     * @param username The username to include as the subject of the token.
     * @return The generated JWT token.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // Set the subject (e.g. username)
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Set the issue time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))  // Set expiration time
                .signWith(key)  // Sign the token with the secret key
                .compact();  // Return the generated token
    }

    /**
     * Validates the JWT token's integrity and checks if it is expired.
     *
     * @param token The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);  // Parse the token
            return true;  // If no exceptions were thrown, the token is valid
        } catch (JwtException e) {
            return false;  // If the token is invalid or expired, return false
        }
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token The JWT token from which to extract the username.
     * @return The username stored in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);  // Extract the username (subject) from the token
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token The JWT token from which to extract the expiration date.
     * @return The expiration date of the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);  // Extract the expiration date from the token
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param <T> The type of the claim.
     * @param token The JWT token from which to extract the claim.
     * @param claimsResolver A function to extract the claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // Extract all claims from the token
        return claimsResolver.apply(claims);  // Apply the claimsResolver to extract the required claim
    }

    /**
     * Parses the JWT token and retrieves all claims.
     *
     * @param token The JWT token to parse.
     * @return The claims contained within the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)  // Set the signing key
                .build()
                .parseClaimsJws(token)  // Parse the token
                .getBody();  // Return the body (claims) of the token
    }

    /**
     * Marks the token as expired.
     * This method currently does not have an implementation.
     *
     * @param token The JWT token to expire.
     */
    public void makeTokenExpire(String token) {
        // Expiration logic (e.g., setting the expiration date in the past)
    }


    public void setKey(SecretKey key) {
        this.key = key;
    }
}
