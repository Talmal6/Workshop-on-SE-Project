package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages bid approvals from multiple store owners
 */
@Component
public class BidApprovalManager {

    // Map of bidId -> Set of owners who approved
    private final Map<String, Set<String>> approvalMap = new ConcurrentHashMap<>();

    // Create a unique bid ID
    public String createBidId(String storeId, String productId, String bidderId, double amount) {
        return storeId + ":" + productId + ":" + bidderId + ":" + amount;
    }

    // Record an approval from an owner
    public synchronized boolean recordApproval(String bidId, String ownerId, int totalOwnersCount) {
        Set<String> approvals = approvalMap.computeIfAbsent(bidId, k -> ConcurrentHashMap.newKeySet());
        approvals.add(ownerId);

        // Check if all owners have approved
        return approvals.size() >= totalOwnersCount;
    }

    // Check if a bid is fully approved
    public boolean isFullyApproved(String bidId, int totalOwnersCount) {
        Set<String> approvals = approvalMap.get(bidId);
        return approvals != null && approvals.size() >= totalOwnersCount;
    }

    // Get the number of approvals for a bid
    public int getApprovalCount(String bidId) {
        Set<String> approvals = approvalMap.get(bidId);
        return approvals != null ? approvals.size() : 0;
    }

    // Remove a bid from tracking (after finalized)
    public void removeBid(String bidId) {
        approvalMap.remove(bidId);
    }
}