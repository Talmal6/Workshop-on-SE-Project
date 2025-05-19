package com.SEGroup.UI.Views;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.AddDiscountPresenter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    private final Button confirmButton;
    private final TextField couponField;
    private final Checkbox useCouponCheckbox;
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

        // Configure Category ComboBox
        categoryComboBox.setItems("Entire Store", "Specific Category", "Specific Item");
        categoryComboBox.setWidth("300px");
        categoryComboBox.setPlaceholder("Select category");
        categoryComboBox.addValueChangeListener(event -> {
            boolean isEntireStore = "Entire Store".equals(event.getValue());
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
            boolean isEntireCategory = "Entire Category".equals(event.getValue());
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

        // Configure Coupon Field and Checkbox
        couponField.setWidth("300px");
        couponField.setPlaceholder("Enter coupon code");
        couponField.setEnabled(false);
        
        useCouponCheckbox.addValueChangeListener(event -> {
            couponField.setEnabled(event.getValue());
            if (!event.getValue()) {
                couponField.clear();
            }
        });

        // Create horizontal layout for coupon field and checkbox
        HorizontalLayout couponLayout = new HorizontalLayout(useCouponCheckbox, couponField);
        couponLayout.setAlignItems(Alignment.BASELINE);
        couponLayout.setSpacing(true);

        // Configure Confirm Button
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.setWidth("300px");
        confirmButton.addClickListener(event -> {
            // This will be handled by the presenter later
            System.out.println("Confirm button clicked");
            String category = categoryComboBox.getValue();
            if (category == null || category.isEmpty() || percentageField.getValue() == null || percentageField.getValue() <= 0 || percentageField.getValue() > 99) {
                categoryComboBox.setInvalid(true);
                return;
            }
            if (category.equals("Entire Store")) {
                presenter.addDiscountToStore(percentageField.getValue());
            } else {
                String item = itemComboBox.getValue();
                if (item == null || item.isEmpty()) {
                    itemComboBox.setInvalid(true);
                    return;
                }
                if (item.equals("Entire Category")) {
                    presenter.addDiscountToCategory(category, percentageField.getValue());
                } else {
                    int minAmount = minAmountField.getValue();
                    if (minAmount <= 0) {
                        minAmountField.setInvalid(true);
                        return;
                    }
                    presenter.addDiscountToProduct(item, percentageField.getValue(), minAmount);
                }
            }
        });

        // Add components to layout
        add(categoryComboBox, itemComboBox, minAmountField, percentageField, couponLayout, confirmButton);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
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