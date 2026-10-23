package com.mushroom.expense.service.report;

/**
 * Utility to guard against Formula Injection (CSV / Excel Injection / CWE-1236).
 * Prepends a single quote (') to textual values beginning with dangerous characters.
 */
public final class CellSanitizer {

    private static final String DANGEROUS_PREFIXES = "=+-@\t\r";

    private CellSanitizer() {
        // utility class
    }

    /**
     * Sanitizes a cell text value to prevent formula injection.
     *
     * @param input the raw text
     * @return safe text or empty string if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        if (input.isEmpty()) {
            return "";
        }
        char firstChar = input.charAt(0);
        if (DANGEROUS_PREFIXES.indexOf(firstChar) != -1) {
            return "'" + input;
        }
        return input;
    }
}
