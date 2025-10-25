# Build Issues and Network Restrictions

## Current Build Status

⚠️ **Note**: The project build may fail in certain environments due to network restrictions.

### Known Issue: Google Maven Repository Blocked

The Android Gradle Plugin is hosted on Google's Maven repository (`dl.google.com`), which may be blocked or inaccessible in some network environments.

**Error message:**
```
Could not GET 'https://dl.google.com/dl/android/maven2/...'
> dl.google.com: No address associated with hostname
```

### Solutions

#### Option 1: Use VPN or Proxy
If you're in an environment where `dl.google.com` is blocked, you may need to:
- Use a VPN to access Google's servers
- Configure a proxy in `gradle.properties`:
  ```properties
  systemProp.http.proxyHost=your.proxy.host
  systemProp.http.proxyPort=8080
  systemProp.https.proxyHost=your.proxy.host
  systemProp.https.proxyPort=8080
  ```

#### Option 2: Use Mirror Repositories (China Mainland)
If you're in China, you can use Aliyun mirrors. Edit `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}
```

#### Option 3: Use Android Studio with Automatic Configuration
Android Studio typically handles these network issues automatically:
1. Open the project in Android Studio
2. Let it sync and download dependencies
3. Android Studio may offer to configure proxy settings for you

### Verifying Your Environment

Check if you can access the required repositories:

```bash
# Test Google Maven Repository
curl -I https://dl.google.com/dl/android/maven2/

# Test Maven Central
curl -I https://repo1.maven.org/maven2/

# Test DNS resolution
ping dl.google.com
```

### Working Environment Requirements

For the build to succeed, you need:
- ✅ Access to `dl.google.com` (Google Maven Repository)
- ✅ Access to `repo1.maven.org` (Maven Central)
- ✅ Access to `plugins.gradle.org` (Gradle Plugin Portal)
- ✅ JDK 11 or later installed
- ✅ Android SDK installed (or let Gradle download it)

### Alternative: Download Dependencies Manually

If all else fails, you can:
1. Download the Android Gradle Plugin JAR files manually from a machine with internet access
2. Set up a local Maven repository
3. Point Gradle to your local repository

This is advanced and not recommended unless absolutely necessary.

### Current Project Configuration

The project is currently configured with:
- Android Gradle Plugin: `8.1.4`
- Kotlin: `1.9.10`
- Gradle: `8.13` (via wrapper)

These versions are stable and widely used. The configuration uses the modern version catalog approach (`libs.versions.toml`).

### Need Help?

If you're experiencing build issues:
1. Check your network connectivity
2. Verify you can access Google's Maven repository
3. Try building from Android Studio instead of command line
4. Check if you're behind a corporate firewall or in a restricted network
5. Consider using a VPN if necessary

### This is NOT Related to the Expo Error

The `expo` and `index.ts` error mentioned in the issue is completely unrelated to this Android/Kotlin project. See [ANDROID_PROJECT_GUIDE.md](ANDROID_PROJECT_GUIDE.md) for clarification.
