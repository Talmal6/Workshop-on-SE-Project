package com.SEGroup.Domain.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a shopping cart that can contain multiple baskets, each associated with a different store.
 * The cart allows adding products to the baskets and changing their quantities.
 */
public class ShoppingCart {



    private final Map<String, Basket> storeToBasket=new ConcurrentHashMap<>();

    /**
     * Adds a product to the basket of a specific store.
     * If the store does not exist, a new basket is created.
     *
     * @param storeId   The ID of the store.
     * @param productId The ID of the product.
     * @param qty       The quantity to add.
     */
    public void add(String storeId, String productId, int qty) {
        storeToBasket
                .computeIfAbsent(storeId, Basket::new)
                .add(productId, qty);
    }

    /**
     * Changes the quantity of a product in the basket of a specific store.
     * If the quantity is 0, the product is removed from the basket.
     *
     * @param storeId   The ID of the store.
     * @param productId The ID of the product.
     * @param qty       The new quantity.
     */
    public void changeQty(String storeId, String productId, int qty) {
        Basket basket = storeToBasket.get(storeId);
        if (basket ==null) throw new IllegalArgumentException("basket not found");
        basket.change(productId, qty);
    }


    /**
     * Retrieves the current state of the shopping cart.
     * It returns a map of store IDs to their corresponding baskets.
     *
     * @return An unmodifiable map of store IDs to baskets.
     */
    public Map<String, Basket> snapShot(){
        return Collections.unmodifiableMap(storeToBasket);

    }

    /**
     * Clears all the baskets in the shopping cart.
     * This removes all products from all stores.
     */

    public void clear() {

        storeToBasket.values().forEach(Basket::clear);
        storeToBasket.clear();
    }
}
