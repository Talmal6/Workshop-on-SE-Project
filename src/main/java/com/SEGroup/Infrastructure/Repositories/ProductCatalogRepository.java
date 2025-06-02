package com.SEGroup.Infrastructure.Repositories;

import com.SEGroup.Domain.IProductCatalog;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryProductCatalogData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.ProductCatalogData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductCatalogRepository is the top‐level repository for all product catalog operations.
 * It implements IProductCatalog and delegates every call to a ProductCatalogData implementation.
 *
 * When the “mem” profile is active, only InMemoryProductCatalogData is registered (as a @Repository @Profile("mem")),
 * and this class’s no‐arg constructor will pick it up.  When “db” or “prod” is active,
 * DbProductCatalog (a @Repository @Profile({"db","prod"})) is registered and injected here.
 */
@Repository
@Profile({ "prod", "db" })
public class ProductCatalogRepository implements IProductCatalog {

    private final ProductCatalogData data;

    /**
     * No‐arg constructor: if Spring cannot inject a ProductCatalogData bean (e.g. running under "mem" only),
     * fall back to the in‐memory implementation directly.
     */
    public ProductCatalogRepository() {
        this.data = new InMemoryProductCatalogData();
    }


    /**
     * Primary constructor: Spring will provide whichever ProductCatalogData bean is active.
     *   • Under "db" or "prod" profile, DbProductCatalog is injected.
     *   • Under "mem" profile, InMemoryProductCatalog
     * Data is injected automatically.
     */
    @Autowired
    public ProductCatalogRepository(ProductCatalogData data) {
        this.data = data;
    }

    /**
     * Add a brand‐new catalog product (catalog‐level).
     */
    @Override
    public void addCatalogProduct(
            String catalogID,
            String name,
            String brand,
            String description,
            List<String> categories
    ) {
        data.addCatalogProduct(catalogID, name, brand, description, categories);
    }

    /**
     * Add a store‐specific entry for an existing catalog product (no image URL).
     */
    @Override
    public void addStoreProductEntry(
            String catalogID,
            String storeName,
            String productID,
            double price,
            int quantity,
            double rating,
            String name
    ) {
        data.addStoreProductEntry(catalogID, storeName, productID, price, quantity, rating, name);
    }

    /**
     * Add a store‐specific entry for an existing catalog product (with image URL).
     */
    @Override
    public void addStoreProductEntryWithImage(
            String catalogID,
            String storeName,
            String productID,
            double price,
            int quantity,
            double rating,
            String name,
            String imageUrl
    ) {
        data.addStoreProductEntryWithImage(catalogID, storeName, productID, price, quantity, rating, name, imageUrl);
    }

    /**
     * Delete a store‐product entry from the catalog.
     */
    @Override
    public void deleteStoreProductEntry(
            String catalogID,
            String storeName,
            String productID
    ) {
        data.deleteStoreProductEntry(catalogID, storeName, productID);
    }

    /**
     * Update price/quantity/rating of a store‐product entry (any argument may be null to skip).
     */
    @Override
    public void updateStoreProductEntry(
            String catalogID,
            String storeName,
            String productID,
            Double price,
            Integer quantity,
            Double rating
    ) {
        data.updateStoreProductEntry(catalogID, storeName, productID, price, quantity, rating);
    }

    /**
     * Retrieve all catalog‐level products.
     */
    @Override
    public List<CatalogProduct> getAllProducts() {
        return data.getAllProducts();
    }

    /**
     * For a given category (e.g. "Electronics"), return all store‐entries across all stores.
     */
    @Override
    public List<StoreSearchEntry> getAllProductsByCategory(String category) {
        return data.getAllProductsByCategory(category);
    }

    /**
     * Search for store‐entries by free‐text query, optional filters, optional storeName, optional categories.
     */
    @Override
    public List<StoreSearchEntry> search(
            String query,
            List<String> filters,
            String storeName,
            List<String> cats
    ) {
        return data.search(query, filters, storeName, cats);
    }

    /**
     * Throw an exception if the catalogID does not exist.
     */
    @Override
    public void isProductExist(String catalogID) throws Exception {
        data.isProductExist(catalogID);
    }

    /**
     * Return the list of categories for a single catalog‐product.
     */
    @Override
    public List<String> getProductCategory(String catalogID) {
        return data.getProductCategory(catalogID);
    }
}
