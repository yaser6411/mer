# Contributing to MERCURY Business Manager

Thank you for your interest in contributing to **MERCURY Business Manager**. This document outlines guidelines and development workflows to maintain high quality, reliability, and security standards.

---

## Development Setup

1. **JDK Version**: Ensure JDK 17 or 21 is configured.
2. **Android SDK**: Install API Level 36 (Android 16).
3. **Build Verification**:
   ```bash
   # Run all local JVM unit tests
   gradle :app:testDebugUnitTest

   # Assemble debug binary
   gradle :app:assembleDebug
   ```

---

## Architecture Principles

1. **Clean Architecture Separation**:
   - **Data Layer** (`data/`): Contains Room Entities, DAOs, SQLite database migrations, and repository implementations.
   - **Domain Layer** (`domain/`): Contains pure Kotlin business use cases and financial arithmetic algorithms. Must have zero Android framework dependencies.
   - **Presentation Layer** (`ui/`): Jetpack Compose screens and `AndroidViewModel` subclasses observing Use Cases and emitting `StateFlow<UiState<T>>`.
2. **Offline-First & Thread Safety**:
   - All database read/write queries must run on background dispatchers (`Dispatchers.IO`).
   - Mutations affecting multiple tables (e.g., Sales + Stock deduction + Ledger) must execute inside `database.withTransaction`.
3. **State Management**:
   - Use `UiState.Loading`, `UiState.Success`, `UiState.Error`, and `UiState.Empty` to ensure consistent handling across every view.

---

## Code Quality & Standards

- **Kotlin Idioms**: Use immutable collections, `val` wherever possible, and `data class` with copy methods.
- **Resource Extraction**: Never hardcode user-facing strings in Composables. All UI strings must reside in `res/values/strings.xml` and `res/values-ar/strings.xml`.
- **Logging**: Use `AppLogger.d()` or `AppLogger.e()` instead of `android.util.Log` or `println` to ensure logs are automatically stripped in production release builds.
- **Testing**:
  - Any new accounting or transactional feature must be covered by Robolectric unit tests in `src/test/java/com/example/`.
  - Test against in-memory Room SQLite instances (`Room.inMemoryDatabaseBuilder`) to verify real database integrity.

---

## Pull Request Checklist

Before submitting a pull request, verify:
- [ ] `./gradlew testDebugUnitTest` runs without errors or warnings.
- [ ] Release bundle builds cleanly: `./gradlew bundleRelease`.
- [ ] No hardcoded strings in newly added Composables.
- [ ] Both English (`values`) and Arabic (`values-ar`) strings are defined.
- [ ] UI components declare touch targets of at least 48dp and have appropriate `Modifier.testTag()`.
