package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In‐memory implementation of ProductCatalogData.
 * Only active when “mem” profile is selected.
 */
@Repository
@Profile("mem")
public class InMemoryProductCatalogData implements ProductCatalogData {

    // Exactly the same collections you had before for in-memory
    private final Map<String, CatalogProduct> catalogIDtoCatalogProduct = new HashMap<>();
    private final Map<String, List<StoreSearchEntry>> catalogIdToStoreOffers = new HashMap<>();
    private final Map<String, List<String>> categoriesToProducts = new HashMap<>();
    private final Map<String, List<StoreSearchEntry>> storeNameToStoreOffers = new HashMap<>();

    @Override
    public void addCatalogProduct(String catalogID, String name, String brand,
                                  String description, List<String> categories) {
        CatalogProduct product = new CatalogProduct(catalogID, name, brand, description, categories);
        catalogIDtoCatalogProduct.put(catalogID, product);

        // Register categories → catalogID
        for (String category : categories) {
            categoriesToProducts
                    .computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>())
                    .add(catalogID);
        }
    }

    @Override
    public void addStoreProductEntry(String catalogID, String storeName,
                                     String productID, double price,
                                     int quantity, double rating, String name) {

        StoreSearchEntry entry = new StoreSearchEntry(
                catalogID, storeName, productID, price, quantity, rating, name
        );
        catalogIdToStoreOffers
                .computeIfAbsent(catalogID, k -> new ArrayList<>())
                .add(entry);
        storeNameToStoreOffers
                .computeIfAbsent(storeName, k -> new ArrayList<>())
                .add(entry);
    }

    @Override
    public void addStoreProductEntryWithImage(String catalogID, String storeName,
                                              String productID, double price,
                                              int quantity, double rating,
                                              String name, String imageUrl) {
        StoreSearchEntry entry = new StoreSearchEntry(
                catalogID, storeName, productID, price, quantity, rating, name
        );
        entry.setImageUrl(imageUrl);

        catalogIdToStoreOffers
                .computeIfAbsent(catalogID, k -> new ArrayList<>())
                .add(entry);
        storeNameToStoreOffers
                .computeIfAbsent(storeName, k -> new ArrayList<>())
                .add(entry);
    }

    @Override
    public void deleteStoreProductEntry(String catalogID, String storeName,
                                        String productID) {
        // Remove from “catalog → storeOffers”
        List<StoreSearchEntry> catalogEntries = catalogIdToStoreOffers.getOrDefault(catalogID, List.of());
        catalogEntries.removeIf(e ->
                e.getStoreName().equalsIgnoreCase(storeName) &&
                        e.getProductID().equals(productID)
        );

        // Remove from “storeName → storeOffers”
        List<StoreSearchEntry> storeEntries = storeNameToStoreOffers.getOrDefault(storeName, List.of());
        storeEntries.removeIf(e ->
                e.getCatalogID().equals(catalogID) &&
                        e.getProductID().equals(productID)
        );
    }

    @Override
    public void updateStoreProductEntry(String catalogID, String storeName,
                                        String productID, Double price,
                                        Integer quantity, Double rating) {
        List<StoreSearchEntry> storeEntries = storeNameToStoreOffers.get(storeName);
        if (storeEntries != null) {
            for (StoreSearchEntry e : storeEntries) {
                if (e.getCatalogID().equals(catalogID) &&
                        e.getProductID().equals(productID)) {
                    if (price    != null) e.setPrice(price);
                    if (quantity != null) e.setQuantity(quantity);
                    if (rating   != null) e.setRating(rating);
                }
            }
        }
    }

    @Override
    public List<CatalogProduct> getAllProducts() {
        return new ArrayList<>(catalogIDtoCatalogProduct.values());
    }

    @Override
    public List<StoreSearchEntry> getAllProductsByCategory(String category) {
        List<String> catalogIDs = categoriesToProducts.getOrDefault(category.toLowerCase(), List.of());
        return catalogIDs.stream()
                .flatMap(id -> catalogIdToStoreOffers.getOrDefault(id, List.of()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreSearchEntry> search(String query, List<String> filters,
                                         String storeName, List<String> cats) {
        // 1st pass: filter catalog by name/brand/description
        Set<String> firstPass = catalogIDtoCatalogProduct.values().stream()
                .filter(p ->
                        p.getName().toLowerCase().contains(query.toLowerCase()) ||
                                p.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                                p.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .map(CatalogProduct::getCatalogID)
                .collect(Collectors.toSet());

        // 2nd pass: find all store entries whose catalogID ∈ firstPass
        List<StoreSearchEntry> pass2 = catalogIdToStoreOffers.entrySet().stream()
                .filter(e -> firstPass.contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .filter(entry -> entry.matchesQuery(query, filters))
                .collect(Collectors.toList());

        // 3rd pass: if storeName != null, filter by storeName
        List<StoreSearchEntry> pass3 = pass2;
        if (storeName != null && !storeName.isBlank()) {
            pass3 = pass2.stream()
                    .filter(e -> e.getStoreName().equalsIgnoreCase(storeName))
                    .collect(Collectors.toList());
        }

        // 4th pass: if cats is nonempty, filter by categories
        List<StoreSearchEntry> pass4 = pass3;
        if (cats != null && !cats.isEmpty()) {
            Set<String> validCatalogIds = cats.stream()
                    .flatMap(cat -> categoriesToProducts.getOrDefault(cat.toLowerCase(), List.of()).stream())
                    .collect(Collectors.toSet());

            pass4 = pass3.stream()
                    .filter(e -> validCatalogIds.contains(e.getCatalogID()))
                    .collect(Collectors.toList());
        }

        return pass4;
    }

    @Override
    public void isProductExist(String catalogID) throws Exception {
        if (!catalogIDtoCatalogProduct.containsKey(catalogID)) {
            throw new Exception("Product " + catalogID + " does not exist");
        }
    }

    @Override
    public List<String> getProductCategory(String catalogID) {
        CatalogProduct p = catalogIDtoCatalogProduct.get(catalogID);
        return (p == null) ? List.of() : p.getCategories();
    }
}
