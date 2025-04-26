package com.SEGroup.Domain;

import java.util.List;
import java.util.Map;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;

public interface IStoreRepository {
    List<StoreDTO> getAllStores();

    StoreDTO getStore(String storeName);

    void createStore(String StoreName, String founderEmail);

    void closeStore(String name, String founderEmail);

    void reopenStore(String storeName, String founderEmail);
// omri funcitions
    void addProductToStore(String email, String storeName, String catalogID, String product_name,String description, double price, int quantity);

    ShoppingProductDTO updateShoppingProduct(String email, String storeName, String catalogID, double price, String description);

    ShoppingProductDTO deleteShoppingProduct(String email, String storeName, String productID);

    ShoppingProductDTO rateProduct(String email, String storeName, String productID, int rating, String review);

    void rateStore(String email, String storeName, int rating, String review);

    // Ownership and manager operations with operator email authorization
    void appointOwner(String storeName, String operatorEmail, String newOwnerEmail);

    void removeOwner(String storeName, String operatorEmail, String ownerToRemove);

    void resignOwnership(String storeName, String operatorEmail);

    void appointManager(String storeName, String operatorEmail, String managerEmail, List<String> permissions);

    void updateManagerPermissions(String storeName, String operatorEmail, String managerEmail,
            List<String> newPermissions);

    List<String> getManagerPermissions(String storeName, String operatorEmail, String managerEmail);

    List<String> getAllOwners(String storeName, String operatorEmail);

    List<String> getAllManagers(String storeName, String operatorEmail);

    void addToBalance(String userBySession, String storeName, double amount);
    //return basket_store_id,price
    Map<BasketDTO, Double> removeItemsFromStores(List<BasketDTO> basketDTOList);

    void rollBackItemsToStores(List<BasketDTO> basketDTO);

    


}