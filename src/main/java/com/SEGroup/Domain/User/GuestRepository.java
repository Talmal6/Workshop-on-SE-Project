package com.SEGroup.Domain.User;

import com.SEGroup.Domain.IGuestRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuestRepository implements IGuestRepository {

    private final Map<String, Guest> guests = new ConcurrentHashMap<>();


    @Override
    public Guest create() {
        String id = "g-" + UUID.randomUUID();      // <— NOT a JWT!
        Guest g = new Guest(id, Instant.now(), new ShoppingCart());
        guests.put(id, g);
        return g;
    }
    @Override
    public ShoppingCart cartOf(String guestId) {
        Guest g = guests.get(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        return g.cart();
    }

}
