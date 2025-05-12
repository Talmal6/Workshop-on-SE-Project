package com.SEGroup.DTO;

import java.util.List;

/**
 * Data Transfer Object representing a store.
 * It contains the store's details such as ID, name, founder's email, active status, balance,
 * average rating, and the list of products available in the store.
 */
public class StoreDTO {

    private int id;
    private String name;
    private String founderEmail;
    private boolean isActive;
    private double balance;
    private final double avgRating;
    private List<ShoppingProductDTO> products;
    private String description;

    /**
     * Constructs a new StoreDTO with the specified details.
     *
     * @param id The unique ID of the store.
     * @param name The name of the store.
     * @param founderEmail The email address of the store founder.
     * @param isActive The status of the store, whether it is active or not.
     * @param balance The balance of the store.
     * @param products The list of products available in the store.
     * @param avgRating The average rating of the store.
     * @param description The description of the store.
     */
    public StoreDTO(int id, String name, String founderEmail, boolean isActive, double balance,
                    List<ShoppingProductDTO> products, double avgRating, String description) {
        this.id = id;
        this.name = name;
        this.founderEmail = founderEmail;
        this.isActive = isActive;
        this.balance = balance;
        this.products = products;
        this.avgRating = avgRating;
        this.description = description;
    }

    /**
     * Retrieves the average rating of the store.
     *
     * @return The average rating of the store.
     */
    public double getAvgRating() {
        return avgRating;
    }

    /**
     * Retrieves the ID of the store.
     *
     * @return The unique ID of the store.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the store.
     *
     * @param id The ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves the name of the store.
     *
     * @return The name of the store.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the store.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the email address of the store founder.
     *
     * @return The email address of the founder.
     */
    public String getFounderEmail() {
        return founderEmail;
    }

    /**
     * Sets the founder's email address.
     *
     * @param founderEmail The email address to set.
     */
    public void setFounderEmail(String founderEmail) {
        this.founderEmail = founderEmail;
    }

    /**
     * Retrieves the active status of the store.
     *
     * @return true if the store is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of the store.
     *
     * @param active The active status to set.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Retrieves the balance of the store.
     *
     * @return The balance of the store.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance of the store.
     *
     * @param balance The balance to set.
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Retrieves the list of products available in the store.
     *
     * @return A list of ShoppingProductDTO objects representing the products.
     */
    public List<ShoppingProductDTO> getProducts() {
        return products;
    }

    /**
     * Sets the list of products available in the store.
     *
     * @param products The list of products to set.
     */
    public void setProducts(List<ShoppingProductDTO> products) {
        this.products = products;
    }

    /**
     * Gets the description of the store.
     *
     * @return The description of the store.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the store.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}