//package com.SEGroup.UI.Presenter;
//
//import com.SEGroup.UI.SecurityContextHolder;
//import com.SEGroup.UI.Views.BidUsersView;
//import com.SEGroup.Service.Result;
//import com.SEGroup.Service.StoreService;
//import com.SEGroup.UI.ServiceLocator;
//
//import java.util.List;
//
///**
// * Presenter for loading and displaying users who bid on a product.
// */
//public class BidUsersPresenter {
//    private final BidUsersView view;
//    private final StoreService storeService;
//    private final String storeName;
//    private final String productId;
//
//    public BidUsersPresenter(BidUsersView view, String storeName, String productId) {
//        this.view = view;
//        this.storeName = storeName;
//        this.productId = productId;
//        this.storeService = ServiceLocator.getStoreService();
//    }
//
//    /**
//     * Fetches the list of users who have requested to bid and updates the view.
//     */
//    public void loadBidUsers() {
//        Result<List<String>> result = storeService.getBidUsers(SecurityContextHolder.token()
//                ,this.storeName, this.productId);
////                storeService.getBidUsers(storeName, productId);
//
//
//        if (result.isSuccess()) {
//            view.displayBidUsers(result.getData());
//        } else {
//            view.showError("Failed to load bids: " + result.getErrorMessage());
//        }
//    }
//}
