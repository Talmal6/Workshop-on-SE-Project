package com.SEGroup.Domain.User;

import com.SEGroup.Domain.IUserRepository;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository implements IUserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();


    @Override
    public User findUserByEmail(String email) {
        return users.get(email);
    }


    @Override
    public void addUser(String username, String email, String passwordHash) {
        if (users.containsKey(email))
            throw new IllegalArgumentException("User already exists: " + email);

        User u = new User(email, passwordHash);
        users.put(email, u);
    }

    @Override
    public void deleteUser(String email) {
        if (users.remove(email) == null)
            throw new IllegalArgumentException("User not found: " + email);
    }




    @Override
    public void addToCart(User user, int storeID, int productID) {
        String sId = Integer.toString(storeID);
        String pId = Integer.toString(productID);
        ShoppingCart cart = user.cart();
        cart.add(sId, pId, 1);
    }

    @Override
    public void checkIfExist(String email) {
        if (!users.containsKey(email))
            throw new IllegalArgumentException("User not found: " + email);
    }


    @Override
    public void appointOwner(String storeName,String email){
        User user=requireUser(email);
        user.addRole(storeName,Role.STORE_OWNER);

    }
    @Override
    public void resignOwnership(String storeName,String email){
        removeOwner(storeName,email);
    }

    @Override
    public void removeOwner(String storeName, String email){
        User user=requireUser(email);
        user.removeRole(storeName,Role.STORE_OWNER);
    }


    @Override
    public void appointManager(String storeName,String email){
        User u = requireUser(email);
        u.addRole(storeName, Role.STORE_MANAGER);
    }


    private User requireUser(String email){
        User u = users.get(email);
        if(u==null) throw new IllegalArgumentException("User not found: "+email);
        return u;
    }

    @Override
    public void deleteRoles(List<String> emails,String storeName){
        Objects.requireNonNull(emails, "emails list is null");
        Objects.requireNonNull(storeName, "storeName is null");

        for(String email : emails){
            User u = users.get(email);
            if(u == null) continue;
            u.snapshotRoles().getOrDefault(storeName, EnumSet.noneOf(Role.class)).
                    forEach(role->u.removeRole(storeName,role));
        }
    }


}

