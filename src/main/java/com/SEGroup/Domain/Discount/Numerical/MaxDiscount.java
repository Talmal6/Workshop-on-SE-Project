package com.SEGroup.Domain.Discount.Numerical;


import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.Map;

/**
 * A discount that returns the maximum value from a set of discounts.
 */
public class MaxDiscount extends NumericalComposite {
    public MaxDiscount(List<Discount> discounts) {
        super(discounts);
    }

    @Override
    public double calculate(ShoppingProduct product,int quantity) {
        double base = product.getPrice() * quantity;
        double maxDiscount = 0.0;
        for (Discount d : discounts) {
            double discountedTotal = d.calculate(product,quantity);
            double discountAmount = base - discountedTotal;
            if (discountAmount > maxDiscount) {
                maxDiscount = discountAmount;
            }
        }
        return maxDiscount;
    }

    public double calculateMaxDiscountForBasket(Map<ShoppingProduct, Integer> productsWithQuantities) {
        double maxDiscountOverall = 0.0;

        for (var entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();
            double discountAmount = calculate(product, quantity);
            if (discountAmount > maxDiscountOverall) {
                maxDiscountOverall = discountAmount;
            }
        }

        return maxDiscountOverall;
    }

}
