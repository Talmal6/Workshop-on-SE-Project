package com.SEGroup.UI.Constants;
/**
 * Constants for store manager permissions.
 * This represents the permissions that managers can have in a store.
 */
public class StorePermission {
    public static final String VIEW_ONLY = "VIEW_ONLY";
    public static final String MANAGE_PRODUCTS = "MANAGE_PRODUCTS";
    public static final String MANAGE_POLICIES = "MANAGE_POLICIES";
    public static final String MANAGE_BIDS = "MANAGE_BIDS";
    public static final String MANAGE_ROLES = "MANAGE_ROLES";
    public static final String[] ALL_PERMISSIONS = {
            VIEW_ONLY,
            MANAGE_PRODUCTS,
            MANAGE_POLICIES,
            MANAGE_BIDS,
            MANAGE_ROLES
    };
}