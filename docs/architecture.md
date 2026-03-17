# LMS Architecture

## Runtime Flow
- `Main.java` installs UI behavior and launches `LoginFrame`.
- `LoginFrame` authenticates through `UserDAO`, populates `CurrentUserContext`, and opens `DashboardFrame`.
- `DashboardFrame` is the application shell. It uses `CardLayout` to host each module panel and wires the controllers.

## Layers
- `ui`
  - Swing panels, dialogs, shell navigation, controller wiring
- `dao`
  - JDBC queries, transactions, schema availability checks
- `service`
  - validation logic, ID generation, hashing, PDF/email helpers, support utilities
- `model`
  - immutable or DTO-style data objects

## Key Design Principles
- Keep business validation in `service`, not inline in UI handlers when avoidable.
- Keep DB writes transactional in `dao`.
- Let UI coordinate workflows, not own persistence logic.
- Prefer deterministic reporting and operational behavior for LMS workflows.

## High-Risk Areas
- Authentication and password reset
- Schema compatibility with Oracle 10g
- Import/export round-tripping
- ID generation and concurrency
- Circulation state transitions between `TBL_ISSUE` and `TBL_BOOK_INFORMATION`
