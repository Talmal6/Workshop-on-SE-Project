package com.SEGroup.Domain.Store;

import jakarta.persistence.Embeddable;

/**
 * Represents the buying policy of a store, including minimum quantity requirements.
 */
@Embeddable
public class BuyingPolicy {
    private int minimumQuantity;

    public BuyingPolicy(int minimumQuantity) {
        if (minimumQuantity < 1) {
            throw new IllegalArgumentException("Minimum quantity must be at least 1");
        }
        this.minimumQuantity = minimumQuantity;
    }
    protected BuyingPolicy() {
        this.minimumQuantity = 1; // ערך ברירת מחדל חוקי
    }
    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    /**
     * Sets the minimum quantity required for purchase.
     *
     * @param minimumQuantity The minimum quantity to set.
     * @throws IllegalArgumentException if the minimum quantity is less than 1.
     */
    public void setMinimumQuantity(int minimumQuantity) {
        if (minimumQuantity < 1) {
            throw new IllegalArgumentException("Minimum quantity must be at least 1");
        }
        this.minimumQuantity = minimumQuantity;
    }

    /**
     * האם מותר לבצע רכישה על פי הכללים?
     */
    public boolean isPurchaseAllowed(int requestedQuantity) {
        return requestedQuantity >= minimumQuantity;
    }

    @Override
    public String toString() {
        return "BuyingPolicy{minimumQuantity=" + minimumQuantity + '}';
    }
}
