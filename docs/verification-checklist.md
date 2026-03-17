# Verification Checklist

## Source-Level Verification
- Run `./run.sh`
- Confirm compilation succeeds
- Run `javac -Xlint:deprecation` full-project compile when relevant

## Database Verification
- Confirm `script.sql` runs without object conflicts
- Confirm `dummy.sql` loads demo data if needed
- Verify seed logins work

## Functional Smoke Checks
- Login as `ADMIN`
- Open each module from dashboard
- Book:
  - add/view stock
- Transaction:
  - seller
  - add order
  - view order
- Student:
  - register or view active students
- Circulation:
  - issue and return flow
- Admin:
  - user management
  - import/export
  - audit log

## Environment Limits
- In headless terminals without X11, GUI launch cannot be fully tested.
- State clearly whether runtime GUI and Oracle-backed checks were actually executed.
