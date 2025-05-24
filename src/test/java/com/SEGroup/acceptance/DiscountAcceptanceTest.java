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
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.*;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.GuestRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.UserRepository;
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
                null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        double discountedPrice = store.calculateDiscount(product, quantity);

        assertEquals(180.0, discountedPrice, 0.001);
    }

    @Test
    public void purchase_WithCategoryLevelSimpleDiscount_ShouldApplyDiscountCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat10", "Laptop", "Dell", "Powerful Laptop",
                Collections.singletonList("electronics"));
        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, "cat10", "Laptop", "Dell XPS", 1000.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addSimpleDiscountToEntireCategoryInStore(OWNER_EMAIL, "electronics", 15, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        double discountAmount = store.calculateDiscount(product, quantity);

        assertEquals(850.0, discountAmount, 0.001);
    }

    @Test
    public void purchase_WithConditionalDiscount_ShouldApplyOnlyIfConditionMet() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat20", "TV", "Samsung", "Smart TV",
                Collections.singletonList("electronics"));
        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, "cat20", "TV", "Samsung TV", 800.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addConditionalDiscountToSpecificProductInStorePercentage(OWNER_EMAIL, productId, 10, 500, 1, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();
        double price = product.getPrice() * quantity;
        store.activateConditionDiscount((int) price);
        // Condition minimum price is 500, actual total is 800*1=800 -> discount applies
        double discountAmount = store.calculateDiscount(product, quantity);
        assertEquals(720.0, discountAmount, 0.001);
    }

    @Test
    public void purchase_WithConditionalDiscount_ShouldNotApplyIfConditionNotMet() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat21", "Phone", "Apple", "iPhone", Collections.singletonList("electronics"));
        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, "cat21", "Phone", "iPhone 14", 400.0, 1, "").getData();

        Store store = storeRepository.findByName(STORE_NAME);
        store.addConditionalDiscountToSpecificProductInStorePercentage(OWNER_EMAIL, productId, 10, 500, 1, null);

        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        // Condition minimum price is 500, actual total is 400*1=400 -> discount does
        // not apply
        double discountAmount = store.calculateDiscount(product, quantity);
        assertEquals(400.0, discountAmount, 0.001);
    }

    @Test
    public void purchase_WithMaxDiscountIncludingCompositeCondition_ShouldApplyMaxCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        // Add product to catalog & store
        storeService.addProductToCatalog("cat60", "Chocolate", "Elite", "Sweet Snack",
                Collections.singletonList("sweets"));

        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, "cat60", "Chocolate", "Dark Chocolate", 10.0, 10, "")
                .getData();

        Store store = storeRepository.findByName(STORE_NAME);
        ShoppingProduct product = store.getProduct(productId);
        int quantity = product.getQuantity();

        // Simple discount: 10% STORE-wide
        Discount simple = new SimpleDiscount(DiscountType.STORE, 10, null, null);

        // Composite discount: 25% on "sweets" only if total price >= 80
        Discount composite = new AndCondition(
                List.of(new com.SEGroup.Domain.Conditions.Condition() {
                    @Override
                    public boolean isSatisfiedBy(List<ShoppingProduct> products, List<Integer> amounts) {
                        double total = 0.0;
                        for (int i = 0; i < products.size(); i++) {
                            total += products.get(i).getPrice() * amounts.get(i);
                        }
                        return total >= 80; // total = 10*10 = 100 → satisfied
                    }
                }),
                DiscountType.CATEGORY,
                25,
                "sweets",
                null
        );

        // Create MaxDiscount and apply to store
        MaxDiscount maxDiscount = new MaxDiscount(List.of(simple, composite));
        store.setDiscounts(maxDiscount);

        double priceBefore = product.getPrice() * quantity;
        double discountedPrice = store.calculateDiscount(product, quantity);
        double discountAmount = priceBefore - discountedPrice;

        // Expected: max of 10% (10.0) and 25% (25.0) → apply 25%
        assertEquals(priceBefore * 0.25, discountAmount, 0.01);
        assertEquals(75.0, discountedPrice, 0.01);
    }

}
