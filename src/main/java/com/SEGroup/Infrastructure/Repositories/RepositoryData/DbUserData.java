package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.User.User;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaUserRepository;

public class DbUserData implements UserData {
    
    JpaUserRepository jpaUserRepository;
    public DbUserData(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }
    
    @Override
    public User getUserById(String userId) {
        // Retrieve user by ID from the database
        return jpaUserRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        // Retrieve user by email from the database
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        // Save user to the database
        jpaUserRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        // Update user in the database
        jpaUserRepository.save(user);
    }

    @Override
    public void deleteUser(String userId) {
        // Delete user from the database
        jpaUserRepository.deleteById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        // Retrieve all users from the database
        return jpaUserRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(String role) {
        // Retrieve users by role from the database
        // return jpaUserRepository.findByRole(role);
        //dbUserRepository
        return null; // Placeholder, implement role-based retrieval if needed
    }

    @Override
    public boolean userExists(String userId) {
        // Check if user exists in the database
        return jpaUserRepository.existsById(userId);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        // Check if user exists by email in the database
        return jpaUserRepository.existsByEmail(email);
    }

}
