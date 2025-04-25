package com.SEGroup.Domain;

import java.util.List;


public class Transaction {
    private List<String> itemsToTransact;
    private double cost;
    private String buyersEmail;
    private String sellerStore;

    // Constructor
    public Transaction(List<String> shoppingProductIds, double cost, String buyersEmail, String store) {
        this.itemsToTransact = shoppingProductIds;
        this.cost = cost;
        this.buyersEmail = buyersEmail;
        this.sellerStore = store;
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

    public String getSellerStore() {
        return sellerStore;
    }

    public void setSellerStore(String sellerStore) {
        this.sellerStore = sellerStore;
    }
}