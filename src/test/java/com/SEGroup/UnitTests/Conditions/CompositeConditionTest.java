package com.SEGroup.UnitTests.Conditions;

import static org.junit.jupiter.api.Assertions.*;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Conditions.OrCondition;
import com.SEGroup.Domain.Conditions.XorCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CompositeConditionTest {

    // Dummy Condition implementations for testing
    private static class TrueCondition implements Condition {
        @Override
        public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
            return true;
        }
    }

    private static class FalseCondition implements Condition {
        @Override
        public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
            return false;
        }
    }

    private ShoppingProduct product1;
    private ShoppingProduct product2;
    private List<ShoppingProduct> basketProducts;
    private List<Integer> basketAmounts;

    @BeforeEach
    public void setup() {
        product1 = new ShoppingProduct("StoreA", "cat1", "p1", "Product1", "desc", 100.0, 0, "", List.of("cat1"));
        product2 = new ShoppingProduct("StoreA", "cat2", "p2", "Product2", "desc", 50.0, 0, "", List.of("cat2"));

        basketProducts = new ArrayList<>();
        basketAmounts = new ArrayList<>();

        basketProducts.add(product1);
        basketAmounts.add(3); // 3 units of product1

        basketProducts.add(product2);
        basketAmounts.add(4); // 4 units of product2
    }

    // ========== AndCondition Tests ==========

    @Test
    public void shouldReturnTrue_WhenAllConditionsAreTrue_InAndCondition() {
        AndCondition andCondition = new AndCondition(
                List.of(new TrueCondition(), new TrueCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertTrue(andCondition.isSatisfiedBy(basketProducts, basketAmounts));
    }

    @Test
    public void shouldReturnFalse_WhenAnyConditionIsFalse_InAndCondition() {
        AndCondition andCondition = new AndCondition(
                List.of(new TrueCondition(), new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertFalse(andCondition.isSatisfiedBy(basketProducts, basketAmounts));
    }

    // ========== OrCondition Tests ==========

    @Test
    public void shouldReturnTrue_WhenAnyConditionIsTrue_InOrCondition() {
        OrCondition orCondition = new OrCondition(
                List.of(new FalseCondition(), new TrueCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertTrue(orCondition.isSatisfiedBy(basketProducts, basketAmounts));
    }

    @Test
    public void shouldReturnFalse_WhenAllConditionsAreFalse_InOrCondition() {
        OrCondition orCondition = new OrCondition(
                List.of(new FalseCondition(), new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertFalse(orCondition.isSatisfiedBy(basketProducts, basketAmounts));
    }

    // ========== XorCondition Tests ==========

    @Test
    public void shouldReturnTrue_WhenExactlyOneConditionIsTrue_InXorCondition() {
        XorCondition xorCondition = new XorCondition(
                List.of(new TrueCondition(), new FalseCondition(), new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertTrue(xorCondition.isSatisfiedBy(basketProducts, basketAmounts));
    }

    @Test
    public void shouldReturnFalse_WhenZeroOrMoreThanOneConditionIsTrue_InXorCondition() {
        XorCondition xorConditionZeroTrue = new XorCondition(
                List.of(new FalseCondition(), new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertFalse(xorConditionZeroTrue.isSatisfiedBy(basketProducts, basketAmounts));

        XorCondition xorConditionTwoTrue = new XorCondition(
                List.of(new TrueCondition(), new TrueCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        assertFalse(xorConditionTwoTrue.isSatisfiedBy(basketProducts, basketAmounts));
    }

    // ========== Discount Calculation Tests ==========

    @Test
    public void shouldCalculateDiscountCorrectly_WhenActiveAndConditionTrue_InAndCondition() {
        AndCondition andCondition = new AndCondition(
                List.of(new TrueCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        andCondition.setActive(true);
        double discountedPrice = andCondition.calculate(product1, 3);
        assertEquals(270.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldCalculateDiscountCorrectly_WhenActiveAndConditionTrue_InOrCondition() {
        OrCondition orCondition = new OrCondition(
                List.of(new TrueCondition()),
                DiscountType.PRODUCT, 15, "p1", null);
        orCondition.setActive(true);
        double discountedPrice = orCondition.calculate(product1, 3);
        assertEquals(255.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldCalculateDiscountCorrectly_WhenActiveAndConditionTrue_InXorCondition() {
        XorCondition xorCondition = new XorCondition(
                List.of(new TrueCondition()),
                DiscountType.PRODUCT, 20, "p1", null);
        xorCondition.setActive(true);
        double discountedPrice = xorCondition.calculate(product1, 3);
        assertEquals(240.0, discountedPrice, 0.001);
    }

    // ========== Price Calculation When Conditions Are Not Met ==========

    @Test
    public void shouldCalculateFullPrice_WhenConditionFalse_InAndCondition() {
        AndCondition andCondition = new AndCondition(
                List.of(new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        andCondition.setActive(true);
        double price = andCondition.calculate(product1, 3);
        assertEquals(300.0, price, 0.001);
    }

    @Test
    public void shouldCalculateFullPrice_WhenConditionFalse_InOrCondition() {
        OrCondition orCondition = new OrCondition(
                List.of(new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        orCondition.setActive(true);
        double price = orCondition.calculate(product1, 3);
        assertEquals(300.0, price, 0.001);
    }

    @Test
    public void shouldCalculateFullPrice_WhenConditionFalse_InXorCondition() {
        XorCondition xorCondition = new XorCondition(
                List.of(new FalseCondition()),
                DiscountType.PRODUCT, 10, "p1", null);
        xorCondition.setActive(true);
        double price = xorCondition.calculate(product1, 3);
        assertEquals(300.0, price, 0.001);
    }
}
