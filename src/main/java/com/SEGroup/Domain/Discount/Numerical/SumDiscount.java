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
    public double calculateDiscountForBasket(Map<ShoppingProduct, Integer> productsWithQuantities) {
        double totalBase = productsWithQuantities.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();

        double totalDiscount = 0.0;
        for (var entry : productsWithQuantities.entrySet()) {
            totalDiscount += calculate(entry.getKey(), entry.getValue());
        }

        // don’t allow discount to exceed totalBase
        return Math.min(totalDiscount, totalBase);
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
