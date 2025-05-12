package com.SEGroup.UI;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationWithSender;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.core.Disposable;
import jakarta.annotation.PostConstruct;
/**
 * Component that subscribes to notifications for the current user
 * and displays them in the UI.
 */
@SpringComponent
@UIScope
public class NotificationSubscriber {

    private final NotificationEndpoint notificationEndpoint;
    private final MainLayout mainLayout;
    private Disposable subscription;
    @Autowired
    public NotificationSubscriber(NotificationEndpoint notificationEndpoint) {
        this.notificationEndpoint = notificationEndpoint;
        this.mainLayout = MainLayout.getInstance();
    }

    /**
     * Initialize the notification subscriber when the application context is ready.
     * This replaces the @PostConstruct annotation.
     */
    @PostConstruct
    public void init() {
        // dispose when THIS UI detaches:
        UI ui = UI.getCurrent();
        ui.addDetachListener(event -> {
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        });

        if (SecurityContextHolder.isLoggedIn()) {
            subscribeToNotifications(SecurityContextHolder.email());
        }
    }

    /**
     * Subscribe to notifications for the given user ID.
     *
     * @param userId The user ID to subscribe for
     */
    public void subscribeToNotifications(String userId) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        subscription = notificationEndpoint
                .subscribe(userId)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(notification -> {
                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        ui.access(() -> processNotification(notification));
                    }
                });
    }
    /**
     * Process a received notification by displaying it in the UI
     * and incrementing the notification counter.
     *
     * @param notification The notification to process
     */
    private void processNotification(Notification notification) {
        String message = notification.getMessage();
        String sender = "";

        // Extract sender if present
        if (notification instanceof NotificationWithSender) {
            sender = ((NotificationWithSender) notification).getSenderId();
            message = sender + ": " + message;
        }

        // Update UI with the notification
        mainLayout.handleNewNotification(message);

        // Show a toast notification
        com.vaadin.flow.component.notification.Notification toast =
                com.vaadin.flow.component.notification.Notification.show(
                        message,
                        3000,
                        com.vaadin.flow.component.notification.Notification.Position.TOP_END
                );
        toast.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }




}