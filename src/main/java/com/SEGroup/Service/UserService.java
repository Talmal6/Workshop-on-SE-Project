package com.SEGroup.Service;


import com.SEGroup.Domain.UserDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;

public class UserService {
    // UserService class implementation goes here
    // This class will handle user-related operations and business logic
    // For example, user registration, login, profile management, etc.

    private IUserRepository userRepository;
    private IAuthenticationService authenticationService;
    
    public UserService(IUserRepository userRepository, IAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }


    public void registerUser(String email, String password) {
       UserDTO user = new UserDTO(email, password);
       this.userRepository.addUser(user);
        // Logic for user registration
    }
    
    public String loginUser(String email, String password) {
        String sessionKey = authenticationService.authenticate(email, password);
        if (sessionKey == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return sessionKey;
    }
    

    // Other user-related methods can be added here
    public void deleteUser(String email) {
        UserDTO user = this.userRepository.findByUsername(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        this.userRepository.deleteUser(email);
    }
    
}
