// === CONTROLLERS ===

package com.SEGroup.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ Use Case 1.3 - User Registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        Result<Void> result = userService.register(username, email, password);
        if (result.isSuccess()) return ResponseEntity.ok("User registered");
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // ✅ Use Case 1.4 - User Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        Result<Void> result = userService.login(email, password);
        if (result.isSuccess()) return ResponseEntity.ok("Login successful");
        return ResponseEntity.status(401).body(result.getErrorMessage());
    }

    // ✅ Use Case 3.1 - User Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String email) {
        Result<Void> result = userService.logout(email);
        if (result.isSuccess()) return ResponseEntity.ok("Logout successful");
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    // ✅ Use Case 3.7 - View Personal Purchase History
    @GetMapping("/history") 
    public ResponseEntity<?> getUserHistory(@RequestParam String email) {
        Result<?> history = userService.getUserHistory(email);
        if (history.isSuccess()) return ResponseEntity.ok(history.getData());
        return ResponseEntity.badRequest().body(history.getErrorMessage());
    }
}
