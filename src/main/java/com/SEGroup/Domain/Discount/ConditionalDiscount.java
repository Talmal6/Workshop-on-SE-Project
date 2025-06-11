package com.SEGroup.Domain.Discount;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public class ConditionalDiscount extends Discount {
    private final int minPrice;
    private final int minAmount;
    private final int maxAmount;

    public ConditionalDiscount(DiscountType type, double percent,int minPrice,int minAmount, int maxAmount,String scopeKey,String Coupon) {
        super(type, percent,scopeKey,Coupon);
        this.minPrice = minPrice;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.setActive(false);
    }
    public ConditionalDiscount(DiscountType type, double percent, int minPrice, int minAmount, String scopeKey, String coupon) {
        this(type, percent, minPrice, minAmount, Integer.MAX_VALUE, scopeKey, coupon);
    }

    @Override
    public double calculate(ShoppingProduct product,int quantity) {
        if(getCoupon()!=null && !isActive() ) {
            return product.getPrice() * quantity;
        }
        if(!isActive() || quantity < minAmount || quantity > maxAmount) {
            return product.getPrice() * quantity;
        }

        double baseTotal = product.getPrice() * quantity;
        switch (getType()) {
            case STORE:
                // always applies
                return baseTotal * (100 - getPercent()) / 100.0;
            case PRODUCT:
                // only if matching product ID
                if (product.getProductId().equals(super.getScopeKey())) {
                    return baseTotal * (100 - getPercent()) / 100.0;
                }
                break;
            case CATEGORY:
                // only if product belongs to category
                List<String> cats = product.getCategories();
                if (cats != null && cats.contains(super.getScopeKey())) {
                    return baseTotal * (100 - getPercent()) / 100.0;
                }
                break;
        }
        // no discount applies
        return baseTotal;
    }
    public void Activate(int price){
        if(this.minPrice <= price) {
            this.setActive(true);
        }
    }
    public void deActivate(){
        this.setActive(false);
    }
}

