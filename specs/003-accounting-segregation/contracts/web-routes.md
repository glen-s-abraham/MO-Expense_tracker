# Web & REST Endpoint Contracts: 4-Pillar Accounting Segregation

## 1. Capital Module Routes (`CapitalController` at `/capital`)
- `GET /capital`: Tabbed Capital dashboard (`tab=INTRODUCED` or `tab=DRAWINGS`), filterable by date range, payment method, status, and amount range; includes KPI summary (`totalIntroduced`, `totalDrawings`, `netCapital`).
- `GET /capital/new?type=INTRODUCED|DRAWINGS`: Render new Capital transaction form.
- `POST /capital/save`: Create or update Capital transaction (supports Draft vs Submit and multipart file attachments).
- `GET /capital/edit/{id}`: Edit existing Capital transaction.
- `GET /capital/view/{id}`: View details, audit trail, attachments, and comments for a Capital transaction.
- `POST /capital/submit/{id}`: Transition `DRAFT` / `UNDER_QUERY` -> `SUBMITTED`.
- `POST /capital/delete/{id}`: Soft-delete (`DRAFT` by creator or `ADMIN`).
- `POST /capital/approve/{id}`, `POST /capital/reject/{id}`, `POST /capital/query/{id}`: Approval workflow transitions (`SUPERVISOR`, `ACCOUNTANT`, `MANAGER`, `ADMIN`).
- `POST /capital/comment/{id}`: Add comment on a Capital transaction.
- `GET /capital/attachment/{id}`: Download attachment.
- `GET /capital/export`: Export filtered Capital transactions to `.xlsx`.

## 2. Loans Module Routes (`LoanController` at `/loans`)
- `GET /loans`: Loans dashboard displaying individual `Loan` facility cards (with Borrowed, Repaid, Outstanding, Status) and two transaction tabs: `tab=BORROWED` (**Loans Inward**) and `tab=REPAYMENT` (**Loan Repayments**).
- `POST /loans/facility`: Create a new `Loan` facility (`loanName`, `lenderName`, `startDate`, `description`).
- `GET /loans/new?type=BORROWED|REPAYMENT&loanId={id}`: Render new Loan transaction form (with option to create a new Loan inline when `type=BORROWED`).
- `POST /loans/save`: Save or submit a `LoanTransaction`. Validates `amount <= loanOutstandingBalance` when `type == REPAYMENT`.
- `GET /loans/edit/{id}`, `GET /loans/view/{id}`, `POST /loans/submit/{id}`, `POST /loans/delete/{id}`: Standard lifecycle endpoints.
- `POST /loans/approve/{id}`, `POST /loans/reject/{id}`, `POST /loans/query/{id}`: Approval workflow transitions; approving a transaction recalculates the parent `Loan` status (`CLOSED` when outstanding == 0, `ACTIVE` when > 0).
- `POST /loans/comment/{id}`, `GET /loans/attachment/{id}`, `GET /loans/export`: Comments, attachments, and `.xlsx` export.

## 3. Dashboard & REST Endpoints (`DashboardController` & `DashboardRestController`)
- `GET /dashboard`: Populates Lifetime Operational P&L (`totalIncome`, `totalExpense`, `netProfit`) and Lifetime Financing & Liquidity (`totalCapitalIntroduced`, `totalDrawings`, `netCapitalPool`, `totalLoanBorrowed`, `totalLoanRepaid`, `outstandingLoanPool`, `netCashPosition`), plus individual `loanSummaries` and `capitalBreakdown` for the expandable pool modals.
- `GET /api/dashboard/monthly-summary?year={y}&month={m}`: Returns Monthly Operational P&L (`totalIncome`, `totalExpense`, `netProfit`, breakdowns, daily trend) AND Monthly Financing (`capitalIntroduced`, `drawings`, `netCapitalChange`, `loansBorrowed`, `loanRepayments`, `netLoanChange`, `netCashFlow`).
- `GET /api/dashboard/yearly-summary?year={fy}`: Returns Financial Year (Apr–Mar) Operational P&L AND Yearly Financing metrics + monthly 4-pillar trend series.
- `GET /api/dashboard/loan-pool-breakdown`: Returns JSON list of all individual loans with `id`, `loanName`, `lenderName`, `borrowed`, `repaid`, `outstanding`, `status`.
- `GET /api/dashboard/capital-pool-breakdown`: Returns JSON summary of `introduced`, `drawings`, `netCapital`, and recent transactions.
