package com.mushroom.expense.service.report;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper utility for Indian Financial Year (1 April - 31 March) calculations,
 * formatting, and month-ordering.
 */
public final class FinancialYear {

    public static final List<String> FY_MONTH_NAMES = Collections.unmodifiableList(
            Arrays.asList("Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar")
    );

    private FinancialYear() {
    }

    /**
     * Determines current FY string in YYYY-YY format (e.g. "2025-26").
     */
    public static String currentFy() {
        return ofDate(LocalDate.now());
    }

    /**
     * Determines the FY string for a given date.
     */
    public static String ofDate(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int startYear = (month >= 4) ? year : year - 1;
        return formatFy(startYear);
    }

    /**
     * Determines the FY string for a given calendar year and month.
     */
    public static String ofYearMonth(int year, int month) {
        int startYear = (month >= 4) ? year : year - 1;
        return formatFy(startYear);
    }

    /**
     * Formats start year into "YYYY-YY" (e.g. 2025 -> "2025-26").
     */
    public static String formatFy(int startYear) {
        int endYearShort = (startYear + 1) % 100;
        return String.format("%d-%02d", startYear, endYearShort);
    }

    /**
     * Parses the start year from FY string (e.g. "2025-26" or "2025-2026" -> 2025).
     */
    public static int parseStartYear(String fy) {
        if (fy == null) {
            throw new IllegalArgumentException("Financial Year cannot be null");
        }
        fy = fy.trim();
        if (fy.matches("^\\d{4}-\\d{2,4}$")) {
            return Integer.parseInt(fy.split("-")[0]);
        }
        throw new IllegalArgumentException("Invalid Financial Year format. Expected YYYY-YY or YYYY-YYYY (e.g. 2025-26), got: " + fy);
    }

    /**
     * Returns the start date of the financial year (April 1).
     */
    public static LocalDate getStartDate(String fy) {
        int startYear = parseStartYear(fy);
        return LocalDate.of(startYear, Month.APRIL, 1);
    }

    /**
     * Returns the end date of the financial year (March 31).
     */
    public static LocalDate getEndDate(String fy) {
        int startYear = parseStartYear(fy);
        return LocalDate.of(startYear + 1, Month.MARCH, 31);
    }

    /**
     * Validates if a calendar year and month fall within the specified financial year.
     */
    public static boolean containsMonth(String fy, int year, int month) {
        if (month < 1 || month > 12) {
            return false;
        }
        int startYear = parseStartYear(fy);
        int expectedStartYear = (month >= 4) ? year : year - 1;
        return startYear == expectedStartYear;
    }

    /**
     * Maps calendar month (1-12) to April-first zero-based index (0-11).
     * Apr=0, May=1, ..., Dec=8, Jan=9, Feb=10, Mar=11.
     */
    public static int toFyMonthIndex(int calendarMonth) {
        if (calendarMonth < 1 || calendarMonth > 12) {
            throw new IllegalArgumentException("Invalid calendar month: " + calendarMonth);
        }
        return (calendarMonth >= 4) ? calendarMonth - 4 : calendarMonth + 8;
    }
}
