package com.SEGroup.UnitTests.Discount.Numerical;

import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.SumDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Store.ShoppingProduct;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SumDiscountTest {

    @Test
    public void shouldCalculateSumDiscountForSingleProduct_WithMultipleDiscounts() {
        List<String> categories = List.of("electronics");

        ShoppingProduct product = new ShoppingProduct(
                "StoreA", "cat1", "p1", "Gadget", "desc", 100.0, 2, "", categories
        ); // total 200

        SimpleDiscount discount10 = new SimpleDiscount(DiscountType.STORE, 10, null, null); // 10% discount
        SimpleDiscount discount5 = new SimpleDiscount(DiscountType.CATEGORY, 5, "electronics", null); // 5% discount

        SumDiscount sumDiscount = new SumDiscount(List.of(discount10, discount5));

        double totalDiscount = sumDiscount.calculate(product, product.getQuantity());

        // Expected discount: 10% of 200 = 20 + 5% of 200 = 10 => total 30
        assertEquals(30.0, totalDiscount, 0.001);
    }

    @Test
    public void shouldCalculateSumDiscount_WithInactiveDiscounts() {
        List<String> categories = List.of("books");

        ShoppingProduct product = new ShoppingProduct(
                "StoreB", "cat2", "p2", "Book", "desc", 50.0, 1, "", categories
        );

        SumDiscount sumDiscount = new SumDiscount(List.of());

        double totalDiscount = sumDiscount.calculate(product, product.getQuantity());

        assertEquals(0.0, totalDiscount, 0.001);
    }

    @Test
    public void shouldCalculateSumDiscounts_WithInactiveDiscounts() {
        List<String> categories = List.of("toys");

        ShoppingProduct product = new ShoppingProduct(
                "StoreC", "cat3", "p3", "Toy", "desc", 40.0, 5, "", categories
        ); // total 200

        SimpleDiscount discountActive = new SimpleDiscount(DiscountType.STORE, 10, null, null);
        SimpleDiscount discountInactive = new SimpleDiscount(DiscountType.CATEGORY, 5, "toys", "COUPON1");

        discountInactive.ApplyCoupon("COUPON1");

        SumDiscount sumDiscount = new SumDiscount(List.of(discountActive, discountInactive));

        double totalDiscount = sumDiscount.calculate(product, product.getQuantity());

        // Both discounts are active, sum of 10% and 5% of 200 = 30
        assertEquals(30.0, totalDiscount, 0.001);
    }

}
