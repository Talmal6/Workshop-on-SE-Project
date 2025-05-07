package com.SEGroup.Infrastructure.Controllers;

import com.SEGroup.Service.GuestService;
import com.SEGroup.Domain.User.ShoppingCart;
import com.SEGroup.Service.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    /** 1. Create a new guest session */
    @PostMapping("/create-session")
    public ResponseEntity<String> createGuestSession() {
        Result<String> result = guestService.createGuestSession();
        if (result.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result.getData());
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(result.getErrorMessage());
        }
    }

}
