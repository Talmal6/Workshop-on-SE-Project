package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.Result;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.CheckoutDialog;

public class CheckoutPresenter {
    private final TransactionService tx = ServiceLocator.getTransactionService();
    private final CheckoutDialog view;

    public CheckoutPresenter(CheckoutDialog v){ this.view = v; }

    public void onPay(String jwt, String email, String card){
        Result<Void> r = tx.purchaseShoppingCart(jwt, email, card);
        if (r.isSuccess()) view.showSuccess("Purchase completed!");
        else               view.showError(r.getErrorMessage());
    }
}