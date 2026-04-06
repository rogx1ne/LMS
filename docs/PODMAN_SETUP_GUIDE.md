# LMS Setup with Podman Oracle - Complete Guide

## ✅ Setup Complete!

The Library Management System is now fully configured and running with your Podman Oracle database.

### Current Configuration
- **Java Version**: 26.0.2 (compatible with Java 8+ target)
- **Database**: Oracle 10g XE (running in Podman container `oracle10g`)
- **JDBC Connection**: `jdbc:oracle:thin:@localhost:1521:xe`
- **DB User**: `PRJ2531H` (owns all LMS tables)
- **DB Password**: `PRJ2531H`

### Database Information
- **Container**: `oracle10g` (status: running)
- **Port**: 1521 (mapped to host)
- **SID**: `xe`
- **Admin User**: `system/oracle` (used for schema initialization only)

## 🚀 How to Run the LMS

### Option 1: Quick Start (Recommended)
```bash
cd /home/abhiadi/mine/clg/LMS
./run-with-env.sh
```
This script:
- Sets all environment variables automatically
- Checks that the Podman Oracle container is running
- Compiles and launches the application

### Option 2: Manual Run with Environment Variables
```bash
export LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"
cd /home/abhiadi/mine/clg/LMS
./run.sh
```

### Option 3: Use .env File (if configured)
```bash
# Copy the example to .env
cp /home/abhiadi/mine/clg/LMS/.env.example /home/abhiadi/mine/clg/LMS/.env

# The run.sh script will automatically source it
cd /home/abhiadi/mine/clg/LMS
./run.sh
```

## 🛠️ Podman Oracle Management

### Start Oracle Container
```bash
podman start oracle10g
```

### Stop Oracle Container
```bash
podman stop oracle10g
```

### Check Oracle Status
```bash
podman ps | grep oracle10g
```

### Access Oracle CLI (sqlplus)
```bash
podman exec -it oracle10g sqlplus system/oracle@xe
```

### Run SQL Script Inside Container
```bash
podman exec oracle10g sqlplus PRJ2531H/PRJ2531H@xe < script.sql
```

## 📊 Database Initialization

The schema has been initialized with:
- ✓ User account: `PRJ2531H` (created and granted privileges)
- ✓ Core tables: TBL_CREDENTIALS, TBL_BOOK_CATALOG, TBL_STUDENT, etc.
- ✓ Audit logging tables and triggers
- ✓ Sample data: Demo users, books, sellers, orders, etc.

### Default Demo Users (from dummy.sql)
| User ID | Password | Role |
|---------|----------|------|
| ADMIN | ADMIN | ADMIN |
| LIB01 | LIB01 | LIBRARIAN |
| LIB02 | LIB02 | LIBRARIAN |
| USR01 | USR01 | LIBRARIAN |

## ⚡ Troubleshooting

### "Connection Failed! Check URL, User, or Password"
1. Verify Oracle is running: `podman ps | grep oracle10g`
2. Verify credentials: `export LMS_DB_USER="PRJ2531H" LMS_DB_PASSWORD="PRJ2531H"`
3. Test connection from container:
   ```bash
   podman exec oracle10g sqlplus PRJ2531H/PRJ2531H@xe "SELECT COUNT(*) FROM TBL_CREDENTIALS;"
   ```

### "Table or View does not exist" 
- **Cause**: Connected to wrong user account
- **Fix**: Ensure `LMS_DB_USER=PRJ2531H` (not `system`)
- Reinitialize if needed: `podman cp script.sql oracle10g:/ && podman exec oracle10g sqlplus system/oracle@xe @/script.sql`

### "Oracle JDBC driver not found"
- Verify `lib/ojdbc6.jar` exists
- Ensure `./run.sh` includes `-cp "$LIB_DIR/*"`

## 📝 Environment Variables (for reference)

These are automatically set by `run-with-env.sh` or `./run.sh` with `.env`:

```bash
LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
LMS_DB_USER="PRJ2531H"
LMS_DB_PASSWORD="PRJ2531H"
```

The `DBConnection.java` class reads these via `System.getenv()`.

## 🔒 Security Notes

- Passwords are hashed (SHA-256) in the TBL_CREDENTIALS table
- Demo credentials above are for development/testing only
- Never commit credentials to git; use environment variables instead
- Audit logs track all sensitive operations

## 📂 Key Files

- `/home/abhiadi/mine/clg/LMS/run.sh` - Main build & run script
- `/home/abhiadi/mine/clg/LMS/run-with-env.sh` - Convenience wrapper with env vars
- `/home/abhiadi/mine/clg/LMS/script.sql` - Schema initialization
- `/home/abhiadi/mine/clg/LMS/dummy.sql` - Sample data
- `/home/abhiadi/mine/clg/LMS/src/com/library/database/DBConnection.java` - Connection config

## ✨ What's Next?

The LMS is ready for:
- Development and testing
- Adding new features
- Running against real Oracle database (update env vars)
- Deployment to production

Happy coding! 🎉
