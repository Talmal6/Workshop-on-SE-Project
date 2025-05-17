package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationWithSender;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

/**
 * Component that subscribes to notifications for the current user
 * and displays them in the UI.
 */
@SpringComponent
@UIScope
public class NotificationSubscriber {

    private final NotificationEndpoint notificationEndpoint;
    private final NotificationBroadcastService broadcastService;
    private final MainLayout mainLayout;
    private Disposable endpointSubscription;
    private Registration broadcastRegistration;

    @Autowired
    public NotificationSubscriber(NotificationEndpoint notificationEndpoint,
                                  NotificationBroadcastService broadcastService) {
        this.notificationEndpoint = notificationEndpoint;
        this.broadcastService = broadcastService;
        this.mainLayout = MainLayout.getInstance();
    }

    @PostConstruct
    public void init() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.addDetachListener(event -> cleanupSubscriptions());
        }

        // Subscribe to notifications if user is logged in
        if (SecurityContextHolder.isLoggedIn()) {
            System.out.println("NotificationSubscriber subscribing for: " +
                    SecurityContextHolder.email());
            try {
                subscribeToNotifications(SecurityContextHolder.email());
                System.out.println("Successfully subscribed to notifications");
            } catch (Exception e) {
                System.err.println("Error subscribing to notifications: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void cleanupSubscriptions() {
        if (endpointSubscription != null && !endpointSubscription.isDisposed()) {
            endpointSubscription.dispose();
        }

        if (broadcastRegistration != null) {
            broadcastRegistration.remove();
        }
    }

    public void subscribeToNotifications(String userId) {
        cleanupSubscriptions();

        final UI currentUI = UI.getCurrent(); // Make it effectively final
        if (currentUI != null) {
            try {
                // Primary subscription via endpoint
                if (notificationEndpoint != null) {
                    endpointSubscription = notificationEndpoint
                            .subscribe(userId)
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe(
                                    notification -> {
                                        if (currentUI.isAttached()) {
                                            System.out.println("Received via endpoint: " + notification);
                                            // Create a final copy of the notification for the lambda
                                            final Notification notificationCopy = notification;
                                            currentUI.access(() -> processNotification(notificationCopy));
                                        }
                                    },
                                    error -> System.err.println("Subscription error: " + error.getMessage()),
                                    () -> System.out.println("Subscription completed for: " + userId)
                            );
                }

                // Always register with broadcast service as backup
                broadcastRegistration = broadcastService.register(
                        userId,
                        currentUI,
                        notification -> {
                            System.out.println("Received via broadcast: " + notification);
                            processNotification(notification);
                        }
                );

                System.out.println("Successfully subscribed to all notification channels");
            } catch (Exception e) {
                System.err.println("ERROR subscribing to notifications: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot subscribe to notifications: no UI available");
        }
    }

    private void processNotification(Notification notification) {
        try {
            String message = notification.getMessage();
            String sender = "";

            // Extract sender if present
            if (notification instanceof NotificationWithSender) {
                sender = ((NotificationWithSender) notification).getSenderId();
                if (sender != null && !sender.isEmpty()) {
                    message = sender + ": " + message;
                }
            }

            // Create final copies for lambda use
            final String finalMessage = message;

            // Update UI with the notification
            if (mainLayout != null) {
                mainLayout.handleNewNotification(finalMessage);
            }

            // Show a toast notification with appropriate styling
            UI ui = UI.getCurrent();
            if (ui != null && ui.isAttached()) {
                ui.access(() -> {
                    com.vaadin.flow.component.notification.Notification toast =
                            com.vaadin.flow.component.notification.Notification.show(
                                    finalMessage,
                                    3000,
                                    com.vaadin.flow.component.notification.Notification.Position.TOP_END
                            );
                    toast.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
            }
        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
        }
    }
}