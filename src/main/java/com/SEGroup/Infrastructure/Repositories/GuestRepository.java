package com.SEGroup.Infrastructure.Repositories;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;
import org.springframework.stereotype.Repository;

/**
 * Represents a repository for managing guest users.
 * It allows creating new guests and retrieving their shopping carts.
 */

@Repository
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
