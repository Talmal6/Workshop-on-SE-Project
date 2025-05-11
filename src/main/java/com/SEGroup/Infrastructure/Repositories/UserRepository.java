package com.SEGroup.Infrastructure.Repositories;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Mapper.BasketMapper;
import com.SEGroup.DTO.BasketDTO;


import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserRepository is responsible for managing user accounts in the system.
 * It provides methods to add, find, delete users, and manage their shopping carts.
 */
public class UserRepository implements IUserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Set<String> admins = ConcurrentHashMap.newKeySet();
    

    /**
     * Constructor to create a new UserRepository instance.
     */
    public UserRepository() {
        createAdmin();
    }


    private void createAdmin() {
        // create hard coded Admin with all permissions
        User admin = new User("Admin", "Admin", "Admin");
        users.put("Admin", admin);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object representing the user.
     */
    @Override
    public User findUserByEmail(String email) {
        return users.get(email);
    }


    /**
     * Adds a new user to the repository.
     *
     * @param username     The username of the user.
     * @param email        The email address of the user.
     * @param passwordHash The hashed password of the user.
     * @throws IllegalArgumentException if the user already exists.
     */
    @Override
    public void addUser(String username, String email, String passwordHash) {
        if (users.containsKey(email))
            throw new IllegalArgumentException("User already exists: " + email);

        User u = new User(email,username, passwordHash);
        users.put(email, u);
    }

    /**
     * Deletes a user from the repository.
     *
     * @param email The email address of the user to delete.
     * @throws IllegalArgumentException if the user does not exist.
     */
    @Override
    public void deleteUser(String email) {
        if (users.remove(email) == null)
            throw new IllegalArgumentException("User not found: " + email);
    }

    /**
     * Retrieves the shopping cart of a user by their email address.
     *
     * @param email The email address of the user.
     * @return A list of BasketDTO objects representing the user's shopping cart.
     */
    @Override
    public List<BasketDTO> getUserCart(String email) {
        User user = requireUser(email);

        return user.cart().snapShot()            // Map<storeId, Basket>
                .entrySet()
                .stream()
                .map(e -> BasketMapper.toDTO(e.getKey(), e.getValue()))
                .toList();                     // Java 17+, else collect(Collectors.toList())
    }

    /**
     * Adds a product to the user's shopping cart.
     *
     * @param user      The user to whom the product will be added.
     * @param storeID   The ID of the store.
     * @param productID The ID of the product.
     */
    @Override
    public void addToCart(User user, int storeID, int productID) {
        String sId = Integer.toString(storeID);
        String pId = Integer.toString(productID);
        ShoppingCart cart = user.cart();
        cart.add(sId, pId, 1);
    }

    /**
     * Checks if a user exists in the repository.
     *
     * @param email The email address of the user to check.
     * @throws IllegalArgumentException if the user does not exist.
     */
    @Override
    public void checkIfExist(String email) {
        if (!users.containsKey(email))
            throw new IllegalArgumentException("User not found: " + email);
    }


    /**
     * Appoints a user as the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to appoint as owner.
     */
    @Override
    public void appointOwner(String storeName,String email){
        User user=requireUser(email);
        user.addRole(storeName,Role.STORE_OWNER);

    }
    /**
     * Resigns a user from being the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to resign as owner.
     */
    @Override
    public void resignOwnership(String storeName,String email){
        removeOwner(storeName,email);
    }

    /**
     * Removes a user from being the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to remove as owner.
     */
    @Override
    public void removeOwner(String storeName, String email){
        User user=requireUser(email);
        user.removeRole(storeName,Role.STORE_OWNER);
    }


    /**
     * Appoints a user as the manager of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to appoint as manager.
     */
    @Override
    public void appointManager(String storeName,String email){
        User u = requireUser(email);
        u.addRole(storeName, Role.STORE_MANAGER);
    }


    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object representing the user.
     * @throws IllegalArgumentException if the user does not exist.
     */
    private User requireUser(String email){
        User u = users.get(email);
        if(u==null) throw new IllegalArgumentException("User not found: "+email);
        return u;
    }

    /**
     * Remove a role from a list of users for a specific store.
     *
     * @param emails   The list of email addresses of the users.
     * @param storeName The name of the store.
     */
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

    /**
     * Clears the shopping cart of a user by their email address.
     *
     * @param email The email address of the user.
     */
    @Override
    public void clearUserCart(String email) {
        User u = requireUser(email);        // will throw if not found
        synchronized (u) {
            u.cart().clear();
        }
    }


    /*
     *  * Retrieves the username of a user by their email address.
     */
    @Override
    public String getUserName(String email) {
        User u = users.get(email);
        if (u == null) throw new IllegalArgumentException("User not found: " + email);
        return u.getUserName();
    }

    /* set user as admin
     * @param email The email address of the user to set as admin.
     * @throws IllegalArgumentException if the user does not exist.
     */

    @Override
    public void setAsAdmin(String assigneeEmail, String email) {
        User u = users.get(email);
        if (u == null) throw new IllegalArgumentException("User not found: " + email);
        if (admins.contains(email)) throw new IllegalArgumentException("User is already an admin: " + email);
        if (!admins.contains(assigneeEmail)) throw new IllegalArgumentException("Assignee is not an admin: " + assigneeEmail);
        admins.add(email);
    }




    @Override
    public void checkIfUserIsAdmin(String email) {
        User u = users.get(email);
        if (u == null) throw new IllegalArgumentException("User not found: " + email);
        if (!admins.contains(email)) throw new IllegalArgumentException("User is not an admin: " + email);
    }




    

}

