package com.SEGroup.Domain.Store;
import jakarta.persistence.*;

/**
 * Represents a purchase policy for a store, including discount percentage and duration.
 */
@Embeddable
public class PurchasePolicy {
    private double discountPercentage;
    private int durationInDays;
    public PurchasePolicy(double discountPercentage, int durationInDays) {
        this.discountPercentage = discountPercentage;
        this.durationInDays = durationInDays;

    }
    protected PurchasePolicy() {}
    public double getDiscountPercentage() {
        return discountPercentage;
    }
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    public int getDurationInDays() {
        return durationInDays;
    }
    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }
}
