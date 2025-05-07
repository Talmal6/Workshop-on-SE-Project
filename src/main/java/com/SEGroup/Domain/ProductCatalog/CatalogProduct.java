package com.SEGroup.Domain.ProductCatalog;

/**
 * Represents a product in the catalog with details such as ID, name, brand, and description.
 */
public class CatalogProduct {
    private final String catalogID;
    private final String name; // Example: Iphone 13
    private final String brand; // Example: Apple
    private final String description; // IOS PHONE

    /**
     * Constructs a new CatalogProduct instance.
     *
     * @param catalogID   The unique identifier for the catalog product.
     * @param name        The name of the product.
     * @param brand       The brand of the product.
     * @param description A brief description of the product.
     */
    public CatalogProduct(String catalogID, String name, String brand, String description) {
        this.catalogID = catalogID;
        this.name = name;
        this.brand = brand;
        this.description = description;
    }

    /**
     * Gets the unique identifier of the catalog product.
     *
     * @return The catalog ID.
     */
    public String getCatalogID() {
        return catalogID;
    }

    /**
     * Gets the name of the product.
     *
     * @return The product name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the brand of the product.
     *
     * @return The product brand.
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Gets the description of the product.
     *
     * @return The product description.
     */
    public String getDescription() {
        return description;
    }

}