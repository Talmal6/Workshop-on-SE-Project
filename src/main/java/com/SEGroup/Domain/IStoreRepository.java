package com.SEGroup.Domain;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.SEGroup.Domain.Store.ManagerData;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;

public interface IStoreRepository {
    List<Store> getAllStores();
    Store findByName(String name);
    void checkIfExist(String name);

    void createStore(String StoreName, String founderEmail);
    void updateStoreName(String email, String storeName, String newStoreName);
    void deleteStore(String name,String founderEmail);

    List<ShoppingProduct> getStoreProducts(String StoreName);

    void addProductToStore(String email, String storeName, String catalogID, double price, int quantity);
    void updateShoppingProduct(String email, String storeName, int productID, double price, String description);
    void deleteShoppingProduct(String email, String storeName, int productID);

    void rateProduct(String email, String storeName, int productID, int rating, String review);
    void rateStore(String email, String storeName, int rating, String review);

    // Ownership and manager operations with operator email authorization
    void appointOwner(String storeName, String operatorEmail, String newOwnerEmail);
    void removeOwner(String storeName, String operatorEmail, String ownerToRemove);
    void resignOwnership(String storeName, String operatorEmail);

    void appointManager(String storeName, String operatorEmail, String managerEmail, Set<ManagerPermission> permissions);
    void updateManagerPermissions(String storeName, String operatorEmail, String managerEmail, Set<ManagerPermission> newPermissions);
    boolean hasManagerPermission(String storeName, String managerEmail, ManagerPermission permission);

    Set<ManagerPermission> getManagerPermissions(String storeName, String operatorEmail, String managerEmail);
    Set<String> getAllOwners(String storeName, String operatorEmail);
    Map<String, ManagerData> getAllManagers(String storeName, String operatorEmail);
}