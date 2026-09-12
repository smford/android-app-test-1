# Contributing Guidelines

Thank you for contributing to this project!

## Code of Conduct
Please be respectful and constructive in all discussions, pull requests, and code reviews.

## Development Workflow

1. **Fork and Clone** the repository.
2. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b bugfix/your-bugfix-name
   ```
3. **Open in Android Studio**:
   - Ensure you are using **Android Studio Koala / Ladybug or newer**.
   - Configure JDK to **Java 17**.
4. **Follow Code Conventions**:
   - Follow Kotlin official style guidelines configured in `.editorconfig`.
   - Maintain unidirectional data flow (UDF) with `StateFlow` and Compose state hoisting.
   - Use Material 3 semantic tokens (`colorScheme`, `typography`, `shapes`).
5. **Verify Build & Tests**:
   Before submitting your PR, ensure local verification succeeds:
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```
6. **Submit a Pull Request**:
   - Fill out the PR template completely.
   - Ensure the CI workflow passes.
