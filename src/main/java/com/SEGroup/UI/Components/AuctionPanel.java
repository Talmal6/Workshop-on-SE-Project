package com.SEGroup.UI.Components;

import com.SEGroup.DTO.AuctionDTO;
import com.SEGroup.UI.SecurityContextHolder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Date;
import java.util.function.Consumer;

public class AuctionPanel extends VerticalLayout {

    private final AuctionDTO auction;
    private final AuctionTimer timer;
    private final Span highestBidLabel;
    private final Span bidCountLabel;
    private final Div bidHistoryPanel;
    private final Consumer<Double> bidPlacedCallback;
    private final Runnable auctionEndCallback;
    private boolean isActive = true;

    public AuctionPanel(AuctionDTO auction, Consumer<Double> bidPlacedCallback, Runnable auctionEndCallback) {
        this.auction = auction;
        this.bidPlacedCallback = bidPlacedCallback;
        this.auctionEndCallback = auctionEndCallback;

        // Set up component styling
        addClassName("auction-panel");
        getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)")
                .set("padding", "1.5rem")
                .set("margin-bottom", "1.5rem");

        // Create header with auction status badge
        HorizontalLayout header = createHeader();

        // Create auction information section
        VerticalLayout infoSection = createInfoSection();

        // Create bid history panel (collapsible)
        bidHistoryPanel = createBidHistoryPanel();

        // Create bidding form
        Component biddingForm = createBiddingForm();

        // Auction timer
        timer = new AuctionTimer();
        timer.setEndTime(auction.getEndTime());
        timer.addClassName("auction-timer");
        timer.getStyle()
                .set("font-size", "1.8rem")
                .set("font-weight", "bold")
                .set("margin", "1rem 0")
                .set("padding", "0.5rem")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "4px")
                .set("text-align", "center");

        timer.setOnComplete(() -> {
            isActive = false;
            UI.getCurrent().access(() -> {
                updateAuctionEndedState();
                if (auctionEndCallback != null) {
                    auctionEndCallback.run();
                }
            });
        });

        // Bid status section
        VerticalLayout bidStatusSection = new VerticalLayout();
        bidStatusSection.setPadding(false);
        bidStatusSection.setSpacing(false);

        // Current highest bid
        highestBidLabel = new Span(formatHighestBid());
        highestBidLabel.getStyle()
                .set("font-size", "1.8rem")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        // Bid count
        bidCountLabel = new Span("0 bids placed");
        bidCountLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)");

        bidStatusSection.add(
                new H4("Current Highest Bid"),
                highestBidLabel,
                bidCountLabel
        );

        // Add all components
        add(
                header,
                new Hr(),
                new H3("Time Remaining"),
                timer,
                new Hr(),
                bidStatusSection,
                infoSection,
                new Hr(),
                bidHistoryPanel,
                new Hr(),
                biddingForm
        );

        // Check if auction has already ended
        if (new Date().after(auction.getEndTime())) {
            isActive = false;
            updateAuctionEndedState();
        }
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Live Auction");
        title.getStyle().set("margin", "0");

        Span badge = new Span(isActive ? "ACTIVE" : "ENDED");
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(isActive ? "success" : "error");
        badge.getStyle()
                .set("margin-left", "1rem")
                .set("font-size", "0.8rem");

        HorizontalLayout header = new HorizontalLayout(title, badge);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);

        // Starting price
        Div startingPrice = new Div();
        startingPrice.setText("Starting Price: $" + String.format("%.2f", auction.getStartingPrice()));
        startingPrice.getStyle().set("margin-bottom", "0.5rem");

        // Store information
        Div storeInfo = new Div();
        storeInfo.setText("Seller: " + auction.getStoreName());
        storeInfo.getStyle().set("margin-bottom", "0.5rem");

        // Auction rules
        Paragraph rules = new Paragraph(
                "This is a timed auction. The highest bidder when the timer reaches zero will win the item. " +
                        "All bids must be higher than the current highest bid. The auction will not extend with last-minute bids."
        );
        rules.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9rem")
                .set("margin-top", "1rem");

        section.add(startingPrice, storeInfo, rules);
        return section;
    }

    private Div createBidHistoryPanel() {
        Div panel = new Div();
        panel.addClassName("bid-history-panel");
        panel.getStyle()
                .set("max-height", "200px")
                .set("overflow-y", "auto")
                .set("padding", "0.5rem")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "4px")
                .set("margin", "1rem 0");

        // Initially hide the panel until we have bids
        panel.getStyle().set("display", "none");

        return panel;
    }

    private Component createBiddingForm() {
        HorizontalLayout form = new HorizontalLayout();
        form.setWidthFull();
        form.setPadding(false);
        form.setSpacing(true);
        form.setAlignItems(FlexComponent.Alignment.BASELINE);
        form.getElement().setAttribute("bidding-form", "true");

        if (!isActive) {
            Span endedMessage = new Span("This auction has ended.");
            endedMessage.getStyle()
                    .set("font-weight", "bold")
                    .set("color", "var(--lumo-error-text-color)");
            form.add(endedMessage);
            return form;
        }

        // Only show bidding form for logged in users who are not owners
        boolean isOwner = SecurityContextHolder.isStoreOwner();

        if (isOwner) {
            Span ownerMessage = new Span("You own this auction.");
            ownerMessage.getStyle().set("font-style", "italic");
            form.add(ownerMessage);
            return form;
        }

        if (!SecurityContextHolder.isLoggedIn()) {
            Button loginButton = new Button("Login to Bid", new Icon(VaadinIcon.SIGN_IN));
            loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));
            form.add(new Span("You must be logged in to place bids"), loginButton);
            return form;
        }

        // CRITICAL FIX: Use NumberField instead of TextField
        NumberField bidField = new NumberField("Your Bid");
        bidField.setWidth("200px");
        bidField.setValue(getMinimumBidAmount());
        bidField.setMin(getMinimumBidAmount());
        bidField.setStep(0.01);
        bidField.setPrefixComponent(new Span("$"));
        bidField.setHelperText("Min bid: $" + String.format("%.2f", getMinimumBidAmount()));

        // Place bid button
        Button bidButton = new Button("Place Bid", new Icon(VaadinIcon.GAVEL));
        bidButton.addClassName("bid-button");
        bidButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bidButton.getStyle()
                .set("height", "40px")
                .set("transition", "all 0.2s ease");

        // Add a single click listener with debounce protection
        bidButton.addClickListener(e -> {
            // Disable the button immediately to prevent double-clicks
            bidButton.setEnabled(false);
            try {
                double bidAmount = bidField.getValue();

                if (bidAmount < getMinimumBidAmount()) {
                    showError("Your bid must be at least $" + String.format("%.2f", getMinimumBidAmount()));
                    bidButton.setEnabled(true);
                    return;
                }

                // Call the bid callback
                bidPlacedCallback.accept(bidAmount);

                // Reset the field to minimum bid + a small increment
                bidField.setValue(getMinimumBidAmount() + 0.01);

                // Re-enable the button after a short delay
                UI.getCurrent().access(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                    bidButton.setEnabled(true);
                });
            } catch (Exception ex) {
                showError("Please enter a valid bid amount");
                bidButton.setEnabled(true);
            }
        });

        // Add - and + buttons for easier incrementing
        Button decreaseButton = new Button(new Icon(VaadinIcon.MINUS), click -> {
            double currentVal = bidField.getValue();
            double newVal = Math.max(getMinimumBidAmount(), currentVal - 1.0);
            bidField.setValue(newVal);
        });

        Button increaseButton = new Button(new Icon(VaadinIcon.PLUS), click -> {
            double currentVal = bidField.getValue();
            double newVal = currentVal + 1.0;
            bidField.setValue(newVal);
        });

        // Style the increment/decrement buttons
        for (Button btn : new Button[]{decreaseButton, increaseButton}) {
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.getStyle().set("min-width", "30px");
        }

        // Create a layout for the input with buttons
        HorizontalLayout inputLayout = new HorizontalLayout(decreaseButton, bidField, increaseButton);
        inputLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        inputLayout.setSpacing(true);

        form.add(inputLayout, bidButton);
        return form;
    }

    public void addBidToHistory(String bidder, double amount) {
        // Create bid history entry
        Div entry = new Div();
        entry.getStyle()
                .set("padding", "0.5rem")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("display", "flex")
                .set("justify-content", "space-between");

        // Bidder info
        Span bidderInfo = new Span(formatBidderName(bidder));
        bidderInfo.getStyle().set("font-weight", bidder.equals(SecurityContextHolder.email()) ? "bold" : "normal");

        // Bid amount
        Span amountInfo = new Span(formatPrice(amount));
        amountInfo.getStyle().set("color", "var(--lumo-primary-color)");

        entry.add(bidderInfo, amountInfo);

        // Add to history panel
        bidHistoryPanel.getElement().insertChild(0, entry.getElement());

        // Make sure the panel is visible
        bidHistoryPanel.getStyle().set("display", "block");

        // Update highest bid if needed
        if (auction.getHighestBid() == null || amount > auction.getHighestBid()) {
            auction.setHighestBid(amount);
            auction.setHighestBidder(bidder);
            highestBidLabel.setText(formatHighestBid());
        }

        // Update bid count
        updateBidCount();
    }

    private void updateBidCount() {
        int childCount = bidHistoryPanel.getElement().getChildCount();
        bidCountLabel.setText(childCount + (childCount == 1 ? " bid" : " bids") + " placed");
    }

    private void updateAuctionEndedState() {
        // Update the badge
        getElement().executeJs("this.querySelector('.auction-panel vaadin-badge').innerText = 'ENDED';");
        getElement().executeJs("this.querySelector('.auction-panel vaadin-badge').setAttribute('theme', 'badge error');");

        // Remove the bidding form
        getChildren().forEach(component -> {
            if (component instanceof HorizontalLayout && component.getElement().hasAttribute("bidding-form")) {
                remove(component);
            }
        });

        // Add auction result message
        Div resultMessage = new Div();
        resultMessage.getStyle()
                .set("padding", "1rem")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "4px")
                .set("margin-top", "1rem")
                .set("font-weight", "bold")
                .set("text-align", "center");

        if (auction.getHighestBidder() != null) {
            resultMessage.setText("Auction ended. Winner: " + formatBidderName(auction.getHighestBidder()) +
                    " with bid of " + formatPrice(auction.getHighestBid()));
            resultMessage.getStyle().set("color", "var(--lumo-success-text-color)");
        } else {
            resultMessage.setText("Auction ended with no bids. Item not sold.");
            resultMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        }

        add(resultMessage);
    }

    private double getMinimumBidAmount() {
        // Minimum bid is either the starting price or the current highest bid + 0.01
        if (auction.getHighestBid() == null) {
            return auction.getStartingPrice();
        } else {
            return auction.getHighestBid() + 0.01;
        }
    }

    private String formatHighestBid() {
        if (auction.getHighestBid() == null) {
            return "No bids yet (Starting at " + formatPrice(auction.getStartingPrice()) + ")";
        } else {
            return formatPrice(auction.getHighestBid());
        }
    }

    private String formatPrice(double price) {
        return "$" + String.format("%.2f", price);
    }

    private String formatBidderName(String email) {
        // Show full email for current user, but mask others for privacy
        if (email.equals(SecurityContextHolder.email())) {
            return "You";
        } else {
            // Mask email: j****@example.com
            int atIndex = email.indexOf('@');
            if (atIndex > 1) {
                return email.charAt(0) + "****" + email.substring(atIndex);
            } else {
                return "Anonymous";
            }
        }
    }

    /**
     * Updates the highest bid information without refreshing the entire panel
     */
    public void updateHighestBid(double amount, String bidder) {
        // Update internal auction object
        auction.setHighestBid(amount);
        auction.setHighestBidder(bidder);

        // Update the highest bid display
        highestBidLabel.setText(formatPrice(amount));

        // Add the bid to history display
        addBidToHistory(bidder, amount);

        // Update bid count
        updateBidCount();

        // Update any minimum bid fields if present
        getElement().executeJs(
                "this.querySelectorAll('vaadin-number-field, vaadin-text-field').forEach(field => {" +
                        "  if(field.helperText && field.helperText.includes('Min:')) {" +
                        "    field.helperText = 'Min: $" + String.format("%.2f", amount + 0.01) + "';" +
                        "    if(field.value) {" +
                        "      field.value = " + String.format("%.2f", amount + 0.01) + ";" +
                        "    }" +
                        "  }" +
                        "});"
        );
    }



    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}