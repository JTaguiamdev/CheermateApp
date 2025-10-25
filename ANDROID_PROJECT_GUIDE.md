# CheermateApp - Android/Kotlin Project Guide

## ⚠️ Important: This is an Android/Kotlin Project, NOT a React Native/Expo Project

### Common Confusion

If you're seeing errors like:
```
Error [ERR_MODULE_NOT_FOUND]: Cannot find package 'expo' imported from C:\Users\owner\CheermateApp\index.ts
```

**This error is NOT related to this repository!** This repository contains a native Android application built with:
- **Kotlin** programming language
- **Android Studio** IDE
- **Gradle** build system
- **Android SDK** and tools

### What This Project IS

✅ **Native Android Application**
- Written in **Kotlin** (Android's modern programming language)
- Uses Android Views with Material Design
- Built with Gradle build system
- Runs on Android devices (phones and tablets)

### What This Project IS NOT

❌ **NOT a React Native Project**
❌ **NOT an Expo Project**
❌ **NOT a Node.js/JavaScript/TypeScript Project**
❌ **Does NOT use npm/yarn/package.json**
❌ **Does NOT have index.ts or index.js files**

### Why the Confusion?

If you're seeing errors about `expo` or `index.ts`:

1. **Wrong Project Directory**: You may be trying to run Node.js/Expo commands in the wrong project directory
2. **Mixed Projects**: You might have both React Native and Android projects and are confusing them
3. **Local Files**: You may have accidentally created `index.ts` or `package.json` files locally that are not part of this repository

### How to Build and Run This Project

#### Prerequisites
- **Android Studio** Arctic Fox or later
- **JDK 11** or later
- **Android SDK** 24 or higher
- **Gradle** (included via gradlew wrapper)

#### Build Commands

```bash
# Clean and build the project
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

#### Using Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the CheermateApp directory
4. Wait for Gradle sync to complete
5. Click the "Run" button (green play icon) to run on an emulator or connected device

### Project Structure

```
CheermateApp/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cheermateapp/  # Kotlin source files
│   │   │   ├── res/              # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumentation tests
│   └── build.gradle.kts          # App-level build configuration
├── build.gradle.kts              # Project-level build configuration
├── settings.gradle.kts           # Gradle settings
└── gradle/                       # Gradle wrapper files
```

### Technology Stack

- **Language**: Kotlin
- **UI**: Android Views with Material Design
- **Database**: SQLite with Room persistence library
- **Architecture**: MVVM pattern with LiveData
- **Async Operations**: Kotlin Coroutines
- **Build System**: Gradle with Kotlin DSL
- **Testing**: JUnit, Espresso

### If You Need a React Native/Expo Project

If you're looking for a React Native or Expo project, this is not it. You would need:

1. A different project with `package.json`
2. Node.js installed
3. npm or yarn package manager
4. React Native CLI or Expo CLI

### Getting Help

For issues specific to this Android/Kotlin project:
- Check `README.md` for general project information
- See `QUICKSTART.md` for development guidelines
- Read `BUILD_AND_TEST_GUIDE.md` for build instructions

For React Native/Expo projects:
- Visit https://reactnative.dev/
- Visit https://expo.dev/

### No Kotlin Code to Remove

**Note**: The user request mentions "removing unnecessary Kotlin code from previous technology stack." However, Kotlin IS the technology stack for this Android application. Kotlin is:

- The primary programming language for Android development (recommended by Google)
- Modern, safe, and concise
- The language ALL code in this project is written in
- NOT unnecessary - it's the core of this project

**Removing Kotlin code would mean removing the entire application!**

If you meant removing old/unused features or refactoring, please specify which features or files you want to modify.
