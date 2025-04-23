package com.SEGroup.Domain;

import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IProductRepository {
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByStoreName(String storeName);
    List<ProductDTO> searchProducts(String query);
    List<ProductDTO> searchInStore(String storeName, String query);
    void addProduct(ProductDTO product);
    void updateProduct(String productName, String storeName, double newPrice);
    void deleteProduct(String productName, String storeName);
}