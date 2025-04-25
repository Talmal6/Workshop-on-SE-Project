package com.SEGroup.Domain.ProductCatalog;

import java.util.ArrayList;
import java.util.List;

public class ProductCatalog {
    private final String storeName;
    private final List<Product> products;
    private final Map<String, List<String>>
    public ProductCatalog(String storeName) {
        this.storeName = storeName;
        this.products = new ArrayList<>();
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public void addProductToCatalog(String catalogID, String description) {
        products.add(new Product(catalogID, description));
    }

    public void addProduct();
}