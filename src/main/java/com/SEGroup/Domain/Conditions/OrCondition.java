package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public class OrCondition extends CompositeCondition {
    public OrCondition(List<Condition> conditions) {
        super(conditions);
    }

    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        for (Condition c : conditions) {
            if (c.isSatisfiedBy(products, amounts)) return true;
        }
        return false;
    }
}