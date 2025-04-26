package com.SEGroup.UnitTests.StoreTests;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Service.Mapper.StoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.SEGroup.Domain.Store.StoreRepository;
import com.SEGroup.Domain.Store.Store;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StoreRepositoryTests {
    private StoreRepository repo;
    private final String founderEmail = "founder@test.com";
    private final String storeName = "TestStore";

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize repository and inject mapper
        repo = new StoreRepository();
        Field mapperField = StoreRepository.class.getDeclaredField("storeMapper");
        mapperField.setAccessible(true);
        mapperField.set(repo, new StoreMapper());

        // Manually add a Store instance
        Field storesField = StoreRepository.class.getDeclaredField("stores");
        storesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Store> storeList = (List<Store>) storesField.get(repo);
        storeList.add(new Store(storeName, founderEmail));
    }

    @Test
    @DisplayName("Given repository with one store, when getAllStores, then returns created StoreDTO")
    public void testGetAllStoresReturnsStoreDTOList() {
        List<StoreDTO> stores = repo.getAllStores();
        assertEquals(1, stores.size());
        StoreDTO dto = stores.get(0);
        assertEquals(storeName, dto.getName());
        assertEquals(founderEmail, dto.getFounderEmail());
        assertTrue(dto.isActive());
        assertEquals(0.0, dto.getBalance());
        assertTrue(dto.getProducts().isEmpty());
    }

    @Test
    @DisplayName("Given existing store, when getStore with valid name, then returns correct StoreDTO")
    public void testGetStoreReturnsCorrectDTO() {
        StoreDTO dto = repo.getStore(storeName);
        assertNotNull(dto);
        assertEquals(storeName, dto.getName());
    }

    @Test
    @DisplayName("Given no such store, when getStore, then RuntimeException")
    public void testGetStoreThrowsForNonexistentStore() {
        assertThrows(RuntimeException.class,
            () -> repo.getStore("Unknown"));
    }

    @Test
    @DisplayName("Given repository with store, when close and reopen, then StoreDTO isActive toggles")
    public void testCloseReopenTogglesIsActive() {
        repo.closeStore(storeName, founderEmail);
        assertFalse(repo.getStore(storeName).isActive());
        repo.reopenStore(storeName, founderEmail);
        assertTrue(repo.getStore(storeName).isActive());
    }

    @Test
    @DisplayName("Given non-founder, when closeStore, then RuntimeException")
    public void testCloseStoreThrowsForNonFounder() {
        assertThrows(RuntimeException.class,
            () -> repo.closeStore(storeName, "other@test.com"));
    }

    @Test
    @DisplayName("Given store with product, when updateShoppingProduct, then returns updated DTO")
    public void testUpdateShoppingProductReturnsUpdatedDTO() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "ProdName", "Desc", 5.0, 10);

        StoreDTO storeDTO = repo.getStore(storeName);
        assertEquals(1, storeDTO.getProducts().size());
        ShoppingProductDTO original = storeDTO.getProducts().get(0);
        String productId = original.getProductId();

        ShoppingProductDTO updated = repo.updateShoppingProduct(founderEmail, storeName, productId, 7.5, "NewDesc");
        assertEquals(7.5, updated.getPrice());
        assertEquals("NewDesc", updated.getDescription());
        assertEquals(catalogID, updated.getCatalogID());
    }

    @Test
    @DisplayName("Given no product in store, when updateShoppingProduct, then RuntimeException")
    public void testUpdateShoppingProductThrowsWhenProductNotFound() {
        assertThrows(RuntimeException.class,
            () -> repo.updateShoppingProduct(founderEmail, storeName, "nonexistent", 3.0, "X"));
    }

    @Test
    @DisplayName("Given store with product, when deleteShoppingProduct, then returns DTO and product removed")
    public void testDeleteShoppingProductRemovesAndReturnsDTO() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 2.0, 5);

        StoreDTO storeDTO = repo.getStore(storeName);
        ShoppingProductDTO original = storeDTO.getProducts().get(0);
        String productId = original.getProductId();

        ShoppingProductDTO dto = repo.deleteShoppingProduct(founderEmail, storeName, productId);
        assertEquals(catalogID, dto.getCatalogID());

        StoreDTO afterDeletion = repo.getStore(storeName);
        assertTrue(afterDeletion.getProducts().isEmpty());
    }

    @Test
    @DisplayName("Given non-owner, when deleteShoppingProduct, then RuntimeException")
    public void testDeleteShoppingProductThrowsForNonOwner() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 2.0, 5);

        StoreDTO storeDTO = repo.getStore(storeName);
        String productId = storeDTO.getProducts().get(0).getProductId();

        assertThrows(RuntimeException.class,
            () -> repo.deleteShoppingProduct("other@test.com", storeName, productId));
    }

    @Test
    @DisplayName("Given store with product, when rateProduct valid, then returns DTO with avgRating")
    public void testRateProductValidReturnsAvgRating() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 4.0, 3);

        StoreDTO storeDTO = repo.getStore(storeName);
        String productId = storeDTO.getProducts().get(0).getProductId();

        ShoppingProductDTO dto = repo.rateProduct("user@test.com", storeName, productId, 3, "Good");
        assertEquals(3.0, dto.getAvgRating());
    }

    @Test
    @DisplayName("Given no product, when rateProduct, then RuntimeException")
    public void testRateProductThrowsWhenProductNotFound() {
        assertThrows(RuntimeException.class,
            () -> repo.rateProduct("user@test.com", storeName, "XXX", 3, ""));
    }

    @Test
    @DisplayName("Given product, when rateProduct invalid score, then IllegalArgumentException")
    public void testRateProductThrowsOnInvalidScore() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 4.0, 3);

        StoreDTO storeDTO = repo.getStore(storeName);
        String productId = storeDTO.getProducts().get(0).getProductId();

        assertThrows(IllegalArgumentException.class,
            () -> repo.rateProduct("u@test.com", storeName, productId, 0, "Bad"));
    }

    @Test
    @DisplayName("Given active store, when rateStore, then avgRating updated in StoreDTO")
    public void testRateStoreUpdatesAvgRating() {
        repo.rateStore("u@test.com", storeName, 5, "Nice");
        StoreDTO dto = repo.getStore(storeName);
        assertEquals(5.0, dto.getAvgRating());
    }

    @Test
    @DisplayName("Given closed store, when rateStore, then RuntimeException")
    public void testRateStoreThrowsWhenClosed() {
        repo.closeStore(storeName, founderEmail);
        assertThrows(RuntimeException.class,
            () -> repo.rateStore("u@test.com", storeName, 4, ""));
    }

    @Test
    @DisplayName("Given active store, when addToBalance valid, then balance updated in StoreDTO")
    public void testAddToBalanceValid() {
        repo.addToBalance(founderEmail, storeName, 10.0);
        StoreDTO dto = repo.getStore(storeName);
        assertEquals(10.0, dto.getBalance());
    }

    @Test
    @DisplayName("Given invalid amount, when addToBalance, then IllegalArgumentException")
    public void testAddToBalanceThrowsOnInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> repo.addToBalance(founderEmail, storeName, -5.0));
    }

    @Test
    @DisplayName("Given unauthorized user, when addToBalance, then RuntimeException")
    public void testAddToBalanceThrowsForUnauthorized() {
        assertThrows(RuntimeException.class,
            () -> repo.addToBalance("other@test.com", storeName, 5.0));
    }

    @Test
    @DisplayName("Given baskets with valid items, when removeItemsFromStores, then returns correct totals")
    public void testRemoveItemsFromStoresReturnsTotals() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 2.0, 5);

        StoreDTO storeDTO = repo.getStore(storeName);
        String productId = storeDTO.getProducts().get(0).getProductId();

        Map<String, Integer> items = new HashMap<>();
        items.put(productId, 3);
        BasketDTO basket = new BasketDTO(storeName, items);

        Map<BasketDTO, Double> result = repo.removeItemsFromStores(List.of(basket));
        assertEquals(1, result.size());
        assertEquals(6.0, result.get(basket));
    }

    @Test
    @DisplayName("Given baskets with insufficient quantity, when removeItemsFromStores, then exception and rollback")
    public void testRemoveItemsFromStoresRollbackOnException() {
        String catalogID = "CID";
        repo.addProductToStore(founderEmail, storeName, catalogID, "Name", "Desc", 2.0, 2);

        StoreDTO storeDTO = repo.getStore(storeName);
        String productId = storeDTO.getProducts().get(0).getProductId();

        Map<String, Integer> items = new HashMap<>();
        items.put(productId, 5);
        BasketDTO basket = new BasketDTO(storeName, items);

        assertThrows(RuntimeException.class,
            () -> repo.removeItemsFromStores(List.of(basket)));

        StoreDTO afterDTO = repo.getStore(storeName);
        ShoppingProductDTO dto = afterDTO.getProducts().stream()
            .filter(p -> p.getCatalogID().equals(catalogID))
            .findFirst().orElseThrow();
        assertEquals(2, dto.getQuantity());
    }
}
