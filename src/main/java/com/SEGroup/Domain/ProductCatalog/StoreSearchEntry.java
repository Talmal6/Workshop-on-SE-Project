package com.SEGroup.Domain.ProductCatalog;

import java.util.List;

public class StoreSearchEntry {

    private final String catalogID;
    private final String storeName;
    private final String productID;
    private double price;
    private int quantity;
    private double rating;
    private final String name;

    public StoreSearchEntry(String catalogID, String storeName, String productID, double price, int quantity, double rating, String name) {
        this.catalogID = catalogID;
        this.storeName = storeName;
        this.productID = productID;
        this.price = price;
        this.quantity = quantity;
        this.rating = rating;
        this.name = name;
    }

    public String getCatalogID() {
        return catalogID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getProductID() {
        return productID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    /**
     * Checks if the product matches the given search query and filters.
     *
     * @param query         The search query.
     * @param searchFilters  The list of search filters.
     * @return true if the product matches the query and filters, false otherwise.
     */
    public boolean matchesQuery(String query, List<String> searchFilters) {
        for (String filter : searchFilters) {
            if (filter.startsWith("price<")) {
                double maxPrice = Double.parseDouble(filter.substring("price<".length()));
                if (this.price > maxPrice) return false;
            } else if (filter.startsWith("price>")) {
                double minPrice = Double.parseDouble(filter.substring("price>".length()));
                if (this.price < minPrice) return false;
            } else if (filter.startsWith("rating>")) {
                double minRating = Double.parseDouble(filter.substring("rating>".length()));
                if (this.rating < minRating) return false;
            } else if (filter.startsWith("quantity>") || filter.startsWith("quantity<")) {
                if (filter.startsWith("quantity>")) {
                    int minQuantity = Integer.parseInt(filter.substring("quantity>".length()));
                    if (this.quantity < minQuantity) return false;
                } else {
                    int maxQuantity = Integer.parseInt(filter.substring("quantity<".length()));
                    if (this.quantity > maxQuantity) return false;
                }
            }
        }
        return true;
    }
}