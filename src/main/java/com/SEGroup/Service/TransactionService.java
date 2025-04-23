package com.SEGroup.Service;

import com.SEGroup.Domain.TransactionDTO;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import java.util.List;

public class    TransactionService {
    private final IAuthenticationService authenticationService;
    private final IPaymentGateway paymentGateway;
    private final ITransactionRepository transactionRepository;
    private final StoreService storeService; // Added StoreService
    private final UserService userService; // Added UserService

    public TransactionService(IAuthenticationService authenticationService,
            IPaymentGateway paymentGateway,
            ITransactionRepository transactionRepository,
            StoreService storeService,
            UserService userService) {
        this.authenticationService = authenticationService;
        this.paymentGateway = paymentGateway;
        this.transactionRepository = transactionRepository;
        this.storeService = storeService; // Initialize StoreService
        this.userService = userService; // Initialize UserService
    }

    /**
     * Processes a payment for the given user and amount.
     */
    public Result<Void> processPayment(String sessionKey, String userEmail, double amount) {
        try {
            // Validate session
            authenticationService.checkSessionKey(sessionKey);

            // Prepare payment details
            String paymentDetails = String.format("User: %s, Amount: %.2f", userEmail, amount);

            // Process payment through gateway
            paymentGateway.processPayment(paymentDetails);

            // Validate payment
            if (!paymentGateway.validatePayment(paymentDetails)) {
                return Result.failure("Payment validation failed");
            }
            // MAYBE MORE MANIPULATIONS HERE ON STORES AND USERS LIKE NOTIFY AND UPDATE
            // BALANCE

            // Record transaction
            TransactionDTO transaction = new TransactionDTO(userEmail, amount);
            transactionRepository.addTransaction(transaction);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<List<TransactionDTO>> getTransactionHistory(String sessionKey, String userEmail) {
        try {
            // Validate session
            authenticationService.checkSessionKey(sessionKey);

            // Fetch transaction history
        List<TransactionDTO> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
        return Result.success(transactions);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
