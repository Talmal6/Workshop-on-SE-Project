package com.SEGroup.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    // Use Case 2.1a - View market stores
    @GetMapping("/active")
    public ResponseEntity<?> viewActiveStores() {
        Result<List<String>> result = storeService.viewActiveStores();
        return ResponseEntity.ok(result.getData());
    }

    // Use Case 2.1b - View products in a specific store
    @GetMapping("/{storeId}/products") // localhost/api/12876328/products
    public ResponseEntity<?> viewStoreProducts(@PathVariable String storeId) {
        Result<List<String>> result = storeService.viewProducts(storeName);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // Use Case 2.3 - Add product to cart
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam String email, @RequestParam String product) {
        Result<Void> result = storeService.addToCart(email, product);
        if (result.isSuccess()) return ResponseEntity.ok("Product added to cart");
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // Use Case 2.4 - Modify cart
    @PostMapping("/cart/modify")
    public ResponseEntity<?> modifyCart(@RequestParam String email, @RequestParam String product, @RequestParam int quantity) {
        Result<List<String>> result = storeService.modifyCart(email, product, quantity);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // Use Case 2.5 - Purchase cart
    @PostMapping("/cart/purchase")
    public ResponseEntity<?> purchaseCart(@RequestParam String email) {
        Result<String> result = storeService.purchaseCart(email);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // Use Case 3.2 - Create store
    @PostMapping("/create")
    public ResponseEntity<?> createStore(@RequestParam String email, @RequestParam String storeName) {
        Result<Void> result = storeService.createStore(email, storeName);
        if (result.isSuccess()) return ResponseEntity.ok("Store created");
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // Use Case 3.4 - Review product/store
    // @PostMapping("/review")
    // public ResponseEntity<?> reviewProduct(@RequestParam String email, @RequestParam String targetId, @RequestParam String review) {
    //     Result<Void> result = storeService.reviewProduct(email, targetId, review);
    //     if (result.isSuccess()) return ResponseEntity.ok("Review submitted");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 3.5 - Send request/question
    // @PostMapping("/inquiry")
    // public ResponseEntity<?> sendInquiry(@RequestParam String email, @RequestParam String storeId, @RequestParam String message) {
    //     Result<Void> result = storeService.sendInquiry(email, storeId, message);
    //     if (result.isSuccess()) return ResponseEntity.ok("Inquiry sent");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 3.9 - Submit purchase offer
    // @PostMapping("/bid")
    // public ResponseEntity<?> submitBid(@RequestParam String email, @RequestParam String productId, @RequestParam double offer) {
    //     Result<Void> result = storeService.submitBid(email, productId, offer);
    //     if (result.isSuccess()) return ResponseEntity.ok("Bid submitted");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 3.10 - Buy in auction
    // @PostMapping("/auction")
    // public ResponseEntity<?> submitAuction(@RequestParam String email, @RequestParam String productId, @RequestParam double bidAmount) {
    //     Result<Void> result = storeService.submitAuction(email, productId, bidAmount);
    //     if (result.isSuccess()) return ResponseEntity.ok("Auction bid submitted");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.1 - Manage inventory
    // @PostMapping("/inventory/manage")
    // public ResponseEntity<?> manageInventory(@RequestParam String store, @RequestParam String action, @RequestParam String product) {
    //     Result<Void> result = storeService.manageInventory(store, action, product);
    //     if (result.isSuccess()) return ResponseEntity.ok("Inventory updated");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.2 - Change policies
    // @PostMapping("/policy/update")
    // public ResponseEntity<?> updatePolicy(@RequestParam String store, @RequestParam String policy) {
    //     Result<Void> result = storeService.updatePolicy(store, policy);
    //     if (result.isSuccess()) return ResponseEntity.ok("Policy updated");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.3 - Appoint co-owner
    // @PostMapping("/owner/appoint")
    // public ResponseEntity<?> appointCoOwner(@RequestParam String store, @RequestParam String email) {
    //     Result<Void> result = storeService.appointCoOwner(store, email);
    //     if (result.isSuccess()) return ResponseEntity.ok("Co-owner appointed");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.4 - Remove co-owner
    // @PostMapping("/owner/remove")
    // public ResponseEntity<?> removeCoOwner(@RequestParam String store, @RequestParam String email) {
    //     Result<Void> result = storeService.removeCoOwner(store, email);
    //     if (result.isSuccess()) return ResponseEntity.ok("Co-owner removed");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.6 - Appoint store manager
    // @PostMapping("/manager/appoint")
    // public ResponseEntity<?> appointManager(@RequestParam String store, @RequestParam String email) {
    //     Result<Void> result = storeService.appointManager(store, email);
    //     if (result.isSuccess()) return ResponseEntity.ok("Manager appointed");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.7 - Change manager permissions
    // @PostMapping("/manager/permissions")
    // public ResponseEntity<?> updateManagerPermissions(@RequestParam String store, @RequestParam String email, @RequestParam String permissions) {
    //     Result<Void> result = storeService.updateManagerPermissions(store, email, permissions);
    //     if (result.isSuccess()) return ResponseEntity.ok("Permissions updated");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.9 - Close store
    // @PostMapping("/close")
    // public ResponseEntity<?> closeStore(@RequestParam String store) {
    //     Result<Void> result = storeService.closeStore(store);
    //     if (result.isSuccess()) return ResponseEntity.ok("Store closed");
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.11 - View roles and permissions
    // @GetMapping("/roles")
    // public ResponseEntity<?> viewRoles(@RequestParam String store) {
    //     Result<?> result = storeService.viewRoles(store);
    //     if (result.isSuccess()) return ResponseEntity.ok(result.getData());
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.12 - Manage inquiries
    // @GetMapping("/inquiries")
    // public ResponseEntity<?> getInquiries(@RequestParam String store) {
    //     Result<?> result = storeService.getInquiries(store);
    //     if (result.isSuccess()) return ResponseEntity.ok(result.getData());
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }

    // // Use Case 4.13 - View store purchase history
    // @GetMapping("/purchases")
    // public ResponseEntity<?> getStorePurchaseHistory(@RequestParam String store) {
    //     Result<?> result = storeService.getPurchaseHistory(store);
    //     if (result.isSuccess()) return ResponseEntity.ok(result.getData());
    //     return ResponseEntity.badRequest().body(result.getErrorMessage());
    // }
}
