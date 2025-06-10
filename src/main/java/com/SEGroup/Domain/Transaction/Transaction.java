package com.SEGroup.Domain.Transaction;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transaction in the system.
 * Includes purchased items, cost, buyer's email, and the store name.
 */
@Entity(name = "transactions")
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transaction_items", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "product_id")
    private List<String> itemsToTransact = new ArrayList<>();

    @Column(name = "cost", nullable = false)
    private double cost;

    @Column(name = "buyers_email", nullable = false)
    private String buyersEmail;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    // Empty constructor for JPA
    protected Transaction() {}

    /**
     * Constructor to create a new Transaction instance.
     *
     * @param shoppingProductIds List of purchased product IDs.
     * @param cost Total cost of the transaction.
     * @param buyersEmail Buyer's email address.
     * @param storeName Name of the store where purchase occurred.
     */
    public Transaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName) {
        this.itemsToTransact = shoppingProductIds;
        this.cost = cost;
        this.buyersEmail = buyersEmail;
        this.storeName = storeName;
    }

    public int getId() {
        return id;
    }

    // Needed for InMemoryTransactionData
    public void setId(int id) {
        this.id = id;
    }

    public List<String> getItemsToTransact() {
        return List.copyOf(itemsToTransact);
    }

    public void setItemsToTransact(List<String> itemsToTransact) {
        this.itemsToTransact = new ArrayList<>(itemsToTransact);
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

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
