package com.SEGroup.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.CreditCardDTO;
import com.SEGroup.DTO.UserSuspensionDTO;
import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Repositories.GuestRepository;
import com.SEGroup.Domain.Report.Report;
import com.SEGroup.Domain.Report.ReportCenter;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService handles user-related operations such as login, registration, cart
 * management, and user deletion.
 * It interacts with the authentication service, user repository, and guest
 * service.
 */
@Service
public class UserService {

    private final GuestService guestService;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private PasswordEncoder passwordEncoder;
    private final ReportCenter reportCenter;

    /**
     * Constructs a new UserService instance with the provided dependencies.
     *
     * @param guestService          The service responsible for managing guest
     *                              sessions.
     * @param userRepository        The repository for managing user data.
     * @param authenticationService The service responsible for user authentication.
     */
    public UserService(GuestService guestService, IUserRepository userRepository,
            IAuthenticationService authenticationService, ReportCenter reportCenter) {
        this.guestService = guestService;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.reportCenter = reportCenter;
        passwordEncoder = new PasswordEncoder();

    }

    /**
     * Creates a guest session.
     * Logs the creation of the guest session.
     *
     * @return A Result object containing the guest session ID if successful, or an
     *         error message.
     */
    @Transactional
    public Result<String> guestLogin() {
        LoggerWrapper.info("Creating a guest session."); // Log the creation of guest session
        return guestService.createGuestSession();
    }

    /**
     * Registers a new user with the provided details.
     * Logs the registration process.
     *
     * @param username The username of the user to register.
     * @param email    The email of the user to register.
     * @param password The password of the user to register.
     * @return A Result object indicating success or failure of the registration.
     */

    @Transactional
    public Result<Void> register(String username, String email, String password, AddressDTO address) {
        try {
            userRepository.addUserWithaddress(username, email, passwordEncoder.encrypt(password), address);
            LoggerWrapper.info("User registered successfully: " + username + ", Email: " + email); // Log successful
            // registration
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to register user: " + e.getMessage(), e); // Log error on failure
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> register(String username, String email, String password) {
        try {
            userRepository.addUser(username, email, passwordEncoder.encrypt(password));
            LoggerWrapper.info("User registered successfully: " + username + ", Email: " + email); // Log successful
                                                                                                   // registration
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to register user: " + e.getMessage(), e); // Log error on failure
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    /**
     * Logs in a user with the provided email and password.
     * Logs the login process.
     *
     * @param email    The email of the user trying to login.
     * @param password The password of the user trying to login.
     * @return A Result object containing the session key if successful, or an error
     *         message.
     */
    @Transactional
    public Result<String> login(String email, String password) {
        try {
            User user = userRepository.findUserByEmail(email);
            authenticationService.matchPassword(user.getPassword(), password);
            String sessionKey = authenticationService.authenticate(email);
            LoggerWrapper.info("User logged in successfully: " + email); // Log successful login
            return Result.success(sessionKey);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to login user: " + e.getMessage(), e); // Log error on failure
            return Result.failure("Failed to login ");
        }
    }

    /**
     * Logs out a user by invalidating their session key.
     * Logs the logout process.
     *
     * @param sessionKey The session key of the user to log out.
     * @return A Result object indicating success or failure of the logout
     *         operation.
     */
    @Transactional
    public Result<Void> logout(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            authenticationService.invalidateSession(sessionKey);
            LoggerWrapper.info("User logged out successfully. Session key: " + sessionKey); // Log successful logout
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to logout: " + e.getMessage(), e); // Log error on failure
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
    @Transactional
    public Result<Void> deleteUser(String AuthKey, String email) {
        try {
            if (!userRepository.userIsAdmin(authenticationService.getUserBySession(AuthKey))) {
                LoggerWrapper.info("User is not an admin: " + email); // Log user not admin scenario
                return Result.failure("User is not an admin: " + email);
            }
            User user = userRepository.findUserByEmail(email);
            if (user == null) {
                LoggerWrapper.info("User not found: " + email); // Log user not found scenario
                return Result.failure("User not found: " + email);
            }
            userRepository.deleteUser(email);
            LoggerWrapper.info("User deleted successfully: " + email); // Log successful deletion
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to delete user: " + e.getMessage(), e); // Log error on failure
            return Result.failure("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Adds a product to the user's cart.
     * Logs the addition of the product to the cart.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param email      The email of the user.
     * @param productID  The ID of the product to add to the cart.
     * @param storeName  The name of the store where the product is located.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> addToUserCart(String sessionKey, String email, String productID, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            userRepository.addToCart(email, storeName, productID); // Add product to user's cart

            LoggerWrapper.info("Added product to user cart: " + email + ", Product ID: " + productID); // Log product
                                                                                                       // addition to
                                                                                                       // cart
            return Result.success("Add item to cart successfully!");
        } catch (Exception e) {
            String errorMessage = "Failed to add item to cart: " + e.getMessage();
            LoggerWrapper.error(errorMessage, e); // Log error on failure
            return Result.failure(errorMessage);
        }
    }

    /**
     * Adds a product to the guest's cart.
     * Logs the addition of the product to the guest's cart.
     *
     * @param guestToken The token of the guest session.
     * @param productId  The ID of the product to add to the cart.
     * @param storeName  The name of the store where the product is located.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> addToGuestCart(String guestToken, String productId, String storeName) {
        try {
            return guestService.addToCart(guestToken, storeName, productId); // Add product to guest's cart
        } catch (Exception e) {
            LoggerWrapper.error("Error adding item to guest cart: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Purchases the shopping cart for a user.
     * Logs the cart purchase process.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param email      The email of the user making the purchase.
     * @return A Result object indicating success or failure of the purchase
     *         operation.
     */
    @Transactional
    public Result<Void> purchaseShoppingCart(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkIfExist(email);

            // WILL ADD METHODS TO HERE

            LoggerWrapper.info("Purchased shopping cart for user: " + email); // Log cart purchase
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Failed to purchase shopping cart: " + e.getMessage(), e); // Log error on failure
            return Result.failure("Failed to purchase shopping cart: " + e.getMessage());
        }
    }

    /**
     * Removes a product from the user's cart.
     * Logs the removal of the product from the cart.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param email      The email of the user.
     * @param productID  The ID of the product to remove from the cart.
     * @param storeName  The name of the store where the product is located.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> removeFromUserCart(String sessionKey, String email, String productID, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);
            if (owner.startsWith("g-")) {
                // If guest, delegate to guestService
                return guestService.modifyCartQuantity(sessionKey, storeName, productID, 0);
            }
            userRepository.checkUserSuspension(owner);
            userRepository.modifyCartQuantity(email, productID, storeName, 0);
            LoggerWrapper.info("Removed product from user cart: " + email + ", Product ID: " + productID);
            return Result.success("Removed item from cart successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Error removing item from user cart: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Modifies the quantity of a product in the user's cart.
     * Logs the modification of the product quantity.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param email      The email of the user.
     * @param productID  The ID of the product to modify.
     * @param storeName  The name of the store where the product is located.
     * @param quantity   The new quantity of the product.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> modifyProductQuantityInCartItem(
            String sessionKey,
            String email,
            String productID,
            String storeName,
            int quantity) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);
            if (owner.startsWith("g-")) {
                // If guest, delegate to guestService
                return guestService.modifyCartQuantity(sessionKey, storeName, productID, quantity);
            }
            userRepository.checkUserSuspension(owner);
            userRepository.modifyCartQuantity(email, productID, storeName, quantity); // Modify product quantity in
                                                                                      // user's cart
            LoggerWrapper.info("Modified product quantity in cart: " + email + ", Product ID: " + productID
                    + ", New Quantity: " + quantity); // Log quantity modification
            return Result.success("Modified product quantity in cart successfully!");
        } catch (Exception e) {
            LoggerWrapper.error("Error modifying product quantity in cart: " + e.getMessage(), e); // Log error on
                                                                                                   // failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<BasketDTO>> getUserCart(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Retrieved user cart: " + email);
            return Result.success(userRepository.getUserCart(email));
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving user cart: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<BasketDTO>> getUserCart(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String email = authenticationService.getUserBySession(sessionKey);
            // Check if the user is a guest
            if (email.startsWith("g-")) {
                return guestService.cart(sessionKey); // Delegate to guestService for guest users
            }
            LoggerWrapper.info("Retrieved user cart: " + email);
            return Result.success(userRepository.getUserCart(email));
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving user cart: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<String> getUserName(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Retrieved user name: " + email);
            return Result.success(userRepository.getUserName(email));
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving user name: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional
    public Result<String> suspendUser(String sessionKey, String email, Integer duration, String reason) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.userIsAdmin(authenticationService.getUserBySession(sessionKey));

            userRepository.suspendUser(email, duration, reason);
            LoggerWrapper.info("User suspended: " + email + ", Duration: " + duration + ", Reason: " + reason); // Log
                                                                                                                // user
                                                                                                                // suspension
            return Result.success("User suspended successfully!");

        } catch (Exception e) {
            LoggerWrapper.error("Error suspending user: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional
    public Result<String> unsuspendUser(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.userIsAdmin(authenticationService.getUserBySession(sessionKey));
            userRepository.unsuspendUser(email);
            LoggerWrapper.info("User unsuspended: " + email); // Log user unsuspension
            return Result.success("User unsuspended successfully!");

        } catch (Exception e) {
            LoggerWrapper.error("Error unsuspending user: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional
    public Result<Void> setAsAdmin(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.setAsAdmin(authenticationService.getUserBySession(sessionKey), email);
            LoggerWrapper.info("User set as admin: " + email); // Log user admin status change
            return Result.success(null);

        } catch (Exception e) {
            LoggerWrapper.error("Error setting user as admin: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    @Transactional
    public Result<Void> removeAdmin(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.removeAdmin(authenticationService.getUserBySession(sessionKey), email);
            LoggerWrapper.info("User removed from admin: " + email); // Log user admin status removal
            return Result.success(null);

        } catch (Exception e) {
            LoggerWrapper.error("Error removing user from admin: " + e.getMessage(), e); // Log error on failure
            return Result.failure(e.getMessage());
        }
    }

    // merge crazy
    /**
     * Adds a product to the cart for both users and guests.
     *
     * @param sessionKey The session key
     * @param productId  The product ID
     * @param storeName  The store name
     * @return Result object with success or failure
     */
    @Transactional
    public Result<String> addToCart(String sessionKey,
            String productId,
            String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);
            if (owner.startsWith("g-")) {
                return guestService.addToCart(sessionKey, storeName, productId);
            } else {
                User user = userRepository.findUserByEmail(owner);
                userRepository.addToCart(user.getEmail(), storeName, productId);
            }
            return Result.success("Added item to cart successfully!");
        } catch (Exception e) {
            return Result.failure("Cannot add to cart: " + e.getMessage());
        }
    }

    /**
     * Removes a product from the user's cart.
     * Logs the removal of the product from the cart.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param productId  The ID of the product to remove from the cart.
     * @param storeName  The name of the store where the product is located.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> removeFromCart(String sessionKey,
            String productId,
            String storeName) {
        return changeCartQuantity(sessionKey, productId, storeName, 0);
    }

    /**
     * Modifies the quantity of a product in the user's cart.
     * Logs the modification of the product quantity.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param productId  The ID of the product to modify.
     * @param storeName  The name of the store where the product is located.
     * @param newQty     The new quantity of the product.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<String> changeCartQuantity(String sessionKey,
            String productId,
            String storeName,
            int newQty) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);

            if (owner.startsWith("g-")) {
                return guestService.modifyCartQuantity(sessionKey, storeName, productId, newQty);
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
     * Checks if the user is an admin.
     *
     * @param sessionKey The session key of the user.
     * @return A Result object indicating whether the user is an admin or not.
     */
    @Transactional(readOnly = true)
    public Result<Boolean> isAdmin(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String Admin = authenticationService.getUserBySession(sessionKey);

            if (userRepository.userIsAdmin(Admin)) {
                return Result.success(true);
            } else {
                return Result.failure("User is not an admin");
            }
        } catch (Exception e) {
            return Result.failure("Error checking admin status: " + e.getMessage());
        }
    }

    // throw exception if the user is suspended
    @Transactional(readOnly = true)
    public Result<Void> isSuspended(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String owner = authenticationService.getUserBySession(sessionKey);

            userRepository.checkUserSuspension(owner);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error checking suspension status: " + e.getMessage());
        }
    }

    /**
     * Makes a system report.
     *
     * @param sessionKey    The session key of the user making the report.
     * @param reportContent The content of the report.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<Void> makeSystemReport(String sessionKey, String reportContent) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            String userId = authenticationService.getUserBySession(sessionKey);
            reportCenter.makeSystemReport(userId, reportContent);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error making system report: " + e.getMessage());
        }
    }

    /**
     * Makes a user report.
     *
     * @param sessionKey     The session key of the user making the report.
     * @param reportContent  The content of the report.
     * @param reportOnUserId The ID of the user being reported.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<Void> makeUserReport(String sessionKey, String reportContent, String reportOnUserId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            String userId = authenticationService.getUserBySession(sessionKey);
            reportCenter.makeUserReport(userId, reportContent, reportOnUserId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error making user report: " + e.getMessage());
        }
    }

    /**
     * Retrieves all reports.
     *
     * @param sessionKey The session key of the user requesting the reports.
     * @return A Result object containing a list of reports if successful, or an
     *         error message.
     */
    @Transactional(readOnly = true)
    public Result<List<Report>> getReports(String sessionKey) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            if (!userRepository.userIsAdmin(authenticationService.getUserBySession(sessionKey))) {
                return Result.failure("User is not an admin");
            }

            return Result.success(reportCenter.getReportIdToReport());
        } catch (Exception e) {
            return Result.failure("Error retrieving reports: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<Report> getReportById(String sessionKey, String reportId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            if (userRepository.userIsAdmin(authenticationService.getUserBySession(sessionKey))) {
                return Result.failure("User is not an admin");
            }

            return Result.success(reportCenter.getReportById(reportId));
        } catch (Exception e) {
            return Result.failure("Error retrieving report: " + e.getMessage());
        }
    }

    /**
     * Handles a report.
     *
     * @param sessionKey The session key of the user handling the report.
     * @param reportId   The ID of the report to handle.
     * @return A Result object indicating success or failure of the operation.
     */
    @Transactional
    public Result<Void> handleReport(String sessionKey, String reportId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            if (!userRepository.userIsAdmin(authenticationService.getUserBySession(sessionKey))) {
                return Result.failure("User is not an admin");
            }

            reportCenter.handleReport(reportId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error handling report: " + e.getMessage());
        }
    }

    /**
     * Retrieves all user emails.
     *
     * @return A list of all user emails.
     */
    @Transactional(readOnly = true)
    public List<String> allUsersEmails() {
        return userRepository.getAllEmails(); // or return List.of();
    }

    /**
     * Retrieves all user roles for a specific email.
     *
     * @param email The email of the user.
     * @return A set of roles associated with the user.
     */
    @Transactional(readOnly = true)
    public Set<Role> rolesOf(String email) {
        return userRepository.getGlobalRoles(email);
    }

    @Transactional(readOnly = true)
    public List<UserSuspensionDTO> allSuspensions() {
        return userRepository.getAllSuspendedUsers();
    }

    @Transactional(readOnly = true)
    public Result<AddressDTO> getUserAddress(String sessionKey, String email) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            AddressDTO address = userRepository.getAddress(email);
            return Result.success(address);
        } catch (Exception e) {
            return Result.failure("Error retrieving user address: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> setUserAddress(String sessionKey, AddressDTO address) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            userRepository.setAddress(authenticationService.getUserBySession(sessionKey), address);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error setting user address: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> setUserName(String sessionKey, String newName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            userRepository.setUserName(authenticationService.getUserBySession(sessionKey), newName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error setting user name: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<CreditCardDTO> getUserPaymentDetails(String token, String email) {
        try {
            authenticationService.checkSessionKey(token);
            return Result.success(userRepository.getCreditCard(email));
        } catch (Exception e) {
            return Result.failure("Error retrieving user payment details: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Void> setUserPaymentDetails(String token, String email,
            CreditCardDTO creditCardDetails, AddressDTO address) {
        try {
            authenticationService.checkSessionKey(token);
            userRepository.setCreditCard(authenticationService.getUserBySession(token), creditCardDetails, address);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Error setting user payment details: " + e.getMessage());
        }
    }


}