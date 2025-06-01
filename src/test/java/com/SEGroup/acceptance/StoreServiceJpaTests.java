package com.SEGroup.acceptance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.INotificationCenter;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Infrastructure.Repositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaStoreRepository;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaUserRepository;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.DbStoreData;
import com.SEGroup.Infrastructure.Repositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.*;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.MarketplaceApplication;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Report.ReportCenter;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
@SpringBootTest(classes = MarketplaceApplication.class)
@ActiveProfiles("db")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StoreServiceJpaTests {
    @Autowired
    private JpaStoreRepository jpaStoreRepository;
    private static String VALID_SESSION = "valid-session";
    private static final String INVALID_SESSION = "invalid-session";
    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String OWNER = "owner";
    private static final String OWNER_PASS = "pass123";
    private static final String STORE_NAME = "MyStore";
    private static final String CATALOG_ID = "catalog123";
    private static final String PRODUCT_ID = "product123";
    private static final String ADMIN_EMAIL = "Admin@Admin.Admin";
    private static final String ADMIN = "Admin";
    private static final String ADMIN_PASS = "$2a$10$BJmR2RNH7hTa7DCGDesel.lRX4MGz1bdYiBTM9LGcL2VWH3jcNwoS";
    private static String defaultAdminSession;
    StoreService storeService;
    StoreRepository storeRepository;
    IAuthenticationService authenticationService;
    InMemoryProductCatalog productCatalog;
    IUserRepository userRepository;
    UserService userService;
    ReportCenter reportCenter;

    @BeforeEach
    public void setUp() throws Exception {
        // Auth stubs: valid vs invalid sessions
        // doNothing().when(authenticationService).checkSessionKey(VALID_SESSION);
        storeRepository = new StoreRepository(new DbStoreData(jpaStoreRepository));
        productCatalog = new InMemoryProductCatalog();
        Security security = new Security();
        // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
        // create a key
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authenticationService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
        // create a key
        ((SecurityAdapter) authenticationService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
        reportCenter = new ReportCenter();
        userRepository = new UserRepository();
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,
                new NotificationCenter(authenticationService));
        userService = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository,
                authenticationService, reportCenter);
        VALID_SESSION = regLoginAndGetSession(OWNER, OWNER_EMAIL, OWNER_PASS); // Register and login to get a valid
        // session
        defaultAdminSession = LoginAndGetSession(ADMIN_EMAIL, ADMIN);
        Result r1 = userService.login(ADMIN_EMAIL, "Admin"); // Register and login to get a valid sessio לא טו

    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        // Register a new user
        Result<Void> regResult = userService.register(userName, email, password);
        // Authenticate the user and get a session key
        String sessionKey = authenticationService.authenticate(email);
        return sessionKey;
    }

    public String LoginAndGetSession(String email, String password) throws Exception {
        Result<String> Admin = userService.login(email, password);
        return Admin.getData();
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
        Result<String> res = storeService.addProductToCatalog(CATALOG_ID, "iphone13", "apple", "Desc",
                Collections.singletonList("phones"));

        Result<String> result = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName",
                "Desc",
                9.99, 5,"");
        assertTrue(result.isSuccess());
        Result<List<ShoppingProductDTO>> productResult = storeService.searchProducts("iphone", Collections.emptyList(),
                null, null);
        assertTrue(productResult.isSuccess());
        assertEquals(productResult.getData().get(0).getName(), "ProdName");

    }

    @Test
    public void addProductToStore_WithNegativeQuantity_ShouldFail() {
        Result<String> result = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName",
                "Desc",
                9.99, -1,"");
        assertFalse(result.isSuccess());
    }

    @Test
    public void appointOwner_WithValidData_ShouldSucceed() throws Exception {
        String newOwner = "newOwner";
        String newOwnerEmail = "newowner@example.com";
        regLoginAndGetSession(newOwner, newOwnerEmail, "newpass123"); // Register and login to get a valid session for
        // the new owner
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<Void> result = storeService.appointOwner(VALID_SESSION, STORE_NAME, newOwnerEmail);
        assertTrue(result.isSuccess());
        Result<List<String>> res = storeService.getAllOwners(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(res.isSuccess());
        assertTrue((res.getData().size() == 2));
        assertTrue(res.getData().contains(OWNER_EMAIL));
        assertTrue(res.getData().contains(newOwnerEmail));

    }

    @Test
    public void appointOwner_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.appointOwner(INVALID_SESSION, STORE_NAME, "someone@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void removeOwner_WithValidData_ShouldSucceed() throws Exception {
        String toRemove = "remove";
        String toRemoveEmail = "remove@example.com";
        regLoginAndGetSession(toRemove, toRemoveEmail, "removePass123"); // Register and login to get a valid session
        // for the new owner
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.appointOwner(VALID_SESSION, STORE_NAME, toRemoveEmail);

        Result<Void> result = storeService.removeOwner(VALID_SESSION, STORE_NAME, toRemoveEmail);
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
    public void appointManager_WithValidData_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String manager = "manager";
        String managerEmail = "manager@example.com";
        regLoginAndGetSession(manager, managerEmail, "managerPass123"); // Register and login to get a valid session for
        // the new manager
        List<String> perms = Collections.singletonList("MANAGE_PRODUCTS");

        Result<Void> result = storeService.appointManager(VALID_SESSION, STORE_NAME, managerEmail, perms);
        assertTrue(result.isSuccess());

        Result<List<String>> mgrs = storeService.getAllManagers(VALID_SESSION, STORE_NAME, OWNER_EMAIL);
        assertTrue(mgrs.isSuccess());
        assertEquals(1, mgrs.getData().size());
        assertTrue(mgrs.getData().contains(managerEmail));
    }

    @Test
    public void appointManager_WithInvalidSession_ShouldFail() {
        Result<Void> result = storeService.appointManager(INVALID_SESSION, STORE_NAME, "mgr@example.com",
                Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    @Test
    public void updateManagerPermissions_WithValidData_ShouldSucceed() {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String manager = "mgr@example.com";
        Result<Void> r = storeService.appointManager(VALID_SESSION, STORE_NAME, manager,
                Collections.singletonList("MANAGE_PRODUCTS"));
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
    public void rateProduct_WithValidData_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "ProdName", "someBrand", "Desc", List.of("Clothes"));
        storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName", "Desc", 5.0, 3,"");
        Result<Void> result = storeService.rateProduct(VALID_SESSION, STORE_NAME,
                storeService.viewStore(STORE_NAME).getData().getProducts().get(0).getProductId(), 4, "Good");
        assertTrue(result.isSuccess());

        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        List<ShoppingProductDTO> products = dto.getProducts();
        assertFalse(products.isEmpty());
        assertEquals(4.0, products.get(0).getAvgRating());
    }

    @Test
    public void purchaseShoppingCart_WithAuctionBid_ShouldSucceed() {
        // Given: Customer bidding and winning an auction
        // assert not implemented yet error:
        // assertTrue(false, "Test not implemented yet");
    }

    @Test
    public void appointManager_WithTwoManagersOnlyOneShouldSucceed() throws Exception {
        String manager1 = "manager1@gmail.com";
        String manager1Name = "manager1";
        String manager1_sk = regLoginAndGetSession(manager1Name, manager1, "manager1Pass123"); // Register and login to
        // get a valid session
        // for the new manager
        storeService.createStore(manager1_sk, STORE_NAME);
        String manager2 = "manager2@gmail.com";
        String manager2Name = "manager2";
        String manager2_sk = regLoginAndGetSession(manager2Name, manager2, "manager2Pass123"); // Register and login to
        // get a valid session
        // for the new manager
        storeService.appointManager(VALID_SESSION, STORE_NAME, manager1, List.of("MANAGE_ROLES"));
        storeService.appointManager(VALID_SESSION, STORE_NAME, manager2, List.of("MANAGE_ROLES"));

        String manager3 = "manager3@gmail.com";
        String manager3Name = "manager3";
        String manager3_sk = regLoginAndGetSession(manager3Name, manager3, "manager3Pass123"); // Register and login to
        // get a valid session
        // for the new manager

        Result<Void> result1 = storeService.appointManager(manager1_sk, STORE_NAME, manager3, List.of("MANAGE_ROLES"));
        // this one fails because currently the system allows only for owner to appoint
        // a manager, it has to be changed
        assertTrue(result1.isSuccess());

        Result<Void> result2 = storeService.appointManager(authenticationService.authenticate(manager2), STORE_NAME,
                manager3, List.of("MANAGE_ROLES"));
        assertFalse(result2.isSuccess());
    }

    // admins tests
    @Test
    public void adminCanCloseStore_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        Result<Void> result = storeService.closeStore(defaultAdminSession, STORE_NAME);
        assertTrue(result.isSuccess());
        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        assertFalse(dto.isActive());
    }

    @Test
    public void adminCanReopenStore_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.closeStore(defaultAdminSession, STORE_NAME);
        Result<Void> result = storeService.reopenStore(defaultAdminSession, STORE_NAME);
        assertTrue(result.isSuccess());
        StoreDTO dto = storeService.viewStore(STORE_NAME).getData();
        assertTrue(dto.isActive());
    }

    @Test
    public void adminCanAppointOwner_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        String newOwnerEmail = "newOwner1@example.com";
        regLoginAndGetSession("newOwner1", newOwnerEmail, "pass");
        Result<Void> result = storeService.appointOwner(defaultAdminSession, STORE_NAME, newOwnerEmail);
        assertTrue(result.isSuccess());
    }

    @Test
    public void adminCanRemoveOwner_ShouldSucceed() throws Exception {
        String newOwnerEmail = "removableOwner@example.com";
        regLoginAndGetSession("removableOwner", newOwnerEmail, "pass");

        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.appointOwner(VALID_SESSION, STORE_NAME, newOwnerEmail);

        Result<Void> result = storeService.removeOwner(defaultAdminSession, STORE_NAME, newOwnerEmail);
        assertTrue(result.isSuccess());
    }

    // Bid and Auction tests

    private void addProductsToStore() {
        // Given: A store with an auction product
        // When: A user sends a bid
        // Then: The bid should be accepted and the auction should be update
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog(CATALOG_ID, "iphone13", "apple", "Desc", Collections.singletonList("phones"));
        storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProdName", "Desc", 9.99, 5,"");
        storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "AuctionProduct", "Desc", 9.99, 5,"");
    }

    @Test
    public void submitBid_WithValidData_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "ProductName", "Brand", "Desc", List.of("Cat"));
        Result<String> added = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProductName",
                "Desc", 20.0, 5,"");
        assertTrue(added.isSuccess());
        String productId = added.getData();

        Result<Void> result = storeService.submitBidToShoppingItem(VALID_SESSION, STORE_NAME, productId, 15.0);

        assertTrue(result.isSuccess());

        // Check if the bid can be seen by the store owner
        Result<List<BidDTO>> productsResult = storeService.getProductBids(VALID_SESSION, STORE_NAME, "0_"+STORE_NAME);
        assertTrue(productsResult.isSuccess());
        List<BidDTO> bids = productsResult.getData();
        assertFalse(bids.isEmpty());
        assertEquals(productId, bids.get(0).getProductId());
    }

    @Test
    public void submitBid_WithNegativeAmount_ShouldFail() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "ProductName", "Brand", "Desc", List.of("Cat"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProductName", "Desc", 20.0, 5,"").getData();
        Result<Void> result = storeService.submitBidToShoppingItem(VALID_SESSION, STORE_NAME, productId, -5.0);
        assertFalse(result.isSuccess());
    }
    @Test
    public void WhenBidSubmited_ThenAcceptBid_ShouldSucceed() throws Exception {
        // Given: A store with an auction product
        addProductsToStore();
        productCatalog.addCatalogProduct(CATALOG_ID, "ProductName", "Brand", "Desc", List.of("Cat"));
        // When: A user sends a bid
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "ProductName", "Desc", 20.0, 5,"").getData();
        Result<Void> result = storeService.submitBidToShoppingItem(VALID_SESSION, STORE_NAME, productId, 15.0);
        assertTrue(result.isSuccess());

        Result<List<BidDTO>> productsResult = storeService.getProductBids(VALID_SESSION, STORE_NAME, productId);
        assertTrue(productsResult.isSuccess());
        List<BidDTO> bids = productsResult.getData();
        assertFalse(bids.isEmpty());
        assertEquals(productId, bids.get(0).getProductId());
    }
    @Test
    public void startAuction_WithValidData_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "AuctionProduct", "Brand", "Desc", List.of("Auctions"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "AuctionProduct", "Desc", 50.0, 10,"").getData();

        Date endDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        Result<Void> result = storeService.startAuction(VALID_SESSION, STORE_NAME, productId, 30.0, endDate);
        assertTrue(result.isSuccess());
    }
    @Test
    public void submitAuctionBid_WithValidData_ShouldSucceed() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "AuctionProduct", "Brand", "Desc", List.of("Auctions"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "AuctionProduct", "Desc", 50.0, 10,"").getData();

        Date endDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        storeService.startAuction(VALID_SESSION, STORE_NAME, productId, 30.0, endDate);

        Result<Void> result = storeService.sendAuctionOffer(VALID_SESSION, STORE_NAME, productId, 35.0);
        assertTrue(result.isSuccess());
    }
    @Test
    public void submitAuctionBid_TooLow_ShouldFail() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "AuctionProduct", "Brand", "Desc", List.of("Auctions"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "AuctionProduct", "Desc", 50.0, 10,"").getData();

        Date endDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        storeService.startAuction(VALID_SESSION, STORE_NAME, productId, 40.0, endDate);

        Result<Void> result = storeService.sendAuctionOffer(VALID_SESSION, STORE_NAME, productId, 25.0);
        assertFalse(result.isSuccess());
    }

    @Test
    public void submitBid_AfterAuctionEnd_ShouldFail() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        productCatalog.addCatalogProduct(CATALOG_ID, "AuctionProduct", "Brand", "Desc", List.of("Auctions"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "AuctionProduct", "Desc", 50.0, 10,"").getData();

        // End date in the past
        Date endDate = new Date(System.currentTimeMillis() - 1000 * 60);
        storeService.startAuction(VALID_SESSION, STORE_NAME, productId, 30.0, endDate);

        Result<Void> result = storeService.sendAuctionOffer(VALID_SESSION, STORE_NAME, productId, 35.0);
        assertFalse(result.isSuccess());
    }
}
