package com.SEGroup.Infrastructure.Controllers;

import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.Result;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.DTO.TransactionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 1. Fetch the raw transaction history for a user
     *    GET /api/transaction/history?sessionKey=…&userEmail=…
     */
    @GetMapping("/history")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory(
            @RequestParam String sessionKey,
            @RequestParam String userEmail
    ) {
        Result<List<TransactionDTO>> r = transactionService.getTransactionHistory(sessionKey, userEmail);
        if (r.isSuccess()) {
            return ResponseEntity.ok(r.getData());
        } else {
            // e.g. invalid session or user not found
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 2. Purchase the current shopping cart (charges card and records transactions)
     *    POST /api/transaction/purchase
     *      body-form: sessionKey, userEmail, paymentDetails
     */
    @PostMapping("/purchase")
    public ResponseEntity<Void> purchaseShoppingCart(
            @RequestParam String sessionKey,
            @RequestParam String userEmail,
            @RequestParam String paymentDetails
    ) {
        Result<Void> r = transactionService.purchaseShoppingCart(sessionKey, userEmail, paymentDetails);
        if (r.isSuccess()) {
            // 200 OK means “all done”
            return ResponseEntity.ok().build();
        } else {
            // could be payment failure, inventory rollback, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 3. Fetch the “purchase history” view for a user 
     *    (alias of history or if you want a different DTO shape)
     *    GET /api/transaction/purchase-history?sessionKey=…&userEmail=…
     */
    @GetMapping("/purchase-history")
    public ResponseEntity<List<TransactionDTO>> viewPurchaseHistory(
            @RequestParam String sessionKey,
            @RequestParam String userEmail
    ) {
        Result<List<TransactionDTO>> r = transactionService.viewPurcaseHistory(sessionKey, userEmail);
        if (r.isSuccess()) {
            return ResponseEntity.ok(r.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
