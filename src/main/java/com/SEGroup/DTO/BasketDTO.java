package com.SEGroup.DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a shopping basket.
 * It contains the store ID and a map of product IDs to quantities.
 */
public record BasketDTO(String storeId, Map<String, Integer> prod2qty) {

    /**
     * Retrieves a list of product IDs in the basket,
     * where each product ID appears according to its quantity.
     *
     * @return A list of product IDs, where each product ID appears as many times as its quantity in the basket.
     */
    public List<String> getBasketProducts() {
        List<String> out = new ArrayList<>();
        // Populate the list with product IDs based on their quantities
        prod2qty.forEach((pid, q) -> out.addAll(java.util.Collections.nCopies(q, pid)));
        return out;
    }
    public String getStoreId() {
        return storeId;
    }
}
