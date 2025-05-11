package com.SEGroup.Infrastructure.NotificationCenter;

import java.util.concurrent.atomic.AtomicInteger;

public class Notification {
    private String message;
    private String receiverId;
    private AtomicInteger attempts = new AtomicInteger(0);
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

    

    public int getAttempts() {
        return attempts.get();
    }

    public void incrementAttempts() {
        attempts.incrementAndGet();
    }

    public void setAttempts(int attempts) {
        this.attempts.set(attempts);
    }
}
