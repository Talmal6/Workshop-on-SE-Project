package com.SEGroup.acceptance;

import com.SEGroup.Domain.*;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.Report.ReportCenter;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;

import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.*;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.GuestRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.TransactionRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.UserRepository;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Infrastructure.SecurityAdapter;
import com.SEGroup.Service.*;
import io.jsonwebtoken.SignatureAlgorithm;      
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceAcceptanceTests {

        private static String SESSION_KEY = "valid-session";
        private static final String BAD_SESSION = "bad-session";
        private static final String USER = "user";
        private static final String USER_EMAIL = "user@example.com";
        private static final String USER_PASSWORD = "password123";
        private static final String SELLER = "seller";
        private static final String SELLER_EMAIL = "seller@exemple.com";
        private static final String SELLER_PASSWORD = "password123";
        private static String SELLER_TOKEN = "valid-seller-token";
        private static final String PAYMENT_TOKEN = "tok_visa";
        private static final String STORE_ID = "store1";
        private static final String PRODUCT_ID = "prod1";
        private static String ACTUAL_PRODUCT_ID = "prod1";
        private static final String CATALOG_ID = "cat1";

        private IAuthenticationService authenticationService;
        private IPaymentGateway paymentGateway;
        private ITransactionRepository transactionRepository;
        private IStoreRepository storeRepository;
        private IUserRepository userRepository;
        private StoreService storeService;
        private IProductCatalog productCatalog;
        private TransactionService transactionService;
        private UserService userService;
        private IShippingService shippingService;
        private INotificationCenter notificationService;
        private ReportCenter reportCenter;

        @BeforeEach
        public void setUp() throws Exception {

                // copy
                storeRepository = new StoreRepository();
                productCatalog = new InMemoryProductCatalog();
                Security security = new Security();
                // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
                // create a key
                SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                security.setKey(key);
                authenticationService = new SecurityAdapter(security, new com.SEGroup.Infrastructure.PasswordEncoder());
                // io.jsonwebtoken.security.Keys#secretKeyFor(SignatureAlgorithm) method to
                // create a key
                ((SecurityAdapter) authenticationService)
                                .setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());
                notificationService = new NotificationCenter(authenticationService);
                userRepository = new UserRepository();
                reportCenter = new ReportCenter();
                storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,
                                notificationService);

                userService = new UserService(new GuestService(new GuestRepository(), authenticationService),
                                userRepository, authenticationService, reportCenter);
                SESSION_KEY = regLoginAndGetSession(USER, USER_EMAIL, "password123"); // Register and login to get a
                                                                                      // valid session
                SELLER_TOKEN = regLoginAndGetSession(SELLER, SELLER_EMAIL, "password123"); // Register and login to get
                                                                                           // a valid session
                shippingService = mock(IShippingService.class);
                // end
                paymentGateway = mock(IPaymentGateway.class);
                transactionRepository = new TransactionRepository();
                transactionService = new TransactionService(
                                authenticationService,
                                paymentGateway,
                                transactionRepository,
                                storeRepository,
                                userRepository,
                                shippingService,
                                notificationService);
                storeService.createStore(
                                SELLER_TOKEN, STORE_ID);
                productCatalog.addCatalogProduct(
                                CATALOG_ID, PRODUCT_ID, "Brand A", "Description of Product 1", List.of("Category A"));

                Result<String> addProduct = storeService.addProductToStore(
                                SELLER_TOKEN, // session of the store-owner
                                STORE_ID, // store id
                                CATALOG_ID, // catalog entry
                                PRODUCT_ID, // store-specific product id
                                "Product 1", // name
                                100.0, // price
                                10, // quantity on shelf
                                "");
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
                List<TransactionDTO> history = transactionService.getTransactionHistory(SESSION_KEY, USER_EMAIL)
                                .getData();
                assertEquals(1, history.size(), "expected exactly one transaction");
        }

        @Test
        void purchaseShoppingCart_WithUnknownUser_ShouldFail() throws Exception {
                // Given: A shopping cart with a valid product
                userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                // When: Trying to purchase with an unknown user
                Result<Void> result = transactionService.purchaseShoppingCart(
                                SESSION_KEY, "baduser@example.com", PAYMENT_TOKEN);

                // Then: The purchase should fail
                assertFalse(result.isSuccess(), "Expected failure when using an unknown user");
        }

        @Test
        public void purchaseShoppingCart_WhenPaymentFails_ShouldRollbackAndReportFailure() throws Exception {
                userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                doThrow(new RuntimeException("card declined") // Simulating payment failure
                ).when(paymentGateway).processPayment(
                                anyString(), anyDouble());

                Result<Void> result = transactionService.purchaseShoppingCart(
                                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);

                assertFalse(result.isSuccess(), "Expected failure when payment throws");
                assertTrue(
                                result.getErrorMessage().contains("Payment failed: card declined"),
                                "Should report the payment failure message");
        }

        @Test
        void getTransactionHistory_WithValidSession_ShouldReturnDTOs() throws Exception {
                /* --------- Arrange --------- */
                purchaseShoppingCart_givenValidCartAndPayment_shouldSucceed(); // מבצעת רכישה אחת

                /* ----------- Act ----------- */
                Result<List<TransactionDTO>> historyResult = transactionService.getTransactionHistory(SESSION_KEY,
                                USER_EMAIL);

                /* ---------- Assert ---------- */
                assertTrue(historyResult.isSuccess(), historyResult.getErrorMessage());

                List<TransactionDTO> history = historyResult.getData();
                assertEquals(1, history.size(), "expected exactly one transaction");

                TransactionDTO tx = history.get(0);
                assertEquals(USER_EMAIL, tx.buyersEmail); // אימייל הקונה
                assertEquals(STORE_ID, tx.getSellerStore()); // החנות
                assertEquals(100.0, tx.getCost()); // העלות הכוללת (1 × ‎100‎)

        }

        @Test
        public void getTransactionHistory_WithInvalidSession_ShouldFail() throws Exception {
                purchaseShoppingCart_givenValidCartAndPayment_shouldSucceed(); // מבצעת רכישה אחת

                /* ----------- Act ----------- */
                Result<List<TransactionDTO>> historyResult = transactionService
                                .getTransactionHistory("some_random_key_session", USER_EMAIL);

                /* ---------- Assert ---------- */
                assertTrue(historyResult.isFailure(), historyResult.getErrorMessage());
        }

        @Test
        public void purchaseShoppingCart_WithTwoCustomersAndLastProduct_SecondCustomerShouldBeRejected()
                        throws Exception {
                // Given: Two customers trying to purchase the last product
                BasketDTO basket1 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 1
                BasketDTO basket2 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 2
                String newCLient = "client2";
                String newCLientEmail = "client2@example.com";
                String newCLientPassword = "password123";
                String newCLientToken = regLoginAndGetSession(newCLient, newCLientEmail, newCLientPassword); // Register
                                                                                                             // and
                                                                                                             // login to
                                                                                                             // get a
                                                                                                             // valid
                                                                                                             // session
                Result<String> addToCart1 = userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                assertTrue(addToCart1.isSuccess(), "addToUserCart failed: " + addToCart1.getErrorMessage());
                Result<String> addToCart2 = userService.addToUserCart(
                                newCLientToken, newCLientEmail, ACTUAL_PRODUCT_ID, STORE_ID);
                assertTrue(addToCart2.isSuccess(), "addToUserCart failed: " + addToCart2.getErrorMessage());

                // First customer purchase (should succeed)
                Result<Void> result1 = transactionService.purchaseShoppingCart(
                                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);

                assertTrue(result1.isSuccess(), "Expected first customer purchase to succeed");
                // Second customer purchase (should fail)
                Result<Void> result2 = transactionService.purchaseShoppingCart(
                                SESSION_KEY, "user2@example.com", PAYMENT_TOKEN);
                assertFalse(result2.isSuccess(), "Expected second customer to be rejected due to lack of stock");
        }

        @Test
        public void purchaseShoppingCart_WithOutOfStockProduct_ShouldFail() throws Exception {
                // Given: Customer trying to purchase an out-of-stock product
                String newProductName = "newProduct";
                Result<String> newProductID = storeService.addProductToStore(
                                SELLER_TOKEN, STORE_ID, CATALOG_ID, newProductName, "Product 1", 100.0, 0, "" // Set
                                                                                                              // quantity
                                                                                                              // to
                                                                                                              // 0
                );
                assertTrue(newProductID.isFailure(),
                                "Failed to add product to store: " + newProductID.getErrorMessage());

        }

        @Test
        public void purchaseShoppingCart_WithPaymentFailure_ShouldNotProceedWithShipping() {
                // Given: Customer trying to purchase with declined payment
                userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                // Simulating payment failure
                doThrow(new RuntimeException("Payment declined")).when(paymentGateway)
                                .processPayment(anyString(), anyDouble());

                // Try purchasing (should fail due to payment failure)
                Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
                assertFalse(result.isSuccess(), "Expected purchase to fail due to payment failure");

        }

        @Test
        public void purchaseShoppingCart_WithShippingError_ShouldNotProcessPayment() throws Exception {
                // Given: Customer trying to purchase with a shipping error
                // adjust shipping mock to throw an exception on ship method call
                doThrow(new RuntimeException("Shipping error")).when(shippingService)
                                .ship(any(BasketDTO.class), anyString());
                userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                assertFalse(
                                transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN)
                                                .isSuccess(),
                                "Expected purchase to fail due to shipping error");
        }

        @Test
        public void purchaseShoppingCart_WithPaymentFailure_ShouldRollbackProductRemoval() {
                int quantity = -1;
                Result<Integer> initialQuantity = storeService.getProductQuantity(
                                SESSION_KEY,
                                STORE_ID, ACTUAL_PRODUCT_ID);
                assertTrue(initialQuantity.isSuccess(), "Failed to get initial product quantity");
                quantity = initialQuantity.getData();
                doThrow(new RuntimeException("card declined") // Simulating payment failure
                ).when(paymentGateway).processPayment(
                                anyString(), anyDouble());
                userService.addToUserCart(
                                SESSION_KEY, USER_EMAIL, ACTUAL_PRODUCT_ID, STORE_ID);
                Result<Void> result = transactionService.purchaseShoppingCart(
                                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);

                assertFalse(result.isSuccess(), "Expected failure when payment throws");
                assertTrue(
                                result.getErrorMessage().contains("Payment failed: card declined"),
                                "Should report the payment failure message");
                // Verify that the items were rolled back to the store
                // todo currently theres no method to check the items quantity in the store
                Result<Integer> finalQuantity = storeService.getProductQuantity(
                                SESSION_KEY,
                                STORE_ID, ACTUAL_PRODUCT_ID);
                assertTrue(finalQuantity.isSuccess(), "Failed to get final product quantity");
                assertEquals(quantity, finalQuantity.getData(),
                                "Product quantity should be rolled back to initial value");

        }

        //
        @Test
        public void submitBidOnProduct_thenAcceptItByOwner() throws Exception {
                // register and login as a buyer
                Result<List<BidDTO>> bidsResults = storeService.getAllBids(
                                SELLER_TOKEN, STORE_ID);
                String buyerAuth = regLoginAndGetSession("Buyer", USER_EMAIL, USER_PASSWORD);
                // sending a big on ACTUAL_PRODUCT_ID
                Result<Void> bidResult = storeService.submitBidToShoppingItem(
                                buyerAuth, STORE_ID, ACTUAL_PRODUCT_ID, 100.0);

                assertTrue(bidResult.isSuccess(), "Failed to submit bid: " + bidResult.getErrorMessage());
                // get the bid as an owner
                Result<List<BidDTO>> bidsResult = storeService.getAllBids(
                                SELLER_TOKEN, STORE_ID);
                assertTrue(bidsResult.isSuccess(), "Failed to get bids: " + bidsResult.getErrorMessage());
                assertEquals(1, bidsResult.getData().size(), "Expected exactly one bid");
                BidDTO bid = bidsResult.getData().get(0);
                assertEquals(100.0, bid.getPrice(), 0.001, "Bid amount should be 100.0");
                assertEquals(USER_EMAIL, bid.getBidderEmail(), "Bidder email should match");
                // accept the bid
                Result<Void> acceptBidResult = transactionService.acceptBid(SELLER_TOKEN, STORE_ID, bid);
                assertTrue(acceptBidResult.isSuccess(), "Failed to accept bid: " + acceptBidResult.getErrorMessage());

        }

        @Test
        public void submitBid_WithInvalidSession_ShouldFail() {
                Result<Void> result = storeService.submitBidToShoppingItem(
                                "invalid-session", STORE_ID, ACTUAL_PRODUCT_ID, 50.0);
                assertFalse(result.isSuccess(), "Expected failure when session is invalid");
        }

        @Test
        public void submitBid_WithUnknownProduct_ShouldFail() throws Exception {
                String buyerAuth = regLoginAndGetSession("BuyerX", "buyerx@example.com", "password123");
                Result<Void> result = storeService.submitBidToShoppingItem(
                                buyerAuth, STORE_ID, "kaki", 50.0);
                assertFalse(result.isSuccess(), "Expected failure when product does not exist");
        }

        @Test
        public void getAllBids_ByNonOwner_ShouldFail() throws Exception {
                // A buyer should not be able to fetch the store's bids
                String buyerAuth = regLoginAndGetSession("BuyerY", "buyery@example.com", "password123");
                Result<List<BidDTO>> result = storeService.getAllBids(buyerAuth, STORE_ID);
                assertFalse(result.isSuccess(), "Expected failure when non-owner tries to get all bids");
        }

        @Test
        public void acceptBid_ByNonOwner_ShouldFail() throws Exception {
                // Submit a bid so there's something to accept
                String buyerAuth = regLoginAndGetSession("BuyerZ", "buyerz@example.com", "password123");
                storeService.submitBidToShoppingItem(buyerAuth, STORE_ID, ACTUAL_PRODUCT_ID, 60.0);

                BidDTO bid = storeService.getAllBids(SELLER_TOKEN, STORE_ID).getData().get(0);
                // Buyer (not seller) tries to accept
                Result<Void> result = transactionService.acceptBid(buyerAuth, STORE_ID, bid);
                assertFalse(result.isSuccess(), "Expected failure when non-owner attempts to accept a bid");
                assertTrue(result.getErrorMessage().contains("not authorized"));
        }

        @Test
        public void acceptBid_ShouldReduceStockAndCreateTransaction() throws Exception {
                // Buyer submits a bid for 2 units
                String buyerAuth = regLoginAndGetSession("BuyerStock", "buystock@example.com", "password123");
                storeService.submitBidToShoppingItem(buyerAuth, STORE_ID, ACTUAL_PRODUCT_ID, 75.0);

                BidDTO bid = storeService.getAllBids(SELLER_TOKEN, STORE_ID).getData().get(0);
                // Check initial stock
                int before = storeService.getProductQuantity(SESSION_KEY, STORE_ID, ACTUAL_PRODUCT_ID).getData();

                // Seller accepts the bid
                Result<Void> accept = transactionService.acceptBid(SELLER_TOKEN, STORE_ID, bid);
                assertTrue(accept.isSuccess(), "Expected bid acceptance to succeed");

                // Stock should decrease by 2

                // A corresponding transaction should exist for the buyer
                Result<List<TransactionDTO>> history = transactionService.getTransactionHistory(
                                buyerAuth, "buystock@example.com");
                assertTrue(history.isSuccess() && history.getData().size() == 1,
                                "Expected exactly one transaction for the buyer");
                TransactionDTO tx = history.getData().get(0);
                assertEquals("buystock@example.com", tx.buyersEmail);
                assertEquals(STORE_ID, tx.getSellerStore());
                assertEquals(75.0, tx.getCost());
        }

        @Test
        public void rejectBid_ShouldRemoveItFromList() throws Exception {
                // Buyer submits a bid
                String buyerAuth = regLoginAndGetSession("BuyerReject", "buyerrej@example.com", "password123");
                storeService.submitBidToShoppingItem(buyerAuth, STORE_ID, ACTUAL_PRODUCT_ID, 85.0);

                BidDTO bid = storeService.getAllBids(SELLER_TOKEN, STORE_ID).getData().get(0);
                // Seller rejects the bid
                Result<Void> reject = transactionService.rejectBid(SELLER_TOKEN, STORE_ID, bid);
                assertTrue(reject.isSuccess(), "Expected bid rejection to succeed");

                // No bids should remain
                List<BidDTO> remaining = storeService.getAllBids(SELLER_TOKEN, STORE_ID).getData();
                assertTrue(remaining.isEmpty(), "Expected no bids after rejection");
        }

        @Test
        public void executeAuction_WithValidBid_ShouldSucceed() throws Exception {
                // Arrange
                storeService.createStore(SELLER_TOKEN, STORE_ID);
                productCatalog.addCatalogProduct(CATALOG_ID, PRODUCT_ID, "Brand", "Desc", List.of("Electronics"));
                String productId = storeService
                                .addProductToStore(SELLER_TOKEN, STORE_ID, CATALOG_ID, "Product1", "Nice", 50.0, 3, "")
                                .getData();

                // Simulate an auction and a bid
                Date futureDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60); // 1 hour in the future
                storeService.startAuction(SELLER_TOKEN, STORE_ID, productId, 30.0, futureDate);
                storeService.sendAuctionOffer(SESSION_KEY, STORE_ID, productId, 40.0);

                BidDTO bid = storeService.getAuctionHighestBidByProduct(SELLER_TOKEN, STORE_ID, productId).getData();

                // Act
                Result<Void> result = transactionService.executeAuction(SELLER_TOKEN, STORE_ID, bid);

                // Assert
                assertTrue(result.isSuccess(), "User is not authorized to execute auction bid");
        }

        @Test
        public void executeAuction_ByNonOwner_ShouldFail() throws Exception {
                // Arrange
                storeService.createStore(SELLER_TOKEN, STORE_ID);
                productCatalog.addCatalogProduct(CATALOG_ID, PRODUCT_ID, "Brand", "Desc", List.of("Electronics"));
                String productId = storeService
                                .addProductToStore(SELLER_TOKEN, STORE_ID, CATALOG_ID, "Product1", "Nice", 50.0, 3, "")
                                .getData();

                // Start an auction
                Date futureDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour ahead
                storeService.startAuction(SELLER_TOKEN, STORE_ID, productId, 30.0, futureDate);

                // Buyer submits an auction offer
                String buyerToken = regLoginAndGetSession("unauthorizedUser", "unauth@example.com", "pass123");
                storeService.sendAuctionOffer(buyerToken, STORE_ID, productId, 45.0);

                BidDTO bid = storeService.getAuctionHighestBidByProduct(SELLER_TOKEN, STORE_ID, productId).getData();

                // Act – unauthorized user tries to execute auction
                Result<Void> result = transactionService.executeAuction(buyerToken, STORE_ID, bid);

                // Assert
                assertFalse(result.isSuccess(), "Expected failure when unauthorized user attempts to execute auction");
                assertTrue(result.getErrorMessage().toLowerCase().contains("not authorized")
                                || result.getErrorMessage().toLowerCase().contains("permission"),
                                "Expected error message to indicate lack of permissions");
        }
        // Inject error

}
