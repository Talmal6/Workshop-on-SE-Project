package com.SEGroup.UnitTests.StoreTests;

import com.SEGroup.Domain.Discount.DiscountScope;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Store.*;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTests {
    private Store store;
    private final String founderEmail = "founder@test.com";
    private final String storeName = "TestStore";

    @BeforeEach
    public void setUp() {
        store = new Store(storeName, founderEmail);
    }

    @Test
    @DisplayName("Given new store, when constructed, then id is positive and fields initialized")
    public void Given_NewStore_When_Constructed_Then_FieldsInitialized() {
        assertTrue(store.getId() > 0);
        assertEquals(storeName, store.getName());
        assertEquals(founderEmail, store.getfounderEmail());
        assertTrue(store.isActive());
        assertEquals(0.0, store.getBalance());
    }

    @Test
    @DisplayName("Given store, when setName with valid name, then name is updated")
    public void Given_Store_When_SetNameValid_Then_NameUpdated() {
        store.setName("NewName");
        assertEquals("NewName", store.getName());
    }

    @Test
    @DisplayName("Given store, when setName with null or empty, then name remains unchanged")
    public void Given_Store_When_SetNameInvalid_Then_NameUnchanged() {
        String original = store.getName();
        store.setName(null);
        store.setName("");
        assertEquals(original, store.getName());
    }

    @Test
    @DisplayName("Given store, when addToBalance, then balance increases")
    public void Given_Store_When_AddToBalance_Then_BalanceIncreases() {
        store.addToBalance(50.0);
        assertEquals(50.0, store.getBalance());
    }

    @Test
    @DisplayName("Given store, when close and open, then isActive toggles")
    public void Given_Store_When_CloseOpen_Then_ActiveToggles() {
        store.close();
        assertFalse(store.isActive());
        store.open();
        assertTrue(store.isActive());
    }

    @Test
    @DisplayName("Given owner, when addProductToStore, then product is present in store")
    public void Given_Owner_When_AddProduct_Then_ProductPresent() {
        store.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 10.0, 5, false,"",List.of());
        Collection<ShoppingProduct> products = store.getAllProducts();
        assertEquals(1, products.size());
        ShoppingProduct prod = products.iterator().next();
        assertEquals("CID", prod.getCatalogID());
        assertEquals("Name", prod.getName());
    }

    @Test
    @DisplayName("Given non-owner, when addProductToStore, then authorization exception thrown")
    public void Given_NonOwner_When_AddProduct_Then_Exception() {
        assertThrows(RuntimeException.class,
                () -> store.addProductToStore("notOwner@test.com", storeName, "CID", "Name", "Desc", 10.0, 5, false,"",List.of()));
    }

    @Test
    @DisplayName("Given store with product, when removeProduct, then product is removed")
    public void Given_StoreWithProduct_When_RemoveProduct_Then_ProductRemoved() {
        store.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 10.0, 5, false,"",List.of());
        ShoppingProduct prod = store.getAllProducts().iterator().next();
        store.removeProduct(prod.getProductId());
        assertTrue(store.getAllProducts().isEmpty());
        assertNull(store.getProduct(prod.getProductId()));
    }

//    @Test
//    @DisplayName("Given store with no bids, when submitBidToShoppingItem invalid params, then returns false")
//    public void Given_StoreWithNoProduct_When_SubmitBidInvalid_Then_False() {
//        assertFalse(store.submitBidToShoppingItem("Unknown", 10.0, "bidder@test.com", 1));
//        store.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 10.0, 5, false);
//        ShoppingProduct prod = store.getAllProducts().iterator().next();
//        assertFalse(store.submitBidToShoppingItem(prod.getProductId(), -1.0, "", 1));
//    }

    @Test
    @DisplayName("Given store with product, when submitBidToShoppingItem valid, then returns true")
    public void Given_StoreWithProduct_When_SubmitBidValid_Then_True() {
        store.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 10.0, 5, false,"",List.of());
        ShoppingProduct prod = store.getAllProducts().iterator().next();
        assertTrue(store.submitBidToShoppingItem(prod.getProductId(), 15.0, "bidder@test.com"));
    }

    @Test
    @DisplayName("Given store and no auction, when submitAuctionOffer, then returns false")
    public void Given_Store_When_SubmitAuctionWithoutAuction_Then_False() {
        store.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 10.0, 5, false,"",List.of());
        ShoppingProduct prod = store.getAllProducts().iterator().next();
        assertFalse(store.submitAuctionOffer(prod.getProductId(), 20.0, "bidder@test.com"));
    }

    @Test
    @DisplayName("Given store, when isOwner called, then founder is owner and others are not")
    public void Given_Store_When_IsOwnerCalled_Then_CorrectOwnerCheck() {
        assertTrue(store.isOwner(founderEmail));
        assertFalse(store.isOwner("random@test.com"));
    }

    @Test
    @DisplayName("Given non-owner, when isOwnerOrHasManagerPermissions, then exception thrown")
    public void Given_NonOwner_When_CheckPermissions_Then_Exception() {
        assertThrows(RuntimeException.class, () -> store.isOwnerOrHasManagerPermissions("random@test.com"));
    }

    @Test
    @DisplayName("Given founder, when appointOwner valid, then new owner is recognized")
    public void Given_Founder_When_AppointOwner_Then_NewOwnerRecognized() {
        assertTrue(store.appointOwner(founderEmail, "owner2@test.com", false));
        List<String> owners = store.getAllOwners();
        assertTrue(owners.contains(founderEmail));
        assertTrue(owners.contains("owner2@test.com"));
    }

    @Test
    @DisplayName("Given non-founder, when appointOwner, then IllegalArgumentException")
    public void Given_NonFounder_When_AppointOwner_Then_Exception() {
        assertThrows(IllegalArgumentException.class,
                () -> store.appointOwner("notFounder@test.com", "owner2@test.com", false));
    }

    @Test
    @DisplayName("Given store with owners, when removeOwner valid, then owner removed")
    public void Given_StoreWithOwners_When_RemoveOwner_Then_Removed() {
        store.appointOwner(founderEmail, "owner2@test.com", false);
        assertTrue(store.removeOwner(founderEmail, "owner2@test.com",false));
        assertFalse(store.getAllOwners().contains("owner2@test.com"));
    }

    @Test
    @DisplayName("Given founder, when resignOwnership for non-founder owner, then returns true")
    public void Given_Founder_When_ResignOwnership_Then_ReturnsTrue() {
        store.appointOwner(founderEmail, "owner2@test.com", false);
        assertTrue(store.resignOwnership("owner2@test.com"));
        assertFalse(store.getAllOwners().contains("owner2@test.com"));
    }

    @Test
    @DisplayName("Given no ratings, when averageRating, then returns 0.0")
    public void Given_NoRatings_When_AverageRating_Then_Zero() {
        assertEquals(0.0, store.averageRating());
    }

    @Test
    @DisplayName("Given valid ratings, when rateStore and averageRating, then correct average and hasRated true")
    public void Given_ValidRatings_When_RateStore_Then_AverageAndHasRated() {
        store.rateStore("r1@test.com", 4, "Good");
        store.rateStore("r2@test.com", 2, "Bad");
        assertTrue(store.hasRated("r1@test.com"));
        assertTrue(store.hasRated("r2@test.com"));
        assertEquals(3.0, store.averageRating());
    }

    @Test
    @DisplayName("Given invalid rating score, when rateStore, then IllegalArgumentException")
    public void Given_InvalidScore_When_RateStore_Then_Exception() {
        assertThrows(IllegalArgumentException.class, () -> store.rateStore("r@test.com", 0, "Bad"));
        assertThrows(IllegalArgumentException.class, () -> store.rateStore("r@test.com", 6, "Bad"));
    }

    @Test
    @DisplayName("Given no manager, when getManagerPermissions, then returns empty list and hasManagerPermission false")
    public void Given_NoManager_When_CheckManager_Then_EmptyAndFalse() {
        assertTrue(store.getAllManagers().isEmpty());
        assertFalse(store.hasManagerPermission("mgr@test.com", ManagerPermission.MANAGE_PRODUCTS));
        assertTrue(store.getManagerPermissions("mgr@test.com").isEmpty());
    }

    @Test
    @DisplayName("Given founder, when appointManager and updateManagerPermissions, then manager permissions are updated correctly")
    public void Given_Founder_When_AppointAndUpdateManager_Then_PermissionsUpdated() {
        // Initial appointment with MANAGE_PRODUCTS
        assertTrue(store.appointManager(founderEmail, "mgr@test.com", Set.of(ManagerPermission.MANAGE_PRODUCTS), false));
        assertTrue(store.hasManagerPermission("mgr@test.com", ManagerPermission.MANAGE_PRODUCTS));
        List<String> perms = store.getManagerPermissions("mgr@test.com");
        assertEquals(1, perms.size());
        assertTrue(perms.contains(ManagerPermission.MANAGE_PRODUCTS.name()));

        // Update permissions to MANAGE_POLICIES
        assertTrue(store.updateManagerPermissions(founderEmail, "mgr@test.com",
                Set.of(ManagerPermission.MANAGE_POLICIES)));
        List<String> updatedPerms = store.getManagerPermissions("mgr@test.com");
        assertEquals(1, updatedPerms.size());
        assertTrue(updatedPerms.contains(ManagerPermission.MANAGE_POLICIES.name()));
    }

    @Test
    @DisplayName("Given product with 10% discount, when calculateFinalPriceAfterDiscount called, then discounted price returned")
    public void Given_ProductWithDiscount_When_CalculateFinalPrice_Then_DiscountApplied() {
        // Arrange
        String catalogID = "CID123";
        String productName = "DiscountedProduct";
        double originalPrice = 100.0;

        String productId = store.addProductToStore(founderEmail, storeName, catalogID, productName, "Desc",
                originalPrice, 1, false,"",List.of());

        DiscountScope scope = new DiscountScope(DiscountScope.ScopeType.PRODUCT, productId);
        store.addDiscount(new SimpleDiscount(10.0, scope));

        InMemoryProductCatalog catalog = new InMemoryProductCatalog();
        catalog.addCatalogProduct(catalogID, productName, "BrandX", "Desc", List.of("General"));

        Map<String, Integer> productQuantityMap = new HashMap<>();
        productQuantityMap.put(productId, 1);

        double finalPrice = store.calculateFinalPriceAfterDiscount(productQuantityMap, catalog);

        // Assert
        assertEquals(90.0, finalPrice, 0.001, "Expected 10% discount on 100.0 = 90.0");
    }

}
