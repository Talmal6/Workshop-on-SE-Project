package com.SEGroup.Domain.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.persistence.*;

/**
 * Represents a shopping cart that can contain multiple baskets, each associated
 * with a different store.
 */
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {
    // ← constructor left untouched
    protected ShoppingCart() {}

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    /**
     * ← one-to-many from cart → basket, cascaded on save(user)
     */
    @OneToMany(
      mappedBy       = "cart",
      cascade        = CascadeType.ALL,
      orphanRemoval  = true,
      fetch = FetchType.EAGER
    )
    @MapKey(name = "storeId")
    private final Map<String, Basket> storeToBasket = new ConcurrentHashMap<>();

    // ← constructor left untouched
    public ShoppingCart(String userId) {
        this.userId = userId;
    }

    public void add(String storeId, String productId, int qty) {
        storeToBasket
            .computeIfAbsent(storeId, k -> new Basket(k, this))
            .add(productId, qty);
    }

    public void changeQty(String storeId, String productId, int qty) {
        Basket basket = storeToBasket.get(storeId);
        if (basket == null) throw new IllegalArgumentException("basket not found");
        basket.change(productId, qty);
    }

    public Map<String, Basket> snapShot() {
        return Collections.unmodifiableMap(storeToBasket);
    }

    public void clear() {
        storeToBasket.values().forEach(Basket::clear);
        storeToBasket.clear();
    }
}
