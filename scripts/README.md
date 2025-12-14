# 🚀 CheermateApp Build Scripts

This directory contains scripts for generating APKs and creating releases for testers.

---

## 📋 Available Scripts

### 1. 🎯 `quick-build-apk.ps1` (Recommended)
**PowerShell script for fast local APK generation**

```powershell
# Basic build
.\scripts\quick-build-apk.ps1

# Clean build  
.\scripts\quick-build-apk.ps1 -Clean

# Release build
.\scripts\quick-build-apk.ps1 -BuildType "release"

# Build without opening folder
.\scripts\quick-build-apk.ps1 -OpenFolder:$false
```

**Features:**
- ✅ Fast local APK generation
- ✅ Automatic versioning with timestamp
- ✅ Generates installation instructions
- ✅ Creates build info JSON
- ✅ No external dependencies required

### 2. 🏷️ `generate-github-release.bat`
**Windows batch script for GitHub releases**

```bash
.\scripts\generate-github-release.bat
```

**Requirements:**
- GitHub CLI installed (`winget install GitHub.CLI`)
- Authenticated with GitHub (`gh auth login`)
- Git repository with remote origin

**Features:**
- ✅ Creates GitHub release with tag
- ✅ Uploads APK as release asset
- ✅ Generates comprehensive release notes
- ✅ Marks as pre-release for testing

### 3. 🔧 `generate-test-apk.bat`
**Simple Windows batch script**

```bash
.\scripts\generate-test-apk.bat
```

**Features:**
- ✅ Basic APK generation
- ✅ Timestamped output files
- ✅ Build information text file
- ✅ Opens output folder automatically

---

## 🤖 Automated GitHub Actions

### `.github/workflows/build-test-apk.yml`

**Automatic APK building on every push to main/develop**

**Triggers:**
- Push to `main` or `develop` branches
- Manual workflow dispatch
- Ignores documentation-only changes

**Outputs:**
- 📱 APK file as GitHub release
- 📋 Comprehensive release notes  
- 🏷️ Automatic version tagging
- 📊 Build artifacts with 30-day retention

**Benefits:**
- ✅ No local setup required
- ✅ Consistent build environment
- ✅ Automatic releases for testers
- ✅ Professional release notes

---

## 🎯 Quick Start for Testers

### Method 1: GitHub Actions (Automatic)
1. **Push code** to main/develop branch
2. **Wait 2-3 minutes** for GitHub Actions to complete
3. **Check Releases** page for new APK
4. **Share release link** with testers

### Method 2: Local PowerShell (Fast)
1. **Open PowerShell** in project root
2. **Run**: `.\scripts\quick-build-apk.ps1`
3. **APK generated** in `test-builds/` folder
4. **Share APK file** directly with testers

### Method 3: GitHub CLI (Advanced)
1. **Install GitHub CLI** if not already installed
2. **Run**: `.\scripts\generate-github-release.bat`  
3. **APK uploaded** to GitHub releases
4. **Share GitHub release link** with testers

---

## 📁 Output Structure

All scripts generate files in the `test-builds/` directory:

```
test-builds/
├── CheermateApp-v1.5-test-20241214_143022.apk
├── build-info-20241214_143022.json
├── INSTALLATION-20241214_143022.txt
└── release-notes-20241214_143022.md
```

### File Descriptions:
- **`.apk`**: The actual Android application file
- **`build-info-*.json`**: Machine-readable build metadata
- **`INSTALLATION-*.txt`**: Human-readable setup instructions  
- **`release-notes-*.md`**: Markdown formatted release notes

---

## 🔧 Configuration

### Version Numbering
```
v1.5-test-YYYYMMDD_HHMMSS
```
- **1.5**: App version (Enhanced UX & Smart Features)
- **test**: Build type identifier
- **Timestamp**: Unique build identifier

### Build Types
- **debug**: Development builds (faster, includes debugging)
- **release**: Production builds (optimized, smaller size)

### APK Naming Convention
```
CheermateApp-v1.5-test-20241214_143022.apk
```

---

## 🧪 Testing Features

### Priority Testing Areas
1. **👆 Swipe Gestures** (NEW!)
   - Swipe right to complete tasks
   - Swipe left to delete tasks
   - Confirmation dialogs

2. **🌙 Dark Mode Toggle**
   - Settings > Dark Mode
   - UI consistency check

3. **📊 Live Statistics**
   - Real-time counter updates
   - Settings statistics accuracy

4. **📱 Core Functionality**
   - Task CRUD operations
   - Search and filtering
   - Navigation and performance

---

## 🐛 Troubleshooting

### Common Issues

#### "gradlew not found"
```bash
# Make sure you're in the project root
cd C:\Users\owner\AndroidStudioProjects\CheermateApp
.\scripts\quick-build-apk.ps1
```

#### "GitHub CLI not found"
```bash
# Install GitHub CLI
winget install GitHub.CLI
# Or download from: https://cli.github.com/
```

#### "Permission denied" on PowerShell
```powershell
# Enable PowerShell script execution
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### "Build failed"
1. **Clean build**: Use `-Clean` parameter
2. **Check Java**: Ensure JDK 17+ is installed
3. **Check internet**: Gradle needs to download dependencies
4. **Check disk space**: Builds require ~500MB free space

### Script Permissions
```bash
# Make scripts executable (if needed)
chmod +x scripts/*.bat
```

---

## 📞 Support

### For Developers
- Check build logs for detailed error messages
- Ensure all dependencies are installed
- Use `gradlew clean` before rebuilding

### For Testers  
- Download APK from GitHub releases page
- Follow installation instructions in release notes
- Report issues with device info and steps to reproduce

---

## 🎉 Success Checklist

After running any script, you should have:

- [ ] ✅ APK file generated successfully
- [ ] 📋 Installation instructions created
- [ ] 📊 Build info available
- [ ] 📁 Files organized in `test-builds/` folder
- [ ] 🚀 Ready to distribute to testers

---

**Last Updated**: December 2024  
**Supports**: CheermateApp v1.5 (Enhanced UX & Smart Features)