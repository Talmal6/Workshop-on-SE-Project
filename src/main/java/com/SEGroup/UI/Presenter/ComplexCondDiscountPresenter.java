package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.ComplexCondDiscountView;

import java.util.List;

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
        if (res.isSuccess()) {
            view.setComboItems(res.getData().stream().map(prod -> prod.getName()).toList());
        } else {
            view.showError("Could not load products: " + res.getErrorMessage());
        }
    }

    public void confirm(String operator,
                 List<String> productIds,
                 List<Integer> minAmounts,
                 double minPrice,
                 int precentage,
                 String couponCode){
        Result<Void> res = storeService.addCompositeConditionDiscountToEntireCategoryInStore(SecurityContextHolder.token(),
                storeName,operator,productIds, minAmounts,  minPrice, percentage,coupon);
        if(res.isSuccess()){
            view.showSuccess("Discount added successfully!");
        }
        else{
            view.showError("Adding discount failed!");
        }

    }
}
