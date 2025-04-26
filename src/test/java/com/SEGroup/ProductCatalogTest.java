package com.SEGroup;


import com.SEGroup.Domain.ProductCatalog;
import com.SEGroup.Domain.Transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ProductCatalogTest {
    private ProductCatalog catalog;

    @BeforeEach
    public void setUp() {
        catalog = new ProductCatalog("YuvalStore");
    }

    @Test
    public void testAddProduct() {
        catalog.addProduct("AlgoTradeCourse", 5000);
        List<Product> all = catalog.getAllProducts();
        assertEquals(1, all.size());
        assertEquals("AlgoTradeCourse", all.get(0).getName());
        assertEquals(5000, all.get(0).getPrice());
    }

    @Test
    public void testFindProductByName() {
        catalog.addProduct("iPhone", 4200);
        Product found = catalog.findProductByName("iphone");
        assertNotNull(found);
        assertEquals("iPhone", found.getName());
    }

    @Test
    public void testSearchByPartialName() {
        catalog.addProduct("iPhone 16 Pro", 4200);
        catalog.addProduct("iPhone 16 ProMax", 5000);
        catalog.addProduct("MacBook Pro", 6500);

        List<Product> result = catalog.search("iphone");
        assertEquals(2, result.size());
    }

    @Test
    public void testRemoveProduct() {
        catalog.addProduct("AirPods", 600);
        boolean removed = catalog.removeProduct("airpods");
        assertTrue(removed);
        assertTrue(catalog.getAllProducts().isEmpty());
    }
}