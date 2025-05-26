package com.SEGroup.DTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for shopping products.
 * Represents a product in a store with all its details.
 */
public class ShoppingProductDTO {
    private final String storeName;
    private final String catalogId;
    private final String productId;
    private final String name;
    private final String description;
    private final double price;
    private final int quantity;
    private final double avgRating;
    private String imageUrl;
    private List<String> categories = new ArrayList<>();

    /**
     * Constructs a new ShoppingProductDTO with the specified details.
     *
     * @param storeName   The name of the store.
     * @param catalogId   The catalog ID of the product.
     * @param productId   The product ID.
     * @param name        The name of the product.
     * @param description The description of the product.
     * @param price       The price of the product.
     * @param quantity    The quantity of the product.
     * @param avgRating   The average rating of the product.
     * @param imageUrl    The URL of the product image.
     */
    public ShoppingProductDTO(String storeName,
                              String catalogId,
                              String productId,
                              String name,
                              String description,
                              double price,
                              int quantity,
                              double avgRating,
                              String imageUrl,
                              List<String> categories) {
        this.storeName   = storeName;
        this.catalogId   = catalogId;
        this.productId   = productId;
        this.name        = name;
        this.description = description;
        this.price       = price;
        this.quantity    = quantity;
        this.avgRating   = avgRating;
        this.imageUrl    = imageUrl;
        this.categories = categories;
    }

    // --- Getters & setters --------------------------------

    public String getStoreName() {
        return storeName;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAvgRating() {
        return avgRating;
    }



    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Replace the entire category list (called from your presenter).
     */
    public void setCategories(List<String> categories) {


            this.categories = (categories == null)
                    ? new ArrayList<>()
                    : new ArrayList<>(categories);
            this.categories.clear();
            this.categories.addAll(categories);

    }

    /**
     * Add one category at a time.
     */
    public void addCategory(String category) {
        if (category != null && !category.isBlank()) {
            this.categories.add(category);
        }
    }


    public String getCatalogID(){
        return catalogId;
    }

    public String getImageUrl(){
        return imageUrl;
    }
}
