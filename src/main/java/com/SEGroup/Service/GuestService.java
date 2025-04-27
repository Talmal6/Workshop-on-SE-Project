package com.SEGroup.Service;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.LoggerWrapper;

/**
 * Service class for managing guest sessions and shopping carts.
 * Provides methods to create a guest session and retrieve a shopping cart.
 */
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
        return Result.success(guestId);
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
        auth.checkSessionKey(guestToken);

        String subject = auth.getUserBySession(guestToken);

        // Check if the token belongs to a guest
        if (!subject.startsWith("guest:")) {
            LoggerWrapper.warning("Token does not belong to a guest: " + guestToken);  // Logging if the token is not a guest token
            throw new IllegalArgumentException("token does not belong to a guest");
        }

        String guestId = subject.substring("guest:".length());
        ShoppingCart cart = guests.cartOf(guestId);

        LoggerWrapper.info("Retrieved shopping cart for guest with ID: " + guestId);  // Logging the successful retrieval of the shopping cart

        return cart;
    }
}
