package com.SEGroup.Infrastructure.Repositories;

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
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Mapper.StoreMapper;

//implement iStore
/**
 The StoreRepository class is responsible for managing the stores in the system.
 It provides methods to create, update, and retrieve store information, as well as
 manage store products, owners, and managers. */
public class StoreRepository implements IStoreRepository {
    private final List<Store> stores = new ArrayList<>();
    private StoreMapper storeMapper = new StoreMapper();

    /**
     Retrieves all stores in the system.

     @return A list of StoreDTO objects representing all stores. */
    @Override
    public List<StoreDTO> getAllStores() {
        List<StoreDTO> storeDTOs = storeMapper.toDTOs(stores);
        return storeDTOs;
    }

    /**
     Retrieves a specific store by its name.

     @param storeName The name of the store to retrieve.
     @return A StoreDTO object representing the store. */
    @Override
    public StoreDTO getStore(String storeName) {
        Store store = findByName(storeName);
        return storeMapper.toDTO(store);
    }

    /**
     Creates a new store with the specified name and founder's email.

     @param storeName The name of the store to create.
     @param founderEmail The email of the store's founder. */
    @Override
    public void createStore(String storeName, String founderEmail) {
        if(isStoreExist(storeName)) {
            throw new RuntimeException("Store already exists");
        }
        stores.add(new Store(storeName, founderEmail));
    }

    /**
     Closes a store, making it inactive.

     @param storeName The name of the store to close.
     @param founderEmail The email of the store's founder. */
    @Override
    public void closeStore(String storeName, String founderEmail) {
        Store store = findByName(storeName);

        if (!store.getfounderEmail().equals(founderEmail)) {
            throw new RuntimeException("Only the founder can close the store");
        }

        store.close();
    }
    /**
     Reopens a closed store.

     @param storeName The name of the store to reopen.
     @param founderEmail The email of the store's founder. */
    @Override
    public void reopenStore(String storeName, String founderEmail) {
        Store store = findByName(storeName);
        if (!store.getfounderEmail().equals(founderEmail)) {
            throw new RuntimeException("Only the founder can reopen the store");
        }

        store.open();
    }
    /**
     Updates the details of a shopping product in the store.

     @param email The email of the user requesting the update.
     @param storeName The name of the store containing the product.
     @param catalogID The catalog ID of the product to update.
     @param price The new price of the product.
     @param description The new description of the product.
     @return A ShoppingProductDTO object representing the updated product. */
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
        product.setDescription(description); // assuming description is name; change if needed
        ShoppingProductDTO productDTO = convertProductToDTO(product);
        return productDTO;          
    }
    /**
     Adds a product to the store.

     @param email The email of the user adding the product.
     @param storeName The name of the store to add the product to.
     @param catalogID The catalog ID of the product.
     @param product_name The name of the product.
     @param description The description of the product.
     @param price The price of the product.
     @param quantity The quantity of the product.
     @return A string indicating success or failure. */
    @Override
    public String addProductToStore(String email, String storeName, String catalogID, String product_name,String description, double price,
                                  int quantity) {
        Store store = findByName(storeName);
        if (store.isOwnerOrHasManagerPermissions(email)) {
            return store.addProductToStore(email, storeName, catalogID, product_name, description, price, quantity);
        }
        return null;


    }
    /**
     Finds a store by its name.

     @param name The name of the store to find.
     @return The Store object representing the store. */
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

    /**
     Checks if a store with the specified name exists.

     @param name The name of the store to check.
     @throws RuntimeException if the store does not exist. */
    public void checkIfExist(String name) {
        boolean exists = stores.stream().anyMatch(store -> store.getName().equals(name));
        if (!exists) {
            throw new RuntimeException("Store does not exist");
        }
    }

    /**
     Checks if a store with the specified name exists.

     @param name The name of the store to check.
     @return true if the store exists, false otherwise. */
    public boolean isStoreExist(String name) {
        return stores.stream().anyMatch(store -> store.getName().equals(name));
    }


    /**
     Appoints a new owner to a store.

     @param storeName The name of the store.
     @param appointerEmail The email of the current owner appointing the new owner.
     @param newOwnerEmail The email of the new owner. */
    @Override
    public void appointOwner(String storeName, String appointerEmail, String newOwnerEmail) {
        Store store = findByName(storeName);
        store.appointOwner(appointerEmail, newOwnerEmail);
    }

    /**
     Removes an owner from a store.

     @param storeName The name of the store.
     @param removerEmail The email of the current owner removing the owner.
     @param ownerToRemove The email of the owner to remove. */
    @Override
    public void removeOwner(String storeName, String removerEmail, String ownerToRemove) {
        Store store = findByName(storeName);
        store.removeOwner(removerEmail, ownerToRemove);
    }

    /**
     Resigns ownership of a store.

     @param storeName The name of the store.
     @param ownerEmail The email of the owner resigning. */
    @Override
    public void resignOwnership(String storeName, String ownerEmail) {
        Store store = findByName(storeName);
        store.resignOwnership(ownerEmail);
    }

    /**
     Appoints a manager to a store with specified permissions.

     @param storeName The name of the store.
     @param ownerEmail The email of the owner appointing the manager.
     @param managerEmail The email of the manager to appoint.
     @param permissions The list of permissions for the manager. */
    @Override
    public void appointManager(String storeName, String ownerEmail, String managerEmail, List<String> permissions) {
        Store store = findByName(storeName);
        Set<ManagerPermission> permissionSet = new HashSet<>();
        for (String perm : permissions) {
            permissionSet.add(ManagerPermission.valueOf(perm));
        }

        store.appointManager(ownerEmail, managerEmail, permissionSet);
    }

    /**
     Updates the permissions of a store manager.

     @param storeName The name of the store.
     @param ownerEmail The email of the owner updating the permissions.
     @param managerEmail The email of the manager whose permissions are being updated.
     @param newPermissions A list of new permissions for the manager. */
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

    /**
     Retrieves the permissions of a store manager.

     @param storeName The name of the store.
     @param operatorEmail The email of the user requesting the permissions.
     @param managerEmail The email of the manager whose permissions are being retrieved.
     @return A list of permissions granted to the manager. */

    @Override
    public List<String> getManagerPermissions(String storeName, String operatorEmail, String managerEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view manager permissions");
        }
        
        return store.getManagerPermissions(managerEmail);
    }

    /**
     Retrieves all owners of a store.

     @param storeName The name of the store.
     @param operatorEmail The email of the user requesting the list of owners.
     @return A list of emails of all owners of the store. */
    @Override
    public List<String> getAllOwners(String storeName, String operatorEmail) {
        Store store = findByName(storeName);
        
        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view store owners");
        }

        return store.getAllOwners();
    }

    /**
     Retrieves all managers of a store.

     @param storeName The name of the store.
     @param operatorEmail The email of the user requesting the list of managers.
     @return A list of emails of all managers of the store. */
    @Override
    public List<String> getAllManagers(String storeName, String operatorEmail) {
        Store store = findByName(storeName);

        if (!store.isOwner(operatorEmail)
                && !store.hasManagerPermission(operatorEmail, ManagerPermission.MANAGE_ROLES)) {
            throw new RuntimeException("User is not authorized to view managers");
        }

        return store.getAllManagers();
    }




    /**
     Deletes a shopping product from a store.

     @param email The email of the user performing the deletion.
     @param storeName The name of the store.
     @param productID The ID of the product to delete.
     @return A ShoppingProductDTO object representing the deleted product. */
    @Override
    public ShoppingProductDTO deleteShoppingProduct(String email, String storeName, String productID) {
        Store store = findByName(storeName);
        ShoppingProductDTO product = convertProductToDTO(store.getProduct(productID));
        if(store.isOwnerOrHasManagerPermissions(email)){
            store.removeProduct(productID);
        }
        return product;
    }

    /**
     Rates a product in a store.

     @param email The email of the user rating the product.
     @param storeName The name of the store.
     @param productID The ID of the product to rate.
     @param rating The rating given to the product (1-5).
     @param review The review text for the product.
     @return A ShoppingProductDTO object representing the rated product. */
    @Override
    public ShoppingProductDTO rateProduct(String email, String storeName, String productID, int rating, String review) {
        Store store = findByName(storeName);
        ShoppingProduct product = store.getProduct(productID);
        
        if (product == null) {
            throw new RuntimeException("Product not found in store ");
        }
        if (rating == 1 || rating > 5) {
            throw new IllegalArgumentException(("Rating must be between 1-5"));
        }
        product.addRating(email, rating, review);
        ShoppingProductDTO productDTO = convertProductToDTO(product);
        return productDTO;
    }

    /**
     Rates a store.

     @param email The email of the user rating the store.
     @param storeName The name of the store.
     @param rating The rating given to the store (1-5).
     @param review The review text for the store. */
    @Override
    public void rateStore( String email, String storeName, int rating, String review) {
        Store store = findByName(storeName);
        if(!store.isActive())
        {
            throw new RuntimeException("Store is closed - cannot be rated ");
        }
        store.rateStore(email,rating,review);
    }

    /**
     Adds an amount to the store's balance.

     @param operatorEmail The email of the user performing the operation.
     @param storeName The name of the store.
     @param amount The amount to add to the balance. */

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

    /**
     Converts a ShoppingProduct object to a ShoppingProductDTO object.
        @param product The ShoppingProduct object to convert.
        @return A ShoppingProductDTO object representing the product. */

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

    @Override
    public ShoppingProductDTO getProduct(String storeName, String productID) {
        Store store = findByName(storeName);
        ShoppingProduct product = store.getProduct(productID);

        return convertProductToDTO(findByName(storeName).getProduct(productID));
    }
    @Override
    public void submitBidToShoppingItem(String Email,String storeName,String productId,double bidAmount){
        Store store = findByName(storeName);
        store.submitBidToShoppingItem(productId,bidAmount,Email);
    }
    @Override
    public void sendAuctionOffer(String Email,String storeName,String productId,double bidAmount){
        Store store = findByName(storeName);
        store.submitAuctionOffer(productId,bidAmount,Email);
    }

    @Override
    public Integer getProductQuantity(String storeName, String productId){
        Store store = findByName(storeName);
        ShoppingProduct product = store.getProduct(productId);
        if (product == null) {
            throw new RuntimeException("Product not found in store ");
        }
        return product.getQuantity();
    }

}
