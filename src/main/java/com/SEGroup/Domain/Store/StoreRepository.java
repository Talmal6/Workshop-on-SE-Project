package com.SEGroup.Domain.Store;
import com.SEGroup.Domain.IStoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//implement iStore
public class StoreRepository implements IStoreRepository {
    private final List<Store> stores = new ArrayList<>();

    @Override
    public List<Store> getAllStores() {
        return new ArrayList<>(stores);
    }


    @Override
    public Store findByName(String name) {
        Store find_store = stores.stream()
                .filter(store -> store.getName().equals(name))
                .findFirst()
                .orElse(null);
        if(find_store == null) {
            throw new RuntimeException("Store does not exist");
        }
        return find_store;
    }

    @Override
    public void checkIfExist(String name) {
        boolean exists = stores.stream().anyMatch(store -> store.getName().equals(name));
        if (!exists) {
            throw new RuntimeException("Store does not exist");
        }
    }
    @Override
    public void createStore(String storeName, String founderEmail) {
        checkIfExist(storeName);
        stores.add(new Store(storeName, founderEmail));
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
    public void deleteStore(String name, String founderEmail) {
        Store store = findByName(name);

        if (!store.getfounderEmail().equals(founderEmail)) {
            throw new RuntimeException("Only the founder can delete the store");
        }

        stores.remove(store);
    }
    @Override
    public void changeStoreName(String oldName, String newName){
        Store store = findByName(oldName);
        store.setName(newName);
    }
    @Override
    public void appointOwner(String storeName, String appointerEmail, String newOwnerEmail) {
        Store store = findByName(storeName);
        store.appointOwner(appointerEmail, newOwnerEmail);
    }

    @Override
    public void removeOwner(String storeName, String removerEmail, String ownerToRemove) {
        Store store = findByName(storeName);
        store.removeOwner(removerEmail, ownerToRemove);
    }

    @Override
    public void resignOwnership(String storeName, String ownerEmail) {
        Store store = findByName(storeName);
        store.resignOwnership(ownerEmail);
    }

    @Override
    public void appointManager(String storeName, String ownerEmail, String managerEmail, Set<ManagerPermission> permissions) {
        Store store = findByName(storeName);
        store.appointManager(ownerEmail, managerEmail, permissions);
    }

    @Override
    public void updateManagerPermissions(String storeName, String ownerEmail, String managerEmail, Set<ManagerPermission> newPermissions) {
        Store store = findByName(storeName);
        store.updateManagerPermissions(ownerEmail, managerEmail, newPermissions);
    }

    @Override
    public boolean hasManagerPermission(String storeName, String managerEmail, ManagerPermission permission) {
        Store store = findByName(storeName);
        return store.hasManagerPermission(managerEmail, permission);
    }

    @Override
    public Set<ManagerPermission> getManagerPermissions(String storeName, String operatorEmail, String managerEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail) && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view manager permissions");
        }

        return store.getManagerPermissions(managerEmail);
    }

    @Override
    public Set<String> getAllOwners(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail) && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view store owners");
        }

        return store.getAllOwners();
    }

    @Override
    public Map<String, ManagerData> getAllManagers(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail) && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view managers");
        }

        return store.getAllManagers();
    }
    @Override
    public List<ShoppingProduct> viewPublicStoreProducts(String storeName) {
        Store store = findByName(storeName);

        if (!store.isActive()) {
            throw new RuntimeException("Store is not active");
        }

        return new ArrayList<>(store.getAllProducts());
    }
}

