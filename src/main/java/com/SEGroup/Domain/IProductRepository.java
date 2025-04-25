package com.SEGroup.Domain;

import java.util.List;

public interface IProductRepository {
    List<Product> getAllProducts();
    List<Product> getProductsByStoreName(String storeName);
    List<Product> searchProducts(String query);
    List<Product> searchInStore(String storeName, String query);
    void addProduct(String productName,String storeName,double price);
    void updateProduct(Product product);
    void deleteProduct(String productName, String storeName);
    Product findById(String productId);
    void checkIfExist(String productId);
    Product getProduct(String productId);
    
}