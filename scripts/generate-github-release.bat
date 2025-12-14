@echo off
:: ======================================
:: CheermateApp - GitHub Release Generator
:: ======================================
:: This script generates APK and creates GitHub release
:: for easy tester distribution

setlocal EnableDelayedExpansion

echo.
echo ========================================
echo   CheermateApp - GitHub Release Tool    
echo ========================================
echo.

:: Check if GitHub CLI is installed
gh --version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] GitHub CLI not found!
    echo.
    echo Please install GitHub CLI from: https://cli.github.com/
    echo Or use 'winget install GitHub.CLI'
    echo.
    pause
    exit /b 1
)

:: Set variables
set "PROJECT_ROOT=%~dp0.."
set "OUTPUT_DIR=%PROJECT_ROOT%\test-builds"
set "APK_DIR=%PROJECT_ROOT%\app\build\outputs\apk\debug"
set "TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=!TIMESTAMP: =0!"
set "VERSION=v1.5-test-%TIMESTAMP%"
set "APK_NAME=CheermateApp-%VERSION%.apk"

:: Create output directory
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo [INFO] Starting GitHub release generation...
echo [INFO] Project root: %PROJECT_ROOT%
echo [INFO] Version tag: %VERSION%
echo [INFO] APK name: %APK_NAME%
echo.

:: Clean and build
echo [STEP 1/6] Cleaning and building APK...
cd /d "%PROJECT_ROOT%"
call gradlew clean assembleDebug
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

:: Copy APK with proper naming
echo.
echo [STEP 2/6] Preparing release files...
set "RELEASE_APK=%OUTPUT_DIR%\%APK_NAME%"
copy "!SOURCE_APK!" "!RELEASE_APK!"

:: Generate release notes
set "RELEASE_NOTES=%OUTPUT_DIR%\release-notes-%TIMESTAMP%.md"
(
    echo # 🚀 CheermateApp Test Release %VERSION%
    echo.
    echo ## 📱 Installation
    echo.
    echo 1. **Download APK**: Click the APK file below to download
    echo 2. **Enable Unknown Sources**: Settings ^> Security ^> Unknown Sources
    echo 3. **Install**: Tap the downloaded APK file
    echo 4. **Allow Permissions**: Grant necessary permissions when prompted
    echo.
    echo ## ✨ New Features to Test
    echo.
    echo ### 👆 Swipe Gestures ^(NEW!^)
    echo - **Swipe Right**: Mark tasks as completed
    echo - **Swipe Left**: Delete tasks
    echo - **Confirmation dialogs** prevent accidents
    echo - **Visual feedback** with green/red backgrounds
    echo.
    echo ### 🎨 Enhanced UI
    echo - **Dark Mode**: Toggle in Settings
    echo - **Improved Statistics**: Live updates in Settings
    echo - **Better Animations**: Smoother transitions
    echo.
    echo ### 📊 Core Features
    echo - **Task Management**: Create, edit, complete tasks
    echo - **Smart Filtering**: All, Today, Pending, Done
    echo - **Real-time Search**: Find tasks instantly
    echo - **Progress Tracking**: Visual completion indicators
    echo.
    echo ## 🧪 Testing Checklist
    echo.
    echo Please test these features and report any issues:
    echo.
    echo - [ ] **Installation**: APK installs without issues
    echo - [ ] **Login/Register**: Account creation and login works
    echo - [ ] **Task Creation**: Can create tasks with all fields
    echo - [ ] **Swipe Right**: Tasks marked as completed ^(NEW!^)
    echo - [ ] **Swipe Left**: Tasks deleted with confirmation ^(NEW!^)
    echo - [ ] **Dark Mode**: Toggle works in Settings
    echo - [ ] **Statistics**: Numbers update after task changes
    echo - [ ] **Search**: Find tasks by title/description
    echo - [ ] **Filters**: All/Today/Pending/Done tabs work
    echo - [ ] **Navigation**: Task browsing with arrows
    echo - [ ] **Notifications**: Reminder system works
    echo.
    echo ## 🐛 Bug Reporting
    echo.
    echo Found an issue? Please report:
    echo.
    echo **What happened:**
    echo - Describe the problem clearly
    echo - Steps to reproduce
    echo - Expected vs actual behavior
    echo.
    echo **Device info:**
    echo - Device model: _______________
    echo - Android version: ___________
    echo - App version: %VERSION%
    echo.
    echo **Screenshots:** ^(Optional but helpful^)
    echo.
    echo ## 📋 Build Information
    echo.
    echo - **Build Date**: %date% %time%
    echo - **Version**: 1.5 ^(Enhanced UX ^& Smart Features^)
    echo - **Build Type**: Debug ^(Testing^)
    echo - **APK Size**: ~10-15 MB
    echo.
    echo ---
    echo.
    echo **Thank you for testing CheermateApp!** 🙏
    echo.
    echo Your feedback helps make the app better for everyone.
) > "!RELEASE_NOTES!"

:: Check git status
echo.
echo [STEP 3/6] Checking git status...
git status --porcelain >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Not in a git repository!
    pause
    exit /b 1
)

:: Create git tag
echo.
echo [STEP 4/6] Creating git tag...
git tag -a "%VERSION%" -m "Test release %VERSION% - Swipe gestures and enhanced UI"
if !errorlevel! neq 0 (
    echo [ERROR] Failed to create git tag!
    pause
    exit /b 1
)

:: Push tag to remote
echo.
echo [STEP 5/6] Pushing tag to GitHub...
git push origin "%VERSION%"
if !errorlevel! neq 0 (
    echo [ERROR] Failed to push tag!
    pause
    exit /b 1
)

:: Create GitHub release
echo.
echo [STEP 6/6] Creating GitHub release...
gh release create "%VERSION%" "!RELEASE_APK!" ^
    --title "🚀 CheermateApp Test Release %VERSION%" ^
    --notes-file "!RELEASE_NOTES!" ^
    --prerelease
if !errorlevel! neq 0 (
    echo [ERROR] Failed to create GitHub release!
    pause
    exit /b 1
)

:: Success message
echo.
echo ========================================
echo        GITHUB RELEASE CREATED!        
echo ========================================
echo.
echo ✅ Tag created: %VERSION%
echo ✅ APK uploaded: %APK_NAME%
echo ✅ Release notes: Generated
echo ✅ Release type: Pre-release ^(for testing^)
echo.
echo 🌐 View release: https://github.com/JTaguiamdev/CheermateApp/releases
echo 📱 Direct APK link will be available in release
echo 📋 Release notes include testing instructions
echo.
echo 📤 Share with testers:
echo 1. Send them the GitHub release link
echo 2. They can download APK directly
echo 3. Release notes have installation guide
echo.

:: Open release page
echo Opening GitHub release page...
gh release view "%VERSION%" --web

echo.
echo Press any key to exit...
pause >nul