package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.BidDTO;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationType;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.BidApprovalManager;
import com.SEGroup.UI.Constants.BidRequest;
import com.SEGroup.UI.DirectNotificationSender;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.BidUsersView;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public class BidUsersPresenter {
    private static final Logger logger = Logger.getLogger(BidUsersPresenter.class.getName());
    private final BidUsersView view;
    private final StoreService storeService;
    private final String storeName;
    private final String productId;
    private final TransactionService transactionService;
    private final DirectNotificationSender notificationSender;

    @Autowired
    private BidApprovalManager approvalManager;
    private int totalOwnersCount = 0;

    public BidUsersPresenter(BidUsersView view, String storeName, String productId) {
        this.view = view;
        this.storeName = storeName;
        this.productId = productId;
        this.storeService = ServiceLocator.getStoreService();
        this.transactionService = ServiceLocator.getTransactionService();
        this.notificationSender = ServiceLocator.getDirectNotificationSender();
        this.approvalManager = ServiceLocator.getBidApprovalManager();

        // Load total owners count
        loadTotalOwnersCount();
    }

    private void loadTotalOwnersCount() {
        try {
            Result<List<String>> ownersResult = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    this.storeName,
                    SecurityContextHolder.email()
            );

            if (ownersResult.isSuccess() && ownersResult.getData() != null) {
                this.totalOwnersCount = ownersResult.getData().size();
                logger.info("Store has " + totalOwnersCount + " owners for approval tracking");
            } else {
                this.totalOwnersCount = 1; // Fallback to single owner
                logger.warning("Failed to get owners count, defaulting to 1");
            }
        } catch (Exception e) {
            this.totalOwnersCount = 1; // Fallback to single owner
            logger.warning("Error getting owners count: " + e.getMessage());
        }
    }

    public void loadBidUsers() {
        Result<List<BidDTO>> r = storeService.getProductBids(
                SecurityContextHolder.token(),
                this.storeName,
                this.productId
        );

        if (r.isSuccess()) {
            List<BidRequest> requests = r.getData().stream()
                    .map(dto -> new BidRequest(dto.getBidderEmail(), dto.getPrice()))
                    .toList();
            view.displayBidUsers(requests);
        } else {
            view.showError("Failed to load bids: " + r.getErrorMessage());
        }
    }

    public void acceptBid(String userEmail, double amount) {
        // First, log the action for debugging
        logger.info("Accepting bid from " + userEmail + " for $" + amount + " on product " + productId);

        // Create a unique bid ID
        String bidId = storeName + ":" + productId + ":" + userEmail + ":" + amount;

        // Record the current owner's approval
        boolean allApproved = approvalManager.recordApproval(
                bidId,
                SecurityContextHolder.email(),
                totalOwnersCount
        );

        int currentApprovals = approvalManager.getApprovalCount(bidId);
        logger.info("Current approvals: " + currentApprovals + " of " + totalOwnersCount + " required");

        if (allApproved) {
            // All owners have approved, finalize the transaction
            Result<Void> res = transactionService.acceptBid(
                    SecurityContextHolder.token(),
                    this.storeName,
                    new BidDTO(userEmail, this.productId, amount)
            );

            if (res.isSuccess()) {
                // Get the current user (owner's email)
                String ownerEmail = SecurityContextHolder.email();

                // Send notification to the bidder directly
                logger.info("Sending BID_ACCEPTED notification to " + userEmail);
                notificationSender.send(
                        NotificationType.BID_ACCEPTED,
                        userEmail,  // Target bidder specifically
                        "Your bid of $" + String.format("%,.2f", amount) + " was accepted by " + ownerEmail,
                        amount,
                        productId,
                        ownerEmail  // Include owner's email as extra info
                );

                // Remove the bid from approval tracking
                approvalManager.removeBid(bidId);

                // Remove this bid from the grid
                view.displayBidUsers(
                        view.usersGrid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                                .filter(b -> !b.email().equals(userEmail))
                                .collect(Collectors.toList())
                );

                view.showSuccess("Accepted " + userEmail + "'s bid of $" + String.format("%,.2f", amount));
            } else {
                view.showError("Could not accept bid: " + res.getErrorMessage());
            }
        } else {
            // Send notifications to other owners requesting approval
            sendApprovalRequestToOtherOwners(userEmail, amount);

            // Notify this owner that more approvals are needed
            view.showInfo("Your approval has been recorded. " +
                    (totalOwnersCount - currentApprovals) + " more approval(s) needed.");
        }
    }

    private void sendApprovalRequestToOtherOwners(String bidderEmail, double amount) {
        try {
            Result<List<String>> ownersResult = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    this.storeName,
                    SecurityContextHolder.email()
            );

            if (ownersResult.isSuccess() && ownersResult.getData() != null) {
                String currentOwner = SecurityContextHolder.email();

                for (String ownerEmail : ownersResult.getData()) {
                    // Don't send request to the current owner
                    if (!ownerEmail.equals(currentOwner)) {
                        // Send notification to each owner
                        notificationSender.send(
                                NotificationType.BID_APPROVAL_NEEDED,
                                ownerEmail,
                                "Approval needed: " + currentOwner + " approved bid of $" +
                                        String.format("%,.2f", amount) + " from " + bidderEmail,
                                amount,
                                productId,
                                bidderEmail  // Include bidder's email as extra info
                        );
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error sending approval requests: " + e.getMessage());
        }
    }

    public void rejectBid(String userEmail, double amount) {
        // First, log the action for debugging
        logger.info("Rejecting bid from " + userEmail + " for $" + amount + " on product " + productId);

        Result<Void> res = transactionService.rejectBid(
                SecurityContextHolder.token(),
                this.storeName,
                new BidDTO(userEmail, this.productId, amount)
        );

        if (res.isSuccess()) {
            // Get the current user (owner's email)
            String ownerEmail = SecurityContextHolder.email();

            // Send notification to the bidder directly
            logger.info("Sending BID_REJECTED notification to " + userEmail);
            notificationSender.send(
                    NotificationType.BID_REJECTED,
                    userEmail,  // Target bidder specifically
                    "Your bid of $" + String.format("%,.2f", amount) + " was rejected by " + ownerEmail,
                    amount,
                    productId,
                    ownerEmail  // Include owner's email as extra info
            );

            // Remove this bid from the grid
            view.displayBidUsers(
                    view.usersGrid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                            .filter(b -> !b.email().equals(userEmail))
                            .collect(Collectors.toList())
            );

            view.showSuccess("Rejected " + userEmail + "'s bid of $" + String.format("%,.2f", amount));
        } else {
            view.showError("Could not reject bid: " + res.getErrorMessage());
        }
    }

    public void counterOffer(String userEmail, double originalAmount, double counterAmount, String message) {
        logger.info("Sending counter-offer of $" + counterAmount + " to " + userEmail);

        // Send the counter-offer notification
        notificationSender.send(
                NotificationType.BID_COUNTER,
                userEmail,
                message != null && !message.isEmpty() ? message :
                        "Counter-offer: $" + String.format("%,.2f", counterAmount),
                counterAmount,
                productId,
                SecurityContextHolder.email()  // Include owner's email as extra info
        );

        view.showSuccess("Counter-offer of $" + String.format("%,.2f", counterAmount) +
                " sent to " + userEmail);
    }
}