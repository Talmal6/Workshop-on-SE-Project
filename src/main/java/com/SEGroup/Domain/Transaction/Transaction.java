package com.SEGroup.Domain.Transaction;

import java.util.List;

/*
 * Represents a transaction in the system, including the items being purchased,
 * the total cost, the buyer's email, and the store name.
 */
public class Transaction {
    private List<String> itemsToTransact;
    private double cost;
    private String buyersEmail;
    private String storeName;

    // Constructor
    public Transaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName) {
        this.itemsToTransact = shoppingProductIds;
        this.cost = cost;
        this.buyersEmail = buyersEmail;
        this.storeName = storeName;
    }

    // Getters and setters
    public List<String> getItemsToTransact() {
        return itemsToTransact;
    }

    public void setItemsToTransact(List<String> itemsToTransact) {
        this.itemsToTransact = itemsToTransact;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getBuyersEmail() {
        return buyersEmail;
    }

    public void setBuyersEmail(String buyersEmail) {
        this.buyersEmail = buyersEmail;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String sellerStore) {
        this.storeName = sellerStore;
    }




}