package com.SEGroup.Service;

import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.IProductRepository;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.Product.Transaction;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.ProductDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import java.util.List;

public class TransactionService {
    private final IAuthenticationService authenticationService;
    private final IPaymentGateway paymentGateway;
    private final ITransactionRepository transactionRepository;
    private final IStoreRepository storeRepository; // Added StoreRepository
    private final IUserRepository userRepository; // Added UserRepository
    private final IProductRepository productRepository; // Added ProductRepository


    public TransactionService(IAuthenticationService authenticationService,
            IPaymentGateway paymentGateway,
            ITransactionRepository transactionRepository,
            IStoreRepository storeRepository,
            IUserRepository userRepository,
            IProductRepository productRepository) {
        this.authenticationService = authenticationService;
        this.paymentGateway = paymentGateway;
        this.transactionRepository = transactionRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Processes a payment for the given user and amount.
     */
    // public Result<Void> processPayment(String sessionKey, String userEmail, double amount) {
    //     try {
    //         // Validate session key and user email
    //         authenticationService.checkSessionKey(sessionKey);
    //         paymentGateway.validatePayment(paymentDetails);
    //         paymentGateway.processPayment(paymentDetails);
    //         return Result.success(null);
    //     } catch (Exception e) {
    //         return Result.failure(e.getMessage());
    //     }
    // }

    public Result<List<TransactionDTO>> getTransactionHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            List<TransactionDTO> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> purchaseShoppingCart(String sessionKey, String userEmail,String paymentDetiles){
        try {
            authenticationService.checkSessionKey(sessionKey);
            List<BasketDTO> cart = userRepository.getUserCart(userEmail);
            storeRepository.removeItemsFromStores(cart);
            int totalCost = 0;
            for (BasketDTO basket : cart) {
                int storeCost = calculateTotalCost(basket);
                totalCost += storeCost;
            }
            try{
                paymentGateway.processPayment(paymentDetiles, totalCost);
            }
            catch (Exception e){
                storeRepository.rollBackItemsToStores(cart);
                return Result.failure("Payment failed: " + e.getMessage());
            }

            // Create a transaction for each basket in the cart
            for (BasketDTO basket : cart) {
                int storeCost = calculateTotalCost(basket);
                transactionRepository.addTransaction(basket.getBasketProducts(), storeCost, userEmail, basket.getStoreName());
            }
            // Clear the user's cart after successful purchase
            userRepository.clearUserCart(userEmail);
            // Return success result
            // Notify the user about the successful purchase
            // notify the stores about the purchase
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());

        }
    }



//Will be added To Util file
    private int calculateTotalCost(BasketDTO basket) {
        int totalCost = 0;
        for (String productId : basket.getBasketProducts()) {
            ProductDTO product = productRepository.getProduct(productId);
            totalCost += product.getPrice(); // Assuming ProductDTO has a getPrice() method
        }
        return totalCost;
    }



    public Result<List<TransactionDTO>> viewPurcaseHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            List<TransactionDTO> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
