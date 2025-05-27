package com.SEGroup.Infrastructure.Repositories.InMemoryRepositories;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Mapper.BasketMapper;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.UserSuspensionDTO;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * UserRepository is responsible for managing user accounts in the system.
 * It provides methods to add, find, delete users, and manage their shopping
 * carts.
 */
@Repository
@Profile("memory")
public class UserRepository implements IUserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    /**
     * Constructor to create a new UserRepository instance.
     */
    public UserRepository() {
        createAdmin();
    }

    private void createAdmin() {
        // Password is: Admin (Its hashed)
        addUser("Admin", "Admin@Admin.Admin", "$2a$10$BJmR2RNH7hTa7DCGDesel.lRX4MGz1bdYiBTM9LGcL2VWH3jcNwoS");
        users.get("Admin@Admin.Admin").addAdminRole();
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

        User u = new User(email, username, passwordHash);
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

        return user.cart().snapShot() // Map<storeId, Basket>
                .entrySet()
                .stream()
                .map(e -> BasketMapper.toDTO(e.getKey(), e.getValue()))
                .toList(); // Java 17+, else collect(Collectors.toList())
    }

    /**
     * Adds a product to the user's shopping cart.
     *
     * @param user      The user to whom the product will be added.
     * @param storeID   The ID of the store.
     * @param productID The ID of the product.
     */
    @Override
    public void addToCart(String email, String storeID, String productID) {
        User user = requireUser(email);
        if (user.isSuspended()) {
            throw new IllegalArgumentException("User is suspended: " + email);
        }
        ShoppingCart cart = user.cart();
        cart.add(storeID, productID, 1);
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
    public void appointOwner(String storeName, String email) {
        User user = requireUser(email);
        if (user.getAllRoles().contains(Role.STORE_OWNER)) {
            throw new IllegalArgumentException("User is already an owner: " + email);
        }
        user.addRole(storeName, Role.STORE_OWNER);

    }

    /**
     * Resigns a user from being the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to resign as owner.
     */
    @Override
    public void resignOwnership(String storeName, String email) {
        removeOwner(storeName, email);
    }

    /**
     * Removes a user from being the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to remove as owner.
     */
    @Override
    public void removeOwner(String storeName, String email) {
        User user = requireUser(email);
        user.removeRole(storeName, Role.STORE_OWNER);
    }

    /**
     * Appoints a user as the manager of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to appoint as manager.
     */
    @Override
    public void appointManager(String storeName, String email) {
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
    private User requireUser(String email) {
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        return u;
    }

    /**
     * Remove a role from a list of users for a specific store.
     *
     * @param emails    The list of email addresses of the users.
     * @param storeName The name of the store.
     */
    @Override
    public void deleteRoles(List<String> emails, String storeName) {
        Objects.requireNonNull(emails, "emails list is null");
        Objects.requireNonNull(storeName, "storeName is null");

        for (String email : emails) {
            User u = users.get(email);
            if (u == null)
                continue;
            u.snapshotRoles().getOrDefault(storeName, EnumSet.noneOf(Role.class))
                    .forEach(role -> u.removeRole(storeName, role));
        }
    }

    /**
     * Clears the shopping cart of a user by their email address.
     *
     * @param email The email address of the user.
     */
    @Override
    public void clearUserCart(String email) {
        User u = requireUser(email); // will throw if not found
        synchronized (u) {
            u.cart().clear();
        }
    }

    /*
     * * Retrieves the username of a user by their email address.
     */
    @Override
    public String getUserName(String email) {
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        return u.getUserName();
    }

    /*
     * set user as admin
     * 
     * @param email The email address of the user to set as admin.
     * 
     * @throws IllegalArgumentException if the user does not exist.
     */

    @Override
    public void setAsAdmin(String AssigneeUsername, String email) {
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (u.isAdmin())
            throw new IllegalArgumentException("User is already an admin: " + email);
        if (!users.get(AssigneeUsername).isAdmin())
            throw new IllegalArgumentException("Assignee is not an admin: " + AssigneeUsername);
        u.addAdminRole();
    }

    @Override
    public void removeAdmin(String assignee, String email) {
        if (email.equals("Admin@Admin.Admin"))
            throw new IllegalArgumentException("Cannot remove the main admin: " + email);
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (!u.isAdmin())
            throw new IllegalArgumentException("User is not an admin: " + email);
        if (!users.get(assignee).isAdmin())
            throw new IllegalArgumentException("Assignee is not an admin: " + assignee);
        u.removeAdminRole();
    }

    @Override
    public boolean userIsAdmin(String username) {
        User u = users.get(username);
        if (u == null)
            return false;
        return u.isAdmin();

    }

    /**
     * Suspends a user by their email address.
     *
     * @param email          The email address of the user to suspend.
     * @param durationInDays The duration of the suspension in days.
     * @param reason         The reason for suspension.
     */
    public void suspendUser(String email, int durationInDays, String reason) {
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (u.isSuspended()) {
            throw new IllegalArgumentException("User is already suspended: " + email);
        }
        Date startTime = new Date(System.currentTimeMillis());
        Date endTime = new Date(System.currentTimeMillis() + (long) durationInDays * 24 * 60 * 60 * 1000);
        UserSuspensionDTO suspension = new UserSuspensionDTO(email, startTime, endTime, reason);
        u.setSuspension(suspension); // Set DTO on User object
    }

    @Override
    public void checkUserSuspension(String email) {
        User u = users.get(email);
        if (u == null) {
            // Or handle as "user not found, so not suspended"
            return;
        }
        UserSuspensionDTO suspension = u.getSuspension();
        if (suspension != null && !suspension.hasPassedSuspension()) {
            throw new IllegalArgumentException("User is suspended: " + email + ". Reason: " + suspension.getReason()
                    + ". Until: " + suspension.getEndTime());
        }
        if (suspension != null && suspension.hasPassedSuspension()) {
            u.setSuspension(null); // Clear suspension if passed
        }
    }

    @Override
    public void unsuspendUser(String email) {
        User u = users.get(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);

        if (!u.isSuspended()) {
            throw new IllegalArgumentException("User is not suspended: " + email);
        }
        u.setSuspension(null); // Clear suspension DTO
    }

    @Override
    public List<UserSuspensionDTO> getAllSuspendedUsers() {
        return users.values().stream()
                .filter(User::isSuspended)
                .map(User::getSuspension) // Correctly map to the UserSuspensionDTO
                .filter(Objects::nonNull) // Ensure DTO is not null after filtering
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllEmails() {
        return users.keySet().stream().toList();
    }

    /**
     * Gets all global roles for a user.
     *
     * @param email The email of the user
     * @return Set of roles the user has
     */
    @Override
    public Set<Role> getGlobalRoles(String email) {
        User u = requireUser(email);
        // union of all role sets (store-specific + system)
        return u.getAllRoles();
    }

    @Override
    public void suspendUser(String username, float suspensionTime, String reason) {
        User u = users.get(username);
        if (u == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        if (u.isSuspended()) {
            throw new IllegalArgumentException("User is already suspended: " + username);
        }
        Date startTime = new Date(System.currentTimeMillis());
        Date endTime = new Date(startTime.getTime() + (long) (suspensionTime * 3_600_000)); // Convert hours to ms
        UserSuspensionDTO suspension = new UserSuspensionDTO(username, startTime, endTime, reason);
        u.setSuspension(suspension);
    }

    @Override
    public void modifyCartQuantity(String email, String productID, String storeName, int quantity) {
        User user = requireUser(email);
        if (user.isSuspended()) {
            throw new IllegalArgumentException("User is suspended: " + email);
        }
        ShoppingCart cart = user.cart();
        if (quantity <= 0) {
            cart.changeQty(storeName, productID, 0); // Remove product
        } else {
            cart.changeQty(storeName, productID, quantity); // Update quantity
        }
    }
}
