package com.SEGroup.Domain.Store;

/**
 * Represents the permissions that a store manager can have.
 * These permissions determine what actions the manager can perform within the store.
 */
public enum ManagerPermission {
    VIEW_ONLY,
    MANAGE_PRODUCTS,
    MANAGE_POLICIES,
    MANAGE_BIDS,
    MANAGE_ROLES
}
