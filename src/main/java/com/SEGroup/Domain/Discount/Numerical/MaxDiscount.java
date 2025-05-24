package com.SEGroup.Domain.Discount.Numerical;


import com.SEGroup.Domain.Conditions.CompositeCondition;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<ShoppingProduct> allProducts = new ArrayList<>(productsWithQuantities.keySet());
        List<Integer> allQuantities = allProducts.stream()
                .map(productsWithQuantities::get)
                .collect(Collectors.toList());

        for (Map.Entry<ShoppingProduct, Integer> entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();

            double base = product.getPrice() * quantity;
            double bestDiscounted = base;

            for (Discount d : discounts) {
                double discounted = d instanceof CompositeCondition
                        ? ((CompositeCondition) d).calculateWithBasket(product, quantity, allProducts, allQuantities)
                        : d.calculate(product, quantity);

                double discountAmount = base - discounted;
                if (discountAmount > base - bestDiscounted) {
                    bestDiscounted = discounted;
                }
            }

            double discountAmount = base - bestDiscounted;
            if (discountAmount > maxDiscount) {
                maxDiscount = discountAmount;
                maxDiscountProduct = product;
            }
        }

        for (Map.Entry<ShoppingProduct, Integer> entry : productsWithQuantities.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();
            double baseTotal = product.getPrice() * quantity;

            if (product.equals(maxDiscountProduct)) {
                double finalPrice = 0.0;
                for (Discount d : discounts) {
                    double discounted = d instanceof CompositeCondition
                            ? ((CompositeCondition) d).calculateWithBasket(product, quantity, allProducts, allQuantities)
                            : d.calculate(product, quantity);

                    if (baseTotal - discounted == maxDiscount) {
                        finalPrice = discounted;
                        break;
                    }
                }
                result.put(product.getProductId(), finalPrice);
            } else {
                result.put(product.getProductId(), baseTotal);
            }
        }

        return result;
    }

}
