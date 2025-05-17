package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationWithSender;
import com.SEGroup.Infrastructure.NotificationCenter.RichNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter that connects domain notifications to UI components.
 * Listens to system channels and forwards to individual users.
 */
@Component
public class NotificationAdapter {

    private final NotificationEndpoint notificationEndpoint;
    private final NotificationBroadcastService broadcastService;
    private final com.SEGroup.UI.NotificationCenter uiNotificationCenter;
    private final Map<String, Disposable> channelSubscriptions = new HashMap<>();

    @Autowired
    public NotificationAdapter(
            NotificationEndpoint notificationEndpoint,
            NotificationBroadcastService broadcastService,
            @Qualifier("uiNotificationCenter") com.SEGroup.UI.NotificationCenter uiNotificationCenter) {
        this.notificationEndpoint = notificationEndpoint;
        this.broadcastService = broadcastService;
        this.uiNotificationCenter = uiNotificationCenter;
    }

    @PostConstruct
    public void init() {
        try {
            // Subscribe to system-wide channels
            subscribeToChannel("SYSTEM_NOTIFICATIONS");
            subscribeToChannel("STORE_NOTIFICATIONS");
            subscribeToChannel("AUCTION_NOTIFICATIONS");
            subscribeToChannel("BID_NOTIFICATIONS");
            subscribeToChannel("ADMIN_CHANNEL");

            // Subscribe to all store-specific channels
            subscribeToStoreChannels();

            System.out.println("Notification channels initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize notification channels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void subscribeToStoreChannels() {
        // This could be enhanced to dynamically load all store names from a service
        // For now, we'll just subscribe to the Demo Store channel
        subscribeToChannel("STORE_DEMO_STORE");
    }

    private void subscribeToChannel(String channelId) {
        Disposable subscription = notificationEndpoint.subscribe(channelId)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        notification -> processChannelNotification(channelId, notification),
                        error -> System.err.println("Error in channel " + channelId + ": " + error.getMessage()),
                        () -> System.out.println("Channel " + channelId + " subscription completed")
                );

        channelSubscriptions.put(channelId, subscription);
        System.out.println("Subscribed to channel: " + channelId);
    }

    private void processChannelNotification(String channelId, Notification notification) {
        System.out.println("Received on " + channelId + ": " + notification);

        try {
            // Extract target recipients from the notification
            if (channelId.startsWith("STORE_")) {
                // Store notifications go to all store owners
                distributeStoreNotification(channelId, notification);
            } else if (channelId.equals("SYSTEM_NOTIFICATIONS")) {
                // System notifications may have specific recipients
                String targetUser = extractTargetUserFromContent(notification);
                if (targetUser != null) {
                    broadcastService.broadcast(targetUser, notification);
                }
            } else if (channelId.equals("BID_NOTIFICATIONS")) {
                // Bid notifications should go to store owners and the bidder
                distributeBidNotification(notification);
            } else if (channelId.equals("AUCTION_NOTIFICATIONS")) {
                // Auction notifications go to all participants
                distributeAuctionNotification(notification);
            }
        } catch (Exception e) {
            System.err.println("Error processing channel notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void distributeStoreNotification(String storeChannel, Notification notification) {
        // Extract store name from channel ID
        String storeName = storeChannel.replace("STORE_", "").replace("_", " ");

        try {
            // In a real implementation, get all owners for this store
            // For demo, we'll broadcast to some hardcoded owners
            broadcastService.broadcast("owner@demo.com", notification);
            broadcastService.broadcast("co-owner@demo.com", notification);
        } catch (Exception e) {
            System.err.println("Error distributing store notification: " + e.getMessage());
        }
    }

    private void distributeBidNotification(Notification notification) {
        // For bid notifications, we need to extract the target store and bidder
        if (notification instanceof RichNotification rich) {
            // For demo, we'll broadcast to hardcoded owners
            broadcastService.broadcast("owner@demo.com", notification);
            broadcastService.broadcast("co-owner@demo.com", notification);

            // Also notify the bidder of any changes
            if (rich.getExtra() != null && rich.getExtra().contains("@")) {
                broadcastService.broadcast(rich.getExtra(), notification);
            }
        }
    }

    private void distributeAuctionNotification(Notification notification) {
        // For auction notifications, notify all bidders and store owners
        if (notification instanceof RichNotification rich) {
            // Broadcast to store owners
            broadcastService.broadcast("owner@demo.com", notification);
            broadcastService.broadcast("co-owner@demo.com", notification);

            // If there's a targeted bidder, notify them specifically
            if (rich.getExtra() != null && rich.getExtra().contains("@")) {
                broadcastService.broadcast(rich.getExtra(), notification);
            }

            // For auction outbid notifications, target previous highest bidder
            if (rich.getSenderId() != null && rich.getSenderId().contains("@")) {
                broadcastService.broadcast(rich.getSenderId(), notification);
            }
        }
    }

    private String extractTargetUserFromContent(Notification notification) {
        // First check if the notification already has a receiver ID
        if (notification.getReceiverId() != null &&
                !notification.getReceiverId().startsWith("SYSTEM_") &&
                !notification.getReceiverId().startsWith("STORE_") &&
                !notification.getReceiverId().isEmpty()) {
            return notification.getReceiverId();
        }

        // Extract from message content
        String content = notification.getMessage();
        if (content != null) {
            // Check for explicit target
            if (content.contains("TARGET_USER:")) {
                return content.split("TARGET_USER:")[1].trim().split("\\s+")[0];
            }

            // Look for email addresses
            if (content.contains("@")) {
                for (String word : content.split("\\s+")) {
                    if (word.contains("@")) {
                        return word.trim().replace(",", "").replace(".", "");
                    }
                }
            }
        }

        // Check rich notification fields
        if (notification instanceof RichNotification rich) {
            if (rich.getExtra() != null && rich.getExtra().contains("@")) {
                return rich.getExtra();
            }
            if (rich.getSenderId() != null && rich.getSenderId().contains("@")) {
                return rich.getSenderId();
            }
        }

        return null;
    }
}