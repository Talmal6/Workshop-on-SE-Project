package com.SEGroup.Infrastructure.Repositories;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.GuestData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryGuestData;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Represents a repository for managing guest users.
 * It allows creating new guests and retrieving their shopping carts.
 */
@Repository
@Profile("memory") // Use this profile for in-memory testing
public class GuestRepository implements IGuestRepository {

    private GuestData   guestData;

    public GuestRepository() {
        this.guestData = new InMemoryGuestData();
    }
    public GuestRepository(GuestData guestData) {
        this.guestData = guestData;
    }
    @Override
    public Guest create() {
        String id = "g-" + UUID.randomUUID();      // <— NOT a JWT!
        Guest g = new Guest(id, Instant.now(), new ShoppingCart(id));
        guestData.saveGuest(g);
        return g;
    }
    @Override
    public ShoppingCart cartOf(String guestId) {
        Guest g = guestData.getGuestById(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        return g.cart();
    }

    @Override
    public void updateCart(String guestId, ShoppingCart cart) {
        Guest g = guestData.getGuestById(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        g.setCart(cart);
        guestData.updateGuest(g);
    }
    @Override
    public void addToCart(String guestId, String storeID, String productID) {
        ShoppingCart cart = cartOf(guestId);
        cart.add(storeID, productID, 1);
        updateCart(guestId, cart);
    }

}
