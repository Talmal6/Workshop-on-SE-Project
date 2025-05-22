package com.SEGroup.UI;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * productId ➜ { bidderEmail, … }
 * Pure client-side cache so we know who bid.
 */
public final class GlobalAuctionRegistry {

    private static final Map<String, Set<String>> map = new ConcurrentHashMap<>();
    // in GlobalAuctionRegistry.java
    private static final Set<String> closed = ConcurrentHashMap.newKeySet();

    /** return true IFF this is the first caller to close this auction */
    public static boolean markClosed(String productId){
        return closed.add(productId);          // add() returns false if already closed
    }

    private GlobalAuctionRegistry() {}

    public static void addBidder(String productId, String email) {
        map.computeIfAbsent(productId, k -> ConcurrentHashMap.newKeySet())
                .add(email);
    }
    public static Set<String> getBidders(String productId) {
        return map.getOrDefault(productId, Set.of());
    }
    public static void clear(String productId) {
        map.remove(productId);
    }
}
