package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.CartPresenter;
import com.SEGroup.UI.Presenter.CartPresenter.CartItemDTO;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.List;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Cart")
public class CartView extends VerticalLayout {
    
    private final CartPresenter presenter;
    private final Grid<CartItemDTO> grid;
    private final Span subtotalLabel;
    private final Button checkoutButton;

    public CartView() {
        this.presenter = new CartPresenter(this);
        setPadding(true);
        setSpacing(true);

        // Title
        add(new H3("Shopping Cart"));

        // Grid for cart items
        grid = new Grid<>(CartItemDTO.class);
        grid.setColumns("productName", "storeName", "productId", "quantity", "price");
        grid.getColumnByKey("productName").setHeader("Product Name");
        grid.getColumnByKey("storeName").setHeader("Store");
        grid.getColumnByKey("productId").setHeader("Product ID");
        grid.getColumnByKey("quantity").setHeader("Quantity");
        grid.getColumnByKey("price").setHeader("Price");

        // Add quantity editor column
        grid.addColumn(new ComponentRenderer<>(item -> {
            IntegerField quantityField = new IntegerField();
            quantityField.setValue(item.quantity());
            quantityField.setMin(1);
            quantityField.setMax(100);
            quantityField.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    presenter.onQuantityChange(item.productId(), e.getValue(), item.storeName());
                }
            });
            return quantityField;
        })).setHeader("Edit Quantity");

        // Add remove button column
        grid.addColumn(new ComponentRenderer<>(item -> {
            Button removeButton = new Button("Remove");
            removeButton.addClickListener(e -> presenter.onQuantityChange(item.productId(), 0, item.storeName()));
            return removeButton;
        })).setHeader("Actions");

        add(grid);

        // Subtotal and checkout section
        HorizontalLayout bottomSection = new HorizontalLayout();
        subtotalLabel = new Span("Subtotal: $0.00");
        checkoutButton = new Button("Checkout");
        checkoutButton.addClickListener(e -> showCheckoutDialog());
        bottomSection.add(subtotalLabel, createSpacer(), checkoutButton);
        add(bottomSection);
        // Load initial cart data
        presenter.loadCart();
    }

    private Component createSpacer() {
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }

    public void showItems(List<CartItemDTO> items) {
        grid.setItems(items);
        recalcSubtotal(items);
    }

    public void showError(String errorMessage) {
        Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
    }

    public void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }

    public void updateSubtotal(double subtotal) {
        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
    }

    public void setCheckoutEnabled(boolean enabled) {
        checkoutButton.setEnabled(enabled);
    }

    private void recalcSubtotal(List<CartItemDTO> items) {
        double subtotal = items.stream()
                .mapToDouble(CartItemDTO::getTotalPrice)
                .sum();
        updateSubtotal(subtotal);
        setCheckoutEnabled(subtotal > 0);
    }

    private void showCheckoutDialog() {
        CheckoutDialog dialog = new CheckoutDialog(presenter);
        dialog.open();
    }
}
