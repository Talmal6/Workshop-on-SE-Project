package com.SEGroup.UnitTests.Conditions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.SEGroup.Domain.Conditions.XorCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class XorConditionTest {

    private static class HasAtLeast5RollsCondition implements Condition {
        @Override
        public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
            int count = 0;
            for (int i = 0; i < products.size(); i++) {
                ShoppingProduct p = products.get(i);
                int quantity = amounts.get(i);
                if (p.getName().toLowerCase().contains("roll")) {
                    count += quantity;
                }
            }
            return count >= 5;
        }
    }

    private static class HasAtLeast2BreadsCondition implements Condition {
        @Override
        public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
            int count = 0;
            for (int i = 0; i < products.size(); i++) {
                ShoppingProduct p = products.get(i);
                int quantity = amounts.get(i);
                if (p.getName().toLowerCase().contains("bread")) {
                    count += quantity;
                }
            }
            return count >= 2;
        }
    }

    @Test
    public void shouldReturnTrue_WhenExactlyOneConditionIsMet() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("StoreX", "cat1", "p1", "Soft Roll", "Desc", 1.0, 0, "", List.of("bakery")));
        amounts.add(5); // 5 rolls

        products.add(new ShoppingProduct("StoreX", "cat2", "p2", "Whole Bread", "Desc", 2.0, 0, "", List.of("bakery")));
        amounts.add(1); // 1 bread

        XorCondition xorCondition = new XorCondition(List.of(
                new HasAtLeast5RollsCondition(),
                new HasAtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = xorCondition.isSatisfiedBy(products, amounts);
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalse_WhenBothConditionsAreMet() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("StoreX", "cat1", "p1", "Soft Roll", "Desc", 1.0, 0, "", List.of("bakery")));
        amounts.add(5); // 5 rolls

        products.add(new ShoppingProduct("StoreX", "cat2", "p2", "Whole Bread", "Desc", 2.0, 0, "", List.of("bakery")));
        amounts.add(2); // 2 breads

        XorCondition xorCondition = new XorCondition(List.of(
                new HasAtLeast5RollsCondition(),
                new HasAtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = xorCondition.isSatisfiedBy(products, amounts);
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalse_WhenNeitherConditionIsMet() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("StoreX", "cat3", "p3", "Cookie", "Desc", 1.0, 0, "", List.of("bakery")));
        amounts.add(10);

        products.add(new ShoppingProduct("StoreX", "cat4", "p4", "Milk", "Desc", 2.0, 0, "", List.of("dairy")));
        amounts.add(5);

        XorCondition xorCondition = new XorCondition(List.of(
                new HasAtLeast5RollsCondition(),
                new HasAtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = xorCondition.isSatisfiedBy(products, amounts);
        assertFalse(result);
    }
}
