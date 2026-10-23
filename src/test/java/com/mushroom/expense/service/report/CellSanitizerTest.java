package com.mushroom.expense.service.report;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellSanitizerTest {

    @Test
    void testNullAndEmpty() {
        assertEquals("", CellSanitizer.sanitize(null));
        assertEquals("", CellSanitizer.sanitize(""));
    }

    @Test
    void testNormalString() {
        assertEquals("Fertilizer purchase", CellSanitizer.sanitize("Fertilizer purchase"));
        assertEquals("Seeds 123", CellSanitizer.sanitize("Seeds 123"));
    }

    @Test
    void testFormulaInjectionPrefixes() {
        assertEquals("'=1+1", CellSanitizer.sanitize("=1+1"));
        assertEquals("'+SUM(A1:A10)", CellSanitizer.sanitize("+SUM(A1:A10)"));
        assertEquals("'-500 discount", CellSanitizer.sanitize("-500 discount"));
        assertEquals("'@SUM", CellSanitizer.sanitize("@SUM"));
        assertEquals("'\tTabbed", CellSanitizer.sanitize("\tTabbed"));
        assertEquals("'\rReturn", CellSanitizer.sanitize("\rReturn"));
    }
}
