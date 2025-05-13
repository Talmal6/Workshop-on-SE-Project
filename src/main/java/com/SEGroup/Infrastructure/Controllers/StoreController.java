package com.SEGroup.Infrastructure.Controllers;

import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.Result;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.DTO.ShoppingProductDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // === Public endpoints ===

    /** 1. Add a product to the global catalog */
    @PostMapping("/catalog")
    public ResponseEntity<String> addProductToCatalog(
            @RequestParam String catalogID,
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam String description,
            @RequestParam List<String> categories
    ) {
        Result<String> r = storeService.addProductToCatalog(
            catalogID, name, brand, description, categories
        );
        if (r.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(r.getData());
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(r.getErrorMessage());
        }
    }

    /** 2. View a single store by name */
    @GetMapping("/{storeName}")
    public ResponseEntity<StoreDTO> viewStore(@PathVariable String storeName) {
        Result<StoreDTO> r = storeService.viewStore(storeName);
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /** 3. List all public stores */
    @GetMapping("/all")
    public ResponseEntity<List<StoreDTO>> viewAllStores() {
        Result<List<StoreDTO>> r = storeService.viewAllStores();
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /** 4. List the public product catalog */
    @GetMapping("/catalog")
    public ResponseEntity<List<CatalogProduct>> viewPublicProductCatalog() {
        Result<List<CatalogProduct>> r = storeService.viewPublicProductCatalog();
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // === Authenticated store management ===

    /** 5. Create a new store */
    @PostMapping("/create")
    public ResponseEntity<Void> createStore(
            @RequestParam String sessionKey,
            @RequestParam String storeName
    ) {
        Result<Void> r = storeService.createStore(sessionKey, storeName);
        return r.isSuccess()
                ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /** 6. Close an existing store */
    @PostMapping("/close")
    public ResponseEntity<Void> closeStore(
            @RequestParam String sessionKey,
            @RequestParam String storeName
    ) {
        Result<Void> r = storeService.closeStore(sessionKey, storeName);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    /** 7. Reopen a closed store */
    @PostMapping("/reopen")
    public ResponseEntity<Void> reopenStore(
            @RequestParam String sessionKey,
            @RequestParam String storeName
    ) {
        Result<Void> r = storeService.reopenStore(sessionKey, storeName);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    // === Store product operations ===

    /** 8. Add a product instance to a specific store */
    @PostMapping("/{storeName}/product")
    public ResponseEntity<String> addProductToStore(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String catalogID,
            @RequestParam String productName,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam String imageURL
    ) {
        Result<String> r = storeService.addProductToStore(
            sessionKey, storeName, catalogID, productName, description, price, quantity, imageURL
        );
        if (r.isSuccess()) {
            return ResponseEntity.ok(r.getData());
        } else {
            return ResponseEntity.badRequest().body(r.getErrorMessage());
        }
    }

    /** 9. Update a product in the store */
    @PutMapping("/{storeName}/product/{productID}")
    public ResponseEntity<Void> updateShoppingProduct(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double price
    ) {
        Result<Void> r = storeService.updateShoppingProduct(
            sessionKey, storeName, productID, description, price
        );
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    /** 10. Delete a product from the store */
    @DeleteMapping("/{storeName}/product/{productID}")
    public ResponseEntity<Void> deleteShoppingProduct(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID
    ) {
        Result<Void> r = storeService.deleteShoppingProduct(sessionKey, storeName, productID);
        return r.isSuccess()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.badRequest().build();
    }

    // === Ratings ===

    /** 11. Rate a store */
    @PostMapping("/{storeName}/rate")
    public ResponseEntity<Void> rateStore(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam int rating,
            @RequestParam String review
    ) {
        Result<Void> r = storeService.rateStore(sessionKey, storeName, rating, review);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    /** 12. Rate a product */
    @PostMapping("/{storeName}/product/{productID}/rate")
    public ResponseEntity<Void> rateProduct(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID,
            @RequestParam int rating,
            @RequestParam String review
    ) {
        Result<Void> r = storeService.rateProduct(sessionKey, storeName, productID, rating, review);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    // === Roles (owners & managers) ===

    @PostMapping("/{storeName}/owner")
    public ResponseEntity<Void> appointOwner(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String apointeeEmail
    ) {
        Result<Void> r = storeService.appointOwner(sessionKey, storeName, apointeeEmail);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{storeName}/owner")
    public ResponseEntity<Void> removeOwner(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String apointeeEmail
    ) {
        Result<Void> r = storeService.removeOwner(sessionKey, storeName, apointeeEmail);
        return r.isSuccess()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{storeName}/owner/resign")
    public ResponseEntity<Void> resignOwnership(
            @RequestParam String sessionKey,
            @PathVariable String storeName
    ) {
        Result<Void> r = storeService.resignOwnership(sessionKey, storeName);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{storeName}/manager")
    public ResponseEntity<Void> appointManager(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String apointeeEmail,
            @RequestParam List<String> permissions
    ) {
        Result<Void> r = storeService.appointManager(sessionKey, storeName, apointeeEmail, permissions);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{storeName}/manager/permissions")
    public ResponseEntity<Void> updateManagerPermissions(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String apointeeEmail,
            @RequestParam List<String> permissions
    ) {
        Result<Void> r = storeService.updateManagerPermissions(sessionKey, storeName, apointeeEmail, permissions);
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @GetMapping("/{storeName}/manager/permissions")
    public ResponseEntity<List<String>> getManagerPermission(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @RequestParam String managerEmail
    ) {
        Result<List<String>> r = storeService.getManagerPermission(sessionKey, storeName, managerEmail);
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/{storeName}/owners")
    public ResponseEntity<List<String>> getAllOwners(
            @RequestParam String sessionKey,
            @PathVariable String storeName
    ) {
        Result<List<String>> r = storeService.getAllOwners(sessionKey, storeName, null);
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/{storeName}/managers")
    public ResponseEntity<List<String>> getAllManagers(
            @RequestParam String sessionKey,
            @PathVariable String storeName
    ) {
        Result<List<String>> r = storeService.getAllManagers(sessionKey, storeName, null);
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // === Search & auctions ===

    @GetMapping("/{storeName}/products/search")
    public ResponseEntity<List<ShoppingProductDTO>> searchProducts(
            @RequestParam String query,
            @RequestParam List<String> searchFilters,
            @PathVariable String storeName,
            @RequestParam List<String> categories
    ) {
        Result<List<ShoppingProductDTO>> r = storeService.searchProducts(
            query, searchFilters, storeName, categories
        );
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/{storeName}/product/{productID}/bid")
    public ResponseEntity<Void> submitBid(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID,
            @RequestParam double bidAmount,
            @RequestParam int quentity
    ) {
        Result<Void> r = storeService.submitBidToShoppingItem(
            sessionKey, storeName, productID, bidAmount, quentity
        );
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{storeName}/product/{productID}/offer")
    public ResponseEntity<Void> sendAuctionOffer(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID,
            @RequestParam double bidAmount,
            @RequestParam Integer quantity
    ) {
        Result<Void> r = storeService.sendAuctionOffer(
            sessionKey, storeName, productID, bidAmount, quantity
        );
        return r.isSuccess()
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    // === Single‐product queries ===

    @GetMapping("/{storeName}/product/{productID}/quantity")
    public ResponseEntity<Integer> getProductQuantity(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID
    ) {
        Result<Integer> r = storeService.getProductQuantity(sessionKey, storeName, productID);
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/{storeName}/product/{productID}")
    public ResponseEntity<ShoppingProductDTO> getProductFromStore(
            @RequestParam String sessionKey,
            @PathVariable String storeName,
            @PathVariable String productID
    ) {
        Result<ShoppingProductDTO> r = storeService.getProductFromStore(
            sessionKey, storeName, productID
        );
        return r.isSuccess()
                ? ResponseEntity.ok(r.getData())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}
