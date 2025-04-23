package com.SEGroup.Service;

import java.util.UUID;

import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.UserDTO;
import com.SEGroup.Infrastructure.IAuthenticationService;

public class UserService {
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;

    public UserService(IUserRepository userRepository,
                       IAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
      
    }

    public Result<Void> registerUser(String email, String password) {
        try {
            if (userRepository.findByUsername(email) != null) {
                return Result.failure("Email already registered: " + email);
            }
            // 5-adapted changed register logic
            UserDTO user = new UserDTO(email, authenticationService.encryptPassword(password));
            userRepository.addUser(user);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    public Result<String> loginUser(String email, String password) {
        try {
            UserDTO user = userRepository.findByUsername(email);
            if(user == null){
                return Result.failure("Invalid email");
            }
            authenticationService.matchPassword(user.getPassword(), password);
            String sessionKey = authenticationService.authenticate(email);
            return Result.success(sessionKey);
        } catch (Exception e) {
            return Result.failure("Failed to login: " + e.getMessage());
        }
    }

    public Result<Void> logoutUser(String sessionKey) {
        try {
            authenticationService.invalidateSession(sessionKey);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to logout: " + e.getMessage());
        }
    }

    public Result<Void> deleteUser(String email) {
        try {
            UserDTO user = userRepository.findByUsername(email);
            if (user == null) {
                return Result.failure("User not found: " + email);
            }
            userRepository.deleteUser(email);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to delete user: " + e.getMessage());
        }
    }

        // Use Case 1.1 GuestLogin
    public Result<String> guestLogin(){
        String guestName = "g_" + UUID.randomUUID().toString();
        userRepository.addNewGuest(guestName);
        return Result.success(authenticationService.authenticate(guestName));
    }

    public Result<String> addToCart(String sessionKey, int storeID, int productID){
        try {
            authenticationService.checkSessionKey(sessionKey);
            userRepository.addToCart(userRepository.findByUsername(authenticationService.getUserBySession(sessionKey)), storeID, productID);
            return Result.success("Add item to cart succsesfully");    
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
