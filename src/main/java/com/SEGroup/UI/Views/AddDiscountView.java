package com.SEGroup.UI.Views;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.AddDiscountPresenter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

@Route(value = "add-discount", layout = MainLayout.class)
@PageTitle("Add Discount")
public class AddDiscountView extends VerticalLayout implements HasUrlParameter<String> {

    private final ComboBox<String> categoryComboBox;
    private final ComboBox<String> itemComboBox;
    private final IntegerField minAmountField;
    private final IntegerField percentageField;
    private final TextField couponField;
    private final Checkbox useCouponCheckbox;
    private final Checkbox isConditionalDiscount;
    private final TextField minimumPrice;
    private final Button confirmButton;
    private AddDiscountPresenter presenter;

    public AddDiscountView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Add header
        H2 title = new H2("Add New Discount");
        Paragraph description = new Paragraph("Configure a new discount for your store");
        add(title, description);

        // Initialize all components first
        categoryComboBox = new ComboBox<>("Discount Category");
        itemComboBox = new ComboBox<>("Item");
        minAmountField = new IntegerField("Minimum Amount");
        percentageField = new IntegerField("Discount Percentage");
        confirmButton = new Button("Confirm Discount");
        couponField = new TextField("Coupon Code");
        useCouponCheckbox = new Checkbox("Use Coupon Code");
        isConditionalDiscount = new Checkbox("Conditional Discount");
        minimumPrice = new TextField("Minimum Price");


        // Configure Category ComboBox
        categoryComboBox.setItems("Entire Store", "Specific Category", "Specific Item");
        categoryComboBox.setWidth("300px");
        categoryComboBox.setPlaceholder("Select category");
        //disable itemComboBox and minAmountField until a category is selected
        itemComboBox.setEnabled(false);
        minAmountField.setEnabled(false);
        categoryComboBox.addValueChangeListener(event -> {
            boolean isEntireStore = event.getValue() == null || "Entire Store".equals(event.getValue());
            itemComboBox.setEnabled(!isEntireStore);
            minAmountField.setEnabled(!isEntireStore);

            if (isEntireStore) {
                itemComboBox.setValue(null);
                minAmountField.setValue(null);
            }
            else {
                List<String> items =
                        new java.util.ArrayList<>(presenter.getProductsByCategory().get(event.getValue()).stream()
                                .map(ShoppingProductDTO::getName)
                                .toList());
                items.add(0, "Entire Category");
                itemComboBox.setItems(items);
                itemComboBox.setPlaceholder("Select item");
            }
        });

        // Configure Item ComboBox
        itemComboBox.setItems("Entire Category", "Specific Item");
        itemComboBox.setWidth("300px");
        itemComboBox.setPlaceholder("Select item");
        itemComboBox.addValueChangeListener(event -> {
            boolean isEntireCategory = event.getValue() == null || "Entire Category".equals(event.getValue());
            minAmountField.setEnabled(!isEntireCategory);

            if (isEntireCategory) {
                minAmountField.setValue(null);
            }
        });

        // Configure Minimum Amount Field
        minAmountField.setWidth("300px");
        minAmountField.setPlaceholder("Enter minimum amount");
        minAmountField.setValue(1);
        minAmountField.setMin(0);

        // Configure Percentage Field
        percentageField.setWidth("300px");
        percentageField.setPlaceholder("Enter discount percentage");
        percentageField.setMin(1);
        percentageField.setMax(99);
        percentageField.setSuffixComponent(new Span("%"));

        minimumPrice.setWidth("300px");
        minimumPrice.setPlaceholder("Enter Minimum Price");
        minimumPrice.setEnabled(false);

        // Configure Coupon Field and Checkbox
        couponField.setWidth("300px");
        couponField.setPlaceholder("Enter coupon code");
        couponField.setEnabled(false);

        isConditionalDiscount.addValueChangeListener(event -> {
            minimumPrice.setEnabled(event.getValue());
            if (!event.getValue()) {
                minimumPrice.clear();
            }
        });

        useCouponCheckbox.addValueChangeListener(event -> {
            couponField.setEnabled(event.getValue());
            if (!event.getValue()) {
                couponField.clear();
            }
        });

        // Create horizontal layout for coupon field and checkbox
        VerticalLayout couponLayout = new VerticalLayout(isConditionalDiscount, minimumPrice, useCouponCheckbox, couponField);
        couponLayout.setAlignItems(Alignment.BASELINE);
        couponLayout.setSpacing(true);


        // Configure Confirm Button
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.setWidth("300px");
        confirmButton.addClickListener(click -> {

            String category = categoryComboBox.getValue();
            Integer percent  = percentageField.getValue();
            boolean couponOn = Boolean.TRUE.equals(useCouponCheckbox.getValue());
            boolean conditionalOn = Boolean.TRUE.equals(isConditionalDiscount.getValue());
            Integer minimumP = 0;
            if (conditionalOn) {
                try {
                    minimumP = Integer.parseInt(minimumPrice.getValue());
                } catch (NumberFormatException e) {
                    minimumPrice.setInvalid(true);
                    return;
                }
            }
            String  coupon   = couponField.getValue();
            Result<Void> result = Result.failure("Unknown error");

            if (empty(category))                     { categoryComboBox.setInvalid(true); return; }
            if (percent == null || percent <= 0 || percent > 99) { percentageField.setInvalid(true); return; }
            if (couponOn && empty(coupon))           { couponField.setInvalid(true); return; }

            switch (category) {
                case "Entire Store" -> {
                    if (couponOn) {
                        if(conditionalOn){
                            result = presenter.addConditionalDiscountToStoreWithCoupon(percent, coupon,minimumP);
                        }
                        else{
                            result = presenter.addDiscountToStoreWithCoupon(percent, coupon);
                        }
                    }
                    else
                    {
                        if(conditionalOn){
                            result = presenter.addConditionalDiscountToStore(percent,minimumP);
                        }
                        else {
                            result = presenter.addDiscountToStore(percent);
                        }
                    }

                }

                default -> {                                     // קטגוריה מסוימת
                    String item = itemComboBox.getValue();
                    if (empty(item)) { itemComboBox.setInvalid(true); return; }

                    if (item.equals("Entire Category")) {
                        if (couponOn){
                            if(conditionalOn){
                                result = presenter.addConditionalDiscountToCategoryWithCoupon(category, percent, coupon, minimumP);
                            }
                            else{
                                result = presenter.addDiscountToCategoryWithCoupon(category, percent, coupon);
                            }
                        }
                        else {
                            if(conditionalOn){
                                result = presenter.addConditionalDiscountToCategory(category,percent,minimumP);
                            }
                            else {
                                result = presenter.addDiscountToCategory(category, percent);
                            }
                        }
                    } else {                                     // פריט בודד
                        Integer min = minAmountField.getValue();
                        if (min == null || min <= 0) {
                            minAmountField.setInvalid(true);
                            return;
                        }
                        if (couponOn)
                        {
                            if(conditionalOn){
                                result = presenter.addConditionalDiscountToProductWithCoupon(category, item, percent, min, coupon, minimumP);
                            }
                            else {
                                result = presenter.addDiscountToProductWithCoupon(category, item, percent, min, coupon);
                            }
                        }
                        else {
                            if(conditionalOn){
                                result = presenter.addConditionalDiscountToProduct(category, item, percent, min, minimumP);
                            }
                            else {
                                result = presenter.addDiscountToProduct(category, item, percent, min);
                            }
                        }
                    }
                }
            }
            if (result.isSuccess()) {
                Notification.show("Discount added successfully!", 3000, Notification.Position.MIDDLE);
                //reset fields
                categoryComboBox.clear();
                itemComboBox.clear();
                minAmountField.clear();
                percentageField.clear();
                couponField.clear();
                useCouponCheckbox.setValue(false);
                isConditionalDiscount.setValue(false);
                minimumPrice.setEnabled(false);
            } else {
                Notification.show("Failed to add discount: " + result.getErrorMessage(), 3000, Notification.Position.MIDDLE);
            }

        });
        // Add components to layout
        add(categoryComboBox, itemComboBox, minAmountField, percentageField, couponLayout, confirmButton);

    }

    private static boolean empty(String s) { return s == null || s.isBlank(); }


    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            presenter = new AddDiscountPresenter(this, parameter);
        }
    }

    public void updateCategories(List<String> categories) {
        categoryComboBox.setItems(categories);
        categoryComboBox.setPlaceholder("Select category");
    }

    public void updateProducts(Map<String, List<ShoppingProductDTO>> products) {
        // This will be implemented when we need to show specific products
    }
} 