package com.SEGroup.Infrastructure.Endpoints;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.UserService;
import com.vaadin.hilla.EndpointExposed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.Nonnull;        // for @NonNull below
import java.util.List;

@EndpointExposed
@PermitAll                               // ✔ expose to every logged‑in user; tighten later
public class UserEndpoint {

    private final UserService userService;

    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }

    /* ---------- authentication ---------- */

    @Nonnull
    public String guestLogin() {
        return unwrap(userService.guestLogin());
    }

    public void register(String username, String email, String password) {
        unwrap(userService.register(username, email, password));
    }

    @Nonnull
    public String login(String email, String password) {
        return unwrap(userService.login(email, password));
    }

    public void logout(String sessionKey) {
        unwrap(userService.logout(sessionKey));
    }

//    public void deleteUser(String email) {       // admin operation?
//        unwrap(userService.deleteUser(email));
//    }

    /* ---------- cart operations ---------- */

    @Nonnull
    public String addToUserCart(String sessionKey, String email,
                                String productId, String storeName) {
        return unwrap(userService.addToUserCart(sessionKey, email, productId, storeName));
    }

    @Nonnull
    public String addToGuestCart(String guestToken, String productId, String storeName) {
        return unwrap(userService.addToGuestCart(guestToken, productId, storeName));
    }

    public void purchaseCart(String sessionKey, String email) {
        unwrap(userService.purchaseShoppingCart(sessionKey, email));
    }

    @Nonnull
    public String removeFromCart(String sessionKey, String email,
                                 String productId, String storeName) {
        return unwrap(userService.removeFromUserCart(sessionKey, email, productId, storeName));
    }

    @Nonnull
    public String modifyQty(String sessionKey, String email,
                            String productId, String storeName, int quantity) {
        return unwrap(userService.modifyProductQuantityInCartItem(
                sessionKey, email, productId, storeName, quantity));
    }

    @Nonnull
    public List<BasketDTO> getUserCart(String sessionKey, String email) {
        return unwrap(userService.getUserCart(sessionKey, email));
    }

    /* ---------- shared helper ---------- */

    private static <T> T unwrap(Result<T> r) {
        if (r.isSuccess()) {
            return r.getData();           // may be null when T == Void
        }
        /* Hilla maps RuntimeException → HTTP 400 by default.
           Swap to custom exception or ResponseStatusException if preferred. */
        throw new RuntimeException(r.getErrorMessage());
    }
}
