package com.SEGroup.Service;

import com.SEGroup.Domain.Transaction;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.Product;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import java.util.List;

public class TransactionService {
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
            // Validate session key and user email
            authenticationService.checkSessionKey(sessionKey);
            String paymentDetails = String.format("User: %s, Amount: %.2f", userEmail, amount);
            paymentGateway.validatePayment(paymentDetails);
            paymentGateway.processPayment(paymentDetails);
            Transaction transaction = new Transaction(userEmail, amount);
            transactionRepository.addTransaction(transaction);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<List<Transaction>> getTransactionHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            List<Transaction> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> addToCart(String sessionKey, String userEmail, String storeName, String shoppingProductId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            Result<Product> product  = storeService.getProduct(sessionKey,storeName ,shoppingProductId);
            userService.addToUserCart(sessionKey,userEmail, shoppingProductId,storeName);
            return Result.success(null);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Boolean> purchaseShoppingItem(String sessionKey, String userEmail, String storeName, String shoppingProductId) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            Result<Product> product = storeService.getProduct(sessionKey, storeName, shoppingProductId);
            double amount = product.getData().getPrice(); 
            Result<Void> res = storeService.checkIfStoreExist(sessionKey, storeName);

            if (res.isFailure()){
                return Result.failure("Store does not exist: " + storeName);
            }

            processPayment(sessionKey, userEmail, amount);
            storeService.addToStoreBalance(sessionKey, storeName, amount); 
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    public Result<Void> viewPurcaseHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            List<Transaction> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            for (Transaction transaction : transactions) {
                System.out.println(transaction.toString());
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }


    
}
