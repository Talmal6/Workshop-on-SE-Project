package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

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
    public double calculateDiscountForBasket(Map<ShoppingProduct, Integer> basket) {
        // Calculate total price before discount
        double totalBefore = 0.0;
        for (Map.Entry<ShoppingProduct, Integer> entry : basket.entrySet()) {
            ShoppingProduct product = entry.getKey();
            int quantity = entry.getValue();
            totalBefore += product.getPrice() * quantity;
        }

        if (totalBefore == 0) return 0.0;

        double ratio = 1.0;

        // For each discount, calculate total discount amount on the basket,
        // compute fraction of amount left after this discount,
        // multiply ratios for sequential application.
        for (Discount d : discounts) {
            double discountValue = 0.0;
            for (Map.Entry<ShoppingProduct, Integer> entry : basket.entrySet()) {
                ShoppingProduct product = entry.getKey();
                int quantity = entry.getValue();
                discountValue += product.getPrice() * quantity - d.calculate(product, quantity);
            }
            double fraction = 1.0 - (discountValue / totalBefore);
            ratio *= fraction;
        }

        double totalAfter = totalBefore * ratio;
        return totalAfter;
    }

    @Override
    public double calculate(ShoppingProduct product, int quantity) {
        // Implementing this method is optional here, or throw UnsupportedOperationException,
        // because the real usage for sequential discount is on baskets, not individual products.
        throw new UnsupportedOperationException("Use calculateSequentialDiscountForBasket instead");
    }
}
