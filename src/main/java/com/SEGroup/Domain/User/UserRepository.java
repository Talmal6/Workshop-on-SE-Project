package com.SEGroup.Domain.User;

import com.SEGroup.Domain.IUserRepository;

import java.util.Map;
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
    public void addNewGuest(String tempName) {
        // password = "__guest__", role = GUEST
        User g = new User(tempName, "__guest__");
        g.addRole(Role.GUEST);
        users.put(tempName, g);
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


}

