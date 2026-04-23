# Interactive One-Click Setup Guide

## Problem Solved ✅

**Before**: Had to manually edit `.env.setup` file with credentials
**Now**: Single-click setup that prompts for credentials interactively!

---

## Two Setup Options

### Option 1: Interactive Setup (Recommended for Most Users)

**Advantages**:
- ✅ Single click - no file editing needed
- ✅ Prompts for credentials step-by-step
- ✅ Validates input as you go
- ✅ Auto-generates .env.setup
- ✅ Cleaner user experience

**How to Use**:

#### Linux/Mac:
```bash
chmod +x interactive-setup.sh
./interactive-setup.sh
```

Then answer the prompts:
```
→ Oracle database URL [jdbc:oracle:thin:@localhost:1521:xe]: 
→ Oracle SYSTEM username [system]: 
→ Oracle SYSTEM password [oracle]: 
```

#### Windows:
```batch
interactive-setup.bat
```

Then answer the prompts when asked.

**What it does**:
1. Checks Java is installed
2. Asks for database URL
3. Asks for SYSTEM username
4. Asks for SYSTEM password
5. Creates .env.setup automatically
6. Compiles and runs setup wizard
7. Shows success/error message


### Option 2: Manual Configuration Setup (For Advanced Users)

**How to Use**:

```bash
# 1. Copy template
cp .env.setup.example .env.setup

# 2. Edit it
nano .env.setup

# 3. Run setup
./setup-wizard.sh
```

---

## Comparison

| Feature | Interactive | Manual |
|---------|-------------|--------|
| Edit files? | No | Yes |
| Prompts? | Yes | No |
| One command? | Yes | No |
| For CI/CD? | No | Yes |
| Learning curve? | Easy | Minimal |
| Speed | Fast | Medium |

---

## New Files Created

### `interactive-setup.sh` (Linux/Mac)
- Prompts for credentials in terminal
- Creates .env.setup automatically
- One single command to run

### `interactive-setup.bat` (Windows)
- Prompts for credentials in Command Prompt
- Creates .env.setup automatically
- One double-click to run

---

## Which One Should I Use?

### Use **Interactive Setup** if:
- ✅ You want the easiest experience
- ✅ You prefer prompts over file editing
- ✅ You're setting up on a single machine
- ✅ You want a "one-click" solution

### Use **Manual Setup** if:
- ✅ You need to automate setup (CI/CD)
- ✅ You want to version control credentials
- ✅ You prefer editing files
- ✅ You're setting up many machines

---

## Under the Hood

Both setups do the same thing:

1. Collect Oracle SYSTEM credentials
2. Create `.env.setup` configuration file
3. Load credentials into environment variables
4. Run setup wizard with credentials
5. Setup wizard connects to Oracle and installs LMS

The difference:
- **Interactive**: Collects credentials via prompts
- **Manual**: Requires editing .env.setup file

---

## First Time Setup Example

### Interactive Way (Recommended):
```bash
$ ./interactive-setup.sh

╔════════════════════════════════════════╗
║  📚 LMS Setup Wizard - Interactive Mode 📚 ║
╚════════════════════════════════════════╝

✓ Java found: 11.0.15

→ Oracle database URL [jdbc:oracle:thin:@localhost:1521:xe]: 
→ Oracle SYSTEM username [system]: 
→ Oracle SYSTEM password [oracle]: your_password

✓ Configuration file created: .env.setup

Starting Setup Wizard...

[GUI Setup Wizard opens...]
```

### Manual Way:
```bash
$ cp .env.setup.example .env.setup
$ nano .env.setup
$ ./setup-wizard.sh
```

---

## Troubleshooting

### "Could not connect as SYSTEM"

1. Verify Oracle is running
2. Check credentials are correct
3. Verify database URL (host:port:SID)

### "Java not found"

Install Java 8 or higher from oracle.com

### ".env.setup already exists"

The previous setup created it. Delete it if you want to re-setup:
```bash
rm .env.setup
./interactive-setup.sh
```

---

## After Setup

Once setup completes successfully:

```bash
./run.sh          # Linux/Mac
# or
run.bat           # Windows
```

Your LMS application should start! 🎉

---

## For Different Machines

Each machine runs the interactive setup separately:

**Machine 1**: `./interactive-setup.sh`
- Enters: system/password1

**Machine 2**: `./interactive-setup.sh`
- Enters: system/password2

**Machine 3**: `./interactive-setup.sh`
- Enters: system/password3

Each gets their own .env.setup with different credentials! ✅

---

## Security

✅ `.env.setup` created locally (not shared)
✅ Credentials not stored in code
✅ .env.setup in .gitignore (not committed)
✅ Each machine has its own credentials

---

## What's New

```
LMS/
├── interactive-setup.sh      [NEW] One-click setup (Linux/Mac)
├── interactive-setup.bat     [NEW] One-click setup (Windows)
├── setup-wizard.sh           [UPDATED] Manual setup still works
├── lms-setup-env.bat         [EXISTING] Manual Windows setup
├── .env.setup.example        [EXISTING] Config template
├── QUICK_FIX_ORACLE.md       [EXISTING] Quick reference
└── ORACLE_CREDENTIALS_SETUP.md [EXISTING] Full guide
```

---

## Next Steps

1. Choose your setup method (interactive or manual)
2. Run the setup
3. Follow prompts or edit .env.setup
4. Setup wizard runs automatically
5. LMS is installed!

**Recommended**: Use `interactive-setup.sh` or `interactive-setup.bat` for easiest experience! 🚀
