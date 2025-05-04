package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Route(value = "catalog", layout = MainLayout.class)
@PageTitle("Product catalog")
public class CatalogView extends VerticalLayout {

    /* ───────── constructor ───────── */
    public CatalogView() {
        setWidthFull();
        setPadding(true);
        setSpacing(false);

        /* title */
        H3 title = new H3("Product catalog");
        title.getStyle()
                .set("margin", "0 0 1em 0")
                .set("text-align", "left");

        /* responsive grid */
        Div grid = new Div();
        grid.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "0.9em");

        /* build demo cards */
        fakeProducts().forEach(p -> {
            RouterLink link = new RouterLink(
                    "",
                    ProductView.class,
                    new RouteParameters(Map.of(
                            "id",  p.id(),
                            "img", encode(p.imageUrl())))
            );
            link.getElement().getStyle()
                    .set("text-decoration", "none")
                    .set("color", "inherit");

            link.add(createCard(p));
            grid.add(link);
        });

        add(title, grid);
    }

    /* ───────── helpers ───────── */

    private Div createCard(Product p) {
        Div card = new Div();
        card.getStyle()
                .set("width", "200px")
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "0.75em")
                .set("background", "#fff")
                .set("box-shadow", "0 3px 8px rgba(0,0,0,0.06)")
                .set("transition", "transform .12s")
                .set("cursor", "pointer");

        /* smooth hover zoom */
        card.addAttachListener(e ->
                card.getElement().executeJs(
                        "this.onmouseenter = _ => this.style.transform='scale(1.03)';" +
                                "this.onmouseleave = _ => this.style.transform='';"));

        Image img = new Image(p.imageUrl(), "product");
        img.setWidth("100%");
        img.getStyle().set("border-radius", "4px");

        Span name  = new Span(p.name());
        Span price = new Span("$" + p.price());
        price.getStyle().set("font-weight", "600");

        Button cart = new Button(VaadinIcon.CART.create(), click ->
                Notification.show(p.name() + " added", 2000, Notification.Position.BOTTOM_CENTER));
        cart.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_SMALL);

        HorizontalLayout bottom = new HorizontalLayout(price, cart);
        bottom.setWidthFull();
        bottom.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottom.setAlignItems(FlexComponent.Alignment.CENTER);

        card.add(img, name, bottom);
        return card;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private List<Product> fakeProducts() {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new Product(
                        "P" + i,
                        "Art Image " + i,
                        i * 10,
                        "https://picsum.photos/seed/" + i + "/380/280"))
                .toList();
    }

    /* simple DTO */
    public record Product(String id, String name, double price, String imageUrl) {}
}
