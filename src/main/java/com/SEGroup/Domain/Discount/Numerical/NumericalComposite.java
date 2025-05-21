package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

/**
 * Abstract base class for numeric composite operations on discounts.
 */
public abstract class NumericalComposite {
    protected final List<Discount> discounts;

    protected NumericalComposite(List<Discount> discounts) {
        this.discounts = discounts;
    }

    /**
     * Calculate the numeric result for a given product (e.g., discount amount).
     * @param product the shopping product to evaluate
     * @return computed numeric result
     */
    public abstract double calculate(ShoppingProduct product,int quantity);
    public void add(Discount discount) {
        discounts.add(discount);
    }
    public void applyCoupon(String coupon) {
        for (Discount discount : discounts) {
            discount.ApplyCoupon(coupon);
        }
    }
}