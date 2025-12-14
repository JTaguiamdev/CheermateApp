@echo off
:: ======================================
:: CheermateApp - Test APK Generator
:: ======================================
:: This script generates APK files for testers
:: with proper versioning and documentation

setlocal EnableDelayedExpansion

echo.
echo ========================================
echo    CheermateApp - Test APK Generator    
echo ========================================
echo.

:: Set variables
set "PROJECT_ROOT=%~dp0.."
set "OUTPUT_DIR=%PROJECT_ROOT%\test-builds"
set "APK_DIR=%PROJECT_ROOT%\app\build\outputs\apk\debug"
set "TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=!TIMESTAMP: =0!"

:: Create output directory
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo [INFO] Starting APK generation...
echo [INFO] Project root: %PROJECT_ROOT%
echo [INFO] Output directory: %OUTPUT_DIR%
echo [INFO] Timestamp: %TIMESTAMP%
echo.

:: Clean previous builds
echo [STEP 1/5] Cleaning previous builds...
cd /d "%PROJECT_ROOT%"
call gradlew clean
if !errorlevel! neq 0 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

:: Build debug APK
echo.
echo [STEP 2/5] Building debug APK...
call gradlew assembleDebug
if !errorlevel! neq 0 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

:: Check if APK exists (try multiple possible names)
set "SOURCE_APK="
if exist "%APK_DIR%\app-debug.apk" set "SOURCE_APK=%APK_DIR%\app-debug.apk"
if exist "%APK_DIR%\CheermateApp-1.0-debug.apk" set "SOURCE_APK=%APK_DIR%\CheermateApp-1.0-debug.apk"

if "!SOURCE_APK!"=="" (
    echo [ERROR] APK not found! Checked:
    echo   - %APK_DIR%\app-debug.apk
    echo   - %APK_DIR%\CheermateApp-1.0-debug.apk
    pause
    exit /b 1
)

:: Copy APK to output directory with timestamp
echo.
echo [STEP 3/5] Copying APK to output directory...
set "OUTPUT_APK=%OUTPUT_DIR%\CheermateApp-test-%TIMESTAMP%.apk"
copy "!SOURCE_APK!" "!OUTPUT_APK!"
if !errorlevel! neq 0 (
    echo [ERROR] Failed to copy APK!
    pause
    exit /b 1
)

:: Generate build info
echo.
echo [STEP 4/5] Generating build information...
set "BUILD_INFO=%OUTPUT_DIR%\build-info-%TIMESTAMP%.txt"
(
    echo ========================================
    echo    CheermateApp - Test Build Info
    echo ========================================
    echo.
    echo Build Date: %date% %time%
    echo APK File: CheermateApp-test-%TIMESTAMP%.apk
    echo Build Type: Debug ^(Testing^)
    echo Version: 1.5 ^(Enhanced UX ^& Smart Features^)
    echo.
    echo ========================================
    echo    Installation Instructions
    echo ========================================
    echo.
    echo 1. Enable "Unknown Sources" in Android Settings
    echo 2. Transfer APK to Android device
    echo 3. Tap APK file to install
    echo 4. Follow installation prompts
    echo.
    echo ========================================
    echo    New Features to Test
    echo ========================================
    echo.
    echo ✅ Swipe Gestures:
    echo    - Swipe right on tasks to mark as completed
    echo    - Swipe left on tasks to delete
    echo    - Test confirmation dialogs
    echo.
    echo ✅ Dark Mode:
    echo    - Toggle in Settings
    echo    - Test UI consistency
    echo.
    echo ✅ Task Management:
    echo    - Create, edit, delete tasks
    echo    - Test filters and search
    echo    - Verify statistics updates
    echo.
    echo ========================================
    echo    Testing Checklist
    echo ========================================
    echo.
    echo □ Install APK successfully
    echo □ Login/Register functionality
    echo □ Create tasks with all fields
    echo □ Test swipe right ^(complete^)
    echo □ Test swipe left ^(delete^)
    echo □ Verify confirmation dialogs
    echo □ Test dark mode toggle
    echo □ Check statistics accuracy
    echo □ Test search and filters
    echo □ Verify task navigation
    echo □ Test reminder notifications
    echo.
    echo ========================================
    echo    Report Issues
    echo ========================================
    echo.
    echo Found bugs? Please report:
    echo - What you were doing
    echo - What happened vs expected
    echo - Device model and Android version
    echo - Screenshots if possible
    echo.
    echo Build completed: %date% %time%
    echo ========================================
) > "!BUILD_INFO!"

:: Generate QR code info (placeholder)
echo.
echo [STEP 5/5] Finalizing build package...

:: Display completion info
echo.
echo ========================================
echo           BUILD COMPLETED!            
echo ========================================
echo.
echo ✅ APK Generated: !OUTPUT_APK!
echo ✅ Build Info: !BUILD_INFO!
echo ✅ Build Size: 
for %%I in ("!OUTPUT_APK!") do echo    %%~zI bytes
echo.
echo 📱 Ready for testing!
echo 📁 Output folder: %OUTPUT_DIR%
echo.

:: Open output directory
echo Opening output directory...
start "" "%OUTPUT_DIR%"

echo.
echo Press any key to exit...
pause >nul