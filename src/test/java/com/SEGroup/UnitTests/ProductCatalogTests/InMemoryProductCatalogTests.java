package com.SEGroup.UnitTests.ProductCatalogTests;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;


public class InMemoryProductCatalogTests {
    private InMemoryProductCatalog catalog;

    @BeforeEach
    void init() {
        catalog = new InMemoryProductCatalog();
    }

    @Test
    void GivenNewProduct_WhenAddCatalogProduct_ThenProductIsAdded() {
        catalog.addCatalogProduct("123", "Laptop", "BrandA", "Powerful laptop", Arrays.asList("Electronics", "Computers"));
        List<CatalogProduct> products = catalog.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void GivenValidStoreEntry_WhenaddStoreProductEntry_ThenEntryIsAdded() {
        catalog.addCatalogProduct("123", "Phone", "BrandB", "Smartphone", Arrays.asList("Electronics"));
        catalog.addStoreProductEntry("123", "BestStore", "p1", 500.0, 10, 4.5, "name");

        List<StoreSearchEntry> entries = catalog.getAllProductsByCategory("Electronics");
        assertEquals(1, entries.size());
        assertEquals("BestStore", entries.get(0).getStoreName());
    }

    // deleteStoreProductEntry
    @Test
    void GivenExistingEntry_WhendeleteStoreProductEntry_ThenEntryIsRemoved() {
        catalog.addCatalogProduct("123", "Tablet", "BrandC", "Big screen", Arrays.asList("Gadgets"));
        catalog.addStoreProductEntry("123", "GadgetStore", "p2", 300.0, 5, 4.0, "name");

        catalog.deleteStoreProductEntry("123", "GadgetStore", "p2");
        List<StoreSearchEntry> entries = catalog.getAllProductsByCategory("Gadgets");
        assertEquals(0, entries.size());
    }

    // updateStoreProductEntry
    @Test
    void GivenExistingEntry_WhenupdateStoreProductEntry_ThenEntryIsUpdated() {
        catalog.addCatalogProduct("123", "Camera", "BrandD", "Professional camera", Arrays.asList("Photography"));
        catalog.addStoreProductEntry("123", "PhotoStore", "p3", 800.0, 3, 4.8, "name");

        catalog.updateStoreProductEntry("123", "PhotoStore", "p3", 750.0, 5, 5.0);

        List<StoreSearchEntry> entries = catalog.getAllProductsByCategory("Photography");
        StoreSearchEntry updatedEntry = entries.get(0);
        assertEquals(5, updatedEntry.getQuantity());
    }

    // getAllProducts
    @Test
    void GivenProducts_WhenGetAllProducts_ThenReturnAllProducts() {
        catalog.addCatalogProduct("1", "Product1", "BrandX", "Desc1", Collections.singletonList("Category1"));
        catalog.addCatalogProduct("2", "Product2", "BrandY", "Desc2", Collections.singletonList("Category2"));

        List<CatalogProduct> allProducts = catalog.getAllProducts();
        assertEquals(2, allProducts.size());
    }

    // getAllProductsByCategory
    @Test
    void GivenCategoryWithEntries_WhenGetAllProductsByCategory_ThenReturnEntries() {
        catalog.addCatalogProduct("123", "Headphones", "BrandE", "Noise cancelling", Arrays.asList("Audio"));
        catalog.addStoreProductEntry("123", "AudioStore", "p4", 150.0, 20, 4.2, "name");

        List<StoreSearchEntry> entries = catalog.getAllProductsByCategory("Audio");
        assertEquals(1, entries.size());
    }

    @Test
    void GivenNonExistingCategory_WhenGetAllProductsByCategory_ThenReturnEmptyList() {
        List<StoreSearchEntry> entries = catalog.getAllProductsByCategory("NonExistingCategory");
        assertTrue(entries.isEmpty());
    }

    // search
    @Test
    void GivenMatchingQuery_WhenSearch_ThenReturnMatchingEntries() {
        catalog.addCatalogProduct("123", "Gaming Mouse", "BrandG", "Fast response mouse", Arrays.asList("Gaming"));
        catalog.addStoreProductEntry("123", "GameStore", "p5", 70.0, 15, 4.7, "name");

        List<StoreSearchEntry> results = catalog.search("mouse", Collections.emptyList(), null, null);
        assertEquals(1, results.size());
    }

    @Test
    void GivenStoreNameFilter_WhenSearch_ThenReturnMatchingStoreEntries() {
        catalog.addCatalogProduct("123", "Keyboard", "BrandH", "Mechanical keyboard", Arrays.asList("Gaming"));
        catalog.addStoreProductEntry("123", "KeyStore", "p6", 120.0, 8, 4.9, "something");
        catalog.addStoreProductEntry("123", "AnotherStore", "p7", 110.0, 5, 4.6, "somhethingese");

        List<StoreSearchEntry> results = catalog.search("keyboard", Collections.emptyList(), "KeyStore", null);
        assertEquals(1, results.size());
        assertEquals("KeyStore", results.get(0).getStoreName());
    }

    @Test
    void GivenNoMatchingQuery_WhenSearch_ThenReturnEmptyList() {
        catalog.addCatalogProduct("123", "Webcam", "BrandJ", "HD webcam", Arrays.asList("Accessories"));
        catalog.addStoreProductEntry("123", "AccessoryStore", "p9", 60.0, 30, 4.1, "Nm");

        List<StoreSearchEntry> results = catalog.search("printer", Collections.emptyList(), null, null);
        assertTrue(results.isEmpty());
    }

    // isProductExist
    @Test
    void GivenExistingCatalogId_WhenIsProductExist_ThenDoNotThrow() {
        catalog.addCatalogProduct("123", "Speaker", "BrandK", "Bluetooth speaker", Arrays.asList("Audio"));
        assertDoesNotThrow(() -> catalog.isProductExist("123"));
    }

    @Test
    void GivenNonExistingCatalogId_WhenIsProductExist_ThenThrowException() {
        Exception exception = assertThrows(Exception.class, () -> catalog.isProductExist("999"));
        assertEquals("Product with catalog ID 999 does not exist.", exception.getMessage());
    }
}
