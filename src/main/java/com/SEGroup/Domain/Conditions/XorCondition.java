package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public class XorCondition extends CompositeCondition {

    public XorCondition(List<Condition> conditions, DiscountType type, double percent, String scopeKey, String coupon) {
        super(conditions, type, percent, scopeKey, coupon);
    }

    /**
     * XOR logic: Exactly one condition must be satisfied for the composite condition to be true.
     * If zero conditions or more than one condition is satisfied, returns false.
     */
    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        int satisfiedCount = 0;

        // Count how many conditions are satisfied
        for (Condition condition : conditions) {
            if (condition.isSatisfiedBy(products, amounts)) {
                satisfiedCount++;
                // Early exit optimization: if more than one is satisfied, XOR fails
                if (satisfiedCount > 1) {
                    return false;
                }
            }
        }

        // XOR is true only when exactly one condition is satisfied
        return satisfiedCount == 1;
    }
}