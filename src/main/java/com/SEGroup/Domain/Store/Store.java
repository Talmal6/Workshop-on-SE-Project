package com.SEGroup.Domain.Store;

import java.util.*;

public class Store {
    //fields
    private static int idCounter = 0;
    private final int id;
    private String name;
    private String ownerEmail;
    private boolean isActive;
    private double balance;

    //products and reviews
    private final Map<String, ShoppingProduct> products;
    private final List<Integer> ratings = new ArrayList<>();
    private final List<String> reviews = new ArrayList<>();

    // Disocunt and policy fields
    private PurchasePolicy purchasePolicy;
    private List<Discount> discounts;

    public Store(String name, String ownerEmail) {
        //field
        this.id = ++idCounter;
        this.name = name;
        this.ownerEmail = ownerEmail;
        this.isActive = true;
        this.balance = 0.0;

        //product and reviews
        this.products = new HashMap<>();
        this.ratings.clear();
        this.reviews.clear();

        // Disocunt and policy fields
        this.purchasePolicy = new PurchasePolicy(0,0);
        this.discounts = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getOwnerEmail() { return ownerEmail; }
    public boolean isActive() { return isActive; }
    public double getBalance() { return balance; }

    // Setters
    public void setName(String name) {
        if (name != null && !name.isEmpty())
            this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void close() {
        this.isActive = false;
    }
    public void open(){
        this.isActive = true;
    }

    // Products (ShoppingProduct)
    public void addProduct(ShoppingProduct product) {
        products.put(product.getProductId(), product);
    }

    public ShoppingProduct getProduct(String productId) {
        return products.get(productId);
    }

    public void removeProduct(String productId) {
        products.remove(productId);
    }

    public Collection<ShoppingProduct> getAllProducts() {
        return products.values();
    }
    // Store rating
    public void rateStore(int rating, String review) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        ratings.add(rating);
        if (review != null && !review.isBlank()) {
            reviews.add(review);
        }
    }
    public void addToBalance(double amount){
        this.balance += amount;
    }
    public boolean submitBidToShoppingItem(String itemName, double bidAmount, String bidderEmail) {
        ShoppingProduct product = products.get(itemName);

        if (product == null) {
            return false; // לא קיים מוצר כזה
        }

        if (bidAmount <= 0 || bidderEmail == null || bidderEmail.isBlank()) {
            return false; // הצעה לא חוקית
        }

        product.addBid(bidderEmail, bidAmount);
        return true;
    }

    public boolean submitAuctionOffer(String itemName, double offerAmount, String bidderEmail) {
        // Logic to submit an auction offer for an item
        return false; // Return true if the offer is successfully submitted
    }
    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", isActive=" + isActive +
                ", balance=" + balance +
                ", products=" + products.keySet() +
                '}';
    }
}
