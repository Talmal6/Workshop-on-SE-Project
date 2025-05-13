package com.SEGroup.Domain.ProductCatalog;

import java.util.List;

/**
 * Represents a product in the catalog with details such as ID, name, brand, and description.
 */
public class CatalogProduct {
    private final String catalogID;
    private final String name;
    private final List<String> categories;


    public CatalogProduct(String catalogID, String name, List<String> categories) {
        this.catalogID = catalogID;
        this.name      = name;
        this.categories = categories;
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

    public List<String> getCategories() {
        return categories;
    }

}