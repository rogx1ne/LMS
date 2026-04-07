#!/bin/bash
# Package LMS Setup Wizard as executable JAR

echo "=========================================="
echo "LMS Setup Wizard Packager"
echo "=========================================="

# Step 1: Ensure everything is compiled
echo ""
echo "[1/4] Compiling all sources (Java 8 target for max compatibility)..."
javac --release 8 -d bin -cp "lib/*" src/com/library/**/*.java
if [ $? -ne 0 ]; then
    echo "✗ Compilation failed"
    exit 1
fi
echo "✓ Compilation successful (Java 8 compatible)"

# Step 2: Create temporary directory for JAR contents
echo ""
echo "[2/4] Preparing JAR contents..."
TEMP_DIR="temp-setup-jar"
rm -rf $TEMP_DIR
mkdir -p $TEMP_DIR

# Copy compiled setup classes with proper package structure
mkdir -p $TEMP_DIR/com/library
cp -r bin/com/library/setup $TEMP_DIR/com/library/
cp -r bin/com/library/service $TEMP_DIR/com/library/ # For PasswordHasher
cp -r bin/com/library/model $TEMP_DIR/com/library/    # For models used by setup

# Copy source code (needed for compilation during installation)
cp -r src $TEMP_DIR/

# Copy libraries
cp -r lib $TEMP_DIR/

# Copy SQL scripts
cp script.sql $TEMP_DIR/

# Copy documentation
cp -r docs $TEMP_DIR/ 2>/dev/null || echo "No docs directory"

# Copy README
cp README.md $TEMP_DIR/ 2>/dev/null || echo "No README.md"

echo "✓ Contents prepared"

# Step 3: Extract library JARs into temp directory (fat JAR approach)
echo ""
echo "[3/4] Creating fat JAR (extracting dependencies)..."
cd $TEMP_DIR
for jar in lib/*.jar; do
    if [ -f "$jar" ]; then
        echo "  Extracting $jar..."
        jar xf "$jar"
        # Remove signature files to avoid conflicts
        rm -rf META-INF/*.SF META-INF/*.DSA META-INF/*.RSA
    fi
done
cd ..

echo "✓ Dependencies extracted"

# Step 4: Create the final JAR
echo ""
echo "[4/4] Building LMS-Setup.jar..."

# Create JAR with manifest
jar cfm LMS-Setup.jar MANIFEST-SETUP.MF -C $TEMP_DIR .

if [ $? -eq 0 ]; then
    echo "✓ JAR created successfully"
    
    # Make it executable on Linux
    chmod +x LMS-Setup.jar
    
    # Get file size
    SIZE=$(du -h LMS-Setup.jar | cut -f1)
    
    echo ""
    echo "=========================================="
    echo "✓✓✓ Package Complete! ✓✓✓"
    echo "=========================================="
    echo ""
    echo "Output: LMS-Setup.jar ($SIZE)"
    echo ""
    echo "To run the installer:"
    echo "  Linux:   java -jar LMS-Setup.jar"
    echo "  Windows: java -jar LMS-Setup.jar"
    echo "  Or just: ./LMS-Setup.jar (Linux)"
    echo ""
else
    echo "✗ JAR creation failed"
    exit 1
fi

# Cleanup
echo "Cleaning up temporary files..."
rm -rf $TEMP_DIR
echo "✓ Done"
