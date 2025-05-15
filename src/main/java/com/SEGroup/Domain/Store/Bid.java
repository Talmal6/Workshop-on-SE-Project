package com.SEGroup.Domain.Store;

/**
 * Represents a bid made by a user in an auction.
 */
public class Bid {
    private final String bidderEmail;
    private final double amount;

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
