// Update your RoleManagementPresenter.java
package com.SEGroup.UI.Presenter;

import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.SEGroup.UI.Views.RoleManagementView;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for the RoleManagementView.
 * Handles business logic for managing store managers and their permissions.
 */
public class RoleManagementPresenter {
    private final RoleManagementView view;
    private final StoreService storeService;
    private final String storeName;

    /**
     * Constructs a new RoleManagementPresenter.
     *
     * @param view The RoleManagement view
     * @param storeName The name of the store being managed
     */
    public RoleManagementPresenter(RoleManagementView view, String storeName) {
        this.view = view;
        this.storeService = ServiceLocator.getStoreService();
        this.storeName = storeName;
    }

    /**
     * Loads the list of managers for the current store.
     */
    public void loadManagers() {
        try {
            if(!storeService.isOwner(SecurityContextHolder.email(),storeName) ||
            !SecurityContextHolder.isAdmin()){
                view.showError("You don't have a permission to manage roles");
                return;
            }
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to view store managers");
                return;
            }

            Result<List<String>> result = storeService.getAllManagers(
                    SecurityContextHolder.token(),
                    storeName,
                    SecurityContextHolder.email()
            );

            if (result.isSuccess()) {
                List<RoleManagementView.ManagerDetails> managerDetailsList = new ArrayList<>();

                for (String managerEmail : result.getData()) {
                    // For each manager, get their permissions
                    Result<List<String>> permissionsResult =
                            storeService.getManagerPermission(
                                    SecurityContextHolder.token(),
                                    storeName,
                                    managerEmail
                            );

                    if (permissionsResult.isSuccess() && permissionsResult.getData() != null) {
                        List<String> permissionStrings = permissionsResult.getData();

                        managerDetailsList.add(new RoleManagementView.ManagerDetails(
                                managerEmail,
                                permissionStrings
                        ));
                    }
                }

                view.displayManagers(managerDetailsList);
            } else {
                view.showError("Failed to load managers: " +
                        (result.getErrorMessage() != null ?
                                result.getErrorMessage() : "Unknown error"));
            }
        } catch (Exception e) {
            view.showError("Error loading store managers: " + e.getMessage());
        }
    }

    /**
     * Appoints a new manager to the current store with the specified permissions.
     *
     * @param managerEmail The email of the user to appoint as manager
     * @param permissions The list of permissions to grant to the manager
     */
    public void appointManager(String managerEmail, List<String> permissions) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to appoint managers");
                return;
            }

            if (managerEmail == null || managerEmail.trim().isEmpty()) {
                view.showError("Please select a user to appoint");
                return;
            }

            if (permissions == null || permissions.isEmpty()) {
                view.showError("Please select at least one permission");
                return;
            }

            Result<Void> result = storeService.appointManager(
                    SecurityContextHolder.token(),
                    storeName,
                    managerEmail,
                    permissions
            );

            if (result.isSuccess()) {
                view.showSuccess("Manager appointed successfully");
                loadManagers(); // Reload the managers list
            } else {
                view.showError("Failed to appoint manager: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error appointing manager: " + e.getMessage());
        }
    }

    /**
     * Updates the permissions of an existing manager in the current store.
     *
     * @param managerEmail The email of the manager whose permissions are being updated
     * @param permissions The new list of permissions for the manager
     */
    public void updateManagerPermissions(String managerEmail, List<String> permissions) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to update manager permissions");
                return;
            }

            if (managerEmail == null || managerEmail.trim().isEmpty()) {
                view.showError("Please select a manager to update");
                return;
            }

            if (permissions == null) {
                permissions = new ArrayList<>(); // Empty list means no permissions
            }

            Result<Void> result = storeService.updateManagerPermissions(
                    SecurityContextHolder.token(),
                    storeName,
                    managerEmail,
                    permissions
            );

            if (result.isSuccess()) {
                view.showSuccess("Manager permissions updated successfully");
                loadManagers(); // Reload the managers list
            } else {
                view.showError("Failed to update manager permissions: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error updating manager permissions: " + e.getMessage());
        }
    }

    /**
     * Removes a manager from the current store.
     *
     * @param managerEmail The email of the manager to remove
     */
    public void removeManager(String managerEmail) {
        try {
            if (!SecurityContextHolder.isLoggedIn()) {
                view.showError("You must be logged in to remove managers");
                return;
            }

            if (managerEmail == null || managerEmail.trim().isEmpty()) {
                view.showError("Please select a manager to remove");
                return;
            }

            // The StoreService doesn't have a direct removeManager method,
            // so we'll use updateManagerPermissions with an empty list to remove all permissions
            Result<Void> result = storeService.updateManagerPermissions(
                    SecurityContextHolder.token(),
                    storeName,
                    managerEmail,
                    new ArrayList<>()  // Empty permissions list effectively removes the manager
            );

            if (result.isSuccess()) {
                view.showSuccess("Manager removed successfully");
                loadManagers(); // Reload the managers list
            } else {
                view.showError("Failed to remove manager: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            view.showError("Error removing manager: " + e.getMessage());
        }
    }

    /**
     * Gets the current store name.
     *
     * @return The store name
     */
    public String getStoreName() {
        return storeName;
    }

    /**
     * Checks if the current user is authorized to manage roles.
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