package com.SEGroup.Domain.Discount;

import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;

import java.util.List;
import java.util.function.Predicate;

public class ConditionalDiscount extends Discount {
    private final Predicate<ShoppingProduct> predicate;

    public ConditionalDiscount(DiscountType type, double percent, Predicate<ShoppingProduct> predicate,String scopeKey,String Coupon) {
        super(type, percent,scopeKey,Coupon);
        this.predicate = predicate;
    }

    @Override
    public double calculate(ShoppingProduct product,int quantity) {
        if(getCoupon()!=null && !isActive()) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        if (!predicate.test(product)) {
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
}

