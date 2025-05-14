package com.SEGroup.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.units.qual.t;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.BidDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Domain.IPaymentGateway;
import com.SEGroup.Domain.IStoreRepository;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.IShippingService;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import org.springframework.stereotype.Service;

/**
 * TransactionService handles the operations related to transactions, including
 * processing payments, viewing transaction history, and purchasing shopping
 * carts.
 * It interacts with authentication, payment gateway, transaction repository,
 * store repository, and user repository.
 */
@Service
public class TransactionService {
    private final IAuthenticationService authenticationService;
    private final IPaymentGateway paymentGateway;
    private final ITransactionRepository transactionRepository;
    private final IStoreRepository storeRepository; // Added StoreRepository
    private final IUserRepository userRepository; // Added UserRepository
    private final IShippingService shippingService; // Added ShippingService
    private final NotificationCenter notificationService;

    /**
     * Constructs a new TransactionService instance with the provided dependencies.
     *
     * @param authenticationService The authentication service for user validation.
     * @param paymentGateway        The payment gateway for processing payments.
     * @param transactionRepository The transaction repository for storing and
     *                              retrieving transactions.
     * @param storeRepository       The store repository for handling store-related
     *                              actions.
     * @param userRepository        The user repository for managing user data.
     */
    public TransactionService(IAuthenticationService authenticationService,
            IPaymentGateway paymentGateway,
            ITransactionRepository transactionRepository,
            IStoreRepository storeRepository,
            IUserRepository userRepository,
            IShippingService shippingService,
            NotificationCenter notificationService) {
        this.authenticationService = authenticationService;
        this.paymentGateway = paymentGateway;
        this.transactionRepository = transactionRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.shippingService = shippingService; // Initialize ShippingService
        this.notificationService = notificationService;
    }

    /**
     * Retrieves the transaction history for a given user.
     * Logs the retrieval of transaction history.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param userEmail  The email of the user whose transaction history is to be
     *                   retrieved.
     * @return A Result object containing the transaction history if successful, or
     *         an error message.
     */
    public Result<List<TransactionDTO>> getTransactionHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);

            LoggerWrapper.info("Fetching transaction history for user: " + userEmail); // Log transaction history
                                                                                       // retrieval
            List<TransactionDTO> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving transaction history for user: " + userEmail + " - " + e.getMessage(),
                    e); // Log error
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Processes the purchase of a shopping cart.
     * Logs the purchase process and any errors.
     *
     * @param sessionKey     The session key of the authenticated user.
     * @param userEmail      The email of the user making the purchase.
     * @param paymentDetails The payment details used for the transaction.
     * @return A Result object indicating success or failure of the operation.
     */
    public Result<Void> purchaseShoppingCart(String sessionKey, String userEmail, String paymentDetails) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.checkUserSuspension(authenticationService.getUserBySession(sessionKey));
            LoggerWrapper.info("Initiating purchase for user: " + userEmail); // Log the start of the purchase

            List<BasketDTO> cart = userRepository.getUserCart(userEmail);
            Map<BasketDTO, Double> basketToPrice = storeRepository.removeItemsFromStores(cart);
            double totalCost = basketToPrice.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            try {
                for (BasketDTO basket : cart) {
                    shippingService.ship(basket, userEmail);
                }
                try {

                    paymentGateway.processPayment(paymentDetails, totalCost);
                    LoggerWrapper.info("Payment processed for user: " + userEmail + ", Amount: " + totalCost); // Log
                                                                                                               // successful
                                                                                                               // payment
                                                                                                               // processing
                } catch (Exception e) {
                    storeRepository.rollBackItemsToStores(cart);
                    LoggerWrapper.error("Payment failed for user: " + userEmail + ", Error: " + e.getMessage(), e); // Log
                                                                                                                    // payment
                                                                                                                    // failure
                    throw new RuntimeException("Payment failed: " + e.getMessage());
                }
            } catch (Exception e) {
                for (BasketDTO basket : cart) {
                    shippingService.cancelShipping(basket, userEmail); // Rollback items to stores in case of shipping
                                                                       // failure
                }
                return Result.failure(e.getMessage());
            }

            for (Map.Entry<BasketDTO, Double> entry : basketToPrice.entrySet()) {
                BasketDTO basket = entry.getKey();
                double storeCost = entry.getValue();
                String storeFounder = storeRepository.getStoreFounder(basket.storeId());
                notificationService.sendSystemNotification(
                        storeFounder,
                        "A product has been purchased from your store '" + basket.getBasketProducts() + "'.");
                transactionRepository.addTransaction(basket.getBasketProducts(), storeCost, userEmail,
                        basket.storeId());
                LoggerWrapper.info("Transaction added for user: " + userEmail + ", Store: " + basket.storeId()); // Log
                                                                                                                 // successful
                                                                                                                 // transaction
                                                                                                                 // addition
            }

            userRepository.clearUserCart(userEmail);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error(
                    "Error processing shopping cart purchase for user: " + userEmail + " - " + e.getMessage(), e); // Log
                                                                                                                   // error
            return Result.failure(e.getMessage());
        }
    }

    /**
     * Views the purchase history for a given user.
     * Logs the process of viewing the purchase history.
     *
     * @param sessionKey The session key of the authenticated user.
     * @param userEmail  The email of the user whose purchase history is to be
     *                   viewed.
     * @return A Result object containing the user's purchase history if successful,
     *         or an error message.
     */
    public Result<List<TransactionDTO>> viewPurcaseHistory(String sessionKey, String userEmail) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            LoggerWrapper.info("Fetching purchase history for user: " + userEmail); // Log the fetching of purchase
                                                                                    // history
            List<TransactionDTO> transactions = transactionRepository.getTransactionsByUserEmail(userEmail);
            return Result.success(transactions);
        } catch (Exception e) {
            LoggerWrapper.error("Error retrieving purchase history for user: " + userEmail + " - " + e.getMessage(), e); // Log
                                                                                                                         // error
            return Result.failure(e.getMessage());
        }
    }

    /*
     * 
     * 
     */

    public Result<Void> acceptBid(String sessionKey, String storeName, BidDTO bidDTO) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String userEmail = authenticationService.getUserBySession(sessionKey);
            storeRepository.acceptBid(storeName, userEmail, bidDTO.getProductId(), bidDTO);
            double cost = bidDTO.getPrice()*bidDTO.getQuantity();
            try {
                shippingService.ship(storeName, userEmail, bidDTO.getProductId());

            } catch (Exception e) {
                LoggerWrapper.error("Shipping failed for bid acceptance: " + e.getMessage(), e); // Log shipping failure
                throw new RuntimeException("Shipping failed: " + e.getMessage());
            }
            try {
                paymentGateway.processPayment(userEmail, cost);
                LoggerWrapper.info("Payment processed for bid acceptance: " + userEmail + ", Amount: " + cost); // Log
                                                                                                                // successful
                                                                                                                // payment
                                                                                                                // processing
            } catch (Exception e) {
                LoggerWrapper.error("Payment failed for bid acceptance: " + e.getMessage(), e); // Log payment failure
                throw new RuntimeException("Payment failed: " + e.getMessage());
            }

            String storeFounder = storeRepository.getStoreFounder(storeName);
            notificationService.sendSystemNotification(
                    storeFounder,
                    "A bid has been accepted for product '" + bidDTO.getProductId() + "' in your store '" + storeName
                            + "'.");
            String user = bidDTO.getBidderEmail();
            notificationService.sendSystemNotification(
                    userRepository.getUserName(userEmail),
                    "Your bid for product '" + bidDTO.getProductId() + "' in store '" + storeName
                            + "' has been accepted.");
            transactionRepository.addTransaction(List.of(bidDTO.getProductId()), cost, bidDTO.getBidderEmail(), storeName);
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error accepting bid for store: " + storeName + " - " + e.getMessage(), e); // Log error
            storeRepository.rollBackByBid(storeName, bidDTO);
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> executeAuction(String sessionKey, String storeName, BidDTO bidDTO) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String userEmail = authenticationService.getUserBySession(sessionKey);
            storeRepository.executeAuctionBid(userEmail, storeName, bidDTO);
            double cost = bidDTO.getPrice()*bidDTO.getQuantity();
            try {
                shippingService.ship(storeName, userEmail, bidDTO.getProductId());

            } catch (Exception e) {
                LoggerWrapper.error("Shipping failed for bid acceptance: " + e.getMessage(), e); // Log shipping failure
                throw new RuntimeException("Shipping failed: " + e.getMessage());
            }
            try {
                paymentGateway.processPayment(userEmail, cost);
                LoggerWrapper.info("Payment processed for bid acceptance: " + userEmail + ", Amount: " + cost); // Log
                                                                                                                // successful
                                                                                                                // payment
                                                                                                                // processing
            } catch (Exception e) {
                LoggerWrapper.error("Payment failed for bid acceptance: " + e.getMessage(), e); // Log payment failure
                throw new RuntimeException("Payment failed: " + e.getMessage());
            }

            // Notify the store founder and the user about the accepted bid
            // Assuming you have a method to get the store founder's email
            String storeFounder = storeRepository.getStoreFounder(storeName);
            notificationService.sendSystemNotification(
                    storeFounder,
                    "A bid has been accepted for product '" + bidDTO.getProductId() + "' in your store '" + storeName
                            + "'.");
            String user = bidDTO.getBidderEmail();
            notificationService.sendSystemNotification(
                    userRepository.getUserName(bidDTO.getBidderEmail()),
                    "Your bid for product '" + bidDTO.getProductId() + "' in store '" + storeName
                            + "' has been accepted.");

            transactionRepository.addTransaction(List.of(bidDTO.getProductId()), cost, bidDTO.getBidderEmail(), storeName);

            return Result.success(null); // Ensure success is returned if no exceptions occur
        } catch (Exception e) {
            LoggerWrapper.error("Error accepting bid for store: " + storeName + " - " + e.getMessage(), e); // Log error
            storeRepository.rollBackByBid(storeName, bidDTO);
            return Result.failure(e.getMessage());
        }
    }

    public Result<Void> rejectBid(String sessionKey, String storeName, BidDTO bidDTO) {
        try {
            authenticationService.checkSessionKey(sessionKey);
            String userEmail = authenticationService.getUserBySession(sessionKey);
            // storeRepository.rejectBid(storeName, userEmail, bidDTO.getProductId(),
            // bidDTO);

            storeRepository.rejectBid(userEmail, storeName, bidDTO);
            notificationService.sendSystemNotification(
                    userRepository.getUserName(bidDTO.getBidderEmail()),
                    "Your bid for product '" + bidDTO.getProductId() + "' in store '" + storeName
                            + "' has been rejected.");
            return Result.success(null);
        } catch (Exception e) {
            LoggerWrapper.error("Error rejecting bid for store: " + storeName + " - " + e.getMessage(), e); // Log error
            return Result.failure(e.getMessage());
        }
    }

}
