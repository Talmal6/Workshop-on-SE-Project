package com.SEGroup.Infrastructure;
import com.SEGroup.Domain.User.ShoppingCart;

import javax.naming.AuthenticationException;

public interface IAuthenticationService {
    // 5-adapter changed String authenticate(String email, String password) -> String authenticate(String email)
    String authenticate(String email);
    void checkSessionKey(String sessionKey) throws AuthenticationException;
    void invalidateSession(String sessionKey) throws AuthenticationException;

    // 5-adapter changed UserDTO getUserBySession(String sessionKey) -> string getUserBySession(String sessionKey) (gets email out of session key)
    String getUserBySession(String sessionKey) throws AuthenticationException;

    // 5-adapter add 2 password encryption functions
    String encryptPassword(String password);
    void matchPassword(String encryptedPassword, String userPassword) throws AuthenticationException;
}
