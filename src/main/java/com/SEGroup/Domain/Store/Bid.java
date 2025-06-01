package com.SEGroup.Domain.Store;

import jakarta.persistence.*;

/**
 * Represents a bid made by a user in an auction.
 */
@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "bidder_email", nullable = false)
    private String bidderEmail;

    @Column(name = "amount", nullable = false)
    private double amount;

    // Constructors
    protected Bid() {
        // Required by JPA
    }

    public Bid(String bidderEmail, double amount) {
        if (bidderEmail == null || bidderEmail.isEmpty())
            throw new IllegalArgumentException("Bidder email cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Bid amount must be positive");
        this.bidderEmail = bidderEmail;
        this.amount = amount;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public double getAmount() {
        return amount;
    }


    @Override
    public String toString() {
        return "Bid{email='" + bidderEmail + "', amount=" + amount + '}';
    }
}
