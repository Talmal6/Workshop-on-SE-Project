package com.SEGroup.Domain.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents a shopping basket for a specific store.
 * The basket contains products and their quantities.
 */
@Entity
@Table(name = "basket")
public class Basket {
    // ← constructor left untouched
    protected Basket() {}

    @Id
    @Column(name = "store_id", nullable = false, updatable = false)
    private String storeId;

    // ← annotate the JSON map so Hibernate knows how to persist it
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "product2qty", columnDefinition = "JSON")
    private final Map<String, Integer> product2qty = new ConcurrentHashMap<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private ShoppingCart cart;

    // ← constructor left untouched
    public Basket(String storeId, ShoppingCart cart) {
        this.storeId = storeId;
        this.cart    = cart;
    }

    public void add(String pid, int q) {
        product2qty.merge(pid, q, Integer::sum);
    }

    public void change(String pid, int q) {
        if (q == 0) product2qty.remove(pid);
        else        product2qty.put(pid, q);
    }

    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(product2qty);
    }

    public String storeId() {
        return storeId;
    }

    public void clear() {
        product2qty.clear();
    }
}
