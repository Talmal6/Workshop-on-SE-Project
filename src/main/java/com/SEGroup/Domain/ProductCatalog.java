package com.SEGroup.Domain;

import java.util.ArrayList;
import java.util.List;

public class ProductCatalog {
    private final String storeName;
    private final List<Product> products;

    public ProductCatalog(String storeName) {
        this.storeName = storeName;
        this.products = new ArrayList<>();
    }

    public String getStoreName() {
        return storeName;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public void addProduct(String name, double price) {
        products.add(new Product(name, price, storeName));
    }

    public boolean removeProduct(String name) {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.getName().equalsIgnoreCase(name)) {
                products.remove(i);
                return true;
            }
        }
        return false;
    }


    public Product findProductByName(String name) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    public List<Product> search(String productName) {
        List<Product> result = new ArrayList<>();
        String lowerProductName = productName.toLowerCase();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerProductName)) {
                result.add(product);
            }
        }
        return result;
    }
}