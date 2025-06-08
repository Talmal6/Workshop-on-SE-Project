package com.SEGroup.UI.Views;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.CartPresenter;
import com.SEGroup.UI.Presenter.ProfilePresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.SEGroup.UI.Views.CheckoutDialog.CreditCardDetails;

public class PaymentDetailsDialog extends Dialog {
    private final ProfilePresenter presenter;
    private final MainLayout mainLayout;
    private final Binder<CreditCardDetails> binder;
    private final CreditCardDetails creditCardDetails;
    private final TextField cardNumber;
    private final TextField cardHolder;
    private final TextField expiryDate;
    private final TextField cvv;
    private final TextField address;
    private final TextField city;
    private final TextField zipCode;
    private final TextField country;
    private final TextField id;
    private final Checkbox useAddressOnFile;

    public PaymentDetailsDialog(ProfilePresenter presenter) {
        this.presenter = presenter;
        this.mainLayout = MainLayout.getInstance();
        this.creditCardDetails = new CreditCardDetails();
        this.binder = new Binder<>(CreditCardDetails.class);

        setWidth("500px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        // Title
        H3 title = new H3("Update Payment Details");
        title.getStyle().set("margin-top", "0");

        // Total amount display
        // Update total amount

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Payment information
        H3 paymentTitle = new H3("Payment Information");
        paymentTitle.getStyle().set("margin-top", "1em").set("margin-bottom", "0.5em");
        paymentTitle.setWidthFull();

        // Credit card fields
        cardNumber = new TextField("Card Number");
        cardNumber.setPlaceholder("1234 5678 9012 3456");
        cardNumber.setMaxLength(19); // 16 digits + 3 spaces
        cardNumber.setPattern("[0-9 ]*");
        cardNumber.setAllowedCharPattern("[0-9 ]");
        cardNumber.setRequired(true);

        cardHolder = new TextField("Card Holder Name");
        cardHolder.setPlaceholder("John Doe");
        cardHolder.setRequired(true);

        expiryDate = new TextField("Expiry Date");
        expiryDate.setPlaceholder("MM/YY");
        expiryDate.setMaxLength(5);
        expiryDate.setPattern("(0[1-9]|1[0-2])/([0-9]{2})");
        expiryDate.setRequired(true);

        cvv = new TextField("CVV");
        cvv.setPlaceholder("123");
        cvv.setMaxLength(3);
        cvv.setPattern("[0-9]*");
        cvv.setAllowedCharPattern("[0-9]");
        cvv.setRequired(true);

        id = new TextField("Buyer Id");
        id.setPlaceholder("123456789");
        id.setMaxLength(9);
        id.setPattern("[0-9]*");
        id.setAllowedCharPattern("[0-9]");
        id.setRequired(true);

        // Add checkbox for address on file
        useAddressOnFile = new Checkbox("Use address on file");
        useAddressOnFile.setValue(true); // Default to checked

        // Shipping information
        H3 shippingTitle = new H3("Shipping Information");
        shippingTitle.getStyle().set("margin-top", "1em").set("margin-bottom", "0.5em");
        shippingTitle.setWidthFull();

        address = new TextField("Shipping Address");
        address.setPlaceholder("123 Main St");
        address.setRequired(true);

        city = new TextField("City");
        city.setPlaceholder("New York");
        city.setRequired(true);

        zipCode = new TextField("Zip/Postal Code");
        zipCode.setPlaceholder("10001");
        zipCode.setRequired(true);

        country = new TextField("Country");
        country.setPlaceholder("United States");
        country.setRequired(true);

        // Make shipping fields invisible by default
        address.setVisible(false);
        city.setVisible(false);
        zipCode.setVisible(false);
        country.setVisible(false);
        shippingTitle.setVisible(false);

        // Add checkbox listener
        useAddressOnFile.addValueChangeListener(event -> {
            boolean useAddress = event.getValue();
            address.setVisible(!useAddress);
            city.setVisible(!useAddress);
            zipCode.setVisible(!useAddress);
            country.setVisible(!useAddress);
            shippingTitle.setVisible(!useAddress);

            // Update required state
            address.setRequired(!useAddress);
            city.setRequired(!useAddress);
            zipCode.setRequired(!useAddress);
            country.setRequired(!useAddress);
            updateBinder(useAddress);
        });
        if (presenter.doesAddressOnFileExist()) {
            useAddressOnFile.setValue(true);
            address.setVisible(false);
            city.setVisible(false);
            zipCode.setVisible(false);
            country.setVisible(false);
            shippingTitle.setVisible(false);
        } else {
            useAddressOnFile.setValue(false);
            address.setVisible(true);
            city.setVisible(true);
            zipCode.setVisible(true);
            country.setVisible(true);
            shippingTitle.setVisible(true);
        }

        //if payment details are already available, populate the fields
        CreditCardDetails existingDetails = presenter.getPaymentDetails();
        if (existingDetails != null) {
            creditCardDetails.setCardNumber(existingDetails.getCardNumber());
            creditCardDetails.setCardHolder(existingDetails.getCardHolder());
            creditCardDetails.setExpiryDate(existingDetails.getExpiryDate());
            creditCardDetails.setCvv(existingDetails.getCvv());
            creditCardDetails.setAddress(existingDetails.getAddress());
            creditCardDetails.setCity(existingDetails.getCity());
            creditCardDetails.setZipCode(existingDetails.getZipCode());
            creditCardDetails.setCountry(existingDetails.getCountry());
            creditCardDetails.setId(existingDetails.getId());

            // Update the binder with existing details
            updateBinder(useAddressOnFile.getValue());
        }

        // Add fields to form

        formLayout.add(paymentTitle, 2);
        formLayout.add(cardNumber, 2);
        formLayout.add(cardHolder, 2);
        formLayout.add(expiryDate, cvv);
        formLayout.add(id);
        formLayout.add(useAddressOnFile, 2);
        formLayout.add(shippingTitle, 2);
        formLayout.add(address, 2);
        formLayout.add(city, country);
        formLayout.add(zipCode);

        // Buttons
        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button savePaymentDetails = new Button("Save Payment Details", e -> {
            BinderValidationStatus<CreditCardDetails> status = binder.validate();
            if (useAddressOnFile.getValue()) {
                if (status.isOk()) {
                    // Ensure the bean is updated with the latest values
                    binder.writeBeanIfValid(creditCardDetails);
                    boolean success = true;//presenter.onCheckout(creditCardDetails);
                    if (success) {
                        close();//        CheckoutDialog dialog = new CheckoutDialog(this);
//        dialog.open();

                    }
                }
                else {
                    //get the validation issue
                    StringBuilder errorMessage = new StringBuilder("Please fill in all fields correctly:\n");
                    status.getFieldValidationErrors().forEach(error -> {
                        errorMessage.append(error.getField()).append(": ").append(error.getMessage()).append("\n");
                    });
                    Notification notification = Notification.show(errorMessage.toString(), 3000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                // Validate both payment and address fields
                if (binder.validate().isOk()) {
                    // Ensure the bean is updated with the latest values
                    binder.writeBeanIfValid(creditCardDetails);

                    // Validate that address fields are not empty
                    if (address.getValue() == null || address.getValue().trim().isEmpty() ||
                            city.getValue() == null || city.getValue().trim().isEmpty() ||
                            zipCode.getValue() == null || zipCode.getValue().trim().isEmpty() ||
                            country.getValue() == null || country.getValue().trim().isEmpty()) {

                        Notification notification = Notification.show("Please fill in all shipping address fields", 3000, Notification.Position.MIDDLE);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    AddressDTO addressDTO = new AddressDTO(
                            address.getValue(),
                            city.getValue(),
                            country.getValue(),
                            zipCode.getValue()
                    );
                    Result<Void> result = presenter.updatePaymentMethod(creditCardDetails, addressDTO);
                    if (result.isSuccess()) {
                        //show a success notification
                        showSuccess("Payment details updated successfully!");
                        close();
                    }
                    else {
                        showError("Failed to update payment details. error: " + result.getErrorMessage() + "\nPlease try again.");
                    }
                } else {
                    Notification notification = Notification.show("Please fill in all fields correctly", 3000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });
        savePaymentDetails.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Layout for buttons
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, savePaymentDetails);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Add components to dialog
        VerticalLayout mainLayout = new VerticalLayout(title, formLayout, buttonLayout);
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        add(mainLayout);
        useAddressOnFile.setVisible(presenter.doesAddressOnFileExist());
        useAddressOnFile.setValue(presenter.doesAddressOnFileExist());
        updateBinder(useAddressOnFile.getValue());
    }


    /**
     * Shows a success notification.
     *
     * @param message The success message to display
     */
    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification.
     *
     * @param message The error message to display
     */
    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void updateBinder(boolean isAddressOnFile) {
        // Update required state
        // Bind fields
        binder.removeBean();
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


        binder.forField(id)
                .withValidator(new StringLengthValidator("Id is required", 9, 9))
                .bind(CreditCardDetails::getId, CreditCardDetails::setId);
        // Set the bean to be bound
        binder.setBean(creditCardDetails);

        if (!isAddressOnFile) {
            binder.forField(address)
                    .withValidator(new StringLengthValidator("Address is required", 1, null))
                    .bind(CreditCardDetails::getAddress, CreditCardDetails::setAddress);

            binder.forField(city)
                    .withValidator(new StringLengthValidator("City is required", 1, null))
                    .bind(CreditCardDetails::getCity, CreditCardDetails::setCity);

            binder.forField(zipCode)
                    .withValidator(new StringLengthValidator("Zip/Postal code is required", 1, null))
                    .bind(CreditCardDetails::getZipCode, CreditCardDetails::setZipCode);

            binder.forField(country)
                    .withValidator(new StringLengthValidator("Country is required", 1, null))
                    .bind(CreditCardDetails::getCountry, CreditCardDetails::setCountry);

        }
        binder.setBean(creditCardDetails);
    }
}