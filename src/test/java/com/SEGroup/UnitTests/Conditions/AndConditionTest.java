package com.SEGroup.UnitTests.Conditions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AndConditionTest {

    private static class AtLeast5RollsCondition implements Condition {
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

    private static class AtLeast2BreadsCondition implements Condition {
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
    public void shouldReturnTrue_WhenBasketContainsAtLeast5RollsAndAtLeast2Breads() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("YuvalStore", "cat1", "p1", "Soft Roll", "Soft tasty roll", 1.0, 0, "", List.of("bakery")));
        amounts.add(5); // 5 rolls

        products.add(new ShoppingProduct("YuvalStore", "cat2", "p2", "Whole Bread", "Healthy bread", 2.0, 0, "", List.of("bakery")));
        amounts.add(2); // 2 breads

        AndCondition andCondition = new AndCondition(List.of(
                new AtLeast5RollsCondition(),
                new AtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = andCondition.isSatisfiedBy(products, amounts);
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalse_WhenBasketContainsLessThan5RollsEvenIfHasAtLeast2Breads() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("YuvalStore", "cat1", "p1", "Soft Roll", "Soft tasty roll", 1.0, 0, "", List.of("bakery")));
        amounts.add(4); // 4 rolls (less than 5)

        products.add(new ShoppingProduct("YuvalStore", "cat2", "p2", "Whole Bread", "Healthy bread", 2.0, 0, "", List.of("bakery")));
        amounts.add(2); // 2 breads

        AndCondition andCondition = new AndCondition(List.of(
                new AtLeast5RollsCondition(),
                new AtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = andCondition.isSatisfiedBy(products, amounts);
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalse_WhenBasketContainsAtLeast5RollsButLessThan2Breads() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("YuvalStore", "cat1", "p1", "Soft Roll", "Soft tasty roll", 1.0, 0, "", List.of("bakery")));
        amounts.add(5); // 5 rolls

        products.add(new ShoppingProduct("YuvalStore", "cat2", "p2", "Whole Bread", "Healthy bread", 2.0, 0, "", List.of("bakery")));
        amounts.add(1); // 1 bread (less than 2)

        AndCondition andCondition = new AndCondition(List.of(
                new AtLeast5RollsCondition(),
                new AtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = andCondition.isSatisfiedBy(products, amounts);
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalse_WhenBasketContainsNeitherRollsNorBreads() {
        List<ShoppingProduct> products = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        products.add(new ShoppingProduct("YuvalStore", "cat3", "p3", "Soft Cookie", "Sweet cookie", 1.0, 0, "", List.of("bakery")));
        amounts.add(10); // 10 cookies, no rolls

        products.add(new ShoppingProduct("YuvalStore", "cat4", "p4", "Milk", "Fresh milk", 2.0, 0, "", List.of("dairy")));
        amounts.add(5); // 5 milk, no bread

        AndCondition andCondition = new AndCondition(List.of(
                new AtLeast5RollsCondition(),
                new AtLeast2BreadsCondition()
        ), null, 0, null, null);

        boolean result = andCondition.isSatisfiedBy(products, amounts);
        assertFalse(result);
    }

}
