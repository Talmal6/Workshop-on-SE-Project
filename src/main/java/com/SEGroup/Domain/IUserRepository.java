package com.SEGroup.Domain;
import java.util.List;


public interface IUserRepository {
    // Define methods for user-related operations
    void addUser(UserDTO user);
    UserDTO getUserById(int id);
    void updateUser(UserDTO user);
    void deleteUser(String email);
    List<UserDTO> getAllUsers();
    UserDTO findByUsername(String username);
    boolean existsByUsername(String username);
    
}
