package com.mushroom.expense.service.report;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class FinancialYearTest {

    @Test
    void testOfDate() {
        assertEquals("2025-26", FinancialYear.ofDate(LocalDate.of(2025, Month.APRIL, 1)));
        assertEquals("2025-26", FinancialYear.ofDate(LocalDate.of(2025, Month.DECEMBER, 31)));
        assertEquals("2025-26", FinancialYear.ofDate(LocalDate.of(2026, Month.JANUARY, 1)));
        assertEquals("2025-26", FinancialYear.ofDate(LocalDate.of(2026, Month.MARCH, 31)));
        assertEquals("2026-27", FinancialYear.ofDate(LocalDate.of(2026, Month.APRIL, 1)));
    }

    @Test
    void testStartAndEndDates() {
        assertEquals(LocalDate.of(2025, 4, 1), FinancialYear.getStartDate("2025-26"));
        assertEquals(LocalDate.of(2026, 3, 31), FinancialYear.getEndDate("2025-26"));
    }

    @Test
    void testContainsMonth() {
        assertTrue(FinancialYear.containsMonth("2025-26", 2025, 4));
        assertTrue(FinancialYear.containsMonth("2025-26", 2025, 12));
        assertTrue(FinancialYear.containsMonth("2025-26", 2026, 1));
        assertTrue(FinancialYear.containsMonth("2025-26", 2026, 3));
        assertFalse(FinancialYear.containsMonth("2025-26", 2025, 3)); // that's FY 2024-25
        assertFalse(FinancialYear.containsMonth("2025-26", 2026, 4)); // that's FY 2026-27
    }

    @Test
    void testToFyMonthIndex() {
        assertEquals(0, FinancialYear.toFyMonthIndex(4));  // Apr
        assertEquals(1, FinancialYear.toFyMonthIndex(5));  // May
        assertEquals(8, FinancialYear.toFyMonthIndex(12)); // Dec
        assertEquals(9, FinancialYear.toFyMonthIndex(1));  // Jan
        assertEquals(11, FinancialYear.toFyMonthIndex(3)); // Mar
    }

    @Test
    void testInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> FinancialYear.parseStartYear("2025"));
        assertThrows(IllegalArgumentException.class, () -> FinancialYear.parseStartYear("2025/26"));
        assertThrows(IllegalArgumentException.class, () -> FinancialYear.parseStartYear(null));
    }
}
