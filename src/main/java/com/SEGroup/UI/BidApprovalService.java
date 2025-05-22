package com.SEGroup.UI;

import com.SEGroup.UI.BidApprovalTracker;
import com.SEGroup.UI.DirectNotificationSender;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationType;

import org.springframework.stereotype.Service;

@Service
public class BidApprovalService {
    private final DirectNotificationSender sender;

    public BidApprovalService(DirectNotificationSender sender) {
        this.sender = sender;
    }

    public void approve(String bidId, String ownerEmail, String productId, double amount) {
        boolean nowAll = BidApprovalTracker.approve(bidId);
        if (!nowAll) return;        // still waiting on others
        String buyer = BidApprovalTracker.finish(bidId);
        sender.send(
                NotificationType.BID_ACCEPTED,
                buyer,
                "Your offer of $" + String.format("%.2f",amount)
                        + " has been accepted by all owners!",
                amount,
                productId,
                null
        );
    }

    public void reject(String bidId, String ownerEmail, String productId) {
        String buyer = BidApprovalTracker.reject(bidId);
        if (buyer == null) return;
        double amount = BidApprovalTracker.amount(bidId);
        sender.send(
                NotificationType.BID_REJECTED,
                buyer,
                "Your offer of $" + String.format("%.2f",amount)
                        + " was rejected by " + ownerEmail,
                amount,
                productId,
                null
        );
    }
}
