# Data Model: Production Dump H2 Seed Data

## Target Tables & Foreign Key Dependency Order

The seed data populates the 9 JPA entity tables in strict parent-to-child order:

| Load Order | Table Name | JPA Entity | Primary Key | Foreign Keys | Prod Row Count (Snapshot) |
|------------|------------|------------|-------------|--------------|---------------------------|
| 1 | `categories` | `Category` | `id (BIGINT IDENTITY)` | None | 14 |
| 2 | `sub_categories` | `SubCategory` | `id (BIGINT IDENTITY)` | `category_id -> categories(id)` | 43 |
| 3 | `users` | `User` | `id (BIGINT IDENTITY)` | None | 5 (+ dev fallback users) |
| 4 | `expenses` | `Expense` | `id (BIGINT IDENTITY)` | `user_id -> users(id)`, `category_id -> categories(id)`, `sub_category_id -> sub_categories(id)` | 107 |
| 5 | `expense_attachments` | `ExpenseAttachment` | `id (BIGINT IDENTITY)` | `expense_id -> expenses(id)` | 39 |
| 6 | `expense_comments` | `ExpenseComment` | `id (BIGINT IDENTITY)` | `expense_id -> expenses(id)`, `user_id -> users(id)` | 9 |
| 7 | `incomes` | `Income` | `id (BIGINT IDENTITY)` | `user_id -> users(id)`, `category_id -> categories(id)` | 33 |
| 8 | `income_attachments` | `IncomeAttachment` | `id (BIGINT IDENTITY)` | `income_id -> incomes(id)` | 0 |
| 9 | `income_comments` | `IncomeComment` | `id (BIGINT IDENTITY)` | `income_id -> incomes(id)`, `user_id -> users(id)` | 3 |

## Sequence Synchronization Rules

After inserting rows with explicit `id` values into H2, each table's `IDENTITY` generator must be synchronized:

```sql
ALTER TABLE categories ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM categories);
ALTER TABLE sub_categories ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM sub_categories);
ALTER TABLE users ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM users);
ALTER TABLE expenses ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM expenses);
ALTER TABLE expense_attachments ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM expense_attachments);
ALTER TABLE expense_comments ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM expense_comments);
ALTER TABLE incomes ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM incomes);
ALTER TABLE income_attachments ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM income_attachments);
ALTER TABLE income_comments ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM income_comments);
```

## Force-Reset Truncation Order (When `app.seed.force-reset=true`)

When force-resetting the local H2 database in dev mode, tables are cleared in reverse dependency order (child-to-parent):
1. `income_comments`
2. `income_attachments`
3. `incomes`
4. `expense_comments`
5. `expense_attachments`
6. `expenses`
7. `sub_categories`
8. `categories`
9. `users`
