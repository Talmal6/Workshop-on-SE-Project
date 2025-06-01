package com.SEGroup.Infrastructure.Repositories;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.Store.ManagerPermission;
import com.SEGroup.Domain.User.Address;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryUserData;
import com.SEGroup.Infrastructure.Repositories.RepositoryData.UserData;
import com.SEGroup.Mapper.BasketMapper;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.UserSuspensionDTO;
import com.SEGroup.DTO.AddressDTO;
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
@Profile({ "prod", "db" })
public class UserRepository implements IUserRepository {
    private final UserData userData;

    public UserRepository() {

        this.userData = new InMemoryUserData();
        createAdmin(); // Create the admin user if it doesn't exist
    }

    /**
     * Constructor to create a new UserRepository instance.
     */
    public UserRepository(UserData userData) {
        this.userData = userData;
        createAdmin();
    }

    private void createAdmin() {
        User admin = new User("Admin@Admin.Admin", "Admin",
                "$2a$10$BJmR2RNH7hTa7DCGDesel.lRX4MGz1bdYiBTM9LGcL2VWH3jcNwoS");
        if (!userData.userExistsByEmail(admin.getEmail())) {
            admin.addAdminRole();
            userData.saveUser(admin);
        }
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object representing the user.
     */
    @Override
    public User findUserByEmail(String email) {
        return userData.getUserByEmail(email);
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
        if (userData.userExistsByEmail(email))
            throw new IllegalArgumentException("User already exists: " + email);
        if (userData.userExistsByName(username))
            throw new IllegalArgumentException("Username already exists: " + username);
        User u = new User(email, username, passwordHash);
        u.setAddress(new Address("","","",""));
        userData.saveUser(u);
    }
    @Override
    public void addUserWithaddress(String username, String email, String passwordHash,AddressDTO addressDTO) {
        if (userData.userExistsByEmail(email))
            throw new IllegalArgumentException("User already exists: " + email);
        if (userData.userExistsByName(username))
            throw new IllegalArgumentException("Username already exists: " + username);
        User u = new User(email, username, passwordHash);
        userData.saveUser(u);
        setAddress(email,addressDTO);
    }

    /**
     * Deletes a user from the repository.
     *
     * @param email The email address of the user to delete.
     * @throws IllegalArgumentException if the user does not exist.
     */
    @Override
    public void deleteUser(String email) {
        if (userData.userExistsByEmail(email))
            userData.deleteUser(email);
        else
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
        userData.updateUser(user); // Save the updated user with the modified cart
    }

    /**
     * Checks if a user exists in the repository.
     *
     * @param email The email address of the user to check.
     * @throws IllegalArgumentException if the user does not exist.
     */
    @Override
    public void checkIfExist(String email) {
        if (!userData.userExistsByEmail(email))
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
        userData.updateUser(user); // Save the updated user with the new owner role

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
        User user = requireUser(email);
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
        userData.updateUser(user); // Save the updated user with the removed owner role
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
        userData.updateUser(u); // Save the updated user with the new manager role
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to retrieve.
     * @return The User object representing the user.
     * @throws IllegalArgumentException if the user does not exist.
     */
    private User requireUser(String email) {
        User u = userData.getUserByEmail(email);
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
            List<BasketDTO> users;
            User u = userData.getUserByEmail(email);
            if (u == null)
                continue;
            u.snapshotRoles().getOrDefault(storeName, EnumSet.noneOf(Role.class))
                    .forEach(role -> u.removeRole(storeName, role));
            userData.updateUser(u); // Save the updated user with the removed roles
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
            userData.updateUser(u); // Save the updated user with the cleared cart
        }
    }

    /*
     * * Retrieves the username of a user by their email address.
     */
    @Override
    public String getUserName(String email) {
        User u = requireUser(email); // will throw if not found
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
        User u = userData.getUserByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (u.isAdmin())
            throw new IllegalArgumentException("User is already an admin: " + email);
        if (!userData.getUserByEmail(AssigneeUsername).isAdmin())
            throw new IllegalArgumentException("Assignee is not an admin: " + AssigneeUsername);
        u.addAdminRole();
        userData.updateUser(u); // Save the updated user with the new admin role
    }

    @Override
    public void removeAdmin(String assignee, String email) {
        if (email.equals("Admin@Admin.Admin"))
            throw new IllegalArgumentException("Cannot remove the main admin: " + email);
        User u = userData.getUserByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (!u.isAdmin())
            throw new IllegalArgumentException("User is not an admin: " + email);
        if (!userData.getUserByEmail(assignee).isAdmin())
            throw new IllegalArgumentException("Assignee is not an admin: " + assignee);
        u.removeAdminRole();
        userData.updateUser(u); // Save the updated user with the removed admin role
    }

    @Override
    public boolean userIsAdmin(String username) {
        User u = userData.getUserByEmail(username);
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
        User u = userData.getUserByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);
        if (u.isSuspended()) {
            throw new IllegalArgumentException("User is already suspended: " + email);
        }
        Date startTime = new Date(System.currentTimeMillis());
        Date endTime = new Date(System.currentTimeMillis() + (long) durationInDays * 24 * 60 * 60 * 1000);
        UserSuspensionDTO suspension = new UserSuspensionDTO(email, startTime, endTime, reason);
        u.setSuspension(suspension); // Set DTO on User object
        userData.updateUser(u); // Save the updated user with the suspension
    }

    @Override
    public void checkUserSuspension(String email) {
        User u = userData.getUserByEmail(email);
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
        userData.updateUser(u); // Save the updated user
    }

    @Override
    public void unsuspendUser(String email) {
        User u = userData.getUserByEmail(email);
        if (u == null)
            throw new IllegalArgumentException("User not found: " + email);

        if (!u.isSuspended()) {
            throw new IllegalArgumentException("User is not suspended: " + email);
        }
        u.setSuspension(null); // Clear suspension DTO
        userData.updateUser(u); // Save the updated user
    }

    @Override
    public List<UserSuspensionDTO> getAllSuspendedUsers() {
        return userData.getAllUsers().stream()
                .filter(User::isSuspended)
                .map(User::getSuspension) // Correctly map to the UserSuspensionDTO
                .filter(Objects::nonNull) // Ensure DTO is not null after filtering
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllEmails() {
        return userData.getAllUsers().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
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
        User u = userData.getUserByEmail(username);
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
        userData.updateUser(u); // Save the updated user with the suspension
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
        userData.updateUser(user); // Save the updated user with the modified cart
    }

    @Override
    public AddressDTO getAddress(String email) {
        User user = userData.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        Address address = user.getAddress();
        if (address == null) {
            throw new IllegalArgumentException("Address not set for user: " + email);
        }
        return new AddressDTO(address);
    }

    @Override
    public void setAddress(String email, AddressDTO address) {
        User user = userData.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        user.setAddress(new Address(address));
        userData.updateUser(user);
    }

    @Override
    public void setUserName(String email, String newUsername) {
        User user = userData.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        if (userData.userExistsByName(newUsername)) {
            throw new IllegalArgumentException("Username already exists: " + newUsername);
        }
        user.setUserName(newUsername);
        userData.updateUser(user);
    }

}
