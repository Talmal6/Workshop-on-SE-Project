package com.SEGroup.Domain.Store;

import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.IStoreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//implement iStore
public class StoreRepository implements IStoreRepository {
    private final List<Store> stores = new ArrayList<>();

    @Override
    public List<StoreDTO> getAllStores() {
        List<StoreDTO> storeDTOs = new ArrayList<>();
        for (Store store : stores) {
            storeDTOs.add(new StoreDTO(store.getId(), store.getName(), store.getFounderEmail(), store.isActive()));
        }
        return storeDTOs;
    }

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
    public void closeStore(String storeName, String founderEmail) {
        Store store = findByName(storeName);

        if (!store.getfounderEmail().equals(founderEmail)) {
            throw new RuntimeException("Only the founder can close the store");
        }

        store.close();
    }

    @Override
    public void reopenStore(String storeName, String founderEmail) {
        Store store = findByName(storeName);

        if (!store.getfounderEmail().equals(founderEmail)) {
            throw new RuntimeException("Only the founder can reopen the store");
        }

        store.open();
    }


    @Override
    public void addProductToStore(String email, String storeName, String catalogID, String product_name, double price,
            int quantity) {
        Store store = findByName(storeName);
        store.addProductToStore(catalogID, product_name, price, quantity);
    }

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
    public void appointManager(String storeName, String ownerEmail, String managerEmail,
            Set<ManagerPermission> permissions) {
        Store store = findByName(storeName);
        store.appointManager(ownerEmail, managerEmail, permissions);
    }

    @Override
    public void updateManagerPermissions(String storeName, String ownerEmail, String managerEmail,
            Set<ManagerPermission> newPermissions) {
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

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view manager permissions");
        }

        return store.getManagerPermissions(managerEmail);
    }

    @Override
    public List<String> getAllOwners(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view store owners");
        }

        return store.getAllOwners();
    }

    @Override
    public Map<String, ManagerData> getAllManagers(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
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

    @Override
    public StoreDTO getStore(String storeName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStore'");
    }

    @Override
    public void updateStoreName(String email, String storeName, String newStoreName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateStoreName'");
    }

    @Override
    public void updateShoppingProduct(String email, String storeName, int productID, double price, String description) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateShoppingProduct'");
    }

    @Override
    public void deleteShoppingProduct(String email, String storeName, int productID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShoppingProduct'");
    }

    @Override
    public void rateProduct(String email, String storeName, int productID, int rating, String review) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rateProduct'");
    }

    @Override
    public void rateStore(String email, String storeName, int rating, String review) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rateStore'");
    }

    @Override
    public void appointManager(String storeName, String operatorEmail, String managerEmail, List<String> permissions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appointManager'");
    }

    @Override
    public void updateManagerPermissions(String storeName, String operatorEmail, String managerEmail,
            List<String> newPermissions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateManagerPermissions'");
    }

    @Override
    public void addToBalance(String userBySession, String storeName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addToBalance'");
    }

}
