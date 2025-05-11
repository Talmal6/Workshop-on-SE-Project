package com.SEGroup.UI.Views;

import com.SEGroup.Domain.User.Role;
import com.SEGroup.UI.SecurityContextHolder;
import com.SEGroup.UI.ServiceLocator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.util.Set;

/**
 * Access control class to ensure only store owners and admins can access
 * the base catalog view. This can be extended to other views as needed.
 */
public class ViewAccessControl implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<?> targetView = event.getNavigationTarget();

        // Check access for GeneralCatalogView (base catalog)
        if (targetView == GeneralCatalogView.class) {
            if (!isStoreOwnerOrAdmin()) {
                event.rerouteTo(AccessDeniedView.class);
            }
        }

        // Add additional view access controls as needed for other views
    }

    /**
     * Checks if the current user is a store owner or admin.
     *
     * @return true if the user has the required role, false otherwise
     */
    private boolean isStoreOwnerOrAdmin() {
        // Allow access if the user is admin
        if (SecurityContextHolder.isAdmin()) {
            return true;
        }

        // Not logged in, deny access
        if (!SecurityContextHolder.isLoggedIn()) {
            return false;
        }

        try {
            // Check if user has STORE_OWNER role
            String email = SecurityContextHolder.email();
            Set<Role> roles = ServiceLocator.getUserService().rolesOf(email);
            return roles != null && roles.stream().anyMatch(role -> role == Role.STORE_OWNER);
        } catch (Exception e) {
            System.err.println("Error checking roles: " + e.getMessage());
            return false;
        }
    }
}