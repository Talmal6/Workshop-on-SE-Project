package com.SEGroup.Domain.ProductCatalog;

import java.util.List;

public interface ProductCatalog {
    void addCatalogProduct(String catalogID, String name, String brand, String description, List<String> categories);

    void addStoreProductEntry(String catalogID, String storeName, String productID, double price, int quantity, double rating , String name);
    void deleteStoreProductEntry(String catalogID, String storeName, String productID);
    void updateStoreProductEntry(String catalogID, String storeName, String productID, Double price, Integer quantity, Double rating);

    List<CatalogProduct> getAllProducts();
    List<StoreSearchEntry> getAllProductsByCategory(String category);
    List<StoreSearchEntry> search(String query, List<String> searchFilters, String storeName, List<String> categories);

    void isProductExist(String catalogID) throws Exception;
}