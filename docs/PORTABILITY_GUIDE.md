# LMS Setup Wizard - Portability Guide

## ✅ YES - You CAN Run from Different Location

**Short Answer:** Yes! Copy `LMS-Setup-2.0.jar` + `lib/` folder to any location and run.

---

## 📦 How It Works

### Current Setup
- JAR manifest specifies relative paths: `lib/ojdbc6.jar lib/poi-5.2.3.jar ...`
- This means JAR expects `lib/` folder in the **same directory**

### Portability
✅ **YES** - JAR is portable as long as `lib/` stays with it  
❌ **NO** - JAR alone won't work without `lib/` nearby

---

## 🚀 Quick Start - Run from Different Location

### Linux/macOS

```bash
# Copy everything to a new location
mkdir ~/LMS-Installer
cd ~/LMS-Installer
cp /home/abhiadi/mine/clg/LMS/LMS-Setup-2.0.jar .
cp /home/abhiadi/mine/clg/LMS/lms-setup.sh .
cp -r /home/abhiadi/mine/clg/LMS/lib .

# Run from the new location
./lms-setup.sh
```

### Windows

```batch
REM Copy everything to a new location
mkdir C:\LMS-Installer
cd C:\LMS-Installer
copy "C:\path\to\LMS\LMS-Setup-2.0.jar" .
copy "C:\path\to\LMS\lms-setup.bat" .
xcopy "C:\path\to\LMS\lib" lib\ /E

REM Run from the new location
lms-setup.bat
```

---

## 📂 Required Directory Structure

For the setup to work from ANY location:

```
YourFolder/
├── LMS-Setup-2.0.jar          ✓ Required
├── lms-setup.sh                ✓ Required (Linux/macOS)
├── lms-setup.bat               ✓ Required (Windows)
├── lib/                        ✓ CRITICAL - Must be here!
│   ├── ojdbc6.jar
│   ├── poi-5.2.3.jar
│   ├── itextpdf.jar
│   ├── commons-io-2.16.1.jar
│   ├── commons-compress-1.26.1.jar
│   ├── commons-collections4-4.4.jar
│   ├── curvesapi-1.08.jar
│   ├── log4j-api-2.18.0.jar
│   ├── log4j-core-2.18.0.jar
│   ├── poi-ooxml-5.2.3.jar
│   ├── activation-1.1.1.jar
│   └── javax.mail-1.6.2.jar
├── SETUP_QUICK_START.md        ✓ Recommended (documentation)
└── WINDOWS_COMPATIBILITY.md    ✓ Recommended (documentation)
```

**Key Rule:** `lib/` folder MUST be in same directory as JAR

---

## 💡 Three Distribution Options

### Option 1: Simple Copy (EASIEST) ✅

Just copy `LMS-Setup-2.0.jar` + `lib/` to destination:

```bash
cp -r ~/LMS-Setup-Distribution/* /path/to/deployment/location/
cd /path/to/deployment/location
./lms-setup.sh
```

**Pros:** Simple, works everywhere  
**Cons:** Large file size (~70 MB for lib/)

---

### Option 2: ZIP Distribution (RECOMMENDED FOR END-USERS) ✅

Create a portable ZIP file:

```bash
cd ~
zip -r LMS-Setup-2.0-complete.zip LMS-Setup-Distribution/
```

End-users extract and run:
```bash
unzip LMS-Setup-2.0-complete.zip
cd LMS-Setup-Distribution
./lms-setup.sh  # or lms-setup.bat on Windows
```

**Pros:** Professional, easy to distribute  
**Cons:** Larger download size

---

### Option 3: Fat JAR (ADVANCED) ⚡

Create single JAR with all dependencies embedded:

```bash
# Would require Maven with shade plugin
mvn package -Dshade
# Produces: LMS-Setup-2.0-fat.jar (~100 MB, no lib/ folder needed)
```

Then run from anywhere:
```bash
java -jar LMS-Setup-2.0-fat.jar
```

**Pros:** Most portable, single file  
**Cons:** Larger file, requires build step

---

## 📋 Pre-Made Distribution Package

I've created a ready-to-use distribution at:

```
~/LMS-Setup-Distribution/
├── LMS-Setup-2.0.jar
├── lms-setup.sh
├── lms-setup.bat
├── lib/                          (all dependencies)
├── SETUP_QUICK_START.md
├── WINDOWS_COMPATIBILITY.md
└── PRODUCTION_SETUP_GUIDE.md
```

**You can use this as-is from any location!**

### Usage

```bash
# From any directory
cd ~/LMS-Setup-Distribution
./lms-setup.sh

# Or move it anywhere
cp -r ~/LMS-Setup-Distribution ~/Desktop/LMS-Install
cd ~/Desktop/LMS-Install
./lms-setup.sh
```

---

## ⚠️ Important Rules

### ✅ DO THIS

```bash
# ✓ Keep lib/ folder with JAR
mkdir ~/MyFolder
cp LMS-Setup-2.0.jar ~/MyFolder/
cp -r lib ~/MyFolder/
cd ~/MyFolder
./lms-setup.sh
```

### ❌ DON'T DO THIS

```bash
# ✗ Run JAR without lib/ nearby - WILL FAIL
java -jar LMS-Setup-2.0.jar

# ✗ Move JAR but leave lib/ behind - WILL FAIL
mv LMS-Setup-2.0.jar ~/Desktop/
cd ~/Desktop
./lms-setup.sh

# ✗ JAR in one place, lib/ somewhere else - WILL FAIL
cp LMS-Setup-2.0.jar ~/Desktop/
cp -r lib ~/Downloads/
cd ~/Desktop
./lms-setup.sh  # Can't find lib/, fails
```

---

## 🧪 Testing Portability

Test from different locations:

### Test 1: From Home Directory
```bash
cd ~
~/LMS-Setup-Distribution/lms-setup.sh
```

### Test 2: From Desktop
```bash
cp -r ~/LMS-Setup-Distribution ~/Desktop/
cd ~/Desktop/LMS-Setup-Distribution
./lms-setup.sh
```

### Test 3: From USB Drive (Simulate Distribution)
```bash
cp -r ~/LMS-Setup-Distribution /media/usb/
cd /media/usb/LMS-Setup-Distribution
./lms-setup.sh
```

---

## 📊 Portability Summary

| Scenario | Portable? | Notes |
|----------|-----------|-------|
| JAR + lib/ in same folder | ✅ YES | Works from any location |
| JAR alone | ❌ NO | Needs lib/ nearby |
| JAR in one place, lib/ elsewhere | ❌ NO | Classpath won't resolve |
| ZIP with JAR + lib/ | ✅ YES | Extract and run |
| USB drive with JAR + lib/ | ✅ YES | Just copy and run |

---

## 🎯 For End-User Distribution

**Recommended Approach:**

1. **Create ZIP file:**
   ```bash
   zip -r LMS-Setup-2.0-complete.zip LMS-Setup-Distribution/
   ```

2. **Include README:**
   ```
   LMS-Setup-2.0-complete.zip
   ├── README.txt (extraction instructions)
   └── LMS-Setup-Distribution/
       ├── LMS-Setup-2.0.jar
       ├── lms-setup.sh / lms-setup.bat
       ├── lib/ (all dependencies)
       └── documentation (MD files)
   ```

3. **User Instructions:**
   ```
   1. Download LMS-Setup-2.0-complete.zip
   2. Extract to desired location
   3. Linux/macOS: ./lms-setup.sh
      Windows: lms-setup.bat
   4. Follow wizard
   ```

---

## 💾 Space Requirements

When copying setup to new location:

| Component | Size |
|-----------|------|
| LMS-Setup-2.0.jar | 394 KB |
| lib/ folder | ~70 MB |
| Documentation | ~30 KB |
| **Total** | **~70 MB** |

Installation adds: ~100-200 MB (depending on database size)

---

## ✨ Conclusion

✅ **YES - The setup wizard is fully portable!**

- Copy `LMS-Setup-2.0.jar` + `lib/` to any location
- Run from Windows, Linux, or macOS
- No dependencies on project repository
- Can be distributed as ZIP to end-users

**Pre-made distribution package ready at:** `~/LMS-Setup-Distribution/`

---

## 📞 Troubleshooting

**Q: JAR runs but can't find JDBC driver**  
A: Ensure `lib/ojdbc6.jar` is in the same directory as JAR

**Q: Different location but lib/ is missing**  
A: Copy `lib/` folder along with JAR: `cp -r lib /your/destination/`

**Q: Can I delete lib/ after installation?**  
A: Only for the LMS runtime (run.sh/run.bat). Keep it for future setup wizard runs.

**Q: Can I move JAR after creation?**  
A: Only if you move `lib/` folder with it. Keep them together always.

---

**Version:** 2.0.1  
**Status:** ✅ PORTABLE & READY FOR DISTRIBUTION
