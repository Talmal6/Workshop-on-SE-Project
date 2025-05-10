package com.SEGroup.Infrastructure.Endpoints;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.Result;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Endpoint
@PermitAll        // demo: allow anyone; secure in prod!
public class TransactionEndpoint {

    private final TransactionService service;
    public TransactionEndpoint(TransactionService s) { this.service = s; }

    public List<TransactionDTO> history(String sessionKey, String userEmail) {
        return service.getTransactionHistory(sessionKey, userEmail).getData();
    }

    public void purchase(String sessionKey, String userEmail, String paymentDetails) {
        service.purchaseShoppingCart(sessionKey, userEmail, paymentDetails);
    }
}
