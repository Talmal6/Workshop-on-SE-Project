package com.SEGroup.Service;

import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.IPaymentGateway;
import java.util.List;
import java.util.Map;

public class TransactionService {
    private final IAuthenticationService authenticationService;
    private final IPaymentGateway paymentGateway;
    private final ITransactionRepository transactionRepository;
    private final IStoreRepository storeRepository; // Added StoreRepository
    private final IUserRepository userRepository; // Added UserRepository


    public TransactionService(IAuthenticationService authenticationService,
            IPaymentGateway paymentGateway,
            ITransactionRepository transactionRepository,
            IStoreRepository storeRepository,
            IUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.paymentGateway = paymentGateway;
        this.transactionRepository = transactionRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
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

    public Result<Void> purchaseShoppingCart(String sessionKey, String userEmail, String paymentDetails) {
        try {
            authenticationService.checkSessionKey(sessionKey);

            List<BasketDTO> cart = userRepository.getUserCart(userEmail);
            Map<BasketDTO, Double> basketToPrice = storeRepository.removeItemsFromStores(cart);

            double totalCost = basketToPrice.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            try {
                paymentGateway.processPayment(paymentDetails, totalCost);
            } catch (Exception e) {
                storeRepository.rollBackItemsToStores(cart);
                return Result.failure("Payment failed: " + e.getMessage());
            }

            for (Map.Entry<BasketDTO, Double> entry : basketToPrice.entrySet()) {
                BasketDTO basket = entry.getKey();
                double storeCost = entry.getValue();
                transactionRepository.addTransaction(basket.getBasketProducts(), storeCost, userEmail, basket.storeId());
            }

            userRepository.clearUserCart(userEmail);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
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
