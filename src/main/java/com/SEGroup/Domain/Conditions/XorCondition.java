package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;
import java.util.function.Predicate;

public class XorCondition extends CompositeCondition {

    public XorCondition(List<Predicate<List<ShoppingProduct>>> conditions) {
        super(conditions);
    }

    @Override
    public boolean test(List<ShoppingProduct> products) {
        int trueCount = 0;
        for (Predicate<List<ShoppingProduct>> cond : conditions) {
            if (cond.test(products)) {
                trueCount++;
            }
        }
        return trueCount == 1;
    }
}
