package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.SEGroup.Domain.User.User;

public class InMemoryUserData implements UserData {
    // This class can be used to hold in-memory data for testing or temporary storage.
    // It can include methods to initialize, clear, or manipulate the in-memory data as needed.
    
    // Example fields for in-memory data
    private final Map<String, User> InMemoryData = new ConcurrentHashMap<>();

    @Override
    public User getUserById(String userId) {
        // Retrieve user by ID from the in-memory store
        return InMemoryData.values().stream()
                .filter(user -> user.getUserName().equals(userId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        // Retrieve user by email from the in-memory store
        return InMemoryData.get(email);
    }

    @Override
    public void saveUser(User user) {
        InMemoryData.put(user.getEmail(), user);
    }

    @Override
    public void updateUser(User user) {
        // Update user in the in-memory store
        if (InMemoryData.containsKey(user.getEmail())) {
            InMemoryData.put(user.getEmail(), user);
        } else {
            throw new RuntimeException("User not found for update");
        }
    }

    @Override
    public void deleteUser(String userId) {
        InMemoryData.remove(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(InMemoryData.values());
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return InMemoryData.values().stream()
                .filter(user -> user.getAllRoles().contains(role))
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String userId) {
        return InMemoryData.containsKey(userId);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return InMemoryData.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

}