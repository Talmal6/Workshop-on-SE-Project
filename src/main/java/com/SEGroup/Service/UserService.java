package com.SEGroup.Service;

import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Infrastructure.PasswordEncoder;

import java.util.Objects;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final IUserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private final ITransactionRepository transactionRepository;




    public UserService(IUserRepository userRepository,
                       IAuthenticationService authenticationService, ITransactionRepository transactionRepository ) {
        this.userRepository        = Objects.requireNonNull(userRepository);
        this.authenticationService = Objects.requireNonNull(authenticationService);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public Result<String> createGuest(){
        try{
            String token = authenticationService.createGuestSession();
            return Result.success(token);
        }
        catch (Exception e){
            return Result.failure(e.getMessage());

        }
    }

    // Registration 1.3

    public Result<Void> register(String email, String rawPassword) {
        try {
            if (userRepository.findByUsername(email) != null) {
                return Result.failure("Email already registered: " + email);
            }
            if (!PasswordPolicy.isStrong(rawPassword))
                return Result.failure("""
            Password must have ≥8 chars, upper-/lower-case letters, a digit and a special char
            """.trim());
            String hash = PasswordEncoder.encrypt(rawPassword);
            UserDTO user = new UserDTO(email, hash);
            userRepository.addUser(user);

            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Failed to register user: " + e.getMessage());
        }
    }
    public Result<String> login(String email, String rawPassword) {
        try {
            UserDTO user = userRepository.findByUsername(email);
            if (user == null ||
                    !PasswordEncoder.checkPassword(rawPassword, user.getPassword())) {
                return Result.failure("Invalid email or password");
            }

            String sessionKey = authenticationService.authenticate(email, rawPassword);
            return Result.success(sessionKey);
        } catch (Exception e) {
            return Result.failure("Failed to login: " + e.getMessage());
        }
    }


    public Result<List<TransactionDTO>> history(String token) {
        try {
            authenticationService.checkSessionKey(token);

            UserDTO user = authenticationService.getUserBySession(token);
            if (user == null)                               // guest
                return Result.failure("Guests have no purchase history");

            return loadHistoryByEmail(user.getEmail());

        } catch (Exception e) {
            return Result.failure("Could not fetch history: " + e.getMessage());
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

    public Result<List<TransactionDTO>> loadHistoryByEmail(String email) {
        try {
            List<TransactionDTO> history = transactionRepository
                    .getAllTransactions()
                    .stream()
                    .filter(t -> email.equals(t.getUserEmail()))
                    .toList();

            return Result.success(history);
        } catch (Exception e) {
            return Result.failure("Could not fetch history: " + e.getMessage());
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


}
