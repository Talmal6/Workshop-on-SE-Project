package com.SEGroup.Domain.User;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    private final String email;
    private       String passwordHash;
    private final List<Role> roles = new CopyOnWriteArrayList<>();
    private volatile ShoppingCart cart;
    private final List<String> purchaseHistory = new LinkedList<>();
    public User(String email, String passwordHash) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.roles.add(Role.SUBSCRIBER);
    }
    public boolean matchesPassword(String raw,
                                   java.util.function.BiPredicate<String,String> matcher) {
        return matcher.test(raw, passwordHash);
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
    }

    public void addRole(Role r)    {
        if (!roles.contains(r)) roles.add(r);
    }
    public void removeRole(Role r) {
        roles.remove(r);
    }

    public ShoppingCart cart() {
        if (cart == null) synchronized (this) {
            if (cart == null) cart = new ShoppingCart();
        }
        return cart;
    }

    public void addToCart(String storeId,String productId){ cart.add(storeId,productId,1); }
    public void removeFromCart(String storeId,String productId){ cart.changeQty(storeId,productId,0); }
    public void addPurchase(String txId) { purchaseHistory.add(txId); }
    public String getPassword() { return passwordHash; }
    public String          getEmail()     { return email; }
    public List<Role>      getRoles()     { return List.copyOf(roles); }
    public List<String>    getHistory()   { return List.copyOf(purchaseHistory); }


}