package com.SEGroup.UI.Presenter;

import com.SEGroup.DTO.BidRequestDTO;
import com.SEGroup.Infrastructure.NotificationCenter.Notification;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationEndpoint;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.Views.BidRequest;
import com.SEGroup.UI.Views.BidUsersView;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.NotificationView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter for loading and displaying users who bid on a product.
 */
public class BidUsersPresenter {
    private final BidUsersView view;
    private final StoreService storeService;
    private final String storeName;
    private final String productId;

    public BidUsersPresenter(BidUsersView view, String storeName, String productId) {
        this.view = view;
        this.storeName = storeName;
        this.productId = productId;
        this.storeService = ServiceLocator.getStoreService();
    }

    /**
     * Fetches the list of users who have requested to bid and updates the view.
     */
    public void loadBidUsers() {
        Result<List<BidRequestDTO>> r = storeService.getBidRequests(SecurityContextHolder.token()
                ,this.storeName, this.productId);

        if (r.isSuccess()) {
            List<BidRequest> requests = r.getData().stream()
                    .map(dto -> new BidRequest(dto.getEmail(), dto.getAmount()))
                    .toList();
            view.displayBidUsers(requests);
        } else {
            view.showError("Failed to load bids: " + r.getErrorMessage());
        }
    }
    public void acceptBid(String userEmail, double amount) {
        Result<Notification> res = storeService.respondToBid(
                SecurityContextHolder.token(),
                storeName,
                productId,
                userEmail,
                true   // accepted
        );
        if (res.isSuccess()) {
            view.displayBidUsers(view.usersGrid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                    .filter(b -> !b.email().equals(userEmail))
                    .collect(Collectors.toList()));
            view.showSuccess("Accepted " + userEmail + "’s bid of $" + amount);
        } else {
            view.showError("Could not accept bid: " + res.getErrorMessage());
        }
    }

    public void rejectBid(String userEmail, double amount) {
        Result<Notification> res = storeService.respondToBid(
                SecurityContextHolder.token(),
                storeName,
                productId,
                userEmail,
                false  // rejected
        );
        if (res.isSuccess()) {
            view.displayBidUsers(view.usersGrid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                    .filter(b -> !b.email().equals(userEmail))
                    .collect(Collectors.toList()));
            view.showSuccess("Rejected " + userEmail + "’s bid of $" + amount);
        } else {
            view.showError("Could not reject bid: " + res.getErrorMessage());
        }
    }
}
