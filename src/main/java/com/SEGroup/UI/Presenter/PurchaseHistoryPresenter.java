package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.PurchaseHistoryView;

import java.util.List;

public class PurchaseHistoryPresenter {

    private final PurchaseHistoryView view;
    private final TransactionService transactionService;
    private final MainLayout mainLayout;

    public PurchaseHistoryPresenter(PurchaseHistoryView view) {
        this.view = view;
        this.transactionService = ServiceLocator.getTransactionService();
        this.mainLayout = MainLayout.getInstance();
    }

    public ShoppingProductDTO getProductDetails(String storeName, String productId) {
        Result<ShoppingProductDTO> result = ServiceLocator.getStoreService().getProductFromStore(
                SecurityContextHolder.token(),
                storeName,
                productId
        );

        if (result.isSuccess()) {
            return result.getData();
        }
        return null;
    }

    public void loadPurchaseHistory() {
        if (!SecurityContextHolder.isLoggedIn()) {
            view.showError("Please sign in to view your purchase history");
            return;
        }

        Result<List<TransactionDTO>> result = transactionService.getTransactionHistory(
                SecurityContextHolder.token(),
                SecurityContextHolder.email()
        );

        if (result.isSuccess()) {
            view.showItems(result.getData());
        } else {
            view.showError(result.getErrorMessage());
            view.hideGrid();
        }
    }
}