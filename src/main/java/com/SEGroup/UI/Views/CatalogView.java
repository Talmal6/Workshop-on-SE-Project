package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.IntStream;

@Route(value = "catalog", layout = MainLayout.class)
@PageTitle("Catalog")
public class CatalogView extends VerticalLayout {

    public CatalogView() {
        addClassName("catalog-view");
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        // Centered Title with underline
        H3 title = new H3("Product Catalog");
        title.getStyle()
                .set("text-align", "left")
                .set("text-decoration", "underline")
                .set("font-size", "1.6em")
                .set("width", "100%")
                .set("margin-bottom", "1em");
        add(title);

        // Wrap catalog in a left-aligned container
        Div catalogContainer = new Div();
        catalogContainer.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "1em")
                .set("align-items", "flex-start")
                .set("justify-content", "flex-start")
                .set("max-width", "800px")
                .set("margin", "0 auto")
                .set("align","left"); // center horizontally


        fakeProducts().forEach(product -> catalogContainer.add(createProductCard(product)));

        add(catalogContainer);
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.getStyle()
                .set("width", "180px")
                .set("border", "1px solid lightgray")
                .set("border-radius", "8px")
                .set("padding", "1em")
                .set("box-shadow", "2px 2px 6px rgba(0,0,0,0.05)")
                .set("flex", "0 0 calc(25% - 1em)"); // 4 per row, minus gap

        Image placeholder = new Image(product.imageUrl(), "Product image");
        placeholder.setWidth("100%");
        card.add(placeholder);

        Span name = new Span(product.name());
        name.getStyle().set("display", "block").set("margin-top", "0.5em");

        Span price = new Span("$" + product.price());
        price.getStyle().set("font-weight", "bold");

        Button addToCart = new Button(VaadinIcon.CART.create());
        addToCart.addClickListener(e -> Notification.show(product.name() + " added"));

        HorizontalLayout bottom = new HorizontalLayout(price, addToCart);
        bottom.setWidthFull();
        bottom.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottom.setAlignItems(FlexComponent.Alignment.CENTER);

        card.add(name, bottom);
        return card;
    }

    private List<Product> fakeProducts() {
        return IntStream.range(1, 13)
                .mapToObj(i -> new Product("P" + i, "Art Image " + i, i * 10,   "https://picsum.photos/seed/" + i + "/200/200"))
                .toList();
    }

    record Product(String id, String name, double price, String imageUrl) {}
}
