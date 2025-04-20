package com.leave.management.system.util.helpers;


import com.leave.management.system.enums.UserPermission;
import com.leave.management.system.exceptions.ApiRequestException;

public class PermissionUtils {

    public static UserPermission validateAndParsePermission(String permissionStr) {
        try {
            return UserPermission.valueOf(permissionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Invalid permission, Allowed values are: ADMIN, STAFF, MANAGER.");
        }
    }
}