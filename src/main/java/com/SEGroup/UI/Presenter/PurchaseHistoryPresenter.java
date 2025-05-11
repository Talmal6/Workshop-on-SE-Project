package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.Service.TransactionService;
import com.SEGroup.UI.MainLayout;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.PurchaseHistoryView;

import java.util.List;

public class PurchaseHistoryPresenter {
    
    private final PurchaseHistoryView view;
    private final TransactionService transactionService;
    private final StoreService storeService;
    private final MainLayout mainLayout;

    public PurchaseHistoryPresenter(PurchaseHistoryView view) {
        this.view = view;
        this.transactionService = ServiceLocator.getTransactionService();
        this.storeService = ServiceLocator.getStoreService();
        this.mainLayout = MainLayout.getInstance();
    }

    public void loadPurchaseHistory() {
        if (false) {
            view.showError("Please sign in to view your purchase history");
            return;
        }

        Result<List<TransactionDTO>> result = transactionService.getTransactionHistory(
            mainLayout.getSessionKey(),
            mainLayout.getUserEmail()
        );

        if (result.isSuccess()) {
            view.showItems(result.getData());
        } else {
            view.showError(result.getErrorMessage());
        }
    }

    public ShoppingProductDTO getProductDetails(String storeName, String productId) {
        Result<ShoppingProductDTO> result = storeService.getProductFromStore(
            mainLayout.getSessionKey(),
            storeName,
            productId
        );
        
        if (result.isSuccess()) {
            return result.getData();
        }
        return null;
    }
} 