package com.SEGroup.Domain.ProductCatalog;

import java.util.*;

public class InMemoryProductRepository implements IProductRepository {
    private final Map<String, List<Product>> storeProducts = new HashMap<>(); // A map to hold store names and their products <storeName, List<Product>>

    @Override
    public List<Product> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        for (List<Product> productList : storeProducts.values()) {
            allProducts.addAll(productList);
        }
        return allProducts;
    }

    @Override
    public List<Product> getProductsByStoreName(String storeName) {
        return new ArrayList<>(storeProducts.getOrDefault(storeName, new ArrayList<>()));
    }

    @Override
    public Product findById(String productName) {
        for (List<Product> products : storeProducts.values()) {
            for (Product p : products) {
                if (p.getName().equals(productName)) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public void addProduct(String productName, String storeName, double price) {
        Product product = new Product(productName, price, storeName);
        if (!storeProducts.containsKey(storeName)) {
            storeProducts.put(storeName, new ArrayList<>());
        }
        storeProducts.get(storeName).add(product);
    }

    @Override
    public void updateProduct(Product updated) {
        List<Product> products = storeProducts.get(updated.getStoreName());
        if (products == null) return;

        for (Product p : products) {
            if (p.getName().equals(updated.getName())) {
                p.setProduct(updated);
                return;
            }
        }
    }

    @Override
    public void deleteProduct(String productName, String storeName) {
        List<Product> products = storeProducts.get(storeName);
        if (products != null) {
            products.removeIf(p -> p.getName().equals(productName));
        }
    }

    @Override
    public List<Product> searchProducts(String productName) {
        List<Product> result = new ArrayList<>();
        String lowerProductName = productName.toLowerCase();

        for (List<Product> productList : storeProducts.values()) {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerProductName)) {
                    result.add(product);
                }
            }
        }

        return result;
    }


    @Override
    public List<Product> searchInStore(String storeName, String productName) {
        List<Product> result = new ArrayList<>();
        String lowerProductName = productName.toLowerCase();

        List<Product> products = storeProducts.getOrDefault(storeName, new ArrayList<>());
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerProductName)) {
                result.add(product);
            }
        }

        return result;
    }


    @Override
    public void checkIfExist(String productName) {
        if (findById(productName) == null) {
            throw new IllegalArgumentException("Product with name '" + productName + "' does not exist.");
        }
    }
    @Override
    public Product getProduct(String productName) {
        return findById(productName);
    }
}
