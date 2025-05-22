package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.RichNotification;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class NotificationBroadcastService {
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Store notification history per user
    private final Map<String, List<Notification>> history = new ConcurrentHashMap<>();
    private final Map<String, List<Notification>> auctionHistory = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, UIListenerPair>> listeners = new ConcurrentHashMap<>();

    // Class to keep UI and listener together
    private static class UIListenerPair {
        final UI ui;
        final Consumer<Notification> listener;

        UIListenerPair(UI ui, Consumer<Notification> listener) {
            this.ui = ui;
            this.listener = listener;
        }
    }

    /**
     * Register a UI to receive notifications for a specific user
     */
    public Registration register(String userId, UI ui, Consumer<Notification> listener) {
        System.out.println("Registering UI " + ui.getUIId() + " for user " + userId);

        Map<Integer, UIListenerPair> userListeners = listeners.computeIfAbsent(
                userId, id -> new ConcurrentHashMap<>());

        userListeners.put(ui.getUIId(), new UIListenerPair(ui, listener));

        return () -> {
            Map<Integer, UIListenerPair> userMap = listeners.get(userId);
            if (userMap != null) {
                userMap.remove(ui.getUIId());
                if (userMap.isEmpty()) {
                    listeners.remove(userId);
                }
            }
        };
    }

    /**
     * Broadcast a notification to all registered UIs for a specific user
     * and store in history
     */
    public void broadcast(String userId, Notification notification) {
        if (userId == null || notification == null) return;

        System.out.println("Broadcasting to " + userId + ": " + notification);
        System.out.println("Broadcasting: " + notification.toString());
        // Store notification in appropriate history collection
        boolean isAuction = false;
        if (notification instanceof RichNotification) {
            RichNotification rich = (RichNotification) notification;
            String typeName = rich.getType().name();
            isAuction = typeName.startsWith("AUCTION_");
        }

        // Add to the appropriate history
        if (isAuction) {
            auctionHistory
                    .computeIfAbsent(userId, id -> Collections.synchronizedList(new ArrayList<>()))
                    .add(notification);
            System.out.println("Added to auction history for " + userId + ": " + notification);
        } else {
            history
                    .computeIfAbsent(userId, id -> Collections.synchronizedList(new ArrayList<>()))
                    .add(notification);
            System.out.println("Added to regular history for " + userId + ": " + notification);
        }

        // Get the listeners for this user
        Map<Integer, UIListenerPair> userListeners = listeners.get(userId);

        if (userListeners != null) {
            // Schedule the broadcast on a background thread
            executor.execute(() -> {
                userListeners.forEach((uiId, pair) -> {
                    try {
                        if (pair.ui != null && pair.ui.isAttached()) {
                            pair.ui.access(() -> {
                                System.out.println("Delivering notification to UI " + uiId);
                                pair.listener.accept(notification);
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Error broadcasting to UI " + uiId + ": " + e.getMessage());
                    }
                });
            });
        }
    }

    /**
     * Get notification history for a user
     */
    public List<Notification> getHistory(String userId) {
        List<Notification> userHistory = history.get(userId);
        System.out.println("Getting history for " + userId + ": " +
                (userHistory != null ? userHistory.size() : 0) + " notifications");
        return userHistory != null ? userHistory : Collections.emptyList();
    }

    /**
     * Get auction notification history for a user
     */
    public List<Notification> getAuctionHistory(String userId) {
        List<Notification> userAuctionHistory = auctionHistory.get(userId);
        System.out.println("Getting auction history for " + userId + ": " +
                (userAuctionHistory != null ? userAuctionHistory.size() : 0) + " notifications");
        return userAuctionHistory != null ? userAuctionHistory : Collections.emptyList();
    }

    /**
     * Remove a notification from a user's history
     */
    public void removeFromHistory(String userId, String message, double price, String productId) {
        List<Notification> userHistory = history.get(userId);
        if (userHistory != null) {
            userHistory.removeIf(n -> matchesNotification(n, message, price, productId));
            System.out.println("Removed notification from history for " + userId);
        }
    }

    /**
     * Remove a notification from a user's auction history
     */
    public void removeFromAuctionHistory(String userId, String message, double price, String productId) {
        List<Notification> userAuctionHistory = auctionHistory.get(userId);
        if (userAuctionHistory != null) {
            userAuctionHistory.removeIf(n -> matchesNotification(n, message, price, productId));
            System.out.println("Removed notification from auction history for " + userId);
        }
    }

    /**
     * Check if a notification matches specified criteria
     */
    private boolean matchesNotification(Notification n, String message, double price, String productId) {
        if (n.getMessage().equals(message)) {
            if (n instanceof RichNotification rich) {
                return Math.abs(rich.getPrice() - price) < 0.001 &&
                        Objects.equals(rich.getProductId(), productId);
            }
            return true;
        }
        return false;
    }

    public void broadcastMany(Set<String> userIds, Notification n) {
        userIds.forEach(uid -> broadcast(uid, n));
    }

    public void broadcastToAll(Notification n){
        listeners.keySet().forEach(uid -> broadcast(uid, n));
    }
}