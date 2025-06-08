package com.SEGroup.UnitTests.StoreTests;

import com.SEGroup.Domain.Store.*;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.MaxDiscount;
import com.SEGroup.Domain.Conditions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTest2 {
    private Store store;

    @BeforeEach
    public void setup() {
        store = new Store("TestStore", "owner@test.com");
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
        store.appointManager("owner@test.com", "manager@test.com", Set.of(ManagerPermission.MANAGE_BIDS), true);
        List<String> bidManagers = store.getAllBidManagers();
        assertTrue(bidManagers.contains("owner@test.com"));
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
    public void testAddSimpleDiscountToStore() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()));
        store.addSimpleDiscountToEntireStore("owner@test.com", 10, "COUPON");
    }

    @Test
    public void testAddSimpleDiscountToCategory() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()));
        store.addSimpleDiscountToEntireCategoryInStore("owner@test.com", "books", 15, "COUPON2");
    }

    @Test
    public void testAddSimpleDiscountToProduct() {
        store.setDiscounts(new MaxDiscount(new ArrayList<>()));
        store.addSimpleDiscountToSpecificProductInStorePercentage("owner@test.com", "p1", 20, "COUPON3");
    }

    @Test
    public void testAddMaxDiscounts() {
        Discount d1 = new SimpleDiscount(DiscountType.STORE, 10, null, null);
        store.addMaxDiscounts(List.of(d1));
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
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_AND() {
        List<String> productIds = List.of("p1", "p2");
        List<Integer> amounts = List.of(2, 3);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                "owner@test.com", 10, 50, productIds, amounts, "COUPON1", "AND"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_OR() {
        List<String> productIds = List.of("p3");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                "owner@test.com", 5, 0, productIds, amounts, "COUPON2", "OR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_XOR() {
        List<String> productIds = List.of("p4", "p5");
        List<Integer> amounts = List.of(1, 1);

        store.addLogicalCompositeConditionalDiscountToEntireStore(
                "owner@test.com", 15, 20, productIds, amounts, "COUPON3", "XOR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_AND() {
        List<String> productIds = List.of("p1");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                "owner@test.com", "category1", 12, 10, productIds, amounts, "COUPON4", "AND"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_OR() {
        List<String> productIds = List.of("p2", "p3");
        List<Integer> amounts = List.of(2, 2);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                "owner@test.com", "category2", 20, 0, productIds, amounts, "COUPON5", "OR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_XOR() {
        List<String> productIds = List.of("p4");
        List<Integer> amounts = List.of(1);

        store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                "owner@test.com", "category3", 8, 5, productIds, amounts, "COUPON6", "XOR"
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_invalidLogicType() {
        List<String> productIds = List.of("p1");
        List<Integer> amounts = List.of(1);

        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireStore(
                        "owner@test.com", 10, 0, productIds, amounts, "COUPON", "INVALID"
                )
        );
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_invalidLogicType() {
        List<String> productIds = List.of("p2");
        List<Integer> amounts = List.of(1);

        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                        "owner@test.com", "category", 10, 0, productIds, amounts, "COUPON", "INVALID"
                )
        );
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
                store.appointOwner("owner@test.com", "owner@test.com", false));
    }

    @Test
    public void testRemoveOwner_NotOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.removeOwner("other@test.com", "unknownOwner@test.com", false));
    }

    @Test
    public void testRemoveAppointedCascade_RemovesAllCascade() {
        store.appointOwner("owner@test.com", "a@test.com", false);
        store.appointOwner("a@test.com", "b@test.com", false);
        store.removeOwner("owner@test.com", "a@test.com", false);
        assertFalse(store.getAllOwners().contains("b@test.com"));
    }

    @Test
    public void testResignOwnership_Founder_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.resignOwnership("owner@test.com"));
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
        assertThrows(IllegalArgumentException.class, () -> store.giveManagementComment("owner@test.com", "invalid", "comment"));
    }

    @Test
    public void testGiveManagementComment_NotOwner_Throws() {
        assertThrows(IllegalArgumentException.class, () -> store.giveManagementComment("notowner@test.com", "id", "comment"));
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireStore_MismatchedLists_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireStore("owner@test.com", 10, 0, List.of("p1"), List.of(1, 2), "COUPON", "AND"));
    }

    @Test
    public void testAddLogicalCompositeConditionalDiscountToEntireCategoryInStore_EmptyProductList_Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                store.addLogicalCompositeConditionalDiscountToEntireCategoryInStore("owner@test.com", "cat", 10, 0, new ArrayList<>(), new ArrayList<>(), "COUPON", "OR"));
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
            store.addLogicalCompositeConditionalDiscountToEntireStore("owner@test.com", 10, 0, null, List.of(1), null, "AND");
        });
    }

    @Test
    public void testMismatchedProductIdAndAmountList() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore("owner@test.com", 10, 0, List.of("p1", "p2"), List.of(1), null, "AND");
        });
    }

    @Test
    public void testEmptyProductIdsThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore("owner@test.com", 10, 0, new ArrayList<>(), new ArrayList<>(), null, "AND");
        });
    }

    @Test
    public void testInvalidLogicTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            store.addLogicalCompositeConditionalDiscountToEntireStore("owner@test.com", 10, 0, List.of("p1"), List.of(1), null, "INVALID");
        });
    }


}
