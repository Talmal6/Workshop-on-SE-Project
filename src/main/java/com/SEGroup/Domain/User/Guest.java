package com.SEGroup.Domain.User;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "guests")
public class Guest {
    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private Instant issuedAt;

    @Transient  // Not persisted—per session/in memory only!
    private ShoppingCart cart;

    // --- JPA needs this ---
    protected Guest() {}

    // --- Your desired constructor ---
    public Guest(String id, Instant issuedAt, ShoppingCart cart) {
        this.id = id;
        this.issuedAt = issuedAt;
        this.cart = cart;
    }

    // --- Record-style accessors ---
    public String id() { return id; }
    public Instant issuedAt() { return issuedAt; }
    public ShoppingCart cart() { return cart == null ? new ShoppingCart(id) : cart; }
    public void setCart(ShoppingCart cart) { this.cart = cart; }

    // Optional JavaBean-style getters/setters
    public String getId() { return id; }
    public Instant getIssuedAt() { return issuedAt; }
    public ShoppingCart getCart() { return cart; }
    public void setId(String id) { this.id = id; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
}
