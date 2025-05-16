package com.SEGroup.DTO;

import java.util.Date;

/**
 * Data Transfer Object for exposing an Auction’s state to the UI.
 */
public class AuctionDTO {
    private String storeName;
    private String productId;
    private double startingPrice;
    private Double highestBid;
    private String highestBidder;
    private Date endTime;
    private long timeRemainingMillis;

    public AuctionDTO() {
        // Jackson / Vaadin etc. need a no-arg ctor
    }

    public AuctionDTO(String storeName,
                      String productId,
                      double startingPrice,
                      Double highestBid,
                      String highestBidder,
                      Date endTime,
                      long timeRemainingMillis) {
        this.storeName = storeName;
        this.productId = productId;
        this.startingPrice = startingPrice;
        this.highestBid = highestBid;
        this.highestBidder = highestBidder;
        this.endTime = endTime;
        this.timeRemainingMillis = timeRemainingMillis;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(Double highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getTimeRemainingMillis() {
        return timeRemainingMillis;
    }

    public void setTimeRemainingMillis(long timeRemainingMillis) {
        this.timeRemainingMillis = timeRemainingMillis;
    }

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "storeName='" + storeName + '\'' +
                ", productId='" + productId + '\'' +
                ", startingPrice=" + startingPrice +
                ", highestBid=" + highestBid +
                ", highestBidder='" + highestBidder + '\'' +
                ", endTime=" + endTime +
                ", timeRemainingMillis=" + timeRemainingMillis +
                '}';
    }
}