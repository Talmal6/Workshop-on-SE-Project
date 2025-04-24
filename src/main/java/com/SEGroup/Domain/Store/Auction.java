package com.SEGroup.Domain.Store;

import java.util.Date;

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

    public boolean isExpired() {
        return new Date().after(endTime);
    }

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

    public void closeAuction() {
        this.ended = true;
    }

    public boolean isEnded() {
        return ended || isExpired();
    }

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
