package com.SEGroup.UI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks in-flight purchase bids waiting on all-owner approval.
 */
public class BidApprovalTracker {
    private record State(String buyerEmail, double amount,
                         AtomicInteger approvals, int totalOwners) {}

    // bidId → State
    private static final Map<String, State> map = new ConcurrentHashMap<>();

    /** Start tracking a new bid. */
    public static void create(String bidId, String buyerEmail, double amount, int totalOwners) {
        map.put(bidId, new State(buyerEmail, amount, new AtomicInteger(0), totalOwners));
    }

    /** Record one owner’s approval; returns true when everyone has approved. */
    public static boolean approve(String bidId) {
        State s = map.get(bidId);
        if (s == null) return false;
        return s.approvals.incrementAndGet() >= s.totalOwners;
    }

    /** One owner rejected: remove tracking and return buyerEmail. */
    public static String reject(String bidId) {
        State s = map.remove(bidId);
        return s != null ? s.buyerEmail : null;
    }

    /** Everyone approved: remove tracking and return buyerEmail. */
    public static String finish(String bidId) {
        State s = map.remove(bidId);
        return s != null ? s.buyerEmail : null;
    }

    /** Get the original bid amount for notification text. */
    public static double amount(String bidId) {
        State s = map.get(bidId);
        return s != null ? s.amount : 0;
    }
}
