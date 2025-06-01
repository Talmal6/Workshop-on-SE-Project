package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.Store;
import java.util.List;

public interface StoreData {
    Store findByName(String storeName);
    void saveStore(Store store);
    void updateStore(Store store);
    List<Store> getAllStores();
    boolean isStoreExist(String storeName);
    public List<Store> getStoresOwnedBy(String ownerEmail);
}