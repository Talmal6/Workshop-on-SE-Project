package com.SEGroup.UnitTests.Discount;

import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class SimpleDiscountTest {

    @Test
    public void shouldApplyProductLevelSimpleDiscount() {
        // Arrange
        double price = 100.0;
        int quantity = 3;
        List<String> categories = new ArrayList<>();
        categories.add("electronics");
        ShoppingProduct product = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Gadget", "Desc", price, quantity, "", categories
        );
        SimpleDiscount discount = new SimpleDiscount(DiscountType.PRODUCT, 20, "p1","COUPON1");

        // Act
        discount.ApplyCoupon("COUPON1");
        double discountedTotal = discount.calculate(product,3);

        // Assert
        // 20% off of (100 * 3) = 240.0
        assertEquals(240.0, discountedTotal, 0.001);
    }

    @Test
    public void shouldApplyCategoryLevelSimpleDiscount() {
        // Arrange
        double price = 50.0;
        int quantity = 2;
        List<String> categories = new ArrayList<>();
        categories.add("dairy");
        ShoppingProduct product = new ShoppingProduct(
                "StoreB", "cat2", "p2", "Milk", "Desc", price, quantity, "", categories
        );
        SimpleDiscount discount = new SimpleDiscount(DiscountType.CATEGORY, 50,"dairy", null);

        // Act
        double discountedTotal = discount.calculate(product,2);

        // Assert
        // 50% off of (50 * 2) = 50.0
        assertEquals(50.0, discountedTotal, 0.001);
    }

    @Test
    public void shouldApplyStoreLevelSimpleDiscount() {
        // Arrange
        double price1 = 10.0;
        int quantity1 = 5;
        List<String> categories1 = new ArrayList<>();
        categories1.add("misc");
        ShoppingProduct product1 = new ShoppingProduct(
                "StoreC", "cat3", "p3", "ItemA", "Desc", price1, quantity1, "", categories1
        );
        // Even for store-level, discount applies per product
        SimpleDiscount discount = new SimpleDiscount(DiscountType.STORE, 10, null,"COUPON3");

        // Act
        discount.ApplyCoupon("COUPON3");
        double discountedTotal = discount.calculate(product1,5);

        // Assert
        // 10% off of (10 * 5) = 45.0
        assertEquals(45.0, discountedTotal, 0.001);
    }
}
