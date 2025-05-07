package com.SEGroup.Domain.Store.Discount;

import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;

import java.util.List;

public class SumDiscount implements Discount{
    private final List<Discount> discounts;

    public SumDiscount(List<Discount> discounts) {
        this.discounts = discounts;
    }

    @Override
    public double calculate(StoreSearchEntry[] entries, InMemoryProductCatalog catalog) {
        double sum = 0.0;

        for (Discount discount : discounts) {
            sum += discount.calculate(entries, catalog);
        }

        return sum;
    }

    @Override
    public String getDescription() {
        return "Sum of discounts";
    }
}
