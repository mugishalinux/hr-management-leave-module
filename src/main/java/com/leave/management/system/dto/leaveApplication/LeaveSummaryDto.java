package com.leave.management.system.dto.leaveApplication;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveSummaryDto {
    private String fullName;
    private LocalDate startDate;
    private LocalDate endDate;

    public LeaveSummaryDto(String fullName, LocalDate startDate, LocalDate endDate) {
    }
}