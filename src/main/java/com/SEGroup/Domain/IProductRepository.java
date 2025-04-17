package com.SEGroup.Domain;
import java.util.List;

public interface IProductRepository {
    // This interface defines the contract for a product repository.
    // It should be implemented by any class that wants to provide product data access functionality.

    // Method to add a product to the repository
    void addProduct(Product product);

    // Method to remove a product from the repository
    void removeProduct(Product product);

    // Method to update a product in the repository
    void updateProduct(Product product);

    // Method to find a product by its ID
    Product findProductById(int id);

    // Method to get all products in the repository
    List<Product> getAllProducts();
}
