package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public class OrCondition extends CompositeCondition {

    public OrCondition(List<Condition> conditions, DiscountType type, double percent, String scopeKey, String coupon) {
        super(conditions, type, percent, scopeKey, coupon);
    }

    /**
     * OR logic: At least one condition must be satisfied for the composite condition to be true.
     */
    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        // At least one condition must be satisfied
        for (Condition condition : conditions) {
            if (condition.isSatisfiedBy(products, amounts)) {
                return true; // If any condition passes, the whole OR passes
            }
        }
        return false; // No conditions passed
    }
}