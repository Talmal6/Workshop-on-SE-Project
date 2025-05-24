package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public interface Condition {
    /**
     * Determines if the condition is satisfied by the given products and their quantities.
     *
     * @param products list of ShoppingProduct in the basket
     * @param amounts corresponding quantities of each product (same index)
     * @return true if condition is satisfied, false otherwise
     */
    boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts);
}
