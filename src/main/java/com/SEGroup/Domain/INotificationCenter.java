package com.SEGroup.Domain;

import java.util.List;

import javax.naming.AuthenticationException;

import com.SEGroup.Infrastructure.NotificationCenter.Notification;

/**
 * Interface for NotificationCenter.
 * Provides methods for sending user and system notifications.
 */
public interface INotificationCenter {

    /**
     * Sends a notification from a user to another user.
     *
     * @param sessionKey The session key of the sender.
     * @param receiverId The ID of the notification receiver.
     * @param msg        The notification message.
     * @param senderId   The ID of the sender.
     * @throws AuthenticationException if the session key is invalid.
     */
    void sendUserNotification(String sessionKey, String receiverId, String msg, String senderId)
            throws AuthenticationException;

    /**
     * Sends a system notification to a user.
     *
     * @param receiverId The ID of the notification receiver.
     * @param msg        The notification message.
     * @throws AuthenticationException if authentication fails.
     */
    void sendSystemNotification(String receiverId, String msg)
            throws AuthenticationException;

    List<Notification> getUserNotifications(String userEmail);
}