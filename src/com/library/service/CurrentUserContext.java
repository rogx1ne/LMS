package com.library.service;

public final class CurrentUserContext {
    private static volatile String userId = "";
    private static volatile String displayName = "";
    private static volatile String role = "";

    private CurrentUserContext() {}

    public static String getUserId() {
        return userId;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdministrator() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isAuthenticated() {
        return userId != null && !userId.trim().isEmpty();
    }

    public static void setUser(String userId, String displayName, String role) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }
        CurrentUserContext.userId = userId.trim();
        CurrentUserContext.displayName =
            (displayName == null || displayName.trim().isEmpty()) ? CurrentUserContext.userId : displayName.trim();
        CurrentUserContext.role =
            (role == null || role.trim().isEmpty()) ? "LIBRARIAN" : role.trim().toUpperCase();
    }

    public static void clear() {
        userId = "";
        displayName = "";
        role = "";
    }
}
