package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.UserSuspensionDTO;
import com.SEGroup.Mapper.BasketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DbUserRepository is responsible for managing user accounts in the system
 * via a database. It provides methods to add, find, delete users,
 * manage their shopping carts, roles, admin status, and suspensions.
 */
@Repository
@Profile("db")
public class DbUserRepository implements IUserRepository {

    private final JpaUserRepository jpaRepo;
    private final Map<String, UserSuspensionDTO> suspendedUsers = new ConcurrentHashMap<>();

    @Autowired
    public DbUserRepository(JpaUserRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
        bootstrapAdmin();
    }

    private void bootstrapAdmin() {
        String adminEmail = "Admin@Admin.Admin";
        if (!jpaRepo.existsByEmail(adminEmail)) {
            // Password is: Admin (hashed)
            User admin = new User(adminEmail, "Admin",
                    "$2a$10$BJmR2RNH7hTa7DCGDesel.lRX4MGz1bdYiBTM9LGcL2VWH3jcNwoS");
            admin.addAdminRole();
            jpaRepo.save(admin);
        }
    }

    private User requireUser(String email) {
        User u = jpaRepo.findByEmail(email);
        if (u == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return u;
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object, or null if not found.
     */
    @Override
    public User findUserByEmail(String email) {
        return jpaRepo.findByEmail(email);
    }

    /**
     * Adds a new user to the repository.
     *
     * @param username     The username of the user.
     * @param email        The email address of the user.
     * @param passwordHash The hashed password of the user.
     */
    @Override
    public void addUser(String username, String email, String passwordHash) {
        if (jpaRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists: " + email);
        }
        User u = new User(email, username, passwordHash);
        jpaRepo.save(u);
    }

    /**
     * Deletes a user from the repository.
     *
     * @param email The email address of the user to delete.
     */
    @Override
    public void deleteUser(String email) {
        if (!jpaRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        jpaRepo.deleteById(email);
    }

    /**
     * Retrieves the shopping cart of a user by their email address.
     *
     * @param email The email address of the user.
     * @return A list of BasketDTO objects representing the user's shopping cart.
     */
    @Override
    public List<BasketDTO> getUserCart(String email) {
        User u = requireUser(email);
        return u.cart().snapShot().entrySet().stream()
                .map(e -> BasketMapper.toDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
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
        user.cart().add(sId, pId, 1);
        jpaRepo.save(user);
    }

    /**
     * Checks if a user exists in the repository.
     *
     * @param email The email address of the user to check.
     */
    @Override
    public void checkIfExist(String email) {
        requireUser(email);
    }

    /**
     * Appoints a user as the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to appoint as owner.
     */
    @Override
    public void appointOwner(String storeName, String email) {
        User u = requireUser(email);
        u.addRole(storeName, Role.STORE_OWNER);
        jpaRepo.save(u);
    }

    /**
     * Removes a user from being the owner of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to remove as owner.
     */
    @Override
    public void removeOwner(String storeName, String email) {
        User u = requireUser(email);
        u.removeRole(storeName, Role.STORE_OWNER);
        jpaRepo.save(u);
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
     * Appoints a user as the manager of a store.
     *
     * @param storeName The name of the store.
     * @param email     The email address of the user to appoint as manager.
     */
    @Override
    public void appointManager(String storeName, String email) {
        User u = requireUser(email);
        u.addRole(storeName, Role.STORE_MANAGER);
        jpaRepo.save(u);
    }

    /**
     * Removes all roles for a list of users in a specific store.
     *
     * @param emails    The list of email addresses of the users.
     * @param storeName The name of the store.
     */
    @Override
    public void deleteRoles(List<String> emails, String storeName) {
        Objects.requireNonNull(emails, "emails list is null");
        Objects.requireNonNull(storeName, "storeName is null");
        for (String em : emails) {
            User u = requireUser(em);
            u.snapshotRoles()
             .getOrDefault(storeName, EnumSet.noneOf(Role.class))
             .forEach(r -> u.removeRole(storeName, r));
            jpaRepo.save(u);
        }
    }

    /**
     * Clears the shopping cart of a user by their email address.
     *
     * @param email The email address of the user.
     */
    @Override
    public void clearUserCart(String email) {
        User u = requireUser(email);
        synchronized (u) {
            u.cart().clear();
        }
        jpaRepo.save(u);
    }

    /**
     * Retrieves the username of a user by their email address.
     *
     * @param email The email address of the user.
     * @return The username.
     */
    @Override
    public String getUserName(String email) {
        return requireUser(email).getUserName();
    }

    /**
     * Sets a user as an administrator.
     *
     * @param assignee The email of the administrator performing the action.
     * @param email    The email of the user to be made admin.
     */
    @Override
    public void setAsAdmin(String assignee, String email) {
        User actor = requireUser(assignee);
        if (!actor.getAllRoles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("Assignee is not an admin: " + assignee);
        }
        User u = requireUser(email);
        u.addAdminRole();
        jpaRepo.save(u);
    }

    /**
     * Removes administrator role from a user.
     *
     * @param assignee The email of the administrator performing the action.
     * @param email    The email of the user whose admin role is removed.
     */
    @Override
    public void removeAdmin(String assignee, String email) {
        User actor = requireUser(assignee);
        if (!actor.getAllRoles().contains(Role.ADMIN)) {
            throw new IllegalArgumentException("Assignee is not an admin: " + assignee);
        }
        if (email.equals("Admin@Admin.Admin")) {
            throw new IllegalArgumentException("Cannot remove main admin");
        }
        User u = requireUser(email);
    
        jpaRepo.save(u);
    }

    /**
     * Checks if a user is an administrator.
     *
     * @param email The email of the user.
     * @return True if the user has admin role.
     */
    @Override
    public boolean userIsAdmin(String email) {
        return requireUser(email).getAllRoles().contains(Role.ADMIN);
    }

    /**
     * Suspends a user by their email address.
     *
     * @param email          The email address of the user.
     * @param suspensionTime Suspension duration in hours.
     * @param reason         The reason for suspension.
     */
    @Override
    public void suspendUser(String email, float suspensionTime, String reason) {
        requireUser(email);
        Date start = new Date(System.currentTimeMillis());
        Date end = new Date(start.getTime() + (long)(suspensionTime * 3_600_000));
        suspendedUsers.put(email, new UserSuspensionDTO(email, start, end, reason));
    }

    /**
     * Checks if a user is currently suspended.
     *
     * @param email The email of the user.
     */
    @Override
    public void checkUserSuspension(String email) {
        UserSuspensionDTO dto = suspendedUsers.get(email);
        if (dto != null && !dto.hasPassedSuspension()) {
            throw new IllegalArgumentException("User is suspended: " + email);
        }
        if (dto != null && dto.hasPassedSuspension()) {
            suspendedUsers.remove(email);
        }
    }

    /**
     * Unsuspends a previously suspended user.
     *
     * @param email The email of the user.
     */
    @Override
    public void unsuspendUser(String email) {
        if (suspendedUsers.remove(email) == null) {
            throw new IllegalArgumentException("User is not suspended: " + email);
        }
    }

    /**
     * Retrieves all suspended users.
     *
     * @return List of UserSuspensionDTO.
     */
    @Override
    public List<UserSuspensionDTO> getAllSuspendedUsers() {
        return new ArrayList<>(suspendedUsers.values());
    }

    /**
     * Retrieves all user emails.
     *
     * @return List of all registered emails.
     */
    @Override
    public List<String> getAllEmails() {
        return jpaRepo.findAll().stream()
                      .map(User::getEmail)
                      .collect(Collectors.toList());
    }

    /**
     * Gets all global roles for a user.
     *
     * @param email The email of the user.
     * @return Set of roles the user has.
     */
    @Override
    public Set<Role> getGlobalRoles(String email) {
        return requireUser(email).getAllRoles();
    }
}
