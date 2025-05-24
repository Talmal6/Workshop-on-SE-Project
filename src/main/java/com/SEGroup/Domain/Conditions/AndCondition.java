package com.SEGroup.Domain.Conditions;


import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.function.Predicate;

public class AndCondition extends CompositeCondition {
    public AndCondition(List<Condition> conditions) {
        super(conditions);
    }

    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        for (Condition c : conditions) {
            if (!c.isSatisfiedBy(products, amounts)) return false;
        }
        return true;
    }
}