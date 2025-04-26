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

        // Manually add a Store (createStore currently throws)
        Field storesField = StoreRepository.class.getDeclaredField("stores");
        storesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Store> storeList = (List<Store>) storesField.get(repo);
        storeList.add(new Store(storeName, founderEmail));
    }

    @Test
    @DisplayName("Given repository with one store, when getAllStores, then returns created StoreDTO")
    public void Given_RepoWithOneStore_When_GetAllStores_Then_ReturnsStoreDTOList() {
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
    public void Given_ExistingStore_When_GetStore_Then_ReturnsCorrectDTO() {
        StoreDTO dto = repo.getStore(storeName);
        assertNotNull(dto);
        assertEquals(storeName, dto.getName());
    }

    @Test
    @DisplayName("Given no such store, when getStore, then RuntimeException")
    public void Given_NoSuchStore_When_GetStore_Then_ThrowsException() {
        assertThrows(RuntimeException.class, () -> repo.getStore("Unknown"));
    }

    @Test
    @DisplayName("Given repository with store, when close and reopen, then StoreDTO isActive toggles")
    public void Given_Store_When_CloseReopen_Then_StateToggles() {
        repo.closeStore(storeName, founderEmail);
        assertFalse(repo.getStore(storeName).isActive());
        repo.reopenStore(storeName, founderEmail);
        assertTrue(repo.getStore(storeName).isActive());
    }

    @Test
    @DisplayName("Given non-founder, when closeStore, then RuntimeException")
    public void Given_NonFounder_When_CloseStore_Then_ThrowsException() {
        assertThrows(RuntimeException.class,
            () -> repo.closeStore(storeName, "other@test.com"));
    }

    @Test
    @DisplayName("Given store with product, when updateShoppingProduct, then returns updated DTO")
    public void Given_StoreWithProduct_When_UpdateShoppingProduct_Then_ReturnsDTO() {
        repo.addProductToStore(founderEmail, storeName, "CID", "ProdName", "Desc", 5.0, 10);
        ShoppingProductDTO updated = repo.updateShoppingProduct(founderEmail, storeName, "CID", 7.5, "NewDesc");
        assertEquals(7.5, updated.getPrice());
        assertEquals("NewDesc", updated.getDescription());
    }

    @Test
    @DisplayName("Given no product in store, when updateShoppingProduct, then RuntimeException")
    public void Given_NoProduct_When_UpdateShoppingProduct_Then_ThrowsException() {
        assertThrows(RuntimeException.class,
            () -> repo.updateShoppingProduct(founderEmail, storeName, "XYZ", 3.0, "X"));
    }

    @Test
    @DisplayName("Given store with product, when deleteShoppingProduct, then returns DTO and product removed")
    public void Given_StoreWithProduct_When_DeleteShoppingProduct_Then_ReturnsDTOAndRemoves() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 2.0, 5);
        ShoppingProductDTO dto = repo.deleteShoppingProduct(founderEmail, storeName, "CID");
        assertEquals("CID", dto.getCatalogID());
        StoreDTO sDto = repo.getStore(storeName);
        assertTrue(sDto.getProducts().isEmpty());
    }

    @Test
    @DisplayName("Given non-owner, when deleteShoppingProduct, then RuntimeException")
    public void Given_NonOwner_When_DeleteShoppingProduct_Then_ThrowsException() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 2.0, 5);
        assertThrows(RuntimeException.class,
            () -> repo.deleteShoppingProduct("other@test.com", storeName, "CID"));
    }

    @Test
    @DisplayName("Given store with product, when rateProduct valid, then returns DTO with avgRating")
    public void Given_StoreWithProduct_When_RateProductValid_Then_ReturnsDTO() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 4.0, 3);
        ShoppingProductDTO dto = repo.rateProduct("user@test.com", storeName, "CID", 3, "Good");
        assertEquals(3.0, dto.getAvgRating());
    }

    @Test
    @DisplayName("Given no product, when rateProduct, then RuntimeException")
    public void Given_NoProduct_When_RateProduct_Then_ThrowsException() {
        assertThrows(RuntimeException.class,
            () -> repo.rateProduct("user@test.com", storeName, "XXX", 3, ""));
    }

    @Test
    @DisplayName("Given product in store, when rateProduct invalid score, then IllegalArgumentException")
    public void Given_Product_When_RateProductInvalidScore_Then_Exception() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 4.0, 3);
        assertThrows(IllegalArgumentException.class,
            () -> repo.rateProduct("u@test.com", storeName, "CID", 0, "Bad"));
    }

    @Test
    @DisplayName("Given active store, when rateStore, then avgRating updated in StoreDTO")
    public void Given_ActiveStore_When_RateStore_Then_Succeeds() {
        repo.rateStore("u@test.com", storeName, 5, "Nice");
        StoreDTO dto = repo.getStore(storeName);
        assertEquals(5.0, dto.getAvgRating());
    }

    @Test
    @DisplayName("Given closed store, when rateStore, then RuntimeException")
    public void Given_ClosedStore_When_RateStore_Then_ThrowsException() {
        repo.closeStore(storeName, founderEmail);
        assertThrows(RuntimeException.class,
            () -> repo.rateStore("u@test.com", storeName, 4, ""));
    }

    @Test
    @DisplayName("Given active store, when addToBalance valid, then balance updated in StoreDTO")
    public void Given_ActiveStore_When_AddToBalanceValid_Then_Succeeds() {
        repo.addToBalance(founderEmail, storeName, 10.0);
        StoreDTO dto = repo.getStore(storeName);
        assertEquals(10.0, dto.getBalance());
    }

    @Test
    @DisplayName("Given invalid amount, when addToBalance, then IllegalArgumentException")
    public void Given_ActiveStore_When_AddToBalanceInvalid_Then_Exception() {
        assertThrows(IllegalArgumentException.class,
            () -> repo.addToBalance(founderEmail, storeName, -5.0));
    }

    @Test
    @DisplayName("Given unauthorized user, when addToBalance, then RuntimeException")
    public void Given_ActiveStore_When_AddToBalanceUnauthorized_Then_Exception() {
        assertThrows(RuntimeException.class,
            () -> repo.addToBalance("other@test.com", storeName, 5.0));
    }

    @Test
    @DisplayName("Given baskets with valid items, when removeItemsFromStores, then returns correct totals")
    public void Given_ValidBaskets_When_RemoveItemsFromStores_Then_ReturnsPriceMap() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 2.0, 5);
        Map<String, Integer> items = new HashMap<>();
        items.put("CID", 3);
        BasketDTO basket = new BasketDTO(storeName, items);

        Map<BasketDTO, Double> result = repo.removeItemsFromStores(List.of(basket));
        assertEquals(1, result.size());
        assertEquals(6.0, result.get(basket));
    }

    @Test
    @DisplayName("Given baskets with insufficient quantity, when removeItemsFromStores, then rollback and RuntimeException")
    public void Given_BasketsWithInsufficientQuantity_When_RemoveItemsFromStores_Then_ExceptionAndRollback() {
        repo.addProductToStore(founderEmail, storeName, "CID", "Name", "Desc", 2.0, 2);
        Map<String, Integer> items = new HashMap<>();
        items.put("CID", 5);
        BasketDTO basket = new BasketDTO(storeName, items);

        assertThrows(RuntimeException.class,
            () -> repo.removeItemsFromStores(List.of(basket)));
        ShoppingProductDTO dto = repo.getStore(storeName)
                                     .getProducts().stream()
                                     .filter(p -> p.getCatalogID().equals("CID"))
                                     .findFirst().orElseThrow();
        assertEquals(2, dto.getQuantity());
    }
}
