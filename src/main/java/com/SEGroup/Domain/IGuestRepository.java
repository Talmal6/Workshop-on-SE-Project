package com.SEGroup.Domain;

import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;

/**
 * Interface for managing guest users in the system.
 * It provides methods to create new guests and retrieve their shopping carts.
 */
public interface IGuestRepository {

    /**
     * Creates a new guest user and returns the guest object.
     *
     * @return The created Guest object.
     */
    Guest create();
    /**
     * Retrieves the shopping cart of a guest user by their ID.
     *
     * @param guestId The ID of the guest user.
     * @return The ShoppingCart object associated with the guest.
\     */
    ShoppingCart cartOf(String guestId);

}
