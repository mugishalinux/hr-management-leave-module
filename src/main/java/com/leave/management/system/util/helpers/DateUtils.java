package com.leave.management.system.util.helpers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class DateUtils {

    public static long calculateWorkingDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        long count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek day = date.getDayOfWeek();
            boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
            boolean isHoliday = holidays.contains(date);
            if (!isWeekend && !isHoliday) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
