package com.SEGroup.Domain;

import java.util.List;

public interface IProductRepository {
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByStoreName(String storeName);
    List<ProductDTO> searchProducts(String query);
    List<ProductDTO> searchInStore(String storeName, String query);
    void addProduct(ProductDTO product);
    void updateProduct(ProductDTO product);
    void deleteProduct(String productName, String storeName);
    ProductDTO findById(String productId);
}