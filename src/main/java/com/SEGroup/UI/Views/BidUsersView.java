//
//package com.SEGroup.UI.Views;
//
//import com.SEGroup.UI.MainLayout;
//import com.SEGroup.UI.Presenter.BidUsersPresenter;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.html.H2;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.Notification.Position;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.BeforeEnterObserver;
//import com.vaadin.flow.router.HasUrlParameter;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//
//import java.util.List;
//
//@Route(value = "product/:productId/:storeName/bids", layout = MainLayout.class)
//@PageTitle("Bid Requests")
//public class BidUsersView extends VerticalLayout implements HasUrlParameter<String> {
//
//    private final Grid<String> usersGrid = new Grid<>(String.class, false);
//
//
//    public BidUsersView() {
//
//        setSizeFull();
//        setPadding(true);
//        setSpacing(true);
//
//        add(new H2("Bid Requests for Product " + productId));
//
//        usersGrid.addColumn(email -> email)
//                .setHeader("User Email")
//                .setAutoWidth(true)
//                .setSortable(true);
//        add(usersGrid);
//
//        // Wire up presenter
//        new BidUsersPresenter(this, storeName, productId)
//                .loadBidUsers();
//    }
//
//    public void beforeEnter(BeforeEnterEvent event){
//            String productId = event.getRouteParameters()
//                    .get("productId").orElse("");
//            String storeName = event.getRouteParameters()
//                    .get("storeName").orElse("");
//
//            if (productId.isEmpty() || storeName.isEmpty()) {
//                Notification.show("Invalid URL parameters", 3000, Notification.Position.MIDDLE);
//                return;
//            }
//        }
//    }
//
//    /** Display the list of user emails who requested a bid */
//    public void displayBidUsers(List<String> users) {
//        usersGrid.setItems(users);
//    }
//
//    /** Show an error notification */
//    public void showError(String message) {
//        Notification.show(message, 3000, Position.MIDDLE);
//    }
//
//}