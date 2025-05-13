package com.SEGroup.Domain.Store;

/**
 * Represents a bid made by a user in an auction.
 */
public class Bid {
    private final String bidderEmail;
    private final double amount;
    private int quantity;

    public Bid(String bidderEmail, double amount,Integer quantity) {
        if (bidderEmail == null || bidderEmail.isEmpty())
            throw new IllegalArgumentException("Bidder email cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Bid amount must be positive");
        if (quantity <= 0)
            throw new IllegalArgumentException("Bid quantity must be positive");

        this.bidderEmail = bidderEmail;
        this.amount = amount;
        this.quantity = quantity;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public double getAmount() {
        return amount;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Bid{email='" + bidderEmail + "', amount=" + amount + '}';
    }
}
