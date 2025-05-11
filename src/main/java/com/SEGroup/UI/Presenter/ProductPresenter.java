package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.ProductView;

import java.util.List;
import java.util.Optional;

public class ProductPresenter {
    private final ProductView view;
    private final String productId;
    private final String storeName;
    private final StoreService storeService;
    private final UserService userService;
    private ShoppingProductDTO product;

    public ProductPresenter(ProductView view, String productId, String storeName) {
        this.view = view;
        this.productId = productId;
        this.storeName = storeName;
        this.storeService = ServiceLocator.getStoreService();
        this.userService = ServiceLocator.getUserService();
    }

    /**
     * Loads the product details from the service and updates the view.
     */
// ProductPresenter.java - Fix loadProductDetails method
// ProductPresenter.java - Fix loadProductDetails method
    public void loadProductDetails() {
        try {
            System.out.println("Loading product details for: " + productId + " in store: " + storeName);
            ShoppingProductDTO product = storeService.getProduct(storeName, productId);

            if (product != null) {
                this.product = product;
                view.displayProduct(product);
                System.out.println("Successfully loaded product: " + product.getName());
            } else {
                // Fallback method - try to find the product in all products
                System.out.println("Product not found directly, trying fallback approach...");
                Result<List<ShoppingProductDTO>> allResult = storeService.getAllProducts();

                if (allResult.isSuccess()) {
                    Optional<ShoppingProductDTO> foundProduct = allResult.getData().stream()
                            .filter(p -> p.getProductId().equals(productId) && p.getStoreName().equals(storeName))
                            .findFirst();

                    if (foundProduct.isPresent()) {
                        this.product = foundProduct.get();
                        view.displayProduct(this.product);
                        System.out.println("Found product using fallback: " + this.product.getName());
                    } else {
                        view.showError("Product not found");
                        System.out.println("Product not found with fallback either");
                    }
                } else {
                    view.showError("Error loading product: " + allResult.getErrorMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading product details: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error loading product: " + e.getMessage());
        }
    }
    /**
     * Adds the current product to the cart.
     */
// In ProductPresenter.java - Fix addToCart method
    public void addToCart() {
        try {
            System.out.println("Adding to cart: " + productId + " from store: " + storeName);
            String token = SecurityContextHolder.token();
            System.out.println("Current token: " + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));

            // For guests, create a session if needed
            if (token == null || token.isEmpty()) {
                System.out.println("No token found, creating guest session");
                Result<String> guestResult = userService.guestLogin();
                if (guestResult.isSuccess()) {
                    token = guestResult.getData();
                    System.out.println("Created guest token: " + token.substring(0, Math.min(10, token.length())) + "...");
                    // Store token in session for future use
                    SecurityContextHolder.storeGuestToken(token);
                } else {
                    view.showError("Failed to create guest session: " + guestResult.getErrorMessage());
                    return;
                }
            }

            // Add to cart using the token
            System.out.println("Adding to cart with token");
            Result<String> result = userService.addToCart(token, productId, storeName);

            if (result.isSuccess()) {
                view.showSuccess("Added to cart!");
                System.out.println("Successfully added to cart");
            } else {
                view.showError("Error: " + result.getErrorMessage());
                System.out.println("Failed to add to cart: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            System.err.println("Exception adding to cart: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error adding to cart: " + e.getMessage());
        }
    }
    public String getProductId() {
        return productId;
    }

    public String getStoreName() {
        return storeName;
    }
}