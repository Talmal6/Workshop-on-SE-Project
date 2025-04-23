package com.SEGroup.acceptance;

import com.SEGroup.Domain.IProductRepository;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.config.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionServiceTests {

    // 2.5 - Immediate Purchase of Shopping Cart
    static TransactionService transactionService;
    static IAuthenticationService authenticationService;
    static IPaymentGateway paymentGateway;
    static ITransactionRepository transactionRepository;
    static IStoreRepository storeRepository;
    static IUserRepository userRepository;
    static IProductRepository productRepository;
    static StoreService storeService;
    static UserService userService;

    @BeforeAll
    static void init() {
        authenticationService = mock(IAuthenticationService.class);
        paymentGateway = mock(IPaymentGateway.class);
        transactionRepository = mock(ITransactionRepository.class);
        storeRepository = mock(IStoreRepository.class);
        userRepository = mock(IUserRepository.class);
        productRepository = mock(IProductRepository.class);
        storeService = new StoreService(storeRepository, productRepository, authenticationService);
        userService = new UserService(userRepository, authenticationService);

        transactionService = new TransactionService(authenticationService, paymentGateway, transactionRepository, storeService, userService);
        transactionService.processPayment("testsessionKey", "testuserEmail", 100.0);
    }

    @Test
    public void GivenValidPurchaseConditions_WhenImmediatePurchase_ThenOrderPlacedAndPaymentApproved() {
        // Empty body for positive acceptance test scenario
        //lets create a user
        userService.register("testSellerUserName", "testSellerEmail", "testPassword");
        //lets login the user
        String sellerSessionKey = userService.login("testSellerEmail", "testPassword").getData();
        //lets create a store
        storeService.createStore(sellerSessionKey, "testStore", "testSellerEmail");

        //lets add a product to the store
        storeService.addProduct(sellerSessionKey, "testStore", "testProduct",  10.0);

        //create a customer
        userService.register("testCustomerUserName","testCustomerEmail", "testPassword");
        //login the customer
        String customerSessionKey = userService.login("testCustomerEmail", "testPassword").getData();
        //add the product to the cart
        //todo when methods are implemented complete this test
        //storeService.addToCart(customerSessionKey, "testProduct", 1);

    }

    @Test
    public void GivenLoggedInUser_WhenRequestingPurchaseHistory_ThenPurchaseHistoryIsRetrieved() {
            String userName = "test5";
            String userEmail = "test5@muEmail.com";
            String userPassword = "testPassword12345";
            Result<Void> result = userService.register(userName,userEmail, userPassword);
            when(authenticationService.authenticate(userEmail,userPassword)).thenReturn("a1234");
            String sessionKey = authenticationService.authenticate(userEmail, userPassword);
            assert transactionService.getTransactionHistory(sessionKey, userEmail).isSuccess();
            //todo: need to check what is the expected behavior
    }

    @Test
    public void GivenPurchaseConstraintFailure_WhenImmediatePurchase_ThenPurchaseCancelled() {

    }

    // 3.9 - Submitting a Purchase Offer (Bid)

    @Test
    public void GivenValidBidAndOfferConfirmed_WhenSubmittingPurchaseOffer_ThenOfferAcceptedAndPurchaseEnabled() {
        // Empty body for positive acceptance test scenario

    }

    @Test
    public void GivenTwoUsersAttemptToPurchaseLastItemSimultaneously_WhenSubmittingPurchaseOffer_ThenOnlyOnePurchaseIsAccepted(){

    }

    @Test
    public void GivenProductDeletedWhileUserAttemptsPurchase_WhenSubmittingPurchaseOffer_ThenPurchaseFailsWithProductNotAvailable(){

    }

    public void GivenTwoOwnersAssignSameUserAsManagerSimultaneously_WhenSubmittingAssignmentRequest_ThenOnlyOneAssignmentIsPersisted(){

    }
    @Test
    public void GivenInvalidBidOrPolicyViolation_WhenSubmittingPurchaseOffer_ThenOfferRejected() {
        // Empty body for negative acceptance test scenario
    }

    // 3.10 - Buying an Item in Auction

    @Test
    public void GivenHighestBid_WhenBuyingItemInAuction_ThenItemSoldToUser() {
        // Empty body for positive acceptance test scenario
    }

    @Test
    public void GivenBidBelowCurrentHighest_WhenBuyingItemInAuction_ThenPurchaseFails() {
        // Empty body for negative acceptance test scenario
    }

    // 3.11 - Buying an Item in Lottery

    @Test
    public void GivenWinningLotteryOutcome_WhenBuyingItemInLottery_ThenItemPurchased() {
        // Empty body for positive acceptance test scenario
    }

    @Test
    public void GivenNonWinningLotteryOutcome_WhenBuyingItemInLottery_ThenPurchaseNotCompleted() {
        // Empty body for negative acceptance test scenario
    }

    // multithreading test
    @Test
    public void GivenMultipleUsersAttemptingToPurchaseSimultaneously_WhenBuyingItem_ThenOnlyOnePurchaseIsSuccessful() {
        //initialize the users
        String userName1 = "testUser1";
        String userName2 = "testUser2";
        String email1 = "email1@mymail.com";
        String email2 = "email2@mymail.com";
        String password1 = "testPassword123";
        String password2 = "testPassword123";
        Result<Void> result1 = userService.register(userName1, email1, password1);
        Result<Void> result2 = userService.register(userName2, email2, password2);
        String sessionKey1 = userService.login(email1, password1).getData();
        String sessionKey2 = userService.login(email2, password2).getData();
        //create a store
        storeService.createStore(sessionKey1, "testStore", email1);
        //add a product to the store
        storeService.addProduct(sessionKey1, "testStore", "testProduct", 10.0);
        //add the product to the cart
        //userService.addToCart(sessionKey1, "testProduct", 1);... add 10 unique products to the shop
        //create 2 threads to simulate the purchase
        Task task1 = new Task(() -> {
            for (int i = 0; i < 10; i++) {
                try{
                    //add item i to cart
                    //sleep a random ammount of time in range of 0-1000ms
                    Thread.sleep((long) (Math.random() * 1000));
                    //userService.purchaseItem(sessionKey1, "testProduct", 1);
                } catch (InterruptedException e) {}
            }
            //todo: add item to cart
            //userService.purchaseItem(sessionKey1, "testProduct", 1);
        });
        //initialize an int array to store the results
        int[] results = new int[10];
        Task task2 = new Task(() -> {
            for (int i = 0; i < 10; i++) {
                try{
                    //add item i to cart
                    //sleep a random ammount of time in range of 0-1000ms
                    Thread.sleep((long) (Math.random() * 1000));
                    //userService.purchaseItem(sessionKey2, "testProduct", 1);
                    //if result is successfull add 1 to the result array in index i
                } catch (InterruptedException e) {
                    //log to file the error
                }
            }
            //todo: add item to cart
            //userService.purchaseItem(sessionKey1, "testProduct", 1);
            //assert that all the results are exactly 1
            //assert results[i] == 1;
            for (int result : results) {
                assert result == 1 : "Expected exactly one purchase to be successful, but got " + result;

            }
        });
        //start the threads
        Thread thread1 = new Thread(task1.getRunnable());
        Thread thread2 = new Thread(task2.getRunnable());
        thread1.start();
        thread2.start();
        //wait for the threads to finish

    }

}


/*Below is an exact list of the use cases that do not yet have corresponding testing methods in the three classes (UserServiceTests, PaymentServiceTests, and StoreServiceTests):

Guest Viewing (1.1 – Guest Viewing)

Guest opens the system and is able to browse publicly available content without being logged in.

Guest Logout (1.2 – Guest Logout)

The guest, while logged in as a visitor, logs out and is redirected to the guest view.

View Market Stores (2.1(a) – View market stores)

User navigates to view a list of currently active stores in the market.

View Products in a Specific Store (2.1(b) – View products in a specific store)

User selects a specific store and sees its list of available products.

Browse Products (2.2(a) – Browse products)

User enters a search query to display matching products across the system.

Browse Products in a Specific Store (2.2(b) – Browse products in a specific store)

User performs a search for products within the context of a particular store.

Add Product to Shopping Cart (2.3 – Add Product to Shopping Cart)

User adds a product to their shopping cart (assuming the product is available in stock).

View And Modify Shopping Cart (2.4 – View And Modify Shopping Cart)

User views the contents of the cart and makes changes (such as updating quantities or removing items).

Write Product Review (3.3 – Write Product Review)

Logged-in user, who has purchased a product, writes and submits a review for that product.

Review Product/Store (3.4 – Review product/store)

User submits a review (text feedback) for a purchased product or store.

Rate Product/Store (3.4 – Rate product/store)

User gives a rating value (typically 1-5) for a product or store they have purchased from.

Send Request/Questions (3.5 – Send request/questions)

User sends a request or question to a store (for example, asking for more information).

These are the scenarios that remain uncovered by the tests in your User, Payment, and Store service test classes. You might consider organizing them into additional test classes (for example, a ProductServiceTests or ShoppingCartServiceTests for the browsing/cart operations, and a ReviewServiceTests for the product/store reviews and ratings) to achieve complete coverage.
*/
