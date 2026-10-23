package com.mushroom.expense.entity;

public enum LoanTransactionType {
    BORROWED("Loans Inward"),
    REPAYMENT("Loan Repayment");

    private final String displayName;

    LoanTransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
