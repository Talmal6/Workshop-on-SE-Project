package com.SEGroup.Service;

import java.util.List;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.ProductCatalog;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.LoggerWrapper;

/**
 * StoreService: handles store-related operations (public browsing, management)
 */
public class StoreService {
    private final IStoreRepository storeRepository;
    private final ProductCatalog productCatalog;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;

    public StoreService(IStoreRepository storeRepository,
            ProductCatalog productCatalog,
            IAuthenticationService authenticationService,
            IUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.productCatalog = productCatalog;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }


    public Result<String> addProductToCatalog(String catalogID, String name, String brand, String description, List<String> categories){
        try {
            productCatalog.addCatalogProduct(catalogID, name, brand, description, categories);
            return Result.success(catalogID);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<StoreDTO> viewStore(String storeName) {
        try {
            return Result.success(storeRepository.getStore(storeName));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    // === Guest / Public Viewing === 
    public Result<List<StoreDTO>> viewAllStores() {
        try {
            LoggerWrapper.info("Fetching all public stores.");
            return Result.success(storeRepository.getAllStores());
        } catch (Exception e){
            LoggerWrapper.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<CatalogProduct>> viewPublicProductCatalog() {
        try {
            return Result.success(productCatalog.getAllProducts());
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Authenticated Operations ===

    public Result<Void> createStore(String sessionKey, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Creating store: " + storeName);
            storeRepository.createStore(storeName, authenticationService.getUserBySession(sessionKey));
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> closeStore(String sessionKey, String storeName) {
        try {
            StoreDTO storeDTO = storeRepository.getStore(storeName);
            storeRepository.closeStore(storeName, authenticationService.getUserBySession(sessionKey));
            for(ShoppingProductDTO sp : storeDTO.getProducts()){
                productCatalog.deleteStoreProductEntry(sp.getCatalogID(), storeName, sp.getProductId());
            }
            return Result.success(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.failure(e.getMessage());
        }
    }
    public Result<Void> reopenStore(String sessionKey, String storeName) {
        try {
            storeRepository.reopenStore(storeName, authenticationService.getUserBySession(sessionKey));
            for(ShoppingProductDTO sp : storeRepository.getStore(storeName).getProducts()){
                productCatalog.addStoreProductEntry(sp.getCatalogID(), storeName, sp.getProductId(), sp.getPrice(), sp.getQuantity(), sp.getAvgRating());
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    public Result<Void> addProductToStore(String sessionKey, String storeName, String catalogID, String productName ,String description, double price, int quantity) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            productCatalog.isProductExist(catalogID);
            storeRepository.addProductToStore(authenticationService.getUserBySession(sessionKey), storeName, catalogID, productName ,description, price, quantity);
            //productRepository.addStoreToProduct(catalogID, storeName);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateShoppingProduct(String sessionKey, String storeName, String productID, String description, Double price) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository.updateShoppingProduct(authenticationService.getUserBySession(sessionKey), storeName, productID, price, description);
            productCatalog.updateStoreProductEntry(sp.getCatalogID(), storeName, productID, price, null, null);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> deleteShoppingProduct(String sessionKey, String storeName, String productID) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository.deleteShoppingProduct(authenticationService.getUserBySession(sessionKey), storeName, productID);
            productCatalog.deleteStoreProductEntry(sp.getCatalogID(), storeName, productID);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> rateStore(String sessionKey, String storeName, int rating, String review) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.rateStore(authenticationService.getUserBySession(sessionKey), storeName, rating, review);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> rateProduct(String sessionKey, String storeName, String productID, int rating, String review) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            ShoppingProductDTO sp = storeRepository.rateProduct(authenticationService.getUserBySession(sessionKey), storeName, productID, rating, review);
            productCatalog.updateStoreProductEntry(sp.getCatalogID(), storeName, productID, null, null, sp.getAvgRating());
            return Result.success(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Result.failure(e.getMessage());
        }
    }

    // === Roles-Related Operations ===

    public Result<Void> appointOwner(String sessionKey, String storeName, String apointeeEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.appointOwner(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail);
            userRepository.appointOwner(storeName, apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> removeOwner(String sessionKey, String storeName, String apointeeEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.removeOwner(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail);
            userRepository.removeOwner(storeName, apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> resignOwnership(String sessionKey, String storeName) {
        try {
            authenticationService.authenticate(sessionKey);
            String userEmail = authenticationService.getUserBySession(sessionKey);
            storeRepository.resignOwnership(storeName, userEmail);
            userRepository.removeOwner(storeName, userEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> appointManager(String sessionKey, String storeName, String apointeeEmail,
            List<String> permissions) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.appointManager(storeName, authenticationService.getUserBySession(sessionKey), apointeeEmail,
                    permissions);
            userRepository.appointManager(storeName, apointeeEmail);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateManagerPermissions(String sessionKey, String storeName, String apointeeEmail,
            List<String> permissions) {
        try {
            authenticationService.authenticate(sessionKey);
            storeRepository.updateManagerPermissions(storeName, authenticationService.getUserBySession(sessionKey),
                    apointeeEmail, permissions);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<String>> getManagerPermission(String sessionKey, String storeName, String managerEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(storeRepository.getManagerPermissions(storeName,
                    authenticationService.getUserBySession(sessionKey), managerEmail));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<String>> getAllOwners(String sessionKey, String storeName, String operatorEmail) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(
                    storeRepository.getAllOwners(storeName, authenticationService.getUserBySession(sessionKey)));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<String>> getAllManagers(String sessionKey, String storeName, String operatorEmai) {
        try {
            authenticationService.authenticate(sessionKey);
            return Result.success(
                    storeRepository.getAllManagers(storeName, authenticationService.getUserBySession(sessionKey)));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    // public Result<List<StoreProductDTO>> searchProducts(String query, String
    // categories, String sortBy) {
    // try {
    // List<> productsLikeQuery =
    // productRepository.searchByNameOrDescription(query.toLowerCase());
    // List<ShoppingProduct> actualProductsFiltered =
    // return Result.success(matches);
    // } catch (Exception e) {
    // return Result.failure(e.getMessage());
    // }
    // }

    // public Result<Product> getProduct(String sessionKey, String storeName, String
    // shoppingProductId) {
    // try {
    // authenticationService.checkSessionKey(sessionKey);
    // storeRepository.checkIfExist(storeName);
    // Product product = productRepository.getProduct(shoppingProductId);
    // return Result.success(product);
    // } catch (Exception e) {
    // return Result.failure(e.getMessage());
    // }
    // }

    // public Result<Void> addToStoreBalance(String sessionKey,
    // String storeName,
    // double amount ) {
    // try {
    // authenticationService.checkSessionKey(sessionKey);
    // storeRepository.addToBalance(authenticationService.getUserBySession(sessionKey),
    // storeName, amount);
    // return Result.success(null);
    // } catch (Exception e) {
    // return Result.failure(e.getMessage());
    // }
    // }

    // public Result<List<Product>> getStoreProductsWithQuery(String storeName,
    // String query, List<String> filters, String sortBy) {
    // try {
    // authenticationService.checkSessionKey(sessionKey);

    // storeRepository.checkIfExist(storeName);
    // List<Product> matches = productRepository.searchInStore(storeName, query);
    // storeRepostity
    // return Result.success(matches);
    // } catch (Exception e) {
    // return Result.failure(e.getMessage());
    // }
    // }
}

// ProductCatalog{
// Map<String, Product> = {
// 'phones' : ["apple-iphone-13" , "apple-iphone-14", "samsung"]
// 'shoes' : ['nike-air1', 'nike-air2']
// ....
// }
// Map<CatalogID, storeNames> : {
// "apple-iphone-13": ["ron-store" , "gil-store"]
// "apple-iphone-14": ["gil-store" , "amit-store"]
// }
// }

// ShoppingProduct{
// CataglogID
// InStoreID
// name
// extraDescription
// Policies
// quantity
// price
// }

// Product{
// String "apple-iphone-13"
// Description "iphone"
// }

// ShoppingProduct

// iphone
