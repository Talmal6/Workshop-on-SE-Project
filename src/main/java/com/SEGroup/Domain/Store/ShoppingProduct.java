package com.SEGroup.Domain.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Represents a product in a store, including its details, bidding information, and ratings.
 */
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

    public void setDescription(String description) {
        this.description = description;
    }

    // Bidding logic
    public void addBid(String bidderEmail, double amount) {
        bids.add(new Bid(bidderEmail, amount));
    }

    public Optional<Bid> getHighestBid() {
        return bids.stream().max(Comparator.comparingDouble(Bid::getAmount));
    }
    /*
        * Starts an auction for the product with a given starting price and end time.
        *
        * @param startingPrice The starting price of the auction.
        * @param endTime     The end time of the auction.
     */
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
    /*
        * Adds a rating for the product from a user.
        * @param raterEmail The email of the user giving the rating.
        * @param score      The rating score (1-5).
        * @param review     The review text.
        * @throws IllegalArgumentException if the score is not between 1 and 5.
     */
    public void addRating(String raterEmail , int score , String review ) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating msut be 1-5 ");

        }
        ratings.put(raterEmail, new Store.Rating(score, review));
    }
    
    //rateStore
    /*
        * Retrieves the rating of the store.
        * @return The rating of the store by averaging all ratings.

     */
    public double averageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.values().stream()
                .mapToInt(r -> r.score)
                .average()
                .orElse(0.0);
    }
}