package com.SEGroup.Domain;
import com.SEGroup.Domain.Store.ManagerData;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStoreRepository {
    List<Store> getAllStores();
    Store findByName(String name);
    void checkIfExist(String name);
    void createStore(String StoreName, String founderEmail);
    void addStore(Store store); //going to be deleted
    void updateStore(Store store); //going to be deleted
    void deleteStore(String name); //going to be deleted
    void deleteStore(String name,String founderEmail);
    void changeStoreName(String oldName, String newName);
    List<ShoppingProduct> viewPublicStoreProducts(String StoreName);

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