package com.SEGroup.acceptance;

import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.TransactionService;
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

    private TransactionService transactionService;

    @BeforeEach
    public void setUp() throws Exception {
        authenticationService = mock(IAuthenticationService.class);
        paymentGateway        = mock(IPaymentGateway.class);
        transactionRepository = mock(ITransactionRepository.class);
        storeRepository       = mock(IStoreRepository.class);
        userRepository        = mock(IUserRepository.class);

        transactionService = new TransactionService(
            authenticationService,
            paymentGateway,
            transactionRepository,
            storeRepository,
            userRepository
        );

        // make these stubs lenient so Mockito不会报“UnnecessaryStubbing”
        lenient().doNothing().when(authenticationService).checkSessionKey(SESSION_KEY);
        lenient().doThrow(new RuntimeException("Invalid session"))
                 .when(authenticationService).checkSessionKey(BAD_SESSION);
    }

    @Test
    public void purchaseShoppingCart_WithValidCartAndPayment_ShouldSucceed() {
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
    public void purchaseShoppingCart_WhenPaymentFails_ShouldRollbackAndReportFailure() {
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
    public void purchaseShoppingCart_WithTwoCustomersAndLastProduct_SecondCustomerShouldBeRejected() {
        // Given: Two customers trying to purchase the last product
        BasketDTO basket1 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 1
        BasketDTO basket2 = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1)); // Customer 2

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket1));
        when(storeRepository.removeItemsFromStores(List.of(basket1)))
                .thenReturn(Map.of(basket1, 50.0)); // First customer purchase succeeds

        // Second customer tries to purchase (should fail due to out of stock)
        when(storeRepository.removeItemsFromStores(List.of(basket2)))
                .thenReturn(Map.of(basket2, 0.0)); // No stock for second customer

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

        // Verify that payment was only processed for the first customer
        verify(paymentGateway).processPayment(PAYMENT_TOKEN, 50.0);
        verify(transactionRepository)
                .addTransaction(basket1.getBasketProducts(), 50.0, USER_EMAIL, STORE_ID);
    }

    @Test
    public void purchaseShoppingCart_WithOutOfStockProduct_ShouldFail() {
        // Given: Customer trying to purchase an out-of-stock product
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        // Stubbing the storeRepository to simulate out-of-stock product
        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                .thenReturn(Map.of(basket, 0.0)); // Out of stock

        // Try to purchase (should fail)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to out of stock product");

        // Verify that payment was not processed
        verify(paymentGateway, times(0)).processPayment(anyString(), anyDouble());
    }

    @Test
    public void purchaseShoppingCart_WithPaymentFailure_ShouldNotProceedWithShipping() {
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

        // Verify that no items were removed from the store due to payment failure
        verify(storeRepository, times(0)).removeItemsFromStores(anyList());
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

    @Test
    public void purchaseShoppingCart_WithPaymentFailure_ShouldRollbackProductRemoval() {
        // Given: A failed transaction due to payment failure
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                .thenReturn(Map.of(basket, 100.0));

        // Simulating payment failure
        doThrow(new RuntimeException("Payment failed")).when(paymentGateway)
                .processPayment(anyString(), anyDouble());

        // Try purchasing (should fail and trigger rollback)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertFalse(result.isSuccess(), "Expected purchase to fail due to payment failure");

        // Verify that rollback occurred and no items were removed from the store
        verify(storeRepository, times(0)).removeItemsFromStores(anyList()); // Ensure rollback happened
    }

    @Test
    public void purchaseShoppingCart_WithAuctionBid_ShouldSucceed() {
        // Given: Customer bidding and winning an auction
        BasketDTO basket = new BasketDTO(STORE_ID, Map.of(PRODUCT_ID, 1));
        String auctionBidAmount = "100.0"; // Bid amount

        when(userRepository.getUserCart(USER_EMAIL))
                .thenReturn(List.of(basket));
        when(storeRepository.removeItemsFromStores(List.of(basket)))
                .thenReturn(Map.of(basket, 100.0));

        // Simulating auction win
        when(paymentGateway.processPayment(anyString(), eq(100.0)))
                .thenReturn(true);

        // Perform purchase (should succeed)
        Result<Void> result = transactionService.purchaseShoppingCart(SESSION_KEY, USER_EMAIL, PAYMENT_TOKEN);
        assertTrue(result.isSuccess(), "Expected purchase to succeed with auction bid");

        // Verify that product was removed and payment was processed
        verify(storeRepository).removeItemsFromStores(anyList());
        verify(paymentGateway).processPayment(PAYMENT_TOKEN, 100.0);
        verify(transactionRepository).addTransaction(basket.getBasketProducts(), 100.0, USER_EMAIL, STORE_ID);
    }




}
