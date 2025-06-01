package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Mapper.StoreMapper;

import java.util.*;

public class InMemoryStoreData implements StoreData {

    private final List<Store> stores = new ArrayList<>();

    @Override
    public List<Store> getAllStores() {
        return stores;
    }
    @Override
    public void saveStore(Store store) {
        stores.add(store);
    }

    @Override
    public Store findByName(String name) {
        Store find_store = stores.stream()
                .filter(store -> store.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (find_store == null) {
            throw new RuntimeException("Store does not exist");
        }
        return find_store;
    }
    @Override
    public boolean isStoreExist(String name) {
        return stores.stream().anyMatch(store -> store.getName().equals(name));
    }
    @Override
    public List<Store> getStoresOwnedBy(String ownerEmail) {
        return stores;
    }
    @Override
    public void updateStore(Store store) {
        for (int i = 0; i < stores.size(); i++) {
            Store existingStore = stores.get(i);
            if (existingStore.getName().equals(store.getName())) {
                stores.set(i, store);
                return;
            }
        }
        throw new RuntimeException("Store to update does not exist: " + store.getName());
    }




}
