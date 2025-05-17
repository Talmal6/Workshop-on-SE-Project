package com.SEGroup.UI.Constants;

/**
 * Defines constants for notification channels
 */
public class NotificationChannels {
    // System-wide channels
    public static final String SYSTEM = "SYSTEM_NOTIFICATIONS";
    public static final String STORE = "STORE_NOTIFICATIONS";
    public static final String AUCTION = "AUCTION_NOTIFICATIONS";
    public static final String BID = "BID_NOTIFICATIONS";

    // Helper method to get the store channel for a specific store
    public static String forStore(String storeName) {
        return "STORE_" + storeName.toUpperCase().replace(" ", "_");
    }

    // Helper method to get the product channel for a specific product
    public static String forProduct(String productId) {
        return "PRODUCT_" + productId;
    }
}