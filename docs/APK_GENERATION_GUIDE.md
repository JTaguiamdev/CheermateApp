# 📱 APK Generation & Distribution Guide

Complete guide for generating and distributing CheermateApp APKs to testers through GitHub.

---

## 🎯 Overview

The CheermateApp project includes multiple methods for generating test APKs and distributing them via GitHub:

1. **🤖 Automated GitHub Actions** - Builds on every push
2. **🏷️ GitHub Releases** - Manual release creation with GitHub CLI
3. **⚡ Quick Local Builds** - Fast PowerShell/Batch scripts

All methods generate APKs that are automatically uploaded to your GitHub repository's releases page for easy tester access.

---

## 🚀 Method 1: Automated GitHub Actions (Recommended)

### Setup (One-time)
The GitHub Actions workflow is already configured in `.github/workflows/build-test-apk.yml`.

### Usage
1. **Push code** to `main` or `develop` branch
2. **GitHub Actions automatically:**
   - Builds APK
   - Creates release with timestamp tag
   - Uploads APK as release asset
   - Generates comprehensive release notes
   - Marks as pre-release for testing

### Benefits
- ✅ **Zero setup** - works automatically  
- ✅ **Consistent builds** - same environment every time
- ✅ **Professional releases** - formatted release notes
- ✅ **No local dependencies** - runs in GitHub cloud

### Viewing Results
1. Go to your repository on GitHub
2. Click **"Actions"** tab to see build progress
3. Click **"Releases"** to see generated APKs
4. Share release URL with testers

---

## 🏷️ Method 2: Manual GitHub Releases

### Setup
```bash
# Install GitHub CLI
winget install GitHub.CLI

# Authenticate
gh auth login
```

### Usage
```bash
# Run the release script
.\scripts\generate-github-release.bat
```

### What it does:
1. Builds APK locally
2. Creates Git tag with timestamp  
3. Pushes tag to GitHub
4. Creates GitHub release
5. Uploads APK as asset
6. Generates release notes

### Benefits  
- ✅ **Manual control** - create releases on demand
- ✅ **Custom messages** - add specific release notes
- ✅ **Immediate upload** - APK goes straight to GitHub

---

## ⚡ Method 3: Quick Local Builds

### PowerShell Script (Recommended)
```powershell
# Basic build
.\scripts\quick-build-apk.ps1

# Clean build
.\scripts\quick-build-apk.ps1 -Clean

# Release build (optimized)
.\scripts\quick-build-apk.ps1 -BuildType "release"
```

### Batch Script (Alternative)
```batch
.\scripts\generate-test-apk.bat
```

### Benefits
- ✅ **Fastest method** - builds locally in 2-3 minutes
- ✅ **No GitHub required** - works offline
- ✅ **Full control** - customize build parameters

### Manual Upload to GitHub
After local build, manually upload to GitHub:
1. Go to repository **Releases** page
2. Click **"Create a new release"**
3. Upload APK file from `test-builds/` folder
4. Copy release notes from generated text files

---

## 📦 APK Distribution via GitHub

### Automatic Release Creation

Every method creates releases on GitHub with this structure:

```
🚀 CheermateApp Test - v1.5-test-20241214-1430

📱 Quick Install
1. Download APK from Assets below  
2. Enable Unknown Sources in Android Settings
3. Install APK by tapping downloaded file
4. Grant permissions when prompted

✨ New Features to Test
👆 Swipe Gestures (NEW!)
- Swipe Right → Mark task as completed ✅
- Swipe Left → Delete task 🗑️
- Confirmation dialogs prevent accidents

🧪 Testing Priority  
- [x] Swipe gestures (NEW feature!)
- [x] Dark mode toggle
- [x] Statistics live updates
- [x] General app stability

📋 Build Details
- Build Date: 2024-12-14 14:30:00 UTC
- Version: 1.5 (Enhanced UX & Smart Features)  
- APK Size: 12MB
- Build Type: Debug (Testing)
```

### Tester Access

Testers can access APKs via:

1. **Direct Release Link**: 
   ```
   https://github.com/YOUR_USERNAME/CheermateApp/releases
   ```

2. **Specific Release**:
   ```  
   https://github.com/YOUR_USERNAME/CheermateApp/releases/tag/v1.5-test-20241214-1430
   ```

3. **Direct APK Download**:
   ```
   https://github.com/YOUR_USERNAME/CheermateApp/releases/download/v1.5-test-20241214-1430/app-debug.apk
   ```

---

## 🔧 Configuration & Customization

### Version Format
```
v1.5-test-YYYYMMDD-HHMM
```
- **v1.5**: App version (Enhanced UX & Smart Features)
- **test**: Build type (test/beta/release) 
- **YYYYMMDD-HHMM**: Timestamp for uniqueness

### Build Types
- **debug**: Fast builds with debugging info (~15MB)
- **release**: Optimized builds for production (~10MB)

### Customizing Release Notes
Edit the template in:
- **GitHub Actions**: `.github/workflows/build-test-apk.yml`
- **Local Scripts**: `scripts/generate-github-release.bat`

### Release Settings
- **Pre-release**: Marked as pre-release for testing
- **Assets**: APK file automatically attached
- **Retention**: GitHub keeps releases indefinitely

---

## 🧪 Testing Workflow

### For Developers

1. **Make changes** to code
2. **Test locally** if needed
3. **Push to main/develop** branch
4. **Wait 2-3 minutes** for GitHub Actions
5. **Check releases page** for new APK
6. **Share release link** with testers

### For Testers

1. **Receive release link** from developer
2. **Download APK** from GitHub release
3. **Follow installation** instructions in release notes  
4. **Test features** listed in release notes
5. **Report issues** back to developer

---

## 📊 Build Outputs

### Generated Files

All builds create these files in `test-builds/`:

```
CheermateApp-v1.5-test-20241214_143022.apk     # The actual APK
build-info-20241214_143022.json                # Build metadata  
INSTALLATION-20241214_143022.txt               # Setup instructions
release-notes-20241214_143022.md              # Release notes
```

### APK Properties
- **Package**: `com.cheermateapp`
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)  
- **Size**: ~10-15MB depending on build type
- **Permissions**: Storage, Network, Notifications

---

## 🐛 Troubleshooting

### Common Issues

#### "GitHub Actions failing"
1. Check **Actions tab** for error details
2. Ensure **GITHUB_TOKEN** has proper permissions
3. Verify **Gradle build** works locally first

#### "APK not appearing in releases"
1. Check if workflow completed successfully
2. Verify release was marked as **pre-release**
3. Look in **draft releases** if not visible

#### "Local build failing"
1. Run `.\gradlew clean` first
2. Check **Java version** (needs JDK 17+)
3. Ensure **internet connection** for dependencies
4. Verify **Android SDK** is installed

#### "GitHub CLI authentication"
```bash
# Re-authenticate with GitHub
gh auth logout
gh auth login
```

### Build Performance
- **GitHub Actions**: 2-4 minutes
- **Local PowerShell**: 1-2 minutes  
- **Local Batch**: 2-3 minutes

---

## 🎯 Best Practices

### Release Strategy
1. **Use GitHub Actions** for regular releases
2. **Use manual scripts** for hotfixes or custom builds
3. **Mark as pre-release** for testing versions
4. **Include comprehensive** release notes

### Version Management
1. **Increment version** for major changes
2. **Use timestamps** for unique identification
3. **Keep old releases** for regression testing
4. **Tag important** milestone releases

### Tester Communication
1. **Share GitHub release links** (not APK files directly)
2. **Include testing instructions** in every release
3. **Specify priority features** to test
4. **Provide clear bug reporting** guidelines

---

## 🔗 Quick Links

### Repository Links
- **📋 Releases Page**: `https://github.com/YOUR_USERNAME/CheermateApp/releases`
- **⚙️ Actions Page**: `https://github.com/YOUR_USERNAME/CheermateApp/actions`  
- **📝 Issues Page**: `https://github.com/YOUR_USERNAME/CheermateApp/issues`

### Documentation
- **📱 User Guide**: [docs/USER_FEATURES_GUIDE.md](USER_FEATURES_GUIDE.md)
- **🔧 Build Scripts**: [scripts/README.md](../scripts/README.md)
- **📋 Changelog**: [CHANGELOG.md](../CHANGELOG.md)

### External Tools
- **GitHub CLI**: https://cli.github.com/
- **PowerShell**: https://docs.microsoft.com/powershell/
- **Android SDK**: https://developer.android.com/studio

---

## ✅ Success Checklist

After setting up APK generation, verify:

- [ ] ✅ **GitHub Actions workflow** exists and runs
- [ ] 📱 **APKs generate** successfully on every push  
- [ ] 🏷️ **Releases created** automatically with proper tags
- [ ] 📋 **Release notes** include installation instructions
- [ ] 🔗 **Links work** for direct APK downloads
- [ ] 📞 **Testers can access** releases without issues
- [ ] 🧪 **Testing instructions** are clear and complete

---

**🎉 Your CheermateApp is now ready for seamless tester distribution via GitHub!**

**Last Updated**: December 2024  
**Supports**: CheermateApp v1.5 (Enhanced UX & Smart Features)