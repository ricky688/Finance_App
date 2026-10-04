# Notification Interception & Auto-Logging Testing Guideline

This document provides a comprehensive operational guideline for autonomous AI agents and developers to test, verify, and extend the **On-Device Payment Notification Interception & Auto-Logging Engine** in **VibeFinance**.

---

## 1. System Architecture Overview

VibeFinance intercepts push notifications and payment alerts locally with zero cloud or network calls (privacy-first).

```
[ Incoming Notification ]
          │
          ▼
┌─────────────────────────────────────────────────────────────┐
│ PaymentNotificationListener (NotificationListenerService)   │
│  1. Ignore own app & ongoing system services                │
│  2. Filter framework packages (Settings, SystemUI, etc.)    │
│  3. Verify InMemoryDatabase.isNotificationLoggingEnabled    │
│  4. Verify InMemoryDatabase.isAppInterceptEnabled()         │
│  5. Strict Payment Intent Guard (isPaymentNotification)     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Deterministic Parsing & Classification                      │
│  - parseNotification(title, text, pkg): Regex-based parsing │
│  - extractCardLast4(text, title): Authentic 4 digits only   │
│  - determineCategory(merchant): Local keyword categorization │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ PendingPaymentStore (Queue & Route Engine)                   │
│  - Check rememberedAccountId() -> Hierarchical Match        │
│    ├── MATCHED: Auto-accepts atomically into DB             │
│    │            Dispatches "Automatically Logged Expense"   │
│    └── UNKNOWN: Enqueues into StateFlow<List<PendingPayment>>│
└──────────────────────────────┬──────────────────────────────┘
                               │ (If unknown)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ UI Interaction: PendingPaymentChoiceDialog (MainScreen)     │
│  - User selects target account                              │
│  - Optional Checkbox: "Remember for [App] marked '[Card]'" │
│  - On Accept: Saves rule, auto-binds cardLast4, logs expense │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Environment Prerequisites

Before testing on any physical device or emulator (e.g. Waydroid at `192.168.240.112:5555` or Samsung Galaxy at `192.168.0.63:34441`):

### 2.1 Grant Notification Listener Permission
Android requires explicit user or root/ADB permission for notification reading:
```bash
adb -s <DEVICE_SERIAL> shell cmd notification allow_listener \
  com.example.vibefinance/com.example.vibefinance.service.PaymentNotificationListener
```
To verify that the permission is active:
```bash
adb -s <DEVICE_SERIAL> shell settings get secure enabled_notification_listeners | grep vibefinance
```

### 2.2 Verify App Settings (Auto-Log & Intercepted Apps)
In VibeFinance **Settings**:
1. **Automatic Notification Logging** must be switched **ON** (`isNotificationLoggingEnabled = true`).
2. The sending app (e.g. `Octopus / 八達通`, `Samsung Pay`, `Google Wallet`, `HSBC`, etc.) must be selected in **"Apps Allowed to Auto-Log"** (`selectedInterceptApps`).

---

## 3. The Two Core Testing Methods

### Method 1: Internal Intent Injection (Recommended for Fast Agent Loops)

VibeFinance's `MainActivity` provides a built-in intent receiver that forwards test payloads directly to `PaymentNotificationListener.processNotificationForTest()` without needing UI clicks or system status bar pulls.

#### Command Template
```bash
adb -s <DEVICE_SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'<NOTIFICATION_TITLE>'" \
  --es test_notif_text "'<NOTIFICATION_BODY>'" \
  --es test_notif_pkg "'<PACKAGE_NAME>'"
```

> [!CAUTION]
> **Bash Escaping Rule**: You MUST wrap the inner argument in single quotes `"'...HK\$XX.XX...'"` and escape dollar signs (`\$`). If you write `"HK$28.00"`, bash will treat `$28` as a shell variable, erase it, and break the amount parser!

#### Curated Test Payloads

##### A. Smart Octopus (Transit, Convenience, Dining)
```bash
# 1. MTR Transit Fare ($6.50)
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'Smart Octopus HK\$6.50 MTR - Central'" \
  --es test_notif_text "'Card ending in 9821'" \
  --es test_notif_pkg "'com.octopuscards.octopus_app'"

# 2. 7-Eleven Convenience Store ($28.00)
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'Smart Octopus HK\$28.00 7-Eleven'" \
  --es test_notif_text "'Card ending in 9821'" \
  --es test_notif_pkg "'com.octopuscards.octopus_app'"

# 3. Starbucks Coffee ($45.00)
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'Smart Octopus HK\$45.00 Starbucks'" \
  --es test_notif_text "'Card ending in 9821'" \
  --es test_notif_pkg "'com.octopuscards.octopus_app'"
```

##### B. Google Wallet / Google Pay
```bash
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'Sushiro HK'" \
  --es test_notif_text "'金額：HK\$157.00；付款卡：AEON Mastercard Credit Card ••2101'" \
  --es test_notif_pkg "'com.google.android.apps.walletnfcrel'"
```

##### C. Banking Push Notification (HSBC / BOCHK / Hang Seng)
```bash
# HSBC Credit Card Purchase
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'Credit Card Transaction Alert'" \
  --es test_notif_text "'You paid HK\$120.00 at PARKnSHOP with card ending in 4421'" \
  --es test_notif_pkg "'com.hsbc.hbap.mobilebanking'"

# BOCHK UnionPay Transit Ticket
adb -s <SERIAL> shell am start -n com.example.vibefinance/.MainActivity \
  --es test_notif_title "'BOC Go UnionPay Diamond'" \
  --es test_notif_text "'15:00 Transit-ticket KOWLOON HKG HK\$3.60'" \
  --es test_notif_pkg "'com.bankofchina.bochk.mobileapplication'"
```

---

### Method 2: System Status Bar Notification Emulation (`cmd notification post`)

Use this method when verifying end-to-end OS-level interception through the live Android status bar.

#### Step 1: Post the Notification to Android Status Bar
```bash
adb -s <SERIAL> shell 'cmd notification post -t "Smart Octopus HK\$33.00 McDonald'"'"'s" octopus_tag_1 "Card ending in 9821"'
```

#### Step 2: (Optional) Expand Notification Shade to Inspect
```bash
# Expand shade
adb -s <SERIAL> shell cmd statusbar expand-notifications

# Take visual screenshot
adb -s <SERIAL> shell screencap -p /sdcard/shade.png && adb -s <SERIAL> pull /sdcard/shade.png .

# Collapse shade
adb -s <SERIAL> shell cmd statusbar collapse
```

> [!NOTE]
> When posted via `cmd notification post`, Android sets the source package to `com.android.shell`. VibeFinance's `PaymentNotificationListener` explicitly recognizes `com.android.shell` for automated testing and treats payment keywords accordingly.

---

## 4. Verifying Auto-Logging & The "Remember Choice" Feature

### 4.1 First Occurrence: Manual Account Selection & Memorization
When an unknown card or asset arrives:
1. `PendingPaymentChoiceDialog` appears with:
   - Formatted Amount (e.g. `HK$45.00`)
   - Merchant Name (e.g. `Starbucks`)
   - Detected Asset Hint (e.g. `Smart Octopus`)
   - Card Last 4 Digits (e.g. `卡號末四位：•••• 9821`)
2. The user selects a target account (e.g. `Wallet (Cash)`).
3. The checkbox appears:
   ```
   [✓] Remember for com.octopuscards.octopus_app alerts marked "Smart Octopus (•••• 9821)"
   ```
4. User taps **Record expense**.
5. The system performs two actions:
   - **Auto-binds `cardLast4`**: If the target account's `cardLast4` was null, it is automatically set to `"9821"`.
   - **Saves Remembered Rule**: Persists routing in `SharedPreferences` (`pending_payment_choices`).

### 4.2 Subsequent Occurrences: True Zero-Touch Auto-Logging
When the same card/wallet triggers a notification in the future:
1. `PendingPaymentStore.rememberedAccountId()` resolves the account via 3-tier hierarchy:
   - **Tier 1 (Highest)**: `$pkg|card:$cardLast4` (e.g. `com.octopuscards.octopus_app|card:9821`)
   - **Tier 2**: `$pkg|$hint|card:$cardLast4`
   - **Tier 3 (Fallback)**: `$pkg|$hint`
2. **Behavior**:
   - **No dialog is shown** (zero disruption to the user).
   - The transaction is recorded in SQLite/InMemoryDatabase immediately.
   - Account balance and daily budget are dynamically recalculated.
   - An Android system notification is posted: `Logged $xx.xx at <Merchant> to <AccountName>`.
   - The transaction appears in the **Activity History** tab with the auto-detected category (e.g. `Food & Dining`).

---

## 5. Automated Unit Test Verification

Always verify changes against the unit test suite before installing APKs or committing:

```bash
# Run all payment listener and store tests
./gradlew testDebugUnitTest --tests "com.example.vibefinance.service.PaymentNotificationListenerTest"

# Run full project test suite
./gradlew testDebugUnitTest
```

### Key Test Cases in `PaymentNotificationListenerTest.kt`
- `refundsAndCreditsAreNotSpending()`: Validates negative filters (reversals, cashbacks, refunds, payroll).
- `nonExpenseKeywordRejection()`: Validates OTP, promos, e-statements, and login alert rejection.
- `testExtractCardLast4()`: Validates deterministic regex extraction across Chinese and English card patterns.
- `testCanRememberChoiceWithCardLast4AndCash()`: Validates that matching card last-4 digits or cash accounts can be remembered.
- `bocGoUnionPayTransitTicketPurchaseKeepsTheCardName()`: Validates transit ticket parsing.

---

## 6. Common Pitfalls & Troubleshooting Checklist

| Symptom | Cause | Solution |
| :--- | :--- | :--- |
| **Notification sent via `am start` but nothing appears** | Bash expanded `$XX` as empty parameter | Wrap in single quotes: `"'...HK\$28.00...'"` |
| **Sent notification ignored / not queued** | Deduplication window hit (`15 minutes`) | Change amount, merchant, or advance timestamp: e.g. `$28.01` instead of `$28.00` |
| **Notification filtered out as non-expense** | Contains words like `Refund`, `OTP`, `Promo`, `Discount`, `退款`, `結單` | Ensure test title/text uses standard expense phrasing (`Paid`, `Spent`, `消費`, `支出`) |
| **Foreign currency alert ignored** | Contains `USD`, `EUR`, `RMB`, `JPY`, etc. | VibeFinance balances are in HKD. Only HKD local currencies (`HK$`, `$`, `HKD`) are auto-logged |
| **"No specific card was identified" message in dialog** | `canRememberChoice` evaluated to false | Ensure `cardLast4` is present in notification, or destination account is `AccountType.CASH` / matching name |
| **Multiple devices attached error** | ADB command missing `-s <serial>` | Always specify target device: `adb -s 192.168.240.112:5555 ...` |
