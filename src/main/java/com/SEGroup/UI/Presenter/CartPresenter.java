package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Domain.User.Basket;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.UserService;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.MainLayout;
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
    private MainLayout mainLayout;

    public CartPresenter(CartView view) {
        this.view = view;
        this.userService = ServiceLocator.getUserService();
        this.storeService = ServiceLocator.getStoreService();
        this.transactionService = ServiceLocator.getTransactionService();
        this.mainLayout = MainLayout.getInstance();
        addSomeItems();
    }

    /** Load current user cart and refresh the view. */
    public void loadCart() {
        Result<ShoppingCart> result  = userService.getUserCart(mainLayout.getSessionKey(), mainLayout.getUserEmail());
        if (result.isFailure()) {
            view.showError(result.getErrorMessage());
            return;
        }
        ShoppingCart cart = result.getData();
        view.showItems(getAllCartItems(cart));
    }

    private List<CartItemDTO> getAllCartItems(ShoppingCart cart) {
        Map<String, Basket> entries = cart.snapShot();
        List<CartItemDTO> items = new ArrayList<>();
        for (String storeName : entries.keySet()) {
            Basket basket = entries.get(storeName);
            Map<String, Integer> products = basket.snapshot();
            for (String productId : products.keySet()) {
                int qty = products.get(productId);
                Result<ShoppingProductDTO> productResult = storeService.getProduct(mainLayout.getSessionKey(), storeName, productId);
                if (productResult.isFailure()) {
                    view.showError(productResult.getErrorMessage());
                    continue;
                }
                ShoppingProductDTO product = productResult.getData();
                items.add(new CartItemDTO(product.getName(), storeName, productId, qty, product.getPrice()));
            }
        }
        return items;
    }

    /** User changed quantity through the UI. */
    public void onQuantityChange(String productId, int newQty, String storeName) {
        userService.modifyProductQuantityInCartItem(mainLayout.getSessionKey(), mainLayout.getUserEmail(), productId, storeName, newQty);
        loadCart();
    }

    /** Handles the checkout process with credit card details. */
    public boolean onCheckout(CheckoutDialog.CreditCardDetails creditCardDetails) {
        // Format credit card details for payment processing
        String paymentDetails = String.format("%s|%s|%s|%s",
            creditCardDetails.getCardNumber().replaceAll("\\s+", ""),
            creditCardDetails.getCardHolder(),
            creditCardDetails.getExpiryDate(),
            creditCardDetails.getCvv()
        );

        // Process the purchase
        Result<Void> result = transactionService.purchaseShoppingCart(
            mainLayout.getSessionKey(),
            mainLayout.getUserEmail(),
            paymentDetails
        );

        if (result.isSuccess()) {
            view.showSuccess("Order placed successfully! Thank you for your purchase.");
            // Navigate to catalog view
            //reload the cart
            UI.getCurrent().navigate("catalog");
            return true;
        } else {
            view.showError(result.getErrorMessage());
            return false;
        }
    }

    private void recalcSubtotal(List<CartItemDTO> items) {
        double subtotal = items.stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();
        view.updateSubtotal(subtotal);
        view.setCheckoutEnabled(subtotal > 0);
    }

    public record CartItemDTO(String productName, String storeName, String productId, int quantity, double price) {
        public double getTotalPrice() {
            return quantity * price;
        }
    }

    private void addSomeItems() {
        StoreService storeService = ServiceLocator.getStoreService();
        String storeName = "Store1";
        String storeOwner = "Owner1";
        String storeEmail = "Owner@email.com";
        userService.register(storeOwner, storeEmail, "password");
        Result<String> result = userService.login(storeEmail, "password");
        String ownerSessionKey = result.getData();
        System.out.println("Owner session key: 2131" + result.getErrorMessage());
        storeService.createStore(ownerSessionKey, storeName);
        //add item to catalog
        String catalogId = "Catalog1";
        String productName = "Product1";
        String brand = "Brand1";
        String description = "Description1";
        List<String> categories = new ArrayList<>();
        categories.add("Category1");
        ServiceLocator.productCatalog.addCatalogProduct(catalogId, productName, brand, description, categories);
        String productID = storeService.addProductToStore(ownerSessionKey, storeName, catalogId, productName, description, 10.0, 100).getData();
        //add item to cart
        if (mainLayout.getUserEmail() == null || mainLayout.getUserEmail().isEmpty())
            userService.addToGuestCart(mainLayout.getSessionKey(), productID, storeName);
        else 
            userService.addToUserCart(mainLayout.getSessionKey(), mainLayout.getUserEmail(), productID, storeName);
        //add another item to cart
        String productName2 = "Product2";
        String brand2 = "aBrand2";
        String description2 = "Description2";
        List<String> categories2 = new ArrayList<>();
        categories2.add("Category2");
        ServiceLocator.productCatalog.addCatalogProduct(catalogId, productName2, brand2, description2, categories2);
        String productID2 = storeService.addProductToStore(ownerSessionKey, storeName, catalogId, productName2, description2, 20.0, 200).getData();
        if (mainLayout.getUserEmail() == null || mainLayout.getUserEmail().isEmpty())
            userService.addToGuestCart(mainLayout.getSessionKey(), productID2, storeName);
        else
            userService.addToUserCart(mainLayout.getSessionKey(), mainLayout.getUserEmail(), productID2, storeName);
    }
}
