package com.SEGroup.Service;

import java.util.UUID;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.PasswordEncoder;

public class UserService {


    private final GuestService guestService;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private PasswordEncoder passwordEncoder;

    public UserService(GuestService guestService, IUserRepository userRepository,
                       IAuthenticationService authenticationService) {
        this.guestService = guestService;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        passwordEncoder = new PasswordEncoder();

    }


    public Result<String> guestLogin() {
        return guestService.createGuestSession();
    }
    public Result<Void> register(String username, String email, String password) {
        try {
            userRepository.addUser(username, email, passwordEncoder.encrypt(password));
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    public Result<String> login(String email, String password) {
        try {
            User user = userRepository.findUserByEmail(email);
            authenticationService.matchPassword(user.getPassword(), passwordEncoder.encrypt(password));
            String sessionKey = authenticationService.authenticate(email);
            return Result.success(sessionKey);
        } catch (Exception e) {
            return Result.failure("Failed to login ");
        }
    }

    public Result<Void> logout(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            authenticationService.invalidateSession(sessionKey);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to logout: " + e.getMessage());
        }
    }

    public Result<Void> deleteUser(String email) {
        try {
            User user = userRepository.findUserByEmail(email);
            if (user == null) {
                return Result.failure("User not found: " + email);
            }
            userRepository.deleteUser(email);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to delete user: " + e.getMessage());
        }
    }


    public Result<String> addToUserCart(String sessionKey, String email, String productID,String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.addToCart(storeName,productID);
            return Result.success("Add item to cart succsesfully!");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<String> addToGuestCart(String guestTok,
                                         String productId, String store) {
        try {
            authenticationService.guestCart(guestTok).add(store, productId, 1);
            return Result.success("added");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> purchaseShoppingCart(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkIfExist(email);

            //WILL ADD METHODS TO HERE
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to purchase shopping cart: " + e.getMessage());
        }
    }

    public Result<String> removeFromUserCart(String sessionKey, String email, String productID, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.removeFromCart(storeName, productID);
            return Result.success("Removed item from cart successfully!");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<String> modifyProductQuantityInCartItem(
            String sessionKey,
            String email,
            String productID,
            String storeName,
            int quantity) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.cart().changeQty(storeName, productID, quantity);
            return Result.success("Modified product quantity in cart successfully!");
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

}
