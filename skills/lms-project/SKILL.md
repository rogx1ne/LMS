---
name: lms-project
description: Use when working on this Java Swing + Oracle Library Management System. Covers architecture, module workflows, database/setup conventions, verification expectations, and repo-specific constraints.
---

# LMS Project Skill

Use this skill when changing code, fixing bugs, adding features, or reviewing behavior in this repository.

## Quick Orientation
- Stack: Java Swing desktop app + Oracle JDBC
- Shell: `src/com/library/ui/DashboardFrame.java`
- Layers:
  - `ui`
  - `dao`
  - `service`
  - `model`

## Core Rules
- Keep operational behavior deterministic where correctness matters.
- Preserve Oracle 10g compatibility unless the project explicitly upgrades databases.
- Do not reintroduce bill-accession navigation into the transaction module without an explicit product decision.
- Passwords must remain hashed in app-managed flows.
- ID generation should remain counter-table based, not count-based.

## Read These References As Needed
- Architecture: `../../docs/architecture.md`
- Database/setup: `../../docs/database-and-operations.md`
- Module workflows: `../../docs/module-workflows.md`
- Verification: `../../docs/verification-checklist.md`

## Verification Minimum
- Run `./run.sh`
- Run full-project `javac -Xlint:deprecation` compile when applicable
- State clearly if DB-backed or GUI-backed verification could not be performed
