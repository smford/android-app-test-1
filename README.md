# Modern Android Auth Architecture

[![Android CI](https://github.com/smford/android-app-test-1/actions/workflows/ci.yml/badge.svg)](https://github.com/smford/android-app-test-1/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09-green.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-1.3.0-blue.svg)](https://m3.material.io/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%2B-brightgreen.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A complete, production-ready Android application built with **Kotlin**, **Jetpack Compose**, **Material 3 (`androidx.compose.material3`)**, the modern **Android Credential Manager API**, and **Firebase Authentication**.

---

## Key Highlights

* **Modern Credential Manager**: Uses `androidx.credentials.CredentialManager` and `com.google.android.libraries.identity.googleid:googleid`—replacing deprecated legacy `GoogleSignInClient` with passkey and biometrics readiness.
* **Firebase Authentication Integration**: Seamless exchange of Google ID Tokens for Firebase Credentials via coroutine-driven `await()`.
* **Instant Auth Gate (Splash)**: Hot `StateFlow` evaluates session state immediately on launch and routes either straight to `Dashboard` or to `Login`.
* **Welcome Card Experience**: Displays the exact text `"Hello [Firstname] [Surname]"` by safely parsing authenticated profile claims with robust fallbacks.
* **Modal Navigation Drawer**:
  * Rich profile header with user avatar, display name, and email.
  * Navigation items: *Home*, *Demo Page 1 (Analytics)*, *Demo Page 2 (Explore)*, and *Settings*.
  * Dedicated **Logout** action styled with danger/error surface colors (`errorContainer` / `onErrorContainer`).
* **Session Invalidation & Backstack Purging**: Logging out clears credentials and executes `navController.navigate(Login) { popUpTo(0) { inclusive = true } }`.
* **Sleek High-Contrast Obsidian Theme**: Custom Material 3 dark palette with subtle container surface elevations, fluid padding, and rounded geometry (16–28dp).
* **Edge-to-Edge Layout**: Enabled via `enableEdgeToEdge()` for full-bleed immersive experiences on Android 14/15.

---

## Architecture & Codebase Layout

```
android-app-test-1/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml          # GitHub Form Issue Template for bug reports
│   │   └── feature_request.yml     # GitHub Form Issue Template for feature requests
│   ├── workflows/
│   │   └── ci.yml                  # GitHub Actions CI (build, unit test, artifact upload)
│   ├── dependabot.yml              # Automated dependency tracking (Gradle & Actions)
│   └── pull_request_template.md    # Standardized Pull Request template
├── app/
│   ├── build.gradle.kts            # Application Gradle build configuration
│   ├── google-services.json        # Firebase configuration template
│   ├── proguard-rules.pro          # Obfuscation and keep rules for Credential Manager
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/modernauthapp/
│       │   │   ├── AuthApplication.kt          # App class initializing Firebase
│       │   │   ├── MainActivity.kt             # Root activity with enableEdgeToEdge()
│       │   │   ├── data/
│       │   │   │   ├── auth/
│       │   │   │   │   └── AuthManager.kt      # Credential Manager + Firebase Auth Service
│       │   │   │   └── model/
│       │   │   │       ├── AuthState.kt        # Reactive authentication states
│       │   │   │       └── AuthUser.kt         # User domain model with name parsing
│       │   │   └── ui/
│       │   │       ├── components/
│       │   │       │   ├── AppDrawer.kt        # ModalNavigationDrawer with Logout action
│       │   │       │   ├── GoogleSignInButton.kt # Google SSO button with loading spinner
│       │   │       │   └── WelcomeCard.kt      # "Hello [Firstname] [Surname]" card
│       │   │       ├── navigation/
│       │   │       │   ├── AppNavHost.kt       # Centralized NavHost with backstack purge
│       │   │       │   └── NavRoutes.kt        # Type-safe route definitions
│       │   │       ├── screens/
│       │   │       │   ├── splash/SplashScreen.kt
│       │   │       │   ├── login/LoginScreen.kt & LoginViewModel.kt
│       │   │       │   ├── dashboard/DashboardScreen.kt & DashboardViewModel.kt
│       │   │       │   ├── analytics/AnalyticsScreen.kt (Demo Page 1)
│       │   │       │   ├── explore/ExploreScreen.kt (Demo Page 2)
│       │   │       │   └── settings/SettingsScreen.kt
│       │   │       └── theme/
│       │   │           ├── Color.kt, Shape.kt, Theme.kt, Type.kt
│       │   └── res/
│       │       └── drawable/ic_google_logo.xml # Official vector Google 'G'
│       └── test/
│           └── java/com/example/modernauthapp/AuthUserTest.kt # Unit tests for name parsing
├── gradle/
│   ├── libs.versions.toml          # Gradle Version Catalog
│   └── wrapper/                    # Gradle Wrapper 8.13
├── .editorconfig                   # Kotlin & Android code formatting standard
├── .gitattributes                  # Normalized line endings (LF for gradlew/scripts)
├── .gitignore                      # Android build & credential ignores
├── build.gradle.kts                # Root project build configuration
├── gradle.properties               # Gradle JVM & AndroidX properties
└── settings.gradle.kts             # Gradle repository & module settings
```

---

## Getting Started

### 1. Prerequisites
* **Android Studio**: Koala (2024.1+) or Ladybug (2024.2+)
* **JDK**: OpenJDK 17
* **Android SDK**: API Level 34 Platform & Build Tools

### 2. Generate SHA-1 Certificate Fingerprint
Google Sign-In with Credential Manager requires registering your app's debug signing key:

```bash
./gradlew signingReport
```
*Look for the `SHA1` string under the `:app:signingReport` section.*

### 3. Firebase Console Configuration
1. Open the [Firebase Console](https://console.firebase.google.com/) and create or open your project.
2. Under **Build > Authentication > Sign-in method**, enable the **Google** provider.
3. In **Project settings > General > Your apps**, register an Android app with package name:
   ```
   com.example.modernauthapp
   ```
4. Paste your **SHA-1 fingerprint** into the app configuration.
5. Download `google-services.json` and place it in the `app/` directory:
   ```bash
   cp ~/Downloads/google-services.json app/google-services.json
   ```

### 4. Configure Web Client ID
In the Firebase Console, go to **Authentication > Sign-in method > Google > Web SDK configuration** and copy the **Web client ID**.

Update the `buildConfigField` in [`app/build.gradle.kts`](file:///Users/asc/git/android-app-test-1/app/build.gradle.kts#L24):
```kotlin
buildConfigField("String", "WEB_CLIENT_ID", "\"YOUR_COPIED_WEB_CLIENT_ID.apps.googleusercontent.com\"")
```

---

## Building and Testing

### Run Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install Directly to Connected Device / Emulator
```bash
./gradlew installDebug
```

---

## Security Best Practices

* **Credential Manager Isolation**: Session state is cleared from both device credential storage and Firebase Auth upon sign-out.
* **Token Protection**: No access tokens or OAuth refresh secrets are logged or persisted locally in unencrypted storage.
* **Backstack Cleared**: On logout, the Jetpack Navigation backstack is pruned completely (`popUpTo(0) { inclusive = true }`) to prevent back navigation to authenticated screens.

---

## Contributing

Contributions are welcome! Please review our [Contributing Guidelines](CONTRIBUTING.md) and [Code of Conduct](CONTRIBUTING.md#code-of-conduct) before opening pull requests.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
