package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;

import java.util.List;

/**
 * Lower‐level persistence abstraction for product catalog.
 * Very similar to UserData / StoreData, but for catalog products and store‐product entries.
 */
public interface ProductCatalogData {
    // Add a brand‐new “catalog product”
    void addCatalogProduct(
            String catalogID,
            String name,
            String brand,
            String description,
            List<String> categories
    );

    // Add a “store‐specific” entry for an existing catalog product (no image)
    void addStoreProductEntry(
            String catalogID,
            String storeName,
            String productID,
            double price,
            int quantity,
            double rating,
            String name
    );

    // Add a “store‐specific” entry for an existing catalog product (with an image URL)
    void addStoreProductEntryWithImage(
            String catalogID,
            String storeName,
            String productID,
            double price,
            int quantity,
            double rating,
            String name,
            String imageUrl
    );

    // Delete a store‐product entry
    void deleteStoreProductEntry(
            String catalogID,
            String storeName,
            String productID
    );

    // Update price/quantity/rating of a store‐product entry (any field may be null to skip)
    void updateStoreProductEntry(
            String catalogID,
            String storeName,
            String productID,
            Double price,
            Integer quantity,
            Double rating
    );

    // Retrieve all “catalog‐level” products
    List<CatalogProduct> getAllProducts();

    // For a given category (e.g. "Electronics"), return all store‐entries (across all stores)
    List<StoreSearchEntry> getAllProductsByCategory(String category);

    // The “search” method: same signature as IProductCatalog.search(...)
    List<StoreSearchEntry> search(
            String query,
            List<String> filters,
            String storeName,
            List<String> cats
    );

    // Throw an exception if the catalogID does not exist
    void isProductExist(String catalogID) throws Exception;

    // Return the list of categories for a single catalog‐product
    List<String> getProductCategory(String catalogID);
}
