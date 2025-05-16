package com.SEGroup.DTO;

import java.util.List;

/**
 * Data Transfer Object representing a store.
 * It contains the store's details such as ID, name, founder's email, active status, balance,
 * average rating, and the list of products available in the store.
 */
public class StoreDTO {

    public int id;
    public String name;
    public String founderEmail;
    public boolean isActive;
    public double balance;
    private final double avgRating;
    public List<ShoppingProductDTO> products;
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
     * Retrieves the name of the store.
     *
     * @return The name of the store.
     */
    public String getName() {
        return name;
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
     * Retrieves the active status of the store.
     *
     * @return true if the store is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
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
     * Retrieves the list of products available in the store.
     *
     * @return A list of ShoppingProductDTO objects representing the products.
     */
    public List<ShoppingProductDTO> getProducts() {
        return products;
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
