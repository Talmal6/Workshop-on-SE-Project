// ui/Views/CatalogView.java (Enhanced)
package com.SEGroup.UI.Views;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.Presenter.CatalogPresenter;
import com.SEGroup.UI.SecurityContextHolder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "catalog", layout = MainLayout.class)
@PageTitle("Marketplace")
public class CatalogView extends VerticalLayout implements HasUrlParameter<String> {
    private final CatalogPresenter presenter;
    private final Grid<ShoppingProductDTO> grid = new Grid<>(ShoppingProductDTO.class, false);
    private final TextField searchField = new TextField();
    private final List<String> activeFilters = new ArrayList<>();
    private final Div filtersContainer = new Div();
    private List<ShoppingProductDTO> currentProducts = new ArrayList<>();

    // Advanced filters
    private final ComboBox<String> storeFilter = new ComboBox<>("Store");
    private final CheckboxGroup<String> categoryFilter = new CheckboxGroup<>("Categories");
    private final NumberField minPriceFilter = new NumberField("Min Price");
    private final NumberField maxPriceFilter = new NumberField("Max Price");
    private final ComboBox<String> ratingFilter = new ComboBox<>("Min Rating");
    private Dialog advancedFilterDialog;

    public CatalogView() {
        this.presenter = new CatalogPresenter(this);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());

        HorizontalLayout searchAndFilters = new HorizontalLayout();
        searchAndFilters.setWidthFull();
        searchAndFilters.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Build search component
        HorizontalLayout searchLayout = createSearchLayout();
        searchAndFilters.add(searchLayout);

        // Build filter buttons
        HorizontalLayout filterButtons = createFilterButtons();
        searchAndFilters.add(filterButtons);

        add(searchAndFilters);

        // Active filters display
        filtersContainer.addClassName("active-filters");
        filtersContainer.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "8px")
                .set("margin-bottom", "16px");
        add(filtersContainer);

        // Configure and add product grid
        configureGrid();
        add(grid);

        // Create sort bar
        add(createSortBar());

        // Set up advanced filters dialog
        createAdvancedFilterDialog();

        // Load initial products
        presenter.loadProducts();
    }

    private Component createHeader() {
        H2 title = new H2("Marketplace");
        Paragraph description = new Paragraph(
                "Browse products from all stores. Use the search and filters to find exactly what you need.");

        VerticalLayout header = new VerticalLayout(title, description);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private HorizontalLayout createSearchLayout() {
        searchField.setPlaceholder("Search products...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("400px");
        searchField.addValueChangeListener(e -> applySearch());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);

        Button searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> applySearch());

        HorizontalLayout layout = new HorizontalLayout(searchField, searchButton);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        return layout;
    }

    private HorizontalLayout createFilterButtons() {
        Button filterByCategoryBtn = new Button("Category", VaadinIcon.FILTER.create());
        filterByCategoryBtn.addClickListener(e -> addCategoryFilter());

        Button filterByPriceBtn = new Button("Price", VaadinIcon.DOLLAR.create());
        filterByPriceBtn.addClickListener(e -> addPriceFilter());

        Button filterByStoreBtn = new Button("Store", VaadinIcon.STORAGE.create());
        filterByStoreBtn.addClickListener(e -> addStoreFilter());

        Button advancedFilterBtn = new Button("Advanced Filters", VaadinIcon.OPTIONS.create());
        advancedFilterBtn.addClickListener(e -> advancedFilterDialog.open());

        // Clear all filters button
        Button clearAllFiltersBtn = new Button("Clear All Filters", VaadinIcon.CLOSE.create());
        clearAllFiltersBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_CONTRAST);
        clearAllFiltersBtn.addClickListener(e -> {
            activeFilters.clear();
            updateFiltersDisplay();
            applySearch();
        });

        HorizontalLayout layout = new HorizontalLayout(
                filterByCategoryBtn, filterByPriceBtn, filterByStoreBtn, advancedFilterBtn, clearAllFiltersBtn);
        layout.setSpacing(true);
        return layout;
    }

    private void configureGrid() {
        // Configure grid columns
        grid.addColumn(ShoppingProductDTO::getName)
                .setHeader("Product")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createCategoryChips)
                .setHeader("Categories")
                .setAutoWidth(true);
        grid.addColumn(ShoppingProductDTO::getStoreName)
                .setHeader("Store")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(product -> String.format("$%.2f", product.getPrice()))
                .setHeader("Price")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(product -> {
                    // Format to one decimal place
                    return String.format("%.1f ★", product.getAvgRating());
                })
                .setHeader("Rating")
                .setSortable(true)
                .setAutoWidth(true);

        // Image column using renderer
        grid.addColumn(new ComponentRenderer<>(product -> {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Image image = new Image(product.getImageUrl(), product.getName());
                image.setHeight("60px");
                image.setWidth("60px");
                image.getStyle().set("object-fit", "contain");
                return image;
            } else {
                Div placeholder = new Div();
                placeholder.setText("No image");
                placeholder.getStyle()
                        .set("width", "60px")
                        .set("height", "60px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background-color", "#f5f5f5")
                        .set("color", "#666")
                        .set("font-size", "10px");
                return placeholder;
            }
        })).setHeader("Image").setWidth("80px").setFlexGrow(0);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(this::createProductActions))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Click to view product details
        grid.addItemClickListener(event -> {
            String columnKey = event.getColumn() != null ? event.getColumn().getKey() : null;
            if (columnKey == null || !"actions".equals(columnKey)) {
                presenter.viewProductDetails(event.getItem());
            }
        });

        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setHeight("600px");
    }

    private Component createCategoryChips(ShoppingProductDTO product) {
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            Span noCategory = new Span("No category");
            noCategory.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic");
            return noCategory;
        }

        // Use HorizontalLayout instead of FlexLayout
        HorizontalLayout chipLayout = new HorizontalLayout();
        chipLayout.setSpacing(false);
        chipLayout.setPadding(false);

        // Set flex-wrap style directly
        chipLayout.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "4px")
                .set("max-width", "200px");

        // Show only first 3 categories to avoid overcrowding
        List<String> categoriesToShow = new ArrayList<>();
        if (product.getCategories().size() > 0) {
            int limit = Math.min(3, product.getCategories().size());
            categoriesToShow = new ArrayList<>(product.getCategories().subList(0, limit));
        }

        for (String category : categoriesToShow) {
            Span chip = new Span(category);
            chip.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("border-radius", "16px")
                    .set("padding", "3px 8px")
                    .set("font-size", "var(--lumo-font-size-xs)");

            // Make chips clickable to filter by that category
            final String categoryValue = category; // Need to make final for lambda
            chip.getElement().addEventListener("click", e -> {
                // Instead of stopPropagation, prevent the default behavior
                e.getEventData().getString("event.preventDefault()");
                // Use JavaScript to stop propagation
                e.getSource().executeJs("event.stopPropagation()");
                // Apply the filter
                categoryFilter.setValue(Collections.singleton(categoryValue));
                applySearch();
            });
            chip.getStyle().set("cursor", "pointer");

            chipLayout.add(chip);
        }

        // If more categories exist, show a "+X more" chip
        if (product.getCategories().size() > 3) {
            Span moreChip = new Span("+" + (product.getCategories().size() - 3) + " more");
            moreChip.getStyle()
                    .set("background-color", "var(--lumo-contrast-10pct)")
                    .set("border-radius", "16px")
                    .set("padding", "3px 8px")
                    .set("font-size", "var(--lumo-font-size-xs)");
            chipLayout.add(moreChip);
        }

        return chipLayout;
    }

    // Then modify the configureGrid method in CatalogView.java to add this category column:


    public void displayProducts(List<ShoppingProductDTO> products) {
        this.currentProducts = products;

        // Ensure all products have a categories list to prevent null pointer exceptions
        for (ShoppingProductDTO product : products) {
            if (product.getCategories() == null) {
                product.setCategories(new ArrayList<>());
            }
        }

        grid.setItems(products);

        // Update filter options based on the products
        updateFilterOptions(products);
    }


    private Component createProductActions(ShoppingProductDTO product) {
        Button viewButton = new Button(new Icon(VaadinIcon.EYE));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.getElement().setAttribute("title", "View Details");
        viewButton.addClickListener(e -> presenter.viewProductDetails(product));

        Button addToCartButton = new Button(new Icon(VaadinIcon.CART));
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        addToCartButton.getElement().setAttribute("title", "Add to Cart");
        addToCartButton.addClickListener(e ->
                presenter.addToCart(product.getProductId(), product.getStoreName()));

        HorizontalLayout actions = new HorizontalLayout(viewButton, addToCartButton);
        actions.setSpacing(true);
        return actions;
    }

    private Component createSortBar() {
        Button sortByNameAsc = new Button("Name (A-Z)");
        sortByNameAsc.addClickListener(e -> presenter.sortByName(true));

        Button sortByNameDesc = new Button("Name (Z-A)");
        sortByNameDesc.addClickListener(e -> presenter.sortByName(false));

        Button sortByPriceAsc = new Button("Price (Low-High)");
        sortByPriceAsc.addClickListener(e -> presenter.sortByPrice(true));

        Button sortByPriceDesc = new Button("Price (High-Low)");
        sortByPriceDesc.addClickListener(e -> presenter.sortByPrice(false));

        Button sortByRating = new Button("Top Rated");
        sortByRating.addClickListener(e -> presenter.sortByRating());

        HorizontalLayout sortBar = new HorizontalLayout();
        sortBar.add(new H5("Sort by:"), sortByNameAsc, sortByNameDesc,
                sortByPriceAsc, sortByPriceDesc, sortByRating);
        sortBar.setSpacing(true);
        sortBar.setPadding(true);
        sortBar.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        sortBar.setWidthFull();

        return sortBar;
    }
    // Add to CatalogView.java after displayProducts method
    private void updateAdminButtons() {
        boolean isAdmin = SecurityContextHolder.isAdmin();
        boolean isStoreOwner = SecurityContextHolder.isStoreOwner();

        // Show admin tools if user is admin or store owner
        boolean showAdminTools = isAdmin || isStoreOwner;

        // If you have admin buttons in this view, make them visible
        System.out.println("Updating admin buttons visibility: " + showAdminTools);
        // Add code to show/hide your admin buttons here
    }
    private void createAdvancedFilterDialog() {
        advancedFilterDialog = new Dialog();
        advancedFilterDialog.setHeaderTitle("Advanced Filters");

        // Store filter
        storeFilter.setPlaceholder("All Stores");
        storeFilter.setWidth("100%");

        // Category filter
        categoryFilter.setWidth("100%");
        categoryFilter.setLabel("Categories");

        // Price range filters
        minPriceFilter.setPlaceholder("0");
        minPriceFilter.setWidth("100%");
        maxPriceFilter.setPlaceholder("Any");
        maxPriceFilter.setWidth("100%");

        // Rating filter
        ratingFilter.setItems("3+", "4+", "4.5+");
        ratingFilter.setPlaceholder("Any Rating");
        ratingFilter.setWidth("100%");

        VerticalLayout dialogLayout = new VerticalLayout(
                storeFilter, categoryFilter,
                new H5("Price Range"),
                minPriceFilter, maxPriceFilter,
                ratingFilter
        );
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        Button applyFiltersBtn = new Button("Apply Filters");
        applyFiltersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyFiltersBtn.addClickListener(e -> {
            applyAdvancedFilters();
            advancedFilterDialog.close();
        });

        Button resetFiltersBtn = new Button("Reset");
        resetFiltersBtn.addClickListener(e -> {
            storeFilter.clear();
            categoryFilter.deselectAll();
            minPriceFilter.clear();
            maxPriceFilter.clear();
            ratingFilter.clear();
        });
        categoryFilter.setLabel("Categories");
        categoryFilter.setWidth("100%");
        categoryFilter.setHeight("200px");               // ← fixed height
        categoryFilter.getElement().getStyle()
                .set("overflow", "auto");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> advancedFilterDialog.close());

        advancedFilterDialog.getFooter().add(cancelBtn, resetFiltersBtn, applyFiltersBtn);
        advancedFilterDialog.add(dialogLayout);
    }

    private void applySearch() {
        String query = searchField.getValue();
        presenter.searchProducts(query, activeFilters);
    }

    private void addCategoryFilter() {
        // Collect all categories from current products
        Set<String> categories = currentProducts.stream()
                .flatMap(p -> p.getCategories().stream())
                .collect(Collectors.toCollection(TreeSet::new));

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filter by Category");

        // Make the checkbox group itself a fixed height so it scrolls
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems(categories);
        checkboxGroup.setWidth("300px");
        checkboxGroup.setHeight("200px");        // ← fixed height
        checkboxGroup.getElement().getStyle()
                .set("overflow", "auto");   // ← enable scrolling

        dialog.add(checkboxGroup);

        Button applyButton = new Button("Apply", e -> {
            Set<String> selected = checkboxGroup.getValue();
            // clear any old category filters
            activeFilters.removeIf(f -> f.startsWith("category="));
            // add new ones
            selected.forEach(cat -> addActiveFilter("category=" + cat));
            dialog.close();
            applySearch();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, applyButton);
        dialog.open();
    }

    private void addPriceFilter() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filter by Price");

        NumberField min = new NumberField("Minimum Price");
        min.setMin(0);
        min.setStep(0.01);

        NumberField max = new NumberField("Maximum Price");
        max.setMin(0);
        max.setStep(0.01);

        VerticalLayout layout = new VerticalLayout(min, max);
        dialog.add(layout);

        Button applyButton = new Button("Apply", e -> {
            if (min.getValue() != null) {
                addActiveFilter("price>" + min.getValue());
            }
            if (max.getValue() != null) {
                addActiveFilter("price<" + max.getValue());
            }
            dialog.close();
            applySearch();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, applyButton);
        dialog.open();
    }

    private void addStoreFilter() {
        // Collect all store names from current products
        Set<String> stores = currentProducts.stream()
                .map(ShoppingProductDTO::getStoreName)
                .collect(Collectors.toSet());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filter by Store");

        ComboBox<String> storeComboBox = new ComboBox<>("Store");
        storeComboBox.setItems(stores);
        storeComboBox.setWidth("300px");

        dialog.add(storeComboBox);

        Button applyButton = new Button("Apply", e -> {
            if (storeComboBox.getValue() != null) {
                addActiveFilter("store=" + storeComboBox.getValue());
            }
            dialog.close();
            applySearch();
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, applyButton);
        dialog.open();
    }

    private void applyAdvancedFilters() {
        activeFilters.clear();
        updateFiltersDisplay();

        // Apply store filter
        if (storeFilter.getValue() != null) {
            addActiveFilter("store=" + storeFilter.getValue());
        }

        // Apply category filters
        for (String category : categoryFilter.getValue()) {
            addActiveFilter("category=" + category);
        }

        // Apply price filters
        if (minPriceFilter.getValue() != null) {
            addActiveFilter("price>" + minPriceFilter.getValue());
        }
        if (maxPriceFilter.getValue() != null) {
            addActiveFilter("price<" + maxPriceFilter.getValue());
        }

        // Apply rating filter
        if (ratingFilter.getValue() != null) {
            String ratingValue = ratingFilter.getValue();
            double minRating = Double.parseDouble(ratingValue.replace("+", ""));
            addActiveFilter("rating>" + minRating);
        }

        applySearch();
    }

    private void addActiveFilter(String filter) {
        if (!activeFilters.contains(filter)) {
            activeFilters.add(filter);
            updateFiltersDisplay();
        }
    }

    private void updateFiltersDisplay() {
        filtersContainer.removeAll();

        if (activeFilters.isEmpty()) {
            return;
        }

        for (String filter : activeFilters) {
            Span filterChip = createFilterChip(filter);
            filtersContainer.add(filterChip);
        }

        // Add clear all button
        if (!activeFilters.isEmpty()) {
            Button clearAllBtn = new Button("Clear All");
            clearAllBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            clearAllBtn.addClickListener(e -> {
                activeFilters.clear();
                updateFiltersDisplay();
                applySearch();
            });
            filtersContainer.add(clearAllBtn);
        }
    }

    private Span createFilterChip(String filter) {
        String displayText = formatFilterForDisplay(filter);

        Span chip = new Span(displayText);
        chip.getStyle()
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "4px 8px")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("display", "inline-flex")
                .set("align-items", "center");

        // Add remove button
        Button removeBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        removeBtn.getStyle()
                .set("margin-left", "4px")
                .set("min-width", "20px")
                .set("width", "20px")
                .set("height", "20px");

        removeBtn.addClickListener(e -> {
            activeFilters.remove(filter);
            updateFiltersDisplay();
            applySearch();
        });

        chip.add(removeBtn);
        return chip;
    }

    private String formatFilterForDisplay(String filter) {
        if (filter.startsWith("category=")) {
            return "Category: " + filter.substring("category=".length());
        } else if (filter.startsWith("store=")) {
            return "Store: " + filter.substring("store=".length());
        } else if (filter.startsWith("price>")) {
            return "Min Price: $" + filter.substring("price>".length());
        } else if (filter.startsWith("price<")) {
            return "Max Price: $" + filter.substring("price<".length());
        } else if (filter.startsWith("rating>")) {
            return "Min Rating: " + filter.substring("rating>".length()) + "★";
        }
        return filter;
    }


    private void updateFilterOptions(List<ShoppingProductDTO> products) {
        // Update store filter options
        Set<String> stores = products.stream()
                .map(ShoppingProductDTO::getStoreName)
                .collect(Collectors.toSet());
        storeFilter.setItems(stores);

        // Update category filter options
        Set<String> categories = new HashSet<>();
        for (ShoppingProductDTO product : products) {
            if (product.getCategories() != null) {
                categories.addAll(product.getCategories());
            }
        }
        categoryFilter.setItems(categories);
    }

    public void navigateToProduct(String productId, String storeName) {
        UI.getCurrent().navigate(
                "product/" + productId + "/" + storeName
        );
    }

    public void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public void showInfo(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();

        // Check for search parameter
        if (queryParameters.getParameters().containsKey("search")) {
            String searchQuery = queryParameters.getParameters().get("search").get(0);
            searchField.setValue(searchQuery);
            // Search will be executed when products are loaded
        }

        // Check for store parameter
        if (queryParameters.getParameters().containsKey("store")) {
            String store = queryParameters.getParameters().get("store").get(0);
            addActiveFilter("store=" + store);
        }

        // Check for category parameter
        if (queryParameters.getParameters().containsKey("category")) {
            String category = queryParameters.getParameters().get("category").get(0);
            addActiveFilter("category=" + category);
        }
    }
}