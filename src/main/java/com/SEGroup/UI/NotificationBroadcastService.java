package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Service to broadcast notifications to Vaadin UIs.
 * This handles the push functionality for real-time notifications.
 */
@Service
public class NotificationBroadcastService {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String, Map<Integer, Consumer<Notification>>> listeners = new ConcurrentHashMap<>();

    /**
     * Register a UI to receive notifications for a specific user.
     *
     * @param userId The ID of the user to receive notifications
     * @param ui The UI instance to update
     * @param listener The callback to handle the notification
     * @return A registration object that can be used to remove the listener
     */
    public Registration register(String userId, UI ui, Consumer<Notification> listener) {
        // Get or create a map of UI listeners for this user
        Map<Integer, Consumer<Notification>> userListeners = listeners.computeIfAbsent(
                userId, id -> new ConcurrentHashMap<>());

        // Add this UI's listener
        userListeners.put(ui.getUIId(), listener);

        // Return a registration object to remove this listener
        return () -> {
            synchronized (listeners) {
                Map<Integer, Consumer<Notification>> userMap = listeners.get(userId);
                if (userMap != null) {
                    userMap.remove(ui.getUIId());
                    if (userMap.isEmpty()) {
                        listeners.remove(userId);
                    }
                }
            }
        };
    }

    /**
     * Broadcast a notification to all registered UIs for a specific user.
     *
     * @param userId The ID of the user to send the notification to
     * @param notification The notification to send
     */
    public void broadcast(String userId, Notification notification) {
        if (userId == null || notification == null) return;

        // Get the listeners for this user
        Map<Integer, Consumer<Notification>> userListeners = listeners.get(userId);
        if (userListeners != null) {
            // Schedule the broadcast on a background thread
            executor.execute(() -> {
                userListeners.forEach((uiId, listener) -> {
                    try {
                        // Find the UI by ID
                        UI ui = UI.getCurrent();
                        if (ui != null && ui.getUIId() == uiId && ui.isAttached()) {
                            // Update the UI safely
                            ui.access(() -> listener.accept(notification));
                        }
                    } catch (Exception e) {
                        System.err.println("Error broadcasting notification to UI " + uiId + ": " + e.getMessage());
                    }
                });
            });
        }
    }
}