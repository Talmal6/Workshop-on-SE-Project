package com.SEGroup.Domain.Discount.Numerical;


import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.HashMap;
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
        return base - maxDiscount;
    }

    @Override
    public Map<String, Double> calculateDiscountForBasket(Map<ShoppingProduct, Integer> productsWithQuantities) {
        Map<String, Double> result = new HashMap<>();
        double maxDiscount = 0.0;
        ShoppingProduct maxDiscountProduct = null;

        // Step 1: Find the product with the maximum discount
        for (Map.Entry<ShoppingProduct, Integer> entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            double base = product.getPrice() * quantity;
            double discounted = calculate(product, quantity);
            double discountAmount = base - discounted;

            if (discountAmount > maxDiscount) {
                maxDiscount = discountAmount;
                maxDiscountProduct = product;
            }
        }

        // Step 2: Build the result map
        for (Map.Entry<ShoppingProduct, Integer> entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            if (product.equals(maxDiscountProduct)) {
                double discountedTotal = calculate(product, quantity); // apply discount
                result.put(product.getProductId(), discountedTotal);
            } else {
                double baseTotal = product.getPrice() * quantity;
                result.put(product.getProductId(), baseTotal); // no discount
            }
        }

        return result;
    }

}
