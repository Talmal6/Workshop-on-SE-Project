package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Store.ShoppingProduct;
import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract composite condition, holds a list of child conditions.
 */
public abstract class CompositeCondition implements Predicate<List<ShoppingProduct>> {
    protected final List<Predicate<List<ShoppingProduct>>> conditions;

    public CompositeCondition(List<Predicate<List<ShoppingProduct>>> conditions) {
        this.conditions = conditions;
    }

    // Abstract test method, subclasses יגדירו את הלוגיקה המתאימה
    @Override
    public abstract boolean test(List<ShoppingProduct> products);
}