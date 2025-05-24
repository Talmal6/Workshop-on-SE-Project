package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumDiscount extends NumericalComposite {

    public SumDiscount(List<Discount> discounts) {
        super(discounts);
    }

    @Override
    public double calculate(ShoppingProduct product, int quantity) {
        double base = product.getPrice() * quantity;
        double totalDiscount = 0.0;

        for (Discount d : discounts) {
            double discountedTotal = d.calculate(product, quantity);
            double discountAmount = base - discountedTotal;
            totalDiscount += discountAmount;
        }

        return totalDiscount;
    }


    @Override
    public Map<String, Double> calculateDiscountForBasket(Map<ShoppingProduct, Integer> productsWithQuantities) {
        Map<String, Double> result = new HashMap<>();

        // Handle empty basket
        if (productsWithQuantities == null || productsWithQuantities.isEmpty()) {
            return result;
        }

        // Calculate final price for each product after applying sum discounts
        for (var entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            double baseTotal = product.getPrice() * quantity;
            double discountAmount = calculate(product, quantity);

            // Don't allow discount to exceed the base price for this product
            double finalPrice = Math.max(0.0, baseTotal - Math.min(discountAmount, baseTotal));

            result.put(product.getProductId(), finalPrice);
        }

        return result;
    }
    public Map<ShoppingProduct, Double> calculateFinalPricesForBasket(Map<ShoppingProduct, Integer> productsWithQuantities) {
        Map<ShoppingProduct, Double> finalPrices = new HashMap<>();

        for (var entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            double baseTotal = product.getPrice() * quantity;
            double discountAmount = calculate(product, quantity);

            double finalPrice = baseTotal - discountAmount;
            finalPrices.put(product, finalPrice);
        }

        return finalPrices;
    }

}
