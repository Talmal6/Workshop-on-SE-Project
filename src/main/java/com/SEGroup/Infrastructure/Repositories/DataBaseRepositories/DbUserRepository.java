package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.User.Role;
import com.SEGroup.Domain.User.User;
import com.SEGroup.Mapper.BasketMapper;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.UserSuspensionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Profile("db")
@Transactional
public class DbUserRepository implements IUserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Autowired
    public DbUserRepository(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
        bootstrapAdmin();
    }

    private void bootstrapAdmin() {
        String adminEmail = "Admin@Admin.Admin";
        if (!jpaUserRepository.existsByEmail(adminEmail)) {
            User admin = new User(adminEmail, "Admin",
                    "$2a$10$BJmR2RNH7hTa7DCGDesel.lRX4MGz1bdYiBTM9LGcL2VWH3jcNwoS"); // Example hash
            admin.addAdminRole();
            jpaUserRepository.save(admin);
        }
    }

    private User requireUser(String email) {
        User user = jpaUserRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return user;
    }

    public User findByEmail(String email) {
        return requireUser(email);
    }

    @Override
    public void addUser(String username, String email, String passwordHash) {
        if (jpaUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists: " + email);
        }
        User u = new User(email, username, passwordHash);
        jpaUserRepository.save(u);
    }

    @Override
    public void deleteUser(String email) {
        if (!jpaUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        jpaUserRepository.deleteById(email);
    }

    @Override
    public List<BasketDTO> getUserCart(String email) {
        User u = requireUser(email);
        return u.cart().snapShot() // Map<storeId, Basket>
                .entrySet()
                .stream()
                .map(e -> BasketMapper.toDTO(e.getKey(), e.getValue()))
                .toList(); // Java 17+, else collect(Collectors.toList())
    }

    @Override
    public void addToCart(String email, String storeID, String productID) {
        User persistentUser = requireUser(email);
        persistentUser.addToCart(storeID, productID);
        jpaUserRepository.save(persistentUser);
    }

    @Override
    public void checkIfExist(String email) {
        if (!jpaUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User not found: " + email);
        }
    }

    @Override
    public void appointOwner(String storeName, String email) {
        User u = requireUser(email);
        if (u.getAllRoles().contains(Role.STORE_OWNER)) {
            throw new IllegalArgumentException("User is already an owner: " + email);
        }
        u.addRole(storeName, Role.STORE_OWNER);
        jpaUserRepository.save(u);
    }

    @Override
    public void resignOwnership(String storeName, String email) {
        User u = requireUser(email);
        if (!u.getAllRoles().contains(Role.STORE_OWNER)) {
            throw new IllegalArgumentException("User is not an owner: " + email);
        }
        u.removeRole(storeName, Role.STORE_OWNER);
        jpaUserRepository.save(u);
    }

    @Override
    public void removeOwner(String storeName, String email) {
        User u = requireUser(email);
        if (!u.getAllRoles().contains(Role.STORE_OWNER)) {
            throw new IllegalArgumentException("User is not an owner: " + email);
        }
        u.removeRole(storeName, Role.STORE_OWNER);
        jpaUserRepository.save(u);
    }

    @Override
    public void appointManager(String storeName, String email) {
        User u = requireUser(email);
        if (u.getAllRoles().contains(Role.STORE_MANAGER)) {
            throw new IllegalArgumentException("User is already a manager: " + email);
        }
        u.addRole(storeName, Role.STORE_MANAGER);
        jpaUserRepository.save(u);
    }

    @Override
    public void deleteRoles(List<String> emails, String storeName) {
        for (String email : emails) {
            User u = requireUser(email);
            u.snapshotRoles().getOrDefault(storeName, EnumSet.noneOf(Role.class))
                    .forEach(role -> u.removeRole(storeName, role));
            jpaUserRepository.save(u);
        }
    }

    @Override
    public void clearUserCart(String email) {
        User u = requireUser(email);
        u.cart().clear();
        jpaUserRepository.save(u);
    }

    @Override
    public String getUserName(String email) {
        return requireUser(email).getUserName();
    }

    @Override
    public void setAsAdmin(String assignerEmail, String userEmailToMakeAdmin) {
        User assigner = requireUser(assignerEmail);
        if (!assigner.isAdmin()) {
            throw new IllegalArgumentException("Only admins can assign admin roles: " + assignerEmail);
        }
        User userToMakeAdmin = requireUser(userEmailToMakeAdmin);
        if (userToMakeAdmin.isAdmin()) {
            throw new IllegalArgumentException("User is already an admin: " + userEmailToMakeAdmin);
        }
        userToMakeAdmin.addAdminRole();
        jpaUserRepository.save(userToMakeAdmin);
    }

    @Override
    public void removeAdmin(String assignerEmail, String userEmailToRemoveAdmin) {
        User assigner = requireUser(assignerEmail);
        if (!assigner.isAdmin()) {
            throw new IllegalArgumentException("Only admins can remove admin roles: " + assignerEmail);
        }
        User userToRemoveAdmin = requireUser(userEmailToRemoveAdmin);
        if (!userToRemoveAdmin.isAdmin()) {
            throw new IllegalArgumentException("User is not an admin: " + userEmailToRemoveAdmin);
        }
        userToRemoveAdmin.removeAdminRole();
        jpaUserRepository.save(userToRemoveAdmin);
    }

    @Override
    public boolean userIsAdmin(String email) {
        return requireUser(email).isAdmin();
    }

    @Override
    public void suspendUser(String email, float suspensionTimeHours, String reason) {
        User u = requireUser(email);
        Date start = new Date(System.currentTimeMillis());
        Date end = new Date(start.getTime() + (long) (suspensionTimeHours * 3_600_000));
        UserSuspensionDTO suspension = new UserSuspensionDTO(email, start, end, reason);
        u.setSuspension(suspension);
        jpaUserRepository.save(u);
    }

    @Override
    public void checkUserSuspension(String email) {
        User user = requireUser(email);
        if (user.isSuspended()) {
            throw new IllegalArgumentException("User is suspended: " + email);
        }
    }

    @Override
    public void unsuspendUser(String email) {
        User user = requireUser(email);
        if (!user.isSuspended()) {
            throw new IllegalArgumentException("User is not suspended: " + email);
        }
        user.setSuspension(null); // Clear the suspension
        jpaUserRepository.save(user);
    }

    @Override
    public List<UserSuspensionDTO> getAllSuspendedUsers() {
        return jpaUserRepository.findAllSuspendedUsers().stream().map(user -> {
            if (user.isSuspended()) {
                UserSuspensionDTO suspension = user.getSuspension();
                if (suspension != null) {
                    return new UserSuspensionDTO(
                            suspension.getUserEmail(),
                            suspension.getStartTime(),
                            suspension.getEndTime(),
                            suspension.getReason());
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllEmails() {
        return jpaUserRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Role> getGlobalRoles(String email) {
        User user = requireUser(email);
        return user.getAllRoles();
    }

    @Override
    public User findUserByEmail(String email) {
        User user = jpaUserRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return user;
    }

    @Override
    public void modifyCartQuantity(String email, String productID, String storeName, int quantity) {
        User user = requireUser(email);
        if (quantity <= 0) {
            user.removeFromCart(storeName, productID);
        } else {
            user.cart().changeQty(storeName, productID, quantity);
        }
        jpaUserRepository.save(user);
    }

}
