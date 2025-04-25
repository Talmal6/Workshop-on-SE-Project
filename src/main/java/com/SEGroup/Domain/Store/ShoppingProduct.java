package com.SEGroup.Domain.Store;

import java.util.*;

public class ShoppingProduct {
    private final String productId;
    private String name;
    private double price;
    private int quantity;

    //bids and auction
    private final List<Bid> bids;
    private Auction auction;
    private BuyingPolicy buyingPolicy;

    public ShoppingProduct(String productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.bids = new ArrayList<>();
        this.buyingPolicy = new BuyingPolicy(1);
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Bidding logic
    public void addBid(String bidderEmail, double amount) {
        bids.add(new Bid(bidderEmail, amount));
    }

    public Optional<Bid> getHighestBid() {
        return bids.stream().max(Comparator.comparingDouble(Bid::getAmount));
    }
    public void startAuction(double startingPrice, Date endTime) {
        this.auction = new Auction(startingPrice, endTime);
    }

    public Auction getAuction() {
        return auction;
    }
}