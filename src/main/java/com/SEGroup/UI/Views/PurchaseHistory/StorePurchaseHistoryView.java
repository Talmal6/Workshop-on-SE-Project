package com.SEGroup.UI.Views.PurchaseHistory;

import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "storePurchaseHistory", layout = MainLayout.class)
public class StorePurchaseHistoryView extends VerticalLayout implements HasUrlParameter<String> {

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H3 title = new H3();

    public StorePurchaseHistoryView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setClassName("store-purchase-history");
        getStyle().set("background-color", "#f9f9f9");

        title.getStyle()
                .set("color", "#065f46")
                .set("font-size", "2em")
                .set("margin-bottom", "10px");

        grid.addColumn(Transaction::getBuyersEmail).setHeader("Buyer Email").setAutoWidth(true);
        grid.addColumn(tx -> String.join(", ", tx.getItemsToTransact())).setHeader("Items").setAutoWidth(true);
        grid.addColumn(Transaction::getCost).setHeader("Total Cost").setAutoWidth(true);

        grid.addClassNames(LumoUtility.Background.CONTRAST_5, "rounded-corners");
        grid.setHeight("100%");
        grid.getStyle().set("border-radius", "12px");

        add(title, grid);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String storeName) {
        // בדיקת הרשאות (אם צריך)
        String managerEmail = (String) VaadinSession.getCurrent().getAttribute("userEmail");
        if (managerEmail == null) {
            Notification.show("Access denied: User not logged in.", 5000, Notification.Position.MIDDLE);
            return;
        }

        if (storeName == null || storeName.isEmpty()) {
            Notification.show("No store specified in the URL.", 5000, Notification.Position.MIDDLE);
            return;
        }

        title.setText("Purchase History - " + storeName);
        grid.setItems(getTransactionsForStore(storeName));
    }

    private List<Transaction> getTransactionsForStore(String storeName) {
        return getDummyTransactions().stream()
                .filter(tx -> storeName.equalsIgnoreCase(tx.getStoreName()))
                .collect(Collectors.toList());
    }

    private List<Transaction> getDummyTransactions() {
        return List.of(
                new Transaction(List.of("Wireless Mouse", "Keyboard"), 129.99, "user1@example.com", "Tech Store"),
                new Transaction(List.of("Yoga Mat", "Protein Shake"), 58.49, "user2@example.com", "Health Shop"),
                new Transaction(List.of("USB Cable", "Power Bank"), 32.00, "user3@example.com", "Tech Store")
        );
    }
}
