package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;

@Route(value = "stores/:storeName", layout = MainLayout.class)
@PageTitle("Store Details")
public class StoreView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String name = event.getRouteParameters()
                .get("storeName")
                .orElse("");
        Store store = fakeStores().stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElse(null);

        if (store == null) {
            add(new Span("Store not found"));
            return;
        }

        // header
        H2 title = new H2(store.name());
        title.getStyle().set("text-decoration", "underline")
                .set("margin-bottom", "0.5em");
        add(title);

        // owner + rating line
        HorizontalLayout meta = new HorizontalLayout(
                new Span("Store owner: " + store.owner()),
                new Span("Rating: " + store.rating())
        );
        meta.getStyle().set("gap", "1em");
        add(meta);

        // description
        add(new Paragraph(store.description()));
    }

    private List<Store> fakeStores() {
        return List.of(
                new Store("Adults +18 store", "Goseph", 5, "This is a store for adults only! …"),
                new Store("Toy store",          "Laura", 2, "This is a store for kids, …"),
                new Store("iHerb store",       "Kaplan", 1, "Selling herbs …")
        );
    }

    record Store(String name, String owner, int rating, String description) {}

}