package com.SEGroup.Service;

import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.StoreDTO;
import com.SEGroup.Domain.IProductRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Domain.Product;
import java.util.List;

public class StoreService {
    private IStoreRepository storeRepository;
    private IProductRepository productRepository;
    private IAuthenticationService authenticationService;

    public StoreService(IStoreRepository storeRepository, IProductRepository productRepository, IAuthenticationService authenticationService) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.authenticationService = authenticationService;
    }

    public void addProduct(String sessionKey, String storeName, String productName, double price) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = this.storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }

        //store.addProduct(productName, price);
        // Logic for adding a product to the store
        // Making Connection Between Store and its Products in the repository
        //productRepository.addProduct(productName, storename);
    }

    public void updateProduct(String sessionKey, String storeName, String productName, double newPrice) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = this.storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }
        // Logic for updating a product in the store
        //store.updateProduct(productName, newPrice);
        // Making Connection Between Store and its Products in the repository
        //productRepository.updateProduct(productName, storename, newPrice);
    }

    public void deleteProduct(String sessionKey, String storeName, String productName) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = this.storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }
        // Logic for deleting a product from the store
        //store.deleteProduct(productName);
        // Making Connection Between Store and its Products in the repository
        //productRepository.deleteProduct(productName, storename);
    }

    public void createStore(String sessionKey, String storeName, String ownerEmail) {
        authenticationService.checkSessionKey(sessionKey);

        if (storeRepository.existsByName(storeName)) {
            throw new IllegalArgumentException("Store already exists: " + storeName);
        }
        StoreDTO store = new StoreDTO(storeName, ownerEmail);
        storeRepository.addStore(store);
    }

    public void updateStoreName(String sessionKey, String storeName, String newStoreName) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }

        // Logic for updating the store name
        //store.setName(newStoreName);
        // Making Connection Between Store and its Products in the repository
        //storeRepository.updateStore(store);
    }

    public void deleteStore(String sessionKey, String storeName) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }

        // Logic for deleting the store
        // using authentication to check if the user is authorized to delete the store
        //storeRepository.deleteStore(store.getId());
    }

    public void viewStore(String sessionKey, String storeName) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }

        // Logic for viewing the store details
    }

    public void viewAllStores(String sessionKey) {
        authenticationService.checkSessionKey(sessionKey);

        // Logic for viewing all stores
        // This could be a simple retrieval of all stores from the repository
        // List<Store> allStores = storeRepository.getAllStores();
        // return allStores;
    }

    public void viewStoreProducts(String sessionKey, String storeName) {
        authenticationService.checkSessionKey(sessionKey);

        StoreDTO store = storeRepository.findByName(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Store not found: " + storeName);
        }

        // Logic for viewing all products in the store
        // List<Product> products = productRepository.getProductsByStore(store.getId());
        // return products;
    }

    public void viewProductCatalog(String sessionKey) {
        authenticationService.checkSessionKey(sessionKey);

        // Logic for viewing the product catalog of the store
        List<Product> catalog = productRepository.getAllProducts();
        // return catalog;
    }
}
