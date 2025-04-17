package com.SEGroup.Infrastructure;
import java.util.List;

public interface IAuthenticationService {

    String authenticate(String email, String password);
    boolean register(String email, String password, String emailAddress);
    boolean resetPassword(String email, String newPassword);
    List<String> getUserRoles(String email);
    boolean isUserAuthenticated(String email);
    void logout(String email);
    boolean checkSessionKey(String sessionKey);
}
