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





    
}
