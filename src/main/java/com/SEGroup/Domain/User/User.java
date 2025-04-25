package com.SEGroup.Domain.User;

import com.SEGroup.Domain.Store.Store;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {

    private final String email;
    private       String passwordHash;
    private final ConcurrentMap<String, EnumSet<Role>> storeRoles = new ConcurrentHashMap<>();

    private volatile ShoppingCart cart;
    private final List<String> purchaseHistory = new LinkedList<>();
    public User(String email, String passwordHash) {
        this.email        = email;
        this.passwordHash = passwordHash;

    }
    public boolean matchesPassword(String raw,
                                   java.util.function.BiPredicate<String,String> matcher) {
        return matcher.test(raw, passwordHash);
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
    }

    public void   addRole(String store, Role r){
        storeRoles.computeIfAbsent(store, k -> EnumSet.noneOf(Role.class)).add(r);
    }


    public void   removeRole(String store, Role r){
        EnumSet<Role> set = storeRoles.get(store);
        if(set!=null){
            set.remove(r);
            if(set.isEmpty()) storeRoles.remove(store);
        }
    }

    public boolean hasRole(String store, Role r){
        return storeRoles.getOrDefault(store, EnumSet.noneOf(Role.class)).contains(r);
    }

    public Map<String, EnumSet<Role>> snapshotRoles(){
        return Collections.unmodifiableMap(storeRoles);
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
    public List<String>    getHistory()   { return List.copyOf(purchaseHistory); }


}