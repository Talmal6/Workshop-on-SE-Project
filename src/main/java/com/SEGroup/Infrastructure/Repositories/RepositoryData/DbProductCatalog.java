package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.CatalogProductEntity;
import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.StoreSearchEntryEntity;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaCatalogRepository;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaStoreEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * JPA‐backed implementation of ProductCatalogData.
 * Active when profile is either “db” or “prod”.
 */
@Repository
@Profile({ "db", "prod" })
@Primary      // so that if anything else also implements ProductCatalogData, this one is preferred
@Transactional
public class DbProductCatalog implements ProductCatalogData {

    private final JpaCatalogRepository products;
    private final JpaStoreEntryRepository offers;

    @Autowired
    public DbProductCatalog(
            JpaCatalogRepository products,
            JpaStoreEntryRepository offers
    ) {
        this.products = products;
        this.offers   = offers;
    }

    @Override
    public void addCatalogProduct(String catalogID, String name, String brand, String description, List<String> categories) {
        DbSafeExecutor.safeExecute("addCatalogProduct", () -> {
            products.save(new CatalogProductEntity(catalogID, name, brand, description, categories));
            return null;
        });
    }

    @Override
    public void addStoreProductEntry(String catalogID, String storeName, String productID, double price, int quantity, double rating, String name) {
        DbSafeExecutor.safeExecute("addStoreProductEntry", () -> {
            offers.save(new StoreSearchEntryEntity(catalogID, storeName, productID, price, quantity, rating, name, null));
            return null;
        });
    }

    @Override
    public void addStoreProductEntryWithImage(String catalogID, String storeName, String productID, double price, int quantity, double rating, String name, String imageUrl) {
        DbSafeExecutor.safeExecute("addStoreProductEntryWithImage", () -> {
            offers.save(new StoreSearchEntryEntity(catalogID, storeName, productID, price, quantity, rating, name, imageUrl));
            return null;
        });
    }

    @Override
    public void deleteStoreProductEntry(String catalogID, String storeName, String productID) {
        DbSafeExecutor.safeExecute("deleteStoreProductEntry", () -> {
            offers.deleteByCatalogIdAndStoreNameAndProductId(catalogID, storeName, productID);
            return null;
        });
    }

    @Override
    public void updateStoreProductEntry(String catalogID, String storeName, String productID, Double price, Integer quantity, Double rating) {
        DbSafeExecutor.safeExecute("updateStoreProductEntry", () -> {
            offers.findByCatalogId(catalogID).stream()
                    .filter(e -> e.getStoreName().equalsIgnoreCase(storeName) && e.getProductId().equals(productID))
                    .forEach(e -> {
                        if (price != null) e.setPrice(price);
                        if (quantity != null) e.setQuantity(quantity);
                        if (rating != null) e.setRating(rating);
                        offers.save(e);
                    });
            return null;
        });
    }

    @Override
    public List<CatalogProduct> getAllProducts() {
        return DbSafeExecutor.safeExecute("getAllProducts", () ->
                products.findAll().stream()
                        .map(CatalogProductEntity::toDomain)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<StoreSearchEntry> getAllProductsByCategory(String category) {
        return DbSafeExecutor.safeExecute("getAllProductsByCategory", () ->
                products.findAll().stream()
                        .filter(e -> e.getCategories().contains(category.toLowerCase()))
                        .flatMap(e -> offers.findByCatalogId(e.getCatalogId()).stream())
                        .map(StoreSearchEntryEntity::toDomain)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<StoreSearchEntry> search(String query, List<String> filters, String storeName, List<String> cats) {
        return DbSafeExecutor.safeExecute("search", () -> {
            Set<String> firstPassCatalogIds = products.findAll().stream()
                    .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                            p.getBrand().toLowerCase().contains(query.toLowerCase()) ||
                            p.getDescription().toLowerCase().contains(query.toLowerCase()))
                    .map(CatalogProductEntity::getCatalogId)
                    .collect(Collectors.toSet());

            List<StoreSearchEntry> pass2 = offers.findAll().stream()
                    .filter(e -> firstPassCatalogIds.contains(e.getCatalogId()))
                    .map(StoreSearchEntryEntity::toDomain)
                    .filter(e -> e.matchesQuery(query, filters))
                    .collect(Collectors.toList());

            if (storeName != null && !storeName.isBlank()) {
                pass2 = pass2.stream()
                        .filter(e -> e.getStoreName().equalsIgnoreCase(storeName))
                        .collect(Collectors.toList());
            }

            if (cats != null && !cats.isEmpty()) {
                Set<String> validCatalogs = cats.stream()
                        .flatMap(cat -> products.findById(cat.toLowerCase()).stream())
                        .map(CatalogProductEntity::getCatalogId)
                        .collect(Collectors.toSet());

                pass2 = pass2.stream()
                        .filter(e -> validCatalogs.contains(e.getCatalogID()))
                        .collect(Collectors.toList());
            }

            return pass2;
        });
    }

    @Override
    public void isProductExist(String catalogID) throws Exception {
        if (!DbSafeExecutor.safeExecute("isProductExist", () -> products.existsById(catalogID))) {
            throw new Exception("Product " + catalogID + " does not exist");
        }
    }

    @Override
    public List<String> getProductCategory(String catalogID) {
        return DbSafeExecutor.safeExecute("getProductCategory", () ->
                products.findById(catalogID)
                        .map(CatalogProductEntity::getCategories)
                        .orElse(Collections.emptyList())
        );
    }
}
