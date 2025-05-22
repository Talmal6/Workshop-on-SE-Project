package com.SEGroup.UnitTests.Discount.Numerical;

import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.SequentialDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequentialDiscountTest {

    @Test
    public void shouldApplySequentialDiscountSuccessfully_WhenApplying5PercentThen20PercentOnSameProduct() {
        List<String> dairy = List.of("dairy");

        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 100.0, 1, "", dairy
        );

        SimpleDiscount dairyDiscount = new SimpleDiscount(DiscountType.CATEGORY, 5, "dairy", null);
        SimpleDiscount storeDiscount = new SimpleDiscount(DiscountType.STORE, 20, null, null);

        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of(dairyDiscount, storeDiscount));

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(milk, 1);

        double discountAmount = sequentialDiscount.calculateDiscountForBasket(basket);

        assertEquals(76.0, discountAmount, 0.001); // 5% of 100 = 5, then 20% of (100 - 5) = 20% of 95 = 19 => total discount = 5 + 19 = 24
    }

    @Test
    public void shouldApplySequentialDiscountAcrossMultipleProducts() {
        List<String> dairy = List.of("dairy");
        List<String> pasta = List.of("pasta");

        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 50.0, 2, "", dairy
        ); // total 100

        ShoppingProduct pastaProduct = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Pasta", "Delicious pasta", 30.0, 3, "", pasta
        ); // total 90

        SimpleDiscount dairyDiscount = new SimpleDiscount(DiscountType.CATEGORY, 10, "dairy", null); // 10% off dairy
        SimpleDiscount storeDiscount = new SimpleDiscount(DiscountType.STORE, 5, null, null);         // 5% off store

        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of(dairyDiscount, storeDiscount));

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(milk, 2);
        basket.put(pastaProduct, 3);

        double discountAmount = sequentialDiscount.calculateDiscountForBasket(basket);

        // Manual calculation:
        // Total before discounts: 100 + 90 = 190
        // First discount 10% on dairy: 10% of 100 = 10
        // Total after first discount: 190 - 10 = 180
        // Second discount 5% on total: 5% of 180 = 9
        // Total discount: 10 + 9 = 19

        assertEquals(171.0, discountAmount, 0.001);
    }

    @Test
    public void shouldReturnZero_WhenNoDiscountsArePresent() {
        List<String> dairy = List.of("dairy");

        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 50.0, 1, "", dairy
        );

        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of());

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(milk, 1);

        double discountAmount = sequentialDiscount.calculateDiscountForBasket(basket);

        assertEquals(50.0, discountAmount, 0.001);
    }

}
