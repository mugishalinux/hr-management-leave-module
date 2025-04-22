package com.leave.management.system.constants;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class HolidayDescriptionConstants {
    public static final Map<LocalDate, String> HOLIDAYS_2025_WITH_DESCRIPTION = Map.ofEntries(
            Map.entry(LocalDate.of(2025, 1, 1), "New Year’s Day"),
            Map.entry(LocalDate.of(2025, 2, 1), "National Heroes Day"),
            Map.entry(LocalDate.of(2025, 4, 7), "Genocide Memorial Day"),
            Map.entry(LocalDate.of(2025, 4, 18), "Good Friday"),
            Map.entry(LocalDate.of(2025, 4, 21), "Easter Monday"),
            Map.entry(LocalDate.of(2025, 5, 1), "Labor Day"),
            Map.entry(LocalDate.of(2025, 7, 1), "Independence Day"),
            Map.entry(LocalDate.of(2025, 7, 4), "Liberation Day"),
            Map.entry(LocalDate.of(2025, 8, 1), "Umuganura Day"),
            Map.entry(LocalDate.of(2025, 8, 15), "Assumption Day"),
            Map.entry(LocalDate.of(2025, 12, 25), "Christmas Day"),
            Map.entry(LocalDate.of(2025, 12, 26), "Boxing Day")
    );
}
