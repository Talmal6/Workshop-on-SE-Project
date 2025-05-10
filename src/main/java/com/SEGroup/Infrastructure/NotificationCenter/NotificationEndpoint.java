package com.SEGroup.Infrastructure.NotificationCenter;
          
import jakarta.annotation.security.PermitAll;
import org.springframework.stereotype.Service;

import com.vaadin.hilla.EndpointExposed;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@EndpointExposed
@PermitAll           // demo only – secure in production
@Service
public class NotificationEndpoint {

    private final Sinks.Many<Notification> sink =
            Sinks.many().multicast().onBackpressureBuffer();

    /** Browser calls this; only items for that user flow down the socket. */
    public Flux<Notification> subscribe(String userId) {
        return sink.asFlux().filter(n -> n.getReceiverId().equals(userId));
    }

    /** Called by NotificationCenter to fan‑out a single message. */
    public void publish(Notification n) {
        sink.tryEmitNext(n);
    }
}
