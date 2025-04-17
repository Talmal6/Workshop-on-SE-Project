package com.SEGroup.Domain;
import java.util.List;


public interface IUserRepository {
    UserDTO findByUsername(String email);
    void addUser(UserDTO user);
    void deleteUser(String email);
}
