package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Conditions.CompositeCondition;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (basket == null || basket.isEmpty()) {
            return result;
        }

        // Extract lists from the basket map
        List<ShoppingProduct> allProducts = new ArrayList<>(basket.keySet());
        List<Integer> allQuantities = allProducts.stream()
                .map(basket::get)
                .collect(Collectors.toList());

        for (Map.Entry<ShoppingProduct, Integer> entry : basket.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            double basePrice = product.getPrice() * quantity;
            double currentPrice = basePrice;

            for (Discount discount : discounts) {
                double discounted = discount instanceof CompositeCondition
                        ? ((CompositeCondition) discount).calculateWithBasket(product, quantity, allProducts, allQuantities)
                        : discount.calculate(product, quantity);

                double discountRatio = discounted / basePrice;
                currentPrice *= discountRatio;
            }

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
