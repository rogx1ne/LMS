# Module Workflows

## Login
- User enters ID and password.
- `UserDAO.validateLogin` verifies credentials.
- `CurrentUserContext` is set.
- `DashboardFrame` opens with admin-only module visibility based on role.

## Book
- Add Book:
  - UI collects catalog/copy details.
  - `BookLogic` validates and normalizes fields.
  - `BookDAO` inserts into `TBL_BOOK_INFORMATION` and ensures catalog row exists.
- Register/View:
  - Accession register loads all copies.
  - Stock summary and low-stock reporting are derived from `TBL_BOOK_INFORMATION`.

## Transaction / Procurement
- Seller Master:
  - create/update sellers
  - seller IDs come from `ProcurementIdGenerator`
- Add Order:
  - collect order header plus detail rows
  - persist to `TBL_ORDER_HEADER` and `TBL_ORDER_DETAILS`
- View Order:
  - review summaries
  - open details
  - download PDF receipt
  - optionally email receipt

## Student
- Registration:
  - validate name, roll, phone, session, course, limit, fee
  - generate card ID and receipt number
  - insert into `TBL_STUDENT`
- View/Edit:
  - active students list
  - preview dialog
  - borrow history lookup

## Circulation
- Issue:
  - verify active student
  - enforce book limit
  - verify book is active and available
  - insert `TBL_ISSUE`
  - set `CIRC_STATUS = 'ISSUED'`
- Return:
  - lock open issue row
  - compute fine
  - mark issue returned
  - restore `CIRC_STATUS = 'AVAILABLE'`

## Admin
- User Management:
  - create/deactivate librarian users
  - audit actions
- Import/Export:
  - students
  - books
  - orders, including order details
- Audit Log:
  - query by user/module/date
