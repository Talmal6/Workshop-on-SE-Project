package com.SEGroup.Domain.Discount;

import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;

import java.util.List;
import java.util.function.Predicate;

public class ConditionalDiscount extends Discount {
    private final int minPrice;
    private final int minAmount;

    public ConditionalDiscount(DiscountType type, double percent,int minPrice,int minAmount,String scopeKey,String Coupon) {
        super(type, percent,scopeKey,Coupon);
        this.minPrice = minPrice;
        this.minAmount = minAmount;
        this.setActive(false);
    }

    @Override
    public double calculate(ShoppingProduct product,int quantity) {
        if(getCoupon()!=null && !isActive() ) {
            return product.getPrice() * quantity;
        }
        if(!isActive() || quantity < minAmount) {
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

