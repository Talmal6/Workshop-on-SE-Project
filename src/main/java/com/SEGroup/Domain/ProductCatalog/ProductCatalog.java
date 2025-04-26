package com.SEGroup.Domain.ProductCatalog;

import java.util.List;
import java.util.UUID;

public interface ProductCatalog {
    void addCatalogProduct(UUID productId, String name, String brand, String description, List<String> categories);

    void addStoreProductEntry(UUID productId, String storeName, UUID inStoreId, double price, int quantity, double rating);
    void deleteStoreProductEntry(UUID productId, String storeName, UUID inStoreId);
    void updateStoreProductEntry(UUID productId, String storeName, UUID inStoreId, Double price, Integer quantity, Double rating);

    List<Product> getAllProducts();
    List<StoreProductEntry> getAllProductsByCategory(String category);
    List<StoreProductEntry> search(String query, List<String> searchFilters);
}