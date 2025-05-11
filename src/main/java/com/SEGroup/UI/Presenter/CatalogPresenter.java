package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.CatalogProductDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.CatalogView;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogPresenter {
    private final CatalogView view;
    private final StoreService storeService;
    private final UserService userService;
    private List<ShoppingProductDTO> currentProducts = new ArrayList<>();

    public CatalogPresenter(CatalogView view) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
        this.userService = ServiceLocator.getUserService();
    }

    /**
     * Loads all products initially.
     */
    /**
     * Loads all products initially.
     */
// Fix for displayBaseCatalog method in GeneralCatalogView.java
// Enhancement for CatalogPresenter.java to better handle categories
// Update the loadProducts method
    public void loadProducts() {
        try {
            Result<List<ShoppingProductDTO>> result = storeService.getAllProducts();

            if (result.isSuccess()) {
                List<ShoppingProductDTO> products = result.getData();

                // Ensure non-null category lists
                for (ShoppingProductDTO product : products) {
                    if (product.getCategories() == null) {
                        product.setCategories(new ArrayList<>());
                    }
                }

                // Enrich each product with its master-catalog categories
                enrichProductCategories(products);

                // Save and display
                currentProducts = products;
                view.displayProducts(currentProducts);

            } else {
                view.showError("Failed to load products: " + result.getErrorMessage());
                view.displayProducts(new ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error loading products: " + e.getMessage());
            view.displayProducts(new ArrayList<>());
        }
    }


    // Category-specific filter method for the CatalogView
    public void filterByCategory(String category) {
        if (category == null || category.isEmpty()) {
            // If no category selected, show all products
            view.displayProducts(currentProducts);
            return;
        }

        System.out.println("Filtering by category: " + category);

        List<ShoppingProductDTO> filteredProducts = currentProducts.stream()
                .filter(product -> {
                    if (product.getCategories() == null) {
                        System.out.println("Warning: Product " + product.getName() + " has null categories");
                        return false;
                    }
                    boolean contains = product.getCategories().contains(category);
                    if (contains) {
                        System.out.println("Product " + product.getName() + " matches category " + category);
                    }
                    return contains;
                })
                .collect(Collectors.toList());

        System.out.println("Found " + filteredProducts.size() + " products in category " + category);
        view.displayProducts(filteredProducts);

        if (filteredProducts.isEmpty()) {
            view.showInfo("No products found in category: " + category);
        }
    }
    /**
     * Searches for products with the given query and filters.
     */
    // In CatalogPresenter.java - Update the searchProducts method
// In CatalogPresenter.java - Update the searchProducts method
// In CatalogPresenter.java - Enhance searchProducts method
    // In CatalogPresenter.java - Improve searchProducts method
    public void searchProducts(String query, List<String> filters) {
        try {
            System.out.println("Searching for: " + query + " with filters: " + String.join(", ", filters));

            // Extract filter criteria
            String storeName = null;
            List<String> categories = new ArrayList<>();
            List<String> otherFilters = new ArrayList<>();
            Double minRating = null;
            Double minPrice = null;
            Double maxPrice = null;

            // Process filters
            for (String filter : filters) {
                if (filter.startsWith("category=")) {
                    categories.add(filter.substring("category=".length()));
                } else if (filter.startsWith("store=")) {
                    storeName = filter.substring("store=".length());
                } else if (filter.startsWith("rating>")) {
                    minRating = Double.parseDouble(filter.substring("rating>".length()));
                } else if (filter.startsWith("price>")) {
                    minPrice = Double.parseDouble(filter.substring("price>".length()));
                } else if (filter.startsWith("price<")) {
                    maxPrice = Double.parseDouble(filter.substring("price<".length()));
                } else {
                    otherFilters.add(filter);
                }
            }

            // Get all products as a base
            Result<List<ShoppingProductDTO>> allProductsResult = storeService.getAllProducts();
            List<ShoppingProductDTO> enriched = allProductsResult.getData();
            for (ShoppingProductDTO p : enriched) {
                if (p.getCategories() == null) {
                    p.setCategories(new ArrayList<>());
                }
            }
            enrichProductCategories(enriched);
            currentProducts = enriched;



            if (allProductsResult.isSuccess()) {
                List<ShoppingProductDTO> filteredProducts = new ArrayList<>(currentProducts);

                System.out.println("Initial product count: " + filteredProducts.size());

                // Apply text search if provided
                if (query != null && !query.trim().isEmpty()) {
                    String lowerQuery = query.toLowerCase().trim();
                    filteredProducts = filteredProducts.stream()
                            .filter(p ->
                                    // Search by name
                                    (p.getName() != null && p.getName().toLowerCase().contains(lowerQuery)) ||
                                            // Search by description
                                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery)) ||
                                            // Search by ID (catalog ID or product ID)
                                            (p.getProductId() != null && p.getProductId().toLowerCase().contains(lowerQuery)) ||
                                            (p.getCatalogID() != null && p.getCatalogID().toLowerCase().contains(lowerQuery)) ||
                                            // Search by store name
                                            (p.getStoreName() != null && p.getStoreName().toLowerCase().contains(lowerQuery))
                            )
                            .collect(Collectors.toList());
                    System.out.println("After text search: " + filteredProducts.size() + " products");
                }

                if (!categories.isEmpty()) {
                    final Set<String> categorySet = new HashSet<>(categories);
                    filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getCategories() != null &&
                                    p.getCategories().stream().anyMatch(categorySet::contains))
                            .collect(Collectors.toList());
                    System.out.println("After category filter: " + filteredProducts.size() + " products");
                }

                // Apply store filter
                if (storeName != null && !storeName.isEmpty()) {
                    final String storeNameFinal = storeName;
                    filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getStoreName().equals(storeNameFinal))
                            .collect(Collectors.toList());
                    System.out.println("After store filter: " + filteredProducts.size() + " products");
                }

                // Apply price filters
                if (minPrice != null) {
                    final Double finalMinPrice = minPrice;
                    filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getPrice() >= finalMinPrice)
                            .collect(Collectors.toList());
                    System.out.println("After min price filter: " + filteredProducts.size() + " products");
                }

                if (maxPrice != null) {
                    final Double finalMaxPrice = maxPrice;
                    filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getPrice() <= finalMaxPrice)
                            .collect(Collectors.toList());
                    System.out.println("After max price filter: " + filteredProducts.size() + " products");
                }

                // Apply rating filter
                if (minRating != null) {
                    final Double finalMinRating = minRating;
                    filteredProducts = filteredProducts.stream()
                            .filter(p -> p.getAvgRating() >= finalMinRating)
                            .collect(Collectors.toList());
                    System.out.println("After rating filter: " + filteredProducts.size() + " products");
                }

                // Update current products and view
                currentProducts = filteredProducts;
                view.displayProducts(currentProducts);

                if (filteredProducts.isEmpty()) {
                    view.showInfo("No products found matching your criteria");
                }
            } else {
                view.showError("Failed to search products: " + allProductsResult.getErrorMessage());
                view.displayProducts(new ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            view.showError("Error searching products: " + e.getMessage());
            view.displayProducts(new ArrayList<>());
        }
    }
    /**
     * Sorts the current products by name
     * @param ascending true for A-Z, false for Z-A
     */
    public void sortByName(boolean ascending) {
        try {
            List<ShoppingProductDTO> sortedProducts = new ArrayList<>(currentProducts);
            if (ascending) {
                sortedProducts.sort(Comparator.comparing(ShoppingProductDTO::getName));
            } else {
                sortedProducts.sort(Comparator.comparing(ShoppingProductDTO::getName).reversed());
            }
            view.displayProducts(sortedProducts);
        } catch (Exception e) {
            view.showError("Error sorting products: " + e.getMessage());
        }
    }

    /**
     * Sorts the current products by price
     * @param ascending true for low-high, false for high-low
     */
    public void sortByPrice(boolean ascending) {
        try {
            List<ShoppingProductDTO> sortedProducts = new ArrayList<>(currentProducts);
            if (ascending) {
                sortedProducts.sort(Comparator.comparing(ShoppingProductDTO::getPrice));
            } else {
                sortedProducts.sort(Comparator.comparing(ShoppingProductDTO::getPrice).reversed());
            }
            view.displayProducts(sortedProducts);
        } catch (Exception e) {
            view.showError("Error sorting products: " + e.getMessage());
        }
    }

    /**
     * Sorts the current products by rating (highest first)
     */
    public void sortByRating() {
        try {
            List<ShoppingProductDTO> sortedProducts = new ArrayList<>(currentProducts);
            sortedProducts.sort(Comparator.comparing(ShoppingProductDTO::getAvgRating).reversed());
            view.displayProducts(sortedProducts);
        } catch (Exception e) {
            view.showError("Error sorting products: " + e.getMessage());
        }
    }

    /**
     * Adds a product to the cart.
     */
// In CatalogPresenter.java - Fix addToCart method
    // CatalogPresenter.java - Improved addToCart method
// CatalogPresenter.java - Improved addToCart method
    public void addToCart(String productId, String storeName) {
        try {
            System.out.println("Adding to cart: " + productId + " from store: " + storeName);
            String token = SecurityContextHolder.token();
            boolean isGuest = !SecurityContextHolder.isLoggedIn();

            System.out.println("Current user is " + (isGuest ? "a guest" : "logged in") +
                    ", token: " + (token != null ? (token.substring(0, Math.min(token.length(), 10)) + "...") : "null"));

            // For guests, create a session if needed
            if (token == null || token.isEmpty()) {
                System.out.println("No token found, creating guest session");
                Result<String> guestResult = userService.guestLogin();
                if (guestResult.isSuccess()) {
                    token = guestResult.getData();
                    System.out.println("Created guest token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                    // Store the guest token in session for future use
                    SecurityContextHolder.storeGuestToken(token);
                } else {
                    view.showError("Failed to create guest session: " + guestResult.getErrorMessage());
                    return;
                }
            }

            // Add to cart using the token
            System.out.println("Adding to cart with token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
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
    /**
     * Navigates to the product details page.
     */
    public void viewProductDetails(ShoppingProductDTO product) {
        try {
            view.navigateToProduct(product.getProductId(), product.getStoreName());
        } catch (Exception e) {
            view.showError("Error navigating to product: " + e.getMessage());
        }
    }

    private void enrichProductCategories(List<ShoppingProductDTO> products) {
        for (ShoppingProductDTO product : products) {
            try {
                Result<CatalogProductDTO> catRes = storeService.getCatalogProduct(product.getCatalogID());
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
}