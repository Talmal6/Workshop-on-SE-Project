package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.CatalogProductDTO;
import com.SEGroup.DTO.StoreCardDto;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.GeneralCatalogView;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for the GeneralCatalogView.
 * Handles business logic for browsing the base catalog and adding products to stores.
 */
public class GeneralCatalogPresenter {
    private final GeneralCatalogView view;
    private final StoreService storeService;

    public GeneralCatalogPresenter(GeneralCatalogView view) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
    }

    /**
     * Loads the base catalog of products and populates the view.
     */
    public void loadBaseCatalog() {
        try {
            Result<List<com.SEGroup.Domain.ProductCatalog.CatalogProduct>> result =
                    storeService.viewPublicProductCatalog();
            if (result.isSuccess()) {
                List<CatalogProductDTO> dtos = new ArrayList<>();
                for (com.SEGroup.Domain.ProductCatalog.CatalogProduct p : result.getData()) {
                    List<String> cats = p.getCategories() != null
                            ? new ArrayList<>(p.getCategories())
                            : new ArrayList<>();
                    dtos.add(new CatalogProductDTO(
                            p.getCatalogID(),
                            p.getName(),
                            cats
                    ));
                }
                view.displayBaseCatalog(dtos);

                // populate store dropdown
                List<StoreCardDto> stores =
                        storeService.listStoresOwnedBy(SecurityContextHolder.email());
                view.populateStoreSelector(stores);

            } else {
                view.showError("Cannot load catalog: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error loading catalog: " + e.getMessage());
        }
    }

    public void addNewCatalogProduct(String catalogId,
                                     String name,
                                     String brand,
                                     String description,
                                     List<String> categories) {
        try {
            Result<String> res = storeService.addProductToCatalog(
                    catalogId,
                    name,
                    brand,
                    description,
                    categories
            );
            if (res.isSuccess()) {
                view.showSuccess("Added “" + name + "” to BASE catalog (ID: " + catalogId + ")");
            } else {
                view.showError("Could not add to catalog: " + res.getErrorMessage());
            }
        } catch (Exception ex) {
            view.showError("Error adding catalog product: " + ex.getMessage());
        }
    }
    /**
     * Adds a product from the base catalog to a specific store.
     */
    public void addToMyStore(String catalogId,
                             String storeName,
                             String productName,
                             String description,
                             double price,
                             int quantity,
                             String imageURL) {
        try {
            Result<String> result = storeService.addProductToStore(
                    SecurityContextHolder.token(),
                    storeName,
                    catalogId,
                    productName,
                    description,
                    price,
                    quantity,
                    imageURL
            );
            if (result.isSuccess()) {
                view.showSuccess("Added \"" + productName + "\" to " + storeName);
            } else {
                view.showError("Failed to add: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error adding to store: " + e.getMessage());
        }
    }
}
