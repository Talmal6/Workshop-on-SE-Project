package com.SEGroup.Infrastructure.Repositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.SEGroup.Domain.IGuestRepository;
import com.SEGroup.Domain.User.Guest;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.GuestData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryGuestData;
import com.SEGroup.Mapper.BasketMapper;
import com.SEGroup.DTO.BasketDTO;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Represents a repository for managing guest users.
 * It allows creating new guests and retrieving their shopping carts.
 */
@Repository
@Profile({"db","prod"}) // Use this profile for in-memory testing
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
    public List<BasketDTO> cartOf(String guestId) {
        Guest g = guestData.getGuestById(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        return parseCart(g.cart(), guestId);
    }

    @Override
    public ShoppingCart getShoppingCart(String guestId) {
        Guest g = guestData.getGuestById(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        return g.cart();
    }

    @Override
    public void modifyCartQuantity(String guestId, String productID, String storeName, int quantity) {
        Guest g = guestData.getGuestById(guestId);
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        ShoppingCart cartOfGuest = g.cart();
        if (cartOfGuest == null) throw new IllegalArgumentException("Guest cart not found");
        if (quantity <= 0) {
            cartOfGuest.changeQty(storeName, productID, 0); // Remove product
        } else {
            cartOfGuest.changeQty(storeName, productID, quantity); // Update quantity
        }
        
        guestData.updateGuest(g);
    }
    @Override
    public void addToCart(String guestId, String storeID, String productID) {
        Guest g = guestData.getGuestById(guestId);
        if (g == null) throw new IllegalArgumentException("Unknown guest id");
        ShoppingCart cart = g.cart();
        cart.add(storeID, productID, 1);
        guestData.updateGuest(g);
    }

    private List<BasketDTO> parseCart(ShoppingCart cart, String guestId) {
        Guest guest = guestData.getGuestById(guestId);
        if (guest == null) throw new IllegalArgumentException("Unknown guest id");
        return guest.cart().snapShot() // Map<storeId, Basket>
                .entrySet()
                .stream()
                .map(e -> BasketMapper.toDTO(e.getKey(), e.getValue()))
                .toList(); // Java 17+, else collect(Collectors.toList())

    }
}
