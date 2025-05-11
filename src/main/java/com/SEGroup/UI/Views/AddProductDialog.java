package com.SEGroup.UI.Views;

import com.SEGroup.Service.Result;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class AddProductDialog extends Dialog {

    private final TextField catalogId = new TextField("Catalog ID");
    private final TextField name = new TextField("Product Name");
    private final TextArea description = new TextArea("Description");
    private final NumberField price = new NumberField("Price ($)");
    private final IntegerField quantity = new IntegerField("Quantity");

    private final Binder<ProductDTO> binder = new Binder<>(ProductDTO.class);

    public AddProductDialog(String storeName) {
        setWidth("500px");

        // Dialog header
        H3 title = new H3("Add New Product");
        title.getStyle().set("margin-top", "0");

        // Configure fields
        // Catalog ID with validation
        catalogId.setHelperText("Enter a valid catalog ID (e.g., TECH-001, FASH-002, HOME-003)");
        catalogId.setRequired(true);
        catalogId.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                if (!value.matches("(TECH|FASH|HOME)-\\d{3}")) {
                    catalogId.setErrorMessage("Invalid format. Use TECH-001, FASH-002, HOME-003 format");
                    catalogId.setInvalid(true);
                } else {
                    catalogId.setInvalid(false);
                }
            }
        });

        // Other field configurations
        name.setRequired(true);

        description.setMinHeight("100px");

        price.setPrefixComponent(new Span("$"));
        price.setStep(0.01);
        price.setMin(0);
        price.setRequired(true);

        quantity.setMin(0);
        quantity.setStepButtonsVisible(true);
        quantity.setRequired(true);

        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(catalogId, name, description, price, quantity);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Button layout
        Button cancelButton = new Button("Cancel", e -> close());
        Button saveButton = new Button("Save", e -> save(storeName));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        // Setup additional validation with binder
        setupValidation();

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(title, formLayout, buttonLayout);
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }

    private void setupValidation() {
        // Catalog ID validation
        binder.forField(catalogId)
                .withValidator(new StringLengthValidator(
                        "Catalog ID is required", 1, null))
                .bind(ProductDTO::getCatalogId, ProductDTO::setCatalogId);

        // Name validation
        binder.forField(name)
                .withValidator(new StringLengthValidator(
                        "Product name must be at least 3 characters", 3, null))
                .bind(ProductDTO::getName, ProductDTO::setName);

        // Description validation
        binder.forField(description)
                .bind(ProductDTO::getDescription, ProductDTO::setDescription);

        // Price validation
        binder.forField(price)
                .withValidator(new DoubleRangeValidator(
                        "Price must be greater than 0", 0.01, null))
                .bind(ProductDTO::getPrice, ProductDTO::setPrice);

        // Quantity validation
        binder.forField(quantity)
                .withValidator(new IntegerRangeValidator(
                        "Quantity must be at least 1", 1, null))
                .bind(ProductDTO::getQuantity, ProductDTO::setQuantity);
    }

    private void save(String storeName) {
        try {
            // Validate form
            ProductDTO productDTO = new ProductDTO();
            binder.writeBean(productDTO);

            // Call service
            Result<String> result = ServiceLocator.getStoreService()
                    .addProductToStore(
                            SecurityContextHolder.token(),
                            storeName,
                            productDTO.getCatalogId(),
                            productDTO.getName(),
                            productDTO.getDescription(),
                            productDTO.getPrice(),
                            productDTO.getQuantity(),
                            productDTO.imageUrl)
                            ;

            if (result.isSuccess()) {
                showSuccess("Product added successfully: " + result.getData());
                close();
            } else {
                showError("Failed to add product: " + result.getErrorMessage());
            }
        } catch (ValidationException e) {
            showError("Please check the form fields: " + e.getMessage());
        } catch (Exception e) {
            showError("Error adding product: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static class ProductDTO {
        private String catalogId;
        private String name;
        private String description;
        private Double price;
        private Integer quantity;
        private String imageUrl;

        public String getCatalogId() { return catalogId; }
        public void setCatalogId(String catalogId) { this.catalogId = catalogId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getImageUrl(){
            return imageUrl;
        }
    }

}