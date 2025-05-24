package com.SEGroup.UnitTests.Discount;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Conditions.OrCondition;
import com.SEGroup.Domain.Conditions.XorCondition;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class CompositeConditionTest {

    private ShoppingProduct milk;
    private ShoppingProduct bread;
    private ShoppingProduct pasta;
    private List<String> dairyCategory;
    private List<String> bakeryCategory;
    private List<String> pastaCategory;

    @BeforeEach
    public void setUp() {
        dairyCategory = List.of("dairy");
        bakeryCategory = List.of("bakery");
        pastaCategory = List.of("pasta");

        milk = new ShoppingProduct("Store1", "cat1", "milk1", "Milk", "Fresh Milk", 10.0, 1, "", dairyCategory);
        bread = new ShoppingProduct("Store1", "cat2", "bread1", "Bread", "Fresh Bread", 5.0, 1, "", bakeryCategory);
        pasta = new ShoppingProduct("Store1", "cat3", "pasta1", "Pasta", "Italian Pasta", 8.0, 1, "", pastaCategory);
    }

    // Helper method to create a simple condition for testing
    private Condition createProductQuantityCondition(String productId, int minQuantity) {
        return new Condition() {
            @Override
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getProductId().equals(productId) && amounts.get(i) >= minQuantity) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private Condition createMinPriceCondition(double minPrice) {
        return new Condition() {
            @Override
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                double total = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    total += products.get(i).getPrice() * amounts.get(i);
                }
                return total >= minPrice;
            }
        };
    }

    // ============ AND CONDITION TESTS ============

    @Test
    public void testAndCondition_AllConditionsSatisfied_ShouldReturnTrue() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 2);
        Condition breadCondition = createProductQuantityCondition("bread1", 1);
        Condition minPriceCondition = createMinPriceCondition(20.0);

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition, minPriceCondition);
        AndCondition andDiscount = new AndCondition(conditions, DiscountType.STORE, 10.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1); // 2 milk + 1 bread = $25 total

        // Act & Assert
        assertTrue(andDiscount.isSatisfiedBy(products, amounts));

        // Test discount calculation
        double discountedPrice = andDiscount.calculate(milk, 2);
        assertEquals(18.0, discountedPrice, 0.001); // 20 - (20 * 0.1) = 18
    }

    @Test
    public void testAndCondition_OneConditionNotSatisfied_ShouldReturnFalse() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 3); // Need 3 milk
        Condition breadCondition = createProductQuantityCondition("bread1", 1);

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition);
        AndCondition andDiscount = new AndCondition(conditions, DiscountType.STORE, 10.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1); // Only 2 milk, but need 3

        // Act & Assert
        assertFalse(andDiscount.isSatisfiedBy(products, amounts));

        // Test no discount applied
        double discountedPrice = andDiscount.calculate(milk, 2);
        assertEquals(20.0, discountedPrice, 0.001); // No discount, original price
    }

    @Test
    public void testAndCondition_EmptyBasket_ShouldReturnFalse() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 1);
        List<Condition> conditions = Arrays.asList(milkCondition);
        AndCondition andDiscount = new AndCondition(conditions, DiscountType.STORE, 10.0, null, null);

        List<ShoppingProduct> products = Arrays.asList();
        List<Integer> amounts = Arrays.asList();

        // Act & Assert
        assertFalse(andDiscount.isSatisfiedBy(products, amounts));
    }

    // ============ OR CONDITION TESTS ============

    @Test
    public void testOrCondition_OneConditionSatisfied_ShouldReturnTrue() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 5); // Need 5 milk (not satisfied)
        Condition breadCondition = createProductQuantityCondition("bread1", 1); // Need 1 bread (satisfied)
        Condition minPriceCondition = createMinPriceCondition(100.0); // Need $100 (not satisfied)

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition, minPriceCondition);
        OrCondition orDiscount = new OrCondition(conditions, DiscountType.CATEGORY, 15.0, "bakery", null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1); // Only bread condition satisfied

        // Act & Assert
        assertTrue(orDiscount.isSatisfiedBy(products, amounts));

        // Test discount calculation on bread (category discount)
        double discountedPrice = orDiscount.calculate(bread, 1);
        assertEquals(4.25, discountedPrice, 0.001); // 5 - (5 * 0.15) = 4.25
    }

    @Test
    public void testOrCondition_MultipleConditionsSatisfied_ShouldReturnTrue() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 1); // Satisfied
        Condition breadCondition = createProductQuantityCondition("bread1", 1); // Satisfied

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition);
        OrCondition orDiscount = new OrCondition(conditions, DiscountType.STORE, 5.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1);

        // Act & Assert
        assertTrue(orDiscount.isSatisfiedBy(products, amounts));
    }

    @Test
    public void testOrCondition_NoConditionsSatisfied_ShouldReturnFalse() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 5); // Need 5 milk
        Condition pastaCondition = createProductQuantityCondition("pasta1", 3); // Need 3 pasta

        List<Condition> conditions = Arrays.asList(milkCondition, pastaCondition);
        OrCondition orDiscount = new OrCondition(conditions, DiscountType.STORE, 10.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread); // No pasta, insufficient milk
        List<Integer> amounts = Arrays.asList(2, 1);

        // Act & Assert
        assertFalse(orDiscount.isSatisfiedBy(products, amounts));

        // Test no discount applied
        double discountedPrice = orDiscount.calculate(milk, 2);
        assertEquals(20.0, discountedPrice, 0.001); // No discount
    }

    // ============ XOR CONDITION TESTS ============

    @Test
    public void testXorCondition_ExactlyOneConditionSatisfied_ShouldReturnTrue() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 5); // Not satisfied (need 5, have 2)
        Condition breadCondition = createProductQuantityCondition("bread1", 1); // Satisfied
        Condition minPriceCondition = createMinPriceCondition(100.0); // Not satisfied (need $100, have $25)

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition, minPriceCondition);
        XorCondition xorDiscount = new XorCondition(conditions, DiscountType.PRODUCT, 20.0, "milk1", null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1);

        // Act & Assert
        assertTrue(xorDiscount.isSatisfiedBy(products, amounts));

        // Test discount calculation on specific product
        double discountedPrice = xorDiscount.calculate(milk, 2);
        assertEquals(16.0, discountedPrice, 0.001); // 20 - (20 * 0.2) = 16
    }

    @Test
    public void testXorCondition_MultipleConditionsSatisfied_ShouldReturnFalse() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 1); // Satisfied
        Condition breadCondition = createProductQuantityCondition("bread1", 1); // Satisfied
        Condition minPriceCondition = createMinPriceCondition(20.0); // Satisfied ($25 total)

        List<Condition> conditions = Arrays.asList(milkCondition, breadCondition, minPriceCondition);
        XorCondition xorDiscount = new XorCondition(conditions, DiscountType.STORE, 10.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1);

        // Act & Assert
        assertFalse(xorDiscount.isSatisfiedBy(products, amounts)); // More than one condition satisfied

        // Test no discount applied
        double discountedPrice = xorDiscount.calculate(milk, 2);
        assertEquals(20.0, discountedPrice, 0.001); // No discount
    }

    @Test
    public void testXorCondition_NoConditionsSatisfied_ShouldReturnFalse() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 10); // Not satisfied
        Condition pastaCondition = createProductQuantityCondition("pasta1", 5); // Not satisfied

        List<Condition> conditions = Arrays.asList(milkCondition, pastaCondition);
        XorCondition xorDiscount = new XorCondition(conditions, DiscountType.STORE, 15.0, null, null);

        List<ShoppingProduct> products = Arrays.asList(milk, bread);
        List<Integer> amounts = Arrays.asList(2, 1);

        // Act & Assert
        assertFalse(xorDiscount.isSatisfiedBy(products, amounts)); // Zero conditions satisfied

        // Test no discount applied
        double discountedPrice = xorDiscount.calculate(bread, 1);
        assertEquals(5.0, discountedPrice, 0.001); // No discount
    }

    // ============ EDGE CASE TESTS ============

    @Test
    public void testCompositeCondition_WithCoupon_ShouldRequireActivation() {
        // Arrange
        Condition milkCondition = createProductQuantityCondition("milk1", 1);
        List<Condition> conditions = Arrays.asList(milkCondition);
        AndCondition couponDiscount = new AndCondition(conditions, DiscountType.STORE, 25.0, null, "SAVE25");

        List<ShoppingProduct> products = Arrays.asList(milk);
        List<Integer> amounts = Arrays.asList(2);

        // Act & Assert - Before coupon activation
        assertTrue(couponDiscount.isSatisfiedBy(products, amounts)); // Condition satisfied
        assertFalse(couponDiscount.isActive()); // But discount not active
        double priceBeforeCoupon = couponDiscount.calculate(milk, 2);
        assertEquals(20.0, priceBeforeCoupon, 0.001); // No discount applied

        // Apply coupon
        couponDiscount.ApplyCoupon("SAVE25");
        assertTrue(couponDiscount.isActive());

        // Test discount after coupon activation
        double priceAfterCoupon = couponDiscount.calculate(milk, 2);
        assertEquals(15.0, priceAfterCoupon, 0.001); // 20 - (20 * 0.25) = 15
    }

    @Test
    public void testCompositeCondition_InvalidConditionsList_ShouldThrowException() {
        // Test null conditions
        assertThrows(IllegalArgumentException.class, () -> {
            new AndCondition(null, DiscountType.STORE, 10.0, null, null);
        });

        // Test empty conditions
        assertThrows(IllegalArgumentException.class, () -> {
            new OrCondition(Arrays.asList(), DiscountType.STORE, 10.0, null, null);
        });
    }
}