package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;


@Route(value = "homeView", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {
    public HomeView() {
        setSpacing(true);
        setPadding(true);

        H2 welcomeMessage = new H2("Welcome to the eCommerce System");
        Paragraph description = new Paragraph("Explore a wide variety of stores and products, all in one secure and private platform designed for your shopping comfort.");
        description.setWidth("100%");
        description.getStyle().set("text-align", "center");
        description.getStyle().set("margin-top", "0px");
        description.getStyle().set("margin-bottom", "20px");

        // Header layout with title
        HorizontalLayout header = new HorizontalLayout(welcomeMessage);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.setAlignItems(Alignment.CENTER);

        add(header, description);
        Div relativeWrapper = new Div();
        relativeWrapper.setWidth("90%");
        relativeWrapper.setHeight("50px");
        relativeWrapper.getStyle().set("position", "relative");
        relativeWrapper.getStyle().set("margin-bottom", "20px");

        H3 floatingTitles = new H3("Recommended Products                                                                   Recommended Stores");

        floatingTitles.getStyle().set("position", "absolute");
        floatingTitles.getStyle().set("top", "30px");
        floatingTitles.getStyle().set("left", "50%");
        floatingTitles.getStyle().set("transform", "translateX(-50%)");
        floatingTitles.getStyle().set("white-space", "pre");
        relativeWrapper.add(floatingTitles);
        add(relativeWrapper);

        HorizontalLayout productsLayout = new HorizontalLayout();
        productsLayout.setSpacing(true);

        // Example product cards
        productsLayout.add(createProductCard("Smart Watch", "$99.00", "https://www.leafstudios.in/cdn/shop/files/1_1099cd20-7237-4bdf-a180-b7126de5ef3d_grande.png?v=1722230645"));
        productsLayout.add(createProductCard("Wireless Earbuds", "$59.00", "https://admin.gomobile.co.il/wp-content/uploads/2024/08/a4b223cb9bca28717ffe246536221c9e.webp"));
        productsLayout.add(createProductCard("Portable Speaker", "$79.00", "https://www.sencor.com/getmedia/6770caad-d0be-4d0d-b5f0-01bbc4c1c555/35059169.jpg.aspx?width=2100&height=2100&ext=.jpg"));
        HorizontalLayout storesHeaderLayout = new HorizontalLayout();
        storesHeaderLayout.setWidthFull();
        storesHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Center the stores title
        storesHeaderLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout storesLayout = new HorizontalLayout();
        storesLayout.setSpacing(true);

        // Example store cards
        storesLayout.add(createStoreCard("Tech Store", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT9LPPqAt-hXGmPF8OEFLKJfIRbNHmkwDShNA&s"));
        storesLayout.add(createStoreCard("Fashion Hub", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkSF8h_PT2oIXCQbiwv_WrCPlTooRVHY25sw&s"));
        storesLayout.add(createStoreCard("Home Goods", "https://brandlogos.net/wp-content/uploads/2023/08/homegoods-logo_brandlogos.net_85xww.png"));

        Div spacer = new Div();
        spacer.setWidth("40px");

        HorizontalLayout featuredLayout = new HorizontalLayout(productsLayout, spacer, storesLayout);
        featuredLayout.setSpacing(true);
        featuredLayout.setWidthFull();

        add(featuredLayout);
    }

    private VerticalLayout createProductCard(String name, String price, String imageUrl) {
        Image image = new Image(imageUrl, name);
        image.setWidth("120px");

        H4 productName = new H4(name);
        Paragraph productPrice = new Paragraph(price);

        Button buyButton = new Button("Buy Now");

        VerticalLayout card = new VerticalLayout(image, productName, productPrice, buyButton);
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("170px");
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

        VerticalLayout card = new VerticalLayout(logo, storeName, visitButton);
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("200px");
        card.setHeight("250px");
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "10px");

        card.setJustifyContentMode(JustifyContentMode.BETWEEN);
        card.setAlignItems(Alignment.CENTER);

        return card;
    }

}
