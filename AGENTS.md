# LMS Agent Guide

## Scope
- This repository is a Java Swing desktop Library Management System backed by Oracle via JDBC.
- Keep changes constrained to LMS functionality. Do not add unrelated platform features.
- Prefer deterministic behavior for operational features such as circulation, reporting, and imports.

## Architecture
- Entry point: `src/Main.java`
- UI shell: `src/com/library/ui/DashboardFrame.java`
- Layers:
  - `ui`: Swing screens, dialogs, controllers
  - `dao`: JDBC persistence and transactions
  - `service`: validation, business rules, utilities
  - `model`: data carriers

## Current Product Shape
- Modules:
  - Login
  - Book
  - Transaction / Procurement
  - Student
  - Circulation
  - Admin
- Transaction module no longer includes the bill-accession flow. Keep it limited to seller and order workflows unless explicitly redesigned.

## Database Rules
- Primary schema script: `script.sql`
- Demo dataset: `dummy.sql`
- Oracle XE 10g compatibility matters.
- Avoid SQL features newer than Oracle 10g unless the migration target is explicitly changed.
- Current runtime DB config can come from:
  - `LMS_DB_URL`
  - `LMS_DB_USER`
  - `LMS_DB_PASSWORD`

## Security Expectations
- Do not introduce plain-text password storage.
- Use the existing password hashing flow in `PasswordHasher`.
- Keep write actions role-aware and auditable.

## Change Strategy
- Prefer targeted fixes over broad rewrites.
- Preserve existing Swing visual language unless the task is specifically UI redesign.
- If schema and code diverge, fix both together.
- When adding documentation, keep `AGENTS.md` concise and move detail into `docs/`.

## Verification
- Minimum verification for code changes:
  - `./run.sh` must compile successfully
  - `javac -Xlint:deprecation` full-project compile should pass when relevant
- In this environment, GUI runtime cannot be fully verified because there is no X11 display.
- If DB-dependent behavior changes, note whether Oracle-side verification was or was not performed.

## Reference Docs
- `docs/architecture.md`
- `docs/database-and-operations.md`
- `docs/module-workflows.md`
- `docs/verification-checklist.md`
