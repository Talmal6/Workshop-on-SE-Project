package com.SEGroup.UI.Views.PurchaseHistory;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.UserPurchaseHistoryPresenter;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Route(value = "userPurchaseHistory", layout = MainLayout.class)
public class UserPurchaseHistoryView extends VerticalLayout {

    private final Grid<TransactionDTO> grid = new Grid<>(TransactionDTO.class, false);

    public UserPurchaseHistoryView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H3 title = new H3("My Purchase History");
        add(title, grid);

        grid.addClassNames(LumoUtility.Background.CONTRAST_5, "rounded-corners");
        grid.setHeight("100%");
        grid.getStyle().set("border-radius", "12px");

        grid.addColumn(TransactionDTO::getSellerStore)
                .setHeader("Store")
                .setAutoWidth(true);
        grid.addColumn(tx -> String.join(", ", tx.getItemsToTransact()))
                .setHeader("Items")
                .setAutoWidth(true);
        grid.addColumn(TransactionDTO::getCost)
                .setHeader("Total Cost")
                .setAutoWidth(true);

        TransactionService transactionService = ServiceLocator.getTransactionService();
        UserPurchaseHistoryPresenter presenter = new UserPurchaseHistoryPresenter(this, null); // כאן לא נשלח דוא"ל של משתמש

        // קבלת העסקאות מה-Presenter
        List<TransactionDTO> transactions = presenter.loadTransactionHistory(null);

        // Ensure transactions is never null
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        if (transactions.isEmpty()) {
            Notification.show("No purchases found.", 5000, Notification.Position.MIDDLE);
        }

        grid.setItems(transactions);
    }

    public void displayPurchaseHistory(List<TransactionDTO> transactions) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        grid.setItems(transactions);
    }

    public void displayErrorMessage(String errorMessage) {
        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
    }
}

