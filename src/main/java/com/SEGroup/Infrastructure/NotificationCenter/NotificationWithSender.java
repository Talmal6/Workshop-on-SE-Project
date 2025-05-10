package com.SEGroup.Infrastructure.NotificationCenter;

public class NotificationWithSender extends Notification {
    private final String senderId;

    public NotificationWithSender(String message, String receiverId, String senderId) {
        super(message, receiverId);
        this.senderId = senderId;
    }
    public String getSenderId() { return senderId; }

    @Override public String toString() {
        return "NotificationWithSender{message='" + getMessage() +
               "', receiverId='" + getReceiverId() +
               "', senderId='" + senderId + "'}";
    }
}
