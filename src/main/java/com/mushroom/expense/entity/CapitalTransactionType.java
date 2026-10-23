package com.mushroom.expense.entity;

public enum CapitalTransactionType {
    INTRODUCED("Capital Introduced"),
    DRAWINGS("Drawings");

    private final String displayName;

    CapitalTransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
