package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.AddDiscountView;

import java.util.*;
import java.util.stream.Collectors;

public class AddDiscountPresenter {
    private final AddDiscountView view;
    private Map<String, List<ShoppingProductDTO>> productsByCategory;
    private List<String> categories;
    private String storeName;
    private StoreService storeService;

    public AddDiscountPresenter(AddDiscountView view, String storeName) {
        this.view = view;
        this.storeName = storeName;
        this.productsByCategory = new HashMap<>();
        this.storeService = ServiceLocator.getStoreService();
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
//        return         Result.failure("Unknown error");
        return storeService.addSimpleDiscountToEntireStore(SecurityContextHolder.token(), storeName, value, null);

    }

    public Result<Void> addDiscountToCategory(String category, Integer value) {
//        return         Result.failure("Unknown error");
        return  storeService.addSimpleDiscountToEntireCategoryInStore(SecurityContextHolder.token(),storeName,category,value, null);

    }

    public Result<Void> addDiscountToProduct(String category, String item, Integer value, int minAmount) {
//        return         Result.failure("Unknown error");
        return storeService.addSimpleDiscountToSpecificProductInStorePercentage(SecurityContextHolder.token(),storeName,item,value,null);
    }

    public Result<Void> addDiscountToStoreWithCoupon(Integer value, String couponCode) {
//        return         Result.failure("Unknown error");
        return  storeService.addSimpleDiscountToEntireStore(SecurityContextHolder.token(), storeName, value, couponCode);
    }

    public Result<Void> addDiscountToCategoryWithCoupon(String category, Integer value, String couponCode) {
        return  storeService.addSimpleDiscountToEntireCategoryInStore(SecurityContextHolder.token(),storeName,category,value, couponCode);

    }

    public Result<Void> addDiscountToProductWithCoupon(String category, String item, Integer value, int minAmount, String couponCode) {
        return storeService.addSimpleDiscountToSpecificProductInStorePercentage(SecurityContextHolder.token(),storeName,item,value,couponCode);

    }

    public Result<Void> addConditionalDiscountToStore(Integer value, Integer minimumPrice) {
//        return         Result.failure("Unknown error");
        return storeService.addConditionalDiscountToEntireStore(SecurityContextHolder.token(), storeName, value, null);

    }

    public Result<Void> addConditionalDiscountToCategory(String category, Integer value, Integer minimumPrice) {
//        return         Result.failure("Unknown error");
        return  storeService.addConditionalDiscountToEntireCategoryInStore(SecurityContextHolder.token(),storeName,category,value, null);

    }

    public Result<Void> addConditionalDiscountToProduct(String category, String item, Integer value, int minAmount, Integer minimumPrice) {
//        return         Result.failure("Unknown error");
        return storeService.addConditionalDiscountToSpecificProductInStorePercentage(SecurityContextHolder.token(),storeName,item,value,null);
    }

    public Result<Void> addConditionalDiscountToStoreWithCoupon(Integer value, String couponCode, Integer minimumPrice) {
//        return         Result.failure("Unknown error");
        return  storeService.addConditionalDiscountToEntireStore(SecurityContextHolder.token(), storeName, value, couponCode);
    }

    public Result<Void> addConditionalDiscountToCategoryWithCoupon(String category, Integer value, String couponCode, Integer minimumPrice) {
        return  storeService.addConditionalDiscountToEntireCategoryInStore(SecurityContextHolder.token(),storeName,category,value, couponCode);

    }

    public Result<Void> addConditionalDiscountToProductWithCoupon(String category, String item, Integer value, int minAmount, String couponCode, Integer minimumPrice) {
        return storeService.addConditionalDiscountToSpecificProductInStorePercentage(SecurityContextHolder.token(),storeName,item,value,couponCode);

    }
} 