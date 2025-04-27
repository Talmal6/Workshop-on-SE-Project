package com.SEGroup.DTO;

import java.util.List;

/**
 * Data Transfer Object representing a transaction.
 * It contains details about the items being transacted, the cost,
 * the buyer's email, and the seller's store information.
 */
public class TransactionDTO {

    public List<String> itemsToTransact;  // List of item IDs to be transacted
    public double cost;                    // Total cost of the transaction
    public String buyersEmail;             // Email address of the buyer
    public String sellerStore;             // Name of the store where the transaction occurred

    /**
     * Default constructor for TransactionDTO.
     */
    public TransactionDTO() {
    }

    /**
     * Constructs a new TransactionDTO with the specified details.
     *
     * @param itemsToTransact The list of item IDs to be transacted.
     * @param cost The total cost of the transaction.
     * @param buyersEmail The email address of the buyer.
     * @param sellerStore The name of the store where the transaction occurred.
     */
    public TransactionDTO(List<String> itemsToTransact, double cost, String buyersEmail, String sellerStore) {
        this.itemsToTransact = itemsToTransact;
        this.cost = cost;
        this.buyersEmail = buyersEmail;
        this.sellerStore = sellerStore;
    }

    /**
     * Retrieves the list of item IDs to be transacted.
     *
     * @return The list of items to be transacted.
     */
    public List<String> getItemsToTransact() {
        return itemsToTransact;
    }

    /**
     * Sets the list of item IDs to be transacted.
     *
     * @param itemsToTransact The list of items to be transacted.
     */
    public void setItemsToTransact(List<String> itemsToTransact) {
        this.itemsToTransact = itemsToTransact;
    }

    /**
     * Retrieves the total cost of the transaction.
     *
     * @return The total cost of the transaction.
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the total cost of the transaction.
     *
     * @param cost The new total cost of the transaction.
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Retrieves the email address of the buyer.
     *
     * @return The email address of the buyer.
     */
    public String getBuyersEmail() {
        return buyersEmail;
    }

    /**
     * Sets the email address of the buyer.
     *
     * @param buyersEmail The email address of the buyer.
     */
    public void setBuyersEmail(String buyersEmail) {
        this.buyersEmail = buyersEmail;
    }

    /**
     * Retrieves the name of the store where the transaction occurred.
     *
     * @return The name of the seller's store.
     */
    public String getSellerStore() {
        return sellerStore;
    }

    /**
     * Sets the name of the store where the transaction occurred.
     *
     * @param sellerStore The name of the store where the transaction occurred.
     */
    public void setSellerStore(String sellerStore) {
        this.sellerStore = sellerStore;
    }
}
