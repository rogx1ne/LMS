@echo off
REM Package LMS Setup Wizard as executable JAR

echo ==========================================
echo LMS Setup Wizard Packager
echo ==========================================

REM Step 1: Ensure everything is compiled
echo.
echo [1/4] Compiling all sources...
javac -d bin -cp "lib/*" src/com/library/setup/*.java src/com/library/service/PasswordHasher.java
if %errorlevel% neq 0 (
    echo X Compilation failed
    exit /b 1
)
echo + Compilation successful

REM Step 2: Create temporary directory for JAR contents
echo.
echo [2/4] Preparing JAR contents...
set TEMP_DIR=temp-setup-jar
if exist %TEMP_DIR% rmdir /s /q %TEMP_DIR%
mkdir %TEMP_DIR%

REM Copy compiled classes
xcopy /E /I bin\com\library\setup %TEMP_DIR%\com\library\setup
xcopy /E /I bin\com\library\service %TEMP_DIR%\com\library\service
xcopy /E /I bin\com\library\model %TEMP_DIR%\com\library\model

REM Copy source code
xcopy /E /I src %TEMP_DIR%\src

REM Copy libraries
xcopy /E /I lib %TEMP_DIR%\lib

REM Copy SQL scripts
copy script.sql %TEMP_DIR%\
copy dummy.sql %TEMP_DIR%\

REM Copy docs if exists
if exist docs xcopy /E /I docs %TEMP_DIR%\docs

REM Copy README if exists
if exist README.md copy README.md %TEMP_DIR%\

echo + Contents prepared

REM Step 3: Extract library JARs (fat JAR)
echo.
echo [3/4] Creating fat JAR (extracting dependencies)...
cd %TEMP_DIR%
for %%j in (lib\*.jar) do (
    echo   Extracting %%j...
    jar xf %%j
    REM Remove signature files
    if exist META-INF\*.SF del /q META-INF\*.SF
    if exist META-INF\*.DSA del /q META-INF\*.DSA
    if exist META-INF\*.RSA del /q META-INF\*.RSA
)
cd ..

echo + Dependencies extracted

REM Step 4: Create the final JAR
echo.
echo [4/4] Building LMS-Setup.jar...

jar cfm LMS-Setup.jar MANIFEST-SETUP.MF -C %TEMP_DIR% .

if %errorlevel% equ 0 (
    echo + JAR created successfully
    
    echo.
    echo ==========================================
    echo +++ Package Complete! +++
    echo ==========================================
    echo.
    echo Output: LMS-Setup.jar
    echo.
    echo To run the installer:
    echo   java -jar LMS-Setup.jar
    echo   Or double-click LMS-Setup.jar
    echo.
) else (
    echo X JAR creation failed
    exit /b 1
)

REM Cleanup
echo Cleaning up temporary files...
rmdir /s /q %TEMP_DIR%
echo + Done
