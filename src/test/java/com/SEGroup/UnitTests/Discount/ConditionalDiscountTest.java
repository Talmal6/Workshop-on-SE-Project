package com.SEGroup.UnitTests.Discount;

import com.SEGroup.Domain.Discount.ConditionalDiscount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionalDiscountTest {

    @Test
    public void shouldApplyProductLevelDiscount_WhenTotalPurchaseExceedsThreshold() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 5, "", vegetables
        ); // total 50
        ShoppingProduct cucumber = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Cucumber", "Green Cucumber", 80.0, 2, "", vegetables
        ); // total 160

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 10, 200, 1, "p1", null
        );

        discount.Activate(50 + 160); // 210 > 200 => active

        double discountedPrice = discount.calculate(tomato, 5);

        assertEquals(45.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldNotApplyProductLevelDiscount_WhenTotalPurchaseIsBelowThreshold() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 1, "", vegetables
        ); // total 10
        ShoppingProduct cucumber = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Cucumber", "Green Cucumber", 18.9, 10, "", vegetables
        ); // total 189

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 10, 200, 1, "p1", null
        );

        discount.Activate(10 + 189); // 199 < 200 => discount NOT active

        double discountedPrice = discount.calculate(tomato, 1);

        assertEquals(10.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldNotApplyCategoryDiscount_WhenQuantityIsBelowMinimum() {
        List<String> dairy = List.of("dairy");

        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 10.0, 1, "", dairy
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.CATEGORY, 10, 0, 2, "dairy", null
        );
        discount.Activate(0);

        double discountedMilk = discount.calculate(milk, 1);

        assertEquals(10.0, discountedMilk, 0.001);
    }

    @Test
    public void shouldApplyCategoryDiscount_WhenQuantityIsAtOrAboveMinimum() {
        List<String> dairy = List.of("dairy");

        ShoppingProduct cheese = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Cheese", "Yellow Cheese", 20.0, 1, "", dairy
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.CATEGORY, 10, 0, 2, "dairy", null
        );
        discount.Activate(0);

        double discountedCheese = discount.calculate(cheese, 2);

        assertEquals(36.0, discountedCheese, 0.001);
    }

    @Test
    public void shouldNotApplyDiscount_WhenCouponIsInactive() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 5, "", vegetables
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 10, 0, 1, "p1", "COUPON1"
        );

        double discountedPrice = discount.calculate(tomato, 5);

        assertEquals(50.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldApplyDiscount_WhenCouponIsActive() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 5, "", vegetables
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 10, 0, 1, "p1", "COUPON1"
        );
        discount.Activate(0);

        discount.ApplyCoupon("COUPON1");

        double discountedPrice = discount.calculate(tomato, 5);

        // 10% הנחה על 50 => 45
        assertEquals(45.0, discountedPrice, 0.001);
    }


    @Test
    public void shouldNotApplyCategoryDiscount_WhenProductNotInCategoryScope() {
        List<String> dairy = List.of("dairy");
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct milk = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Milk", "Fresh Milk", 10.0, 2, "", dairy
        );

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Tomato", "Red Tomato", 20.0, 5, "", vegetables
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.CATEGORY, 10, 0, 1, "dairy", null
        );
        discount.Activate(10);

        double discountedMilk = discount.calculate(milk, 2);
        assertEquals(18.0, discountedMilk, 0.001);

        double discountedTomato = discount.calculate(tomato, 5);
        assertEquals(100.0, discountedTomato, 0.001);
    }

    @Test
    public void shouldApplyStoreLevelDiscount_WhenDiscountIsActiveAndQuantityMeetsMinimum_ForMultipleProducts() {
        List<String> categories = List.of("any");

        ShoppingProduct product1 = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Product", "Some product", 50.0, 2, "", categories
        );

        ShoppingProduct product2 = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Product2", "Some product2", 100.0, 1, "", categories
        );

        // Store level discount 10%, minPrice=0, minAmount=1
        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.STORE, 10, 0, 1, null, null
        );
        discount.Activate(0);

        // Calculate discounted price per product
        double discountedPrice1 = discount.calculate(product1, product1.getQuantity()); // 50*2=100 -> 90
        double discountedPrice2 = discount.calculate(product2, product2.getQuantity()); // 100*1=100 -> 90

        double totalDiscountedPrice = discountedPrice1 + discountedPrice2; // 90 + 90 = 180

        assertEquals(180.0, totalDiscountedPrice, 0.001);
    }

    @Test
    public void shouldNotApplyStoreLevelDiscount_WhenQuantityIsBelowMinimum_ForMultipleProducts() {
        List<String> categories1 = List.of("category1");
        List<String> categories2 = List.of("category2");

        ShoppingProduct product1 = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Product1", "Description1", 50.0, 1, "", categories1
        );

        ShoppingProduct product2 = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Product2", "Description2", 100.0, 2, "", categories2
        );

        // Store level discount 10%, minAmount=3 (threshold higher than any product quantity)
        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.STORE, 10, 0, 3, null, null
        );
        discount.Activate(0);

        // Calculate discounted price for each product
        double discountedPrice1 = discount.calculate(product1, product1.getQuantity()); // quantity=1 < 3, no discount
        double discountedPrice2 = discount.calculate(product2, product2.getQuantity()); // quantity=2 < 3, no discount

        double totalPrice = discountedPrice1 + discountedPrice2;
        double expectedPrice = (50.0 * 1) + (100.0 * 2); // no discounts applied

        assertEquals(expectedPrice, totalPrice, 0.001);
    }

    @Test
    public void shouldNotApplyStoreLevelDiscount_WhenDiscountIsInactive_ForMultipleProducts() {
        List<String> categories1 = List.of("category1");
        List<String> categories2 = List.of("category2");

        ShoppingProduct product1 = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Product1", "Description1", 50.0, 3, "", categories1
        );

        ShoppingProduct product2 = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Product2", "Description2", 100.0, 2, "", categories2
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.STORE, 15, 0, 1, null, "COUPON1"
        );
        // Discount not activated, coupon not applied

        double discountedPrice1 = discount.calculate(product1, product1.getQuantity()); // no discount applied
        double discountedPrice2 = discount.calculate(product2, product2.getQuantity()); // no discount applied

        double totalPrice = discountedPrice1 + discountedPrice2;
        double expectedPrice = (50.0 * 3) + (100.0 * 2); // full price

        assertEquals(expectedPrice, totalPrice, 0.001);
    }

    @Test
    public void shouldApplyStoreLevelDiscount_WhenCouponIsActive_ForMultipleProducts() {
        List<String> categories1 = List.of("category1");
        List<String> categories2 = List.of("category2");

        ShoppingProduct product1 = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Product1", "Description1", 50.0, 3, "", categories1
        );

        ShoppingProduct product2 = new ShoppingProduct(
                "YuvalStore", "cat2", "p2", "Product2", "Description2", 100.0, 2, "", categories2
        );

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.STORE, 15, 0, 1, null, "COUPON1"
        );
        discount.Activate(0);
        discount.ApplyCoupon("COUPON1");

        double discountedPrice1 = discount.calculate(product1, product1.getQuantity()); // 15% off
        double discountedPrice2 = discount.calculate(product2, product2.getQuantity()); // 15% off

        double totalPrice = discountedPrice1 + discountedPrice2;
        double expectedPrice = (50.0 * 3 * 0.85) + (100.0 * 2 * 0.85); // 15% off on both products

        assertEquals(expectedPrice, totalPrice, 0.001);
    }

    @Test
    public void shouldApplyDiscount_WhenQuantityWithinMinAndMaxAmount() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 5, "", vegetables
        ); // quantity = 3 (between 2 and 5)

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 20, 0, 2, 5, "p1", null
        );
        discount.Activate(30); // Activate with any price ≥ 0

        double discountedPrice = discount.calculate(tomato, 3); // 3 * 10 = 30 → 20% off = 24

        assertEquals(24.0, discountedPrice, 0.001);
    }

    @Test
    public void shouldNotApplyDiscount_WhenQuantityExceedsMaxAmount() {
        List<String> vegetables = List.of("vegetables");

        ShoppingProduct tomato = new ShoppingProduct(
                "YuvalStore", "cat1", "p1", "Tomato", "Red Tomato", 10.0, 10, "", vegetables
        ); // quantity = 6 (above max = 5)

        ConditionalDiscount discount = new ConditionalDiscount(
                DiscountType.PRODUCT, 20, 0, 2, 5, "p1", null
        );
        discount.Activate(60); // Activate with any price ≥ 0

        double discountedPrice = discount.calculate(tomato, 6); // no discount → 6 * 10 = 60

        assertEquals(60.0, discountedPrice, 0.001);
    }



}
