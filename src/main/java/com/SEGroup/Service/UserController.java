// === CONTROLLERS ===

package com.SEGroup.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Service;
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // use case 1.1 - guest viewing
    @PostMapping("/guest")                        // 1.1 Guest Viewing
    public ResponseEntity<?> guest(){
        var res = userService.createGuest();
        return res.isSuccess()
                ? ResponseEntity.ok(res.getData())   // returns JWT
                : ResponseEntity.internalServerError().body(res.getErrorMessage());
    }
    // ✅ Use Case 1.3 - User Registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String email, @RequestParam String password) {
        Result<Void> result = userService.register( email, password);
        if (result.isSuccess()) return ResponseEntity.ok("User registered");
        return ResponseEntity.badRequest().body(result.getErrorMessage());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password) {
        Result<String> result = userService.login(email,password);
        return result.isSuccess()
                ? ResponseEntity.ok(result.getData())   // ← returns JWT token
                : ResponseEntity.status(401).body(result.getErrorMessage());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String token) {
        return userService.logout(token).isSuccess()
                ? ResponseEntity.ok("Logout successful")
                : ResponseEntity.badRequest().body("Invalid session");
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestParam String token) {
        var res = userService.history(token);
        return res.isSuccess()
                ? ResponseEntity.ok(res.getData())
                : ResponseEntity.badRequest().body(res.getErrorMessage());
    }
}
