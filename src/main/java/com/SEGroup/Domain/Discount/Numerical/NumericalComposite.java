package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.ConditionalDiscount;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.Map;

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
    public abstract double calculateDiscountForBasket(Map<ShoppingProduct, Integer> productsWithQuantities);
    public void add(Discount discount) {
        discounts.add(discount);
    }
    public void applyCoupon(String coupon) {
        for (Discount discount : discounts) {
            discount.ApplyCoupon(coupon);
        }
    }
    public void activateDiscount(int minprice){
        for (Discount discount : discounts) {
            if(discount instanceof ConditionalDiscount)
                ((ConditionalDiscount) discount).Activate(minprice);
        }
    }
    public void deactivateDiscount(){
        for (Discount discount : discounts) {
            if(discount instanceof ConditionalDiscount)
                ((ConditionalDiscount) discount).deActivate();
        }
    }
}