package com.SEGroup.Domain.Discount;

import com.SEGroup.Domain.Store.ShoppingProduct;

public abstract class Discount {
    /**
     * Level at which the discount is applied.
     */

    private final DiscountType type;
    private final double percent;
    private final String Coupon;
    private final String scopeKey; // storeName= null, category name, or productId
    private boolean isActive;

    protected Discount(DiscountType type, double percent, String scopeKey, String Coupon) {
        this.type = type;
        this.percent = percent;
        this.Coupon = Coupon;
        this.scopeKey = scopeKey;
        if (Coupon == null || Coupon == "" || Coupon.isEmpty()) {
            this.isActive = true;
        } else {
            this.isActive = false;
        }

    }

    public DiscountType getType() {
        return type;
    }

    public double getPercent() {
        return percent;
    }

    public String getCoupon() {
        return Coupon;
    }

    public String getScopeKey() {
        return scopeKey;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public void ApplyCoupon(String coupon) {
        if (coupon.equals(Coupon))
            this.isActive = true;
    }

    /**
     * Calculate discounted price for a product and quantity in the cart.
     */
    public abstract double calculate(ShoppingProduct product,int quantity);
}


