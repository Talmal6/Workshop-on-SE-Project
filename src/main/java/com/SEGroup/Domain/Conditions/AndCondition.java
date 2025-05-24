package com.SEGroup.Domain.Conditions;


import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.function.Predicate;

public // AndCondition implementation
class AndCondition extends CompositeCondition {

    public AndCondition(List<Condition> conditions, DiscountType type, double percent, String scopeKey, String coupon) {
        super(conditions, type, percent, scopeKey, coupon);
    }

    /**
     * AND logic: All conditions must be satisfied for the composite condition to be true.
     */
    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        // All conditions must be satisfied
        for (Condition condition : conditions) {
            if (!condition.isSatisfiedBy(products, amounts)) {
                return false; // If any condition fails, the whole AND fails
            }
        }
        return true; // All conditions passed
    }
}