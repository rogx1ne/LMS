package com.library.service;

public final class CurrentUserContext {
    private static volatile String userId = "ADMIN";
    private static volatile String displayName = "ADMIN";
    private static volatile String role = "ADMIN";

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
}
