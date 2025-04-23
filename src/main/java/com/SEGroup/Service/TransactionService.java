package com.SEGroup.Service;

import com.SEGroup.Domain.Transaction;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.Product;
import com.SEGroup.Domain.Store;
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
            // Prepare payment details
            String paymentDetails = String.format("User: %s, Amount: %.2f", userEmail, amount);
            paymentGateway.validatePayment(paymentDetails)
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
            // Fetch transaction history
            List<Transaction> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> addToCart(String sessionKey, String userEmail, String storeName, String shoppingProductId) {
        try {
            Result<Product> productResult = storeService.getProductFromStore(sessionKey,shoppingProductId);
            // Add to user's cart
            Result<Void> result = userService.addToCart(userEmail, shoppingProductId);
            if (result.isSuccess()) {
                return Result.success(null);
            } else {
                return Result.failure(result.getErrorMessage());
            }
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Boolean> purchaseShoppingItem(String sessionKey, String userEmail, String storeName, String shoppingProductId) {
        try {
            // Get product details
            Result<Product> productResult = storeService.getProductFromStore(sessionKey, storeName, shoppingProductId);
            if (productResult.isFailure()) {
                return Result.failure("Product does not exist");
            }

            // Process payment for the user
            double amount = productResult.getData().getPrice(); // Assuming ProductDTO has a getPrice() method
            Result<Void> paymentResult = processPayment(sessionKey, userEmail, amount);
            if (paymentResult.isFailure()) {
                return Result.failure("Payment failed");
            }

            // Update store balance
            Result<Store> storeResult = storeService.viewStore(sessionKey, storeName);
            Store store = storeResult.getData();
            double storeBalance = store.getBalance(); // Assuming StoreDTO has a getBalance() method
            store.setBalance(storeBalance + amount); // Update the store's balance

            // Return success after completing the purchase
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
