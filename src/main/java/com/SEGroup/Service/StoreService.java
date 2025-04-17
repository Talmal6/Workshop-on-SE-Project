package com.SEGroup.Service;

import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.IAuthenticationService;
import java.util.List;

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
    public Result<List<StoreDTO>> viewPublicStores() {
        try {
            List<StoreDTO> allStores = storeRepository.getAllStores();
            return Result.success(allStores);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> viewPublicProductCatalog() {
        try {
            List<ProductDTO> catalog = productRepository.getAllProducts();
            return Result.success(catalog);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> viewPublicStoreProducts(String storeName) {
        try {
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            List<ProductDTO> products = productRepository.getProductsByStoreName(storeName);
            return Result.success(products);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Authenticated Operations ===
    private void ensureAuthenticated(String sessionKey) {
        authenticationService.checkSessionKey(sessionKey);
    }

    public Result<Void> addProduct(String sessionKey,
                                   String storeName,
                                   String productName,
                                   double price) {
        try {
            ensureAuthenticated(sessionKey);
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            ProductDTO product = new ProductDTO(productName);
            productRepository.addProduct(product);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> updateProduct(String sessionKey,
                                      String storeName,
                                      String productName,
                                      double newPrice) {
        try {
            ensureAuthenticated(sessionKey);
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            productRepository.updateProduct(productName, storeName, newPrice);
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
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
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
            if (storeRepository.existsByName(storeName)) {
                return Result.failure("Store already exists: " + storeName);
            }
            StoreDTO store = new StoreDTO(storeName, ownerEmail);
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
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            store.setName(newStoreName);
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
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            storeRepository.deleteStore(store.getName());
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<StoreDTO> viewStore(String sessionKey,
                                      String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            return Result.success(store);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<StoreDTO>> viewAllStores(String sessionKey) {
        try {
            ensureAuthenticated(sessionKey);
            List<StoreDTO> allStores = storeRepository.getAllStores();
            return Result.success(allStores);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> viewStoreProducts(String sessionKey,
                                                       String storeName) {
        try {
            ensureAuthenticated(sessionKey);
            StoreDTO store = storeRepository.findByName(storeName);
            if (store == null) {
                return Result.failure("Store not found: " + storeName);
            }
            List<ProductDTO> products = productRepository.getProductsByStoreName(storeName);
            return Result.success(products);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> viewProductCatalog(String sessionKey) {
        try {
            ensureAuthenticated(sessionKey);
            List<ProductDTO> catalog = productRepository.getAllProducts();
            return Result.success(catalog);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    // === Search/Browse ===
    public Result<List<ProductDTO>> searchProducts(String sessionKey, String query) {
        try {
            ensureAuthenticated(sessionKey);
            List<ProductDTO> matches = productRepository.searchProducts(query);
            return Result.success(matches);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<ProductDTO>> searchProductsInStore(String sessionKey,
                                                           String storeName,
                                                           String query) {
        try {
            ensureAuthenticated(sessionKey);
            List<ProductDTO> matches = productRepository.searchInStore(storeName, query);
            return Result.success(matches);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
