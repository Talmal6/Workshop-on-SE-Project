package com.SEGroup.Infrastructure.Repositories.InMemoryRepositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.SEGroup.Domain.IProductCatalog;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * An in-memory implementation of the ProductCatalog interface.
 * This class manages catalog products, store product entries, and their relationships.
 */
@Component
public class InMemoryProductCatalog implements IProductCatalog {

    private final Map<String, CatalogProduct> catalogIDtoCatalogProduct = new HashMap<>();
    private final Map<String, List<StoreSearchEntry>> catalogIdToStoreOffers = new HashMap<>();
    private final Map<String, List<String>> categoriesToProducts = new HashMap<>();
    private final Map<String, List<StoreSearchEntry>> storeNameToStoreOffers = new HashMap<>();


    /**
     * Adds a new catalog product to the catalog.
     *
     * @param catalogID   The unique identifier for the catalog product.
     * @param name        The name of the product.
     * @param brand       The brand of the product.
     * @param description A brief description of the product.
     * @param categories  A list of categories associated with the product.
     */
    @Override
    public void addCatalogProduct(String catalogID, String name, String brand, String description,
            List<String> categories) {
        CatalogProduct product = new CatalogProduct(catalogID, name, brand, description,categories);
        catalogIDtoCatalogProduct.put(catalogID, product);

        for (String category : categories) {
            categoriesToProducts.computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>()).add(catalogID);
        }
    }

    /**
     * Adds a store product entry to the catalog.
     *
     * @param catalogID  The catalog ID of the product.
     * @param storeName  The name of the store offering the product.
     * @param productID  The unique identifier for the product in the store.
     * @param price      The price of the product.
     * @param quantity   The quantity of the product available.
     * @param rating     The rating of the product.
     * @param name       The name of the product.
     */

    @Override
    public void addStoreProductEntry(String catalogID, String storeName, String productID, double price, int quantity,
            double rating, String name) {

        StoreSearchEntry entry = new StoreSearchEntry(catalogID, storeName, productID, price, quantity, rating, name);
        catalogIdToStoreOffers.computeIfAbsent(catalogID, k -> new ArrayList<>()).add(entry);
        storeNameToStoreOffers.computeIfAbsent(storeName, k -> new ArrayList<>()).add(entry);



    }

    /**
     * Deletes a store product entry from the catalog.
     *
     * @param catalogID  The catalog ID of the product.
     * @param storeName  The name of the store offering the product.
     * @param productID  The unique identifier for the product in the store.
     */

    @Override
    public void deleteStoreProductEntry(String catalogID, String storeName, String productID) {
        List<StoreSearchEntry> catalogEntries = catalogIdToStoreOffers.getOrDefault(catalogID, new ArrayList<>());
        catalogEntries
                .removeIf(entry -> entry.getStoreName().equals(storeName) && entry.getProductID().equals(productID));

        List<StoreSearchEntry> storeEntries = storeNameToStoreOffers.getOrDefault(storeName, new ArrayList<>());
        storeEntries
                .removeIf(entry -> entry.getCatalogID().equals(catalogID) && entry.getProductID().equals(productID));
    }
    /**
     * Updates a store product entry in the catalog.
     *
     * @param catalogID  The catalog ID of the product.
     * @param storeName  The name of the store offering the product.
     * @param productID  The unique identifier for the product in the store.
     * @param price      The new price of the product (nullable).
     * @param quantity   The new quantity of the product (nullable).
     * @param rating     The new rating of the product (nullable).
     */

    @Override
    public void updateStoreProductEntry(String catalogID, String storeName, String productID, Double price,
            Integer quantity, Double rating) {
        List<StoreSearchEntry> storeEntries = storeNameToStoreOffers.get(storeName);
        if (storeEntries != null) {
            for (StoreSearchEntry entry : storeEntries) {
                if (entry.getCatalogID().equals(catalogID) && entry.getProductID().equals(productID)) {
                    if (price != null)
                        entry.setPrice(price);
                    if (quantity != null)
                        entry.setQuantity(quantity);
                    if (rating != null)
                        entry.setRating(rating);
                }
            }
        }
    }


    /**
     * Retrieves all catalog products.
     *
     * @return A list of all catalog products.
     */
    @Override
    public List<CatalogProduct> getAllProducts() {
        return new ArrayList<>(catalogIDtoCatalogProduct.values());
    }

    /**
     * Retrieves all store product entries by category.
     *
     * @param category The category to filter products by.
     * @return A list of store product entries in the specified category.
     */
    @Override
    public List<StoreSearchEntry> getAllProductsByCategory(String category) {
        List<String> catalogIDs = categoriesToProducts.getOrDefault(category.toLowerCase(), Collections.emptyList());
        return catalogIDs.stream()
                .flatMap(id -> catalogIdToStoreOffers.getOrDefault(id, Collections.emptyList()).stream())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all store product entries by store name.
     *
     * @param storeName The name of the store to filter products by.
     * @return A list of store product entries in the specified store.
     */
    @Override
    public List<StoreSearchEntry> search(String query, List<String> searchFilters, String storeName,
            List<String> categories) {
        // Step 1: First pass - Filter CatalogProducts by name, brand, or description
        // matching the query
        Set<String> firstPass = catalogIDtoCatalogProduct.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()) ||
                        product.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(query.toLowerCase()))
                .map(CatalogProduct::getCatalogID)
                .collect(Collectors.toSet());

        // Step 2: Second pass - Find StoreProductEntries matching those CatalogProducts
        // and matching search filters
        List<StoreSearchEntry> secondPass = catalogIdToStoreOffers.entrySet().stream()
                .filter(entry -> firstPass.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(storeProductEntry -> storeProductEntry.matchesQuery(query, searchFilters))
                .collect(Collectors.toList());

        // Step 3: Third pass - If storeName is specified, filter entries by store name
        List<StoreSearchEntry> thirdPass = secondPass;
        if (storeName != null && !storeName.isEmpty()) {
            thirdPass = thirdPass.stream()
                    .filter(entry -> entry.getStoreName().equalsIgnoreCase(storeName))
                    .collect(Collectors.toList());
        }

        // Step 4: Fourth pass - If categories are specified, filter entries by
        // categories
        List<StoreSearchEntry> fourthPass = thirdPass;
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

    /**
     * Checks if a product exists in the catalog.
     *
     * @param catalogID The catalog ID of the product.
     * @throws Exception If the product does not exist.
     */
    @Override
    public void isProductExist(String catalogID) throws Exception {
        if (!catalogIDtoCatalogProduct.containsKey(catalogID)) {
            throw new Exception("Product with catalog ID " + catalogID + " does not exist.");
        }
    }

    /**
     * Returns all categories associated with a given catalogID.
     *
     * @param catalogID The ID of the product in the catalog.
     * @return          A list of category names, or empty list if not found.
     */
    public List<String> getCategoriesOfProduct(String catalogID) {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : categoriesToProducts.entrySet()) {
            String category = entry.getKey();
            List<String> products = entry.getValue();

            if (products.contains(catalogID)) {
                result.add(category);
            }
        }

        return result;
    }

    /**
     * Adds a store product entry with an image URL to the catalog.
     *
     * @param catalogID  The catalog ID of the product.
     * @param storeName  The name of the store offering the product.
     * @param productID  The unique identifier for the product in the store.
     * @param price      The price of the product.
     * @param quantity   The quantity of the product available.
     * @param rating     The rating of the product.
     * @param name       The name of the product.
     * @param imageUrl   The URL of the product image.
     */
    @Override
    public void addStoreProductEntryWithImage(String catalogID, String storeName, String productID, double price,
                                              int quantity, double rating, String name, String imageUrl) {
        StoreSearchEntry entry = new StoreSearchEntry(catalogID, storeName, productID, price, quantity, rating, name);
        entry.setImageUrl(imageUrl); // Add this field to StoreSearchEntry class

        // Add to both maps just like in addStoreProductEntry
        catalogIdToStoreOffers.computeIfAbsent(catalogID, k -> new ArrayList<>()).add(entry);
        storeNameToStoreOffers.computeIfAbsent(storeName, k -> new ArrayList<>()).add(entry);
    }
    public List<String> getProductCategory(String catalogID) {
        CatalogProduct product = catalogIDtoCatalogProduct.get(catalogID);
        return product.getCategories();
    }
}
