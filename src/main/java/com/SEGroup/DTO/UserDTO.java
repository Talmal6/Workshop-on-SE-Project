package com.SEGroup.DTO;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object representing a user.
 * It contains the user's email and encrypted password hash.
 * Implements {@link Serializable} to allow the object to be serialized.
 */
public final class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String email;         // The user's email address
    private final String passwordHash;  // The user's password hash (already encrypted)

    /**
     * Constructs a new UserDTO with the specified email and password hash.
     *
     * @param email The email of the user.
     * @param passwordHash The encrypted password hash of the user.
     * @throws NullPointerException if either email or passwordHash is null.
     */
    public UserDTO(String email, String passwordHash) {
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash cannot be null");
    }

    /**
     * Retrieves the email of the user.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the encrypted password hash of the user.
     *
     * @return The password hash of the user.
     */
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Compares this UserDTO to another object for equality.
     * Two UserDTO objects are considered equal if their email addresses are the same.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO dto)) return false;
        return email.equals(dto.email);
    }

    /**
     * Computes a hash code for this UserDTO based on the user's email.
     *
     * @return The hash code for this UserDTO.
     */
    @Override
    public int hashCode() {
        return email.hashCode();
    }

    /**
     * Returns a string representation of this UserDTO.
     *
     * @return A string representation of the UserDTO, showing only the email.
     */
    @Override
    public String toString() {
        return "UserDTO[" + email + "]";
    }
}
