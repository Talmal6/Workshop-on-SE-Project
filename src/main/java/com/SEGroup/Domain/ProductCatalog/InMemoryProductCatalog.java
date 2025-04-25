package com.SEGroup.Domain.ProductCatalog;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import java.util.stream.Collectors;

public class InMemoryProductCatalog implements ProductCatalog {

    private final Map<UUID, Product> products = new HashMap<>();
    private final Map<UUID, List<StoreProductEntry>> catalogIdToStoreOffers = new HashMap<>();
    private final Map<String, List<UUID>> categoriesToProducts = new HashMap<>();
    private final Map<String, List<StoreProductEntry>> storeNameToStoreOffers = new HashMap<>();

    @Override
    public void addCatalogProduct(UUID productId, String name, String brand, String description, List<String> categories) {
        Product product = new Product(productId, name, brand, description);
        products.put(productId, product);

        for (String category : categories) {
            categoriesToProducts.computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>()).add(productId);
        }
    }

    @Override
    public void addStoreProductEntry(UUID productId, String storeName, UUID inStoreId, double price, int quantity, double rating) {
        StoreProductEntry entry = new StoreProductEntry(productId, storeName, inStoreId, price, quantity, rating);
        catalogIdToStoreOffers.computeIfAbsent(productId, k -> new ArrayList<>()).add(entry);
        storeNameToStoreOffers.computeIfAbsent(storeName, k -> new ArrayList<>()).add(entry);
    }

    @Override
    public void deleteStoreProductEntry(UUID productId, String storeName, UUID inStoreId) {
        List<StoreProductEntry> catalogEntries = catalogIdToStoreOffers.getOrDefault(productId, new ArrayList<>());
        catalogEntries.removeIf(entry -> entry.getStoreName().equals(storeName) && entry.getInStoreId().equals(inStoreId));

        List<StoreProductEntry> storeEntries = storeNameToStoreOffers.getOrDefault(storeName, new ArrayList<>());
        storeEntries.removeIf(entry -> entry.getProductId().equals(productId) && entry.getInStoreId().equals(inStoreId));
    }

    @Override
    public void updateStoreProductEntry(UUID productId, String storeName, UUID inStoreId, Double price, Integer quantity, Double rating) {
        List<StoreProductEntry> storeEntries = storeNameToStoreOffers.get(storeName);
        if (storeEntries != null) {
            for (StoreProductEntry entry : storeEntries) {
                if (entry.getProductId().equals(productId) && entry.getInStoreId().equals(inStoreId)) {
                    if (price != null) entry.setPrice(price);
                    if (quantity != null) entry.setQuantity(quantity);
                    if (rating != null) entry.setRating(rating);
                }
            }
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<StoreProductEntry> getAllProductsByCategory(String category) {
        List<UUID> productIds = categoriesToProducts.getOrDefault(category.toLowerCase(), Collections.emptyList());
        return productIds.stream()
                .flatMap(id -> catalogIdToStoreOffers.getOrDefault(id, Collections.emptyList()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreProductEntry> search(String query, List<String> searchFilters) {
        Set<UUID> matchingProductIds = products.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()) ||
                                    product.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                                    product.getDescription().toLowerCase().contains(query.toLowerCase()))
                .map(Product::getProductId)
                .collect(Collectors.toSet());

        return catalogIdToStoreOffers.entrySet().stream()
                .filter(entry -> matchingProductIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(storeProductEntry -> storeProductEntry.matchesQuery(query, searchFilters))
                .collect(Collectors.toList());
    }
}

