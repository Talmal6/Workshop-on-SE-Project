package com.SEGroup.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.SEGroup.DTO.*;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IProductCatalog;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Store.Auction;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Mapper.AuctionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import javax.naming.AuthenticationException;

/**
 * StoreService: handles store-related operations (public browsing, management)
 */
@Service
public class StoreService {

    private final IStoreRepository storeRepository;
    private final IProductCatalog productCatalog;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private final NotificationCenter notificationCenter;

    /**
     * Constructs a new StoreService instance with the provided dependencies.
     *
     * @param storeRepository The store repository for managing store data.
     * @param productCatalog The product catalog for managing product data.
     * @param authenticationService The authentication service for handling user sessions.
     * @param userRepository The user repository for managing user data.
     * @param notificationCenter The notification center for sending notifications.
     */
    @Autowired
    public StoreService(IStoreRepository storeRepository,
                        IProductCatalog productCatalog,
                        IAuthenticationService authenticationService,
                        IUserRepository userRepository,
                        @Autowired(required = false) NotificationCenter notificationCenter) {
        this.storeRepository = storeRepository;
        this.productCatalog = productCatalog;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.notificationCenter = notificationCenter;
    }

    /**
     * A version of StoreService constructor for backward compatibility.
     */
    public StoreService(IStoreRepository storeRepository,
                        IProductCatalog productCatalog,
                        IAuthenticationService authenticationService,
                        IUserRepository userRepository) {
        this(storeRepository, productCatalog, authenticationService, userRepository, null);
    }

    // ========================
    // Methods for Store Listing
    // ========================

    /**
     * Lists all stores for display in the card view.
     *
     * @return List of store card DTOs for all stores
     */
    public List<StoreCardDto> listAllStores() {
        return storeRepository.getAllStores()
                .stream()
                .map(this::toCard)
                .toList();
    }

    /**
     * Lists stores owned by a specific user for display in the card view.
     *
     * @param ownerEmail Email of the store owner
     * @return List of store card DTOs for stores owned by the specified user
     */
    public List<StoreCardDto> listStoresOwnedBy(String ownerEmail) {
        return storeRepository.getStoresOwnedBy(ownerEmail)
                .stream()
                .map(this::toCard)
                .toList();
    }

    /**
     * Helper method to convert StoreDTO to StoreCardDto
     */
    private StoreCardDto toCard(StoreDTO dto) {
        return new StoreCardDto(
                dto.getName(),
                dto.getFounderEmail(),
                dto.getAvgRating(),
                dto.getDescription());
    }

    /**
     * Checks if a user is an owner of a store.
     *
     * @param email The email of the user
     * @param storeName The name of the store
     * @return True if the user is an owner, false otherwise
     */
    public boolean isOwner(String email, String storeName) {
        return storeRepository.getAllOwners(storeName, email).contains(email);
    }

    // ========================
    // Store Management Methods
    // ========================

    /**
     * Creates a new store for the current user.
     * Non-Result version for AllStoresPresenter.
     *
     * @param token The authentication token
     * @param storeName The name of the new store
     * @throws Exception if creation fails
     */

    public void createStoreDirectly(String token, String storeName) throws Exception {
        // Validate the token
        authenticationService.checkSessionKey(token);
        String userEmail = authenticationService.getUserBySession(token);  // Fixed typo

        // Check if store name is valid
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be empty");
        }

        // Create the store
        storeRepository.createStore(storeName, userEmail);  // CORRECT ORDER

        LoggerWrapper.info("Created new store: " + storeName + " for user: " + userEmail);
    }

    /**
     * Creates a store for an authenticated user.
     * Result-returning version for backward compatibility.
     *
     * @param sessionKey The session key for the authenticated user.
     * @param storeName The name of the store to create.
     * @return A Result object indicating success or failure.
     */
    public Result<Void> createStore(String sessionKey, String storeName, String founderEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Creating store: " + storeName);
            storeRepository.createStore(storeName, founderEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error creating store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Closes a store for an authenticated user.
     *
     * @param sessionKey The session key for the authenticated user.
     * @param storeName The name of the store to close.
     * @return A Result object indicating success or failure.
     */
    public Result<Void> closeStore(String sessionKey, String storeName) {
        try {
            StoreDTO storeDTO = storeRepository.getStore(storeName);
            storeRepository.closeStore(storeName, authenticationService.getUserBySession(sessionKey));
            for (ShoppingProductDTO sp : storeDTO.getProducts()) {
                productCatalog.deleteStoreProductEntry(sp.getCatalogID(), storeName, sp.getProductId());
            }
            LoggerWrapper.info("Store closed: " + storeName);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error closing store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Reopens a store for an authenticated user.
     *
     * @param sessionKey The session key for the authenticated user.
     * @param storeName The name of the store to reopen.
     * @return A Result object indicating success or failure.
     */
    public Result<Void> reopenStore(String sessionKey, String storeName) {
        try {
            storeRepository.reopenStore(storeName, authenticationService.getUserBySession(sessionKey));
            for (ShoppingProductDTO sp : storeRepository.getStore(storeName).getProducts()) {
                productCatalog.addStoreProductEntry(sp.getCatalogID(), storeName, sp.getProductId(), sp.getPrice(),
                        sp.getQuantity(), sp.getAvgRating(), sp.getName());
            }
            LoggerWrapper.info("Store reopened: " + storeName);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error reopening store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Product Catalog Methods
    // ========================

    /**
     * Adds a product to the catalog.
     *
     * @param catalogID The ID of the product catalog.
     * @param name The name of the product.
     * @param brand The brand of the product.
     * @param description A description of the product.
     * @param categories The categories the product belongs to.
     * @return A Result object containing the catalog ID if successful, or an error message.
     */
    public Result<String> addProductToCatalog(String catalogID, String name, String brand, String description,
                                              List<String> categories) {
        try {
            LoggerWrapper.info("Adding product to catalog: " + catalogID);
            productCatalog.addCatalogProduct(catalogID, name, categories);
            return Result.success(catalogID);
        } catch (Exception e) {
            LoggerWrapper.error("Error adding product to catalog: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves the product catalog for public viewing.
     *
     * @return A Result object containing the list of catalog products if successful, or an error message.
     */
    public Result<List<CatalogProduct>> viewPublicProductCatalog() {
        try {
            LoggerWrapper.info("Fetching public product catalog.");
            return Result.success(productCatalog.getAllProducts());
        } catch (Exception e) {
            LoggerWrapper.error("Error fetching product catalog: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Fetches a single catalog product by its catalog ID.
     *
     * @param catalogId the unique ID of the catalog product
     * @return a Result containing a CatalogProductDTO on success, or an error message on failure
     */
    public Result<CatalogProductDTO> getCatalogProduct(String catalogId) {
        try {
            LoggerWrapper.info("Fetching catalog product: " + catalogId);
            CatalogProduct cp = productCatalog.getCatalogProduct(catalogId);
            if (cp == null) {
                return Result.failure("Catalog product not found: " + catalogId);
            }
            CatalogProductDTO dto = new CatalogProductDTO(
                    cp.getCatalogID(),
                    cp.getName(),
                    cp.getCategories()
            );
            return Result.success(dto);
        } catch (Exception e) {
            LoggerWrapper.error("Error fetching catalog product: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Store View Methods
    // ========================

    /**
     * Views a store by its name.
     *
     * @param storeName The name of the store to view.
     * @return A Result object containing the store DTO if successful, or an error message.
     */
    public Result<StoreDTO> viewStore(String storeName) {
        try {
            LoggerWrapper.info("Viewing store: " + storeName);
            return Result.success(storeRepository.getStore(storeName));
        } catch (Exception e) {
            LoggerWrapper.error("Error viewing store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves all stores for public viewing.
     *
     * @return A Result object containing the list of all store DTOs if successful, or an error message.
     */
    public Result<List<StoreDTO>> viewAllStores() {
        try {
            LoggerWrapper.info("Fetching all public stores.");
            return Result.success(storeRepository.getAllStores());
        } catch (Exception e) {
            LoggerWrapper.error("Error fetching all stores: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Product Management Methods
    // ========================

    /**
     * Adds a product to a store with image URL.
     *
     * @param sessionKey  The session key of the user performing the action.
     * @param storeName   The name of the store where the product will be added.
     * @param catalogID   The catalog ID of the product.
     * @param productName The name of the product.
     * @param description A description of the product.
     * @param price       The price of the product.
     * @param quantity    The quantity of the product.
     * @param imageURL    The image URL of the product.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<String> addProductToStore(String sessionKey, String storeName, String catalogID, String productName,
                                            String description, double price, int quantity, String imageURL) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            productCatalog.isProductExist(catalogID);
            String productID = storeRepository.addProductToStore(authenticationService.getUserBySession(sessionKey), storeName, catalogID,
                    productName, description, price, quantity, imageURL);
            productCatalog.addStoreProductEntry(catalogID, storeName, productID, price, quantity, 0, productName);
            LoggerWrapper.info("Added product to store: " + storeName + ", Product ID: " + productID);
            return Result.success(productID);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LoggerWrapper.error("Error adding product to store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Adds a product to a store without image URL (for backward compatibility).
     *
     * @param sessionKey  The session key of the user performing the action.
     * @param storeName   The name of the store where the product will be added.
     * @param catalogID   The catalog ID of the product.
     * @param productName The name of the product.
     * @param description A description of the product.
     * @param price       The price of the product.
     * @param quantity    The quantity of the product.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<String> addProductToStore(String sessionKey, String storeName, String catalogID, String productName,
                                            String description, double price, int quantity) {
        return addProductToStore(sessionKey, storeName, catalogID, productName, description, price, quantity, "");
    }

    /**
     * Updates a shopping product in the store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the product is located.
     * @param productID The ID of the product being updated.
     * @param description The updated description of the product.
     * @param price The updated price of the product.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> updateShoppingProduct(String sessionKey, String storeName, String productID, String description,
                                              Double price) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository.updateShoppingProduct(
                    authenticationService.getUserBySession(sessionKey), storeName, productID, price, description);
            productCatalog.updateStoreProductEntry(sp.getCatalogID(), storeName, productID, price, null, null);
            LoggerWrapper.info("Updated product in store: " + storeName + ", Product ID: " + productID);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error updating shopping product: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Deletes a shopping product from the store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the product is located.
     * @param productID The ID of the product to be deleted.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> deleteShoppingProduct(String sessionKey, String storeName, String productID) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository
                    .deleteShoppingProduct(authenticationService.getUserBySession(sessionKey), storeName, productID);
            productCatalog.deleteStoreProductEntry(sp.getCatalogID(), storeName, productID);
            LoggerWrapper.info("Deleted product from store: " + storeName + ", Product ID: " + productID);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error deleting shopping product: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Product Search/Query Methods
    // ========================

    /**
     * Retrieves a product from the store by its ID with detailed error handling.
     *
     * @param storeName The name of the store.
     * @param productId The ID of the product.
     * @return Result containing the product details or an error message.
     */
    public Result<ShoppingProductDTO> getProduct(String storeName, String productId) {
        try {
            // Get the product from the store repository
            ShoppingProductDTO product = storeRepository.getProduct(storeName, productId);

            if (product == null) {
                return Result.failure("Product not found: " + productId + " in store: " + storeName);
            }

            // Look up additional information from the catalog like the image URL
            List<StoreSearchEntry> entries = productCatalog.search(
                    product.getName(), List.of(), storeName, null
            );

            // Find the matching entry
            for (StoreSearchEntry entry : entries) {
                if (entry.getProductID().equals(productId) && entry.getStoreName().equals(storeName)) {
                    // If the entry has an image URL, update the product with it
                    if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
                        product.setImageUrl(entry.getImageUrl());
                    }
                    break;
                }
            }

            return Result.success(product);
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving product from store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Direct access to a product (for internal use).
     *
     * @param storeName The name of the store.
     * @param productId The ID of the product.
     * @return The product details or null if not found.
     */
    public ShoppingProductDTO getProductDirect(String storeName, String productId) {
        try {
            // Get the product from the store repository
            ShoppingProductDTO product = storeRepository.getProduct(storeName, productId);

            // Look up additional information from the catalog like the image URL
            if (product != null) {
                // Find the product entry in the catalog to get its image
                List<StoreSearchEntry> entries = productCatalog.search(
                        product.getName(), List.of(), storeName, null
                );

                // Find the matching entry
                for (StoreSearchEntry entry : entries) {
                    if (entry.getProductID().equals(productId) && entry.getStoreName().equals(storeName)) {
                        // If the entry has an image URL, update the product with it
                        if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
                            product.setImageUrl(entry.getImageUrl());
                        }
                        break;
                    }
                }
            }

            return product;
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving product from store: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets all products for a specific store.
     *
     * @param storeName The name of the store to get products for.
     * @return A Result object containing a list of products in the store if successful, or an error message.
     */
    public Result<List<ShoppingProductDTO>> getStoreProducts(String storeName) {
        try {
            LoggerWrapper.info("Fetching products for store: " + storeName);

            // Use the existing searchProducts method with empty query and filters
            return searchProducts("", List.of(), storeName, (PriceRangeDTO)null);
        } catch (Exception e) {
            LoggerWrapper.error("Error getting store products: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Gets all products available in the system, across all stores.
     * This is useful for the main catalog display.
     *
     * @return A Result object containing a list of all products if successful, or an error message.
     */
    public Result<List<ShoppingProductDTO>> getAllProducts() {
        try {
            LoggerWrapper.info("Fetching all products from all stores");

            List<ShoppingProductDTO> allProducts = new ArrayList<>();

            // Get all stores
            List<StoreDTO> stores = storeRepository.getAllStores();

            // For each store, get its products
            for (StoreDTO store : stores) {
                allProducts.addAll(store.getProducts());
            }
            for (ShoppingProductDTO p : allProducts) {
                List<StoreSearchEntry> entries = productCatalog.search(
                        p.getName(), List.of(), p.getStoreName(), null);

                for (StoreSearchEntry e : entries) {
                    if (e.getProductID().equals(p.getProductId()) &&
                            e.getStoreName().equals(p.getStoreName()) &&
                            e.getImageUrl() != null && !e.getImageUrl().isBlank()) {

                        p.setImageUrl(e.getImageUrl());
                        break;                 // picture found → stop inner loop
                    }
                }
            }

            return Result.success(allProducts);
        } catch (Exception e) {
            LoggerWrapper.error("Error getting all products: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves product details from a store.
     *
     * @param sessionKey The session key for authentication
     * @param storeName The name of the store
     * @param productId The ID of the product to retrieve
     * @return A Result object containing the product details if successful, or an error message
     */
    public Result<ShoppingProductDTO> getProductFromStore(String sessionKey, String storeName, String productId) {
        try {
            // Authenticate the session if provided
            if (sessionKey != null && !sessionKey.isEmpty()) {
                try {
                    authenticationService.checkSessionKey(sessionKey);
                } catch (Exception e) {
                    // Authentication failed but we'll continue for public products
                    LoggerWrapper.info("Non-authenticated product view request: " + e.getMessage());
                }
            }

            // Get the product from the store
            ShoppingProductDTO product = getProductDirect(storeName, productId);

            if (product != null) {
                LoggerWrapper.info("Retrieved product from store: " + storeName + ", Product: " + productId);
                return Result.success(product);
            } else {
                LoggerWrapper.info("Product not found: " + productId + " in store: " + storeName);
                return Result.failure("Product not found");
            }
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving product from store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Gets the quantity of a product in a store.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product
     * @return A Result containing the quantity or an error message
     */
    public Result<Integer> getProductQuantity(String sessionKey, String storeName, String productId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            return Result.success(storeRepository.getProductQuantity(storeName, productId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Searches for products based on search criteria with price range.
     *
     * @param query The search query to match product names.
     * @param filters List of category filters to apply.
     * @param storeName Optional store name to limit search to a specific store.
     * @param priceRange Optional price range to filter products.
     * @return A Result containing a list of matching shopping products.
     */
    public Result<List<ShoppingProductDTO>> searchProducts(String query,
                                                           List<String> filters,
                                                           String storeName,
                                                           PriceRangeDTO priceRange) {
        try {
            LoggerWrapper.info("Searching products with query: '" + query +
                    "', in store: " + storeName +
                    ", with filters: " + filters);

            // Get search entries from the product catalog
            List<StoreSearchEntry> entries = productCatalog.search(query, filters, storeName, null);
            List<ShoppingProductDTO> products = new ArrayList<>();

            // For each search entry, get the full product details
            for (StoreSearchEntry entry : entries) {
                ShoppingProductDTO product = storeRepository.getProduct(
                        entry.getStoreName(),
                        entry.getProductID()
                );

                if (product != null) {
                    // Add image URL from the catalog entry if available
                    if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
                        product.setImageUrl(entry.getImageUrl());
                    }

                    // Apply price range filter if provided
                    if (priceRange != null) {
                        double price = product.getPrice();
                        Double min = priceRange.getMin();
                        Double max = priceRange.getMax();
                        if ((min == null || price >= min) && (max == null || price <= max)) {
                            products.add(product);
                        }
                    } else {
                        products.add(product);
                    }
                }
            }

            return Result.success(products);
        } catch (Exception e) {
            LoggerWrapper.error("Error searching products: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Searches for products based on a query, search filters, and categories.
     * This overload is provided for compatibility with older code.
     *
     * @param query The search query to look for products.
     * @param searchFilters The filters to apply to the search.
     * @param storeName The store to search in.
     * @param categories The categories of products to filter by.
     * @return A Result object containing a list of found products if successful, or an error message.
     */
    public Result<List<ShoppingProductDTO>> searchProducts(String query, List<String> searchFilters, String storeName,
                                                           List<String> categories) {
        try {
            List<ShoppingProductDTO> searchResults = new ArrayList<>();
            for (StoreSearchEntry spe : productCatalog.search(query, searchFilters, storeName, categories)) {
                ShoppingProductDTO p = storeRepository.getProduct(spe.getStoreName(), spe.getProductID());
                if (p != null && spe.getImageUrl() != null && !spe.getImageUrl().isBlank()) {
                    p.setImageUrl(spe.getImageUrl());        // copy picture into the DTO
                }
                if (p != null) {
                    searchResults.add(p);
                }
            }
            LoggerWrapper.info("Searched products in store: " + storeName + ", Query: " + query);
            return Result.success(searchResults);
        } catch (Exception e) {
            LoggerWrapper.error("Error searching products: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Rating Methods
    // ========================

    /**
     * Rates a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store being rated.
     * @param rating The rating given to the store.
     * @param review The review written for the store.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> rateStore(String sessionKey, String storeName, int rating, String review) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.rateStore(authenticationService.getUserBySession(sessionKey), storeName, rating, review);
            LoggerWrapper.info("Rated store: " + storeName + ", Rating: " + rating);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error rating store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Rates a product.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the product is located.
     * @param productID The ID of the product being rated.
     * @param rating The rating given to the product.
     * @param review The review written for the product.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> rateProduct(String sessionKey, String storeName, String productID, int rating, String review) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository.rateProduct(authenticationService.getUserBySession(sessionKey),
                    storeName, productID, rating, review);
            productCatalog.updateStoreProductEntry(sp.getCatalogID(), storeName, productID, null, null, sp.getAvgRating());
            LoggerWrapper.info("Rated product in store: " + storeName + ", Product ID: " + productID + ", Rating: " + rating);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error rating product: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Gets all ratings for a store.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param storeName The name of the store.
     * @return A Result containing the map of user IDs to ratings.
     */
    public Result<Map<String,Store.Rating>> getAllRatings(String sessionKey, String storeName)
            throws AuthenticationException {
        authenticationService.checkSessionKey(sessionKey);
        Map<String,Store.Rating> ratings = storeRepository.findRatingsByStore(storeName);
        return Result.success(ratings);
    }

    // ========================
    // Owner/Manager Methods
    // ========================

    /**
     * Appoints a new owner for a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the owner is being appointed.
     * @param apointeeEmail The email of the new owner.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> appointOwner(String sessionKey, String storeName, String apointeeEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.appointOwner(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail);
            userRepository.appointOwner(storeName, apointeeEmail);
            LoggerWrapper.info("Appointed new owner for store: " + storeName + ", New Owner: " + apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error appointing owner: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Removes an owner from a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store from which the owner is being removed.
     * @param apointeeEmail The email of the owner being removed.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> removeOwner(String sessionKey, String storeName, String apointeeEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.removeOwner(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail);
            userRepository.removeOwner(storeName, apointeeEmail);
            LoggerWrapper.info("Removed owner from store: " + storeName + ", Owner: " + apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error removing owner: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Allows an owner to resign ownership of a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store from which the owner is resigning.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> resignOwnership(String sessionKey, String storeName) {
        try {
            authenticationService.authenticate(sessionKey);
            String userEmail = authenticationService.getUserBySession(sessionKey);
            storeRepository.resignOwnership(storeName, userEmail);
            userRepository.removeOwner(storeName, userEmail);
            LoggerWrapper.info("Owner resigned from store: " + storeName + ", Owner: " + userEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error resigning ownership: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Appoints a new manager to a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the manager is being appointed.
     * @param apointeeEmail The email of the new manager.
     * @param permissions The permissions granted to the manager.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> appointManager(String sessionKey, String storeName, String apointeeEmail,
                                       List<String> permissions) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.appointManager(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail,
                    permissions);
            userRepository.appointManager(storeName, apointeeEmail);
            LoggerWrapper.info("Appointed manager for store: " + storeName + ", Manager: " + apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error appointing manager: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Updates the permissions of a manager in a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the manager's permissions are being updated.
     * @param apointeeEmail The email of the manager whose permissions are being updated.
     * @param permissions The updated list of permissions for the manager.
     * @return A Result object indicating the success or failure of the operation.
     */
    public Result<Void> updateManagerPermissions(String sessionKey, String storeName, String apointeeEmail,
                                                 List<String> permissions) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.updateManagerPermissions(storeName, authenticationService.getUserBySession(sessionKey),
                    apointeeEmail, permissions);
            LoggerWrapper.info("Updated manager permissions for store: " + storeName + ", Manager: " + apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error updating manager permissions: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves the permissions of a manager in a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the manager's permissions are being retrieved.
     * @param managerEmail The email of the manager whose permissions are being retrieved.
     * @return A Result object containing the manager's permissions if successful, or an error message.
     */
    public Result<List<String>> getManagerPermission(String sessionKey, String storeName, String managerEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(storeRepository.getManagerPermissions(storeName,
                    authenticationService.getUserBySession(sessionKey), managerEmail));
        } catch (Exception e) {
            LoggerWrapper.error("Error getting manager permissions: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves a list of all owners for a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the owners are being retrieved.
     * @param operatorEmail The email of the operator performing the action.
     * @return A Result object containing a list of all owners if successful, or an error message.
     */
    public Result<List<String>> getAllOwners(String sessionKey, String storeName, String operatorEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(
                    storeRepository.getAllOwners(storeName, authenticationService.getUserBySession(sessionKey)));
        } catch (Exception e) {
            LoggerWrapper.error("Error getting all owners: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Retrieves a list of all managers for a store.
     *
     * @param sessionKey The session key of the user performing the action.
     * @param storeName The name of the store where the managers are being retrieved.
     * @param operatorEmail The email of the operator performing the action.
     * @return A Result object containing a list of all managers if successful, or an error message.
     */
    public Result<List<String>> getAllManagers(String sessionKey, String storeName, String operatorEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(
                    storeRepository.getAllManagers(storeName, authenticationService.getUserBySession(sessionKey)));
        } catch (Exception e) {
            LoggerWrapper.error("Error getting all managers: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    // ========================
    // Auction Methods
    // ========================

    /**
     * Starts an auction for a product in a store.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product to auction
     * @param startingPrice The starting price for the auction
     * @param durationMillis The duration of the auction in milliseconds
     * @return A Result indicating success or failure
     */
    public Result<Void> startAuction(String sessionKey,
                                     String storeName,
                                     String productId,
                                     double startingPrice,
                                     long durationMillis) {
        try {
            // 1) Authenticate
            authenticationService.checkSessionKey(sessionKey);

            // 2) Kick‐off the auction in the domain
            storeRepository.startAuction(storeName, productId, startingPrice, durationMillis);

            // 3) Build a user‐facing message
            String operator = authenticationService.getUserBySession(sessionKey);
            long mins = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
            String msg = String.format(
                    "🔨 Auction started on %s/%s at $%.2f, ends in %d min",
                    storeName, productId, startingPrice, mins);

            // 4) Gather store owners & managers
            List<String> recipients = new ArrayList<>(storeRepository.getAllOwners(storeName, operator));
            recipients.addAll(storeRepository.getAllManagers(storeName, operator));

            // 5) Fire off system notifications
            if (notificationCenter != null) {
                for (String userId : recipients) {
                    notificationCenter.sendSystemNotification(sessionKey, userId, msg);
                }
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Places a bid on an auction.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product being auctioned
     * @param amount The bid amount
     * @return A Result containing a boolean indicating success or failure
     */
    public Result<Boolean> placeBidOnAuction(String sessionKey,
                                             String storeName,
                                             String productId,
                                             double amount) {
        try {
            // 1) Authenticate
            authenticationService.checkSessionKey(sessionKey);
            String who = authenticationService.getUserBySession(sessionKey);

            // 2) Attempt the bid
            boolean ok = storeRepository.bidOnAuction(who, storeName, productId, amount);
            if (!ok) {
                return Result.failure("Bid too low or auction closed");
            }

            // 3) Notify the store owners
            if (notificationCenter != null) {
                String msg = String.format("💰 %s placed a bid of $%.2f on %s/%s",
                        who, amount, storeName, productId);
                for (String ownerEmail : storeRepository.getAllOwners(storeName, who)) {
                    // sendUserNotification includes a senderId
                    notificationCenter.sendUserNotification(sessionKey, ownerEmail, msg, who);
                }
            }

            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Gets information about an active auction.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product being auctioned
     * @return A Result containing auction information or an error message
     */
    public Result<AuctionDTO> getAuction(String sessionKey,
                                         String storeName,
                                         String productId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            var a = storeRepository.getAuctionInfo(storeName, productId);
            if (a == null || a.isEnded()) {
                return Result.failure("No active auction");
            }
            return Result.success(AuctionMapper.toDTO(storeName, productId, a));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Submits a bid for a shopping item.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product
     * @param bidAmount The amount to bid
     * @return A Result indicating success or failure
     */
    public Result<Void> submitBidToShoppingItem(String sessionKey,
                                                String storeName,
                                                String productId,
                                                double bidAmount) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.submitBidToShoppingItem(authenticationService.getUserBySession(sessionKey), storeName, productId, bidAmount);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Gets users who have placed bids on a product.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product
     * @return A Result containing the list of bidder emails
     */
    public Result<List<String>> getBidUsers(String sessionKey,
                                            String storeName,
                                            String productId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            if (isOwner(authenticationService.getUserBySession(sessionKey), storeName)) {
                List<String> users = storeRepository.getBidUsers(storeName, productId);
                return Result.success(users);
            }
            return Result.failure("User is not an owner of the store");
        } catch (Exception e) {
            LoggerWrapper.error("Error fetching bid users: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Sends an auction offer.
     *
     * @param sessionKey The session key of the authenticated user
     * @param storeName The name of the store
     * @param productId The ID of the product
     * @param bidAmount The amount to bid
     * @return A Result indicating success or failure
     */
    public Result<Void> sendAuctionOffer(String sessionKey,
                                         String storeName,
                                         String productId,
                                         double bidAmount) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.sendAuctionOffer(authenticationService.getUserBySession(sessionKey), storeName, productId, bidAmount);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Sorts stores by rating in descending order.
     *
     * @return List of all stores sorted by rating (highest first)
     */
    public List<StoreCardDto> sortByRating() {
        try {
            return listAllStores().stream()
                    .sorted((s1, s2) -> Double.compare(s2.rating(), s1.rating()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LoggerWrapper.error("Failed to sort stores by rating: " + e.getMessage(), e);
            return List.of();
        }
    }

    public Result<Void> createStore(String sessionKey, String storeName) {
        try {
            // Call the renamed method
            createStoreDirectly(sessionKey, storeName);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error creating store: " + e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }
}