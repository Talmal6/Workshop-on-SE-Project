package com.SEGroup.Service;

import java.util.ArrayList;

import com.SEGroup.DTO.ProductDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.*;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.IAuthenticationService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;

import com.SEGroup.Domain.Store.ManagerData;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.StoreRepository;
import com.SEGroup.Infrastructure.LoggerWrapper;

/**
 * StoreService: handles store-related operations (public browsing, management)
 */
public class StoreService {
    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;

    public StoreService(IStoreRepository storeRepository,
            IProductRepository productRepository,
            IAuthenticationService authenticationService,
            IUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    // === Guest / Public Viewing ===

    public Result<StoreDTO> viewStore(String storeName) {
        try {
            return Result.success(storeRepository.getStore(storeName));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<StoreDTO>> viewAllStores() {
        try {
            LoggerWrapper.info("Fetching all public stores.");
            return Result.success(storeRepository.getAllStores());
        } catch (Exception e) {
            LoggerWrapper.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> viewPublicProductCatalog() {
        try {
            return Result.success(productRepository.getAllProducts());
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Authenticated Operations ===

    public Result<Void> createStore(String sessionKey, String storeName) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.createStore(storeName, authenticationService.getUserBySession(sessionKey));
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> closeStore(String sessionKey, String storeName) {
        try {
            storeRepository.closeStore(authenticationService.getUserBySession(sessionKey), storeName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    public Result<Void> reopenStore(String sessionKey, String storeName) {
        try {
            storeRepository.reopenStore(authenticationService.getUserBySession(sessionKey), storeName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
    public Result<Void> addProductToStore(String sessionKey, String category, String storeName, String catalogID, String product_name,String description, double price,
                                          int quantity) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.addProductToStore(authenticationService.getUserBySession(sessionKey), storeName, catalogID,
                   category, product_name ,description, price, quantity);
            //productRepository.addStoreToProduct(catalogID, storeName);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateShoppingProduct(String sessionKey, String storeName, String productID, String description,
            Double price) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.updateShoppingProduct(authenticationService.getUserBySession(sessionKey), storeName,
                    productID, price, description);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> deleteShoppingProduct(String sessionKey, String storeName, String productID) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.deleteShoppingProduct(authenticationService.getUserBySession(sessionKey), storeName,
                    productID);
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

    public Result<Void> rateProduct(String sessionKey, String storeName, int productID, int rating, String review) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            storeRepository.rateProduct(authenticationService.getUserBySession(sessionKey), storeName, productID,
                    rating, review);
            return Result.success(null);
        } catch (Exception e) {
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
            return Result.success(storeRepository.getManagerPermissions(
                    authenticationService.getUserBySession(sessionKey), storeName, managerEmail));
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
