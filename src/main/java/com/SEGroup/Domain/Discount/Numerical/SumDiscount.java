package com.SEGroup.Domain.Discount.Numerical;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

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

}
