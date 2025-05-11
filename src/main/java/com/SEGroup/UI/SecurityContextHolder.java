package com.SEGroup.UI;

import com.SEGroup.Domain.User.Role;
import com.vaadin.flow.server.VaadinSession;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityContextHolder {

    private static final String KEY = "SESSION_DATA";
    public static final String GUEST_TOKEN_KEY = "GUEST_TOKEN";

    private record SessionData(String token, String email, Set<Role> roles) {
    }

    private SecurityContextHolder() {
    }

    /* ---------- session lifecycle ---------- */

    public static void openSession(String token, String email) {
        Set<Role> roles = ServiceLocator.getUserService().rolesOf(email);
        VaadinSession.getCurrent()
                .setAttribute(KEY, new SessionData(token, email, roles));
    }

    public static void closeSession() {
        VaadinSession.getCurrent().setAttribute(KEY, null);
    }

    /* ---------- getters ---------- */

    private static SessionData get() {
        return (SessionData) VaadinSession.getCurrent().getAttribute(KEY);
    }

    public static boolean isLoggedIn() {
        return get() != null;
    }

    public static boolean isAdmin() {
        return hasRole(Role.SYSTEM_MANAGER);
    }

    public static boolean hasRole(Role r) {
        return get() != null && get().roles().contains(r);
    }

    public static String email() {
        return get() == null ? "guest" : get().email();
    }

    public static Role primaryRole() {
        return get() == null ?
                Role.GUEST :
                get().roles().stream().findFirst().orElse(Role.SUBSCRIBER);
    }

    /**
     * Stores a guest token in the session.
     * This allows guest users to have persistent carts without formal login.
     */
    public static void storeGuestToken(String token) {
        if (VaadinSession.getCurrent() != null) {
            System.out.println("Storing guest token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
            VaadinSession.getCurrent().setAttribute(GUEST_TOKEN_KEY, token);
        } else {
            System.err.println("Cannot store guest token - no active session");
        }
    }

    /**
     * Gets the current authenticated token or guest token.
     * This method first checks for a regular authenticated session token.
     * If not found, it falls back to the guest token if available.
     *
     * @return The session token or null if no token is available
     */
    public static String token() {
        SessionData data = get();
        if (data != null) {
            return data.token();
        }

        // Check for guest token
        if (VaadinSession.getCurrent() != null) {
            String guestToken = (String) VaadinSession.getCurrent().getAttribute(GUEST_TOKEN_KEY);
            if (guestToken != null) {
                System.out.println("Using guest token: " + guestToken.substring(0, Math.min(guestToken.length(), 10)) + "...");
                return guestToken;
            }
        }

        return null;
    }
    public static boolean isStoreOwner() {
        return hasRole(Role.STORE_OWNER);
    }

    // In SecurityContextHolder.java, ensure this method exists:

}