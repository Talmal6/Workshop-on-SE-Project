package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA implementation of IGuestRepository.
 */
@Repository
@Profile("db")
@Transactional
public class DbGuestRepository implements IGuestRepository {

    private final JpaGuestRepository jpaRepo;

    public DbGuestRepository(JpaGuestRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    /**
     * Create and persist a new Guest with an in-memory ShoppingCart.
     */
    @Override
    public Guest create() {
        String id = "g-" + UUID.randomUUID();
        Guest g = new Guest(id, Instant.now(), new ShoppingCart(id));
        // save only id & issuedAt; cart is @Transient
        return jpaRepo.save(g);
    }

    /**
     * Load the Guest entity, ensure it has a ShoppingCart instance, and return it.
     */
    @Override
    public ShoppingCart cartOf(String guestId) {
        Guest g = jpaRepo.findById(guestId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown guest id: " + guestId));
        // on load, cart() will be null, so wire it up:
        if (g.cart() == null) {
            g.setCart(new ShoppingCart(guestId));
        }
        return g.cart();
    }
}
