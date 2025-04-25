package com.SEGroup.DTO;

import java.io.Serializable;
import java.util.Objects;


public final class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String email;
    private final String passwordHash;   // already encrypted


    public UserDTO(String email, String passwordHash) {
        this.email        = Objects.requireNonNull(email,        "email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash cannot be null");
    }

    public String getEmail()    { return email; }
    public String getPassword() { return passwordHash; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO dto)) return false;
        return email.equals(dto.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return "UserDTO[" + email + "]";
    }
}
