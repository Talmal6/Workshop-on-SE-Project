package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Domain.User.Basket;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Service.*;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.CartView;
import com.SEGroup.UI.Views.CheckoutDialog;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartPresenter {
    private final CartView view;
    private final UserService userService;
    private final StoreService storeService;
    private final TransactionService transactionService;
    private List<CartView.ShoppingCartProduct> currentCartProducts = new ArrayList<>();
    private double cartTotal = 0.0;

    public CartPresenter(CartView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
        this.storeService = ServiceLocator.getStoreService();
        this.transactionService = ServiceLocator.getTransactionService();
    }

    /**
     * Loads the cart data from the service layer.
     * Works for both guest and authenticated users.
     */
    public void loadCart() {
        try {
            String token = SecurityContextHolder.token();
            boolean isLoggedIn = SecurityContextHolder.isLoggedIn();

            System.out.println("Loading cart for " + (isLoggedIn ? "authenticated user" : "guest user"));
            System.out.println("Token: " + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null"));

            // Handle guest users or missing tokens
            if (token == null || token.isEmpty()) {
                System.out.println("No token found, creating guest session");
                Result<String> guestResult = userService.guestLogin();
                if (guestResult.isSuccess()) {
                    token = guestResult.getData();
                    System.out.println("Created guest token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                    SecurityContextHolder.storeGuestToken(token);
                } else {
                    view.showError("Failed to create guest session: " + guestResult.getErrorMessage());
                    view.displayCart(new ArrayList<>(), new ArrayList<>());
                    return;
                }
            }

            // Different path for guests vs registered users
            List<BasketDTO> baskets;
            if (isLoggedIn) {
                // For registered users
                String email = SecurityContextHolder.email();
                Result<List<BasketDTO>> result = userService.getUserCart(token, email);

                if (!result.isSuccess()) {
                    System.out.println("Error loading cart: " + result.getErrorMessage());
                    view.showError("Error loading cart: " + result.getErrorMessage());
                    view.displayCart(new ArrayList<>(), new ArrayList<>());
                    return;
                }

                baskets = result.getData();
            } else {
                // For guests - Directly access the GuestService
                try {
                    // Get the GuestService
                    GuestService guestService = ServiceLocator.getGuestService();
                    ShoppingCart guestCart = guestService.cart(token);

                    // Convert ShoppingCart to BasketDTO list
                    baskets = new ArrayList<>();

                    // Get the snapshot of the shopping cart
                    Map<String, Basket> cartSnapshot = guestCart.snapShot();

                    // Convert each Basket to a BasketDTO
                    for (Map.Entry<String, Basket> entry : cartSnapshot.entrySet()) {
                        String storeId = entry.getKey();
                        Basket basket = entry.getValue();

                        // Get the product map from the basket using the snapshot() method
                        Map<String, Integer> productMap = basket.snapshot();

                        // Create a BasketDTO
                        baskets.add(new BasketDTO(storeId, productMap));
                    }

                    System.out.println("Successfully loaded guest cart with " + baskets.size() + " baskets");
                } catch (Exception e) {
                    System.err.println("Error accessing guest cart: " + e.getMessage());
                    e.printStackTrace();
                    view.showError("Error loading cart: " + e.getMessage());
                    view.displayCart(new ArrayList<>(), new ArrayList<>());
                    return;
                }
            }

            // Continue with common code for both user types
            List<CartView.ShoppingCartProduct> products = fetchProductDetails(baskets);
            currentCartProducts = products;

            // Calculate cart total
            calculateCartTotal(baskets, products);

            view.displayCart(baskets, products);
            System.out.println("Successfully loaded cart with " + products.size() + " products");

        } catch (Exception e) {
            System.err.println("Cart loading error: " + e.getMessage());
            e.printStackTrace();
            view.showError("Cart loading error: " + e.getMessage());
            view.displayCart(new ArrayList<>(), new ArrayList<>());
            cartTotal = 0.0;
        }
    }

    /**
     * Calculates the total price of all items in the cart.
     */
    private void calculateCartTotal(List<BasketDTO> baskets, List<CartView.ShoppingCartProduct> products) {
        cartTotal = 0.0;

        for (CartView.ShoppingCartProduct product : products) {
            // Find the basket containing this product
            for (BasketDTO basket : baskets) {
                if (basket.storeId().equals(product.getStoreName()) &&
                        basket.prod2qty().containsKey(product.getId())) {

                    int quantity = basket.prod2qty().get(product.getId());
                    cartTotal += product.getPrice() * quantity;
                }
            }
        }
    }

    /**
     * Returns the current cart total price.
     */
    public double getCartTotal() {
        return cartTotal;
    }

    /**
     * Fetches product details for all products in the baskets.
     */
    private List<CartView.ShoppingCartProduct> fetchProductDetails(List<BasketDTO> baskets) {
        List<CartView.ShoppingCartProduct> products = new ArrayList<>();

        try {
            for (BasketDTO basket : baskets) {
                String storeName = basket.storeId();
                System.out.println("Processing basket for store: " + storeName);

                for (Map.Entry<String, Integer> entry : basket.prod2qty().entrySet()) {
                    String productId = entry.getKey();
                    int quantity = entry.getValue();

                    // Skip products with quantity 0
                    if (quantity <= 0) {
                        continue;
                    }

                    // Fetch actual product details from store
                    System.out.println("Fetching product: " + productId + " from store: " + storeName);
                    Result<ShoppingProductDTO> productResult = storeService.getProductFromStore(SecurityContextHolder.token(),storeName, productId);

                    if (productResult.isSuccess() && productResult.getData() != null) {
                        ShoppingProductDTO product = productResult.getData();
                        // Create CartView.ShoppingCartProduct with 4 parameters to match the class definition
                        CartView.ShoppingCartProduct cartProduct = new CartView.ShoppingCartProduct(
                                productId,
                                product.getName(),
                                storeName,
                                product.getPrice()
                        );
                        products.add(cartProduct);
                        System.out.println("Added product to cart: " + product.getName());
                    } else {
                        // Fallback if product details not available - using 4 parameters
                        System.out.println("Product details not available, using fallback");
                        products.add(new CartView.ShoppingCartProduct(
                                productId,
                                "Product " + productId,
                                storeName,
                                0.0
                        ));
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't fail completely
            System.err.println("Error fetching product details: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

    /**
     * Changes the quantity of an item in the cart.
     */
    public void changeQuantity(String productId, String storeName, int quantity) {
        try {
            String token = SecurityContextHolder.token();
            if (token == null || token.isEmpty()) {
                view.showError("No active session found");
                return;
            }

            // If quantity is 0, remove the item
            if (quantity <= 0) {
                removeItem(productId, storeName);
                return;
            }

            Result<String> result = userService.changeCartQuantity(token, productId, storeName, quantity);

            if (result.isSuccess()) {
                loadCart(); // Reload the cart to show updated quantities
                view.showSuccess("Cart updated");
            } else {
                view.showError("Failed to update quantity: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error updating quantity: " + e.getMessage());
        }
    }

    /**
     * Removes an item from the cart.
     */
    public void removeItem(String productId, String storeName) {
        try {
            String token = SecurityContextHolder.token();
            if (token == null || token.isEmpty()) {
                view.showError("No active session found");
                return;
            }

            Result<String> result = userService.removeFromCart(token, productId, storeName);

            if (result.isSuccess()) {
                loadCart(); // Reload the cart
                view.showSuccess("Item removed from cart");
            } else {
                view.showError("Failed to remove item: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error removing item: " + e.getMessage());
        }
    }

    /**
     * Clears the entire cart.
     */
    public void clearCart() {
        try {
            // We need to manually clear each item since there might not be a direct clearCart method
            String token = SecurityContextHolder.token();
            if (token == null || token.isEmpty()) {
                view.showError("No active session found");
                return;
            }

            // Make a copy of the current products to avoid concurrent modification
            List<CartView.ShoppingCartProduct> productsCopy = new ArrayList<>(currentCartProducts);

            for (CartView.ShoppingCartProduct product : productsCopy) {
                userService.removeFromCart(token, product.getId(), product.getStoreName());
            }

            loadCart(); // Reload cart after clearing
            view.showSuccess("Cart cleared");
        } catch (Exception e) {
            view.showError("Error clearing cart: " + e.getMessage());
        }
    }

    /**
     * Proceeds to checkout.
     */
    public void checkout() {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                UI.getCurrent().navigate("signin");
                view.showError("Please sign in to checkout");
                return;
            }

            CheckoutDialog dialog = new CheckoutDialog(this);
            dialog.open();
        } catch (Exception e) {
            view.showError("Error during checkout: " + e.getMessage());
        }
    }

    /**
     * Processes the checkout with the provided credit card details.
     *
     * @param creditCardDetails The credit card and shipping details
     * @return True if checkout was successful, false otherwise
     */
    public boolean onCheckout(CheckoutDialog.CreditCardDetails creditCardDetails) {
        try {
            String token = SecurityContextHolder.token();
            String email = SecurityContextHolder.email();

            if (token == null || email == null) {
                view.showError("You must be logged in to complete checkout");
                return false;
            }

            // Create payment details string from credit card information
            String paymentDetails = createPaymentDetailsString(creditCardDetails);

            // Use the TransactionService for checkout rather than UserService
            Result<Void> result = transactionService.purchaseShoppingCart(
                    token,
                    email,
                    paymentDetails
            );

            if (result.isSuccess()) {
                view.showSuccess("Order placed successfully!");
                loadCart(); // Reload cart (should be empty after checkout)
                return true;
            } else {
                view.showError("Checkout failed: " + result.getErrorMessage());
                return false;
            }

        } catch (Exception e) {
            view.showError("Error processing checkout: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a payment details string from credit card information.
     *
     * @param creditCardDetails The credit card details
     * @return A formatted payment details string
     */
    private String createPaymentDetailsString(CheckoutDialog.CreditCardDetails creditCardDetails) {
        // Format payment details as needed by the payment processor
        return String.format("CARD:%s;NAME:%s;EXP:%s;ADDR:%s,%s,%s,%s",
                creditCardDetails.getCardNumber(),
                creditCardDetails.getCardHolder(),
                creditCardDetails.getExpiryDate(),
                creditCardDetails.getAddress(),
                creditCardDetails.getCity(),
                creditCardDetails.getZipCode(),
                creditCardDetails.getCountry());
    }

    public Result<Void> applyCoupon(String value) {
        //todo!
        return null;
    }

    public double getCartTotalAfterDiscount() {
        Result<Map<String, Double>> result = transactionService.getDiscountsForCart(SecurityContextHolder.token());
        if (!result.isSuccess()) {
            view.showError("Failed to get discounts: " + result.getErrorMessage());
        }else {
            Map<String, Double> discounts = result.getData();
            double totalAfterDiscount = 0;
            for (double discountedPrice : discounts.values()) {
                totalAfterDiscount += discountedPrice;
            }
            return totalAfterDiscount;
        }
        return cartTotal;
    }
}