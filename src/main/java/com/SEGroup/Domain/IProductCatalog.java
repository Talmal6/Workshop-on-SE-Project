package com.SEGroup.Domain;

import java.util.List;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
/**
 * Interface for managing a product catalog, including adding products, store entries,
 * and searching for products.
 */
public interface IProductCatalog {
    /**
     * Adds a new catalog product to the catalog.
     *
     * @param catalogID   The unique identifier for the catalog product.
     * @param name        The name of the product.

     * @param categories  A list of categories associated with the product.
     */
    void addCatalogProduct(String catalogID, String name, List<String> categories);

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
    void addStoreProductEntry(String catalogID, String storeName, String productID, double price, int quantity, double rating , String name);
    /**
     * Retrieves a list of all products in the catalog.
     *
     * @return A list of CatalogProduct objects representing all products in the catalog.
     */
    void deleteStoreProductEntry(String catalogID, String storeName, String productID);
    /**
     * Updates an existing store product entry in the catalog.
     *
     * @param catalogID  The catalog ID of the product.
     * @param storeName  The name of the store offering the product.
     * @param productID  The unique identifier for the product in the store.
     * @param price      The new price of the product.
     * @param quantity   The new quantity of the product available.
     * @param rating     The new rating of the product.
     */
    void updateStoreProductEntry(String catalogID, String storeName, String productID, Double price, Integer quantity, Double rating);

    /**
     * Retrieves a list of all products in the catalog.
     *
     * @return A list of CatalogProduct objects representing all products in the catalog.
     */
    List<CatalogProduct> getAllProducts();
    /**
     * Retrieves a list of all products from a specific category.
     *
     * @param category The category to filter products by.
     * @return A list of CatalogProduct objects representing all products in the specified category.
     */
    List<StoreSearchEntry> getAllProductsByCategory(String category);
    /**
     * Searches for products in the catalog based on a query and optional filters.
     *
     * @param query         The search query.
     * @param searchFilters  A list of filters to apply to the search.
     * @param storeName     The name of the store to filter results by.
     * @param categories    A list of categories to filter results by.
     * @return A list of StoreSearchEntry objects representing the search results.
     */
    List<StoreSearchEntry> search(String query, List<String> searchFilters, String storeName, List<String> categories);

    /**
     * Checks if a product exists in the catalog.
     *
     *  @param catalogID The unique identifier for the catalog product.
     *
     */
    void isProductExist(String catalogID) throws Exception;


    /**
     * Adds a product entry to the catalog with an image URL.
     *
     * @param catalogID The ID of the catalog product.
     * @param storeName The name of the store.
     * @param productID The ID of the product.
     * @param price The price of the product.
     * @param quantity The quantity of the product.
     * @param rating The rating of the product.
     * @param name The name of the product.
     * @param imageUrl The image URL for the product.
     */
    void addStoreProductEntryWithImage(String catalogID, String storeName, String productID, double price,
                                       int quantity, double rating, String name, String imageUrl);


    CatalogProduct getCatalogProduct(String catalogId);
}