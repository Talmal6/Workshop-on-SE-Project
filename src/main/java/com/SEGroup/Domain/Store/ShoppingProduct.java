package com.SEGroup.Domain.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShoppingProduct {
    private final String productId;
    private String catalogID;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private final String storeName;
    private final Map<String, Store.Rating> ratings = new HashMap<>();

    // Bids and auction
    private final List<Bid> bids;
    private Auction auction;
    private BuyingPolicy buyingPolicy;

    public ShoppingProduct(String storeName, String catalogID, String productId, String name,
                           String description, double price, int quantity) {
        this.storeName = storeName;
        this.catalogID = catalogID;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.bids = new ArrayList<>();
        this.buyingPolicy = new BuyingPolicy(1);
        this.ratings.clear();
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getCatalogID() {
        return catalogID;
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

    public String getStoreName(){
        return storeName;
    }


    public String getDescription(){
        return description;
    }
    public void addRating(String raterEmail , int score , String review ) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating msut be 1-5 ");

        }
        ratings.put(raterEmail, new Store.Rating(score, review));

    }
    //rateStore
    public double averageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.values().stream()
                .mapToInt(r -> r.score)
                .average()
                .orElse(0.0);
    }
}