package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.PurchaseHistory.UserPurchaseHistoryView;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.Result;
import java.util.List;

public class UserPurchaseHistoryPresenter {

    private final UserPurchaseHistoryView userPurchaseHistoryView;
    private final String userEmail;
    private final TransactionService transactionService;

    public UserPurchaseHistoryPresenter(UserPurchaseHistoryView userPurchaseHistoryView, String userEmail) {
        this.userEmail = userEmail;
        this.userPurchaseHistoryView = userPurchaseHistoryView;
        this.transactionService = ServiceLocator.getTransactionService();
    }

    public List<TransactionDTO> loadTransactionHistory(String sessionKey) {
        Result<List<TransactionDTO>> result = transactionService.getTransactionHistory(sessionKey, userEmail);

        if (result.isSuccess()) {
            userPurchaseHistoryView.displayPurchaseHistory(result.getData());
        } else {
            userPurchaseHistoryView.displayErrorMessage(result.getErrorMessage());
        }
        return null;
    }
}

