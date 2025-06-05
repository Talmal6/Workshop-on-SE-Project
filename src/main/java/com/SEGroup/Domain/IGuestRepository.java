package com.SEGroup.Domain;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;

import java.util.List;

import org.springframework.context.annotation.Bean;

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
     */
    List<BasketDTO> cartOf(String guestId);



    void addToCart(String guestId, String storeID, String productID);

    void modifyCartQuantity(String guestId, String productID, String storeName, int quantity);
    ShoppingCart getShoppingCart(String guestId);

}
