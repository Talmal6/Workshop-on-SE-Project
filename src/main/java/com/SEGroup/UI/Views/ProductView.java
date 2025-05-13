package com.SEGroup.UI.Views;

import com.SEGroup.DTO.AuctionDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.ProductPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.web.servlet.View;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * View for displaying a single product's details.
 */
@Route(value = "product/:productId/:storeName", layout = MainLayout.class)
@PageTitle("Product Details")
public class ProductView extends VerticalLayout implements HasUrlParameter<String> {
    private final View view;
    private ProductPresenter presenter;
    private final Div productDetails = new Div();
    private String productId;
    private String storeName;
    private boolean isOwner;
    private final DecimalFormat ratingFormat = new DecimalFormat("0.0");

    public ProductView(View view) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(productDetails);
        this.view = view;
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        // Extract productId and storeName from the URL
        try {
            RouteParameters params = event.getRouteParameters();
            this.productId = params.get("productId").orElse("");
            this.storeName = params.get("storeName").orElse("");

            if (productId.isEmpty() || storeName.isEmpty()) {
                showError("Invalid product or store information");
                return;
            }

            this.presenter = new ProductPresenter(this, productId, storeName);
            this.isOwner = presenter.isOwner();
            presenter.loadAuctionInfo();
            presenter.loadProductDetails();
        } catch (Exception e) {
            showError("Error loading product: " + e.getMessage());
        }
    }

    public void displayAuctionInfo(AuctionDTO auction) {
        if (auction == null) {
            return;
        }
        if (isOwner) {
            Button startAuctionBtn = new Button("Start Auction", VaadinIcon.GAVEL.create());
            startAuctionBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            startAuctionBtn.addClickListener(e -> openStartAuctionDialog());
            addComponentAtIndex(0, startAuctionBtn);
        }

        // 1) convert the Date->Instant for formatting
        Instant endInstant = auction.getEndTime().toInstant();
        String endsAt = endInstant
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .toString();

        // 2) optionally show "time remaining"
        long millisLeft = auction.getTimeRemainingMillis();
        long seconds = (millisLeft / 1000) % 60;
        long minutes = (millisLeft / (1000 * 60)) % 60;
        long hours   = millisLeft / (1000 * 60 * 60);
        String remaining = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        Span currentBid  = new Span("Current bid: $ " + auction.getHighestBid());
        Span endsAtSpan  = new Span("Ends at: " + endsAt);
        Span remainSpan  = new Span("Time left: " + remaining);

        TextField bidField = new TextField("Your bid");
        bidField.setWidth("150px");

        Button bidBtn = new Button("Place Bid", VaadinIcon.GAVEL.create());
        bidBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        TextField quantityField = new TextField("Your quantity");
        quantityField.setWidth("150px");
        bidBtn.addClickListener(e -> {
            try {
                double amt = Double.parseDouble(bidField.getValue());
                Integer qu = Integer.parseInt((quantityField.getValue()));
                presenter.placeBid(amt, qu);
            } catch (NumberFormatException ex) {
                showError("Please enter a valid number");
            }
        });

        HorizontalLayout auctionBar = new HorizontalLayout(
                currentBid,
                endsAtSpan,
                remainSpan,
                bidField,
                quantityField,
                bidBtn
        );
        auctionBar.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "1em")
                .set("border-radius", "8px")
                .set("margin-bottom", "1em");

        // insert at top
        addComponentAtIndex(0, auctionBar);
    }

    public void displayProduct(ShoppingProductDTO product) {
        productDetails.removeAll();

        // Create navigation buttons
        HorizontalLayout navigationBar = createNavigationBar(product);

        // Product image container
        Div imageContainer = new Div();
        imageContainer.setWidth("400px");
        imageContainer.setHeight("400px");
        imageContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");

        // First try to load the actual product image if available
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            System.out.println("Attempting to load image URL: " + product.getImageUrl());

            // Create real image element
            Image productImage = new Image(product.getImageUrl(), product.getName());
            productImage.setId("product-real-image");
            productImage.setMaxHeight("350px");
            productImage.setMaxWidth("350px");
            productImage.getStyle().set("object-fit", "contain");

            // Create fallback icon div (hidden initially)
            String productType = detectProductType(product.getName(), product.getDescription());
            Div iconFallback = createProductIcon(productType, product.getName());
            iconFallback.setId("product-icon-fallback");
            iconFallback.getStyle().set("display", "none"); // Initially hidden

            // Add both to container
            imageContainer.add(productImage, iconFallback);

            // Setup error handling to switch to icon if image fails
            productImage.getElement().executeJs(
                    "this.onerror = function() {" +
                            "  console.log('Image failed to load: ' + this.src);" +
                            "  this.style.display = 'none';" + // Hide the broken image
                            "  document.getElementById('product-icon-fallback').style.display = 'flex';" + // Show the fallback
                            "  return true;" + // Prevent infinite error loop
                            "};"
            );
        } else {
            // No image URL available, use icon immediately
            System.out.println("No image URL available for product: " + product.getName());
            String productType = detectProductType(product.getName(), product.getDescription());
            Div productIcon = createProductIcon(productType, product.getName());
            imageContainer.add(productIcon);
        }

        // Product info
        H2 productName = new H2(product.getName());

        Div ratingDiv = new Div();
        // Format rating to one decimal place
        String formattedRating = ratingFormat.format(product.getAvgRating());
        Span ratingText = new Span("Rating: " + formattedRating + " / 5.0");

        // Add star icons based on rating
        Span stars = new Span();
        stars.getStyle().set("color", "gold").set("margin-left", "10px");
        int fullStars = (int) Math.floor(product.getAvgRating());
        boolean hasHalfStar = product.getAvgRating() - fullStars >= 0.5;

        StringBuilder starsString = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            starsString.append("★");
        }
        if (hasHalfStar) {
            starsString.append("⯨");
        }
        for (int i = 0; i < 5 - fullStars - (hasHalfStar ? 1 : 0); i++) {
            starsString.append("☆");
        }
        stars.setText(starsString.toString());

        ratingDiv.add(ratingText, stars);

        H3 priceHeader = new H3("$" + String.format("%.2f", product.getPrice()));
        priceHeader.getStyle().set("color", "var(--lumo-primary-color)");

        Paragraph description = new Paragraph(product.getDescription());
        description.getStyle().set("max-width", "600px");

        Div storeInfo = new Div();
        Span storeLabel = new Span("Sold by: ");
        Button storeLink = new Button(product.getStoreName(), e ->
                getUI().ifPresent(ui -> ui.navigate("store/" + product.getStoreName()))
        );
        storeLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        storeLink.getStyle().set("font-weight", "bold");
        storeInfo.add(storeLabel, storeLink);

        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart", VaadinIcon.CART.create());
        addToCartBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartBtn.addClickListener(e -> presenter.addToCart());

        TextField offer = new TextField("BID Offer Price");
        Button bidBuyBtn = new Button("Enter", VaadinIcon.GAVEL.create());
        bidBuyBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        bidBuyBtn.addClickListener(e -> {
            if(!offer.isEmpty()) {
                presenter.bidBuy(offer.getValue());
            }
            else{
                showError("Text is empty");
            }
        });

        // Organize content in layouts
        VerticalLayout productInfo = new VerticalLayout(
                productName,
                ratingDiv,
                priceHeader,
                description,
                storeInfo
        );
        productInfo.setSpacing(false);
        productInfo.setPadding(false);

        HorizontalLayout productLayout = new HorizontalLayout(
                imageContainer,
                productInfo
        );
        productLayout.setAlignItems(FlexComponent.Alignment.START);
        productLayout.setWidthFull();

        VerticalLayout content = new VerticalLayout(
                navigationBar,
                productLayout,
                addToCartBtn,
                offer,
                bidBuyBtn
        );
        content.setAlignSelf(FlexComponent.Alignment.START, navigationBar);
        content.setAlignSelf(FlexComponent.Alignment.START, addToCartBtn);

        productDetails.add(content);
    }

    /**
     * Creates a navigation bar with back buttons
     */
    private HorizontalLayout createNavigationBar(ShoppingProductDTO product) {
        // Button to go back to marketplace
        Button backToMarketButton = new Button("Back to Marketplace", VaadinIcon.ARROW_LEFT.create());
        backToMarketButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        backToMarketButton.getStyle()
                .set("margin-bottom", "20px")
                .set("margin-right", "10px");
        backToMarketButton.addClickListener(e -> getUI().ifPresent(ui ->
                ui.navigate("catalog")));

        // Button to go back to the store
        Button backToStoreButton = new Button("Back to " + storeName, VaadinIcon.STORAGE.create());
        backToStoreButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToStoreButton.addClickListener(e -> getUI().ifPresent(ui ->
                ui.navigate("store/" + storeName)));

        HorizontalLayout navigationBar = new HorizontalLayout(backToMarketButton, backToStoreButton);
        navigationBar.setSpacing(true);
        return navigationBar;
    }

    /**
     * Creates a styled icon div for a product based on its type
     */
    private Div createProductIcon(String productType, String productName) {
        Div iconContainer = new Div();
        iconContainer.setWidth("300px");
        iconContainer.setHeight("300px");
        iconContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("border-radius", "8px")
                .set("background-color", getColorForProductType(productType))
                .set("color", "white")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("text-align", "center");

        // First letter as large icon
        Span iconLetter = new Span(productType.substring(0, 1).toUpperCase());
        iconLetter.getStyle()
                .set("font-size", "80px")
                .set("font-weight", "bold")
                .set("margin-bottom", "10px");

        // Product type label
        Span typeLabel = new Span(productType);
        typeLabel.getStyle()
                .set("font-size", "18px")
                .set("opacity", "0.8");

        // Name (optional)
        Span nameLabel = new Span(productName);
        nameLabel.getStyle()
                .set("font-size", "14px")
                .set("margin-top", "10px")
                .set("max-width", "90%")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        iconContainer.add(iconLetter, typeLabel, nameLabel);
        return iconContainer;
    }

    /**
     * Detects product type based on name and description
     */
    private String detectProductType(String name, String description) {
        String nameLower = name.toLowerCase();
        String descLower = description != null ? description.toLowerCase() : "";

        if (nameLower.contains("phone") || descLower.contains("smartphone")) {
            return "Smartphone";
        } else if (nameLower.contains("laptop") || descLower.contains("laptop")) {
            return "Laptop";
        } else if (nameLower.contains("coffee") || descLower.contains("coffee")) {
            return "Coffee Maker";
        } else if (nameLower.contains("vacuum") || descLower.contains("vacuum")) {
            return "Vacuum";
        } else if (nameLower.contains("watch") || descLower.contains("watch")) {
            return "Smartwatch";
        } else if (nameLower.contains("jacket") || descLower.contains("jacket")) {
            return "Jacket";
        } else if (nameLower.contains("monitor") || descLower.contains("monitor")) {
            return "Monitor";
        } else if (nameLower.contains("earbuds") || descLower.contains("earbuds")) {
            return "Earbuds";
        } else if (nameLower.contains("bedding") || descLower.contains("bedding")) {
            return "Bedding";
        } else if (nameLower.contains("knife") || descLower.contains("knife")) {
            return "Knives";
        } else if (nameLower.contains("tote") || nameLower.contains("bag")) {
            return "Bag";
        } else if (nameLower.contains("scarf")) {
            return "Scarf";
        } else if (nameLower.contains("shoes") || descLower.contains("shoes")) {
            return "Shoes";
        } else {
            return "Product";
        }
    }

    /**
     * Returns a color for product type
     */
    private String getColorForProductType(String type) {
        switch (type) {
            case "Smartphone": return "#3498db";
            case "Laptop": return "#2980b9";
            case "Coffee Maker": return "#8e44ad";
            case "Vacuum": return "#2c3e50";
            case "Smartwatch": return "#1abc9c";
            case "Jacket": return "#d35400";
            case "Monitor": return "#27ae60";
            case "Earbuds": return "#f39c12";
            case "Bedding": return "#3498db";
            case "Knives": return "#c0392b";
            case "Bag": return "#16a085";
            case "Scarf": return "#e74c3c";
            case "Shoes": return "#f1c40f";
            default: return "#34495e";
        }
    }

    private void openStartAuctionDialog() {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Start Auction for " + presenter.getProductName());

        NumberField startPrice = new NumberField("Starting Price");
        startPrice.setMin(0);
        startPrice.setRequiredIndicatorVisible(true);

        NumberField duration = new NumberField("Duration (minutes)");
        duration.setMin(1);
        duration.setRequiredIndicatorVisible(true);

        Button cancel = new Button("Cancel", e -> dlg.close());
        Button ok = new Button("Start", e -> {
            try {
                double sp = startPrice.getValue();
                long durMs = (long) (duration.getValue() * 60000);
                Date endDate = new Date(System.currentTimeMillis() + durMs);  // <-- פה השינוי
                presenter.startAuction(sp, endDate);
                dlg.close();
            } catch (Exception ex) {
                showError("Invalid values");
            }
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dlg.add(new VerticalLayout(startPrice, duration));
        dlg.getFooter().add(cancel, ok);
        dlg.open();
    }

    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreName() {
        return storeName;
    }
}