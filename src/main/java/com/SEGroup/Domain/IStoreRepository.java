package com.SEGroup.Domain;
import java.util.List;

public interface IStoreRepository {
    List<Store> getAllStores();
    Store findByName(String name);
    boolean checkIfExist(String name);
    void addStore(Store store);
    void updateStore(Store store);
    void deleteStore(String storeId);
}