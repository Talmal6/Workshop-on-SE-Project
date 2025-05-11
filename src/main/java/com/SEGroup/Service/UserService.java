package com.SEGroup.Service;

import java.util.List;
import java.util.Set;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * UserService handles user-related operations such as login, registration, cart management, and user deletion.
 * It interacts with the authentication service, user repository, and guest service.
 */
@Service
public class UserService {

    private final GuestService guestService;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserService instance with the provided dependencies.
     *
     * @param guestService The service responsible for managing guest sessions.
     * @param userRepository The repository for managing user data.
     * @param authenticationService The service responsible for user authentication.
     */
    @Autowired
    public UserService(GuestService guestService, IUserRepository userRepository,
                       IAuthenticationService authenticationService, PasswordEncoder passwordEncoder) {
        this.guestService = guestService;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder; // Use injected encoder instead of creating new one
    }
    // All the existing methods remain the same...

    /**
     * Creates a guest session.
     * Logs the creation of the guest session.
     *
     * @return A Result object containing the guest session ID if successful, or an error message.
     */
    public Result<String> guestLogin() {
        LoggerWrapper.info("Creating a guest session.");  // Log the creation of guest session
        return guestService.createGuestSession();
    }

    /**
     * Registers a new user with the provided details.
     * Logs the registration process.
     *
     * @param username The username of the user to register.
     * @param email The email of the user to register.
     * @param password The password of the user to register.
     * @return A Result object indicating success or failure of the registration.
     */
    public Result<Void> register(String username, String email, String password) {
        try {
            userRepository.addUser(username, email, password);
            LoggerWrapper.info("User registered successfully: " + username + ", Email: " + email);  // Log successful registration
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to register user: " + e.getMessage(), e);  // Log error on failure
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    /**
     * Logs in a user with the provided email and password.
     * Logs the login process.
     *
     * @param email The email of the user trying to login.
     * @param password The password of the user trying to login.
     * @return A Result object containing the session key if successful, or an error message.
     */
    public Result<String> login(String email, String password) {
        try {
            User user = userRepository.findUserByEmail(email);
            authenticationService.matchPassword(user.getPassword(), password);
            String sessionKey = authenticationService.authenticate(email);
            LoggerWrapper.info("User logged in successfully: " + email);  // Log successful login
            return Result.success(sessionKey);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to login user: " + e.getMessage(), e);  // Log error on failure
            return Result.failure("Failed to login ");
        }
    }

    /**
     * Logs out a user by invalidating their session key.
     * Logs the logout process.
     *
     * @param sessionKey The session key of the user to log out.
     * @return A Result object indicating success or failure of the logout operation.
     */
    public Result<Void> logout(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            authenticationService.invalidateSession(sessionKey);
            LoggerWrapper.info("User logged out successfully. Session key: " + sessionKey);  // Log successful logout
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to logout: " + e.getMessage(), e);  // Log error on failure
            return Result.failure("Failed to logout: " + e.getMessage());
        }
    }

    /**
     * Deletes a user based on the provided email.
     * Logs the user deletion process.
     *
     * @param email The email of the user to delete.
     * @return A Result object indicating success or failure of the deletion.
     */
    public Result<Void> deleteUser(String email) {
        try {
            User user = userRepository.findUserByEmail(email);
            if (user == null) {
                LoggerWrapper.info("User not found: " + email);  // Log user not found scenario
                return Result.failure("User not found: " + email);
            }
            userRepository.deleteUser(email);
            LoggerWrapper.info("User deleted successfully: " + email);  // Log successful deletion
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to delete user: " + e.getMessage(), e);  // Log error on failure
            return Result.failure("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Adds a product to the cart for both users and guests.
     *
     * @param sessionKey The session key
     * @param productId The product ID
     * @param storeName The store name
     * @return Result object with success or failure
     */
    public Result<String> addToCart(String sessionKey,
                                    String productId,
                                    String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);
            if (owner.startsWith("g-")) {
                ShoppingCart cart = guestService.cart(sessionKey);
                cart.add(storeName, productId, 1);
            } else {
                User user = userRepository.findUserByEmail(owner);
                user.addToCart(storeName, productId);
            }
            return Result.success("Added item to cart successfully!");
        } catch (Exception e) {
            return Result.failure("Cannot add to cart: " + e.getMessage());
        }
    }

    /**
     * Adds a product to the user's cart.
     *
     * @param sessionKey The session key for authentication
     * @param email The user's email
     * @param productID The product ID to add
     * @param storeName The store name
     * @return Result with success or error message
     */
    public Result<String> addToUserCart(String sessionKey, String email, String productID, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.addToCart(storeName, productID);
            LoggerWrapper.info("Added product to user cart: " + email + ", Product: " + productID);
            return Result.success("Added item to cart successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Failed to add to user cart: " + e.getMessage(), e);
            return Result.failure("Cannot add to cart: " + e.getMessage());
        }
    }

    /**
     * Adds a product to a guest's cart.
     *
     * @param guestToken The guest token
     * @param productId The product ID
     * @param storeName The store name
     * @return Result with success or error message
     */
    public Result<String> addToGuestCart(String guestToken, String productId, String storeName) {
        try {
            ShoppingCart cart = guestService.cart(guestToken);
            cart.add(storeName, productId, 1);
            LoggerWrapper.info("Added product to guest cart: " + guestToken + ", Product: " + productId);
            return Result.success("Added item to cart successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Failed to add to guest cart: " + e.getMessage(), e);
            return Result.failure("Cannot add to cart: " + e.getMessage());
        }
    }

    /**
     * Purchases the shopping cart for a user.
     * Logs the cart purchase process.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param email The email of the user making the purchase.
     * @return A Result object indicating success or failure of the purchase operation.
     */
    public Result<Void> purchaseShoppingCart(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkIfExist(email);

            // WILL ADD METHODS TO HERE

            LoggerWrapper.info("Purchased shopping cart for user: " + email);  // Log cart purchase
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to purchase shopping cart: " + e.getMessage(), e);  // Log error on failure
            return Result.failure("Failed to purchase shopping cart: " + e.getMessage());
        }
    }

    /**
     * Removes a product from the user's cart.
     * Logs the removal of the product from the cart.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param productId The ID of the product to remove from the cart.
     * @param storeName The name of the store where the product is located.
     * @return A Result object indicating success or failure of the operation.
     */
    public Result<String> removeFromCart(String sessionKey,
                                         String productId,
                                         String storeName) {
        return changeCartQuantity(sessionKey, productId, storeName, 0);
    }

    /**
     * Removes a product from a specific user's cart.
     *
     * @param sessionKey The session key for authentication
     * @param email The user's email
     * @param productID The product ID to remove
     * @param storeName The store name
     * @return Result with success or error message
     */
    public Result<String> removeFromUserCart(String sessionKey, String email, String productID, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.cart().changeQty(storeName, productID, 0);
            LoggerWrapper.info("Removed product from user cart: " + email + ", Product: " + productID);
            return Result.success("Product removed from cart successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Failed to remove from user cart: " + e.getMessage(), e);
            return Result.failure("Cannot remove from cart: " + e.getMessage());
        }
    }

    /**
     * Modifies the quantity of a product in the user's cart.
     * Logs the modification of the product quantity.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param productId The ID of the product to modify.
     * @param storeName The name of the store where the product is located.
     * @param newQty The new quantity of the product.
     * @return A Result object indicating success or failure of the operation.
     */
    public Result<String> changeCartQuantity(String sessionKey,
                                             String productId,
                                             String storeName,
                                             int newQty) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);

            if (owner.startsWith("g-")) {
                ShoppingCart cart = guestService.cart(sessionKey);
                cart.changeQty(storeName, productId, newQty);
            } else {
                User user = userRepository.findUserByEmail(owner);
                user.cart().changeQty(storeName, productId, newQty);
            }

            return Result.success("Cart updated");
        } catch (Exception e) {
            return Result.failure("Cannot update cart: " + e.getMessage());
        }
    }

    /**
     * Modifies the quantity of a product in a specific user's cart.
     *
     * @param sessionKey The session key for authentication
     * @param email The user's email
     * @param productID The product ID to modify
     * @param storeName The store name
     * @param quantity The new quantity
     * @return Result with success or error message
     */
    public Result<String> modifyProductQuantityInCartItem(String sessionKey, String email, String productID,
                                                          String storeName, int quantity) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            User user = userRepository.findUserByEmail(email);
            user.cart().changeQty(storeName, productID, quantity);
            LoggerWrapper.info("Modified product quantity in user cart: " + email + ", Product: " + productID + ", Quantity: " + quantity);
            return Result.success("Cart updated successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Failed to modify product quantity: " + e.getMessage(), e);
            return Result.failure("Cannot update cart: " + e.getMessage());
        }
    }

    public Result<List<BasketDTO>> getUserCart(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);

            // Get the session owner (could be a guest ID or email)
            String owner = authenticationService.getUserBySession(sessionKey);

            if (owner.startsWith("g-")) {
                // Guest user - get cart from guestService
                ShoppingCart guestCart = guestService.cart(sessionKey);
                return Result.success(guestCart.getBaskets());
            } else {
                // Registered user - get cart from userRepository
                return Result.success(userRepository.getUserCart(owner));
            }
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving user cart: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    public Result<String> getUserName(String sessionKey, String email){
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Retrieved user name: " + email);
            return Result.success(userRepository.getUserName(email));
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving user name: " + e.getMessage(), e);  // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    public List<String> allUsersEmails() {
        // TODO replace when repository supports it
        return userRepository.getAllEmails(); // or return List.of();
    }

    public Result<Void> suspendUser(String email, int days) {
        try { userRepository.suspend(email, days); return Result.success(null); }
        catch (Exception e) { return Result.failure(e.getMessage()); }
    }

    public Result<Void> unsuspendUser(String email) {
        try { userRepository.unsuspend(email); return Result.success(null); }
        catch (Exception e) { return Result.failure(e.getMessage()); }
    }

    public List<SuspensionDTO> allSuspensions() {
        return userRepository.getAllSuspensions();
    }

    public Set<Role> rolesOf(String email) {
        return userRepository.getGlobalRoles(email);
    }

    public record SuspensionDTO(String email, String since, String until) {
    }
}