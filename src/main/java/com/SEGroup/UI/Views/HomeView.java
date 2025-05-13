package com.SEGroup.UI.Views;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreCardDto;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {

    private final StoreService storeService;
    private final DecimalFormat ratingFormat = new DecimalFormat("0.0");

    public HomeView() {
        this.storeService = ServiceLocator.getStoreService();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeroBanner());
        add(createFeaturedProductsSection());
        add(createPopularStoresSection());
        add(createCategoriesSection());
    }

    private Component createHeroBanner() {
        Div banner = new Div();
        banner.addClassName("hero-banner");
        banner.getStyle()
                .set("background-image", "linear-gradient(to right, #6a11cb 0%, #2575fc 100%)")
                .set("color", "white")
                .set("border-radius", "8px")
                .set("padding", "40px")
                .set("margin-bottom", "30px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");

        H1 title = new H1("Welcome to Our Marketplace");
        title.getStyle().set("margin-top", "0");

        Paragraph subtitle = new Paragraph("Discover amazing products from our verified stores!");
        subtitle.getStyle().set("font-size", "1.2em");

        Button shopNowBtn = new Button("Shop Now", new Icon(VaadinIcon.ARROW_RIGHT));
        shopNowBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        shopNowBtn.getStyle()
                .set("background-color", "white")
                .set("color", "#2575fc")
                .set("font-weight", "bold")
                .set("margin-top", "20px");
        shopNowBtn.addClickListener(e -> UI.getCurrent().navigate("catalog"));

        VerticalLayout content = new VerticalLayout(title, subtitle, shopNowBtn);
        content.setPadding(false);
        content.setSpacing(true);
        banner.add(content);

        return banner;
    }

    private Component createFeaturedProductsSection() {
        H2 sectionTitle = new H2("Featured Products");

        HorizontalLayout productsLayout = new HorizontalLayout();
        productsLayout.setWidthFull();
        productsLayout.setPadding(true);
        productsLayout.setSpacing(true);
        productsLayout.getStyle().set("overflow-x", "auto");

        // Get featured products from service
        try {
            Result<List<ShoppingProductDTO>> result = storeService.getAllProducts();
            if (result.isSuccess()) {
                List<ShoppingProductDTO> allProducts = result.getData();
                // Get top 4 products by rating
                List<ShoppingProductDTO> featuredProducts = allProducts.stream()
                        .sorted((p1, p2) -> Double.compare(p2.getAvgRating(), p1.getAvgRating()))
                        .limit(4)
                        .collect(Collectors.toList());

                for (ShoppingProductDTO product : featuredProducts) {
                    productsLayout.add(createProductCard(product));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading featured products: " + e.getMessage());
            // Add placeholder products
            for (int i = 0; i < 4; i++) {
                Div placeholder = new Div();
                placeholder.setText("Product unavailable");
                placeholder.getStyle()
                        .set("width", "250px")
                        .set("height", "300px")
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("border-radius", "8px");
                productsLayout.add(placeholder);
            }
        }

        Button viewAllBtn = new Button("View All Products", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addClickListener(e -> UI.getCurrent().navigate("catalog"));

        VerticalLayout section = new VerticalLayout(sectionTitle, productsLayout, viewAllBtn);
        section.setPadding(false);
        section.setSpacing(true);
        section.setHorizontalComponentAlignment(FlexComponent.Alignment.END, viewAllBtn);

        return section;
    }

    private Component createProductCard(ShoppingProductDTO product) {
        Div card = new Div();
        card.getStyle()
                .set("width", "250px")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease")
                .set("cursor", "pointer")
                .set("background-color", "white");

        card.addClickListener(e -> navigateToProduct(product));

        // Hover effect
        card.getElement().addEventListener("mouseover", event -> {
            card.getStyle()
                    .set("transform", "translateY(-5px)")
                    .set("box-shadow", "0 5px 15px rgba(0,0,0,0.1)");
        });

        card.getElement().addEventListener("mouseout", event -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        });

        // Image
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("height", "150px")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Image image = new Image(product.getImageUrl(), product.getName());
            image.setHeight("150px");
            image.getStyle().set("object-fit", "contain");
            imageContainer.add(image);
        } else {
            Span placeholder = new Span("No Image");
            imageContainer.add(placeholder);
        }

        // Product details
        Div details = new Div();
        details.getStyle().set("padding", "16px");

        H4 name = new H4(product.getName());
        name.getStyle()
                .set("margin", "0 0 8px 0")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        Paragraph store = new Paragraph("Store: " + product.getStoreName());
        store.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9em");

        HorizontalLayout priceRating = new HorizontalLayout();
        priceRating.setWidthFull();
        priceRating.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span price = new Span("$" + product.getPrice());
        price.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        Span rating = new Span(ratingFormat.format(product.getAvgRating()) + " ★");
        rating.getStyle()
                .set("color", "#FFA500")
                .set("font-weight", "bold");

        priceRating.add(price, rating);

        details.add(name, store, priceRating);

        card.add(imageContainer, details);

        return card;
    }

    private void navigateToProduct(ShoppingProductDTO product) {
        UI.getCurrent().navigate("product/" + product.getProductId() + "/" + product.getStoreName());
    }

    private Component createPopularStoresSection() {
        H2 sectionTitle = new H2("Popular Stores");

        HorizontalLayout storesLayout = new HorizontalLayout();
        storesLayout.setWidthFull();
        storesLayout.setPadding(true);
        storesLayout.setSpacing(true);
        storesLayout.getStyle().set("overflow-x", "auto");

        // Get popular stores from service
        try {
            List<StoreCardDto> allStores = storeService.listAllStores();
            // Sort by rating and limit to 3
            List<StoreCardDto> popularStores = allStores.stream()
                    .sorted((s1, s2) -> Double.compare(s2.rating(), s1.rating()))
                    .limit(3)
                    .collect(Collectors.toList());

            for (StoreCardDto store : popularStores) {
                storesLayout.add(createStoreCard(store));
            }
        } catch (Exception e) {
            System.err.println("Error loading popular stores: " + e.getMessage());
            // Add placeholder stores
            for (int i = 0; i < 3; i++) {
                Div placeholder = new Div();
                placeholder.setText("Store unavailable");
                placeholder.getStyle()
                        .set("width", "300px")
                        .set("height", "200px")
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("border-radius", "8px");
                storesLayout.add(placeholder);
            }
        }

        Button viewAllBtn = new Button("View All Stores", new Icon(VaadinIcon.ARROW_RIGHT));
        viewAllBtn.addClickListener(e -> UI.getCurrent().navigate("stores"));

        VerticalLayout section = new VerticalLayout(sectionTitle, storesLayout, viewAllBtn);
        section.setPadding(false);
        section.setSpacing(true);
        section.setHorizontalComponentAlignment(FlexComponent.Alignment.END, viewAllBtn);

        return section;
    }

    private Component createStoreCard(StoreCardDto  store) {
        Div card = new Div();
        card.getStyle()
                .set("width", "300px")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease")
                .set("cursor", "pointer")
                .set("background-color", "white");

        card.addClickListener(e -> navigateToStore(store));

        // Hover effect
        card.getElement().addEventListener("mouseover", event -> {
            card.getStyle()
                    .set("transform", "translateY(-5px)")
                    .set("box-shadow", "0 5px 15px rgba(0,0,0,0.1)");
        });

        card.getElement().addEventListener("mouseout", event -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        });

        // Store icon
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("padding", "20px")
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Icon storeIcon = VaadinIcon.STORAGE.create();
        storeIcon.setSize("40px");
        iconContainer.add(storeIcon);

        // Store details
        Div details = new Div();
        details.getStyle().set("padding", "16px");

        H3 name = new H3(store.name());
        name.getStyle().set("margin", "0 0 8px 0");

        Paragraph owner = new Paragraph("Owner: " + store.owner());
        owner.getStyle()
                .set("margin", "0 0 8px 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9em");

        Paragraph description = new Paragraph(store.description());
        description.getStyle()
                .set("margin", "0 0 8px 0")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical")
                .set("height", "40px");

        Span rating = new Span(ratingFormat.format(store.rating()) + " ★");
        rating.getStyle()
                .set("color", "#FFA500")
                .set("font-weight", "bold");

        details.add(name, owner, description, rating);

        card.add(iconContainer, details);

        return card;
    }

    private void navigateToStore(StoreCardDto store) {
        UI.getCurrent().navigate("store/" + store.name());
    }

    private Component createCategoriesSection() {
        H2 sectionTitle = new H2("Browse Categories");

        // Common categories - you can adapt these based on your actual categories
        List<String> categories = List.of(
                "Electronics", "Clothing", "Home", "Accessories",
                "Kitchen", "Phones", "Computers", "Audio"
        );

        HorizontalLayout categoryLayout = new HorizontalLayout();
        categoryLayout.setWidthFull();
        categoryLayout.setPadding(true);
        categoryLayout.setSpacing(true);
        categoryLayout.getStyle().set("flex-wrap", "wrap");

        for (String category : categories) {
            categoryLayout.add(createCategoryButton(category));
        }

        VerticalLayout section = new VerticalLayout(sectionTitle, categoryLayout);
        section.setPadding(false);
        section.setSpacing(true);

        return section;
    }

    private Component createCategoryButton(String category) {
        Button button = new Button(category);
        button.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "20px")
                .set("padding", "8px 16px")
                .set("margin", "5px");

        button.addClickListener(e -> {
            UI.getCurrent().navigate("catalog?category=" + category);
        });

        return button;
    }
}