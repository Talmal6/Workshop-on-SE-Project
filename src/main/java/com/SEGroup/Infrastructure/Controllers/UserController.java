package com.SEGroup.Infrastructure.Controllers;

import com.SEGroup.Service.UserService;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Service.Result;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/signup")
    public ResponseEntity<Void> register(@RequestParam String username,
                                         @RequestParam String email,
                                         @RequestParam String password) {
        Result<Void> result = userService.register(username, email, password);
        if (result.isSuccess()) {
            // 201 Created, no body
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            // 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<String> login(@RequestParam String email,
                                        @RequestParam String password) {
        Result<String> result = userService.login(email, password);
        if (result.isSuccess()) {
            // 200 OK + session key
            return ResponseEntity.ok(result.getData());
        } else {
            // 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(result.getErrorMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String sessionKey) {
        Result<Void> result = userService.logout(sessionKey);
        if (result.isSuccess()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam String email) {
        Result<Void> result = userService.deleteUser(email);
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/cart/add")
    public ResponseEntity<String> addToUserCart(@RequestParam String sessionKey,
                                                @RequestParam String email,
                                                @RequestParam String productID,
                                                @RequestParam String storeName) {
        Result<String> result = userService.addToUserCart(sessionKey, email, productID, storeName);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PostMapping("/guest-cart/add")
    public ResponseEntity<String> addToGuestCart(@RequestParam String guestToken,
                                                 @RequestParam String productId,
                                                 @RequestParam String storeName) {
        Result<String> result = userService.addToGuestCart(guestToken, productId, storeName);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PostMapping("/cart/purchase")
    public ResponseEntity<Void> purchaseShoppingCart(@RequestParam String sessionKey,
                                                     @RequestParam String email) {
        Result<Void> result = userService.purchaseShoppingCart(sessionKey, email);
        if (result.isSuccess()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/cart/remove")
    public ResponseEntity<String> removeFromUserCart(@RequestParam String sessionKey,
                                                     @RequestParam String email,
                                                     @RequestParam String productID,
                                                     @RequestParam String storeName) {
        Result<String> result = userService.removeFromUserCart(sessionKey, email, productID, storeName);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getErrorMessage());
        }
    }

    @PutMapping("/cart/modify")
    public ResponseEntity<String> modifyProductQuantityInCartItem(@RequestParam String sessionKey,
                                                                  @RequestParam String email,
                                                                  @RequestParam String productID,
                                                                  @RequestParam String storeName,
                                                                  @RequestParam int quantity) {
        Result<String> result = userService.modifyProductQuantityInCartItem(
                sessionKey, email, productID, storeName, quantity);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getErrorMessage());
        }
    }

    @GetMapping("/cart")
    public ResponseEntity<List<BasketDTO>> getUserCart(@RequestParam String sessionKey,
                                                       @RequestParam String email) {
        Result<List<BasketDTO>> result = userService.getUserCart(sessionKey, email);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}