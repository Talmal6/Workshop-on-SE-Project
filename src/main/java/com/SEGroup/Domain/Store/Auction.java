package com.SEGroup.Domain.Store;

import java.util.Date;

/**
 * Represents an auction for a product with a starting price and an end time.
 */
public class Auction {
    private final double startingPrice;
    private final Date endTime;
    private Bid highestBid;
    private boolean ended;

    public Auction(double startingPrice, Date endTime) {
        this.startingPrice = startingPrice;
        this.endTime = endTime;
        this.highestBid = null;
        this.ended = false;
    }

    /**
     * Checks if the auction has expired.
     *
     * @return true if the auction has expired, false otherwise.
     */
    public boolean isExpired() {
        return new Date().after(endTime);
    }

    /**
     * Gets the remaining time for the auction.
     *
     * @return the remaining time in milliseconds.
     */
    public boolean submitBid(String bidderEmail, double amount) {
        if (ended || isExpired()) {
            this.ended = true;
            return false;
        }

        double currentHighest = (highestBid != null) ? highestBid.getAmount() : startingPrice;
        if (amount > currentHighest) {
            this.highestBid = new Bid(bidderEmail, amount);
            return true;
        }

        return false;
    }

    /**
     * Closes the auction.
     */
    public void closeAuction() {
        this.ended = true;
    }

    /**
     * Checks if the auction has ended.
     *
     * @return true if the auction has ended, false otherwise.
     */
    public boolean isEnded() {
        return ended || isExpired();
    }

    /**
     * Gets the highest bid for the auction.
     *
     * @return the highest bid.
     */
    public Bid getHighestBid() {
        return highestBid;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public Date getEndTime() {
        return endTime;
    }
}
