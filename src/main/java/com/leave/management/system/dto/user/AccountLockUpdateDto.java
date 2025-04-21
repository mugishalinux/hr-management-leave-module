package com.leave.management.system.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountLockUpdateDto {
    @NotNull(message = "Status is required")
    private Boolean status; // true = enable, false = disable
    @NotNull(message = "UserId is required")
    private String userId; // true = enable, false = disable
}
