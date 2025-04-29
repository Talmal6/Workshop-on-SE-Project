package com.SEGroup.acceptance;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.ProductCatalog.ProductCatalog;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import com.SEGroup.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceAcceptanceTests {

    private static final String SESSION_KEY   = "valid-session";
    private static final String BAD_SESSION   = "bad-session";
    private static final String USER_EMAIL    = "user@example.com";
    private static final String PAYMENT_TOKEN = "tok_visa";
    private static final String STORE_ID      = "store1";
    private static final String PRODUCT_ID    = "prod1";

    private IAuthenticationService authenticationService;
    private IPaymentGateway        paymentGateway;
    private ITransactionRepository transactionRepository;
    private IStoreRepository       storeRepository;
    private IUserRepository        userRepository;
    private StoreService            storeService;
    private ProductCatalog          productCatalog;
    private TransactionService transactionService;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        authenticationService = mock(IAuthenticationService.class);
        paymentGateway        = mock(IPaymentGateway.class);
        transactionRepository = mock(ITransactionRepository.class);
        storeRepository       = mock(IStoreRepository.class);
        userRepository        = mock(IUserRepository.class);
        productCatalog        = mock(ProductCatalog.class);
        userService = new UserService(new GuestService(mock(IGuestRepository.class),authenticationService), userRepository, authenticationService);
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository);
        transactionService = new TransactionService(
            authenticationService,
            paymentGateway,
            transactionRepository,
            storeRepository,
            userRepository
        );

        lenient().doNothing().when(authenticationService).checkSessionKey(SESSION_KEY);
        lenient().doThrow(new RuntimeException("Invalid session"))
                 .when(authenticationService).checkSessionKey(BAD_SESSION);
    }

    @Test
    public void purchaseShoppingCart_WithValidCartAndPayment_ShouldSucceed() throws Exception {
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 2));
        when(userRepository.getUserCart(USER_EMAIL))
            .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
            .thenReturn(Map.of(basket, 200.0));

        // payment succeeds
        doNothing().when(paymentGateway).processPayment(PAYMENT_TOKEN, 200.0);

        Result<Void> result = transactionService.purchaseShoppingCart(
            SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertTrue(result.isSuccess(), "Expected purchaseShoppingCart to succeed");
        verify(paymentGateway).processPayment(PAYMENT_TOKEN, 200.0);
        verify(transactionRepository)
            .addTransaction(basket.getBasketProducts(), 200.0, USER_EMAIL, STORE_ID);
        verify(userRepository).clearUserCart(USER_EMAIL);
    }

    @Test
    public void purchaseShoppingCart_WhenPaymentFails_ShouldRollbackAndReportFailure() throws Exception {
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
        when(userRepository.getUserCart(USER_EMAIL))
            .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
            .thenReturn(Map.of(basket, 50.0));

        doThrow(new RuntimeException("card declined"))
            .when(paymentGateway).processPayment(PAYMENT_TOKEN, 50.0);

        Result<Void> result = transactionService.purchaseShoppingCart(
            SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertFalse(result.isSuccess(), "Expected failure when payment throws");
        assertTrue(
            result.getErrorMessage().contains("Payment failed: card declined"),
            "Should report the payment failure message"
        );
        verify(storeRepository).rollBackItemsToStores(List.of(basket));
        verify(transactionRepository, never())
            .addTransaction(anyList(), anyDouble(), anyString(), anyString());
    }

    @Test
    public void getTransactionHistory_WithValidSession_ShouldReturnDTOs() {
        TransactionDTO dto = new TransactionDTO(
            List.of(PRODUCT_ID), 30.0, USER_EMAIL, STORE_ID
        );
        when(transactionRepository.getTransactionsByUserEmail(USER_EMAIL))
            .thenReturn(List.of(dto));

        Result<List<TransactionDTO>> result =
            transactionService.getTransactionHistory(SESSION_KEY, USER_EMAIL);

        assertTrue(result.isSuccess(), "Expected history retrieval to succeed");
        assertEquals(1, result.getData().size());
        assertEquals(dto, result.getData().get(0));
    }

    @Test
    public void getTransactionHistory_WithInvalidSession_ShouldFail() {
        Result<List<TransactionDTO>> result =
            transactionService.getTransactionHistory(BAD_SESSION, USER_EMAIL);

        assertFalse(result.isSuccess(), "Invalid session should not succeed");
    }

    @Test
    public void purchaseShoppingCart_WithTwoCustomersAndLastProduct_SecondCustomerShouldBeRejected() throws Exception {
        // Given: Two customers trying to purchase the last product
        assertTrue((true));
        BasketDTO basket1 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 1
        BasketDTO basket2 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 2

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket1));
        when(storeRepository.removeItemsFromStores(List.of(basket1)))
                .thenReturn(Map.of(basket1, 50.0)); // First customer purchase succeeds

        // Second customer tries to purchase (should fail due to out of stock)

        // First customer purchase (should succeed)
        Result<Void> result1 = transactionService.purchaseShoppingCart(
                SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN
        );

        assertTrue(result1.isSuccess(), "Expected first customer purchase to succeed");

        when(storeRepository.removeItemsFromStores(List.of(basket2)))
                .thenThrow(new RuntimeException("Not enough quantity for product: " + PRODUCT_ID));

        // Second customer purchase (should fail)
        Result<Void> result2 = transactionService.purchaseShoppingCart(
                SESSION_KEY, "user2@example.com", PAYMENT_TOKEN
        );
        assertFalse(result2.isSuccess(), "Expected second customer to be rejected due to lack of stock");

        // Verify that payment was only processed for the first customer
        verify(paymentGateway).processPayment(PAYMENT_TOKEN, 50.0);
        verify(transactionRepository)
                .addTransaction(basket1.getBasketProducts(), 50.0, USER_EMAIL, STORE_ID);
    }

    @Test
    public void purchaseShoppingCart_WithOutOfStockProduct_ShouldFail() throws Exception {
        // Given: Customer trying to purchase an out-of-stock product
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        // Stubbing the storeRepository to simulate out-of-stock product
        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                //it should return                         throw new RuntimeException("Not enough quantity for product: " + productId);

                .thenThrow(new RuntimeException("Not enough quantity for product: " + PRODUCT_ID) );// Simulating out of stock
        // Try to purchase (should fail)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to out of stock product");

        // Verify that payment was not processed
        verify(paymentGateway, times(0)).processPayment(anyString(), anyDouble());
    }

    @Test
    public void purchaseShoppingCart_WithPaymentFailure_ShouldNotProceedWithShipping(){
        // Given: Customer trying to purchase with declined payment
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                .thenReturn(Map.of(basket, 100.0));

        // Simulating payment failure
        doThrow(new RuntimeException("Payment declined")).when(paymentGateway)
                .processPayment(anyString(), anyDouble());

        // Try purchasing (should fail due to payment failure)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to payment failure");

        //i'm not sure if quantity not changed should be tested here anyway:
        //todo: check if quantity not changed
    }

    @Test
    public void purchaseShoppingCart_WithShippingError_ShouldNotProcessPayment() {
        // Given: Customer trying to purchase with a shipping error
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                .thenReturn(Map.of(basket, 100.0));

        // Simulating shipping error
        doThrow(new RuntimeException("Shipping error")).when(storeRepository)
                .removeItemsFromStores(anyList());

        // Try purchasing (should fail due to shipping error)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to shipping error");

        // Verify that payment was not processed due to the shipping error
        verify(paymentGateway, times(0)).processPayment(anyString(), anyDouble());
    }

//    @Test
//    public void purchaseShoppingCart_WithPaymentFailure_ShouldRollbackProductRemoval() {
//        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
//
//        when(userRepository.getUserCart(USER_EMAIL))
//                .thenReturn(List.of(basket));
//        when(storeRepository.removeItemsFromStores(List.of(basket)))
//                .thenReturn(Map.of(basket, 100.0));
//
//        // Simulating payment failure
//        doThrow(new RuntimeException("Payment failed")).when(paymentGateway)
//                .processPayment(anyString(), anyDouble());
//
//        // Try purchasing (should fail and trigger rollback)
//        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
//        assertFalse(result.isSuccess());
//
//        // Verify that rollback occurred and no items were removed from the store
////        verify(storeRepository, times(0)).removeItemsFromStores(anyList()); // Ensure rollback happened
//
//    }

}
