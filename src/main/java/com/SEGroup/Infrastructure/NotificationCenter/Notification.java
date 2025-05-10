package com.SEGroup.Infrastructure.NotificationCenter;

public class Notification {
    private String message;
    private String receiverId;

    public Notification(String message, String receiverId) {   // ← fixed param name
        this.message  = message;
        this.receiverId = receiverId;
    }

    public String getMessage()        { return message; }
    public void   setMessage(String m){ this.message = m; }

    public String getReceiverId()         { return receiverId; }
    public void   setReceiverId(String r) { this.receiverId = r; }

    @Override public String toString() {
        return "Notification{message='" + message + "', receiverId='" + receiverId + "'}";
    }
}
