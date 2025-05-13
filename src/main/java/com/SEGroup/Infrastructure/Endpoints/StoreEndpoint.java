package com.SEGroup.Infrastructure.Endpoints;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.vaadin.hilla.EndpointExposed;

import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@EndpointExposed
@PermitAll         // tighten later (e.g., @RolesAllowed)
public class StoreEndpoint {

    private final StoreService storeService;

    public StoreEndpoint(StoreService storeService) {
        this.storeService = storeService;
    }

    /* ---------- 1. Global catalog ---------- */

    @Nonnull
    public String addProductToCatalog(String catalogID,
                                      String name,
                                      String brand,
                                      String description,
                                      List<String> categories) {
        return unwrap(storeService.addProductToCatalog(
                catalogID, name, brand, description, categories));
    }

    /* ---------- 2‑4. Store & catalog queries ---------- */

    @Nonnull
    public StoreDTO viewStore(String storeName) {
        return unwrap(storeService.viewStore(storeName));
    }

    @Nonnull
    public List<StoreDTO> viewAllStores() {
        return unwrap(storeService.viewAllStores());
    }

    @Nonnull
    public List<CatalogProduct> viewPublicProductCatalog() {
        return unwrap(storeService.viewPublicProductCatalog());
    }

    /* ---------- 5‑7. Store life‑cycle ---------- */

    public void createStore(String sessionKey, String storeName) {
        unwrap(storeService.createStore(sessionKey, storeName));
    }

    public void closeStore(String sessionKey, String storeName) {
        unwrap(storeService.closeStore(sessionKey, storeName));
    }

    public void reopenStore(String sessionKey, String storeName) {
        unwrap(storeService.reopenStore(sessionKey, storeName));
    }

    /* ---------- 8‑10. Store product CRUD ---------- */

    @Nonnull
    public String addProductToStore(String sessionKey, String storeName,
                                    String catalogID, String productName,
                                    String description, double price, int quantity) {
        return unwrap(storeService.addProductToStore(
                sessionKey, storeName, catalogID, productName, description, price, quantity));
    }

    public void updateShoppingProduct(String sessionKey, String storeName,
                                      String productID, String description, Double price) {
        unwrap(storeService.updateShoppingProduct(
                sessionKey, storeName, productID, description, price));
    }

    public void deleteShoppingProduct(String sessionKey, String storeName, String productID) {
        unwrap(storeService.deleteShoppingProduct(sessionKey, storeName, productID));
    }

    /* ---------- 11‑12. Ratings ---------- */

    public void rateStore(String sessionKey, String storeName, int rating, String review) {
        unwrap(storeService.rateStore(sessionKey, storeName, rating, review));
    }

    public void rateProduct(String sessionKey, String storeName,
                            String productID, int rating, String review) {
        unwrap(storeService.rateProduct(sessionKey, storeName, productID, rating, review));
    }

    /* ---------- 13‑18. Ownership & management ---------- */

    public void appointOwner(String sessionKey, String storeName, String apointeeEmail) {
        unwrap(storeService.appointOwner(sessionKey, storeName, apointeeEmail));
    }

    public void removeOwner(String sessionKey, String storeName, String apointeeEmail) {
        unwrap(storeService.removeOwner(sessionKey, storeName, apointeeEmail));
    }

    public void resignOwnership(String sessionKey, String storeName) {
        unwrap(storeService.resignOwnership(sessionKey, storeName));
    }

    public void appointManager(String sessionKey, String storeName,
                               String apointeeEmail, List<String> permissions) {
        unwrap(storeService.appointManager(sessionKey, storeName, apointeeEmail, permissions));
    }

    public void updateManagerPermissions(String sessionKey, String storeName,
                                         String apointeeEmail, List<String> permissions) {
        unwrap(storeService.updateManagerPermissions(sessionKey, storeName, apointeeEmail, permissions));
    }

    @Nonnull
    public List<String> getManagerPermission(String sessionKey, String storeName, String managerEmail) {
        return unwrap(storeService.getManagerPermission(sessionKey, storeName, managerEmail));
    }

    @Nonnull
    public List<String> getAllOwners(String sessionKey, String storeName) {
        return unwrap(storeService.getAllOwners(sessionKey, storeName, null));
    }

    @Nonnull
    public List<String> getAllManagers(String sessionKey, String storeName) {
        return unwrap(storeService.getAllManagers(sessionKey, storeName, null));
    }

    /* ---------- 19‑22. Search, bids, offers ---------- */

    @Nonnull
    public List<ShoppingProductDTO> searchProducts(String query,
                                                   List<String> searchFilters,
                                                   String storeName,
                                                   List<String> categories) {
        return unwrap(storeService.searchProducts(query, searchFilters, storeName, categories));
    }

    public void submitBid(String sessionKey, String storeName,
                          String productID, double bidAmount, int quantity) {
        unwrap(storeService.submitBidToShoppingItem(sessionKey, storeName, productID, bidAmount , quantity));
    }

    public void sendAuctionOffer(String sessionKey, String storeName,
                                 String productID, double bidAmount, int quantity) {
        unwrap(storeService.sendAuctionOffer(sessionKey, storeName, productID, bidAmount, quantity));
    }

    /* ---------- 23‑24. Single‑product queries ---------- */

    public int getProductQuantity(String sessionKey, String storeName, String productID) {
        return unwrap(storeService.getProductQuantity(sessionKey, storeName, productID));
    }

    @Nonnull
    public ShoppingProductDTO getProductFromStore(String sessionKey, String storeName, String productID) {
        return unwrap(storeService.getProductFromStore(sessionKey, storeName, productID));
    }

    /* ---------- helper: unwrap Result<T> ---------- */
    private static <T> T unwrap(Result<T> r) {
        if (r.isSuccess()) return r.getData();            // may be null if T == Void
        throw new RuntimeException(r.getErrorMessage());  // Hilla maps to 400 response
    }
}