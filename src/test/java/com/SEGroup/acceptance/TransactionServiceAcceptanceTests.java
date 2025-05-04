package com.SEGroup.acceptance;

import com.SEGroup.Domain.*;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.TransactionDTO;

import com.SEGroup.Infrastructure.Repositories.*;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceAcceptanceTests {

    private static String SESSION_KEY   = "valid-session";
    private static final String BAD_SESSION   = "bad-session";
    private static final String USER    = "user";
    private static final String USER_EMAIL    = "user@example.com";
    private static final String USER_PASSWORD = "password123";
    private static final String SELLER = "seller";
    private static final String SELLER_EMAIL = "seller@exemple.com";
    private static final String SELLER_PASSWORD = "password123";
    private static String SELLER_TOKEN = "valid-seller-token";
    private static final String PAYMENT_TOKEN = "tok_visa";
    private static final String STORE_ID      = "store1";
    private static final String PRODUCT_ID    = "prod1";
    private static String ACTUAL_PRODUCT_ID = "prod1";
    private static final String CATALOG_ID    = "cat1";

    private IAuthenticationService authenticationService;
    private IPaymentGateway        paymentGateway;
    private ITransactionRepository transactionRepository;
    private IStoreRepository       storeRepository;
    private IUserRepository        userRepository;
    private StoreService            storeService;
    private IProductCatalog          productCatalog;
    private TransactionService transactionService;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {



        //copy
        storeRepository = new StoreRepository();
        productCatalog = new InMemoryProductCatalog();
        Security security = new Security();
        //io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to create a key
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);
        authenticationService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
        //io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to create a key
        (( SecurityAdapter)authenticationService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
        userRepository = new UserRepository();
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository);
        userService = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository, authenticationService);
        SESSION_KEY = regLoginAndGetSession(USER, USER_EMAIL, "password123"); // Register and login to get a valid session
        SELLER_TOKEN = regLoginAndGetSession(SELLER, SELLER_EMAIL, "password123"); // Register and login to get a valid session

        //end
        paymentGateway        = mock(IPaymentGateway.class);
        transactionRepository = new TransactionRepository();
        transactionService = new TransactionService(
            authenticationService,
            paymentGateway,
            transactionRepository,
            storeRepository,
            userRepository
        );
        storeService.createStore(
                SELLER_TOKEN, STORE_ID
        );
        productCatalog.addCatalogProduct(
            CATALOG_ID, PRODUCT_ID, "Brand A", "Description of Product 1", List.of("Category A")
        );

        Result<String> addProduct = storeService.addProductToStore(
                SELLER_TOKEN,               // session of the store-owner
                STORE_ID,                   // store id
                CATALOG_ID,                 // catalog entry
                PRODUCT_ID,                 // store-specific product id
                "Product 1",                // name
                100.0,                      // price
                10                          // quantity on shelf
        );
        ACTUAL_PRODUCT_ID = addProduct.getData();



    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        // Register a new user
        Result<Void> regResult = userService.register(userName, email, password);
        // Authenticate the user and get a session key
        String sessionKey = authenticationService.authenticate(email);
        if (userService.login(email, password).isSuccess()) {
            return sessionKey;
        } else {
            throw new Exception("Login failed for user: " + email);
        }
    }
    @Test
    void purchaseShoppingCart_givenValidCartAndPayment_shouldSucceed() throws Exception {
        Result<String> addToCart = userService.addToUserCart(
                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
        assertTrue(addToCart.isSuccess(), "addToUserCart failed: " + addToCart.getErrorMessage());
        Result<Void> purchase = transactionService.purchaseShoppingCart(
                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertTrue(purchase.isSuccess(), "purchaseShoppingCart failed: " + purchase.getErrorMessage());

        // optional – verify the transaction really landed in history
        List<TransactionDTO> history =
                transactionService.getTransactionHistory(SESSION_KEY, USER_EMAIL).getData();
        assertEquals(1, history.size(), "expected exactly one transaction");
    }

    @Test
    void purchaseShoppingCart_WithUnknownUser_ShouldFail() throws Exception {
        // Given: A shopping cart with a valid product
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        // When: Trying to purchase with an unknown user
        Result<Void> result = transactionService.purchaseShoppingCart(
                SESSION_KEY, "baduser@example.com", PAYMENT_TOKEN
        );

        // Then: The purchase should fail
        assertFalse(result.isSuccess(), "Expected failure when using an unknown user");
    }


    @Test
    public void purchaseShoppingCart_WhenPaymentFails_ShouldRollbackAndReportFailure() throws Exception {
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
        doThrow(new RuntimeException("card declined") // Simulating payment failure
        ).when(paymentGateway).processPayment(
            anyString(), anyDouble());

        Result<Void> result = transactionService.purchaseShoppingCart(
            SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertFalse(result.isSuccess(), "Expected failure when payment throws");
        assertTrue(
            result.getErrorMessage().contains("Payment failed: card declined"),
            "Should report the payment failure message"
        );
    }

    @Test
    void getTransactionHistory_WithValidSession_ShouldReturnDTOs() throws Exception {
        /* --------- Arrange --------- */
        purchaseShoppingCart_givenValidCartAndPayment_shouldSucceed(); // מבצעת רכישה אחת

        /* ----------- Act ----------- */
        Result<List<TransactionDTO>> historyResult =
                transactionService.getTransactionHistory(SESSION_KEY, USER_EMAIL);

        /* ---------- Assert ---------- */
        assertTrue(historyResult.isSuccess(), historyResult.getErrorMessage());

        List<TransactionDTO> history = historyResult.getData();
        assertEquals(1, history.size(), "expected exactly one transaction");

        TransactionDTO tx = history.get(0);
        assertEquals(USER_EMAIL, tx.buyersEmail); // אימייל הקונה
        assertEquals(STORE_ID,  tx.getSellerStore());   // החנות
        assertEquals(100.0,      tx.getCost());         // העלות הכוללת (1 × ‎100‎)

    }


    @Test
    public void getTransactionHistory_WithInvalidSession_ShouldFail() throws Exception {
        purchaseShoppingCart_givenValidCartAndPayment_shouldSucceed(); // מבצעת רכישה אחת

        /* ----------- Act ----------- */
        Result<List<TransactionDTO>> historyResult =
                transactionService.getTransactionHistory("some_random_key_session", USER_EMAIL);

        /* ---------- Assert ---------- */
        assertTrue(historyResult.isFailure(), historyResult.getErrorMessage());
    }

    @Test
    public void purchaseShoppingCart_WithTwoCustomersAndLastProduct_SecondCustomerShouldBeRejected() throws Exception {
        // Given: Two customers trying to purchase the last product
        BasketDTO basket1 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 1
        BasketDTO basket2 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 2
        String newCLient =  "client2";
        String newCLientEmail =  "client2@example.com";
        String newCLientPassword =  "password123";
        String newCLientToken =  regLoginAndGetSession(newCLient, newCLientEmail, newCLientPassword); // Register and login to get a valid session
        Result<String> addToCart1 = userService.addToUserCart(
                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
        assertTrue(addToCart1.isSuccess(), "addToUserCart failed: " + addToCart1.getErrorMessage());
        Result<String> addToCart2 = userService.addToUserCart(
                newCLientToken, newCLientEmail, ACTUAL_PRODUCT_ID, STORE_ID);
        assertTrue(addToCart2.isSuccess(), "addToUserCart failed: " + addToCart2.getErrorMessage());

        // First customer purchase (should succeed)
        Result<Void> result1 = transactionService.purchaseShoppingCart(
                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertTrue(result1.isSuccess(), "Expected first customer purchase to succeed");
        // Second customer purchase (should fail)
        Result<Void> result2 = transactionService.purchaseShoppingCart(
                SESSION_KEY, "user2@example.com", PAYMENT_TOKEN
        );
        assertFalse(result2.isSuccess(), "Expected second customer to be rejected due to lack of stock");
    }

    @Test
    public void purchaseShoppingCart_WithOutOfStockProduct_ShouldFail() throws Exception {
        // Given: Customer trying to purchase an out-of-stock product
        String newProductName = "newProduct";
        String newProductID = storeService.addProductToStore(
                SELLER_TOKEN, STORE_ID, CATALOG_ID, newProductName, "Product 1", 100.0, 0 // Set quantity to 0
        ).getData();
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(newProductName, 1));

        Result r4 = userService.addToUserCart(
                SESSION_KEY, USER_EMAIL, newProductID, STORE_ID
        );
        assertTrue(r4.isSuccess(), "addToUserCart failed: " + r4.getErrorMessage());
        // Stubbing the storeRepository to simulate out-of-stock product
                //it should return                         throw new RuntimeException("Not enough quantity for product: " + productId);
        // Try to purchase (should fail)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to out of stock product");

        // Verify that payment was not processed
    }

    @Test
    public void purchaseShoppingCart_WithPaymentFailure_ShouldNotProceedWithShipping(){
        // Given: Customer trying to purchase with declined payment
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        // Simulating payment failure
        doThrow(new RuntimeException("Payment declined")).when(paymentGateway)
                .processPayment(anyString(), anyDouble());

        // Try purchasing (should fail due to payment failure)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to payment failure");
        //todo currently theres no shipping management in the system
        fail();
        //i'm not sure if quantity not changed should be tested here anyway:

    }

    @Test
    public void purchaseShoppingCart_WithShippingError_ShouldNotProcessPayment() {
        // Given: Customer trying to purchase with a shipping error
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
        //todo currently theres no shipping management in the system
        fail();
    }

    @Test
    public void purchaseShoppingCart_WithPaymentFailure_ShouldRollbackProductRemoval() {
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
        doThrow(new RuntimeException("card declined") // Simulating payment failure
        ).when(paymentGateway).processPayment(
                anyString(), anyDouble());

        Result<Void> result = transactionService.purchaseShoppingCart(
                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertFalse(result.isSuccess(), "Expected failure when payment throws");
        assertTrue(
                result.getErrorMessage().contains("Payment failed: card declined"),
                "Should report the payment failure message"
        );
        // Verify that the items were rolled back to the store
        //todo currently theres no method to check the items quantity in the store
        fail();

    }

}
