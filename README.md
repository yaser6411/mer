# MERCURY Business Manager

[![Android Build & Test](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)]()
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)]()
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey.svg)]()

**MERCURY Business Manager** is a high-performance, offline-first Point of Sale (POS), inventory management, and business accounting application designed for small businesses, shops, workshops, distributors, freelancers, and merchants.

Built using **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **Clean Architecture**.

---

## Key Features

- **Executive Financial Dashboard**: Real-time KPI summaries for today's sales, procurement, operating expenses, estimated gross/net margins, customer receivables, supplier payables, and low stock warnings.
- **Point of Sale (POS)**: Fast checkout flow with multi-item carts, instantaneous line-item discounts, variable tax calculations, split payment statuses (Paid, Partial, Credit), and receipt generation.
- **Inventory & Stock Management**: Product catalog with SKU/barcode indexing, cost vs. selling price calculation, category filtering, min-stock triggers, and stock adjustment auditing with full ledger history.
- **Supplier & Purchase Order Tracking**: Inbound stock management, cost updates, supplier ledger balancing, and payment record keeping.
- **Contact CRM & Credit Ledger**: Unified tracking of customer receivables and supplier payables with one-touch balance auditing and settlement.
- **Operating Expense Tracker**: Categorized overhead logging (Rent, Salaries, Utilities, Maintenance, Marketing) integrated into P&L statements.
- **Reports & Financial Statements**: Comprehensive Profit & Loss (P&L) statements, Cost of Goods Sold (COGS), order frequency, inventory valuation, and debt distribution across customizable time horizons.
- **Offline-First Resilience & Portability**: 100% on-device SQLite database with complete JSON snapshot backup/restore and CSV import/export compatible with Microsoft Excel and Google Sheets.
- **Bilingual & RTL-Ready**: Native English and Arabic support with automatic RTL (Right-to-Left) mirroring and localized typography.

---

## Architecture Overview

The codebase is organized following **Clean Architecture** and **MVVM** principles:

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── dao/             # Room DAOs (Product, Sale, Purchase, Customer, Supplier, Expense)
│   │   ├── entity/          # Room Entities with indexes, foreign keys & cascades
│   │   └── MercuryDatabase  # Central Room Database with thread-safe singleton
│   ├── preferences/         # Local settings, theme preferences & locale management
│   └── repository/          # Repository implementing offline-first data aggregation
├── di/
│   └── AppContainer.kt      # Lightweight application container for Dependency Injection
├── domain/
│   ├── FinancialEngine.kt   # Pure domain accounting and currency arithmetic engine
│   └── usecase/             # Business logic use cases (GetDashboardSummary, ProcessSale, etc.)
├── ui/
│   ├── components/          # Reusable Material 3 design system components & state views
│   ├── screens/             # Jetpack Compose screens (Dashboard, POS, Inventory, Reports, etc.)
│   ├── theme/               # Material 3 Color Schemes, Typography, Shapes & Motion
│   └── viewmodel/           # Reactive AndroidViewModels exposing StateFlow<UiState<T>>
└── util/
    ├── AppLogger.kt         # Secure production logger disabling debug logs in release
    └── UiState.kt           # Sealed UI State hierarchy (Loading, Success, Error, Empty)
```

---

## Tech Stack & Dependencies

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3 Design Tokens
- **Local Persistence**: Room SQLite 2.7.0 with KSP (Kotlin Symbol Processing)
- **Asynchronous Flow**: Kotlin Coroutines & Reactive `StateFlow`
- **Dependency Injection**: Application-scoped Container (`AppContainer`)
- **JSON Serialization**: Kotlinx Serialization
- **Image Handling**: Coil 3 Compose
- **Testing**: Robolectric, JUnit 4, Roborazzi Screenshot Testing

---

## Build & Test Instructions

### Prerequisites
- JDK 17 or JDK 21
- Android SDK Platform 36 (minSdk: 26)
- Gradle 8.13+

### 1. Compile Debug Application
```bash
gradle :app:assembleDebug
```

### 2. Run Local Robolectric Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### 3. Generate Release Android App Bundle (AAB)
```bash
gradle :app:bundleRelease
```
The resulting `.aab` will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

---

## Google Play Release Information

- **Application ID**: `com.yusrtec.mercury.businessmanager`
- **Internal Namespace**: `com.example`
- **Target SDK**: 36 (Android 16 / Play 2026 requirement)
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Security**: Network Security Config disallows cleartext HTTP traffic. R8 optimization configured for production builds.

For detailed store listing metadata, policy answers, and rollout instructions, refer to [`PLAY_STORE_METADATA.md`](PLAY_STORE_METADATA.md).
