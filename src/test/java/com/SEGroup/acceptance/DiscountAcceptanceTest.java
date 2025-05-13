package com.SEGroup.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Discount.ConditionalDiscount;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountScope;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Domain.User.Basket;
import com.SEGroup.Infrastructure.Repositories.*;

import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Predicate;

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
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,
                notificationService);
        userService = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository,
                authenticationService);
        VALID_SESSION = regLoginAndGetSession(OWNER, OWNER_EMAIL, OWNER_PASS);
    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        userService.register(userName, email, password);
        return authenticationService.authenticate(email);
    }

    @Test
    public void purchase_WithProductLevelDiscount_ShouldApplyDiscountCorrectly() throws Exception {
        // Create store and add product
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog(CATALOG_ID, "Shoes", "Nike", "Comfortable running shoes",
                Collections.singletonList("Footwear"));
        String productId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "Shoes", "Nike running", 200.0, 1,"").getData();

        // Add 10% discount on this product
        Store store = storeRepository.findByName(STORE_NAME);
        store.addDiscount(new SimpleDiscount(10.0, new DiscountScope(DiscountScope.ScopeType.PRODUCT, productId)));

        // Simulate purchase
        Map<String, Integer> productMap = new HashMap<>();
        productMap.put(productId, 1);
        double discountedPrice = store.calculateFinalPriceAfterDiscount(productMap, productCatalog);

        // Assert 10% discount applied on 200 -> 180
        assertEquals(180.0, discountedPrice, 0.001);
    }

    @Test
    public void purchase_WithConditionalDiscount_UsingBasket_ShouldApplyDiscountOnMatchingProduct() throws Exception {
        // Arrange: create store, register catalog, and add products
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat1", "Tomato", "FreshCo", "Fresh tomatoes", List.of("Vegetables"));
        storeService.addProductToCatalog("cat2", "Cucumber", "FreshCo", "Fresh cucumbers", List.of("Vegetables"));

        String tomatoId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Tomato", "Fresh", 100.0, 2,"")
                .getData();
        String cucumberId = storeService
                .addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Cucumber", "Fresh", 50.0, 2,"").getData();

        // Prepare conditional discount: if total purchase > 200, 10% discount on
        // tomatoes
        Store store = storeRepository.findByName(STORE_NAME);
        DiscountScope tomatoScope = new DiscountScope(DiscountScope.ScopeType.PRODUCT, tomatoId);
        Discount discountOnTomato = new SimpleDiscount(10.0, tomatoScope);

        Predicate<StoreSearchEntry[]> condition = arr -> {
            double total = 0;
            for (StoreSearchEntry e : arr)
                total += e.getPrice() * e.getQuantity();
            return total > 200;
        };

        Discount tomatoConditionalDiscount = new ConditionalDiscount(condition, discountOnTomato);
        store.addDiscount(tomatoConditionalDiscount);

        // Act: use Basket to represent user's purchase
        Basket basket = new Basket(STORE_NAME);
        basket.add(tomatoId, 2); // 2 x 100 = 200
        basket.add(cucumberId, 2); // 2 x 50 = 100

        double finalPrice = store.calculateFinalPriceAfterDiscount(basket.snapshot(), productCatalog);

        // Assert: tomato price becomes 180, cucumber stays 100, total = 280
        assertEquals(280.0, finalPrice, 0.001);
    }

    @Test
    public void purchase_WithCompositeConditionUsingUserBasket_ShouldApplyDairyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        productCatalog.addCatalogProduct("c1", "Pasta A", "Barilla", "500g", List.of("pasta"));
        productCatalog.addCatalogProduct("c2", "Pasta B", "Barilla", "Spaghetti", List.of("pasta"));
        productCatalog.addCatalogProduct("c3", "Milk", "Tnuva", "1L", List.of("dairy"));

        String p1 = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "c1", "Pasta A", "desc", 20.0, 10,"")
                .getData();
        String p2 = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "c2", "Pasta B", "desc", 25.0, 10,"")
                .getData();
        String p3 = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "c3", "Milk", "desc", 10.0, 10,"").getData();

        Basket userBasket = new Basket(STORE_NAME);
        userBasket.add(p1, 2); // 2 x 20 = 40
        userBasket.add(p2, 1); // 1 x 25 = 25
        userBasket.add(p3, 5); // 5 x 10 = 50

        Predicate<StoreSearchEntry[]> over100 = arr -> {
            double total = 0;
            for (StoreSearchEntry e : arr) {
                total += e.getPrice() * e.getQuantity();
            }
            return total > 100;
        };

        Predicate<StoreSearchEntry[]> atLeast3Pastas = arr -> {
            int count = 0;
            for (StoreSearchEntry e : arr) {
                if (productCatalog.getCategoriesOfProduct(e.getCatalogID()).contains("pasta")) {
                    count += e.getQuantity();
                }
            }
            return count >= 3;
        };

        Predicate<StoreSearchEntry[]> combined = new AndCondition(List.of(over100, atLeast3Pastas));

        Discount dairyDiscount = new SimpleDiscount(5, new DiscountScope(DiscountScope.ScopeType.CATEGORY, "dairy"));
        Discount conditional = new ConditionalDiscount(combined, dairyDiscount);

        Store store = storeRepository.findByName(STORE_NAME);
        store.addDiscount(conditional);

        double finalPrice = store.calculateFinalPriceAfterDiscount(userBasket.snapshot(), productCatalog);

        assertEquals(112.5, finalPrice, 0.001);
    }

}
