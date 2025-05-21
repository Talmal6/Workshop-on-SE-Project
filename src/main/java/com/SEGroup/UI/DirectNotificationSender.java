package com.SEGroup.UI;

import com.SEGroup.Domain.INotificationCenter;
import com.SEGroup.Infrastructure.NotificationCenter.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publishes notifications in two steps:
 *   ① live push   → NotificationEndpoint (web-socket)
 *   ② store copy  → NotificationBroadcastService (history + fan-out)
 */
@Service
public class DirectNotificationSender {

    @Autowired NotificationEndpoint endpoint;
    @Autowired NotificationBroadcastService broadcast;
    @Autowired(required = false)
    INotificationCenter domainCenter;

    private static final Logger log = Logger.getLogger(DirectNotificationSender.class.getName());

    /* Plain system text notification */
    public void sendSystemNotification(String receiverId, String message) {
        if (receiverId == null || receiverId.isBlank() || message == null) return;
        push(new Notification(message, receiverId));
    }

    /* Rich / typed notification to ONE user */
    public void send(NotificationType type,
                     String receiverId,
                     String message,
                     double price,
                     String productId,
                     String extra)
    {
        if (receiverId == null || receiverId.isBlank()) return;
        if (message == null || message.isBlank()) message = type.name();

        String sender = SecurityContextHolder.email();  // may be null (system)
        RichNotification n = new RichNotification(
                receiverId, sender, message,
                type, price, productId, extra);

        push(n);
    }

    /* Same rich notification to MANY users */
    public void sendToMany(Collection<String> receivers,
                           NotificationType type,
                           String message,
                           double price,
                           String productId,
                           String extra)
    {
        if (receivers == null || receivers.isEmpty()) {
            log.warning("No receivers specified for notification");
            return;
        }

        // Safely send to each non-null receiver
        receivers.stream()
                .filter(Objects::nonNull)
                .forEach(r -> {
                    try {
                        send(type, r, message, price, productId, extra);
                    } catch (Exception e) {
                        log.warning("Failed to send to " + r + ": " + e.getMessage());
                    }
                });
    }

    /* Internal helper – do the actual work */
    private void push(Notification n) {
        System.out.println("DirectNotificationSender.push: " + n);

        /* ① live push over WebSocket */
        try {
            if (endpoint != null) {
                System.out.println("Publishing to endpoint: " + n);
                endpoint.publish(n);
            } else {
                System.out.println("WARNING: endpoint is null, skipping publish!");
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "endpoint publish failed", ex);
        }

        /* ② history + fan-out to every open UI */
        try {
            if (broadcast != null) {
                System.out.println("Broadcasting to: " + n.getReceiverId());
                broadcast.broadcast(n.getReceiverId(), n);
            } else {
                System.out.println("WARNING: broadcast is null, skipping broadcast!");
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "broadcast failed", ex);
        }

        /* ③ last-resort (unit-tests without Spring) */
        if (domainCenter != null && !(n instanceof RichNotification)) {
            try {
                System.out.println("Falling back to domain notification center");
                // Make sure this is the correct method signature your NotificationCenter implementation has
                domainCenter.sendSystemNotification(n.getReceiverId(), n.getMessage());
            } catch (Exception ignore) {
                System.err.println("Error in fallback notification: " + ignore.getMessage());
            }
        } else if (domainCenter == null) {
            System.out.println("WARNING: domainCenter is null, skipping fallback!");
        }
    }
}