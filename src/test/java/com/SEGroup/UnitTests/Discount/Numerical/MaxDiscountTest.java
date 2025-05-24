package com.SEGroup.UnitTests.Discount.Numerical;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Conditions.Condition;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.MaxDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaxDiscountTest {

    @Test
    public void shouldCalculateMaxDiscountForSingleProduct_WithMultipleDiscounts() {
        List<String> pastaCats = List.of("pasta");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 20.0, 3, "", pastaCats
        ); // total 60

        SimpleDiscount discount5Percent = new SimpleDiscount(DiscountType.CATEGORY, 5, "pasta", null);
        SimpleDiscount discount10Percent = new SimpleDiscount(DiscountType.STORE, 10, null, null);

        MaxDiscount maxDiscount = new MaxDiscount(List.of(discount5Percent, discount10Percent));

        double discountAmount = maxDiscount.calculate(pasta, 3);

        // 5% of 60 = 3.0, 10% of 60 = 6.0, max is 6.0
        assertEquals(54.0, discountAmount, 0.001);
    }

    @Test
    public void shouldReturnZeroDiscount_WhenNoDiscountsApply() {
        List<String> categories = List.of("electronics");

        ShoppingProduct product = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Gadget", "desc", 50.0, 2, "", categories
        );

        // Empty MaxDiscount (no discounts)
        MaxDiscount maxDiscount = new MaxDiscount(List.of());

        double discountAmount = maxDiscount.calculate(product, 2);

        assertEquals(100, discountAmount, 0.001);
    }

    public void shouldCalculateMaxDiscountForBasket_CorrectlyIdentifyMaxDiscount() {
        List<String> pastaCats = List.of("pasta");
        List<String> dairyCats = List.of("dairy");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 20.0, 0, "", pastaCats
        ); // price 20 per unit

        ShoppingProduct milk = new ShoppingProduct(
                "StoreA", "cat2", "p2", "Milk", "desc", 10.0, 0, "", dairyCats
        ); // price 10 per unit

        SimpleDiscount pastaDiscount = new SimpleDiscount(DiscountType.CATEGORY, 5, "pasta", null);
        SimpleDiscount milkDiscount = new SimpleDiscount(DiscountType.CATEGORY, 17, "dairy", null);

        MaxDiscount maxDiscount = new MaxDiscount(List.of(pastaDiscount, milkDiscount));

        Map<ShoppingProduct, Integer> basket = new HashMap<>();
        basket.put(pasta, 3);  // total = 60, discount = 5% = 3
        basket.put(milk, 4);   // total = 40, discount = 17% = 6.8

        Map<String, Double> discountedPrices = maxDiscount.calculateDiscountForBasket(basket);

        // Only milk gets the discount → 40 - 6.8 = 33.2
        // Pasta stays full price → 3 * 20 = 60
        assertEquals(2, discountedPrices.size());
        assertEquals(60.0, discountedPrices.get("p1"), 0.001);   // pasta has no discount
        assertEquals(33.2, discountedPrices.get("p2"), 0.001);   // milk got the discount
    }

    @Test
    public void shouldCalculateMaxDiscountWithCoupon_OnlyActiveDiscountApplies() {
        List<String> pastaCats = List.of("pasta");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 20.0, 3, "", pastaCats
        );

        SimpleDiscount discount5PercentInactive = new SimpleDiscount(DiscountType.CATEGORY, 5, "pasta", "COUPON1");
        SimpleDiscount discount10PercentActive = new SimpleDiscount(DiscountType.STORE, 10, null, null);

        MaxDiscount maxDiscount = new MaxDiscount(List.of(discount5PercentInactive, discount10PercentActive));

        // Apply coupon only to the inactive discount
        maxDiscount.applyCoupon("COUPON1");

        double discountAmount = maxDiscount.calculate(pasta, 3);

        // The 5% discount is activated by coupon, but 10% store discount is active by default
        // max between 5% of 60=3 and 10% of 60=6 is 6
        assertEquals(54.0, discountAmount, 0.001);
    }

    @Test
    public void shouldReturnZeroDiscount_WhenBasketIsEmpty() {
        MaxDiscount maxDiscount = new MaxDiscount(List.of());
        Map<String,Double> discountAmount = maxDiscount.calculateDiscountForBasket(new HashMap<>());
        assertEquals(discountAmount.isEmpty(), "Expected empty discount map when basket is empty");
    }
    @Test
    public void shouldNotApplyCompositeDiscount_WhenConditionFails() {
        List<String> pastaCats = List.of("pasta");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 20.0, 0, "", pastaCats
        );

        // תנאי מינימום מחיר על הסל כולו (100 ש"ח)
        Condition minPriceCondition = new Condition() {
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                double total = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    total += products.get(i).getPrice() * amounts.get(i);
                }
                return total >= 100;
            }
        };

        Discount composite = new AndCondition(
                List.of(minPriceCondition),
                DiscountType.CATEGORY,
                30, // 30% הנחה
                "pasta",
                null
        );

        MaxDiscount maxDiscount = new MaxDiscount(List.of(composite));

        Map<ShoppingProduct, Integer> basket = Map.of(pasta, 2); // רק 40 ש"ח

        Map<String, Double> discounted = maxDiscount.calculateDiscountForBasket(basket);

        // תנאי לא מתקיים → לא מופעלת הנחה
        assertEquals(40.0, discounted.get("p1"), 0.001);
    }
    @Test
    public void shouldApplyCompositeDiscount_WhenMinPriceConditionIsSatisfied() {
        List<String> pastaCats = List.of("pasta");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 25.0, 0, "", pastaCats
        );

        Condition minPriceCondition = new Condition() {
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                double total = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    total += products.get(i).getPrice() * amounts.get(i);
                }
                return total >= 50;
            }
        };

        Discount composite = new AndCondition(
                List.of(minPriceCondition),
                DiscountType.CATEGORY,
                20, // 20% הנחה
                "pasta",
                null
        );

        MaxDiscount maxDiscount = new MaxDiscount(List.of(composite));

        Map<ShoppingProduct, Integer> basket = Map.of(pasta, 2); // 50 ש"ח

        Map<String, Double> discounted = maxDiscount.calculateDiscountForBasket(basket);

        // 20% הנחה → 50 * 0.8 = 40
        assertEquals(40.0, discounted.get("p1"), 0.001);
    }
    @Test
    public void shouldApplyMaxDiscount_WhenCompositeConditionIsBetterThanRegularDiscount() {
        List<String> pastaCats = List.of("pasta");

        ShoppingProduct pasta = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Pasta", "desc", 25.0, 0, "", pastaCats
        );

        // Regular discount: 10% on the whole store
        Discount regularDiscount = new SimpleDiscount(
                DiscountType.STORE, 10, null, null
        );

        // Composite discount: 30% on pasta, only if total >= 50
        Condition minPriceCondition = new Condition() {
            @Override
            public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                double total = 0.0;
                for (int i = 0; i < products.size(); i++) {
                    total += products.get(i).getPrice() * amounts.get(i);
                }
                return total >= 50;
            }
        };

        Discount compositeDiscount = new AndCondition(
                List.of(minPriceCondition),
                DiscountType.CATEGORY,
                30,
                "pasta",
                null
        );

        MaxDiscount maxDiscount = new MaxDiscount(List.of(regularDiscount, compositeDiscount));

        Map<ShoppingProduct, Integer> basket = Map.of(pasta, 2); // total = 50

        Map<String, Double> discounted = maxDiscount.calculateDiscountForBasket(basket);

        // Regular: 10% → 50 * 0.9 = 45
        // Composite: 30% → 50 * 0.7 = 35 → should win
        assertEquals(35.0, discounted.get("p1"), 0.001);
    }
}
