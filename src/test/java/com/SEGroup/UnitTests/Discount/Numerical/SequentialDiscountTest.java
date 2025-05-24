package com.SEGroup.UnitTests.Discount.Numerical;
import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Discount.Discount;
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

        Map<String, Double> result = sequentialDiscount.calculateDiscountForBasket(basket);
        double finalPrice = result.get("p1"); // Using product ID

        // Sequential calculation:
        // Original: 100
        // After 5% dairy discount: 100 - 5 = 95
        // After 20% store discount on remaining 95: 95 - (20% of 95) = 95 - 19 = 76
        assertEquals(76.0, finalPrice, 0.001);
    }

    @Test
    public void shouldApplySequentialDiscountAcrossMultipleProducts_WhenCategoryAndStoreDiscountsArePresent() {
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

        Map<String, Double> result = sequentialDiscount.calculateDiscountForBasket(basket);
        double milkFinalPrice = result.get("p1");
        double pastaFinalPrice = result.get("p2");
        double totalFinalPrice = milkFinalPrice + pastaFinalPrice;

        // Manual calculation for milk (dairy product):
        // Original: 100, After 10% dairy discount: 90, After 5% store discount: 90 - 4.5 = 85.5
        assertEquals(85.5, milkFinalPrice, 0.001);

        // Manual calculation for pasta (no dairy discount applies):
        // Original: 90, After 5% store discount: 90 - 4.5 = 85.5
        assertEquals(85.5, pastaFinalPrice, 0.001);

        // Total final price
        assertEquals(171.0, totalFinalPrice, 0.001);
    }

    @Test
    public void shouldReturnOriginalPrice_WhenNoDiscountsArePresentInSequentialDiscount() {
        List<String> dairy = List.of("dairy");
        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 50.0, 1, "", dairy
        );

        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of());

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(milk, 1);

        Map<String, Double> result = sequentialDiscount.calculateDiscountForBasket(basket);
        double finalPrice = result.get("p1");

        // No discounts applied, should return original price
        assertEquals(50.0, finalPrice, 0.001);
    }
    @Test
    public void shouldApplyCompositeConditionAsPartOfSequentialDiscount_WhenTotalPriceMeetsThreshold() {
        List<String> sweets = List.of("sweets");

        ShoppingProduct chocolate = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Chocolate", "Sweet Chocolate", 40.0, 2, "", sweets
        );

        Condition minPriceCondition = new Condition() {
            @Override
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                double total = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    total += products.get(i).getPrice() * amounts.get(i);
                }
                return total >= 50; // will be satisfied: 2 * 40 = 80
            }
        };

        Discount compositeDiscount = new AndCondition(
                List.of(minPriceCondition),
                DiscountType.CATEGORY,
                25,
                "sweets",
                null
        );

        Discount storeDiscount = new SimpleDiscount(DiscountType.STORE, 10, null, null);

        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of(compositeDiscount, storeDiscount));

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(chocolate, 2); // total before: 80.0

        Map<String, Double> result = sequentialDiscount.calculateDiscountForBasket(basket);
        double finalPrice = result.get("p1");

        // 1. 25% → 80 * 0.75 = 60
        // 2. 10%  60 → 60 * 0.9 = 54
        assertEquals(54.0, finalPrice, 0.001);
    }
}
