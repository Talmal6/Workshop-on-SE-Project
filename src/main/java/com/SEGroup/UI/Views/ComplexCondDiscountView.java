package com.SEGroup.UI.Views;

import com.SEGroup.UI.Presenter.ComplexCondDiscountPresenter;
import com.SEGroup.UI.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "add-complex-Cond-discount", layout = MainLayout.class)
@PageTitle("Complex Cond Discount")
public class ComplexCondDiscountView extends VerticalLayout implements HasUrlParameter<String> {
    private List<String> cachedProductNames = List.of();
    private final Button andBtn    = new Button("And");
    private final Button orBtn     = new Button("Or");
    private final Button xorBtn    = new Button("Xor");
    private final Button addProdBtn= new Button("➕ Add Product");


    // we'll replace comboA/comboB with a dynamic list:
    private final List<ComboBox<String>>     productCombos = new ArrayList<>();
    private final List<IntegerField>         qtyFields     = new ArrayList<>();

    private final IntegerField    minPrice      = new IntegerField("Min Price");
    private final IntegerField   discountPct   = new IntegerField("Discount %");
    private final Checkbox       useCoupon     = new Checkbox("Use Coupon Code");
    private final TextField      couponField   = new TextField("Coupon Code");

    private final Button         confirmBtn    = new Button("Confirm Discount");
    private final ComboBox<String> select = new ComboBox<>("What to apply discount on:");
    public final ComboBox<String> categories = new ComboBox<>("Category");
    public final ComboBox<String> products = new ComboBox<>("Product");

    private final VerticalLayout productContainer = new VerticalLayout();

    private ComplexCondDiscountPresenter presenter;
    private String storeName;
    private String operator;

    public ComplexCondDiscountView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.storeName = parameter;
        this.presenter = new ComplexCondDiscountPresenter(this, storeName);
        buildLayout();
        presenter.loadProducts();
    }

    private void buildLayout() {
        HorizontalLayout ops = new HorizontalLayout();
        ops.setWidthFull();

        ops.add(new H3("Click on one of these: "), andBtn, orBtn, xorBtn);

        Div spacer = new Div();
        ops.add(spacer);

        ops.add(addProdBtn);

// now give the spacer all the remaining space:
        ops.expand(spacer);

        andBtn.addClickListener(e -> selectOp(andBtn, "And"));
        orBtn .addClickListener(e -> selectOp(orBtn,  "Or"));
        xorBtn.addClickListener(e -> selectOp(xorBtn, "Xor"));

        addProdBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addProdBtn.addClickListener(e -> addProductRow());

        add(ops);

        productContainer.setSpacing(false);
        productContainer.setPadding(false);
        // start with two rows:
        addProductRow();
        addProductRow();
        add(productContainer);

        // --- Min Price & Discount & Coupon ---
        minPrice.setRequiredIndicatorVisible(true);
        discountPct.setRequiredIndicatorVisible(true);
        useCoupon.addValueChangeListener(evt -> {
            couponField.setEnabled(evt.getValue());
            if (!evt.getValue()) couponField.clear();
        });
        couponField.setEnabled(false);

        select.setItems(List.of("Entire Store", "Entire Category", "Product"));
        categories.setVisible(false);
        products.setVisible(false);
        select.addValueChangeListener(event -> {
            if(select.getValue().equals("Entire Category")){
                categories.setVisible(true);
                products.setVisible(false);
            }
            else
            if(select.getValue().equals("Product")){
                categories.setVisible(false);
                products.setVisible(true);
            }
            else{
                categories.setVisible(false);
                products.setVisible(false);
            }
        });
        add(minPrice, select, categories, products, discountPct, useCoupon, couponField);


        // --- Confirm ---
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmBtn.addClickListener(e -> {
            if (operator == null || !allInputsValid()) {
                showError("Please select an operator and fill in all required fields.");
                return;
            }
            var combos = productContainer.getChildren()
                    .map(c -> (HorizontalLayout)c)
                    .map(row -> row.getComponentAt(0).toString())
                    .toList();
            var minAmounts = productContainer.getChildren()
                    .map(c -> (HorizontalLayout)c)
                    .map(row -> ((IntegerField)row.getComponentAt(1)).getValue())
                    .toList();
            if(select.getValue().equals("Entire Store")){
                presenter.apply_on_entire_store(operator, combos, minAmounts,
                        minPrice.getValue(), discountPct.getValue(), couponField.getValue());
            }
            else
                if(select.getValue().equals("Product")){
                    presenter.apply_on_product(operator, products.getValue(), combos, minAmounts,
                            minPrice.getValue(), discountPct.getValue(), couponField.getValue());
                }
                else{
                    presenter.apply_on_entire_category(operator, categories.getValue(), combos, minAmounts,
                            minPrice.getValue(), discountPct.getValue(), couponField.getValue());
                }
        });
        add(confirmBtn);
    }

    private void selectOp(Button btn, String op) {
        operator = op;
        for (Button b : List.of(andBtn, orBtn, xorBtn)) {
            b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        }
        btn.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private void addProductRow() {
        ComboBox<String> combo = new ComboBox<>("Product");
        combo.setRequiredIndicatorVisible(true);
        combo.setWidth("200px");
//        combo.setItemLabelGenerator(String::valueOf);
        combo.setItems(cachedProductNames);

        IntegerField qty = new IntegerField("Min Amount");
        qty.setRequiredIndicatorVisible(true);
        qty.setMin(1);

        // store in our lists:
        productCombos.add(combo);
        qtyFields.add(qty);

        productContainer.add(new HorizontalLayout(combo, qty));
    }

    private boolean allInputsValid() {
        if (operator == null) return false;
        boolean ok = productContainer.getChildren().allMatch(c -> {
            var row = (HorizontalLayout)c;
            ComboBox<?> cb = (ComboBox<?>)row.getComponentAt(0);
            IntegerField qf = (IntegerField)row.getComponentAt(1);
            return cb.getValue() != null && qf.getValue() != null;
        });
        return ok && minPrice.getValue() != null && discountPct.getValue() != null;
    }

    // called by presenter:
    public void setComboItems(List<String> items) {
        this.cachedProductNames = items;
        productCombos.forEach(c -> c.setItems(items));
    }

    public void showSuccess(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        navigateBack();
    }

    public void showError(String msg) {
        Notification n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void navigateBack() {
        UI.getCurrent().navigate("store/" + storeName);
    }

}

