package com.SEGroup.Domain.Discount.Numerical;


import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;

import java.util.List;

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
}
