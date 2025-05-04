package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Cart")
public class CartView extends VerticalLayout {

    public CartView() {
        add(new H3("Shopping cart:"));

        var grid = new Grid<Item>();
        grid.addColumn(Item::store).setHeader("Store");
        grid.addColumn(Item::name).setHeader("Product");
        grid.addColumn(Item::qty).setHeader("Qty");
        grid.addColumn(Item::price).setHeader("Price");
        grid.setItems(fakeCart());
        add(grid);

        Button checkout = new Button("Checkout", e -> Notification.show("not yet…"));
        add(new HorizontalLayout(createSpacer(), checkout));
    }

    private Component createSpacer() {
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        return spacer;
    }

    record Item(String store, String name, int qty, double price) {}

    private List<Item> fakeCart() {
        return List.of(new Item("Proficuro", "Product X", 1, 10));
    }
}
