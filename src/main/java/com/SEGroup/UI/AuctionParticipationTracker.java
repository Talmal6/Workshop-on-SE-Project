package com.SEGroup.UI;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records every auction (product-ID) in which the current user has placed ≥ 1 bid
 * so we can later decide whether to show AUCTION_* notifications.
 */
public final class AuctionParticipationTracker {

    private static final Set<String> participated = ConcurrentHashMap.newKeySet();

    private AuctionParticipationTracker() { }

    public static void mark(String productId)      { participated.add(productId); }
    public static boolean has(String productId)    { return participated.contains(productId); }
    public static void reset()                     { participated.clear(); }   // call on logout
}
