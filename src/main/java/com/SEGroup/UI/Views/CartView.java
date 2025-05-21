package com.SEGroup.UI.Views;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.CartPresenter;
import com.SEGroup.UI.SecurityContextHolder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Shopping Cart")
public class CartView extends VerticalLayout {

    private final CartPresenter presenter;
    private final Grid<CartItem> grid = new Grid<>();
    private final Button checkoutButton = new Button("Checkout", VaadinIcon.CART_O.create());
    private final Span totalPriceLabel = new Span("Total: $0.00");
    private final Span totalAfterDiscountLabel = new Span("Total after discount: $0.00");
    private final TextField couponField = new TextField("Coupon Code");
    private final Button applyCouponButton = new Button("Apply Coupon");
    private final VerticalLayout emptyCartMessage = new VerticalLayout();
    private double totalPrice = 0.0;

    public CartView() {
        this.presenter = new CartPresenter(this);
        try {
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            add(new H3("Shopping Cart"));

            // Configure grid
            configureGrid();
            add(grid);

            // Empty cart message
            configureEmptyCartMessage();
            add(emptyCartMessage);

            // Total and checkout button
            totalPriceLabel.getStyle().set("font-weight", "bold").set("font-size", "1.2em");
            totalAfterDiscountLabel.getStyle().set("font-weight", "bold").set("font-size", "1.2em").set("color", "var(--lumo-success-color)");

            // Configure coupon field and button
            couponField.setPlaceholder("Enter coupon code");
            couponField.setWidth("200px");
            applyCouponButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            applyCouponButton.addClickListener(e -> {
                // TODO: Implement coupon application logic
                if (couponField.getValue() == null || couponField.getValue().isEmpty()) {
                    showError("Please enter a coupon code.");
                    couponField.setInvalid(true);
                    return;
                }
                Result<Void> result = presenter.applyCoupon(couponField.getValue());
                if (result.isFailure()) {
                    showError("Failed to apply coupon: " + result.getErrorMessage());
                    couponField.setInvalid(true);
                    return;
                }
                showSuccess("Coupon applied successfully!");
                System.out.println("Applying coupon: " + couponField.getValue());
            });

            HorizontalLayout couponLayout = new HorizontalLayout(couponField, applyCouponButton);
            couponLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            couponLayout.setSpacing(true);

            checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            checkoutButton.addClickListener(e -> presenter.checkout());

            Button clearCartButton = new Button("Clear Cart", VaadinIcon.TRASH.create());
            clearCartButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            clearCartButton.addClickListener(e -> presenter.clearCart());

            VerticalLayout totalsLayout = new VerticalLayout(
                    totalPriceLabel,
                    totalAfterDiscountLabel,
                    couponLayout
            );
            totalsLayout.setSpacing(true);

            HorizontalLayout footer = new HorizontalLayout(
                    totalsLayout,
                    createSpacer(),
                    clearCartButton,
                    checkoutButton
            );
            footer.setWidthFull();
            footer.setAlignItems(FlexComponent.Alignment.CENTER);
            add(footer);

            // Load cart data
            presenter.loadCart();
        } catch (Exception e) {
            add(new Span("Error initializing cart: " + e.getMessage()));
        }
    }

    private void configureGrid() {
        grid.addColumn(CartItem::getStoreName).setHeader("Store").setFlexGrow(1);
        grid.addColumn(CartItem::getProductName).setHeader("Product").setFlexGrow(2);
        grid.addColumn(CartItem::getQuantity).setHeader("Quantity").setFlexGrow(0).setWidth("100px");
        grid.addColumn(CartItem::getFormattedPrice).setHeader("Price").setFlexGrow(0).setWidth("100px");
        grid.addColumn(CartItem::getFormattedSubtotal).setHeader("Subtotal").setFlexGrow(0).setWidth("100px");

        // Action column for quantity adjustments and removal
        grid.addComponentColumn(item -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button minus = new Button("-", e -> presenter.changeQuantity(
                    item.getProductId(), item.getStoreName(), Math.max(0, item.getQuantity() - 1)
            ));
            minus.getStyle().set("min-width", "40px");

            Button plus = new Button("+", e -> presenter.changeQuantity(
                    item.getProductId(), item.getStoreName(), item.getQuantity() + 1
            ));
            plus.getStyle().set("min-width", "40px");

            Button remove = new Button(VaadinIcon.TRASH.create(), e ->
                    presenter.removeItem(item.getProductId(), item.getStoreName())
            );
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            remove.getStyle().set("min-width", "40px");

            actions.add(minus, plus, remove);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("180px");

        grid.setWidthFull();
    }

    private void configureEmptyCartMessage() {
        emptyCartMessage.setVisible(false);
        emptyCartMessage.setPadding(true);
        emptyCartMessage.setAlignItems(FlexComponent.Alignment.CENTER);

        Span emptyText = new Span("Your cart is empty");
        emptyText.getStyle()
                .set("font-size", "1.2em")
                .set("color", "var(--lumo-secondary-text-color)");

        Button browseButton = new Button("Browse Products", e -> UI.getCurrent().navigate("catalog"));
        browseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        emptyCartMessage.add(emptyText, browseButton);
    }

    /**
     * Displays the cart contents in the grid.
     */
    public void displayCart(List<BasketDTO> basketDTOs, List<ShoppingCartProduct> products) {
        List<CartItem> cartItems = new ArrayList<>();
        totalPrice = 0.0;

        // Convert BasketDTO objects to CartItem objects using product info
        for (ShoppingCartProduct product : products) {
            // Find the basket containing this product
            for (BasketDTO basket : basketDTOs) {
                if (basket.storeId().equals(product.getStoreName()) &&
                        basket.prod2qty().containsKey(product.getId())) {

                    int quantity = basket.prod2qty().get(product.getId());

                    if (quantity > 0) {
                        CartItem item = new CartItem(
                                product.getId(),
                                product.getName(),
                                product.getStoreName(),
                                quantity,
                                product.getPrice()
                        );

                        cartItems.add(item);
                        totalPrice += item.getSubtotal();
                    }
                }
            }
        }

        // Handle empty cart
        if (cartItems.isEmpty()) {
            grid.setVisible(false);
            emptyCartMessage.setVisible(true);
            couponField.setVisible(false);
            applyCouponButton.setVisible(false);
        } else {
            grid.setVisible(true);
            emptyCartMessage.setVisible(false);
            grid.setItems(cartItems);
            couponField.setVisible(true);
            applyCouponButton.setVisible(true);
        }

        totalPriceLabel.setText(String.format("Total: $%.2f", totalPrice));
        totalAfterDiscountLabel.setText(String.format("Total after discount: $%.2f", totalPrice)); // TODO: Update with actual discount calculation
        checkoutButton.setEnabled(!cartItems.isEmpty());
    }

    /**
     * Shows a success notification to the user.
     */
    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification to the user.
     */
    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private Component createSpacer() {
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }

    /**
     * Data class for products in the shopping cart.
     */
    public static class ShoppingCartProduct {
        private final String id;
        private final String name;
        private final String storeName;
        private final double price;

        public ShoppingCartProduct(String id, String name, String storeName, double price) {
            this.id = id;
            this.name = name;
            this.storeName = storeName;
            this.price = price;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getStoreName() { return storeName; }
        public double getPrice() { return price; }
    }

    /**
     * Represents an item in the shopping cart.
     */
    public static class CartItem {
        private final String productId;
        private final String productName;
        private final String storeName;
        private final int quantity;
        private final double price;

        public CartItem(String productId, String productName, String storeName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.storeName = storeName;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getStoreName() { return storeName; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public double getSubtotal() { return price * quantity; }
        public String getFormattedPrice() { return String.format("$%.2f", price); }
        public String getFormattedSubtotal() { return String.format("$%.2f", getSubtotal()); }
    }
}