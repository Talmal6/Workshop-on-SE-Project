package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.User.User;

public interface UserData {
    User getUserById(String userId);
    User getUserByEmail(String email);
    void saveUser(User user);
    void updateUser(User user);
    void deleteUser(String userId);
    List<User> getAllUsers();
    List<User> getUsersByRole(String role);
    boolean userExists(String userId);
    boolean userExistsByEmail(String email);
    
}
