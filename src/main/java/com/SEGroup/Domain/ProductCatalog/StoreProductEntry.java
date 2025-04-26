package com.SEGroup.Domain.ProductCatalog;

import java.util.List;
import java.util.UUID;

public class StoreProductEntry {

    private final UUID productId;
    private final String storeName;
    private final UUID inStoreId;
    private double price;
    private int quantity;
    private double rating;

    public StoreProductEntry(UUID productId, String storeName, UUID inStoreId, double price, int quantity, double rating) {
        this.productId = productId;
        this.storeName = storeName;
        this.inStoreId = inStoreId;
        this.price = price;
        this.quantity = quantity;
        this.rating = rating;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getStoreName() {
        return storeName;
    }

    public UUID getInStoreId() {
        return inStoreId;
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