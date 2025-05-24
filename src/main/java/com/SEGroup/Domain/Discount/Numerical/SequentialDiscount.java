package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequentialDiscount extends NumericalComposite {

    public SequentialDiscount(List<Discount> discounts) {
        super(discounts);
    }

    /**
     * Calculate the total sequential discount amount for a basket of products.
     *
     * @param basket Map of ShoppingProduct to quantity purchased.
     * @return The total discount amount applied sequentially.
     */
    @Override
    public Map<String, Double> calculateDiscountForBasket(Map<ShoppingProduct, Integer> basket) {
        Map<String, Double> result = new HashMap<>();

        // Handle empty basket
        if (basket == null || basket.isEmpty()) {
            return result;
        }

        // Calculate discounted price for each product
        for (Map.Entry<ShoppingProduct, Integer> entry : basket.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            // Start with original total price for this product
            double currentPrice = product.getPrice() * quantity;

            // Apply each discount sequentially to this specific product
            for (Discount discount : discounts) {
                // Calculate what the discount saves on the current price
                double originalProductTotal = product.getPrice() * quantity;
                double discountSavings = originalProductTotal - discount.calculate(product, quantity);

                // Apply the discount percentage to current price
                double discountRatio = discountSavings / originalProductTotal;
                currentPrice = currentPrice * (1.0 - discountRatio);
            }

            // Store the final price for this product
            result.put(product.getProductId(), currentPrice);
        }

        return result;
    }

    @Override
    public double calculate(ShoppingProduct product, int quantity) {
        // Implementing this method is optional here, or throw UnsupportedOperationException,
        // because the real usage for sequential discount is on baskets, not individual products.
        throw new UnsupportedOperationException("Use calculateSequentialDiscountForBasket instead");
    }
}
