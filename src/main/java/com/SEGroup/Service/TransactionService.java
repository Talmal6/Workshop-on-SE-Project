package com.SEGroup.Service;

import com.SEGroup.Domain.TransactionDTO;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.ProductDTO;
import com.SEGroup.Domain.StoreDTO;
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


    public Result<Void> addToCart(String sessionKey, String userEmail,String storeName, String shoppingProductId) {
        try {
            // Validate session
            authenticationService.checkSessionKey(sessionKey);

            // Add product to cart in the store service

            Result<ProductDTO> productResult = storeService.getProductFromStore(sessionKey,storeName ,shoppingProductId);
            if (productResult.isFailure() ) {
                return Result.failure(productResult.getErrorMessage() != null 
                    ? productResult.getErrorMessage() 
                    : "Product does not exist");
            }
            // Check if the user exists in the user service
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


    public Result<Boolean> purchaseShoppingItem(String sessionKey, String userEmail,String storeName, String shoppingProductId) {
        try {
            // Validate session
            authenticationService.checkSessionKey(sessionKey);
            //check store exists
            Result<StoreDTO> storeResult = storeService.viewStore(sessionKey, storeName);
            if (storeResult.isFailure()) {
                return Result.failure(storeResult.getErrorMessage() != null 
                    ? storeResult.getErrorMessage() 
                    : "Store does not exist");
            }
            // Check if the product exists in the store
            Result<ProductDTO> productResult = storeService.getProductFromStore(sessionKey,storeName ,shoppingProductId);
            if (productResult.isFailure()) {
                return Result.failure(productResult.getErrorMessage() != null 
                    ? productResult.getErrorMessage() 
                    : "Product does not exist");
            }
            // Check if the user exists in the user service
            Result<Void> result = userService.addToCart(userEmail, shoppingProductId);
            if (result.isFailure()) {
                return Result.failure(result.getErrorMessage() != null 
                    ? result.getErrorMessage() 
                    : "User does not exist");
            }

            // Process payment for the user
            double amount = productResult.getData().getPrice(); // Assuming ProductDTO has a getPrice() method
            Result<Void> paymentResult = processPayment(sessionKey, userEmail, amount);
            if (paymentResult.isFailure()) {
                return Result.failure(paymentResult.getErrorMessage() != null 
                    ? paymentResult.getErrorMessage() 
                    : "Payment failed");
            }

            //add To store balance
            StoreDTO store = storeResult.getData();
            double storeBalance = store.getBalance(); // Assuming StoreDTO has a getBalance() method
            store.setBalance(storeBalance + amount); // Update the store's balance
            
            // Return success after completing the purchase
            return Result.success(true);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
