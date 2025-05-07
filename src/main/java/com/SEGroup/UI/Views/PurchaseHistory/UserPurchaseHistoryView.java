package com.SEGroup.UI.Views.PurchaseHistory;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.UserPurchaseHistoryPresenter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "userPurchaseHistory", layout = MainLayout.class)
public class UserPurchaseHistoryView extends VerticalLayout {
    private final Grid<TransactionDTO> grid = new Grid<>(TransactionDTO.class, false);
    private final UserPurchaseHistoryPresenter presenter;

    @Autowired
    public UserPurchaseHistoryView(UserPurchaseHistoryPresenter presenter) {
        this.presenter = presenter;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        String userEmail = (String) VaadinSession.getCurrent().getAttribute("userEmail");
        String sessionKey = (String) VaadinSession.getCurrent().getAttribute("sessionKey");

        if (userEmail == null || sessionKey == null) {
            Notification.show("Access denied: User not logged in.", 5000, Notification.Position.MIDDLE);
            return;
        }

        List<TransactionDTO> transactions = presenter.getUserPurchaseHistory(sessionKey, userEmail);
        presenter.getUserPurchaseHistory()

        grid.addColumn(TransactionDTO::getSellerStore)
                .setHeader("Store")
                .setAutoWidth(true);

        grid.addColumn(tx -> String.join(", ", tx.getItemsToTransact()))
                .setHeader("Items")
                .setAutoWidth(true);

        grid.addColumn(TransactionDTO::getCost)
                .setHeader("Total Cost")
                .setAutoWidth(true);

        grid.setItems(transactions);
        add(new H3("My Purchase History"), grid);

        grid.addClassNames(LumoUtility.Background.CONTRAST_5, "rounded-corners");
        grid.setHeight("100%");
        grid.getStyle().set("border-radius", "12px");
    }
}
