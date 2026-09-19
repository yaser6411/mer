# Changelog

All notable changes to **MERCURY Business Manager** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - 2026-09-18

### Fixed
- **Play Store Publishing Deadlock**: Resolved `400 Bad Request` package collision by permanently setting `applicationId` to `com.yusrtec.mercury.businessmanager` while preserving internal namespace `com.example`.
- **Release Bundle Signing**: Configured robust keystore fallback logic in `app/build.gradle.kts` to guarantee zero-failure execution of `:app:bundleRelease` and `:app:signReleaseBundle`.
- **R8 / Missing Classes Warnings**: Added comprehensive ProGuard and R8 keep rules to cleanly handle JVM/desktop classes referenced by transitive networking dependencies.

### Added
- **Clean Architecture Domain Layer**:
  - `GetDashboardSummaryUseCase`: Pure domain aggregation of daily/monthly revenues, COGS, expenses, low stock items, and balance totals.
  - `ProcessSaleUseCase`: Transactional POS processing with automated stock deduction, line-item ledger insertions, and customer balance updates.
  - `ProcessPurchaseUseCase`: Inbound supplier purchase order execution with automated stock replenishment and supplier debt accounting.
  - `FinancialEngine`: Standalone arithmetic engine with unit tests for calculating subtotals, compound discounts, tax tranches, and margins.
- **Dependency Injection**: Integrated thread-safe `AppContainer` providing lifecycle-scoped repositories and use cases.
- **Robust UI State Architecture**: Introduced `UiState<T>` (`Loading`, `Success`, `Error`, `Empty`) across core ViewModel flows.
- **Material 3 State Components**: Created reusable `LoadingStateView` and `ErrorStateView` with accessible retry affordances.
- **Automated Unit & Integration Tests**: Implemented local JVM Robolectric test suite with in-memory Room SQLite testing (`ProcessSaleUseCaseTest`, `ProcessPurchaseUseCaseTest`, `GetDashboardSummaryUseCaseTest`).
- **Security & Privacy Hardening**:
  - `network_security_config.xml`: Disallowed cleartext HTTP, enforcing TLS 1.3 encryption.
  - `AppLogger`: Production-safe logging wrapper stripping debug information from release APKs and AABs.
- **Play Store Publishing Kit**: Created `PLAY_STORE_METADATA.md` with complete title, short/full descriptions, feature graphic specifications, Data Safety declarations, and step-by-step rollout guides.

---

## [1.0.0] - 2026-09-01
- Initial release with offline-first Room database, POS checkout, product catalog, customer/supplier ledgers, expense tracking, and JSON/CSV backup utilities.
