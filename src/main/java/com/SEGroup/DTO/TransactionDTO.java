package com.SEGroup.DTO;
import java.util.List;




public class TransactionDTO {
    public List<String> itemsToTransact;
    public double cost;
    public String buyersEmail;
    public String sellerStore;

    // Default constructor
    public TransactionDTO() {
    }

    // Parameterized constructor
    public TransactionDTO(List<String> itemsToTransact, double cost, String buyersEmail, String sellerStore) {
        this.itemsToTransact = itemsToTransact;
        this.cost = cost;
        this.buyersEmail = buyersEmail;
        this.sellerStore = sellerStore;
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