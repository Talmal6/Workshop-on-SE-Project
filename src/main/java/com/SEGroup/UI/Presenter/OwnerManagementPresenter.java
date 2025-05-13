package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.OwnerManagementView;

import java.util.List;

/**
 * Presenter for the Owner Management view.
 * Handles business logic for managing store owners.
 */
public class OwnerManagementPresenter {
    private final OwnerManagementView view;
    private final StoreService storeService;
    private final String storeName;

    /**
     * Constructs a new OwnerManagementPresenter.
     *
     * @param view The Owner Management view
     * @param storeName The name of the store being managed
     */
    public OwnerManagementPresenter(OwnerManagementView view, String storeName) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
        this.storeName = storeName;
    }

    /**
     * Loads the list of owners for the current store.
     */
    public void loadOwners() {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to view store owners");
                return;
            }
            if(!storeService.isOwner(SecurityContextHolder.email(), this.storeName)){
                            view.showError("You are not an owner for this store");
                            return;
                        }
            Result<List<String>> result = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            if (result.isSuccess()) {
                view.displayOwners(result.getData());
            } else {
                view.showError("Failed to load owners: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error loading store owners: " + e.getMessage());
        }
    }

    /**
     * Appoints a new owner to the current store.
     *
     * @param newOwnerEmail The email of the user to appoint as owner
     */
    public void appointOwner(String newOwnerEmail) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to appoint owners");
                return;
            }

            if (newOwnerEmail == null || newOwnerEmail.trim().isEmpty()) {
                view.showError("Please select a user to appoint");
                return;
            }

            Result<Void> result = storeService.appointOwner(
                    SecurityContextHolder.token(),
                    storeName,
                    newOwnerEmail
            );

            if (result.isSuccess()) {
                view.showSuccess("Owner appointed successfully");
                loadOwners(); // Reload the owners list
            } else {
                view.showError("Failed to appoint owner: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error appointing owner: " + e.getMessage());
        }
    }

    /**
     * Removes an owner from the current store.
     *
     * @param ownerEmail The email of the owner to remove
     */
    public void removeOwner(String ownerEmail) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to remove owners");
                return;
            }

            if (ownerEmail == null || ownerEmail.trim().isEmpty()) {
                view.showError("Please select an owner to remove");
                return;
            }

            Result<Void> result = storeService.removeOwner(
                    SecurityContextHolder.token(),
                    storeName,
                    ownerEmail
            );

            if (result.isSuccess()) {
                view.showSuccess("Owner removed successfully");
                loadOwners(); // Reload the owners list
            } else {
                view.showError("Failed to remove owner: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error removing owner: " + e.getMessage());
        }
    }

    /**
     * Gets the current store name.
     *
     * @return The name of the store being managed
     */
    public String getStoreName() {
        return storeName;
    }

    /**
     * Checks if the current user is authorized to manage owners.
     *
     * @return true if the user is authorized, false otherwise
     */
    public boolean isCurrentUserAuthorized() {
        if (!SecurityContextHolder.isLoggedIn()) {
            return false;
        }

        try {
            Result<List<String>> result = storeService.getAllOwners(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            // User is authorized if they are an owner of the store
            return result.isSuccess() && result.getData().contains(SecurityContextHolder.email());
        } catch (Exception e) {
            view.showError("Error checking authorization: " + e.getMessage());
            return false;
        }
    }
}