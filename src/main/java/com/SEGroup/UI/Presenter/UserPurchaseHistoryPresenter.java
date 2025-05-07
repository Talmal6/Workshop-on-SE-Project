package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.Service.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserPurchaseHistoryPresenter {

    private static final Logger logger = LoggerFactory.getLogger(UserPurchaseHistoryPresenter.class);
    private final TransactionService transactionService;

    @Autowired
    public UserPurchaseHistoryPresenter(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public List<TransactionDTO> getUserPurchaseHistory(String sessionKey, String userEmail) {
        Result<List<TransactionDTO>> result = transactionService.getTransactionHistory(sessionKey, userEmail);
        if (result.isSuccess()) {
            return result.getData();
        } else {
            logger.error("Failed to get purchase history for user {}: {}", userEmail, result.getErrorMessage());
            return Collections.emptyList();
        }
    }
}
