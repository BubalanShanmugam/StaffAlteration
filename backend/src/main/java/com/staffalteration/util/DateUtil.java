package com.staffalteration.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class DateUtil {
    
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }
    
    public static boolean isBeforeNineAM() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) < 9;
    }
    
    public static int getWeekOfYear(LocalDate date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(java.sql.Date.valueOf(date));
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
    
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.minus(date.getDayOfWeek().getValue() - 1, ChronoUnit.DAYS);
    }
    
    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.plus(7 - date.getDayOfWeek().getValue(), ChronoUnit.DAYS);
    }
    
    public static boolean isSameWeek(LocalDate date1, LocalDate date2) {
        return getWeekOfYear(date1) == getWeekOfYear(date2) && date1.getYear() == date2.getYear();
    }
}
