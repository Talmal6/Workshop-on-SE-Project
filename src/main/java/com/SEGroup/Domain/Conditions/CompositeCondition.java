package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public abstract class CompositeCondition implements Condition {
    protected final List<Condition> conditions;

    public CompositeCondition(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions list cannot be null or empty");
        }
        this.conditions = conditions;
    }

    /**
     * Evaluate if the composite condition is satisfied by the given list of products and their amounts.
     * The concrete subclasses implement this according to their logic (AND, OR, XOR).
     *
     * @param products list of ShoppingProduct in the basket
     * @param amounts corresponding amounts per product, same indices as products
     * @return true if the composite condition is satisfied, false otherwise
     */
    @Override
    public abstract boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts);
}
