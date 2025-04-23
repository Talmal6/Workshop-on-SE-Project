package com.SEGroup.Infrastructure;
import java.util.List;

import com.SEGroup.Domain.ShoppingCart;
import com.SEGroup.Domain.UserDTO;

public interface IAuthenticationService {
    String authenticate(String email, String password);
    void checkSessionKey(String sessionKey);
    void invalidateSession(String sessionKey);
    UserDTO getUserBySession(String sessionKey);
    String createGuestSession();
    ShoppingCart guestCart(String token);
}
