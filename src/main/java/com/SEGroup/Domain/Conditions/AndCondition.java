package com.SEGroup.Domain.Conditions;


import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.function.Predicate;

public class AndCondition extends CompositeCondition {

    public AndCondition(List<Predicate<List<ShoppingProduct>>> conditions) {
        super(conditions);
    }

    @Override
    public boolean test(List<ShoppingProduct> products) {
        for (Predicate<List<ShoppingProduct>> cond : conditions) {
            if (!cond.test(products)) {
                return false;
            }
        }
        return true;
    }
}
