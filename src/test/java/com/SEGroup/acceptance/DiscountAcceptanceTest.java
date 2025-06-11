package com.SEGroup.acceptance;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.Conditions.AndCondition;
import com.SEGroup.Domain.Discount.Discount;
import com.SEGroup.Domain.Discount.DiscountType;
import com.SEGroup.Domain.Discount.Numerical.MaxDiscount;
import com.SEGroup.Domain.Discount.SimpleDiscount;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.Repositories.ProductCatalogRepository;
import com.SEGroup.Infrastructure.Repositories.StoreRepository;
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
    ProductCatalogRepository productCatalog;
    IUserRepository userRepository;
    IGuestRepository guestRepository;
    UserService userService;
    NotificationCenter notificationService;
    ReportCenter reportCenter;
    TransactionService transactionService;
    @BeforeEach
    public void setUp() throws Exception {
        storeRepository = new StoreRepository();
        productCatalog = new ProductCatalogRepository();
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authenticationService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        ((SecurityAdapter) authenticationService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
        notificationService = new NotificationCenter(authenticationService);
        userRepository = new UserRepository();
        guestRepository = new GuestRepository();
        reportCenter = new ReportCenter();
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,
                notificationService);
        userService = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository,
                authenticationService, reportCenter);
        transactionService = new TransactionService(
                authenticationService,
                null,
                null,
                storeRepository,
                userRepository,
                null,
                notificationService,
                guestRepository
        );
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
    @Test
    public void purchase_WithCompositeDiscountAppliedThroughService_ShouldApplyCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        // Add products to catalog
        storeService.addProductToCatalog("cat1", "Coffee Maker", "Deluxe", "Coffee Maker Deluxe", List.of("kitchen"));
        storeService.addProductToCatalog("cat2", "Smartphone", "X Pro", "High-end smartphone", List.of("electronics"));
        storeService.addProductToCatalog("cat3", "Laptop", "Pro Max", "High-end laptop", List.of("electronics"));

        // Add products to store
        String coffeeId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Coffee Maker", "Deluxe", 100.0, 5, "").getData();
        String phoneId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Smartphone", "X Pro", 300.0, 5, "").getData();
        String laptopId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat3", "Laptop", "Pro Max", 700.0, 5, "").getData();

        // Add composite conditional discount to Coffee Maker if user buys Smartphone + Laptop
        storeService.addLogicalCompositeConditionalDiscountToSpecificProductInStorePercentage(
                VALID_SESSION,
                STORE_NAME,
                coffeeId,
                50,
                List.of(phoneId, laptopId), // required products
                List.of(1, 1), // min amounts
                List.of(5,5),
                100,
                "AND",
                ""
        );

        // Prepare basket with required conditions met
        BasketDTO basket = new BasketDTO(
                STORE_NAME,
                Map.of(
                        coffeeId, 1,
                        phoneId, 1,
                        laptopId, 1
                )
        );

        // Act
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        // Assert
        double discountedCoffee = result.get(coffeeId);
        assertEquals(50.0, discountedCoffee, 0.01); // 100 * 50% = 50
    }

    @Test
    public void purchase_WithCompositeDiscount_WithMaxAmountCondition_ShouldApplyCorrectly() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        // Add products to catalog
        storeService.addProductToCatalog("cat1", "Coffee Maker", "Deluxe", "Coffee Maker Deluxe", List.of("kitchen"));
        storeService.addProductToCatalog("cat2", "Smartphone", "X Pro", "High-end smartphone", List.of("electronics"));
        storeService.addProductToCatalog("cat3", "Laptop", "Pro Max", "High-end laptop", List.of("electronics"));

        // Add products to store
        String coffeeId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Coffee Maker", "Deluxe", 100.0, 5, "").getData();
        String phoneId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Smartphone", "X Pro", 300.0, 5, "").getData();
        String laptopId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat3", "Laptop", "Pro Max", 700.0, 5, "").getData();

        // Add composite discount with min=1 and max=2 for both phone and laptop
        storeService.addLogicalCompositeConditionalDiscountToSpecificProductInStorePercentage(
                VALID_SESSION,
                STORE_NAME,
                coffeeId,
                50,
                List.of(phoneId, laptopId),
                List.of(1, 1),
                List.of(2, 2),  // maxAmount
                100,
                "AND",
                ""
        );

        // Prepare basket with valid quantity (within max limit)
        BasketDTO basket = new BasketDTO(
                STORE_NAME,
                Map.of(
                        coffeeId, 1,
                        phoneId, 2,    // within limit
                        laptopId, 2    // within limit
                )
        );

        // Act
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        // Assert
        double discountedCoffee = result.get(coffeeId);
        assertEquals(50.0, discountedCoffee, 0.01); // 50% discount on 100
    }

    @Test
    public void purchase_WithCompositeDiscount_ExceedingMaxAmount_ShouldNotApplyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        storeService.addProductToCatalog("cat1", "Coffee Maker", "Deluxe", "Coffee Maker Deluxe", List.of("kitchen"));
        storeService.addProductToCatalog("cat2", "Smartphone", "X Pro", "High-end smartphone", List.of("electronics"));
        storeService.addProductToCatalog("cat3", "Laptop", "Pro Max", "High-end laptop", List.of("electronics"));

        String coffeeId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Coffee Maker", "Deluxe", 100.0, 5, "").getData();
        String phoneId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Smartphone", "X Pro", 300.0, 5, "").getData();
        String laptopId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat3", "Laptop", "Pro Max", 700.0, 5, "").getData();

        // Add composite discount with maxAmount = 2
        storeService.addLogicalCompositeConditionalDiscountToSpecificProductInStorePercentage(
                VALID_SESSION,
                STORE_NAME,
                coffeeId,
                50,
                List.of(phoneId, laptopId),
                List.of(1, 1),
                List.of(2, 2),
                100,
                "AND",
                ""
        );

        // Prepare basket with too many laptops (exceeding max)
        BasketDTO basket = new BasketDTO(
                STORE_NAME,
                Map.of(
                        coffeeId, 1,
                        phoneId, 2,
                        laptopId, 3 // exceeds maxAmount
                )
        );

        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        double discountedCoffee = result.get(coffeeId);
        assertEquals(100.0, discountedCoffee, 0.01); // No discount applied
    }

    @Test
    public void specificProductDiscount_WithMaxAmount_ConditionMet_ShouldApplyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat1", "Juice", "Orange", "Orange Juice", List.of("beverages"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Juice", "Orange", 20.0, 10, "").getData();

        // Add discount: applies if buy 2–5 units
        storeService.addConditionalDiscountToSpecificProductInStorePercentage(
                VALID_SESSION,
                STORE_NAME,
                productId,
                25,
                0,
                5,
                5,
                ""
        );

        BasketDTO basket = new BasketDTO(STORE_NAME, Map.of(productId, 3)); // 3 within [2,5]
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        double discounted = result.get(productId);
        assertEquals(45.0, discounted, 0.01); // 20*3=60 → 25% → 45
    }

    @Test
    public void specificProductDiscount_WithMaxAmount_Exceeded_ShouldNotApplyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);
        storeService.addProductToCatalog("cat1", "Juice", "Orange", "Orange Juice", List.of("beverages"));
        String productId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Juice", "Orange", 20.0, 10, "").getData();

        // Discount: applies only if quantity ∈ [2,5]
        storeService.addConditionalDiscountToSpecificProductInStorePercentage(
                VALID_SESSION,
                STORE_NAME,
                productId,
                25,
                0,
                2,
                5,
                ""
        );

        BasketDTO basket = new BasketDTO(STORE_NAME, Map.of(productId, 6)); // 6 > 5
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        double discounted = result.get(productId);
        assertEquals(120.0, discounted, 0.01); // No discount applied
    }

    @Test
    public void compositeCategoryDiscount_WithAmountInRange_ShouldApplyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        // Add products to catalog and store under the "dairy" category
        storeService.addProductToCatalog("cat1", "Milk", "1L", "Whole Milk", List.of("dairy"));
        storeService.addProductToCatalog("cat2", "Cheese", "Cheddar", "Cheddar Cheese", List.of("dairy"));

        String milkId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Milk", "1L", 10.0, 10, "").getData();
        String cheeseId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Cheese", "Cheddar", 20.0, 10, "").getData();

        // Add composite discount: 20% on "dairy" if:
        // - Milk quantity is between 2–5
        // - Cheese quantity is between 1–3
        storeService.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                VALID_SESSION,
                STORE_NAME,
                "dairy",                     // category name
                20,                          // discount percentage
                List.of(milkId, cheeseId),   // product IDs
                List.of(2, 1),               // min amounts
                List.of(5, 3),               // max amounts
                0,                           // no minimum price condition
                "AND",                       // logical operator
                null                         // no coupon
        );

        // Prepare basket: both products meet quantity conditions
        BasketDTO basket = new BasketDTO(STORE_NAME, Map.of(
                milkId, 3,    // within [2–5]
                cheeseId, 2   // within [1–3]
        ));

        // Calculate discount
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));

        double total = result.get(milkId) + result.get(cheeseId);
        assertEquals(62.0, total, 0.01);
    }

    @Test
    public void compositeCategoryDiscount_ExceedingMaxAmount_ShouldNotApplyDiscount() throws Exception {
        storeService.createStore(VALID_SESSION, STORE_NAME);

        // Add "dairy" products to catalog and store
        storeService.addProductToCatalog("cat1", "Milk", "1L", "Whole Milk", List.of("dairy"));
        storeService.addProductToCatalog("cat2", "Cheese", "Cheddar", "Cheddar Cheese", List.of("dairy"));

        String milkId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat1", "Milk", "1L", 10.0, 10, "").getData();
        String cheeseId = storeService.addProductToStore(VALID_SESSION, STORE_NAME, "cat2", "Cheese", "Cheddar", 20.0, 10, "").getData();

        // Add composite discount: 20% if Milk ∈ [2–5], Cheese ∈ [1–3]
        storeService.addLogicalCompositeConditionalDiscountToEntireCategoryInStore(
                VALID_SESSION,
                STORE_NAME,
                "dairy",
                20,
                List.of(milkId, cheeseId),
                List.of(2, 1),
                List.of(5, 3),
                0,
                "AND",
                null
        );

        // Prepare basket: milk quantity exceeds max (6 > 5)
        BasketDTO basket = new BasketDTO(STORE_NAME, Map.of(
                milkId, 6,     // exceeds max of 5
                cheeseId, 2    // valid
        ));

        // Expect: no discount applied → total = 10*6 + 20*2 = 100.0
        Map<String, Double> result = storeRepository.CalculateDiscountToStores(List.of(basket));
        double total = result.get(milkId) + result.get(cheeseId);
        assertEquals(100.0, total, 0.01);
    }




//    @Test
//    public void test_AddSimpleDiscountToSpecificProductInStorePercentage_ViaServiceOnly() throws Exception {
//        // Arrange
//        storeService.createStore(VALID_SESSION, STORE_NAME);
//        storeService.addProductToCatalog(CATALOG_ID, "Book", "Author", "Interesting book",
//                Collections.singletonList("Books"));
//
//        Result<String> addProductResult = storeService
//                .addProductToStore(VALID_SESSION, STORE_NAME, CATALOG_ID, "Book", "Hardcover", 100.0, 1, "");
//
//        String productId = addProductResult.getData();
//
//        // הוספת הנחה של 20% דרך ה־service
//        storeService.addSimpleDiscountToSpecificProductInStorePercentage(STORE_NAME, OWNER_EMAIL, productId, 20, null);
//
//        // הוספת המוצר לעגלת המשתמש (הסל)
//        userService.addToCart(VALID_SESSION, STORE_NAME, productId);
//
//        // Act
//        Result<Map<String, Double>> result = transactionService.getDiscountsForCart(VALID_SESSION);
//
//        // Assert
//        assertTrue(result.isSuccess());
//        Map<String, Double> discounts = result.getData();
//        assertTrue(discounts.containsKey(productId));
//        assertEquals(80.0, discounts.get(productId), 0.01); // 100 - 20%
//    }
}
