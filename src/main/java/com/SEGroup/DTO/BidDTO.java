package com.SEGroup.DTO;

public class BidDTO {
    private Integer id;
    private String originalBidderEmail; // Always the costumer who Initially placed the bid
    private String product;
    private double price;
    private BidState state;
    private String storeName;

    public BidDTO(Integer BidId , String bidderEmail, String product, double amount, BidState state, String storeName) {
        if (bidderEmail == null || bidderEmail.isEmpty())
            throw new IllegalArgumentException("Bidder email cannot be null or empty");
        if (product == null || product.isEmpty())
            throw new IllegalArgumentException("Product cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Bid amount must be positive");

        this.originalBidderEmail = bidderEmail;
        this.product = product;
        this.price = amount;
        this.id = BidId;
        this.state = state;
        this.storeName = storeName;
    }

    public String getOriginalBidderEmail() {
        return originalBidderEmail;
    }

    public String getProductId() {
        return product;
    }

    public double getPrice() {
        return price;
    }

    public BidState getState() {
        return state;
    }

    public Integer getId() {
        return id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Bid amount must be positive");
        }
        this.price = price;
    }

    public void setState(BidState state) {
        if (state == null) {
            throw new IllegalArgumentException("Bid state cannot be null");
        }
        this.state = state;
    }

    @Override
    public String toString() {
        return "Bid{email='" + originalBidderEmail + "', product='" + product + "', amount=" + price + ", state="
                + state + ", id=" + id + '}';
    }
}
