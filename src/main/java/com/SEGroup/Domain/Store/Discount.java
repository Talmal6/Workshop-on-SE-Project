package com.SEGroup.Domain.Store;

/**
 * Represents a discount applied to a product in a store.
 */
public class Discount {
    private final String description;

    public Discount(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
