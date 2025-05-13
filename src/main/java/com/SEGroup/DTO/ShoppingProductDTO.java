package com.SEGroup.DTO;

/**
 * Data Transfer Object representing a shopping product.
 * It contains details such as the product's ID, catalog ID, name, description, price,
 * quantity, category, store name, and average rating.
 */
public class ShoppingProductDTO {

    private String productId;
    private String catalogID;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String category;
    private String storeName;
    private double avgRating;

    /**
     * Constructs a new ShoppingProductDTO with the specified details.
     *
     * @param storeName The name of the store selling the product.
     * @param catalogID The catalog ID of the product.
     * @param productId The unique ID of the product.
     * @param name The name of the product.
     * @param description A description of the product.
     * @param price The price of the product.
     * @param quantity The quantity available for sale.
     * @param avgRating The average rating of the product.
     */
    public ShoppingProductDTO(String storeName, String catalogID, String productId, String name, String description, double price, int quantity, double avgRating) {
        this.productId = productId;
        this.catalogID = catalogID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.storeName = storeName;
        this.avgRating = avgRating;
    }

    /**
     * Retrieves the catalog ID of the product.
     *
     * @return The catalog ID of the product.
     */
    public String getCatalogID() {
        return catalogID;
    }

    /**
     * Retrieves the product ID.
     *
     * @return The product ID.
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     *
     * @param productId The new product ID to set.
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Retrieves the name of the product.
     *
     * @return The name of the product.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the product.
     *
     * @param name The new name of the product.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the price of the product.
     *
     * @return The price of the product.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of the product.
     *
     * @param price The new price of the product.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Retrieves the quantity of the product available for sale.
     *
     * @return The quantity of the product.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the product available for sale.
     *
     * @param quantity The new quantity of the product.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Retrieves the description of the product.
     *
     * @return The description of the product.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the product.
     *
     * @param description The new description of the product.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the category of the product.
     *
     * @return The category of the product.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the product.
     *
     * @param category The new category of the product.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Retrieves the store name where the product is sold.
     *
     * @return The store name.
     */
    public String getStoreName() {
        return storeName;
    }

    /**
     * Sets the store name where the product is sold.
     *
     * @param storeName The new store name.
     */
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    /**
     * Retrieves the average rating of the product.
     *
     * @return The average rating of the product.
     */
    public double getAvgRating() {
        return avgRating;
    }
}
