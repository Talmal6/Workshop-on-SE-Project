package com.SEGroup.Domain.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a shopping basket for a specific store.
 * The basket contains products and their quantities.
 */
public class Basket {


    private final String storeId;
    private final Map<String, Integer> product2qty = new ConcurrentHashMap<>();

    public Basket(String storeId) {

        this.storeId = storeId;

    }


    /**
     * Adds a product to the basket or updates its quantity if it already exists.
     *
     * @param pid The product ID.
     * @param q   The quantity to add.
     */
    public void add(String pid, int q) {
        product2qty.merge(pid, q, Integer::sum);  // im adding more to quantitiy of pid

    }
    /**
     * Changes the quantity of a product in the basket.
     * If the quantity is 0, the product is removed from the basket.
     * @param pid The product ID.
     * @param q   The new quantity.
     */
    public void change(String pid, int q) {

        product2qty.put(pid, q);

    }
    /**
     * Retrieves the current Basket list.
     * @return A map of product IDs to their quantities.
     */
    public Map<String,Integer> snapshot() {  // to see the current Basket list

        return Collections.unmodifiableMap(product2qty);

    }
    public String storeId() {

        return storeId;
    }
    public void clear() {

        product2qty.clear();
    }


}
