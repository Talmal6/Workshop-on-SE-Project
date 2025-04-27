package com.SEGroup.acceptance;

import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.InMemoryProductCatalog;
import com.SEGroup.Domain.ProductCatalog.ProductCatalog;
import com.SEGroup.Infrastructure.IAuthenticationService;

import com.SEGroup.Service.StoreService;
/*
 *     public StoreService(IStoreRepository storeRepository,
            ProductCatalog productCatalog,
            IAuthenticationService authenticationService,
            IUserRepository userRepository) {
 */
import com.SEGroup.Service.Result;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Domain.Store.StoreRepository;
import com.SEGroup.Domain.User.UserRepository;

import com.SEGroup.DTO.StoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceAcceptanceTests {

    private static final String VALID_SESSION = "valid-session";
    private static final String INVALID_SESSION = "invalid-session";
    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String STORE_NAME = "MyStore";
    private static final String CATALOG_ID = "catalog123";
    private static final String PRODUCT_ID = "product123";
    StoreService storeService;
    StoreRepository storeRepository;
    IAuthenticationService authenticationService;
    ProductCatalog productCatalog;
    IUserRepository userRepository;

    @BeforeEach
    public void setUp() throws Exception {
        // Auth stubs: valid vs invalid sessions
        // doNothing().when(authenticationService).checkSessionKey(VALID_SESSION);
        storeRepository = new StoreRepository();
        productCatalog = new InMemoryProductCatalog();
        authenticationService = mock(IAuthenticationService.class);
        userRepository = mock(UserRepository.class);
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository);
        lenient().doNothing().when(authenticationService).checkSessionKey(VALID_SESSION);
        lenient().doThrow(new RuntimeException("Invalid session")).when(authenticationService)
                .checkSessionKey(INVALID_SESSION);

        lenient().when(authenticationService.authenticate(VALID_SESSION)).thenReturn(VALID_SESSION);
        lenient().doThrow(new RuntimeException("Invalid authenticate")).when(authenticationService)
                .authenticate(INVALID_SESSION);

        lenient().when(authenticationService.getUserBySession(VALID_SESSION)).thenReturn(OWNER_EMAIL);

        // Product catalog stub
        // doNothing().when(productCatalog).isProductExist(CATALOG_ID); // Ensure
        // exception is handled

    }

    @Test
    public void createStore_WithValidSession_ShouldSucceed() {
        Result<Void> result = storeService.createStore(VALID_SESSION, STORE_NAME);
        assertTrue(result.isSuccess());

    }

    @Test
    public void createStore_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.createStore(INVALID_SESSION, STORE_NAME);

        assertFalse(result.isSuccess());
    }

    @Test
    public void addProductToStore_WithValidDetails_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<String> res = storeService.addProductToCatalog(CATALOG_ID, "iphone13", "apple", "Desc", Collections.singletonList("phones"));
        
        Result<Void> result = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName", "Desc",
                9.99, 5);
        assertTrue(result.isSuccess());
        Result<List<ShoppingProductDTO>> productResult = storeService.searchProducts("iphone",Collections.emptyList(),null,null);
        assertTrue(productResult.isSuccess());
        assertEquals(productResult.getData().get(0).getName(),"ProdName");
        
    }

    @Test
    public void addProductToStore_WithNegativeQuantity_ShouldFail() {
        Result<Void> result = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName", "Desc",
                9.99, -1);
        assertFalse(result.isSuccess());
    }

    @Test
    public void appointOwner_WithValidData_ShouldSucceed() {
        String newOwner = "newowner@example.com";
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<Void> result = storeService.appointOwner(VALID_SESSION, STORE_NAME, newOwner);
        assertTrue(result.isSuccess());
        Result<List<String>> res = storeService.getAllOwners(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(res.isSuccess());
        assertTrue(res.getData().size() == 2);
        assertTrue(res.getData().contains(OWNER_EMAIL));
        assertTrue(res.getData().contains(newOwner));

    }

    @Test
    public void appointOwner_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.appointOwner(INVALID_SESSION, STORE_NAME, "someone@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void removeOwner_WithValidData_ShouldSucceed() {
        String toRemove = "remove@example.com";
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.appointOwner(VALID_SESSION, STORE_NAME, toRemove);
        doNothing().when(userRepository).removeOwner(STORE_NAME, toRemove);

        Result<Void> result = storeService.removeOwner(VALID_SESSION, STORE_NAME, toRemove);
        assertTrue(result.isSuccess());
        Result<List<String>> res = storeService.getAllOwners(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(res.isSuccess());
        assertTrue(res.getData().size() == 1);
        assertTrue(res.getData().get(0).equals(OWNER_EMAIL));
    }

    @Test
    public void removeOwner_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.removeOwner(INVALID_SESSION, STORE_NAME, "foo@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void appointManager_WithValidData_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String manager = "manager@example.com";
        List<String> perms = Collections.singletonList("MANAGE_PRODUCTS");

        Result<Void> result = storeService.appointManager(VALID_SESSION, STORE_NAME, manager, perms);
        assertTrue(result.isSuccess());

        Result<List<String>> mgrs = storeService.getAllManagers(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(mgrs.isSuccess());
        assertEquals(1, mgrs.getData().size());
        assertTrue(mgrs.getData().contains(manager));
    }

    @Test
    public void appointManager_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.appointManager(INVALID_SESSION, STORE_NAME, "mgr@example.com", Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    @Test
    public void updateManagerPermissions_WithValidData_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String manager = "mgr@example.com";
        Result<Void> r = storeService.appointManager(VALID_SESSION, STORE_NAME, manager, Collections.singletonList("MANAGE_PRODUCTS"));
        List<String> newPerms = Arrays.asList("MANAGE_PRODUCTS", "MANAGE_ROLES");

        Result<Void> result = storeService.updateManagerPermissions(VALID_SESSION, STORE_NAME, manager, newPerms);
        assertTrue(result.isSuccess());

        Result<List<String>> permsRes = storeService.getManagerPermission(VALID_SESSION, STORE_NAME, manager);
        assertTrue(permsRes.isSuccess());
        assertEquals(2, permsRes.getData().size());
        assertTrue(permsRes.getData().contains("MANAGE_ROLES"));
    }

    @Test
    public void getManagerPermission_WithValidData_ShouldReturnPermissions() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String manager = "mgr@example.com";
        List<String> perms = Collections.singletonList("VIEW_ONLY");
        storeService.appointManager(VALID_SESSION, STORE_NAME, manager, perms);

        Result<List<String>> result = storeService.getManagerPermission(VALID_SESSION, STORE_NAME, manager);
        assertTrue(result.isSuccess());
        assertEquals(perms, result.getData());
    }

    @Test
    public void getAllOwners_WithValidData_ShouldReturnOwners() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String coOwner = "coowner@example.com";
        storeService.appointOwner(VALID_SESSION, STORE_NAME, coOwner);

        Result<List<String>> result = storeService.getAllOwners(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertTrue(result.getData().contains(OWNER_EMAIL));
        assertTrue(result.getData().contains(coOwner));
    }

    @Test
    public void closeStore_WithValidSession_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<Void> result = storeService.closeStore(VALID_SESSION, STORE_NAME);
        assertTrue(result.isSuccess());

        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        assertFalse(dto.isActive());
    }

    @Test
    public void reopenStore_WithValidSession_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.closeStore(VALID_SESSION, STORE_NAME);

        Result<Void> result = storeService.reopenStore(VALID_SESSION, STORE_NAME);
        assertTrue(result.isSuccess());

        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        assertTrue(dto.isActive());
    }

    @Test
    public void rateStore_WithValidData_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<Void> result = storeService.rateStore(VALID_SESSION, STORE_NAME, 5, "Great");
        assertTrue(result.isSuccess());
        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        assertEquals(5.0, dto.getAvgRating());
    }

    @Test
    public void rateProduct_WithValidData_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName", "Desc", 5.0, 3);
        Result<Void> result = storeService.rateProduct(VALID_SESSION, STORE_NAME,
                storeService.viewStore(STORE_NAME).getData().getProducts().get(0).getProductId()
                , 4, "Good");
        assertTrue(result.isSuccess());

        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        List<ShoppingProductDTO> products = dto.getProducts();
        assertFalse(products.isEmpty());
        assertEquals(4.0, products.get(0).getAvgRating());
    }
    @Test
    public void appointManager_WithTwoManagersOnlyOneShouldSucceed() {
        // Given: Two managers trying to appoint permissions for a third manager
        String managerEmail = "manager3@example.com";
        List<String> permissions = List.of("READ", "WRITE");

        // First manager attempts to set permissions (should succeed)
        doNothing().when(storeRepository).appointManager(STORE_NAME, "manager1@example.com", managerEmail, permissions);

        // Second manager attempts to set permissions (should fail)
        doThrow(new RuntimeException("Permission conflict")).when(storeRepository)
                .appointManager(STORE_NAME, "manager2@example.com", managerEmail, permissions);

        // First manager appointment (should succeed)
        Result<Void> result1 = storeService.appointManager(VALID_SESSION, STORE_NAME, managerEmail, permissions);
        assertTrue(result1.isSuccess(), "Expected first manager to succeed in setting permissions");

        // Second manager appointment (should fail)
        Result<Void> result2 = storeService.appointManager(VALID_SESSION, STORE_NAME, managerEmail, permissions);
        assertFalse(result2.isSuccess(), "Expected second manager to fail due to conflict");

        // Verify that only the first manager's request succeeded
        verify(storeRepository).appointManager(STORE_NAME, "manager1@example.com", managerEmail, permissions);
        verify(storeRepository, times(0)).appointManager(STORE_NAME, "manager2@example.com", managerEmail, permissions);
    }


}
