package com.SEGroup.Domain.Store;
import com.SEGroup.Domain.IStoreRepository;

import java.util.ArrayList;
import java.util.List;
//implement iStore
public class StoreRepository implements IStoreRepository {
    private final List<Store> stores = new ArrayList<>();

    @Override
    public List<Store> getAllStores() {
        return new ArrayList<>(stores);
    }

    @Override
    public Store findByName(String name) {
        return stores.stream()
                .filter(store -> store.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void checkIfExist(String name) {
        boolean exists = stores.stream().anyMatch(store -> store.getName().equals(name));
        if (!exists) {
            throw new RuntimeException("Store does not exist");
        }
    }

    @Override
    public void addStore(Store store) {
        checkIfExist(store.getName());
        stores.add(store);
    }

    @Override
    public void updateStore(Store store) {
        //CHANGE ID TO STRING
        int id = store.getId();
        deleteStore(String.valueOf(id));
        stores.add(store);
    }

    @Override
    public void deleteStore(String storeId) {
        stores.removeIf(store -> store.getId() == Integer.parseInt(storeId));
    }

    @Override
    public void changeStoreName(String oldName, String newName){
        Store store = findByName(oldName);
        store.setName(newName);
    }
}

