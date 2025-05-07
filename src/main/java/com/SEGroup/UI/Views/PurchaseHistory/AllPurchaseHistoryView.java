package com.SEGroup.UI.Views.PurchaseHistory;

import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

@Route(value = "allPurchaseHistory", layout = MainLayout.class)
public class AllPurchaseHistoryView extends VerticalLayout {

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false); // false: don't auto-generate columns

    public AllPurchaseHistoryView() {
        // General styling
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setClassName("all-purchase-history-view");
        getStyle().set("background-color", "#f9f9f9");

        // Title
        H3 title = new H3("Purchase History");
        title.getStyle()
                .set("color", "#1e40af")
                .set("font-size", "2em")
                .set("margin-bottom", "10px");

        // Grid configuration
        grid.addColumn(Transaction::getStoreName)
                .setHeader("Store")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(transaction -> String.join(", ", transaction.getItemsToTransact()))
                .setHeader("Items")
                .setAutoWidth(true)
                .setSortable(false);

        grid.addColumn(Transaction::getCost)
                .setHeader("Total Cost")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(Transaction::getBuyersEmail)
                .setHeader("Buyer Email")
                .setAutoWidth(true)
                .setSortable(false);

        grid.setItems(getDummyTransactions());
        grid.addClassNames(LumoUtility.Background.CONTRAST_5, "rounded-corners");
        grid.setHeight("100%");
        grid.getStyle().set("border-radius", "12px");

        add(title, grid);
    }

    private List<Transaction> getDummyTransactions() {
        return List.of(
                new Transaction(List.of("Wireless Mouse", "Keyboard"), 129.99, "user1@example.com", "Tech Store"),
                new Transaction(List.of("Yoga Mat", "Protein Shake"), 58.49, "user2@example.com", "Health Shop"),
                new Transaction(List.of("Notebook", "Pens"), 18.00, "user3@example.com", "Office Supplies")
        );
    }
}
