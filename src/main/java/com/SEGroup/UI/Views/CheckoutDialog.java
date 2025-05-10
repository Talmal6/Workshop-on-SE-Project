package com.SEGroup.UI.Views;

import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.CartPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class CheckoutDialog extends Dialog {
    private final CartPresenter presenter;
    private final MainLayout mainLayout;
    private final Binder<CreditCardDetails> binder;
    private final CreditCardDetails creditCardDetails;
    private final TextField cardNumber;
    private final TextField cardHolder;
    private final TextField expiryDate;
    private final TextField cvv;

    public CheckoutDialog(CartPresenter presenter) {
        this.presenter = presenter;
        this.mainLayout = MainLayout.getInstance();
        this.creditCardDetails = new CreditCardDetails();
        this.binder = new Binder<>(CreditCardDetails.class);

        setWidth("400px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        // Title
        add(new H3("Checkout"));

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Credit card fields
        cardNumber = new TextField("Card Number");
        cardNumber.setPlaceholder("1234 5678 9012 3456");
        cardNumber.setMaxLength(19); // 16 digits + 3 spaces
        cardNumber.setPattern("[0-9 ]*");
        cardNumber.setAllowedCharPattern("[0-9 ]");

        cardHolder = new TextField("Card Holder Name");
        cardHolder.setPlaceholder("John Doe");

        expiryDate = new TextField("Expiry Date");
        expiryDate.setPlaceholder("MM/YY");
        expiryDate.setMaxLength(5);
        expiryDate.setPattern("(0[1-9]|1[0-2])/([0-9]{2})");

        cvv = new TextField("CVV");
        cvv.setPlaceholder("123");
        cvv.setMaxLength(3);
        cvv.setPattern("[0-9]*");
        cvv.setAllowedCharPattern("[0-9]");

        // Add fields to form
        formLayout.add(cardNumber, cardHolder, expiryDate, cvv);

        // Bind fields
        binder.forField(cardNumber)
                .withValidator(new StringLengthValidator("Card number must be 16 digits", 16, 19))
                .bind(CreditCardDetails::getCardNumber, CreditCardDetails::setCardNumber);

        binder.forField(cardHolder)
                .withValidator(new StringLengthValidator("Card holder name is required", 1, null))
                .bind(CreditCardDetails::getCardHolder, CreditCardDetails::setCardHolder);

        binder.forField(expiryDate)
                .withValidator(new StringLengthValidator("Expiry date must be in MM/YY format", 5, 5))
                .bind(CreditCardDetails::getExpiryDate, CreditCardDetails::setExpiryDate);

        binder.forField(cvv)
                .withValidator(new StringLengthValidator("CVV must be 3 digits", 3, 3))
                .bind(CreditCardDetails::getCvv, CreditCardDetails::setCvv);

        // Set the bean to be bound
        binder.setBean(creditCardDetails);

        // Buttons
        Button cancelButton = new Button("Cancel", e -> close());
        Button checkoutButton = new Button("Complete Purchase", e -> {
            if (binder.validate().isOk()) {
                // Ensure the bean is updated with the latest values
                binder.writeBeanIfValid(creditCardDetails);
                boolean success = presenter.onCheckout(creditCardDetails);
                if (success) {
                    close();
                }
            } else {
                Notification.show("Please fill in all fields correctly", 3000, Notification.Position.MIDDLE);
            }
        });
        checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Add components to dialog
        add(formLayout);
        add(new HorizontalLayout(cancelButton, checkoutButton));
    }

    public static class CreditCardDetails {
        private String cardNumber;
        private String cardHolder;
        private String expiryDate;
        private String cvv;

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public String getCardHolder() { return cardHolder; }
        public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
    }
} 