package com.leave.management.system.dto.leavePolicy;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyDto {

    @Positive(message = "Accrual rate must be greater than 0")
    private double accrualRate;

    @Min(value = 0, message = "Carry forward days must be zero or more")
    private double maxCarryForwardDays;
}
