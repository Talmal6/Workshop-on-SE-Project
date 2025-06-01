package com.SEGroup.Domain.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.*;
/*
 * Represents a product in a store, including its details, bidding information, and ratings.
 */
@Entity
@Table(name = "shopping_product")
public class ShoppingProduct {
    @Id
    @Column(name = "product_id")
    private final String productId;

    @Column(name = "catalog_id")
    private String catalogID;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "store_name")
    private final String storeName;

    @Column(name = "image_url")
    private final String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "user_id")
    @CollectionTable(name = "product_ratings", joinColumns = @JoinColumn(name = "product_id"))
    private final Map<String, Rating> ratings = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category")
    private final List<String> categories;;

    // Bids and auction
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private final List<Bid> bids;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Embedded
    private BuyingPolicy buyingPolicy;

    protected ShoppingProduct() {
        this.productId = null;
        this.storeName = null;
        this.imageUrl = null;
        this.bids = new ArrayList<>();
        this.categories = new ArrayList<>();
    }
    public ShoppingProduct(String storeName, String catalogID, String productId, String name,
                           String description, double price, int quantity, String imageUrl,List<String> categories) {
        this.storeName = storeName;
        this.catalogID = catalogID;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.bids = new ArrayList<>();
        this.buyingPolicy = new BuyingPolicy(1);
        this.imageUrl=imageUrl;
        this.ratings.clear();
        this.categories = categories;
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
    public void setAuction(Auction auction){
        this.auction=auction;
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
        // prevent duplicate bid
        boolean exists = bids.stream()
            .anyMatch(bid -> bid.getBidderEmail().equals(bidderEmail)
                && bid.getAmount() == amount);
        if (exists) {
            throw new IllegalArgumentException("Bid already exists");
        }
        bids.add(new Bid(bidderEmail, amount));
    }

    public void removeBid(String bidderEmail, double amount) {
        boolean removed = bids.removeIf(bid -> bid.getBidderEmail().equals(bidderEmail) && bid.getAmount() == amount );
        if (!removed) {
            throw new IllegalArgumentException("Bid not found");
        }
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
    public void closeAuction(){
        this.auction.closeAuction();
        this.auction = null;
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
        ratings.put(raterEmail, new Rating(score, review));
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
    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public boolean hasAuction() {
        return auction != null;
    }
    public void removeBid(String bidderEmail) {
        this.bids.removeIf(b -> b.getBidderEmail().equals(bidderEmail));
    }
    public Map<String, Rating> getAllRatings() {
        return Collections.unmodifiableMap(ratings);
    }

}