package com.SEGroup.UnitTests.StoreTests;

import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.MaxDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Store.*;

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
        assertTrue(store.submitBidToShoppingItem(prod.getProductId(), 15.0, "bidder@test.com") != null);
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
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_AND() {
        List<String> productIds = List.of("p1");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                founderEmail, "category1", 12, 10, productIds, amounts, "COUPON4", "AND"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_EmptyProductList_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(founderEmail, "cat", 10, 0, new ArrayList<>(), new ArrayList<>(), "COUPON", "OR"));
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_OR() {
        List<String> productIds = List.of("p2", "p3");
        List<Integer> amounts = List.of(2, 2);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                founderEmail, "category2", 20, 0, productIds, amounts, "COUPON5", "OR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_XOR() {
        List<String> productIds = List.of("p4");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                founderEmail, "category3", 8, 5, productIds, amounts, "COUPON6", "XOR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_invalidLogicType() {
        List<String> productIds = List.of("p2");
        List<Integer> amounts = List.of(1);

        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                        founderEmail, "category", 10, 0, productIds, amounts, "COUPON", "INVALID"
                )
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_AND() {
        List<String> productIds = List.of("p1", "p2");
        List<Integer> amounts = List.of(2, 3);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                founderEmail, 10, 50, productIds, amounts, "COUPON1", "AND"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_MismatchedLists_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireStore(founderEmail, 10, 0, List.of("p1"), List.of(1, 2), "COUPON", "AND"));
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_OR() {
        List<String> productIds = List.of("p3");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                founderEmail, 5, 0, productIds, amounts, "COUPON2", "OR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_XOR() {
        List<String> productIds = List.of("p4", "p5");
        List<Integer> amounts = List.of(1, 1);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                founderEmail, 15, 20, productIds, amounts, "COUPON3", "XOR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_invalidLogicType() {
        List<String> productIds = List.of("p1");
        List<Integer> amounts = List.of(1);

        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireStore(
                        founderEmail, 10, 0, productIds, amounts, "COUPON", "INVALID"
                )
        );
    }

    @Test
    public void testAddMaxDiscounts() {
        Discount d1 = new SimpleDiscount(DiscountType.STORE, 10, null, null);
        store.addMaxDiscounts(List.of(d1));
    }

    @Test
    public void testAddSimpleDiscountToCategory() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()))
        ;store.addSimpleDiscountToEntireCategoryInStore(founderEmail, "books", 15, "COUPON2");
    }

    @Test
    public void testAddSimpleDiscountToProduct() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()));
        store.addSimpleDiscountToSpecificProductInStorePercentage(founderEmail, "p1", 20, "COUPON3");
    }

    @Test
    public void testAddSimpleDiscountToStore() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()))
        ;store.addSimpleDiscountToEntireStore(founderEmail, 10, "COUPON");
    }
    @Test
    public void testCloseAuction() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        Auction auction = new Auction(10.0, new Date(System.currentTimeMillis() + 10000));
        product.setAuction(auction);
        store.updateProduct("p1", product);

        store.closeAuction("p1");
        assertNull(product.getAuction());
    }

    @Test
    public void testStartAuction() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p2", "prod", "desc", 10.0, 1, "", List.of());
        store.updateProduct("p2", product);
        store.startAuction("p2", 5.0, new Date(System.currentTimeMillis() + 5000));
        assertNotNull(product.getAuction());
    }

    @Test
    public void testGetAllBidManagers() {
        store.appointManager(founderEmail, "manager@test.com", Set.of(ManagerPermission.MANAGE_BIDS), true);
        List<String> bidManagers = store.getAllBidManagers();
        assertTrue(bidManagers.contains(founderEmail));
        assertTrue(bidManagers.contains("manager@test.com"));
    }

    @Test
    public void testGiveStoreReview() {
        store.giveStoreReview("user", "great store!", 5);
        assertEquals(1, store.getAllStoreReviews().size());
    }

    @Test
    public void testGetAllProductRatings() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        product.addRating("user", 5, "good");
        store.updateProduct("p1", product);
        Map<String, Rating> ratings = store.getAllProductRatings("p1");
        assertTrue(ratings.containsKey("user"));
    }

    @Test
    public void testBidOnAuction() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        Auction auction = new Auction(10.0, new Date(System.currentTimeMillis() + 5000));
        product.setAuction(auction);
        store.updateProduct("p1", product);
        assertTrue(store.bidOnAuction("p1", "user@test.com", 15.0, 1));
    }



    @Test
    public void testSetAndGetDiscounts() {
        MaxDiscount max = new MaxDiscount(new ArrayList<>());
        store.setDiscounts(max);
        assertEquals(max, store.getDiscounts());
    }

    @Test
    public void testGetProductBids() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        store.updateProduct("p1", product);
        assertNotNull(store.getProductBids("p1"));
    }

    @Test
    public void testToString() {
        String output = store.toString();
        assertTrue(output.contains("TestStore"));
    }

    @Test
    public void testSetBalance() {
        store.setBalance(123.45);
        assertEquals(123.45, store.getBalance());
    }

//    @Test
//    public void testAddProductToStore_NotAuthorized_ReturnsNull() {
    ////        String result = store.addProductToStore("unauthorized@test.com", "TestStore", "cat1", "prod", "desc", 10.0, 1, false, "", List.of());
    ////        assertNull(result);
//    }

    @Test
    public void testCloseAuction_InvalidProduct_Throws() {
        assertThrows(IllegalArgumentException.class, () -> store.closeAuction("nonexistent"));
    }

    @Test
    public void testAppointOwner_AlreadyOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.appointOwner(founderEmail, founderEmail, false));
    }

    @Test
    public void testRemoveOwner_NotOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.removeOwner("other@test.com", "unknownOwner@test.com", false));
    }

    @Test
    public void testRemoveAppointedCascade_RemovesAllCascade() {
        store.appointOwner(founderEmail, "a@test.com", false);
        store.appointOwner("a@test.com", "b@test.com", false);
        store.removeOwner(founderEmail, "a@test.com", false);
        assertFalse(store.getAllOwners().contains("b@test.com"));
    }

    @Test
    public void testResignOwnership_Founder_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.resignOwnership(founderEmail));
    }

    @Test
    public void testResignOwnership_NotOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.resignOwnership("notAnOwner@test.com"));
    }

    @Test
    public void testGetProductQuantity_NonExistingProduct() {
        assertNull(store.getProductQuantity("nonexistent"));
    }

    @Test
    public void testBidOnAuction_NoAuctionRunning_Throws() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        store.updateProduct("p1", product);
        assertThrows(RuntimeException.class, () -> store.bidOnAuction("p1", "user@test.com", 15.0, 1));
    }

    @Test
    public void testGetAuctionInfo_ReturnsAuction() {
        ShoppingProduct product = new ShoppingProduct("TestStore", "cat1", "p1", "prod", "desc", 10.0, 1, "", List.of());
        Auction auction = new Auction(5.0, new Date(System.currentTimeMillis() + 10000));
        product.setAuction(auction);
        store.updateProduct("p1", product);
        assertEquals(auction, store.getAuctionInfo("p1"));
    }

    @Test
    public void testGetProductAuction_NonExistingProduct() {
        assertNull(store.getProductAuction("nonexistent"));
    }

    @Test
    public void testGetProductBids_NonExistingProduct() {
        assertNull(store.getProductBids("nonexistent"));
    }

    @Test
    public void testGetAllProductRatings_ProductNotFound_Throws() {
        assertThrows(RuntimeException.class, () -> store.getAllProductRatings("nonexistent"));
    }

    @Test
    public void testGiveManagementComment_ReviewNotFound_Throws() {
        assertThrows(IllegalArgumentException.class, () -> store.giveManagementComment(founderEmail, "invalid", "comment"));
    }

    @Test
    public void testGiveManagementComment_NotOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () -> store.giveManagementComment("notowner@test.com", "id", "comment"));
    }

    @Test
    public void testOnlyOwnerCanAddDiscount() {
        assertThrows(RuntimeException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore("not_owner", 10, 0, List.of("p1"), List.of(1), null, "AND");
        });
    }

    @Test
    public void testNullProductIdsThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore(founderEmail, 10, 0, null, List.of(1), null, "AND");
        });
    }

    @Test
    public void testMismatchedProductIdAndAmountList() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore(founderEmail, 10, 0, List.of("p1", "p2"), List.of(1), null, "AND");
        });
    }

    @Test
    public void testEmptyProductIdsThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore(founderEmail, 10, 0, new ArrayList<>(), new ArrayList<>(), null, "AND");
        });
    }

    @Test
    public void testInvalidLogicTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore(founderEmail, 10, 0, List.of("p1"), List.of(1), null, "INVALID");
        });
    }

}
