package com.SEGroup.Infrastructure.NotificationCenter;

import com.SEGroup.Domain.IAuthenticationService;
import org.springframework.stereotype.Component;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import javax.naming.AuthenticationException;
import java.util.*;
import java.util.concurrent.*;


@Component("infrastructureNotificationCenter")
public class NotificationCenter implements Runnable {

    private final List<Notification> queue = new ArrayList<>();
    private final Object lock = new Object();
    private final ExecutorService pool = Executors.newFixedThreadPool(8);
    private final IAuthenticationService auth;
    private final NotificationEndpoint endpoint;      // Hilla endpoint

    public NotificationCenter(IAuthenticationService auth,
                              NotificationEndpoint endpoint) {
        this.auth = auth;
        this.endpoint = endpoint;
        Thread t = new Thread(this, "notification-dispatcher");
        t.setDaemon(true);
        t.start();
    }

    /* ---------------------- public API ---------------------- */

    public void sendUserNotification(String sessionKey, String receiverId,
                                     String msg, String senderId) throws AuthenticationException {
        auth.checkSessionKey(sessionKey);
        enqueue(new NotificationWithSender(msg, receiverId, senderId));
    }

    public void sendSystemNotification(String sessionKey, String receiverId,
                                       String msg) throws AuthenticationException {
        auth.checkSessionKey(sessionKey);
        enqueue(new Notification(msg, receiverId));
    }

    /* ---------------------- internal ------------------------ */

    private void enqueue(Notification n) {
        synchronized (lock) {
            queue.add(n);
        }
        lock.notifyAll();
    }

    @Override
    public void run() {
        while (true) {
            List<Notification> batch;
            synchronized (lock) {
                while (queue.isEmpty()) {            // wait for work
                    try { lock.wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                batch = new ArrayList<>(queue);      // drain queue
                queue.clear();
            }

            // process asynchronously
            for (Notification n : batch) {
                pool.execute(() -> endpoint.publish(n));
            }
        }
    }
}
