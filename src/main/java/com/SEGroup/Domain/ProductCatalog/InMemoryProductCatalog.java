package com.SEGroup.Domain.ProductCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryProductCatalog implements ProductCatalog {

    private final Map<String, CatalogProduct> products = new HashMap<>();
    private final Map<String, List<StoreProductEntry>> catalogIdToStoreOffers = new HashMap<>();
    private final Map<String, List<String>> categoriesToProducts = new HashMap<>();
    private final Map<String, List<StoreProductEntry>> storeNameToStoreOffers = new HashMap<>();

    @Override
    public void addCatalogProduct(String catalogID, String name, String brand, String description, List<String> categories) {
        CatalogProduct product = new CatalogProduct(catalogID, name, brand, description);
        products.put(catalogID, product);

        for (String category : categories) {
            categoriesToProducts.computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>()).add(catalogID);
        }
    }

    @Override
    public void addStoreProductEntry(String catalogID, String storeName, String productID, double price, int quantity, double rating) {
        StoreProductEntry entry = new StoreProductEntry(catalogID, storeName, productID, price, quantity, rating);
        catalogIdToStoreOffers.computeIfAbsent(catalogID, k -> new ArrayList<>()).add(entry);
        storeNameToStoreOffers.computeIfAbsent(storeName, k -> new ArrayList<>()).add(entry);
    }

    @Override
    public void deleteStoreProductEntry(String catalogID, String storeName, String productID) {
        List<StoreProductEntry> catalogEntries = catalogIdToStoreOffers.getOrDefault(catalogID, new ArrayList<>());
        catalogEntries.removeIf(entry -> entry.getStoreName().equals(storeName) && entry.getProductID().equals(productID));

        List<StoreProductEntry> storeEntries = storeNameToStoreOffers.getOrDefault(storeName, new ArrayList<>());
        storeEntries.removeIf(entry -> entry.getCatalogID().equals(catalogID) && entry.getProductID().equals(productID));
    }

    @Override
    public void updateStoreProductEntry(String catalogID, String storeName, String productID, Double price, Integer quantity, Double rating) {
        List<StoreProductEntry> storeEntries = storeNameToStoreOffers.get(storeName);
        if (storeEntries != null) {
            for (StoreProductEntry entry : storeEntries) {
                if (entry.getCatalogID().equals(catalogID) && entry.getProductID().equals(productID)) {
                    if (price != null) entry.setPrice(price);
                    if (quantity != null) entry.setQuantity(quantity);
                    if (rating != null) entry.setRating(rating);
                }
            }
        }
    }

    @Override
    public List<CatalogProduct> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<StoreProductEntry> getAllProductsByCategory(String category) {
        List<String> catalogIDs = categoriesToProducts.getOrDefault(category.toLowerCase(), Collections.emptyList());
        return catalogIDs.stream()
                .flatMap(id -> catalogIdToStoreOffers.getOrDefault(id, Collections.emptyList()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreProductEntry> search(String query, List<String> searchFilters, String storeName, List<String> categories) {
        // Step 1: First pass - Filter CatalogProducts by name, brand, or description matching the query
        Set<String> firstPass = products.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()) ||
                                   product.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                                   product.getDescription().toLowerCase().contains(query.toLowerCase()))
                .map(CatalogProduct::getCatalogID)
                .collect(Collectors.toSet());
    
        // Step 2: Second pass - Find StoreProductEntries matching those CatalogProducts and matching search filters
        List<StoreProductEntry> secondPass = catalogIdToStoreOffers.entrySet().stream()
                .filter(entry -> firstPass.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(storeProductEntry -> storeProductEntry.matchesQuery(query, searchFilters))
                .collect(Collectors.toList());
    
        // Step 3: Third pass - If storeName is specified, filter entries by store name
        List<StoreProductEntry> thirdPass = secondPass;
        if (storeName != null && !storeName.isEmpty()) {
            thirdPass = thirdPass.stream()
                    .filter(entry -> entry.getStoreName().equalsIgnoreCase(storeName))
                    .collect(Collectors.toList());
        }
    
        // Step 4: Fourth pass - If categories are specified, filter entries by categories
        List<StoreProductEntry> fourthPass = thirdPass;
        if (categories != null && !categories.isEmpty()) {
            Set<String> validCatalogIds = categories.stream()
                    .filter(categoriesToProducts::containsKey)
                    .flatMap(category -> categoriesToProducts.get(category).stream())
                    .collect(Collectors.toSet());
    
            fourthPass = fourthPass.stream()
                    .filter(entry -> validCatalogIds.contains(entry.getCatalogID()))
                    .collect(Collectors.toList());
        }
    
        return fourthPass;
    }
    

    @Override
    public void isProductExist(String catalogID) throws Exception {
        if(!products.containsKey(catalogID)){
            throw new Exception("Product with catalog ID " + catalogID + " does not exist.");
        }
    }
}

