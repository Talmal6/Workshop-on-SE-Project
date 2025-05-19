package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.AddDiscountView;

import java.util.*;
import java.util.stream.Collectors;

public class AddDiscountPresenter {
    private final AddDiscountView view;
    private Map<String, List<ShoppingProductDTO>> productsByCategory;
    private List<String> categories;
    private String storeName;

    public AddDiscountPresenter(AddDiscountView view, String storeName) {
        this.view = view;
        this.storeName = storeName;
        this.productsByCategory = new HashMap<>();
        loadStoreProducts();
    }

    private void loadStoreProducts() {
        Result<List<ShoppingProductDTO>> result = ServiceLocator.getStoreService().getStoreProducts(storeName);
        if (result.isSuccess()) {
            List<ShoppingProductDTO> products = result.getData();
            
            // Group products by category
            productsByCategory = products.stream()
                    .collect(Collectors.groupingBy(
                            product -> product.getCategories().isEmpty() ? "Uncategorized" : product.getCategories().get(0)
                    ));

            // Sort categories alphabetically
            productsByCategory = new TreeMap<>(productsByCategory);

            // Sort products within each category by name
            productsByCategory.replaceAll((category, productList) -> 
                productList.stream()
                    .sorted(Comparator.comparing(ShoppingProductDTO::getName))
                    .collect(Collectors.toList())
            );
            // create a list of categories
            categories = new ArrayList<>(productsByCategory.keySet());
            categories.add(0, "Entire Store");
            view.updateCategories(categories);
            view.updateProducts(productsByCategory);



            System.out.println("Loaded " + products.size() + " products in " + productsByCategory.size() + " categories");
        } else {
            System.err.println("Failed to load store products");
        }
    }

    public Map<String, List<ShoppingProductDTO>> getProductsByCategory() {
        return productsByCategory;
    }

    public List<String> getCategories() {
        return new ArrayList<>(productsByCategory.keySet());
    }

    public List<ShoppingProductDTO> getProductsInCategory(String category) {
        return productsByCategory.getOrDefault(category, Collections.emptyList());
    }

    public Result<Void> addDiscountToStore(Integer value) {
        //todo
        return         Result.failure("Unknown error");

    }

    public Result<Void> addDiscountToCategory(String category, Integer value) {
        //todo
        return         Result.failure("Unknown error");

    }

    public Result<Void> addDiscountToProduct(String category, String item, Integer value, int minAmount) {
        return         Result.failure("Unknown error");

    }

    public Result<Void> addDiscountToStoreWithCoupon(Integer value, String couponCode) {
        return         Result.failure("Unknown error");

    }

    public Result<Void> addDiscountToCategoryWithCoupon(String category, Integer value, String couponCode) {
        return         Result.failure("Unknown error");

    }

    public Result<Void> addDiscountToProductWithCoupon(String category, String item, Integer value, int minAmount, String couponCode) {
        return         Result.failure("Unknown error");

    }
} 