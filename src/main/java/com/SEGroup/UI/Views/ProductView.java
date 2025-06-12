package com.SEGroup.UI.Views;

import com.SEGroup.DTO.AuctionDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.*;
import com.SEGroup.UI.Components.AuctionPanel;
import com.SEGroup.UI.Components.AuctionTimer;
import com.SEGroup.UI.Presenter.ProductPresenter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.web.servlet.View;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * View for displaying a single product's details.
 */
@Route(value = "product/:productId/:storeName", layout = MainLayout.class)
@PageTitle("Product Details")
public class ProductView extends VerticalLayout implements HasUrlParameter<String> {
    private final View view;
    private final StoreService storeService;
    private ProductPresenter presenter;
    private final Div productDetails = new Div();
    private String productId;
    private String storeName;
    private boolean isOwner;
    private final DecimalFormat ratingFormat = new DecimalFormat("0.0");

    public ProductView(View view, StoreService storeService) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(productDetails);
        this.view = view;
        this.storeService = storeService;
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

            // Get the DirectNotificationSender from ServiceLocator
            DirectNotificationSender notificationSender = ServiceLocator.getDirectNotificationSender();

            // Create the presenter with the notification sender
            this.presenter = new ProductPresenter(this, productId, storeName, notificationSender);
            this.isOwner = presenter.isOwner();
            presenter.loadProductDetails();
            presenter.loadAuctionInfo();
        } catch (Exception e) {
            showError("Error loading product: " + e.getMessage());
        }
    }

    /**
     * Displays auction information with enhanced UI and real-time updates
     */
    /**
     * Displays auction information with enhanced UI and real-time updates
     */
    public void displayAuctionInfo(AuctionDTO auction) {
        if (auction == null) {
            return;
        }

        // Remove previous auction UI if exists
        getChildren().forEach(c -> {
            if (c instanceof AuctionPanel ||
                    c.getElement().hasAttribute("auction-panel") ||
                    c.getElement().hasAttribute("auction-bar")) {
                remove(c);
            }
        });

        // Create the enhanced auction panel
        AuctionPanel auctionPanel = new AuctionPanel(
                auction,
                // Bid callback
                bid -> presenter.placeBid(bid),
                // Auction end callback
                () -> presenter.processAuctionEnd());

        // Set an attribute for easier identification
        auctionPanel.getElement().setAttribute("auction-panel", "true");

        // Add to the view at the top
        addComponentAtIndex(0, auctionPanel);
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
                            "  document.getElementById('product-icon-fallback').style.display = 'flex';" + // Show the
                                                                                                           // fallback
                            "  return true;" + // Prevent infinite error loop
                            "};");
        } else {
            // No image URL available, use icon immediately
            System.out.println("No image URL available for product: " + product.getName());
            String productType = detectProductType(product.getName(), product.getDescription());
            Div productIcon = createProductIcon(productType, product.getName());
            imageContainer.add(productIcon);
        }

        // Product info section
        VerticalLayout productInfo = new VerticalLayout();
        productInfo.setSpacing(false);
        productInfo.setPadding(false);

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

        Paragraph description = new Paragraph(product.getDescription());
        description.getStyle().set("max-width", "600px");

        Div storeInfo = new Div();
        Span storeLabel = new Span("Sold by: ");
        Button storeLink = new Button(product.getStoreName(),
                e -> getUI().ifPresent(ui -> ui.navigate("store/" + product.getStoreName())));
        storeLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        storeLink.getStyle().set("font-weight", "bold");
        storeInfo.add(storeLabel, storeLink);

        productInfo.add(
                productName,
                ratingDiv,
                description,
                storeInfo);

        // Setup product layout with image and info
        HorizontalLayout productLayout = new HorizontalLayout(
                imageContainer,
                productInfo);
        productLayout.setAlignItems(FlexComponent.Alignment.START);
        productLayout.setWidthFull();

        // 1. Immediate Purchase Panel
        Div immediatePanel = new Div();
        immediatePanel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "20px");

        H4 immediateTitle = new H4("Immediate Purchase");
        immediateTitle.getStyle().set("margin-top", "0");

        H3 priceHeader = new H3("$" + String.format("%.2f", product.getPrice()));
        priceHeader.getStyle().set("color", "var(--lumo-primary-color)");

        Button addToCartBtn = new Button("Add to Cart", VaadinIcon.CART.create());
        addToCartBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartBtn.addClickListener(e -> presenter.addToCart());

        immediatePanel.add(
                immediateTitle,
                priceHeader,
                new Paragraph("Buy now at the listed price."),
                addToCartBtn);

        // 2. Make an Offer Panel
        Div bidPanel = new Div();
        bidPanel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("margin-bottom", "20px");

        H4 bidTitle = new H4("Make an Offer");
        bidTitle.getStyle().set("margin-top", "0");

        TextField bidField = new TextField("Your Offer");
        bidField.setWidth("200px");
        bidField.setPlaceholder("Enter your price");
        bidField.setPrefixComponent(new Span("$"));

        Button bidButton = new Button("Submit Offer", VaadinIcon.GAVEL.create());
        bidButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bidButton.addClickListener(e -> {
            try {
                double amount = Double.parseDouble(bidField.getValue());
                if (amount <= 0) {
                    showError("Please enter a valid positive amount");
                    return;
                }
                presenter.bidBuy(amount);
                bidField.clear();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid number");
            }
        });

        String bidInfoText = "Make an offer below the product's listed price. Store owners will review your offer and can accept, reject, or counter-offer.";
        Paragraph bidInfo = new Paragraph(bidInfoText);
        bidInfo.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)");

        bidPanel.add(
                bidTitle,
                bidInfo,
                new HorizontalLayout(bidField, bidButton));

        // 3. Auction Panel (for owners or showing active auction)
        Div auctionPanel = new Div();
        auctionPanel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "16px");

        H4 auctionTitle = new H4("Auction");
        auctionTitle.getStyle().set("margin-top", "0");

        AuctionDTO currentAuction = presenter.getAuction();

        if (currentAuction != null) {
            // Display active auction info
            Span highestBidLabel = new Span("Current highest bid: ");
            Span highestBidValue = new Span("$" + String.format("%.2f",
                    currentAuction.getHighestBid() != null ? currentAuction.getHighestBid()
                            : currentAuction.getStartingPrice()));
            highestBidValue.getStyle().set("font-weight", "bold");

            Span highestBidderLabel = new Span("Highest bidder: ");
            Span highestBidderValue = new Span(
                    currentAuction.getHighestBidder() != null ? currentAuction.getHighestBidder() : "None yet");

            HorizontalLayout bidderInfo = new HorizontalLayout(
                    highestBidderLabel, highestBidderValue);

            // Create auction timer component
            AuctionTimer timer = new AuctionTimer();
            timer.setEndTime(currentAuction.getEndTime());
            timer.setOnComplete(() -> {
                // When timer completes, refresh to show auction results
                getUI().ifPresent(ui -> ui.getPage().reload());
            });

            Paragraph timerLabel = new Paragraph("Time remaining:");

            // For buyers - allow placing bids if auction active
            if (!presenter.isOwner()) {
                TextField auctionBidField = new TextField("Your Bid");
                auctionBidField.setWidth("200px");
                auctionBidField.setPlaceholder("Enter bid amount");
                auctionBidField.setPrefixComponent(new Span("$"));

                double minBid = currentAuction.getHighestBid() != null ? currentAuction.getHighestBid() + 0.01
                        : currentAuction.getStartingPrice();

                auctionBidField.setHelperText("Min: $" + String.format("%.2f", minBid));

                Button auctionBidBtn = new Button("Place Bid", VaadinIcon.GAVEL.create());
                auctionBidBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                auctionBidBtn.addClickListener(e -> {
                    try {
                        double amount = Double.parseDouble(auctionBidField.getValue());
                        presenter.placeBid(amount);
                        auctionBidField.clear();
                    } catch (NumberFormatException ex) {
                        showError("Please enter a valid number");
                    }
                });

                auctionPanel.add(
                        auctionTitle,
                        new Paragraph("This item is currently up for auction!"),
                        bidInfo,
                        bidderInfo,
                        timerLabel,
                        timer,
                        new HorizontalLayout(auctionBidField, auctionBidBtn));
            } else {
                // For owners - show auction management
                auctionPanel.add(
                        auctionTitle,
                        new Paragraph("Auction in progress!"),
                        bidInfo,
                        bidderInfo,
                        timerLabel,
                        timer);
            }
        } else if (presenter.isOwner()) {
            // For owners when no auction exists
            Paragraph auctionInfo = new Paragraph(
                    "Start an auction to let buyers place competitive bids with a time limit. The highest bidder will automatically win.");
            auctionInfo.getStyle()
                    .set("font-style", "italic")
                    .set("color", "var(--lumo-secondary-text-color)");

            Button startAuctionBtn = new Button("Start Auction", VaadinIcon.TIMER.create());
            startAuctionBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            startAuctionBtn.addClickListener(e -> openStartAuctionDialog());

            auctionPanel.add(
                    auctionTitle,
                    auctionInfo,
                    startAuctionBtn);
        } else {
            // For buyers when no auction exists
            Paragraph auctionInfo = new Paragraph("No auction is currently running for this product.");
            auctionInfo.getStyle()
                    .set("font-style", "italic")
                    .set("color", "var(--lumo-secondary-text-color)");

            auctionPanel.add(
                    auctionTitle,
                    auctionInfo);
        }

        // Owner Management Actions
        Div ownerPanel = new Div();
        if (presenter.isOwner()) {
            ownerPanel.getStyle()
                    .set("border", "1px solid var(--lumo-primary-color)")
                    .set("border-radius", "8px")
                    .set("padding", "16px")
                    .set("margin", "20px 0")
                    .set("background-color", "var(--lumo-primary-color-10pct)");

            H4 ownerTitle = new H4("Store Owner Controls");
            ownerTitle.getStyle().set("margin-top", "0");

            Button manageOffersBtn = new Button("Manage Offers", VaadinIcon.USERS.create());
            manageOffersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            manageOffersBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(
                    String.format("product/%s/%s/bids", productId, storeName))));

            ownerPanel.add(
                    ownerTitle,
                    new Paragraph("Manage customer offers and auctions for this product."),
                    manageOffersBtn);
        } else {
            // No content for non-owners
            ownerPanel.setVisible(false);
        }

        // Combine all panels into the final layout
        H3 purchaseOptionsHeader = new H3("Purchase Options");
        purchaseOptionsHeader.getStyle().set("margin-top", "20px");

        VerticalLayout content = new VerticalLayout(
                navigationBar,
                productLayout,
                purchaseOptionsHeader,
                immediatePanel,
                bidPanel,
                auctionPanel,
                ownerPanel);
        content.setPadding(false);
        content.setSpacing(true);

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
        backToMarketButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("catalog")));

        // Button to go back to the store
        Button backToStoreButton = new Button("Back to " + storeName, VaadinIcon.STORAGE.create());
        backToStoreButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToStoreButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("store/" + storeName)));

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
            case "Smartphone":
                return "#3498db";
            case "Laptop":
                return "#2980b9";
            case "Coffee Maker":
                return "#8e44ad";
            case "Vacuum":
                return "#2c3e50";
            case "Smartwatch":
                return "#1abc9c";
            case "Jacket":
                return "#d35400";
            case "Monitor":
                return "#27ae60";
            case "Earbuds":
                return "#f39c12";
            case "Bedding":
                return "#3498db";
            case "Knives":
                return "#c0392b";
            case "Bag":
                return "#16a085";
            case "Scarf":
                return "#e74c3c";
            case "Shoes":
                return "#f1c40f";
            default:
                return "#34495e";
        }
    }

    /**
     * Opens an enhanced dialog for starting a new auction
     */
    private void openStartAuctionDialog() {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Start Auction for " + presenter.getProductName());
        dlg.setWidth("600px"); // Make it wider

        HorizontalLayout content = new HorizontalLayout(); // Use horizontal layout
        content.setWidthFull();
        content.setSpacing(true);

        // Product info in left column
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(true);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("40%");

        Div productInfo = new Div();
        productInfo.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "1rem")
                .set("border-radius", "4px")
                .set("margin-bottom", "1rem");

        H4 productName = new H4(presenter.getProductName());
        productName.getStyle().set("margin", "0 0 0.5rem 0");

        Paragraph productDesc = new Paragraph(
                "Starting an auction will allow users to bid on this product until the auction ends. The highest bidder will win the product.");
        productDesc.getStyle().set("margin", "0");

        productInfo.add(productName, productDesc);
        leftColumn.add(productInfo);

        // Settings in right column
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(true);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("60%");

        // Starting price field
        NumberField startPrice = new NumberField("Starting Price");
        startPrice.setWidthFull();
        startPrice.setMin(0);
        startPrice.setPrefixComponent(new Span("$"));
        startPrice.setValue(presenter.getProduct().getPrice() * 0.7); // Suggest 70% of regular price
        startPrice.setRequiredIndicatorVisible(true);
        startPrice.setStepButtonsVisible(true);

        // Duration field with radio buttons in a nice format
        H5 durationLabel = new H5("Auction Duration");
        durationLabel.getStyle().set("margin-bottom", "0.5rem");

        RadioButtonGroup<String> durationOptions = new RadioButtonGroup<>();
        durationOptions.setItems("1 hour", "6 hours", "12 hours", "24 hours", "3 days", "7 days", "Custom");
        durationOptions.setValue("24 hours");
        durationOptions.getStyle().set("margin-bottom", "1rem");

        // Custom duration field
        NumberField customDuration = new NumberField("Custom Duration (minutes)");
        customDuration.setWidthFull();
        customDuration.setMin(10);
        customDuration.setValue(60.0); // Default 1 hour
        customDuration.setVisible(false);
        customDuration.setStepButtonsVisible(true);

        durationOptions.addValueChangeListener(e -> {
            customDuration.setVisible(e.getValue().equals("Custom"));
        });

        // Reserve price option with better styling
        Checkbox reservePrice = new Checkbox("Set reserve price");
        reservePrice.setWidthFull();
        reservePrice.getStyle().set("margin-top", "1rem");

        NumberField reservePriceField = new NumberField("Reserve Price");
        reservePriceField.setWidthFull();
        reservePriceField.setPrefixComponent(new Span("$"));
        reservePriceField.setVisible(false);
        reservePriceField.setStepButtonsVisible(true);

        reservePrice.addValueChangeListener(e -> {
            reservePriceField.setVisible(e.getValue());
        });

        rightColumn.add(
                startPrice,
                durationLabel,
                durationOptions,
                customDuration,
                reservePrice,
                reservePriceField);

        // Add columns to content
        content.add(leftColumn, rightColumn);

        // Footer buttons
        Button cancel = new Button("Cancel", e -> dlg.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button ok = new Button("Start Auction", new Icon(VaadinIcon.TIMER));
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        ok.getStyle().set("margin-left", "auto");

        ok.addClickListener(e -> {
            try {
                double sp = startPrice.getValue();

                // Calculate duration in minutes
                long durationMinutes;
                String selectedDuration = durationOptions.getValue();
                switch (selectedDuration) {
                    case "1 hour":
                        durationMinutes = 60;
                        break;
                    case "6 hours":
                        durationMinutes = 360;
                        break;
                    case "12 hours":
                        durationMinutes = 720;
                        break;
                    case "24 hours":
                        durationMinutes = 1440;
                        break;
                    case "3 days":
                        durationMinutes = 4320;
                        break;
                    case "7 days":
                        durationMinutes = 10080;
                        break;
                    case "Custom":
                        durationMinutes = customDuration.getValue().longValue();
                        break;
                    default:
                        durationMinutes = 1440; // Default to 24 hours
                }

                long durMs = durationMinutes * 60000;
                Date endDate = new Date(System.currentTimeMillis() + durMs);

                presenter.startAuction(sp, endDate);
                dlg.close();
            } catch (Exception ex) {
                showError("Invalid values: " + ex.getMessage());
            }
        });

        dlg.add(content);
        dlg.getFooter().add(cancel, ok);
        dlg.open();
    }

    /**
     * Opens a dialog showing all bids for the current product
     */
    public void openBidsDialog() {

        Dialog bidsDialog = new Dialog();
        bidsDialog.setHeaderTitle("Offers for " + presenter.getProductName());
        bidsDialog.setWidth("800px");

        Result<List<BidDTO>> bidsResult = presenter.loadProductBids();

        if (!bidsResult.isSuccess() || bidsResult.getData() == null || bidsResult.getData().isEmpty()) {
            bidsDialog.add(createNoBidsMessage());
        } else {
            // Create a grid for bids
            Grid<BidDTO> bidsGrid = new Grid<>();
            bidsGrid.addColumn(BidDTO::getOriginalBidderEmail).setHeader("Customer").setAutoWidth(true);
            bidsGrid.addColumn(bid -> String.format("$%.2f", bid.getPrice())).setHeader("Offered Price")
                    .setAutoWidth(true);

            // Add actions column
            bidsGrid.addComponentColumn(bid -> {
                HorizontalLayout actions = new HorizontalLayout();

                Button acceptBtn = new Button("Accept", VaadinIcon.CHECK.create());
                acceptBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

                Button rejectBtn = new Button("Decline", VaadinIcon.CLOSE_SMALL.create());
                rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

                Button counterBtn = new Button("Counter", VaadinIcon.EXCHANGE.create());
                counterBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);

                acceptBtn.addClickListener(e -> {
                    Result<Void> result = presenter.acceptBid(bid.getOriginalBidderEmail(), bid.getPrice());
                    if (result.isSuccess()) {
                        showSuccess("Offer accepted! The customer has been notified.");
                        bidsDialog.close();
                    } else {
                        showError("Error accepting offer: " + result.getErrorMessage());
                    }
                });

                rejectBtn.addClickListener(e -> {
                    presenter.rejectBid(bid.getOriginalBidderEmail(), bid.getPrice());
                    showSuccess("Offer declined. The customer has been notified.");
                    bidsDialog.close();
                });

                counterBtn.addClickListener(e -> {
                    makeCounterOffer(bid.getOriginalBidderEmail(), bid.getPrice());
                    bidsDialog.close();
                });

                actions.add(acceptBtn, rejectBtn, counterBtn);
                actions.setSpacing(true);
                return actions;
            }).setHeader("Actions").setFlexGrow(1);

            bidsGrid.setItems(bidsResult.getData());
            bidsGrid.setHeight("400px");

            bidsDialog.add(bidsGrid);
        }

        Button closeBtn = new Button("Close", e -> bidsDialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bidsDialog.getFooter().add(closeBtn);

        bidsDialog.open();
    }

    /**
     * Opens a dialog to make a counter offer to a bidder
     */
    private void makeCounterOffer(String bidderEmail, double originalPrice) {
        Dialog counterDialog = new Dialog();
        counterDialog.setHeaderTitle("Make Counter Offer");

        TextField priceField = new TextField("Counter Price");
        priceField.setValue(String.valueOf(originalPrice));
        priceField.setPrefixComponent(new Span("$"));

        TextField messageField = new TextField("Additional Message");
        messageField.setPlaceholder("Optional message to the customer");

        Button sendBtn = new Button("Send Counter Offer", e -> {
            try {
                double counterPrice = Double.parseDouble(priceField.getValue());
                String additionalMessage = !messageField.isEmpty() ? messageField.getValue() : null;
                presenter.counterBid(bidderEmail, counterPrice, additionalMessage);
                showSuccess("Counter offer sent to customer");
                counterDialog.close();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid price");
            }
        });

        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> counterDialog.close());

        VerticalLayout layout = new VerticalLayout(
                new Paragraph("The customer will be notified of your counter offer"),
                priceField,
                messageField);

        counterDialog.add(layout);
        counterDialog.getFooter().add(cancelBtn, sendBtn);

        counterDialog.open();
    }

    /**
     * Shows an informational message
     */
    public void showInfo(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    /**
     * Shows a section separator with a title
     */
    private Component createSectionDivider(String title) {
        HorizontalLayout divider = new HorizontalLayout();
        divider.setWidthFull();

        Hr leftLine = new Hr();
        leftLine.setWidth("30%");

        H5 sectionTitle = new H5(title);
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-secondary-text-color)");

        Hr rightLine = new Hr();
        rightLine.setWidth("30%");

        divider.add(leftLine, sectionTitle, rightLine);
        divider.setAlignItems(FlexComponent.Alignment.CENTER);
        divider.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        return divider;
    }

    /**
     * Shows a message when no bids are available
     */
    private Component createNoBidsMessage() {
        Div container = new Div();
        container.getStyle()
                .set("text-align", "center")
                .set("padding", "20px")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic");

        container.add(new Span("No bids have been placed yet"));

        return container;
    }

    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Shows auction ended with a winner
     */
    /**
     * Shows auction ended with a winner
     */
    public void showAuctionEnded(String winnerEmail, double winningBid) {
        // Remove auction display if it exists
        getChildren().forEach(component -> {
            if (component.getElement().hasAttribute("auction-panel") ||
                    component.getElement().hasAttribute("auction-bar")) {
                remove(component);
            }
        });

        // Create auction result display
        HorizontalLayout resultLayout = new HorizontalLayout();
        resultLayout.setWidthFull();
        resultLayout.getElement().setAttribute("auction-bar", "true");

        H3 header = new H3("Auction Completed");
        header.getStyle().set("color", "var(--lumo-primary-color)");

        Span winnerInfo = new Span("Winner: " + winnerEmail + " with bid of $" + String.format("%.2f", winningBid));
        winnerInfo.getStyle().set("font-weight", "bold");

        resultLayout.add(header, winnerInfo);
        resultLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        resultLayout.getStyle()
                .set("border", "1px solid var(--lumo-primary-color)")
                .set("padding", "1em")
                .set("border-radius", "8px")
                .set("margin-bottom", "1em")
                .set("background-color", "var(--lumo-contrast-5pct)");

        addComponentAtIndex(0, resultLayout);
    }

    /**
     * Shows auction ended with no winner
     */
    public void showAuctionEndedNoWinner() {
        // Remove auction display if it exists
        getChildren().forEach(component -> {
            if (component.getElement().hasAttribute("auction-panel") ||
                    component.getElement().hasAttribute("auction-bar")) {
                remove(component);
            }
        });

        // Create auction result display
        HorizontalLayout resultLayout = new HorizontalLayout();
        resultLayout.setWidthFull();
        resultLayout.getElement().setAttribute("auction-bar", "true");

        H3 header = new H3("Auction Completed");
        header.getStyle().set("color", "var(--lumo-primary-color)");

        Span noWinnerInfo = new Span("No bids were placed. Item not sold.");
        noWinnerInfo.getStyle().set("font-style", "italic");

        resultLayout.add(header, noWinnerInfo);
        resultLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        resultLayout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("padding", "1em")
                .set("border-radius", "8px")
                .set("margin-bottom", "1em")
                .set("background-color", "var(--lumo-contrast-5pct)");

        addComponentAtIndex(0, resultLayout);
    }

    private void addTestNotificationButton() {
        Button testBtn = new Button("Test Notification", e -> {
            try {
                DirectNotificationSender dns = ServiceLocator.getDirectNotificationSender();

                // Current user viewing the product
                String currentUser = SecurityContextHolder.email();

                // Send to the viewing user
                dns.sendSystemNotification(currentUser, "Test notification at " + new Date());

                String owner = "owner@demo.com"; // Hardcoded for testing
                if (!owner.equals(currentUser)) {
                    // Also send a notification to the owner
                    dns.sendSystemNotification(owner, "Product viewed by " + currentUser);
                }

                // Use the view's methods directly
                showSuccess("Test notifications sent!");
            } catch (Exception ex) {
                showError("Failed to send test notification: " + ex.getMessage());
            }
        });

        // Add to the layout at a good position
        HorizontalLayout buttonRow = new HorizontalLayout(testBtn);
        buttonRow.setMargin(true);

        // Add it near the top of the view, but after the product details
        if (getComponentCount() > 1) {
            addComponentAtIndex(1, buttonRow);
        } else {
            add(buttonRow);
        }
    }

    public void updateAuctionHighestBid(double amount, String bidder) {
        // Find the auction panel
        getChildren().forEach(c -> {
            if (c instanceof AuctionPanel) {
                AuctionPanel panel = (AuctionPanel) c;
                // Call the update method on the panel - FIXED CORRECT METHOD NAME
                panel.updateHighestBid(amount, bidder);
            }
        });
    }

}