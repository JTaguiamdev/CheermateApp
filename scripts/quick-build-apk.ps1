# ======================================
# CheermateApp - Quick APK Builder
# ======================================
# PowerShell script for fast local APK generation

param(
    [string]$BuildType = "debug",
    [switch]$Clean = $false,
    [switch]$OpenFolder = $true
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CheermateApp - Quick APK Builder    " -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set variables
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$OutputDir = Join-Path $ProjectRoot "test-builds"
$ApkDir = Join-Path $ProjectRoot "app\build\outputs\apk\$BuildType"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$Version = "v1.5-test-$Timestamp"

# Create output directory
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

Write-Host "[INFO] Build Configuration:" -ForegroundColor Green
Write-Host "  Project: $ProjectRoot" -ForegroundColor Gray
Write-Host "  Build Type: $BuildType" -ForegroundColor Gray
Write-Host "  Version: $Version" -ForegroundColor Gray
Write-Host "  Output: $OutputDir" -ForegroundColor Gray
Write-Host ""

try {
    # Change to project directory
    Set-Location $ProjectRoot

    # Clean build if requested
    if ($Clean) {
        Write-Host "[STEP 1/3] 🧹 Cleaning previous build..." -ForegroundColor Yellow
        & .\gradlew.bat clean
        if ($LASTEXITCODE -ne 0) {
            throw "Clean failed with exit code $LASTEXITCODE"
        }
    }

    # Build APK
    Write-Host "[STEP 2/3] 🔨 Building $BuildType APK..." -ForegroundColor Yellow
    & .\gradlew.bat "assemble$($BuildType.Substring(0,1).ToUpper())$($BuildType.Substring(1))"
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed with exit code $LASTEXITCODE"
    }

    # Find and copy APK (try multiple naming patterns)
    $PossibleApkNames = @("app-$BuildType.apk", "CheermateApp-1.0-$BuildType.apk")
    $SourceApk = $null
    foreach ($apkName in $PossibleApkNames) {
        $testPath = Join-Path $ApkDir $apkName
        if (Test-Path $testPath) {
            $SourceApk = $testPath
            break
        }
    }
    if ($SourceApk -eq $null -or !(Test-Path $SourceApk)) {
        throw "APK not found! Checked locations:`n" + ($PossibleApkNames | ForEach-Object { "  - $(Join-Path $ApkDir $_)" } | Out-String)
    }

    Write-Host "[STEP 3/3] 📦 Packaging release..." -ForegroundColor Yellow
    
    # Copy APK with version name
    $TargetApk = Join-Path $OutputDir "CheermateApp-$Version.apk"
    Copy-Item $SourceApk $TargetApk
    
    # Get APK size
    $ApkSize = [math]::Round((Get-Item $TargetApk).Length / 1MB, 2)
    
    # Generate build info
    $BuildInfo = Join-Path $OutputDir "build-info-$Timestamp.json"
    $BuildData = @{
        version = $Version
        buildDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss UTC"
        buildType = $BuildType
        apkFile = Split-Path $TargetApk -Leaf
        apkSizeMB = $ApkSize
        commit = if (Get-Command git -ErrorAction SilentlyContinue) { 
            (git rev-parse --short HEAD 2>$null) 
        } else { 
            "unknown" 
        }
        features = @(
            "Swipe Gestures (NEW!)",
            "Dark Mode Toggle", 
            "Live Statistics",
            "Task Management",
            "Smart Filtering",
            "Real-time Search"
        )
        testingPriority = @(
            "Test swipe right to complete tasks",
            "Test swipe left to delete tasks", 
            "Verify confirmation dialogs",
            "Test dark mode toggle",
            "Check statistics live updates"
        )
    }
    
    $BuildData | ConvertTo-Json -Depth 3 | Out-File $BuildInfo -Encoding UTF8

    # Generate readable instructions
    $Instructions = Join-Path $OutputDir "INSTALLATION-$Timestamp.txt"
    @"
========================================
    CheermateApp Test Installation
========================================

📱 APK File: $(Split-Path $TargetApk -Leaf)
📦 Size: $ApkSize MB
🏷️ Version: $Version
📅 Built: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

========================================
    Installation Steps
========================================

1. 📲 TRANSFER APK TO DEVICE
   - Email to yourself, or
   - Use USB cable, or  
   - Upload to cloud storage

2. ⚙️ ENABLE UNKNOWN SOURCES
   - Settings > Security > Unknown Sources (Android 7-)
   - Settings > Apps & Notifications > Special Access > Install Unknown Apps (Android 8+)

3. 📱 INSTALL APK  
   - Tap the APK file
   - Follow installation prompts
   - Grant permissions when asked

========================================
    New Features to Test
========================================

👆 SWIPE GESTURES (NEW!)
   ✅ Swipe RIGHT on tasks → Mark as completed
   🗑️ Swipe LEFT on tasks → Delete task
   ⚠️ Confirmation dialogs prevent accidents

🌙 DARK MODE
   🔄 Toggle in Settings
   🎨 Test UI consistency

📊 LIVE STATISTICS  
   📈 Check Settings for real-time counts
   🔄 Verify updates after task changes

========================================
    Testing Checklist
========================================

□ App installs successfully
□ Login/Register works
□ Can create new tasks
□ Swipe right completes tasks ⭐ NEW
□ Swipe left deletes tasks ⭐ NEW  
□ Confirmation dialogs appear ⭐ NEW
□ Dark mode toggle works
□ Statistics update live
□ Search finds tasks
□ Filters work (All/Today/Pending/Done)

========================================
    Report Issues
========================================

🐛 Found a bug? Please note:
   - What you were doing
   - What happened vs expected
   - Device model & Android version
   - Steps to reproduce

Built with ❤️ for testing
"@ | Out-File $Instructions -Encoding UTF8

    # Success message
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "        ✅ BUILD COMPLETED!              " -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📱 APK File: " -NoNewline -ForegroundColor White
    Write-Host "$(Split-Path $TargetApk -Leaf)" -ForegroundColor Cyan
    Write-Host "📦 Size: " -NoNewline -ForegroundColor White  
    Write-Host "$ApkSize MB" -ForegroundColor Cyan
    Write-Host "🏷️ Version: " -NoNewline -ForegroundColor White
    Write-Host "$Version" -ForegroundColor Cyan
    Write-Host "📁 Location: " -NoNewline -ForegroundColor White
    Write-Host "$OutputDir" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🎯 Files generated:" -ForegroundColor Yellow
    Write-Host "  • $(Split-Path $TargetApk -Leaf)" -ForegroundColor Gray
    Write-Host "  • $(Split-Path $BuildInfo -Leaf)" -ForegroundColor Gray  
    Write-Host "  • $(Split-Path $Instructions -Leaf)" -ForegroundColor Gray
    Write-Host ""

    if ($OpenFolder) {
        Write-Host "📂 Opening output folder..." -ForegroundColor Green
        Invoke-Item $OutputDir
    }

    Write-Host "🚀 Ready for testing!" -ForegroundColor Green
    Write-Host ""

} catch {
    Write-Host ""
    Write-Host "❌ BUILD FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

Write-Host "Press any key to continue..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")