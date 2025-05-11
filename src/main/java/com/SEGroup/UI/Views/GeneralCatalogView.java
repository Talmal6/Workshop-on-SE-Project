package com.SEGroup.UI.Views;

import com.SEGroup.DTO.CatalogProductDTO;
import com.SEGroup.DTO.StoreCardDto;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.GeneralCatalogPresenter;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "base-catalog", layout = MainLayout.class)
@PageTitle("Base Catalog")
public class GeneralCatalogView extends VerticalLayout implements BeforeEnterObserver {
    private final GeneralCatalogPresenter presenter;
    private final Grid<CatalogProductDTO> catalogGrid = new Grid<>(CatalogProductDTO.class, false);
    private final TextField filter = new TextField();
    private final ComboBox<String> storeSelector = new ComboBox<>("Select Your Store");
    private final ComboBox<String> categoryFilter = new ComboBox<>("Category");
    private final Map<String, CatalogProductDTO> catalogProductMap = new HashMap<>();
    private List<CatalogProductDTO> currentProducts;
    private final Button createStoreBtn = new Button("Create Store", VaadinIcon.PLUS_CIRCLE.create());
    private final Button manageStoresBtn = new Button("Manage Stores", VaadinIcon.COGS.create());

    public GeneralCatalogView() {
        this.presenter = new GeneralCatalogPresenter(this);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Top bar
        add(createNavigationBar());
        add(createHeader());

        // Admin buttons
        HorizontalLayout adminButtons = new HorizontalLayout(createStoreBtn, manageStoresBtn);
        createStoreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageStoresBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        createStoreBtn.setVisible(false);
        manageStoresBtn.setVisible(false);
        createStoreBtn.addClickListener(e -> UI.getCurrent().navigate("all-stores"));
        manageStoresBtn.addClickListener(e -> UI.getCurrent().navigate("all-stores"));
        add(adminButtons);

        // Filters
        add(createFilterBar());

        // Grid
        configureCatalogGrid();
        add(catalogGrid);

        // Load data & permissions
        presenter.loadBaseCatalog();
        updateAdminButtons();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!isAuthorized()) {
            event.rerouteTo(AccessDeniedView.class);
        }
    }

    private boolean isAuthorized() {
        if (SecurityContextHolder.isAdmin()) return true;
        if (!SecurityContextHolder.isLoggedIn()) return false;
        String email = SecurityContextHolder.email();
        try {
            if (ServiceLocator.getUserService().rolesOf(email).contains(Role.STORE_OWNER)) {
                return true;
            }
        } catch (Exception ignored) {}
        try {
            return !ServiceLocator.getStoreService().listStoresOwnedBy(email).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private Component createNavigationBar() {
        Button back = new Button("Back to Marketplace", VaadinIcon.ARROW_LEFT.create());
        back.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        back.addClickListener(e -> UI.getCurrent().navigate("catalog"));
        HorizontalLayout bar = new HorizontalLayout(back);
        bar.setPadding(false);
        bar.setMargin(false);
        return bar;
    }

    private Component createHeader() {
        H2 title = new H2("Base Product Catalog");
        Paragraph desc = new Paragraph(
                "Browse our extensive base catalog and add products to your store. " +
                        "Select your store from the dropdown, then click 'Add to Store'."
        );
        VerticalLayout layout = new VerticalLayout(title, desc);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createFilterBar() {
        filter.setPlaceholder("Search catalog…");
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> filterCatalog(e.getValue()));
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setWidth("300px");

        categoryFilter.setPlaceholder("All Categories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> filterByCategory(e.getValue()));

        storeSelector.setPlaceholder("Select your store");
        storeSelector.setEnabled(SecurityContextHolder.isLoggedIn());
        storeSelector.setWidth("250px");
        if (!SecurityContextHolder.isLoggedIn()) {
            storeSelector.setHelperText("Login to add products");
        }

        Button clear = new Button("Clear Filters", VaadinIcon.CLOSE.create());
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        clear.addClickListener(e -> {
            filter.clear();
            categoryFilter.clear();
            catalogGrid.setItems(currentProducts);
            showInfo("Filters cleared");
        });

        HorizontalLayout bar = new HorizontalLayout(filter, categoryFilter, storeSelector, clear);
        bar.setAlignItems(FlexComponent.Alignment.BASELINE);
        bar.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "10px")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");
        return bar;
    }

    private void configureCatalogGrid() {
        catalogGrid.addColumn(CatalogProductDTO::getCatalogId)
                .setHeader("Catalog ID")
                .setSortable(true)
                .setAutoWidth(true);

        catalogGrid.addColumn(CatalogProductDTO::getName)
                .setHeader("Product Name")
                .setSortable(true)
                .setFlexGrow(1)
                .setAutoWidth(true);

        catalogGrid.addColumn(dto -> {
                    List<String> cats = dto.getCategories();
                    return (cats == null || cats.isEmpty())
                            ? "No category"
                            : String.join(", ", cats);
                })
                .setHeader("Categories")
                .setAutoWidth(true);

        catalogGrid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Actions")
                .setFlexGrow(0)
                .setAutoWidth(true);

        catalogGrid.setSelectionMode(Grid.SelectionMode.NONE);
        catalogGrid.setSizeFull();
        catalogGrid.addItemDoubleClickListener(e -> showProductDetailsDialog(e.getItem()));
    }

    private Component createActionButtons(CatalogProductDTO dto) {
        Button add = new Button("Add to Store", new Icon(VaadinIcon.PLUS));
        add.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        add.setEnabled(storeSelector.getValue() != null && SecurityContextHolder.isLoggedIn());
        add.addClickListener(e -> {
            if (storeSelector.getValue() == null) {
                showError("Please select a store first");
            } else {
                showAddToStoreDialog(dto, storeSelector.getValue());
            }
        });

        Button details = new Button("Details", new Icon(VaadinIcon.INFO_CIRCLE));
        details.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        details.addClickListener(e -> showProductDetailsDialog(dto));

        return new HorizontalLayout(details, add);
    }

    private void showAddToStoreDialog(CatalogProductDTO dto, String store) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Add Product to " + store);

        TextField nameF = new TextField("Product Name");
        nameF.setValue(dto.getName());
        nameF.setWidthFull();
        nameF.setRequiredIndicatorVisible(true);


        TextField nameD = new TextField("Description");
        nameF.setValue(dto.getName());
        nameF.setWidthFull();
        nameF.setRequiredIndicatorVisible(true);

        NumberField priceF = new NumberField("Price");
        priceF.setMin(0.01);
        priceF.setStep(0.01);
        priceF.setValue(0.0);
        priceF.setWidthFull();
        priceF.setRequiredIndicatorVisible(true);

        IntegerField qtyF = new IntegerField("Quantity");
        qtyF.setMin(1);
        qtyF.setValue(1);
        qtyF.setWidthFull();
        qtyF.setRequiredIndicatorVisible(true);

        TextField imgUrl = new TextField("ImageURL");


        d.add(new VerticalLayout(nameF, nameD,priceF, qtyF));

        Button cancel = new Button("Cancel", e -> d.close());
        Button confirm = new Button("Add to Store", e -> {
            if (nameF.isEmpty() || priceF.isEmpty() || qtyF.isEmpty() || nameD.isEmpty()) {
                showError("Please fill all required fields");
                return;
            }
            presenter.addToMyStore(
                    dto.getCatalogId(),
                    store,
                    nameF.getValue(),
                    nameD.getValue(),
                    priceF.getValue(),
                    qtyF.getValue(),
                    imgUrl.getValue()

            );
            d.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        d.getFooter().add(cancel, confirm);
        d.open();
    }

    private void filterCatalog(String text) {
        if (currentProducts == null) return;
        if (text == null || text.isBlank()) {
            catalogGrid.setItems(currentProducts);
            return;
        }
        String t = text.toLowerCase().trim();
        List<CatalogProductDTO> filtered = currentProducts.stream()
                .filter(p ->
                        p.getName().toLowerCase().contains(t) ||
                                p.getCatalogId().toLowerCase().contains(t) ||
                                p.getCategories().stream().anyMatch(c -> c.toLowerCase().contains(t))
                )
                .collect(Collectors.toList());

        catalogGrid.setItems(filtered);
        if (filtered.isEmpty()) showInfo("No products match '" + text + "'");
    }

    private void filterByCategory(String cat) {
        if (currentProducts == null) return;
        if (cat == null || cat.isBlank()) {
            catalogGrid.setItems(currentProducts);
            return;
        }
        List<CatalogProductDTO> filtered = currentProducts.stream()
                .filter(p -> p.getCategories().contains(cat))
                .collect(Collectors.toList());

        catalogGrid.setItems(filtered);
        if (filtered.isEmpty()) showInfo("No products in category '" + cat + "'");
    }

    public void displayBaseCatalog(List<CatalogProductDTO> products) {
        this.currentProducts = products;
        catalogGrid.setItems(products);
        catalogProductMap.clear();
        for (var p : products) {
            catalogProductMap.put(p.getCatalogId(), p);
        }
        Set<String> cats = products.stream()
                .flatMap(p -> p.getCategories().stream())
                .collect(Collectors.toSet());
        List<String> sorted = new ArrayList<>(cats);
        Collections.sort(sorted);
        categoryFilter.setItems(sorted);

        showInfo("Loaded " + products.size() + " catalog products");
    }

    private void updateAdminButtons() {
        boolean isAdmin = SecurityContextHolder.isAdmin();
        boolean isOwner = SecurityContextHolder.isLoggedIn()
                && ServiceLocator.getUserService()
                .rolesOf(SecurityContextHolder.email())
                .contains(Role.STORE_OWNER);
        boolean show = isAdmin || isOwner;
        createStoreBtn.setVisible(show);
        manageStoresBtn.setVisible(show);
    }

    private void showProductDetailsDialog(CatalogProductDTO p) {
        Dialog d = new Dialog();
        d.setHeaderTitle(p.getName());
        d.setWidth("400px");
        Span idSpan = new Span("Catalog ID: " + p.getCatalogId());
        Span catsSpan = new Span("Categories: " + String.join(", ", p.getCategories()));
        d.add(new VerticalLayout(idSpan, catsSpan));
        Button close = new Button("Close", e -> d.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        d.getFooter().add(close);
        d.open();
    }

    public void populateStoreSelector(List<StoreCardDto> stores) {
        List<String> names = stores.stream()
                .map(StoreCardDto::name)
                .collect(Collectors.toList());
        storeSelector.setItems(names);
        if (names.size() == 1) {
            storeSelector.setValue(names.get(0));
        }
    }

    public CatalogProductDTO getSelectedCatalogProduct(String catalogId) {
        return catalogProductMap.get(catalogId);
    }

    public void showSuccess(String msg) {
        var n = Notification.show(msg, 3000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String msg) {
        var n = Notification.show(msg, 4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void showInfo(String msg) {
        var n = Notification.show(msg, 3000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }
}
