package com.SEGroup.Infrastructure.NotificationCenter;

import org.springframework.stereotype.Component;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Domain.INotificationCenter;

import javax.naming.AuthenticationException;
import java.util.*;
import java.util.concurrent.*;

/** Central dispatcher for user & system notifications. */
@Component
public class NotificationCenter implements Runnable, INotificationCenter {

    /* ------------ configuration ------------ */



    private final Queue<Notification> queue      = new ArrayDeque<>();
    private final Object               lock       = new Object();
    private final ExecutorService      pool       = Executors.newFixedThreadPool(8);
    private static final int           MAX_QUEUE   = 1000; // optional
    private static final int           MAX_RETRIES = 2;   // optional  
    private final IAuthenticationService auth;
    private final NotificationEndpoint   endpoint;

    /** Successful deliveries grouped by receiver-id. */
    private final ConcurrentMap<String, List<Notification>> userNotificationHistory =
            new ConcurrentHashMap<>();

    public NotificationCenter(IAuthenticationService auth) {
        this.auth      = auth;
        this.endpoint  = new NotificationEndpoint();

        Thread t = new Thread(this, "notification-dispatcher");
        t.setDaemon(true);
        t.start();
    }

    /* ------------ public API ------------ */

    public void sendUserNotification(String sessionKey, String receiverId,
                                     String msg, String senderId)
            throws AuthenticationException {

        auth.checkSessionKey(sessionKey);
        enqueue(new NotificationWithSender(msg, receiverId, senderId));
    }

    public void sendSystemNotification( String receiverId,
                                       String msg)
            throws AuthenticationException {

        enqueue(new Notification(msg, receiverId));
    }

    /* ------------ core loop ------------ */

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Notification> batch;
            synchronized (lock) {
                while (queue.isEmpty()) {
                    try { lock.wait(); }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                batch = new ArrayList<>(queue);
                queue.clear();
            }

            // send every notification asynchronously
            for (Notification n : batch) {
                pool.execute(() -> processNotification(n));
            }
        }
    }

    /* ------------ helpers ------------ */

    private void enqueue(Notification n) {
        synchronized (lock) {
            queue.add(n);
            lock.notifyAll();
        }
    }

    private void processNotification(Notification n) {
        try {
            try {
                endpoint.publish(n);   // ← assume publish() performs the operation
                addToHistory(n);
            } catch (Exception ex) {
                retry(n);
            }
        } catch (Exception ex) {               
            retry(n);
        }
    }

    private void addToHistory(Notification n) {
        userNotificationHistory
            .computeIfAbsent(n.getReceiverId(), k -> Collections.synchronizedList(new ArrayList<>()))
            .add(n);
    }

    private void retry(Notification n) {
        if ( n.getAttempts() < MAX_RETRIES) {
            n.incrementAttempts();
            enqueue(n);
        } else {
            // אפשר לרשום ללוג / מטריקה ולהתעלם
        }
    }

    private void checkUserUnsendedNotifications(String userId){
        for (Notification n : queue){
            if (n.getReceiverId().equals(userId)){
                n.setAttempts(0);
            }
        }
    }

    // NotificationCenter.java (for testing only!)
    public List<Notification> getUserNotifications(String userEmail) {
        return userNotificationHistory.getOrDefault(userEmail, Collections.emptyList());
    }

}
