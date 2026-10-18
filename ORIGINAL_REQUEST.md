# Original User Request

## Initial Request — 2026-09-16T11:30:17Z

Comprehensive optimization of the VibeFinance Android application with Material 3 Expressive design, spring motion, dynamic shapes, and responsive layouts across all screens while strictly preserving all existing functionality and financial calculations.

Working directory: /home/ricky/Antigravity_project/finance_app
Integrity mode: development

## Requirements

### R1. Material 3 Expressive UI & Kinetic Elevation Overhaul
Upgrade the visual presentation across all app destinations (Daily Home, Assets & Accounts, Recurring Subscriptions, Transaction History, and bottom sheets/dialogs) using Material 3 Expressive standards:
- Apply expressive container tokens, tactile spring kinetics, asymmetric bento layouts, and organic shapes.
- Implement responsive layout adaptation to prevent any content or chip truncation on compact screen widths.
- Elevate typography, hierarchical contrast, and dynamic color harmonization throughout all views.

### R2. Strict Functional Parity & State Integrity
Preserve 100% of existing application functionality without behavioral or mathematical regression:
- Budget calculations (daily allowance, rollover allocation, spending ratio, pacing analytics) must produce identical financial outputs.
- Transaction operations (creating, editing, filtering, category tagging, deleting) must continue to function correctly.
- Recurring subscription tracking, payment method links, and notification listeners must remain intact.
- Persistent and in-memory databases must preserve all state across screen transitions.

### R3. Controlled Device Targeting
All local testing and execution on devices via ADB must strictly target the Waydroid emulator (`-s 192.168.240.112:5555`) and must never run commands against attached physical devices.

## Acceptance Criteria

### Build & Test Suite Verification
- [ ] `./gradlew assembleDebug` builds successfully with zero compilation errors.
- [ ] `./gradlew testDebugUnitTest` runs and passes all existing test suites without regression.
- [ ] `graphify update .` executes without errors to keep the codebase architecture index up to date.

### Functional Verification on Emulator
- [ ] App successfully installs (`adb -s 192.168.240.112:5555 install -r ...`) and boots into MainActivity.
- [ ] Bottom navigation smoothly switches between all 4 tabs (Daily, Assets, Recurring, History) with full interactivity intact.
- [ ] Action flows (Add Expense/Income sheet, Add Recurring Subscription sheet, Recalculate Budget sheet, Settings sheet) open, accept user input, save changes, and correctly reflect updated values in the UI.

### Material 3 Expressive Polish
- [ ] Motion transitions adhere to M3 Expressive motion specifications (spring kinetics for transforms and scale, linear/standard easing for alpha and colors).
- [ ] Cards, chips, and list items have appropriate touch target bounds, expressive corners, and no horizontal clipping.


## 2026-10-17T09:41:12Z

Refactor the Add Transaction page in VibeFinance into a Material 3 Expressive 2-Card Bento layout with an in-place expandable Category `FilterChip` tag cloud (with category icons/emojis, localized display-name deduplication, and auto-collapse on selection) and a full-width Selected Asset Card backed by a modal bottom sheet account picker.

Working directory: /home/ricky/Antigravity_project/finance_app
Integrity mode: development

## Requirements

### R1. Bento 2-Card Visual Grouping & Hero Header
Organize the transaction entry page into a unified Hero Header (Transaction Mode Switcher + centered 42sp Hero Amount + Currency conversion banner) followed by two structured Bento section cards (`RoundedCornerShape(22.dp)`):
- **Card 1 (Budget & Note)**: Daily Budget switch (in Expense mode), live Budget / Income Asset Boost / Transfer Balance Flow preview, and the Merchant/Note input field with a 48dp receipt scanner button.
- **Card 2 (Classification & Asset)**: Category `FilterChip` selector (in Expense/Income modes), Expense Icon selector (in Expense mode), and Asset/Account selector (plus Destination Account in Transfer mode).

### R2. Category FilterChips with In-Place Tag Cloud Expansion, Icons & Deduplication
- Provide a header-level expand/collapse toggle (`展開全部` / `收合`) next to the Category label that smoothly transitions (`animateContentSize`) between a single-row horizontally scrollable `LazyRow` (with scroll-state-aware `horizontalFadingEdge`) and a multi-row wrapped `FlowRow` tag cloud.
- Selecting a category chip while expanded automatically collapses the tag cloud back to the single-row `LazyRow` and scrolls the selected chip into view.
- Display a category icon or emoji on each Category `FilterChip` and deduplicate category choices that resolve to the same localized display name (e.g., `"Food"` and `"Food & Drink"` both resolving to `餐飲美食`) so duplicate labels never appear side by side.

### R3. Full-Width Selected Asset Card with Live Balance & Modal Bottom Sheet Picker
- Render a full-width Selected Asset Card (aligned with the Expense Icon row above it) showing the active account's icon, display name, account type, formatted live balance / credit card owing amount, and a trailing chevron.
- Tapping the Selected Asset Card (or Destination Account Card in Transfer mode) opens a standard Material 3 `AppModalBottomSheet` listing all selectable accounts with their live balances and an inline `+ 新增帳戶` creation flow, replacing the legacy floating `Popup` overlay on the transaction entry page.

### R4. Checklist & Verification Compliance
- Follow `modify.md`: bump `versionCode` and `versionName` once in `app/build.gradle.kts`, update `PROGRESS.md` and `README.md`, update affected tests, run `graphify update .` and `git diff --check`, and do not git commit or push.

## Acceptance Criteria

### UI/UX & Functional Verification
- [ ] Opening Add Expense, Income, or Transfer displays the Hero Header and 2 Bento section cards without any auto-opening popup covering the screen.
- [ ] Tapping the Category expand/collapse toggle transitions smoothly between the single-row `LazyRow` and multi-row `FlowRow` of `FilterChip`s, and selecting a chip in expanded mode auto-collapses to the single row and reveals the selected chip.
- [ ] Duplicate localized category labels (such as duplicate `餐飲美食`) do not appear simultaneously, and each category chip displays its category icon/emoji.
- [ ] Asset selection displays a full-width card with real-time formatted account balance and opens an `AppModalBottomSheet` account selector (with account creation support) on tap.
- [ ] `./gradlew assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin` succeeds with 0 errors.
- [ ] `graphify update .` and `git diff --check` complete cleanly.
