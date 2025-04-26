package com.SEGroup;

import org.junit.jupiter.api.Test;

import com.SEGroup.Domain.Transaction;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    public void testProductCreationAndGetters() {
        Product product = new Product("iPhone", 4200, "AppleStore");

        assertEquals("iPhone", product.getName());
        assertEquals(4200, product.getPrice());
        assertEquals("AppleStore", product.getStoreName());
    }


    @Test
    public void testSetters() {
        Product product = new Product("OldName", 1000, "TestStore");
        product.setName("NewName");
        product.setPrice(2000);

        assertEquals("NewName", product.getName());
        assertEquals(2000, product.getPrice());
    }

    @Test
    public void testSetProductCopiesFields() {
        Product p1 = new Product("MacBook", 8000, "AppleStore");
        Product p2 = new Product("Y", 1, "AppleStore");
        p2.setProduct(p1);

        assertEquals("MacBook", p2.getName());
        assertEquals(8000, p2.getPrice());
    }
}