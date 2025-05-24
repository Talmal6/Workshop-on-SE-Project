package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;

import java.util.List;

public class XorCondition extends CompositeCondition {
    public XorCondition(List<Condition> conditions) {
        super(conditions);
    }

    @Override
    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
        int trueCount = 0;
        for (Condition c : conditions) {
            if (c.isSatisfiedBy(products, amounts)) trueCount++;
        }
        return trueCount == 1;
    }
}