package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.ComplexCondDiscountView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComplexCondDiscountPresenter {

    private final ComplexCondDiscountView view;
    private final String storeName;
    private final StoreService storeService;  // inject via ServiceLocator or constructor

    public ComplexCondDiscountPresenter(ComplexCondDiscountView view, String storeName) {
        this.view = view;
        this.storeName = storeName;
        this.storeService = ServiceLocator.getStoreService(); // however you get it
    }

    public void loadProducts() {
        Result<List<ShoppingProductDTO>> res = storeService.getStoreProducts(storeName);
        Set<String> categories = new HashSet<>();
        if (res.isSuccess()) {
            List<ShoppingProductDTO> ps = res.getData();
            for(ShoppingProductDTO p: ps){
               categories.addAll(p.getCategories());
            }
            view.categories.setItems(categories);
            view.products.setItems(res.getData());
            view.products.setItemLabelGenerator(ShoppingProductDTO::getName);
            view.setComboItems(res.getData());
        } else {
            view.showError("Could not load products: " + res.getErrorMessage());
        }
    }

    public void apply_on_entire_store(String operator,
                 List<String> productIds,
                 List<Integer> minAmounts,
                 List<Integer> maxAmounts,
                 int minPrice,
                 int percentage,
                 String couponCode){
        Result<Void> res = storeService.addLogicalCompositeConditionalDiscountToEntireStore(
                SecurityContextHolder.token(),
                storeName, percentage, productIds, minAmounts, maxAmounts, minPrice, operator,couponCode);
        if(res.isSuccess()){
            view.showSuccess("Discount added successfully!");
            view.navigateBack();
        }
        else{
            view.showError("Adding discount failed!");
        }
    }

    public void apply_on_entire_category(String operator,
                                 String category,
                                 List<String> productIds,
                                 List<Integer> minAmounts,
                                 List<Integer> maxAmounts,
                                 int minPrice,
                                 int percentage,
                                 String couponCode){
        Result<Void> res = storeService.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                SecurityContextHolder.token(),
                storeName, category, percentage, productIds, minAmounts, maxAmounts, minPrice, operator,couponCode);
        if(res.isSuccess()){
            view.showSuccess("Discount added successfully!");
            view.navigateBack();
        }
        else{
            view.showError("Adding discount failed!");
        }
    }

    public void apply_on_product(String operator,
                                      String product_id,
                                      List<String> productIds,
                                      List<Integer> minAmounts,
                                      List<Integer> maxAmounts,
                                      int minPrice,
                                      int percentage,
                                      String couponCode){
        System.out.println(product_id);
        System.out.println(productIds);
        Result<Void> res = storeService.addLogicalCompositeConditionalDiscountToSpecificProductInStorePercentage(
                SecurityContextHolder.token(),
                storeName, product_id, percentage, productIds, minAmounts, maxAmounts, minPrice, operator,couponCode);
        if(res.isSuccess()){
            view.showSuccess("Discount added successfully!");
            view.navigateBack();
        }
        else{
            view.showError("Adding discount failed!");
        }
    }
}
