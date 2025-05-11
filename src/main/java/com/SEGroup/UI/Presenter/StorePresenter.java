// StorePresenter.java (Fixed)
package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.CatalogProductDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.StoreView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter for the StoreView.
 * Handles business logic and communication with the service layer.
 */
public class StorePresenter {
    private final StoreView view;
    private final StoreService storeService;
    private final UserService userService;
    private final String storeName;

    private List<ShoppingProductDTO> allStoreProducts = new ArrayList<>();

    /**
     * Constructs a new StorePresenter.
     *
     * @param view The store view this presenter is associated with
     * @param storeName The name of the store being displayed
     */
    public StorePresenter(StoreView view, String storeName) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
        this.userService = ServiceLocator.getUserService();
        this.storeName = storeName;
        if(SecurityContextHolder.isStoreOwner()){
            view.showManagingOwnersButton();
            view.showManagingRolesButton();
        }
    }

    /**
     * Loads store details from the service layer.
     */
    public void loadStoreDetails() {
        try {
            Result<StoreDTO> result = storeService.viewStore(storeName);

            if (result.isSuccess()) {
                // Process store data if needed before displaying
                view.displayStore(result.getData());
            } else {
                view.showError("Failed to load store: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error loading store details: " + e.getMessage());
        }
    }

    /**
     * Loads all products for the current store from the service layer.
     */
    public void loadStoreProducts() {
        List<ShoppingProductDTO> products = new ArrayList<>();
        try {
            // attempt to fetch store-specific products
            Result<List<ShoppingProductDTO>> result = storeService.getStoreProducts(storeName);
            if (result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                products = result.getData();
            } else {
                // fallback: get all and filter by store
                Result<List<ShoppingProductDTO>> allResult = storeService.getAllProducts();
                if (allResult.isSuccess() && allResult.getData() != null) {
                    products = allResult.getData().stream()
                            .filter(p -> storeName.equals(p.getStoreName()))
                            .collect(Collectors.toList());
                }
            }

            // ensure no null lists
            ensureProductsHaveCategories(products);
            // enrich with master catalog categories
            enrichProductCategories(products);

            // cache and display
            this.allStoreProducts = new ArrayList<>(products);
            view.displayProducts(products);

        } catch (Exception e) {
            view.showError("Error loading store products: " + e.getMessage());
            view.displayProducts(new ArrayList<>());
        }
    }


    private void enrichProductCategories(List<ShoppingProductDTO> products) {
        for (ShoppingProductDTO product : products) {
            try {
                String catalogId = product.getCatalogId();
                // assume StoreService provides this method:
                Result<CatalogProductDTO> catRes = storeService.getCatalogProduct(catalogId);
                if (catRes.isSuccess() && catRes.getData() != null) {
                    product.setCategories(catRes.getData().getCategories());
                } else {
                    product.setCategories(new ArrayList<>());
                }
            } catch (Exception e) {
                product.setCategories(new ArrayList<>());
            }
        }
    }
    /**
     * Helper method to ensure all products have categories.
     * This prevents NPEs when filtering products by category.
     */
    private void ensureProductsHaveCategories(List<ShoppingProductDTO> products) {
        for (ShoppingProductDTO product : products) {
            if (product.getCategories() == null) {
                product.setCategories(new ArrayList<>());
            }
        }
    }

    /**
     * Checks if the current user is an owner of this store.
     *
     * @return true if the current user is an owner, false otherwise
     */
    public boolean isCurrentUserOwner() {
        if (!SecurityContextHolder.isLoggedIn()) return false;

        try {
            // Get current user email
            String currentUserEmail = SecurityContextHolder.email();
            System.out.println("Checking if " + currentUserEmail + " is owner of " + storeName);

            // Check if user is in owners list
            Result<List<String>> result = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    currentUserEmail
            );

            boolean isOwner = result.isSuccess() && result.getData().contains(currentUserEmail);
            System.out.println("User is owner: " + isOwner + " for store " + storeName);
            return isOwner;
        } catch (Exception e) {
            System.err.println("Error checking ownership: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a new product to the store.
     *
     * @param catalogId The catalog ID of the product
     * @param name The name of the product
     * @param description The description of the product
     * @param price The price of the product
     * @param quantity The quantity of the product
     */
    public void addProduct(String catalogId, String name, String description, double price, int quantity, String imageURL) {
        try {
            Result<String> result = storeService.addProductToStore(
                    SecurityContextHolder.token(),
                    storeName,
                    catalogId,
                    name,
                    description,
                    price,
                    quantity,
                    imageURL
            );

            if (result.isSuccess()) {
                view.showSuccess("Product added successfully: " + result.getData());
                loadStoreProducts(); // Reload store products
            } else {
                view.showError("Failed to add product: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error adding product: " + e.getMessage());
        }
    }

    /**
     * Searches and filters products within this store based on criteria.
     *
     * @param query The search query text
     * @param filters List of filter strings (category=X, price>Y, etc.)
     */
    public void searchProducts(String query, List<String> filters) {
        try {
            // Filter the products in the presenter instead of in the view
            List<ShoppingProductDTO> filteredProducts = new ArrayList<>(allStoreProducts);

            // Apply text search
            if (query != null && !query.trim().isEmpty()) {
                String searchText = query.toLowerCase().trim();
                filteredProducts = filteredProducts.stream()
                        .filter(product ->
                                product.getName().toLowerCase().contains(searchText) ||
                                        (product.getDescription() != null &&
                                                product.getDescription().toLowerCase().contains(searchText)))
                        .collect(Collectors.toList());
            }

            // Apply filters
            for (String filter : filters) {
                if (filter.startsWith("category=")) {
                    String selectedCategory = filter.substring("category=".length());
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getCategories().contains(selectedCategory))
                            .collect(Collectors.toList());
                } else if (filter.startsWith("price>")) {
                    double minPrice = Double.parseDouble(filter.substring("price>".length()));
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getPrice() >= minPrice)
                            .collect(Collectors.toList());
                } else if (filter.startsWith("price<")) {
                    double maxPrice = Double.parseDouble(filter.substring("price<".length()));
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getPrice() <= maxPrice)
                            .collect(Collectors.toList());
                } else if (filter.startsWith("rating>")) {
                    double minRating = Double.parseDouble(filter.substring("rating>".length()));
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getAvgRating() >= minRating)
                            .collect(Collectors.toList());
                }
            }

            // Update the view with filtered products
            view.displayProducts(filteredProducts);

            // Show message if no products found
            if (filteredProducts.isEmpty() && !allStoreProducts.isEmpty()) {
                view.showInfo("No products match your filters");
            }
        } catch (Exception e) {
            view.showError("Error searching products: " + e.getMessage());
        }
    }

    /**
     * Adds a product to the user's cart.
     *
     * @param productId The ID of the product to add to the cart
     */
    public void addToCart(String productId) {
        try {
            String token = SecurityContextHolder.token();

            // For guests, create a session if needed
            if (token == null || token.isEmpty() || !SecurityContextHolder.isLoggedIn()) {
                // Try to create guest session
                Result<String> guestResult = userService.guestLogin();
                if (guestResult.isSuccess()) {
                    token = guestResult.getData();
                    SecurityContextHolder.storeGuestToken(token);
                } else {
                    view.showError("Failed to create guest session: " + guestResult.getErrorMessage());
                    return;
                }
            }

            // Add to cart using the token
            Result<String> result = userService.addToCart(token, productId, storeName);

            if (result.isSuccess()) {
                view.showSuccess("Added to cart!");
            } else {
                view.showError("Error: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error adding to cart: " + e.getMessage());
        }
    }
}