package com.SEGroup.Service;

import com.SEGroup.Domain.UserDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;

public class UserService {
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;

    public UserService(IUserRepository userRepository,
                       IAuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
      
    }

    public Result<Void> register(String username, String email, String password) {
        try {
            if (userRepository.findByUsername(email) != null) {
                return Result.failure("Email already registered: " + email);
            }
            UserDTO user = new UserDTO(email, password);
            userRepository.addUser(user);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }

    public Result<String> login(String email, String password) {
        try {
            String sessionKey = authenticationService.authenticate(email, password);
            if (sessionKey == null) {
                return Result.failure("Invalid email or password");
            }
            return Result.success(sessionKey);
        } catch (Exception e) {
            return Result.failure("Failed to login: " + e.getMessage());
        }
    }

    public Result<Void> logout(String sessionKey) {
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


    public Result<Void> addToCart(String userEmail, String shoppingProductId) {
        try {
            UserDTO user = userRepository.findByUsername(userEmail);
            if (user == null) {
                return Result.failure("User not found: " + userEmail);
            }
            //user.getCart().add(shoppingProductId);
            //userRepository.updateUser(user);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to add product to cart: " + e.getMessage());
        }
    }




}
