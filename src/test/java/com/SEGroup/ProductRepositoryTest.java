package com.SEGroup;

import com.SEGroup.Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {
    private ProductRepository repo;

    @BeforeEach
    public void setUp() {
        repo = new ProductRepository();
    }

    @Test
    public void testAddAndFindProduct() {
        repo.addProduct("iPhone", "AppleStore", 5000);
        Product found = repo.findById("iPhone");
        assertNotNull(found);
        assertEquals("AppleStore", found.getStoreName());
    }

    @Test
    public void testSearchProducts() {
        repo.addProduct("iPhone", "AppleStore", 5000);
        repo.addProduct("MacBook", "AppleStore", 8000);
        repo.addProduct("Charger", "AppleStore", 100);
        List<Product> results = repo.searchProducts("mac");
        assertEquals(1, results.size());
        assertEquals("MacBook", results.get(0).getName());
    }

    @Test
    public void testDeleteProduct() {
        repo.addProduct("Watch", "AppleStore", 2000);
        repo.deleteProduct("Watch", "AppleStore");
        assertNull(repo.findById("Watch"));
    }
}

