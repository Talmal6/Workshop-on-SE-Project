package com.SEGroup.UI.Views;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.PurchaseHistoryPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Route(value = "purchase-history", layout = MainLayout.class)
@PageTitle("Purchase History")
public class PurchaseHistoryView extends VerticalLayout {

    private final PurchaseHistoryPresenter presenter;
    private final Grid<TransactionDTO> grid;

    public PurchaseHistoryView() {
        this.presenter = new PurchaseHistoryPresenter(this);
        setPadding(true);
        setSpacing(true);

        // Title
        add(new H3("Purchase History"));

        // Grid for transactions
        grid = new Grid<>(TransactionDTO.class);
        grid.setColumns("buyersEmail", "sellerStore", "cost");
        grid.getColumnByKey("buyersEmail").setHeader("Buyer");
        grid.getColumnByKey("sellerStore").setHeader("Store");
        grid.getColumnByKey("cost").setHeader("Total Cost");

        // Add view items button column
        grid.addColumn(new ComponentRenderer<>(transaction -> {
            Button viewItemsButton = new Button("View Items");
            viewItemsButton.addClickListener(e -> showItemsDialog(transaction));
            return viewItemsButton;
        })).setHeader("Items");

        add(grid);

        // Load initial data
        presenter.loadPurchaseHistory();
    }

    private void showItemsDialog(TransactionDTO transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Purchased Items from " + transaction.getSellerStore());

        UnorderedList itemsList = new UnorderedList();
        itemsList.getStyle().set("list-style-type", "none");
        itemsList.getStyle().set("padding", "0");

        // Count occurrences of each product ID to get quantities
        Map<String, Integer> productQuantities = new HashMap<>();
        for (String itemId : transaction.getItemsToTransact()) {
            productQuantities.merge(itemId, 1, Integer::sum);
        }

        // Display each unique product with its quantity
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            ShoppingProductDTO product = presenter.getProductDetails(transaction.getSellerStore(), itemId);
            if (product != null) {
                ListItem item = new ListItem();
                item.setText(String.format("%s - $%.2f x %d = $%.2f",
                        product.getName(),
                        product.getPrice(),
                        quantity,
                        product.getPrice() * quantity));
                itemsList.add(item);
            } else {
                ListItem item = new ListItem();
                item.setText(String.format("Product ID: %s (Details not available) x %d", itemId, quantity));
                itemsList.add(item);
            }
        }

        dialog.add(itemsList);
        dialog.setWidth("400px");
        dialog.open();
    }

    public void hideGrid() {
        grid.setVisible(false);
        Span  noItemsField = new Span ("No purchases are recorded for you yet. please shop to see your purchase history");
        noItemsField.setWidth("400px");
        noItemsField.setHeight("100%");
        noItemsField.getStyle().set("text-align", "center");
        noItemsField.getStyle().set("margin-top", "var(--lumo-space-xl)");
        noItemsField.getStyle().set("color", "var(--lumo-secondary-text-color)");
        noItemsField.getStyle().set("font-size", "var(--lumo-font-size-l)");
        noItemsField.getStyle().set("font-weight", "bold");
        noItemsField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        noItemsField.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        noItemsField.getStyle().set("padding", "var(--lumo-space-m)");
        noItemsField.getStyle().set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)");
        noItemsField.getStyle().set("margin", "auto");
        add(noItemsField);
    }

    public void showItems(List<TransactionDTO> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            hideGrid();
            return;
        }
        grid.setItems(transactions);
    }

    public void showError(String errorMessage) {
        Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
    }
}