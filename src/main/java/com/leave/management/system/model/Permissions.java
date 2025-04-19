package com.leave.management.system.model;

import java.util.Arrays;

public enum Permissions {
    POST_MANAGEMENT,USER_MANAGEMENT, VIEW_DASHBOARD;

    public static String GET_PERMISSION(String role) {
        Permissions.valueOf(role.toUpperCase());
        if (Permissions.valueOf(role.toUpperCase()).toString().equalsIgnoreCase(role)) {
            return role.toUpperCase();
        }
        return null;
    }

    public static final String GET_PERMISSION_JSON() {
        StringBuilder data = new StringBuilder();
        data.append("{\"data\": [");
        Arrays.stream(Permissions.values()).forEach(permissions -> {
            data.append("\"" + permissions.toString() + "\",");
        });
        data.deleteCharAt(data.length() - 1);
        data.append("]}");
        return data.toString();
    }
}
