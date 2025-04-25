package com.SEGroup.Domain.Store;

public class BuyingPolicy {
    private int minimumQuantity;

    public BuyingPolicy(int minimumQuantity) {
        if (minimumQuantity < 1) {
            throw new IllegalArgumentException("Minimum quantity must be at least 1");
        }
        this.minimumQuantity = minimumQuantity;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

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
