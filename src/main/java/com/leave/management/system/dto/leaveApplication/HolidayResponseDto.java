package com.leave.management.system.dto.leaveApplication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class HolidayResponseDto {
    private LocalDate date;
    private String description;
}
