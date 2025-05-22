package com.SEGroup.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Discount.ConditionalDiscount;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.MaxDiscount;
import com.SEGroup.Domain.Discount.Numerical.SequentialDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Domain.User.Basket;
import com.SEGroup.Infrastructure.Repositories.*;
import com.SEGroup.Domain.Report.ReportCenter;

import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import javax.crypto.SecretKey;
import java.util.*;

public class DiscountAcceptanceTest {

    private static String VALID_SESSION = "valid-session";
    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String OWNER = "owner";
    private static final String OWNER_PASS = "pass123";
    private static final String STORE_NAME = "DiscountStore";
    private static final String CATALOG_ID = "cat001";

    StoreService storeService;
    StoreRepository storeRepository;
    IAuthenticationService authenticationService;
    InMemoryProductCatalog productCatalog;
    IUserRepository userRepository;
    UserService userService;
    NotificationCenter notificationService;
    ReportCenter reportCenter;

    @BeforeEach
    public void setUp() throws Exception {
        storeRepository = new StoreRepository();
        productCatalog = new InMemoryProductCatalog();
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authenticationService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        ((SecurityAdapter) authenticationService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
        notificationService = new NotificationCenter(authenticationService);
        userRepository = new UserRepository();
        reportCenter = new ReportCenter();
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,
                notificationService);
        userService = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository,
                authenticationService, reportCenter);
        VALID_SESSION = regLoginAndGetSession(OWNER, OWNER_EMAIL, OWNER_PASS);
    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        userService.register(userName, email, password);
        return authenticationService.authenticate(email);
    }

    @Test
    public void purchase_WithProductLevelDiscount_ShouldApplyDiscountCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog(CATALOG_ID, "Shoes", "Nike", "Comfortable running shoes",
                Collections.singletonList("Footwear"));
        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "Shoes", "Nike running", 200.0, 1, "")
                .getData();

        Store store = storeRepository.findByName(STORE_NAME);

        store.addSimpleDiscountToSpecificProductInStorePercentage(
                OWNER_EMAIL,
                productId,
                10,
                null
        );

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        double discountedPrice = store.calculateDiscount(product, quantity);

        assertEquals(20.0, discountedPrice, 0.001);
    }

    @Test
    public void purchase_WithCategoryLevelSimpleDiscount_ShouldApplyDiscountCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat10", "Laptop", "Dell", "Powerful Laptop", Collections.singletonList("electronics"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat10", "Laptop", "Dell XPS", 1000.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addSimpleDiscountToEntireCategoryInStore(OWNER_EMAIL, "electronics", 15, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        double discountAmount = store.calculateDiscount(product, quantity);

        assertEquals(150.0, discountAmount, 0.001);
    }

    @Test
    public void purchase_WithConditionalDiscount_ShouldApplyOnlyIfConditionMet() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat20", "TV", "Samsung", "Smart TV", Collections.singletonList("electronics"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat20", "TV", "Samsung TV", 800.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addConditionalDiscountToSpecificProductInStorePercentage(OWNER_EMAIL, productId, 10, 500, 1, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        // Condition minimum price is 500, actual total is 800*1=800 -> discount applies
        double discountAmount = store.calculateDiscount(product, quantity);
        assertEquals(80.0, discountAmount, 0.001);
    }

    @Test
    public void purchase_WithConditionalDiscount_ShouldNotApplyIfConditionNotMet() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat21", "Phone", "Apple", "iPhone", Collections.singletonList("electronics"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat21", "Phone", "iPhone 14", 400.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addConditionalDiscountToSpecificProductInStorePercentage(OWNER_EMAIL, productId, 10, 500, 1, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        // Condition minimum price is 500, actual total is 400*1=400 -> discount does not apply
        double discountAmount = store.calculateDiscount(product, quantity);
        assertEquals(0.0, discountAmount, 0.001);
    }

//    @Test
//    public void purchase_WithMaxDiscount_ShouldChooseMaxFromMultiple() throws Exception {
//        storeService.createStore(VALID_SESSION, STORE_NAME);
//        storeService.addProductToCatalog("cat30", "Juice", "Tropicana", "Fresh Juice", Collections.singletonList("drinks"));
//        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat30", "Juice", "Orange Juice", 10.0, 10, "").getData();
//
//        Store store = storeRepository.findByName(STORE_NAME);
//
//        MaxDiscount maxDiscount = new MaxDiscount(List.of(
//                new SimpleDiscount(DiscountType.CATEGORY, 10, "drinks", null),
//                new SimpleDiscount(DiscountType.STORE, 5, null, null)
//        ));
//
//        store.setDiscounts(maxDiscount);
//
//        ShoppingProduct product = store.getProduct(productId);
//        int quantity = product.getQuantity();
//
//        double priceBefore = product.getPrice() * quantity;
//        double discountedPrice = store.calculateDiscount(product, quantity);
//        double discountAmount = priceBefore - discountedPrice;
//
//        // Discount should be max(10%, 5%) = 10%
//        assertEquals(priceBefore * 0.10, discountAmount, 0.01);
//    }
//
//    @Test
//    public void purchase_WithSequentialDiscount_ShouldApplyAllSequentially() throws Exception {
//        storeService.createStore(VALID_SESSION, STORE_NAME);
//        storeService.addProductToCatalog("cat40", "Pasta", "Barilla", "Italian Pasta", Collections.singletonList("pasta"));
//        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat40", "Pasta", "Spaghetti", 30.0, 3, "").getData();
//
//        Store store = storeRepository.findByName(STORE_NAME);
//
//        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of(
//                new SimpleDiscount(DiscountType.CATEGORY, 10, "pasta", null),
//                new SimpleDiscount(DiscountType.STORE, 20, null, null)
//        ));
//
//        store.setDiscounts(sequentialDiscount);
//
//        ShoppingProduct product = store.getProduct(productId);
//        int quantity = product.getQuantity();
//
//        double priceBefore = product.getPrice() * quantity;
//        double discountedPrice = store.calculateDiscount(product, quantity);
//        double discountAmount = priceBefore - discountedPrice;
//
//        // Total discount = 1 - (0.9 * 0.8) = 0.28 or 28%
//        assertEquals(priceBefore * 0.28, discountAmount, 0.01);
//    }
//
//    @Test
//    public void purchase_WithMaxAndSequentialDiscountCombination_ShouldApplyCorrectly() throws Exception {
//        storeService.createStore(VALID_SESSION, STORE_NAME);
//        storeService.addProductToCatalog("cat50", "Yogurt", "DairyFarm", "Fresh Yogurt", Collections.singletonList("dairy"));
//        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat50", "Yogurt", "Fresh Yogurt", 10.0, 5, "").getData();
//
//        Store store = storeRepository.findByName(STORE_NAME);
//
//        MaxDiscount maxDiscount = new MaxDiscount(List.of(
//                new SimpleDiscount(DiscountType.CATEGORY, 5, "dairy", null),
//                new SimpleDiscount(DiscountType.STORE, 10, null, null)
//        ));
//
//        SequentialDiscount sequentialDiscount = new SequentialDiscount(List.of(
//                maxDiscount,
//                new SimpleDiscount(DiscountType.STORE, 5, null, null)
//        ));
//
//        store.setDiscounts(sequentialDiscount);
//
//        ShoppingProduct product = store.getProduct(productId);
//        int quantity = product.getQuantity();
//
//        double priceBefore = product.getPrice() * quantity;
//        double discountedPrice = store.calculateDiscount(product, quantity);
//        double discountAmount = priceBefore - discountedPrice;
//
//        // sequential discount = maxDiscount (10%) then 5%
//        // total discount = 1 - (0.9 * 0.95) = 0.145 = 14.5%
//        assertEquals(priceBefore * 0.145, discountAmount, 0.01);
//    }

}

