package com.SEGroup.Domain.ProductCatalog;

/**
 * Represents the options for searching products in the catalog.
 * This class contains various filters that can be applied to a product search.
 */
public class ProductSearchOptions {
    private String text;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String brand;

    // Getters and setters

    /**
     * Constructs a new ProductSearchOptions instance with default values.
     */
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

}