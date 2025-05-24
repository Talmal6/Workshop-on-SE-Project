package com.SEGroup.Domain.Conditions;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Store.ShoppingProduct;
import java.util.List;

public abstract class CompositeCondition extends Discount {
    protected final List<Condition> conditions;

    public CompositeCondition(List<Condition> conditions, DiscountType type, double percent, String scopeKey, String coupon) {
        super(type, percent, scopeKey, coupon);
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Conditions list cannot be null or empty");
        }
        this.conditions = conditions;
    }

    /**
     * Evaluate if the composite condition is satisfied by the given list of products and their amounts.
     * The concrete subclasses implement this according to their logic (AND, OR, XOR).
     *
     * @param products list of ShoppingProduct in the basket
     * @param amounts corresponding amounts per product, same indices as products
     * @return true if the composite condition is satisfied, false otherwise
     */
    public abstract boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts);

    /**
     * Calculate discounted price for a product and quantity in the cart.
     * Only applies discount if conditions are satisfied.
     */
    @Override
    public double calculate(ShoppingProduct product, int quantity) {
        // Convert single product to list format for condition checking
        List<ShoppingProduct> products = List.of(product);
        List<Integer> amounts = List.of(quantity);

        // Only apply discount if conditions are satisfied and discount is active
        if (!isActive() || !isSatisfiedBy(products, amounts)) {
            return product.getPrice() * quantity; // No discount applied
        }

        // Apply discount based on type and scope
        double basePrice = product.getPrice() * quantity;
        double discountAmount = 0.0;

        switch (getType()) {
            case STORE:
                // Store-wide discount applies to all products
                discountAmount = basePrice * (getPercent() / 100.0);
                break;
            case CATEGORY:
                // Category discount applies only if product is in the specified category
                if (product.getCategories().contains(getScopeKey())) {
                    discountAmount = basePrice * (getPercent() / 100.0);
                }
                break;
            case PRODUCT:
                // Product discount applies only if it's the specific product
                if (product.getProductId().equals(getScopeKey())) {
                    discountAmount = basePrice * (getPercent() / 100.0);
                }
                break;
        }

        return basePrice - discountAmount;
    }
    public double calculateWithBasket(ShoppingProduct targetProduct,
                                      int quantity,
                                      List<ShoppingProduct> basketProducts,
                                      List<Integer> basketQuantities) {
        if (!isActive() || !isSatisfiedBy(basketProducts, basketQuantities)) {
            return targetProduct.getPrice() * quantity;
        }

        double basePrice = targetProduct.getPrice() * quantity;
        boolean applies = switch (getType()) {
            case STORE -> true;
            case CATEGORY -> targetProduct.getCategories().contains(getScopeKey());
            case PRODUCT -> targetProduct.getProductId().equals(getScopeKey());
        };

        if (applies) {
            double discountAmount = basePrice * (getPercent() / 100.0);
            return basePrice - discountAmount;
        } else {
            return basePrice;
        }
    }
    public List<Condition> getConditions() {
        return conditions;
    }
}