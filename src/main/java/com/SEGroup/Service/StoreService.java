package com.SEGroup.Service;

import com.SEGroup.Domain.*;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.IAuthenticationService;
import java.util.List;

import javax.naming.AuthenticationException;

/**
 * StoreService: handles store-related operations (public browsing, management)
 */
public class StoreService {
    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;
    private final IAuthenticationService authenticationService;

    public StoreService(IStoreRepository storeRepository,
                        IProductRepository productRepository,
                        IAuthenticationService authenticationService) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.authenticationService = authenticationService;
    }

    // === Guest / Public Viewing ===
    public Result<List<Store>> viewPublicStores() {
        try {
            List<Store> allStores = storeRepository.getAllStores();
            return Result.success(allStores);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Product>> viewPublicProductCatalog() {
        try {
            List<Product> catalog = productRepository.getAllProducts();
            return Result.success(catalog);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Product>> viewPublicStoreProducts(String storeName) {
        try {
            storeRepository.checkIfExist(storeName);
            List<Product> products = productRepository.getProductsByStoreName(storeName);
            return Result.success(products);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Authenticated Operations ===
    private void ensureAuthenticated(String sessionKey)  throws AuthenticationException{
        authenticationService.checkSessionKey(sessionKey);
    }

    public Result<Void> addProduct(String sessionKey,
                                   String storeName,
                                   String productName,
                                   double price) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            productRepository.addProduct(productName,storeName, price);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateProduct(String sessionKey,
                                      String storeName,
                                      String productId,
                                      String productName,
                                      double price) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            Product product = productRepository.findById(productId);
            productRepository.updateProduct(product);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<Void> deleteProduct(String sessionKey,
                                      String storeName,
                                      String productName) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            productRepository.deleteProduct(productName, storeName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> createStore(String sessionKey,
                                    String storeName,
                                    String ownerEmail) {
        try {
            ensureAuthenticated(sessionKey);
            Store store = new Store(storeName, ownerEmail);
            storeRepository.addStore(store);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateStoreName(String sessionKey,
                                        String storeName,
                                        String newStoreName) {
        try {
            ensureAuthenticated(sessionKey);
            Store store = storeRepository.findByName(storeName);
            storeRepository.changeStoreName(storeName,newStoreName);
            storeRepository.updateStore(store);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> deleteStore(String sessionKey,
                                    String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            storeRepository.deleteStore(storeName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Store> viewStore(String sessionKey,
                                      String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            Store store = storeRepository.findByName(storeName);
            return Result.success(store);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Store>> viewAllStores(String sessionKey) {
        try {
            ensureAuthenticated(sessionKey);
            List<Store> allStores = storeRepository.getAllStores();
            return Result.success(allStores);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Product>> viewStoreProducts(String sessionKey,
                                                       String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            List<Product> products = productRepository.getProductsByStoreName(storeName);
            return Result.success(products);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Product>> viewProductCatalog(String sessionKey) {
        try {
            ensureAuthenticated(sessionKey);
            List<Product> catalog = productRepository.getAllProducts();
            return Result.success(catalog);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Search/Browse ===
    public Result<List<Product>> searchProducts(String sessionKey, String query) {
        try {
            ensureAuthenticated(sessionKey);
            List<Product> matches = productRepository.searchProducts(query);
            return Result.success(matches);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Product>> searchProductsInStore(String sessionKey,
                                                           String storeName,
                                                           String query) {
        try {
            ensureAuthenticated(sessionKey);
            List<Product> matches = productRepository.searchInStore(storeName, query);
            return Result.success(matches);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<Product> getProduct(String sessionKey, String storeName, String shoppingProductId) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            Product product = productRepository.getProduct(shoppingProductId);
            return Result.success(product);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    //3.4
    public Result<Void> rateProduct(String sessionKey,
                                   String storeName,
                                   String productName,
                                   int rating,
                                   String review) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            Product product = productRepository.findById(productName);
            product.addReview(rating, review);
            productRepository.updateProduct(product);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> addToStoreBalance(String sessionKey,
                                        String storeName,
                                        double amount ) {
        try {
            ensureAuthenticated(sessionKey);
            Store store = storeRepository.findByName(storeName);
            store.addToBalance(amount);
            storeRepository.updateStore(store);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<Void> checkIfStoreExist(String sessionKey, String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    //3.3
    public Result<Void> rateStore(String sessionKey,
                                  String storeName,
                                  int rating,
                                  String review) {
        try {
            ensureAuthenticated(sessionKey);
            Store store = storeRepository.findByName(storeName);
            store.rateStore(rating, review);
            storeRepository.updateStore(store);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<List<Product>> getStoreProductsWithQuery(String sessionKey, String storeName, String query) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            List<Product> matches = productRepository.searchInStore(storeName, query);
            return Result.success(matches);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    //3.9
    public Result<Void> submitBidToShoppingItem(String sessionKey,
                                                String storeName,
                                                String productId,
                                                double bidAmount,
                                                String bidderEmail) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            productRepository.checkIfExist(productId);
            Store store = storeRepository.findByName(storeName);
            store.submitBidToShoppingItem(productId, bidAmount, bidderEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    //3.11
    public Result<Void> sendAuctionOffer(String sessionKey,
                                                String storeName,
                                                String productId,
                                                double bidAmount,
                                                String bidderEmail) {
        try {
            ensureAuthenticated(sessionKey);
            storeRepository.checkIfExist(storeName);
            productRepository.checkIfExist(productId);
            Store store = storeRepository.findByName(storeName);
            store.submitAuctionOffer(productId, bidAmount, bidderEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}

 
