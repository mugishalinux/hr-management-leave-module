package com.leave.management.system.constants;

import java.time.LocalDate;
import java.util.Set;

public class HolidayConstants {
    public static final Set<LocalDate> HOLIDAYS_2025 = Set.of(
            LocalDate.of(2025, 1, 1),   // New Year’s Day
            LocalDate.of(2025, 2, 1),   // National Heroes Day
            LocalDate.of(2025, 4, 7),   // Genocide Memorial Day
            LocalDate.of(2025, 4, 18),  // Good Friday
            LocalDate.of(2025, 4, 21),  // Easter Monday
            LocalDate.of(2025, 5, 1),   // Labor Day
            LocalDate.of(2025, 7, 1),   // Independence Day
            LocalDate.of(2025, 7, 4),   // Liberation Day
            LocalDate.of(2025, 8, 1),   // Umuganura Day
            LocalDate.of(2025, 8, 15),  // Assumption Day
            LocalDate.of(2025, 12, 25), // Christmas Day
            LocalDate.of(2025, 12, 26)  // Boxing Day
    );
}
