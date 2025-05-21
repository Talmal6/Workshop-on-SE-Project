package com.SEGroup.Domain.Discount;


import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

/**
 * Simple discount that always applies to the product's unit price.
 */
public class SimpleDiscount extends Discount {

    public SimpleDiscount(DiscountType type, double percent, String scopeKey,String Coupon) {
        super(type, percent,scopeKey,Coupon);
    }

    @Override
    public double calculate(ShoppingProduct product,int quantity) {
        if(getCoupon()!=null && !isActive()) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        double baseTotal = product.getPrice() * quantity;
        switch (getType()) {
            case STORE:
                // always applies
                return baseTotal * (100 - getPercent()) / 100.0;
            case PRODUCT:
                // only if matching product ID
                String skopekey=getScopeKey();
                if (product.getProductId().equals(getScopeKey())) {
                    return baseTotal * (100 - getPercent()) / 100.0;
                }
                break;
            case CATEGORY:
                // only if product belongs to category
                List<String> cats = product.getCategories();
                if (cats != null && cats.contains(getScopeKey())) {
                    return baseTotal * (100 - getPercent()) / 100.0;
                }
                break;
        }
        // no discount applies
        return baseTotal;
    }
}
