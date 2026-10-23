# Phase 1 Data Model: 4-Pillar Accounting Segregation

## 1. Capital Module Entities

### `CapitalTransactionType` (Enum)
- `INTRODUCED` ("Capital Introduced")
- `DRAWINGS` ("Drawings")

### `CapitalTransaction` (`capital_transactions`)
| Field | Java Type | Column | Constraints |
|---|---|---|---|
| `id` | `Long` | `id` | PK, Auto-increment |
| `type` | `CapitalTransactionType` | `type` | Not Null (`ENUM STRING`) |
| `amount` | `BigDecimal` | `amount` | Not Null, `precision=19, scale=2`, `> 0` |
| `transactionDate` | `LocalDate` | `transaction_date` | Not Null |
| `ownerName` | `String` | `owner_name` | Optional (e.g., Partner/Owner name) |
| `paymentMethod` | `String` | `payment_method` | Optional (`Cash`, `Bank Transfer`, `UPI`, `Cheque`) |
| `description` | `String` | `description` | `TEXT` |
| `status` | `ExpenseStatus` | `status` | Not Null (`DRAFT`, `SUBMITTED`, `UNDER_QUERY`, `APPROVED`, `REJECTED`) |
| `createdBy` | `User` | `created_by` | ManyToOne (`users.id`), Not Null |
| `approvedBy` | `User` | `approved_by` | ManyToOne (`users.id`), Nullable |
| `createdAt` | `LocalDateTime` | `created_at` | Audit timestamp |
| `updatedAt` | `LocalDateTime` | `updated_at` | Audit timestamp |
| `deleted` | ` boolean` | `deleted` | Soft-delete flag (`@SQLDelete`, `@SQLRestriction("deleted = false")`) |
| `comments` | `List<CapitalComment>` | - | OneToMany (`mappedBy="capitalTransaction"`) |
| `attachments` | `List<CapitalAttachment>` | - | OneToMany (`mappedBy="capitalTransaction"`) |

### `CapitalComment` (`capital_comments`) & `CapitalAttachment` (`capital_attachments`)
- Mirror `IncomeComment` and `IncomeAttachment` structure, linked via `capital_transaction_id`.

---

## 2. Loans Module Entities

### `LoanStatus` (Enum)
- `ACTIVE`
- `CLOSED`

### `LoanTransactionType` (Enum)
- `BORROWED` ("Loan Inward / Borrowed")
- `REPAYMENT` ("Loan Repayment")

### `Loan` (`loans`)
| Field | Java Type | Column | Constraints |
|---|---|---|---|
| `id` | `Long` | `id` | PK, Auto-increment |
| `loanName` | `String` | `loan_name` | Not Null, Unique (case-insensitive logical lookup) |
| `lenderName` | `String` | `lender_name` | Not Null |
| `description` | `String` | `description` | `TEXT` |
| `startDate` | `LocalDate` | `start_date` | Nullable |
| `status` | `LoanStatus` | `status` | Not Null (`ACTIVE`, `CLOSED`) |
| `createdBy` | `User` | `created_by` | ManyToOne (`users.id`), Nullable |
| `createdAt` | `LocalDateTime` | `created_at` | Audit timestamp |
| `deleted` | `boolean` | `deleted` | Soft-delete flag |
| `transactions` | `List<LoanTransaction>` | - | OneToMany (`mappedBy="loan"`) |

### `LoanTransaction` (`loan_transactions`)
| Field | Java Type | Column | Constraints |
|---|---|---|---|
| `id` | `Long` | `id` | PK, Auto-increment |
| `loan` | `Loan` | `loan_id` | ManyToOne (`loans.id`), Not Null |
| `type` | `LoanTransactionType` | `type` | Not Null (`BORROWED`, `REPAYMENT`) |
| `amount` | `BigDecimal` | `amount` | Not Null, `precision=19, scale=2`, `> 0` |
| `transactionDate` | `LocalDate` | `transaction_date` | Not Null |
| `paymentMethod` | `String` | `payment_method` | Optional |
| `description` | `String` | `description` | `TEXT` |
| `status` | `ExpenseStatus` | `status` | Not Null |
| `createdBy` | `User` | `created_by` | ManyToOne (`users.id`), Not Null |
| `approvedBy` | `User` | `approved_by` | ManyToOne (`users.id`), Nullable |
| `createdAt` | `LocalDateTime` | `created_at` | Audit timestamp |
| `updatedAt` | `LocalDateTime` | `updated_at` | Audit timestamp |
| `deleted` | `boolean` | `deleted` | Soft-delete flag |
| `comments` | `List<LoanComment>` | - | OneToMany (`mappedBy="loanTransaction"`) |
| `attachments` | `List<LoanAttachment>` | - | OneToMany (`mappedBy="loanTransaction"`) |

---

## 3. Income Module Enhancement

### `Income` (`incomes`)
- Add optional `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sub_category_id") private SubCategory subCategory;`
- Seed new `INCOME` category: `Income From Property Sales`
- Seed default `SubCategory` entries under `Income From Sales`, `Income From Property Sales`, and `Other Income`.
