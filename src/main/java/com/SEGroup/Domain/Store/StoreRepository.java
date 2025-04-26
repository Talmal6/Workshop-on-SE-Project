package com.SEGroup.Domain.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Service.Mapper.StoreMapper;

//implement iStore
public class StoreRepository implements IStoreRepository {
    private final List<Store> stores = new ArrayList<>();
    private StoreMapper storeMapper;

    @Override
    public List<StoreDTO> getAllStores() {
        List<StoreDTO> storeDTOs = storeMapper.toDTOs(stores);
        return storeDTOs;
    }

    @Override
    public StoreDTO getStore(String storeName) {
        Store store = findByName(storeName);
        return storeMapper.toDTO(store);
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
    public ShoppingProductDTO updateShoppingProduct(String email, String storeName, String catalogID, double price, String description) {
        Store store = findByName(storeName);
        if(!store.isOwnerOrHasManagerPermissions(email)) {
            throw new RuntimeException("User is not authorized to update product");
        }
        ShoppingProduct product = store.getProduct(catalogID);
        if (product == null) {
            throw new RuntimeException("Product not found in store");
        }
        product.setPrice(price);
        product.setName(description); // assuming description is name; change if needed
        ShoppingProductDTO productDTO = convertProductToDTO(product);
        return productDTO;          
    }
    @Override
    public void addProductToStore(String email, String storeName, String catalogID, String product_name,String description, double price,
                                  int quantity) {
        Store store = findByName(storeName);
        if (store.isOwnerOrHasManagerPermissions(email)) {
            store.addProductToStore(email, storeName, catalogID, product_name, description, price, quantity);
        }
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
    public void appointManager(String storeName, String ownerEmail, String managerEmail, List<String> permissions) {
        Store store = findByName(storeName);
        Set<ManagerPermission> permissionSet = new HashSet<>();
        for (String perm : permissions) {
            permissionSet.add(ManagerPermission.valueOf(perm));
        }

        store.appointManager(ownerEmail, managerEmail, permissionSet);
    }

    @Override
    public void updateManagerPermissions(String storeName, String ownerEmail, String managerEmail,
                                         List<String> newPermissions) {
        Store store = findByName(storeName);
        Set<ManagerPermission> permissionSet = new HashSet<>();
        for (String perm : newPermissions) {
            permissionSet.add(ManagerPermission.valueOf(perm));
        }
        store.updateManagerPermissions(ownerEmail, managerEmail, permissionSet);
    }


    @Override
    public List<String> getManagerPermissions(String storeName, String operatorEmail, String managerEmail) {
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
    public List<String> getAllManagers(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view managers");
        }

        return store.getAllManagers();
    }



    @Override
    public ShoppingProductDTO deleteShoppingProduct(String email, String storeName, String productID) {
        Store store = findByName(storeName);
        ShoppingProductDTO product = convertProductToDTO(store.getProduct(productID));
        if(store.isOwnerOrHasManagerPermissions(email)){
            store.removeProduct(productID);
        }
        return product;
    }

    @Override
    public ShoppingProductDTO rateProduct(String email, String storeName, String productID, int rating, String review) {
        Store store = findByName(storeName);
        ShoppingProduct product = store.getProduct(productID);
        ShoppingProductDTO productDTO = convertProductToDTO(product);
        if (product == null) {
            throw new RuntimeException("Product not found in store ");
        }
        if (rating == 1 || rating > 5) {
            throw new IllegalArgumentException(("Rating must be between 1-5"));

        }

        product.addRating(email, rating, review);
        return productDTO;
    }

    @Override
    public void rateStore( String email, String storeName, int rating, String review) {
        Store store = findByName(storeName);
        if(!store.isActive())
        {
            throw new RuntimeException("Store is closed - cannot be rated ");
        }
        store.rateStore(email,rating,review);
    }


    @Override
    public void addToBalance(String operatorEmail,
                             String storeName,
                             double amount) {

        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        Store store = findByName(storeName);
        boolean authorised =
                store.isOwner(operatorEmail) ||
                        store.hasManagerPermission(operatorEmail,
                                ManagerPermission.MANAGE_ROLES);
        if (!authorised)
            throw new RuntimeException("User is not allowed to modify balance");

        store.addToBalance(amount);
    }

    private ShoppingProductDTO convertProductToDTO(ShoppingProduct product) {
        return new ShoppingProductDTO(
                product.getStoreName(),
                product.getCatalogID(),
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.averageRating()
        );
    }


    private List<ShoppingProductDTO> convertProductsToDTO(List<ShoppingProduct> products) {
        List<ShoppingProductDTO> dtos = new ArrayList<>();
        for (ShoppingProduct product : products) {
            dtos.add(convertProductToDTO(product));
        }
        return dtos;
    }

    @Override
    public Map<BasketDTO, Double> removeItemsFromStores(List<BasketDTO> basketDTOList) {
        Map<BasketDTO, Double> basketToTotalPrice = new HashMap<>();
        List<BasketDTO> succeededRemovals = new ArrayList<>();

        try {
            for (BasketDTO basketDTO : basketDTOList) {
                Store store = findByName(basketDTO.storeId());
                double storeTotal = 0;

                for (Map.Entry<String, Integer> entry : basketDTO.prod2qty().entrySet()) {
                    String productId = entry.getKey();
                    int quantityToRemove = entry.getValue();

                    ShoppingProduct product = store.getProduct(productId);
                    if (product == null) {
                        throw new RuntimeException("Product not found: " + productId);
                    }

                    if (product.getQuantity() < quantityToRemove) {
                        throw new RuntimeException("Not enough quantity for product: " + productId);
                    }

                    product.setQuantity(product.getQuantity() - quantityToRemove);
                    storeTotal += product.getPrice() * quantityToRemove;
                }

                succeededRemovals.add(basketDTO);
                basketToTotalPrice.put(basketDTO, storeTotal);
            }
        } catch (Exception e) {
            rollBackItemsToStores(succeededRemovals);
            throw new RuntimeException("Failed to remove items from stores: " + e.getMessage());
        }

        return basketToTotalPrice;
    }
    @Override
    public void rollBackItemsToStores(List<BasketDTO> basketDTOList) {
        for (BasketDTO basketDTO : basketDTOList) {
            Store store = findByName(basketDTO.storeId());

            for (Map.Entry<String, Integer> entry : basketDTO.prod2qty().entrySet()) {
                String productId = entry.getKey();
                int quantityToAddBack = entry.getValue();

                ShoppingProduct product = store.getProduct(productId);
                if (product != null) {
                    product.setQuantity(product.getQuantity() + quantityToAddBack);
                }
            }
        }
    }
}
