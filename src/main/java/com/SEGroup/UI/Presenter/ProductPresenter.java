package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.AuctionDTO;
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
    private boolean isBidStarted;
    private AuctionDTO auction;

    public ProductPresenter(ProductView view, String productId, String storeName) {
        this.view = view;
        this.productId = productId;
        this.storeName = storeName;
        this.storeService = ServiceLocator.getStoreService();
        this.userService = ServiceLocator.getUserService();
        this.isBidStarted = false;
    }


    public AuctionDTO getAuction() {
        return auction;
    }
    public void loadAuctionInfo() {
        Result<AuctionDTO> r = storeService.getAuction(
                SecurityContextHolder.token(), storeName, productId);
        if (r.isSuccess()) {
            auction = r.getData();
            view.displayAuctionInfo(auction);
        }
    }

    // In ProductPresenter.java
    public void placeBid(double amount) {
        Result<Boolean> r = storeService.placeBidOnAuction(
                SecurityContextHolder.token(),
                storeName,
                productId,
                amount
        );
        if (r.isSuccess()) {
            if (Boolean.TRUE.equals(r.getData())) {
                view.showSuccess("Bid placed!");
            } else {
                view.showError("Your bid was too low or the auction is closed.");
            }
        } else {
            view.showError("Failed to place bid: " + r.getErrorMessage());
        }
    }

    /**
     * Loads the product details from the service and updates the view.
     */
// ProductPresenter.java - Fix loadProductDetails method
// ProductPresenter.java - Fix loadProductDetails method
    public void loadProductDetails() {
        try {
            System.out.println("Loading product details for: " + productId + " in store: " + storeName);
            Result<ShoppingProductDTO> productResult = storeService.getProduct(storeName, productId);

            if (productResult.isSuccess() && productResult.getData() != null) {
                this.product = productResult.getData();
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
                    view.showError("Error loading product: " + (productResult.isSuccess() ? "No data found" : productResult.getErrorMessage()));
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


    public void startAuction(double startingPrice, long durationMillis) {
        String token = SecurityContextHolder.token();
        Result<Void> r = storeService.startAuction(token, storeName, productId, startingPrice, durationMillis);
        if (r.isSuccess()) {
            view.showSuccess("Auction started!");
            loadAuctionInfo();         // refresh the auction bar below
        } else {
            view.showError("Failed to start auction: " + r.getErrorMessage());
        }
    }

    public void bidBuy(String amount1){
        if(isBidStarted) {
            double amount = Double.parseDouble((amount1.trim()));
            Result<Void> res = this.storeService.submitBidToShoppingItem(SecurityContextHolder.token(), this.storeName, this.productId, amount);
            if (res.isSuccess()) {
                this.view.showSuccess("Buying well done..Good Luck");
            } else {
                this.view.showError("Problem caught: " + res.getErrorMessage());
            }
        }
    }
    public String getProductName() {
        return product != null
                ? product.getName()
                : "";
    }

    public boolean isOwner(){
        return SecurityContextHolder.isStoreOwner();
    }
    public String getProductId() {
        return productId;
    }

    public String getStoreName() {
        return storeName;
    }
}