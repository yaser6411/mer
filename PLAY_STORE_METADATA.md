# Google Play Store Listing & Publishing Kit

This document provides the complete, pre-formatted store listing metadata, policy compliance declarations, and step-by-step rollout instructions for **MERCURY Business Manager**.

---

## 1. App Identity & Store Listing

### App Title (24 characters / max 30)
```
MERCURY Business Manager
```

### Short Description (78 characters / max 80)
```
Offline POS, inventory, supplier purchases, expense tracker & profit analytics.
```

### Full Description (Max 4000 characters)
```
Take complete control of your retail shop, wholesale distribution, workshop, or freelance business with MERCURY Business Manager — the intelligent, 100% offline-first Point of Sale (POS) and accounting solution.

Designed specifically for business owners who demand speed, reliability, and privacy, MERCURY works anywhere without requiring an active internet connection. All your business records, product inventory, and customer transactions remain securely on your device.

KEY HIGHLIGHTS:

📊 REAL-TIME EXECUTIVE DASHBOARD
- Instant financial metrics: today's sales, purchases, operating expenses, and estimated net profit.
- Monthly revenue trends and profit margin tracking.
- Real-time inventory valuation at cost and retail prices.
- Fast tracking of customer receivables and supplier payables.
- Automated low-stock alerts to prevent supply stockouts.

💳 FAST POINT OF SALE (POS) & INVOICING
- Intuitive, high-speed cash register and cart management.
- Quick product search by name, SKU, or barcode scanning.
- Flexible payment processing: full payment, partial downpayments, or credit sales.
- Professional invoice generation with customizable tax rates and business headers.
- Instant invoice sharing via PDF, WhatsApp, email, or direct thermal printing.

📦 SMART INVENTORY & STOCK LEDGER
- Comprehensive product catalog with multi-category classification.
- Low-stock safety thresholds and automated alert triggers.
- Detailed stock movement history: every sale, purchase order, and manual adjustment is tracked.
- Support for customizable units of measurement (pcs, kg, box, liters).

🤝 CUSTOMER & SUPPLIER CREDIT MANAGEMENT
- Complete credit ledger for customer receivables and supplier payables.
- Record payments and partial settlements with timestamps.
- Detailed contact transaction history and current debt positions.

📈 EXPENSE TRACKING & P&L STATEMENTS
- Categorize overhead costs: Rent, Salaries, Utilities, Maintenance, Shipping, Marketing, and Taxes.
- Comprehensive Profit and Loss (P&L) statements across customizable timeframes.
- Cost of Goods Sold (COGS) analysis to identify your most profitable product lines.

🔒 100% PRIVACY & DATA OWNERSHIP
- No mandatory cloud accounts, no monthly recurring subscription fees, and no third-party data tracking.
- Complete one-tap full system backup to JSON.
- Export and import spreadsheets in standard CSV format for Excel and Google Sheets.
- Dual-language interface with seamless English and Arabic (RTL) support.

Whether you run a grocery store, boutique, coffee shop, electronic repair shop, or distribution hub, MERCURY Business Manager equips you with professional enterprise tools right from your Android device.
```

---

## 2. Categorization & Contact Details

- **Application Category**: Business / Productivity
- **Tags**: Point of Sale, POS, Inventory Management, Invoicing, Accounting, Small Business
- **Target Audience**: 18+ (General Business Owners)
- **Contact Email**: `support@yusrtec.com` (or your verified support email)
- **Privacy Policy URL**: Hosted URL for your privacy policy (e.g. `https://yusrtec.com/privacy/mercury-business-manager`)

---

## 3. Data Safety Declaration (Google Play Console)

Because **MERCURY Business Manager** operates completely offline:

1. **Does your app collect or share any user data?**
   - Select: **No**, this app does not collect or share any user personal or device data.
2. **Is all user data stored locally?**
   - Select: **Yes**, all application data (sales, products, contacts) is stored on-device in the local SQLite database.
3. **Data Encryption**:
   - Data stored in local app storage is protected by Android system sandboxing and OS encryption.
4. **Account Creation**:
   - Select: **No account creation required**. The app does not mandate or support remote cloud accounts.

---

## 4. Google Play Publishing Step-by-Step Guide

Follow these exact steps in the [Google Play Console](https://play.google.com/console):

### Step 1: Create the App
1. Go to **All apps** > click **Create app**.
2. **App name**: `MERCURY Business Manager`
3. **Default language**: English (United States)
4. **App or game**: App
5. **Free or paid**: Free (or Paid depending on your pricing strategy)
6. Check the declarations for Developer Program Policies and US export laws.
7. Click **Create app**.

### Step 2: Set Up Store Presence
1. Under **Grow** > **Store presence** > **Main store listing**:
   - Paste the **Short description** and **Full description** provided above.
   - **App icon**: Upload `public/play-store-assets/app_icon_512x512.png` (512 x 512 px, 246 KB).
   - **Feature graphic**: Upload `public/play-store-assets/feature_graphic_1024x500.png` (1024 x 500 px, 687 KB).
   - **Phone screenshots** (Upload 2-8 images, 9:16 aspect ratio):
     1. `public/play-store-assets/screenshot_01_dashboard_1080x1920.png` (Executive Financial Dashboard & KPIs)
     2. `public/play-store-assets/screenshot_02_pos_checkout_1080x1920.png` (Point of Sale & Invoicing)
     3. `public/play-store-assets/screenshot_03_inventory_1080x1920.png` (Inventory & Low-Stock Alerts)
   - Click **Save**.

### Step 3: Complete App Content & Policy Questionnaires
Navigate to **Policy and programs** > **App content**:
- **Privacy Policy**: Provide the link to your privacy policy.
- **Ads**: Select **No, my app does not contain ads**.
- **App Access**: Select **All functionality is available without special access restrictions**.
- **Content Ratings**: Complete the questionnaire (select "Utility / Productivity", answering No to violence, drugs, gambling, and user interaction).
- **Target Audience**: Select **18 and over**.
- **Financial Features**: Declare as "Financial tracking / accounting tool" without direct banking or money transfer capabilities.
- **Data Safety**: Declare that **no data is collected or shared**.

### Step 4: Upload the Release Android App Bundle (AAB)
1. In the left menu, navigate to **Release** > **Production** (or **Testing** > **Closed testing** for internal verification).
2. Click **Create new release**.
3. Under **App bundles**, upload:
   `app/build/outputs/bundle/release/app-release.aab`
4. Verify the package name displays: `com.yusrtec.mercury.businessmanager`.
5. Verify the version code displays: `3 (3.0)`.
6. Add Release Notes:
   ```
   Initial production release of MERCURY Business Manager:
   - Full offline POS and invoicing system
   - Real-time executive financial dashboard & KPI reports
   - Multi-category inventory management with low-stock alerts
   - Customer receivables & supplier payables ledger
   - One-tap JSON backup and Excel CSV export/import
   - English and Arabic RTL language support
   ```
7. Click **Next** > review any warnings > click **Save**.

### Step 5: Roll Out the Release
1. Review the release summary.
2. Click **Start rollout to Production** (or Closed Testing).
3. The app will enter Google Play Review and will go live once reviewed!
