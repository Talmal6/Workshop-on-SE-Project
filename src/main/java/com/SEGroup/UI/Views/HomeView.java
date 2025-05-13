package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "homeView", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setWidthFull();

        // Welcome section
        H2 welcomeMessage = new H2("Welcome to the eCommerce System");
        Paragraph description = new Paragraph("Explore a wide variety of stores and products, all in one secure and private platform designed for your shopping comfort.");
        description.getStyle().set("text-align", "center");
        description.getStyle().set("margin-top", "0px");
        description.getStyle().set("margin-bottom", "20px");

        add(welcomeMessage, description);

        // Product Section Title
        H3 productsTitle = new H3("Recommended Products");
        add(productsTitle);

        // Responsive product layout
        Div productsDiv = new Div();
        productsDiv.getStyle().set("display", "flex");
        productsDiv.getStyle().set("flex-wrap", "wrap");
        productsDiv.getStyle().set("justify-content", "center");
        productsDiv.getStyle().set("gap", "20px");
        productsDiv.setWidthFull();

        productsDiv.add(
                createProductCard("Smart Watch", "$99.00", "https://www.leafstudios.in/cdn/shop/files/1_1099cd20-7237-4bdf-a180-b7126de5ef3d_grande.png?v=1722230645", "GadgetZone", 5),
                createProductCard("Wireless Earbuds", "$59.00", "https://admin.gomobile.co.il/wp-content/uploads/2024/08/a4b223cb9bca28717ffe246536221c9e.webp", "SoundWave", 5),
                createProductCard("Portable Speaker", "$79.00", "https://www.sencor.com/getmedia/6770caad-d0be-4d0d-b5f0-01bbc4c1c555/35059169.jpg.aspx?width=2100&height=2100&ext=.jpg", "BassBoom", 5)
        );

        add(productsDiv);

        Div extraSpacer = new Div();
        extraSpacer.setHeight("20px");
        add(extraSpacer);

        // Store Section Title
        H3 storesTitle = new H3("Recommended Stores");
        add(storesTitle);

        // Responsive store layout
        Div storesDiv = new Div();
        storesDiv.getStyle().set("display", "flex");
        storesDiv.getStyle().set("flex-wrap", "wrap");
        storesDiv.getStyle().set("justify-content", "center");
        storesDiv.getStyle().set("gap", "20px");
        storesDiv.setWidthFull();


        storesDiv.add(
                createStoreCard("Tech Store", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT9LPPqAt-hXGmPF8OEFLKJfIRbNHmkwDShNA&s"),
                createStoreCard("Fashion Hub", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkSF8h_PT2oIXCQbiwv_WrCPlTooRVHY25sw&s"),
                createStoreCard("Home Goods", "https://brandlogos.net/wp-content/uploads/2023/08/homegoods-logo_brandlogos.net_85xww.png")
        );

        add(storesDiv);
    }

    private VerticalLayout createProductCard(String name, String price, String imageUrl, String storeName, int rating) {
        Image image = new Image(imageUrl, name);
        image.setWidth("120px");

        Span ratingStars = new Span();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) stars.append("★");
        for (int i = rating; i < 5; i++) stars.append("☆");
        ratingStars.setText(stars.toString());
        ratingStars.getStyle().set("font-size", "14px").set("color", "gold");

        H4 productName = new H4(name);
        Paragraph productPrice = new Paragraph(price);
        Paragraph productStore = new Paragraph("Seller: " + storeName);
        productStore.getStyle().set("font-size", "12px").set("color", "gray");

        Button buyButton = new Button("Buy Now");

        VerticalLayout card = new VerticalLayout(image, ratingStars, productName, productPrice, productStore, buyButton);
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("190px");
        card.setHeight("300px");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "10px");

        return card;
    }

    private VerticalLayout createStoreCard(String name, String logoUrl) {
        Image logo = new Image(logoUrl, name);
        logo.setWidth("170px");

        H4 storeName = new H4(name);
        Button visitButton = new Button("Visit Store");
        visitButton.getStyle().set("margin-top", "20px");

        VerticalLayout card = new VerticalLayout(logo, storeName, visitButton);
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("190px");
        card.setHeight("300px");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "10px");

        return card;
    }
}
