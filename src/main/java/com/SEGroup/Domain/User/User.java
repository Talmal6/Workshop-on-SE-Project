package com.SEGroup.Domain.User;

import com.SEGroup.Domain.Store.Store;

import jakarta.persistence.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a user in the system, including their email, password hash, roles in different stores,
 * shopping cart, and purchase history.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "email", unique = true, nullable = false)
    private final String email;

    @Column(name = "password_hash", nullable = false)
    private       String passwordHash;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "store_name")
    @CollectionTable(name = "user_store_roles", joinColumns = @JoinColumn(name = "user_email"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)

    private final ConcurrentMap<String, EnumSet<Role>> storeRoles = new ConcurrentHashMap<>();

    @Column(name = "username", nullable = false)
    private final String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_admin", nullable = false)
    private Role isAdmin;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private ShoppingCart cart;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "purchase_history", joinColumns = @JoinColumn(name = "user_email"))
    @Column(name = "transaction_id")
    private final List<String> purchaseHistory = new LinkedList<>();

    /**
     * Constructor to create a new User instance.
     *
     * @param email        The email of the user.
     * @param passwordHash The hashed password of the user.
     * @param username     The username of the user.
     */
    public User(String email,String username ,String passwordHash) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.username     = username;


    }

    /**
     * Checks if the provided password matches the stored password hash.
     *
     * @param raw     The raw password to check.
     * @param matcher The function to use for matching.
     * @return true if the password matches, false otherwise.
     */
    public boolean matchesPassword(String raw,
                                   java.util.function.BiPredicate<String,String> matcher) {
        return matcher.test(raw, passwordHash);
    }

    /**
     * Chenges the password hash of the user.
     *
     * @param newHash The new password hash to set.
     */
    public void changePassword(String newHash) {
        this.passwordHash = newHash;
    }

    /**
     * Adds a role to the user for a specific store.
     *
     * @param store The store ID.
     * @param r     The role to add.
     */
    public void   addRole(String store, Role r){
        storeRoles.computeIfAbsent(store, k -> EnumSet.noneOf(Role.class)).add(r);
    }


    /**
     * Removes a role from the user for a specific store.
     *
     * @param store The store ID.
     * @param r     The role to remove.
     */
    public void   removeRole(String store, Role r){
        EnumSet<Role> set = storeRoles.get(store);
        if(set!=null){
            set.remove(r);
            if(set.isEmpty()) storeRoles.remove(store);
        }
    }

    /**
     * Checks if the user has a specific role for a given store.
     *
     * @param store The store ID.
     * @param r     The role to check.
     * @return true if the user has the role, false otherwise.
     */
    public boolean hasRole(String store, Role r){
        return storeRoles.getOrDefault(store, EnumSet.noneOf(Role.class)).contains(r);
    }

    /**
     * Retrieves the current roles of the user for a specific store.
     *
     * @return An unmodifiable set of roles for the store.
     */
    public Map<String, EnumSet<Role>> snapshotRoles(){
        return Collections.unmodifiableMap(storeRoles);
    }

    /**
     * Retrieves the shopping cart of the user.
     *
     * @return The shopping cart instance.
     */
    public ShoppingCart cart() {
        if (cart == null) {
            synchronized (this) {
                if (cart == null) {
                    cart = new ShoppingCart(this.username); // Ensure ShoppingCart constructor is accessible
                }
            }
        }
        return cart;
    }

    /**
     * Adds a product to the user's shopping cart for a specific store.
     *
     * @param storeId   The ID of the store.
     * @param productId The ID of the product.
     */
    public void addToCart(String storeId, String productId) {
        ShoppingCart currentCart = cart();
        if (currentCart != null) {
            currentCart.add(storeId, productId, 1);
        } else {
            throw new IllegalStateException("ShoppingCart is not initialized.");
        }
    }
    public Set<Role> getAllRoles() {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        for (EnumSet<Role> roleSet : storeRoles.values()) {
            roles.addAll(roleSet);
        }
        return roles;
    }
    /**
     * Removes a product from the user's shopping cart for a specific store.
     *
     * @param storeId   The ID of the store.
     * @param productId The ID of the product.
     */
    public void removeFromCart(String storeId,String productId){ cart().changeQty(storeId, productId, 0); }
    public void addPurchase(String txId) { purchaseHistory.add(txId); }

    public String getPassword() { return passwordHash; }
    public String          getEmail()     { return email; }
    public List<String>    getHistory()   { return List.copyOf(purchaseHistory); }
    public String          getUserName()  { return username; }
    public void addAdminRole() {
        isAdmin = Role.ADMIN;
    }

    public void removeAdminRole() {
        isAdmin = null;
    }

    public boolean isAdmin() {
        return isAdmin == Role.ADMIN;
    }

}