package com.SEGroup.Service;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.ShoppingCart;
import org.springframework.stereotype.Service;

/**
 * Service class for managing guest sessions and shopping carts.
 * Provides methods to create a guest session and retrieve a shopping cart.
 */
@Service
public class GuestService {

    private final IGuestRepository guests;
    private final IAuthenticationService auth;

    /**
     * Constructs a new GuestService instance with the provided guest repository and authentication service.
     *
     * @param guests The guest repository used to create and manage guest data.
     * @param auth The authentication service used to validate guest tokens.
     */
    public GuestService(IGuestRepository guests, IAuthenticationService auth) {
        this.guests = guests;
        this.auth = auth;
    }

    /**
     * Creates a new guest session and returns the guest ID.
     * Logs the creation of a new guest session.
     *
     * @return A Result object containing the guest ID.
     */
    public Result<String> createGuestSession() {
        String guestId = guests.create().id();
        LoggerWrapper.info("Created a new guest session with ID: " + guestId);  // Logging the creation of a guest session
        String token  = auth.authenticate(guestId);  // Authenticating the guest session
        
        return Result.success(token);
    }

    /**
     * Retrieves the shopping cart for the given guest token.
     * Logs the retrieval of the shopping cart and any errors.
     *
     * @param guestToken The token associated with the guest session.
     * @return The shopping cart for the guest.
     * @throws Exception If the token is invalid or the token does not belong to a guest.
     */
    public ShoppingCart cart(String guestToken) throws Exception {
        LoggerWrapper.info("Retrieving cart for guest token: " + guestToken);  // Logging the request for a shopping cart
        String subject = auth.getUserBySession(guestToken);
        // Check if the token belongs to a guest
        if (!subject.startsWith("g-")) {
            LoggerWrapper.warning("Token does not belong to a guest: " + guestToken);  // Logging if the token is not a guest token
            throw new IllegalArgumentException("token does not belong to a guest");
        }

        ;
        ShoppingCart cart = guests.cartOf(subject);

        LoggerWrapper.info("Retrieved shopping cart for guest with ID: " + subject);  // Logging the successful retrieval of the shopping cart

        return cart;
    }


    //add to cart
    public void addToCart(String guestToken, String storeID, String productID) throws Exception {
        LoggerWrapper.info("Adding product to cart for guest token: " + guestToken);  // Logging the addition of a product to the cart
        String subject = auth.getUserBySession(guestToken);
        // Check if the token belongs to a guest
        if (!subject.startsWith("g-")) {
            LoggerWrapper.warning("Token does not belong to a guest: " + guestToken);  // Logging if the token is not a guest token
            throw new IllegalArgumentException("token does not belong to a guest");
        }

        guests.addToCart(subject, storeID, productID);
        LoggerWrapper.info("Added product with ID: " + productID + " to cart for guest with ID: " + subject);  // Logging the successful addition of the product
    }
}
