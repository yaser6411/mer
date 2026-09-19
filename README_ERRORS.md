# MERCURY Business Manager — Google Play Publishing Error Report & Troubleshooting Guide

This document explains the Google Play publishing error encountered during automated deployment, the root cause analysis, why the automated system looped, and the step-by-step resolution options.

---

## 1. Exact Publishing Error

```
Publishing failed: <eye3 title='INTERNAL'/> generic::INTERNAL: Failed to create app listing: 400 Bad Request
POST https://androidpublisher.googleapis.com/androidpublisher/v3/developers/8889416246417428956/appsmanagement
{
  "code": 400,
  "details": [
    {
      "@type": "type.googleapis.com/google.rpc.DebugInfo",
      "detail": "[ORIGINAL ERROR] generic::invalid_argument: com.google.apps.framework.request.BadRequestException: Package name com.mercury.businessmanager is not available on Play. [google.rpc.error_details_ext] { message: \"Package name com.mercury.businessmanager is not available on Play.\" }"
    }
  ],
  "errors": [
    {
      "domain": "global",
      "message": "Package name com.mercury.businessmanager is not available on Play.",
      "reason": "badRequest",
      "debugInfo": "detail: \"[ORIGINAL ERROR] generic::invalid_argument: com.google.apps.framework.request.BadRequestException: Package name com.mercury.businessmanager is not available on Play. [google.rpc.error_details_ext] { message: \\\"Package name com.mercury.businessmanager is not available on Play.\\\" }\"\n"
    }
  ],
  "message": "Package name com.mercury.businessmanager is not available on Play.",
  "status": "INVALID_ARGUMENT"
}
```

---

## 2. Root Cause Analysis

### What `Package name ... is not available on Play` Means:
1. **Global Uniqueness Requirement**: Every Android application on Google Play must have an `applicationId` (package name) that is globally unique across all Android developers worldwide.
2. **Namespace Collision / Reservation**: The identifier `com.mercury.businessmanager` is already registered, reserved, or claimed by an entity in the global Google Play ecosystem. 
3. **API Restriction on New App Creation**: Google Play Developer API's `appsmanagement` endpoint does not permit third-party API clients to claim or register a package name that is unavailable or not pre-authorized in the developer's console.

---

## 3. Why the Automated System Looped (100 Times)

1. **The API Rejected the Package Name**: The automated publisher sent `com.mercury.businessmanager` to Google Play and received `400 Bad Request`.
2. **Conflicting Guardrail Instructions**: The automated retry harness dispatched messages with strict programmatic rules:
   > *"CRITICAL RULES: NEVER change the applicationId/package name in any build file."*
3. **The Deadlock**:
   - The publisher couldn't succeed without changing the package name or registering it.
   - The automated prompt strictly forbade changing the package name.
   - Every retry executed the exact same payload, yielding the exact same `400 Bad Request`.

---

## 4. Full Codebase Readiness Audit

The codebase itself is fully compliant with Google Play requirements:

| Area | Status | Notes |
| :--- | :---: | :--- |
| **App Title** | Pass | `"MERCURY Business Manager"` is 24 characters (within 30-char Play limit) |
| **Target SDK** | Pass | `targetSdk = 36`, `compileSdk = 36`, `minSdk = 26` |
| **App Icons** | Pass | Full adaptive launcher icon set configured in `mipmap-anydpi-v26/` and `mipmap/`, plus raster WebP assets across all densities (`mdpi` through `xxxhdpi`) |
| **Permissions** | Pass | Minimal required permissions (`CAMERA` with `required="false"` hardware flag) |
| **File Provider** | Pass | AndroidX `FileProvider` mapped to `${applicationId}.fileprovider` |
| **Compilation** | Pass | All Gradle tasks build successfully and JVM unit tests pass |

---

## 5. Recommended Solutions

### Solution A: Update `applicationId` to a Unique Name (Recommended)

Change `applicationId` in `app/build.gradle.kts` to an identifier incorporating your developer organization name or domain:

**Suggested Candidates:**
- `com.yusrtec.mercury.businessmanager`
- `com.yusrtec.mercury`
- `com.aistudio.mercury.businessmanager`

#### How to Apply in `app/build.gradle.kts`:
```kotlin
android {
    namespace = "com.example" // Keep namespace as com.example
    
    defaultConfig {
        applicationId = "com.yusrtec.mercury.businessmanager" // Unique package name
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

> **Important**: Changing `applicationId` does **not** require changing source code directories or the `namespace = "com.example"` declaration.

---

### Solution B: Register Draft App in Google Play Console First

If `com.mercury.businessmanager` is owned by your organization:
1. Log in to the [Google Play Console](https://play.google.com/console).
2. Click **Create app**.
3. Name the app **MERCURY Business Manager**.
4. In the initial setup, specify package name `com.mercury.businessmanager`.
5. If accepted, save the draft. The publishing API can then upload to the created listing.
6. If Play Console displays *"This package name is not available"*, you must use **Solution A**.
