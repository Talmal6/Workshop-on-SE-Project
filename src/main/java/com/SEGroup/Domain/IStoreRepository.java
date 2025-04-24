package com.SEGroup.Domain;
import com.SEGroup.Domain.Store.Store;

import java.util.List;

public interface IStoreRepository {
    List<Store> getAllStores();
    Store findByName(String name);
    void checkIfExist(String name);
    void addStore(Store store);
    void updateStore(Store store);
    void deleteStore(String storeId);
    void changeStoreName(String oldName, String newName);
}