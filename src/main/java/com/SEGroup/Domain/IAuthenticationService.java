package com.SEGroup.Domain;

import com.SEGroup.Domain.User.ShoppingCart;

import javax.naming.AuthenticationException;

/**
 * Interface representing a service for authentication.
 * It provides methods for authenticating users, managing session keys,
 * and handling password encryption.
 */

public interface IAuthenticationService {

    /**
     * Authenticates a user based on their email address.
     *
     * @param email The email of the user to authenticate.
     * @return A session key if authentication is successful.
     */
    String authenticate(String email);

    /**
     * Checks the validity of a session key.
     *
     * @param sessionKey The session key to validate.
     * @throws AuthenticationException If the session key is invalid.
     */
    void checkSessionKey(String sessionKey) throws AuthenticationException;

    /**
     * Invalidates a session based on the provided session key.
     *
     * @param sessionKey The session key to invalidate.
     * @throws AuthenticationException If the session key is invalid or cannot be invalidated.
     */
    void invalidateSession(String sessionKey) throws AuthenticationException;

    /**
     * Retrieves the email address of the user associated with a session key.
     *
     * @param sessionKey The session key to retrieve the user's email from.
     * @return The email address of the user associated with the session key.
     * @throws AuthenticationException If the session key is invalid.
     */
    String getUserBySession(String sessionKey) throws AuthenticationException;

    /**
     * Encrypts a given password for secure storage.
     *
     * @param password The password to encrypt.
     * @return The encrypted password.
     */
    String encryptPassword(String password);

    /**
     * Compares the provided encrypted password with the user's input password.
     *
     * @param encryptedPassword The stored encrypted password.
     * @param userPassword The plain-text password provided by the user.
     * @throws AuthenticationException If the passwords do not match.
     */
    void matchPassword(String encryptedPassword, String userPassword) throws AuthenticationException;
}
