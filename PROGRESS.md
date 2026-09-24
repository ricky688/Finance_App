# VibeFinance Project Progress & Agent Handoff

This document details all recent features, architectural changes, modified files, and operational protocols for any AI agent or developer continuing development on **VibeFinance**.

---

## 1. Executive Summary & Current Status

VibeFinance is a modern personal finance Android application using Jetpack Compose, Material 3 Expressive design tokens, Room/`InMemoryDatabase`, Kotlin Coroutines/Flow, and MVI architecture.

### 2026-09-25: Connected Assets Group Switch Transition
- **Selection motion**: Kept the native Material 3 Expressive connected `ToggleButton` shape spring and added medium-low spring transitions for selected fill and content colors, a subtle spring scale on the icon/count, and a 32 dp accent that springs between the three choices. The accent uses direction-aware offset so it follows the buttons in right-to-left layouts. These animations run only when the selection changes.
- **List motion**: Account cards now use `LazyColumn.animateItem` with spring fade and placement when filtering adds, removes, or moves rows. Filtering and edit actions remain unchanged.
- **Verification**: `assembleDebug` and focused `AccountsScreenBentoTest` passed. Installed on Waydroid `192.168.240.112:5555`, switched All Assets → Cash & Bank → Credit Cards → All Assets, and reviewed the recorded transition and final screen states.
- **Design preference for future work**: Treat transitions between UI states as a primary part of each interaction. Prefer finite spring entry, exit, and selection motion that remains responsive to quick changes and settles when idle.

### 2026-09-25: Native Material 3 Expressive Connected Assets Group
- **Design**: Replaced the custom Assets & Cards selection dock with the Material 3 Expressive connected single-select button pattern: three native `ToggleButton`s, `ButtonGroupDefaults.ConnectedSpaceBetween`, and leading, middle, and trailing connected shapes. The buttons retain the app's colors, icons, live counts, and localized two-line labels. Material 3 now supplies the pressed and checked shape motion.
- **Behavior and access**: Preserved the `ALL`, `CASH_BANK`, and `CREDIT_CARDS` filters, equal-width layout, account list, and full-account net-worth summary. The group keeps radio-button and single-selection semantics.
- **Verification**: `assembleDebug` and focused `AccountsScreenBentoTest` passed. Installed on Waydroid `192.168.240.112:5555` and visually checked all three connected states: All Assets shows wallet, checking, and Amex; Cash & Bank shows wallet and checking; Credit Cards shows Amex only. Restored All Assets afterward.

### 2026-09-24: Assets Group Selector Redesign
- **Design**: Replaced the horizontally scrolling filter chips on Assets & Cards with a compact, full-width three-cell selection dock. Each cell keeps its group icon and live account count; the selected cell springs into a rounded `primaryContainer` surface with animated shape and color. All three choices remain visible on a phone without horizontal scrolling.
- **Behavior and access**: Preserved the `ALL`, combined `CASH_BANK`, and `CREDIT_CARDS` filters and the account list behavior. The net-worth summary still covers every account. Added single-choice accessibility semantics (`selectableGroup` and radio-button roles) and localized the three labels in English and Chinese resource sets.
- **Verification**: `assembleDebug` and the focused `AccountsScreenBentoTest` suite passed. Installed the APK on Waydroid `192.168.240.112:5555`; checked All Assets (3 accounts), Cash & Bank (wallet and checking), Credit Cards (Amex only), then restored All Assets.

### 2026-09-24: Daily Days Left Flat/Wavy Circular Indicator Optimization
- **Indicator**: Replaced the hand-drawn 360° track and sine-wave path with Material 3's determinate `CircularWavyProgressIndicator`. The existing tap toggles amplitude between `0f` (flat) and `1f` (wavy), using the component's built-in shape transition. The remaining-days ratio still uses its tested clamping helper and spring progress animation. The native indicator also exposes progress range semantics.
- **Idle cost and layout**: Set `waveSpeed = 0.dp` so the wavy shape rests without a perpetual animation; removed the old four-second phase animation and per-frame path math. The circle now sizes from the smaller available width/height, rather than card height alone. Moved the Flat/Wavy label above the number, away from the floating add button, and increased supporting-text contrast.
- **Thickness refinement**: Increased both the active and track strokes from Material 3's 4 dp default to 5 dp, preserving rounded caps and the existing gap. Rebuilt and installed the APK on Waydroid, then visually checked both Wavy and Flat states.
- **Verification**: `assembleDebug` passed and the APK was installed on Waydroid `192.168.240.112:5555`. Visually checked both Wavy → Flat and Flat → Wavy toggles and their settled labels/shapes. Three one-second idle samples on Daily showed 0% app main-thread and RenderThread CPU after the indicator settled.

### 2026-09-24: History Category Exit Motion and Flat Budget Progress
- **Category analytics**: Clearing a selected category now keeps its detail card composed through the spring shrink/fade exit, instead of removing its content immediately. The Focused chip springs closed horizontally, and the filter hint slides and fades back to the unfiltered text. Selection, chart highlighting, and transaction filtering retain their existing behavior.
- **Budget period**: Replaced the custom filled `Box` track with Material 3's determinate, flat `LinearProgressIndicator`. It keeps the existing spent/budget ratio, spring progress change, theme colors, and error color at 100%.
- **Verification**: `assembleDebug` passed; installed the APK on Waydroid `192.168.240.112:5555`. Checked History's partial budget fill, selected Electronics, and cleared the category; the detail, chip, chart, and list returned to their expected states. Updated the Graphify code graph.

### 2026-09-24: Waydroid CPU/Battery Idle Animation Fix
- **Measured issue**: On the Daily page of the debug build, Waydroid showed approximately 85–96% CPU on the app main thread plus 28–44% on RenderThread while untouched. Both threads fell to 0% in short samples when the app was backgrounded or History was open, isolating the load to foreground Daily visuals.
- **Causes**: `HeroDailyBudgetCard`, `RestAndSpentBudgetCard`, and `DaysLeftCard` ran perpetual wave transitions. The recalculation sheet also had an unconditional frame loop and recreated 160 confetti particles whenever the previous batch disappeared, so its celebration could never finish.
- **Changes**: Daily waves now play one 4–5 second entrance cycle and settle. Hero and remaining-budget wave state is read in the graphics layer instead of recomposing each card's labels every frame. Confetti is created once after measuring the canvas, updated only while particles exist, and is no longer mutated from the Canvas draw pass.
- **Verification**: `assembleDebug` passed and the APK was installed on Waydroid `192.168.240.112:5555`. After the entrance cycle, three one-second Daily samples showed 0% main/RenderThread CPU; three further samples with the recalculation sheet open after confetti ended also showed 0%. The Daily layout and sheet were visually checked on device. These numbers are Waydroid debug-build samples, not a battery-life estimate for physical phones.

### 2026-09-24: History List Motion Parity and Daily Net Fix
- **History row motion**: Replaced the History transaction row's `SwipeActions` shell with `ExpressiveSwipeRow`, matching Recurring subscription rows' 26/8 dp grouped corners, 5 dp gaps, two-direction action canvas, 40% drag threshold, haptic ticks, spring icon/backdrop pop, 4 dp floating elevation, and spring return. History uses the same spring collapse/placement motion for deletion. Tapping or swiping right still opens the transaction editor; swiping left or tapping the existing delete icon still deletes after the exit animation.
- **Yesterday total**: The old footer counted only positive, non-transfer budget expenses. Seeded Yesterday contains a `-HK$200` internal transfer and `+HK$2,500` salary, so it incorrectly displayed HK$0. The footer is now a signed **Daily Net** of the visible rows, including transfers; Yesterday displays **+HK$2,300.00**. Budget spending calculations remain separate. Updated English and Chinese locale strings.
- **Verification**: Built and installed the debug app on Waydroid (`192.168.240.112:5555`), visually confirmed Yesterday's two rows and corrected net, and confirmed right swipe still opens the edit dialog. Four focused `HistoryScreenTest` instrumentation tests pass on Waydroid, covering short-swipe abort, right-swipe edit, left-swipe delete/undo, and the income-plus-transfer net regression. ARTEMIS diagnosed as ready but its longer initial UI inspection timed out; ADB and device screenshots completed verification.

### 2026-09-24: Subscription Deletion: Past Payment Records Decision Flow
- **Problem**:
  - When users deleted a recurring subscription (e.g. ChatGPT Plus HK$ 160.00, Netflix Premium HK$ 93.00), the app previously deleted only the subscription rule without consulting the user about what should happen to the paid transactions already logged in the system.
  - Users who paid for a subscription in the past wanted the option to keep their historical expense records (preserving spending analytics, account balances, and budget reports) or explicitly purge them alongside the subscription rule.
- **Architectural Solution & Implementation**:
  1. **Past Payment Record Grounding & Matching Engine**:
     - Built [`findMatchingTransactionsForSubscription(sub, transactions)`](file:///home/ricky/Antigravity_project/finance_app/app/src/main/java/com/example/vibefinance/ui/recurring/RecurringScreen.kt#L4152-L4163):
       - Identifies all debit transactions associated with the target subscription.
       - Matches system auto-charges (`"Auto-charge: ${sub.name}"`) and manual entries (`tx.description.contains(sub.name, ignoreCase = true)`).
       - Filters out transfers (`toAccountId == null`) to avoid accidental transfer deletions.
     - Extracted to shared UI utility used identically across `RecurringScreen.kt`, `FinanceViewModel.kt`, and unit tests.
  2. **Material 3 Expressive Confirmation Dialog (`DeleteSubscriptionConfirmDialog`)**:
     - Designed and integrated into `RecurringScreen.kt`:
       - Shows subscription metadata (name, amount, frequency, linked account).
       - When past records exist: displays a summary banner indicating the exact record count and total sum (e.g., *已找到 1 筆過往扣款紀錄，共計 HK$ 160.00*).
       - **Option 1 ("保留付款紀錄 (推薦)")**: Cancels future auto-charges and deletes the recurring rule, while preserving all past transactions in history and reports.
       - **Option 2 ("連同付款紀錄一併刪除")**: Purges both the recurring rule and all matching historical debit transactions, automatically reversing account balances.
       - **Option 3 ("取消")**: Dismisses the dialog safely with no mutations; swiped rows spring back smoothly to resting state.
       - Fallback for 0 past records: Displays a clean single confirmation prompt.
  3. **ViewModel & State Modernization**:
     - Updated `FinanceIntent.DeleteSubscription(val sub: SubscriptionEntity, val deletePastTransactions: Boolean = false)`.
     - In `FinanceViewModel.kt`, when `deletePastTransactions == true`, iterates and removes matching transactions via `transactionRepository.deleteTransaction(tx)` (which reverses account balances) and emits informative toast messages with counts and formatted totals.
     - Fixed an edge-case bug where `FinanceIntent.SeedMockData` previously did not clear `isLoading = false` on success.
  4. **Seeded Realistic Past Payment Data**:
     - Seeded Tx 7 in `DataSeeder.kt`: ChatGPT Plus auto-charge of HK$ 160.00 on Amex Credit Card (18 days ago), ensuring instant demonstration out of the box.
  5. **Localization**:
     - Added 9 localized strings across all 5 locale files (`values/strings.xml`, `values-b+zh+Hant/`, `values-zh-rHK/`, `values-zh-rTW/`, `values-zh/`).
  6. **Automated Unit Testing & Verification**:
     - Added 3 unit tests in `RecurringScreenBentoTest.kt` verifying exact matching, case-insensitivity, transfer exclusion, and blank-name edge cases (77/77 tests passing).
     - Verified end-to-end on Waydroid emulator (`192.168.240.112:5555`):
       - ChatGPT Plus: Kept past record -> HK$ 160.00 confirmed in History tab under `軟體 / AI`.
       - Netflix Premium: Deleted past record -> HK$ 15.49 purged from History tab, total expense adjusted correctly.
       - Cancel: Card sprang back smoothly with zero visual anomalies.

### 2026-09-24: Gmail-Style Swipe-To-Action Animation on Recurring Page (`RecurringScreen.kt`)
- **Problem**:
  - The previous swipe-to-action implementation on `RecurringScreen.kt` utilized an experimental "peeling card physics" model (`peelRotationZ`, `peelRotationY`, `peelScaleX`, `peelScaleY`, `cameraDistance = 14f`).
  - Swiping a subscription card or installment plan caused the card to distort, tilt into the screen in 3D, warp its corner radii, and violently sway and rotate neighboring cards (`neighborTranslationX`, `neighborRotationZ`).
  - The user requested replacing this with the clean, tactile, and standard **Gmail swipe-to-action** animation.
- **Architectural Solution & Implementation**:
  1. **Clean Horizontal Translation & Floating Card Elevation**:
     - Removed all 3D rotation (`peelRotationY = 0f`, `peelRotationZ = 0f`), scale warping (`scaleX = 1f`, `scaleY = 1f`), and neighbor card swaying (`neighborTranslationX = 0f`, `neighborRotationZ = 0f`).
     - Foreground cards now slide with natural 1:1 finger tracking strictly along the X-axis (`translationX = currentOffset`).
     - Added dynamic floating card shadow elevation: lifts from `0.dp` to `4.dp` (`cardElevation`) when swiping begins, separating the moving card from the background canvas.
  2. **Gmail-Style Action Background Canvas**:
     - Behind the moving card, the action background fills the card boundary with the item's stable grouped shape (`itemShape`, no corner morphing).
     - **Swipe Left (Delete)**:
       - Morphs smoothly between `errorContainer` (soft red, pre-threshold) and `error` (saturated red, post-threshold) via `animateColorAsState(tween(200))`.
       - Trash icon (`Icons.Default.Delete`) anchored at `Alignment.CenterEnd` with `24.dp` margin.
     - **Swipe Right (Edit / Manage)**:
       - Morphs smoothly between `tertiaryContainer` and `tertiary`.
       - Pencil edit icon (`Icons.Default.Edit`) anchored at `Alignment.CenterStart` with `24.dp` margin.
  3. **Signature Gmail Kinetic Icon Pop & Circular Backdrop Indicator**:
     - **Dynamic Spring Pop**: When passing the activation threshold (~38% of card width), the action icon dynamically springs up from `1.0f` to `1.25f` using `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)`.
     - **Circular Backdrop Disc**: Behind the 24dp icon, a 44dp circular capsule (`CircleShape`) scales up with a spring bounce and highlights with translucent contrast (`Color.White.copy(alpha = 0.22f)`), matching Gmail's Material 3 action cue.
     - **Tactile Haptic Ticks**: Fires `HapticFeedbackType.LongPress` upon crossing threshold, and `HapticFeedbackType.TextHandleMove` when pulling back below threshold.
  4. **Release Physics & Vertical Collapse**:
     - **Delete (Past Threshold)**: Swiping left flings cleanly off-screen (`animateTo(-screenWidthPx * 1.35f, tween(200, FastOutLinearInEasing))`), followed by smooth vertical collapse (`shrinkVertically` + `fadeOut`), triggering `onDelete()`.
     - **Edit (Past Threshold)**: Swiping right springs cleanly back to `0.dp` (`spring(Spring.DampingRatioMediumBouncy)`) and opens the edit modal (`onClick()`).
     - **Release Before Threshold**: Springs back elastically to `0.dp`.
  5. **Parity Across Both Models**:
     - Applied identically to both `SubscriptionRowItem` and `InstallmentRowItem`.
- **Live Verification (Waydroid `192.168.240.112:5555`)**:
  - Unit tests passed cleanly (`./gradlew testDebugUnitTest` - `BUILD SUCCESSFUL in 1s`, 74/74 tests passing).
  - Built and installed debug APK.
  - Verified live mid-swipe states with deterministic `input motionevent`:
    - Swipe Left (Delete) active state: `gmail_swipe_left_active.png`.
    - Swipe Right (Edit) active state: `gmail_swipe_right_active.png`.
    - Installment plan swipe left: `gmail_swipe_installment_left.png`.
    - Full swipe-to-delete dismissal and vertical collapse: `gmail_swipe_delete_result.png`.
    - Light mode swipe left: `gmail_swipe_light_mode.png`.

### 2026-09-24: Unified 4-Page Financial Chart Color System (`ChartColors.kt`)
- **Problem**:
  - The color schemes across the four primary tabs (`每日` Daily, `資產` Assets, `定期` Recurring, `紀錄` History) were fragmented and inconsistent:
    - Daily (`HomeScreen.kt`) had a localized semantic color map.
    - Recurring (`RecurringScreen.kt`) used an isolated hardcoded palette where colors did not match Daily.
    - History (`CategoryBreakdownCard.kt`) used an arbitrary 6-color static list mapped by `index % 6`, causing identical categories (e.g., Electronics, Groceries) to swap colors arbitrarily depending on transaction ordering or date ranges.
    - Assets (`MoneySeparationChart.kt`) used separate ad-hoc hex literals for cash, bank, and debt.
- **Architectural Solution & Implementation**:
  1. **Centralized Palette Authority (`theme/ChartColors.kt`)**:
     - Built a unified `ChartColors` singleton providing the single source of truth for all financial breakdown charts across the app.
     - **`UNIFIED_PALETTE`**: 12-color high-contrast, wide-spectrum chromatic palette:
       - Royal Purple (`0xFF8B5CF6`) · Emerald Mint (`0xFF10B981`) · Ocean Blue (`0xFF3B82F6`) · Warm Amber (`0xFFF59E0B`)
       - Hot Rose (`0xFFEC4899`) · Cyan (`0xFF06B6D4`) · Coral Orange (`0xFFF97316`) · Indigo (`0xFF6366F1`)
       - Sunflower Gold (`0xFFEAB308`) · Bright Teal (`0xFF14B8A6`) · Golden Amber (`0xFFFBBF24`) · Coral Crimson (`0xFFF43F5E`)
     - **Deterministic Category Semantic Mapping (`getSemanticCategoryColor`)**:
       - Both English & Chinese keys consistently resolve to the same color:
         - **Electronics / 數碼電器 / 3C / 數碼**: Ocean Blue (`0xFF3B82F6`)
         - **Groceries / 超市雜貨 / 生鮮 / 日用**: Emerald Mint (`0xFF10B981`)
         - **Entertainment / 休閒娛樂 / 娛樂 / 影音**: Royal Purple (`0xFF8B5CF6`)
         - **Food & Drink / 餐飲美食 / 餐飲 / 飲食**: Coral Orange (`0xFFF97316`)
         - **Shopping / 購物消費 / 購物 / 服飾**: Hot Rose (`0xFFEC4899`)
         - **Transport / 交通出行 / 交通 / 通勤**: Golden Amber (`0xFFFBBF24`)
         - **Utilities / 生活水電 / 水電 / 帳單 / 繳費**: Coral Crimson (`0xFFF43F5E`)
         - **Software / AI / 軟體訂閱 / 訂閱**: Bright Teal (`0xFF14B8A6`)
         - **Health / 醫療健康 / 健身 / 運動**: Cyan (`0xFF06B6D4`)
         - **Housing / 房屋居住 / 房租 / 房貸**: Indigo (`0xFF6366F1`)
         - **Investment / 投資理財 / 理財 / 儲蓄**: Sunflower Gold (`0xFFEAB308`)
     - **Two-Pass Allocation Guarantee (`buildCategoryColorMap`)**:
       - Guarantees zero color collisions even when mixing recognized semantic categories with user-created custom categories.
       - Blends 12% dynamic primary tint for organic cohesion with active theme while preserving 88% spectral distinction.
     - **Unified Asset Type Segment Tokens (`getAssetSegmentColor`)**:
       - Harmonizes asset types: Cash (`0xFF10B981`), Bank Accounts (`0xFF3B82F6`), Debt/Credit (`0xFFF43F5E`).
  2. **Page 1: 每日 (Daily / `HomeScreen.kt`)**:
     - `CategoryDonutChart` consumes `ChartColors.buildCategoryColorMap()`.
     - Electronics = Ocean Blue, Groceries = Emerald Mint, Entertainment = Royal Purple, Food = Coral Orange.
  3. **Page 2: 資產 (Assets / `MoneySeparationChart.kt` & `AccountsScreen.kt`)**:
     - `InteractiveDonutChart` and `MoneySeparationChart` consume `ChartColors.getAssetSegmentColor("cash")`, `getAssetSegmentColor("bank")`, `getAssetSegmentColor("debt")`.
  4. **Page 3: 定期 (Recurring / `RecurringScreen.kt`)**:
     - `RecurringCategoryDonutChart` consumes `ChartColors.buildCategoryColorMap()`.
     - Entertainment = Royal Purple, Software/AI = Bright Teal / Emerald Mint, Electronics = Ocean Blue, Utilities = Warm Amber.
  5. **Page 4: 紀錄 (History / `CategoryBreakdownCard.kt` & `HistoryScreen.kt`)**:
     - Replaced index-based 6-color list with `remember(categoryTotals, isDark, primaryColor) { ChartColors.buildCategoryColorMap(...) }`.
     - Stacked bar segments, interactive popover cards, and legend rows now display the exact same colors and dynamic category tinting as Page 1.
- **Verification**:
  - All 74 unit tests passed cleanly (`./gradlew testDebugUnitTest` in 26s).
  - Built debug APK (`assembleDebug` in 13s) and deployed to Waydroid emulator (`192.168.240.112:5555`).
  - Captured live screenshots across all 4 pages:
    - Page 1 Daily: `unified_page1_daily.png`
    - Page 2 Assets: `unified_page2_assets.png`
    - Page 3 Recurring: `unified_page3_recurring.png`
    - Page 4 History (Normal & Selected): `unified_page4_history.png`, `unified_page4_history_selected.png`

### 2026-09-24: Settings Sheet Dismissal Toast Fix & Daily Donut Chart Categorical Color Overhaul
- **Settings Sheet Dismissal Toast Elimination (`SettingsSheet.kt` & `FinanceViewModel.kt`)**:
  - **Problem**: Whenever closing `SettingsSheet` (via either the top-right `X` button, dragging the bottom sheet down, or pressing the system back key), a toast saying `"Custom period budget set successfully!"` was triggered unconditionally.
  - **Root Cause**: `SettingsSheet.kt:saveChanges()` was executed unconditionally during dismissal, dispatching `FinanceIntent.SetCustomPeriodBudget` whenever budget amount and end date were non-null, regardless of whether any settings had actually been modified. In `FinanceViewModel.kt`, `SetCustomPeriodBudget` emitted `FinanceUiEvent.ShowToast("Custom period budget set successfully!")` on every invocation.
  - **Fix**:
    1. Added `val showToast: Boolean = false` parameter to `FinanceIntent.SetCustomPeriodBudget`.
    2. Updated `FinanceViewModel.kt` to only emit `FinanceUiEvent.ShowToast` when `intent.showToast == true`. User-confirmed flows (e.g. `NewPeriodBudgetSheet` in `MainScreen.kt`) explicitly pass `showToast = true`.
    3. In `SettingsSheet.kt:saveChanges()`, added change-detection diff checks comparing `amt` with `budgetInfo.totalMonthlyBudget`, `selectedEndDateMillis` with `budgetInfo.endDate`, and `selectedRolloverMode` with `budgetInfo.rolloverMode`. Only dispatches if values have changed, and passes `showToast = false` to guarantee quiet dismissal.
  - **Live Verification**:
    - Opened `SettingsSheet` and closed via `X` button: verified NO toast appears (`settings_closed_no_toast.png`).
    - Opened `SettingsSheet` and dismissed via system Back key: verified NO toast appears (`settings_closed_back_no_toast.png`).

- **Daily Donut Chart Categorical Color Palette Overhaul (`HomeScreen.kt`)**:
  - **Problem**: In `CategoryDonutChart` on the daily home screen, categories (e.g. Electronics, Groceries, Entertainment) all displayed in nearly identical pale cyan/teal tones (`home_donut_actual.png`), making visual classification impossible.
  - **Root Cause**: `CategoryDonutChart` previously used `MaterialTheme.colorScheme` tokens (`primary`, `secondary`, `tertiary`, `inversePrimary`, `primaryContainer`, `secondaryContainer`, `tertiaryContainer`). In Material 3 dynamic themes, `primary`, `inversePrimary`, and `primaryContainer` share the exact same hue angle; `secondary` and `secondaryContainer` also share the same hue, collapsing category separation.
  - **Fix**:
    1. Implemented `getDailySemanticCategoryColor(category: String, isDark: Boolean): Color?` mapping English, Traditional Chinese, and common synonyms to distinct semantic hues:
       - **Electronics / 數碼電器**: Sky Blue (`0xFF38BDF8` dark, `0xFF0284C7` light)
       - **Groceries / 超市雜貨**: Vibrant Emerald Green (`0xFF34D399` dark, `0xFF059669` light)
       - **Entertainment / 休閒娛樂**: Vivid Royal Purple (`0xFFA855F7` dark, `0xFF7C3AED` light)
       - **Food & Drink / 餐飲美食**: Warm Coral Orange (`0xFFFB923C` dark, `0xFFEA580C` light)
       - **Shopping / 購物消費**: Hot Rose Pink (`0xFFF472B6` dark, `0xFFDB2777` light)
       - **Transport / 交通出行**: Golden Amber (`0xFFFBBF24` dark, `0xFFD97706` light)
       - **Utilities / 生活水電**: Crimson Red (`0xFFF87171` dark, `0xFFDC2626` light)
       - **Software & Subscriptions**: Electric Cyan (`0xFF22D3EE` dark, `0xFF0891B2` light)
       - **Health & Fitness**: Crisp Mint (`0xFF2DD4BF` dark, `0xFF0D9488` light)
       - **Housing / 房屋居住**: Royal Indigo (`0xFF818CF8` dark, `0xFF4F46E5` light)
       - **Investment & Finance**: Sunflower Gold (`0xFFEAB308` dark, `0xFFCA8A04` light)
    2. Implemented `getDailyFallbackPalette(isDark: Boolean)` with a 10-color wide-spectrum chromatic cycle for custom/arbitrary user categories.
    3. Implemented `buildDailyCategoryColorMap(categories, isDark, primaryColor)` with Material Design 3 dynamic color harmonization: subtly blends 12% of `MaterialTheme.colorScheme.primary` into each hue so the chart naturally matches the user's active dynamic theme while preserving 88% spectral hue distinction without collisions.
    4. Calibrated luminance for both Dark Obsidian mode and Light theme.
  - **Live Verification**:
    - Dark mode donut chart: Electronics (Sky Blue), Groceries (Emerald Green), Entertainment (Purple), Food & Drink (Coral Orange) clearly distinguished in both donut arcs and legend (`home_donut_new_palette.png`).
    - Legend interaction: Tapped "Groceries", verified spring expansion of green slice and highlighted emerald legend container (`home_donut_groceries_selected.png`).
    - Light mode donut chart: Verified high contrast and crisp visibility of all categories against light surfaces (`home_donut_light_palette.png`).

### 2026-09-24: Icon Shape Personalization & Global Dynamic Container Shape System
- **Context & Problem Solved**:
  - In `SettingsSheet.kt` under "自訂" (Customization), Item 4 ("圖示形狀" / Icon Shape: "在卡片的前導圖示下方新增具有所選形狀的容器", inspired by Image Toolbox) was previously a static non-clickable card without state persistence, view model dispatch, or a shape selection interface.
  - The leading icon containers across `SettingsSheet.kt`, `AppearancePickerSheet.kt`, `RecurringScreen.kt`, and `HistoryScreen.kt` were either hardcoded to `CloverIconShape` or static `RoundedCornerShape(14.dp)`.
- **Architectural Implementation**:
  1. **Shape Tokens & CompositionLocal (`theme/Shape.kt`)**:
     - Defined `IconShapeMode` enum containing 8 expressive geometric shapes: `CLOVER` (四葉草), `SQUIRCLE` (平滑方角), `ROUNDED_SQUARE` (圓角矩形), `CIRCLE` (圓形), `SCALLOP` (十二瓣花), `FLOWER` (八葉花), `DIAMOND` (寶石菱形), `STAR` (八角星芒).
     - Provided localized bilingual titles and descriptions (`titleZh`, `subtitleZh`, `titleEn`, `subtitleEn`, `localizedTitle()`, `localizedSubtitle()`).
     - Declared `LocalIconShape = compositionLocalOf<Shape> { CloverIconShape }`.
  2. **Theme Hierarchy (`theme/Theme.kt` & `MainActivity.kt`)**:
     - `VibeFinanceTheme` accepts `iconShape: androidx.compose.ui.graphics.Shape = CloverIconShape` and provides it globally via `CompositionLocalProvider(LocalIconShape provides iconShape)`.
     - `MainActivity.kt` passes `iconShape = state.iconShape.shape` to `VibeFinanceTheme`, enabling automatic reactive shape propagation across the entire Compose tree.
  3. **MVI Architecture & Persistence (`FinanceViewModel.kt`)**:
     - Added `iconShape: IconShapeMode = IconShapeMode.CLOVER` to `FinanceUiState`.
     - Added `FinanceIntent.SetIconShape(val shape: IconShapeMode)` to `FinanceIntent`.
     - Restored `savedIconShape` from `SharedPreferences("vibe_finance_prefs")` in `loadData()` during initialization.
     - Handled `SetIconShape` in `dispatch()`: updates state, persists the enum name to `vibe_finance_prefs`, and emits a confirmatory feedback event.
  4. **Material 3 Expressive Shape Picker (`IconShapePickerDialog.kt` & `SettingsSheet.kt`)**:
     - Created `IconShapePickerDialog` displaying all 8 shapes with tactile haptic feedback, real-time live geometric shape previews with outline/check indicators, localized titles/subtitles, and active radio indicators.
     - Wired Item 4 in `SettingsSheet.kt` to be clickable, displaying the selected shape name in the subtitle and live shape preview container on the right.
     - Integrated `PredictiveBackHandler` for smooth gesture dismissal.
  5. **Global Container Shape Propagation**:
     - Refactored `SettingsSheet.kt` (expandable card headers and all customization items), `AppearancePickerSheet.kt` (header and all 4 customization items), `HistoryScreen.kt` (transaction item leading avatars), and `RecurringScreen.kt` (recurring subscription cards, installment plans, and preset avatars) to use `LocalIconShape.current`.
- **Live Device Verification (Waydroid `192.168.240.112:5555`)**:
  - Unit tests passed cleanly (`BUILD SUCCESSFUL in 23s`).
  - Assembled and deployed debug APK.
  - Tapped Item 4, opened `IconShapePickerDialog`, and tested switching across shapes (`CLOVER` -> `SCALLOP` -> `DIAMOND`).
  - Verified live UI updates:
    - Settings sheet items & headers immediately transformed into the 12-lobed Scallop shape (`settings_sheet_scallop.png`).
    - Settings sheet and Appearance sheet items immediately transformed into faceted Diamond shape (`settings_sheet_diamond.png`, `appearance_sheet_diamond.png`).
    - Recurring subscription cards transformed into Diamond shape (`recurring_screen_cards_diamond.png`).
    - History transaction items transformed into Diamond shape (`history_screen_diamond_scrolled.png`).
    - Force-stopped app and restarted to verify persistence across app lifecycle (`settings_persisted_diamond.png`).

### 2026-09-24: Second Top Bar Two-Line Separated Spring Transitions & Material 3 Expressive Switcher Spec Parity
- **Second Top Bar Separated Two-Line Spring Kinetics (`MainScreen.kt`)**:
  - Separated the animated transitions for Line 1 (primary title, e.g. `財務總覽`) and Line 2 (supporting subtitle, e.g. `Vibe Overview · 每日收支`).
  - Instead of a single monolithic container block, each line is orchestrated with its own independent `AnimatedContent`:
    - **Line 1 (Primary Title)**: Energetic, high-impact vertical spring translation (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessMediumLow`) arriving with a crisp tactile bounce.
    - **Line 2 (Supporting Subtitle)**: Cascading, delayed softer spring kinetics (`delayMillis = 65`, `Spring.DampingRatioLowBouncy`, `Spring.StiffnessLow`, `FastOutSlowInEasing`) flowing gracefully into position beneath the primary title.
- **Material 3 Expressive Switcher Specification Parity (`ExpressiveSwitch.kt`)**:
  - Brought `ExpressiveSwitch` into 100% visual parity with the official Material 3 specification image:
    - **OFF State**: Solid 2dp outline border (`MaterialTheme.colorScheme.outline`), filled container track (`surfaceContainerHighest`), 24dp thumb pill (`outline`), and centered `Icons.Filled.Close` (`✕`) cross icon tinted with `surfaceContainerHighest`.
    - **ON State**: 24dp `onPrimary` thumb pill with centered `Icons.Filled.Check` (`✓`) checkmark icon, filled `primary` track with seamless border transition.
    - **Press Dynamics**: Added `interactionSource.collectIsPressedAsState()` with 28dp horizontal stretch on press and spring snap back upon release.
    - **Icon Crossfade & Scale**: Spring-powered `scaleIn` + `fadeIn` together with `scaleOut` + `fadeOut` between `✓` and `✕` states.
    - Verified across both Dark Obsidian mode and Light mode on Waydroid emulator.

### 2026-09-24: XML Audit & Pure Jetpack Compose Brand Vector Implementation
- **Repository-Wide XML Audit**:
  - **Zero XML UI Layouts**: Confirmed that `app/src/main/res/layout/` contains **0 layout files**. The application UI is 100% Jetpack Compose across all screens (`HomeScreen`, `HistoryScreen`, `AccountsScreen`, `RecurringScreen`, `RadarScreen`), dialogs, bottom sheets (`SettingsSheet`, `RecalcBudgetSheet`), bento grids, and navigation. Zero ViewBinding, DataBinding, or legacy `AndroidView` interop exists.
  - **Platform-Mandatory XMLs**: Analyzed necessary OS-level XML files required by Android platform architecture:
    1. `AndroidManifest.xml`: OS package manifest declaring application components, services, and system permissions.
    2. `res/mipmap-anydpi-v26/ic_launcher*.xml`: OS launcher icon wrappers queried by the Android OS home screen launcher process prior to JVM/runtime boot.
    3. `res/values/themes.xml`: Base platform window style (`Theme.VibeFinance` extending `android:Theme.Material.Light.NoActionBar`) required for pre-Compose window decor during cold startup.
    4. `res/values/strings*.xml`: Standard Android resource string tables supporting localized strings in English and Chinese, consumed idiomatically via Compose's `stringResource(R.string.*)`.
    5. `res/xml/backup_rules.xml` & `data_extraction_rules.xml`: Android 12+ backup descriptors for OS data safety.
    6. `buckwheat/`: Isolated external reference repository containing legacy XML resources; excluded from Gradle compilation (`settings.gradle.kts` only includes `:app`).
- **Compose Brand Vector Implementation (`AppBrandIcon.kt`)**:
  - Re-implemented the XML vector asset (`ic_launcher_foreground.xml`) into a pure Jetpack Compose Canvas component `VibeFinanceIcon(modifier, size, showBackground, shapeRadius)`.
  - Accurately renders the ambient shadow, the stylized geometric 'V' lettermark with emerald linear gradient (`#34D399` -> `#059669`), the upward growth sparkline overlay with round caps/joins, and the glowing coin capital node.
  - Integrated `VibeFinanceIcon` into `ExpressiveCollapsingTopBar` in `MainScreen.kt`, replacing the generic placeholder `Savings` icon with the official native Compose brand icon.
- **Verification**:
  - All unit tests pass cleanly: `./gradlew testDebugUnitTest` (`BUILD SUCCESSFUL in 8s`).
  - Assembled and deployed to Waydroid emulator (`192.168.240.112:5555`).
  - Captured live screenshot (`home_topbar_compose_icon.png`) verifying the sharp Compose-rendered brand badge in the top bar.
  - Updated codebase knowledge graph via `graphify update .`.

### 2026-09-24: Full-Width Connected Button Group, Predictive Back Navigation, and Charts & Analytics Interactivity
- **Full-Width Connected Group Buttons (`BudgetPeriodIndicatorCard.kt`)**:
  - Fixed the 3 filter buttons (`[全部記錄]`, `[當前週期]`, `[過往週期]`) in the budget period indicator card shown in `media_1790221231822.png`.
  - Changed `isScrollable = true` to `isScrollable = false` in `BudgetPeriodIndicatorCard.kt`.
  - In `MainScreen.kt` (`ExpressiveSegmentedButtonGroup`), `isScrollable = false` eliminates the dark left fading edge shadow artifact (`horizontalFadingEdge`) and applies `Modifier.weight(1f)` with responsive horizontal padding, causing all 3 buttons to extend evenly across 100% of the card width to the right edge.
- **Predictive Back Navigation (`PredictiveBackHandler`)**:
  - **OS-Level Opt-In**: Enabled `android:enableOnBackInvokedCallback="true"` on `<application>` in `AndroidManifest.xml` for Android 13+ / 14 / 15 / 16 predictive back system gesture support.
  - **Top-Level Tab Predictive Back (`MainScreen.kt`)**: Implemented `PredictiveBackHandler` across secondary tabs (`History`, `Recurring`, `Assets`, `Radar`). Swiping back smoothly drives `tabBackProgress` with spring kinetics (`Spring.StiffnessMediumLow`), scaling down the content (`scaleX = 1f - progress * 0.08f`) and morphing corner radius dynamically before returning to `TabItem.HOME`.
  - **FAB Menu Predictive Back (`MainScreen.kt`)**: Intercepts back gestures when the Floating Action Button menu is expanded to smoothly collapse it.
  - **Category Filter & Edit Modal Predictive Back (`HistoryScreen.kt`)**: Swiping back with an active category filter or transaction edit modal clears the filter / closes the modal first without unexpectedly popping the screen.
  - **Nested Bottom Sheets & Dialogs (`SettingsSheet.kt`)**: Added reverse-order `PredictiveBackHandler` handlers for `AppearancePickerSheet`, reset confirmation dialog, and allowed apps dialog.
- **Charts & Analytics Interactivity**:
  - **Home Line Chart (`DailySpendingLineChart.kt`)**:
    - **Dual Mode Toggle**: Added Material 3 Expressive pill toggle in the header: `[累計 📈]` (`CUMULATIVE`) and `[單日 📊]` (`DAILY`).
    - **Daily Spending Mode**: Smoothly calculates and plots single-day spending trajectories with vertical translucent day pillars, bezier line curves, and dashed daily average spending guide line.
    - **Interactive Scrubbing & Floating Tooltip Badge**: Implemented continuous touch and drag gesture tracking with dynamic crosshairs, pulsating glow nodes, rolling numbers, and an expressive floating tooltip pill badge that glides horizontally above the touched node in real time.
    - **Haptic Feedback**: Fires crisp `TextHandleMove` haptic ticks as the user scrubs across day boundaries.
  - **History Category Breakdown Chart (`CategoryBreakdownCard.kt`)**:
    - **Continuous Drag Scrubbing**: Added `detectDragGestures` and `detectTapGestures` to the multi-segment stacked progress bar. Sliding a finger across the segments calculates the touched category via cumulative fraction thresholds in real time.
    - **Spring Segment Dynamics**: Touched/active category segments dynamically expand in height (`24.dp`) and scale with bouncy spring physics (`Spring.DampingRatioMediumBouncy`), while inactive segments dim cleanly.
    - **Interactive Category Summary Card**: Expands an M3 detail card directly below the bar displaying category color dot, localized name, transaction count (`%d 筆消費`), total amount, and percentage of total expenses.
    - **Filter Integration**: Tapping or scrubbing pins the category filter to immediately update the transaction list below; swiping back with predictive back gracefully dismisses the filter.
- **Verified**: 100% unit tests passing (`./gradlew testDebugUnitTest`), built debug APK (`assembleDebug`), installed on Waydroid emulator (`192.168.240.112:5555`), and captured verification screenshots across History, Daily spending modes, drag scrubbing, and predictive back navigation.

### 2026-09-24: Transitional Animation for Color Schemes & Material 3 Expressive Switcher
- **Transitional Color Scheme Animation (`animateColorSchemeAsState`)**:
  - Implemented `animateColorSchemeAsState` in `theme/Theme.kt` using Material 3 Expressive spring physics (`Spring.DampingRatioNoBouncy`, `Spring.StiffnessLow`).
  - Animates all 30+ semantic color tokens (`primary`, `surface`, `surfaceContainer`, `outline`, etc.) simultaneously. When switching palettes (e.g., from Mint to Sunset, Amber, Violet, Rose, or Ocean), changing contrast, or toggling pure black dark mode, all screen colors morph smoothly and organically across the entire application without abrupt jumps.
  - In `AppearancePickerSheet.kt`, `ScallopColorSwatchItem` now features responsive spring kinetics (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessMediumLow`) for scale bounce, dynamic animated borders and background colors, and an animated checkmark badge that pops in with `AnimatedVisibility` (spring `scaleIn + fadeIn` / `scaleOut + fadeOut`) with tactile haptic feedback.
- **Material 3 Expressive Switch (`ExpressiveSwitch`)**:
  - Built custom `ExpressiveSwitch` in `ui/components/ExpressiveSwitch.kt` adhering strictly to M3 Expressive motion and token guidelines:
    - 52dp x 32dp pill track with animated color and border tokens.
    - Morphing thumb: expands from 18dp (inactive) to 24dp (active) with spring physics (`Spring.DampingRatioMediumBouncy`).
    - Bouncy translation travel: 4dp to 24dp with subtle overshoot spring physics.
    - Expressive thumb icon: animated checkmark (`Icons.Filled.Check`) that scales and fades in/out with spring physics.
    - Tactile haptic feedback (`HapticFeedbackType.LongPress`) on toggle.
    - Accessible 48dp minimum touch target with `Role.Switch` semantics and `ToggleableState`.
  - Replaced all switches across `SettingsSheet.kt` (`動態色彩`, `純黑深色模式`, `智慧記帳通知`) and `AppearancePickerSheet.kt` (`顏色反轉`, `表情符號作為配色方案`).
- **Verified**: 100% unit tests passing (`./gradlew testDebugUnitTest`), built debug APK, installed on Waydroid (`192.168.240.112:5555`), verified fluid color cross-morphing across all 7 color schemes, bouncy switch toggles, and state persistence via ARTEMIS.

### 2026-09-24: Customization Section Redesign (Image Toolbox & Material 3 Expressive Parity)
- **Settings Customization Section (`自訂`) Parity**: Redesigned the primary customization section to match the Image Toolbox design:
  - Section header renamed to `自訂` with a 4-lobed squircle shape (`CloverIconShape`).
  - **Item 1: 色彩方案 (Color scheme)**: Leading icon in `CloverIconShape`, title `色彩方案`, subtitle `應用程式主題將會以選擇的顏色為基礎`. Right side features a 12-lobed scalloped rosette badge (`ScallopBadgeShape`, 54dp) containing the multi-tone circular preview disc (primary upper half, tertiary lower-left, secondary lower-right) with a centered edit pencil badge. Tapping opens `AppearancePickerSheet`.
  - **Item 2: 動態色彩 (Dynamic color)**: Leading icon in `CloverIconShape`, title `動態色彩`, subtitle `如果啟用，則將會採用桌面色彩為應用程式色彩`, right `Switch`.
  - **Item 3: 純黑深色模式 (Pure black dark mode)**: Leading icon in `CloverIconShape`, title `純黑深色模式`, subtitle `如果啟用，在夜間模式下背景色彩將會被設定為純黑`, right `Switch`.
  - **Item 4: 圖示形狀 (Icon shape)**: Leading icon in `CloverIconShape`, title `圖示形狀`, subtitle `在卡片的前導圖示下方新增具有所選形狀的容器`, right `CloverIconShape` contour preview.
  - **Item 5: 主題模式 (Theme mode)**: 3 segmented option cards (`跟隨系統`, `淺色`, `深色`).
- **AppearancePickerSheet (`色彩方案` Modal Bottom Sheet)**:
  - Header with top drag handle, leading icon in `CloverIconShape`, and `色彩方案` title.
  - **Card 1: Style & Controls**: Rounded 24dp card containing:
    - `調色盤風格` (Palette style / `色調點綴`) with edit pencil action.
    - `顏色反轉` (Invert colors / `若啟用，將會將主題顏色更換為相反顏色`) with `Switch`.
    - `表情符號作為配色方案` (Emoji palette / `使用表情符號原色作為應用程式配色方案...`) with `Switch`.
    - `對比` (Contrast): Leading icon + title, right pill badge showing current contrast (`0`, `-1`, `1`), and an Expressive slider with a center vertical detent tick mark at 0.
  - **Card 2: 簡單變體 (Simple Variants)**: Rounded 24dp card with centered title `簡單變體` and a horizontal scrollable row of 12-lobed scalloped swatches (`ScallopBadgeShape`): `SUNSET`, `AMBER`, `LIME` (`0xFF689F38`), `ORIGINAL` (Emerald Mint), `OCEAN`, `VIOLET`, `ROSE`, and custom `+` preset button.
  - **Bottom Action Bar**: Left pill button with pencil icon, Right pill button `關閉` (Close) with primary container background.
- **Expressive Shapes in `Shape.kt`**: Implemented `RosetteShape(lobes, depth)`, `CloverIconShape` (4-lobed squircle, depth 0.16f), and `ScallopBadgeShape` (12-lobed rosette, depth 0.10f).
- **Localization**: Added all string definitions across English and all 4 Chinese resource directories (`values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`).
- **Verified**: 100% unit tests passing (`./gradlew testDebugUnitTest`), built debug APK, installed on Waydroid (`192.168.240.112:5555`), verified visual layout, opening modal sheet, live recoloring across swatches, closing sheet, and state persistence via ARTEMIS.

### 2026-09-24: Settings Color Scheme Selection Optimization (Inline Multi-Tone Swatches & Curated Palettes)
- **Direct Inline Palette Section**: Eliminated the nested bottom sheet anti-pattern (where clicking "Color Scheme" previously launched a modal sheet over the Settings sheet). Embedded `InlineColorPaletteSection` directly within the "Appearance & Style" (`外觀與風格`) expandable accordion in `SettingsSheet.kt`.
- **Vibrant Multi-Tone Preview Discs**: Replaced murky/dark 4-quadrant previews with crisp circular preview discs showcasing dominant `primary` on the left half, vibrant `tertiary` on top-right, and accent `secondary` on bottom-right. Features spring-scale selection dynamics (`animateFloatAsState` with `Spring.DampingRatioMediumBouncy`) and a centered checkmark badge.
- **Expanded Palette Selection**: Added two new curated Material tonal palettes in `AppearancePalette.kt`: `ROSE` (`0xFF9E2A5E`, Rose Pink) and `AMBER` (`0xFF855300`, Warm Amber), joining `ORIGINAL` (Mint), `OCEAN` (Blue), `SUNSET` (Orange), and `VIOLET` (Purple).
- **Direct Inline Contrast Controls**: Embedded compact contrast selectors (`Low` [-1], `Standard` [0], `High` [1]) and a live active palette pill badge right inside the Settings surface.
- **Localization**: Added `appearance_palette_rose` and `appearance_palette_amber` string definitions across English and all four Chinese locales (`values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`).
- **Verified**: 100% unit tests passing (`./gradlew testDebugUnitTest`), built debug APK, installed on Waydroid (`192.168.240.112:5555`), and confirmed live theme switching across Mint, Ocean, Sunset, Violet, Rose, and Amber, high contrast adjustment, and immediate whole-app recoloring.

### 2026-09-24: Recurring Category Donut Chart: Distinct Categorical Palette & Single-Line Legend
- **Distinct Categorical Palette**: Replaced the monochromatic Material 3 tonal palette (which produced confusing, adjacent shades of pale green/mint) with `RECURRING_CHART_PALETTE`, semantic category mapping (`getSemanticCategoryChartColor`), and collision-free assignment (`buildCategoryColorMap`). Entertainment now maps to Royal Purple (`#8B5CF6`), Software / AI to Emerald Mint (`#10B981`), Electronics to Ocean Blue (`#3B82F6`), and Utilities to Warm Amber (`#F59E0B`).
- **Single-Line Legend Layout**: Eliminated the vertical two-line wrapping caused by stacking amount and percentage in a `Column`. Replaced with a unified horizontal `Row` featuring the formatted currency amount (`$%,.0f` / `$%,.2f`) and a sleek rounded percentage badge pill (`[ 36% ]`) with category-tinted focus states.
- **Segment Separation & Localized Labels**: Added clean 3.5° negative space gaps between donut arcs to prevent overlapping round caps. Integrated `getCategoryDisplayName` so category names in the legend and donut center are localized in Chinese and English.
- **Verified**: 100% unit tests passing (`RecurringScreenBentoTest.kt`), built debug APK, installed on Waydroid (`192.168.240.112:5555`), and confirmed visual layout, single-line alignment, high-contrast segment separation, and spring selection mechanics via ARTEMIS.

### 2026-09-24: Expressive loading and Settings navigation
- **Added** Compose Material 3's `ContainedLoadingIndicator` to the shared loading state in `ui/main/MainScreen.kt`. It appears over existing skeletons only when loading lasts more than 250 ms, with a localized label and spring fade/scale entrance and exit. This avoids a flash for fast local loads.
- **Refined** `ui/settings/SettingsSheet.kt`: larger two-line headline, tonal surfaces, icon-led groups, one expanded group at a time, and spring-animated section height, corner, color, and chevron. Appearance starts expanded; opening Language, Smart Logging, Budget, Pacing, or Privacy brings that group's controls into view. Theme and language choices use animated expressive corners and allow two-line labels at large text sizes. Added localized loading and Settings labels to all string sets.
- **Verified** `./gradlew assembleDebug` passed and the APK was installed and launched on Waydroid (`192.168.240.112:5555`). ARTEMIS screen capture plus ADB interactions confirmed the Settings overview, Appearance → Language and Language → Budget transitions, and budget controls without changing values. Checked Settings at 200% font scale and restored Waydroid to 100%. ARTEMIS autonomous tasks remain blocked because its diagnosis still reports a missing model API key in `/home/ricky/artemis/.env`; the user is configuring it.

### 2026-09-24: Appearance customization inspired by Image Toolbox
- **Added** a color-scheme picker to Settings with four persistent choices (original mint, ocean, sunset, violet), live palette previews, and low/standard/high contrast controls. Selecting a palette or contrast level turns off wallpaper-based dynamic color so the selected scheme is visible; the existing dynamic-color toggle can turn wallpaper colors back on.
- **Added** a persistent pure-black option for dark mode. Color schemes are generated from Material tonal seed colors and applied through `VibeFinanceTheme`; the original mint palette remains the default appearance. New controls and labels are localized in English and the app's Chinese resource sets.
- **Changed** `app/build.gradle.kts`, `theme/AppearancePalette.kt`, `theme/Theme.kt`, `MainActivity.kt`, `ui/FinanceViewModel.kt`, `ui/settings/SettingsSheet.kt`, and `ui/settings/AppearancePickerSheet.kt`.
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` passed. Installed and launched on Waydroid (`192.168.240.112:5555`); confirmed the settings layout, picker, live ocean recoloring, high contrast, and pure-black dark surfaces. Reinstalled and visually checked the final full-width picker. Restored Waydroid to its original mint/standard contrast/pure-black-off appearance and left the existing Traditional Chinese language setting unchanged.

### 2026-09-24: Custom two-tier expressive collapsing glass top bar
- **Changed** `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt`: replaced the built-in `LargeTopAppBar` with `ExpressiveCollapsingTopBar`. Its fixed row shows the app logo, brand name, and existing theme/settings actions while expanded; `AnimatedContent` swaps the brand for the selected page's compact two-line bilingual title when collapsed. The separate large title row uses a 26 sp ExtraBold headline and supporting subtitle, with `AnimatedVisibility` spring height, slide, and fade transitions. An optional back-icon branch is ready for screens with back navigation.
- **Scroll and glass**: the bar follows consumed vertical list scroll distance, resets on tab changes, and preserves the measured expanded inset so compact-screen content scrolls behind it. Its background uses Haze blur under `surface.copy(alpha = 0.90f)`, `.statusBarsPadding()`, and a horizontal specular gradient bottom border. The compact row grows with font scaling. Added localized `nav_back` descriptions to all string sets.
- **Verified** `./gradlew assembleDebug` passed; installed and launched the APK on Waydroid (`192.168.240.112:5555`). Checked Daily expanded/collapsed/re-expanded, expanded headers on Assets, Recurring, and History, and compact/expanded History in English and Traditional Chinese. Restored Waydroid font scaling to 100% after a larger-text check. The app's mandatory daily settlement sheet appeared after the date changed; its budget choices were left untouched, and the app language is currently Traditional Chinese on Waydroid.

### 2026-09-23: Collapsing large frosted top app bar
- **Changed** `ui/main/MainScreen.kt`: replaced the fixed top bar with a Material 3 `LargeTopAppBar` connected to `exitUntilCollapsedScrollBehavior`. Scroll movement collapses its large bilingual title section into a compact single-line title row; spring snapping settles the bar, and spring-based `AnimatedContent` still swaps titles when changing tabs. The leading Savings badge and theme/settings actions remain in the top row. Tab changes reset the bar to expanded.
- **Added real backdrop blur** with Haze 1.7.2 in `app/build.gradle.kts`. The top bar samples scrolling page content through a tinted frosted surface with a subtle bottom edge. Daily, Assets, Recurring, and History list content now starts below the expanded bar and scrolls behind it on compact screens; the wide Recurring layout retains its non-overlapping inset.
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` passed. Installed and launched the debug APK on Waydroid (`192.168.240.112:5555`). Confirmed expanded/collapsed/re-expanded states, inspected video frames during collapse, checked all four expanded page titles, Traditional Chinese text, dark/light themes, and 150%/200% font scaling. Restored Waydroid font scaling and the app's English/dark settings afterward.

### 2026-09-23: Pure spring motion and bilingual top bar hierarchy
- **Changed** `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt`: the standard `TopAppBar` now animates its two-line title as one unit using spring-based slide, fade, and size transitions. The theme icon's scale, fade, rotation, and tint transitions also use springs. The selected app language remains the bold primary title; its translation appears in the smaller secondary line before the existing page detail.
- **Localized** alternate titles for Daily, Assets, Recurring, and History in `values/strings.xml` and the Traditional Chinese resource sets (`values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`).
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` succeeded. Installed the APK on Waydroid (`192.168.240.112:5555`), checked all four top bars in English and Traditional Chinese without clipping, and inspected a device recording of the Daily → Recurring title transition. Restored the app to English afterward.

### 2026-09-23: Original top bar motion, typography, and leading icon
- **Changed** `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt`: kept the standard Material 3 `TopAppBar`, restored the original vertical slide/fade title transition, responsive ExtraBold title and muted subtitle styles, and animated moon/sun rotation and crossfade inside the theme action. Added the original Savings brand badge in the leading app bar slot.
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` succeeded. Installed the APK on Waydroid (`192.168.240.112:5555`); ADB screenshots confirmed the leading icon and title styling, consecutive frames during a Daily → Recurring switch showed the restored title animation, and the theme action still switches themes.

### 2026-09-23: Standard Material 3 top app bar
- **Changed** `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt`: replaced the floating pill overlay with a standard `TopAppBar` in the `Scaffold` top bar slot. Kept the per-tab title/subtitle and theme/settings actions, now using Material 3 `IconButton`s. Removed obsolete overlay measurements and disabled the top content fade because pages now start below the bar.
- **Adjusted content spacing** in `ui/home/HomeScreen.kt`, `ui/accounts/AccountsScreen.kt`, `ui/recurring/RecurringScreen.kt`, `ui/history/HistoryScreen.kt`, and `ui/components/PulsingSkeleton.kt`: removed header spacers and added 16 dp top content padding so loaded and loading states align beneath the app bar.
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` succeeded. Installed the APK on Waydroid (`192.168.240.112:5555`); ADB screenshots confirmed correct layout on Daily, Assets, Recurring, and History. The theme toggle, Settings action, and Recurring add sheet still open correctly.

### 2026-09-23: Daily ↔ Recurring FAB transition
- **Changed** `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt`: replaced the two independent FAB visibility animations with one bottom-end-aligned `AnimatedContent` host. Its spring size/scale motion and short fade carry the Daily action menu into the Recurring extended FAB and back. The host still hides the action while its entry sheet is open, and outgoing actions ignore taps after a tab switch.
- **Verified** `./gradlew compileDebugKotlin` and `./gradlew assembleDebug` succeeded. Installed the debug APK on Waydroid (`192.168.240.112:5555`), confirmed both FAB destinations, and inspected consecutive screenshots of the Recurring → Daily switch showing the outgoing extended FAB fade before the Daily button settles. The Daily FAB menu still opens and closes on tab change, and the Recurring FAB still opens its subscription sheet. ARTEMIS automation remains unavailable because its model API key is missing, though its screenshot and hierarchy tools work.

### Latest Major Milestones Completed:
1. **Subscription Deletion: Past Payment Records Decision Flow & Balance Reversal**:
   - Built [`findMatchingTransactionsForSubscription`](file:///home/ricky/Antigravity_project/finance_app/app/src/main/java/com/example/vibefinance/ui/recurring/RecurringScreen.kt): matches subscription debit transactions across auto-charges and manual descriptions while excluding transfers.
   - Material 3 Expressive `DeleteSubscriptionConfirmDialog`: presents "Keep payment records (Recommended)", "Delete payment records also", and "Cancel".
   - Seamless balance reversal in Room/`InMemoryDatabase` upon transactional deletion.
   - Seeded ChatGPT Plus HK$ 160.00 auto-charge (Tx 7 in `DataSeeder.kt`) for out-of-the-box demonstration.
2. **Gmail-Style Swipe-To-Action Parity across Recurring & History (`ExpressiveSwipeRow.kt`)**:
   - Replaced 3D tilt/peel distortion with clean 1:1 horizontal translation, floating card shadow elevation (`4.dp`), dynamic spring icon pop (`1.0f -> 1.25f`), and 44dp translucent circular backdrop disc.
   - Implemented two-direction actions: Left swipe for Delete (error container -> error red with trash icon), Right swipe for Edit (tertiary container -> tertiary with pencil icon).
   - Unified across `SubscriptionRowItem`, `InstallmentRowItem`, and `HistoryScreen` transaction rows.
3. **Unified 4-Page Financial Chart Color System (`ChartColors.kt`)**:
   - Centralized 12-color high-contrast wide-spectrum palette (`UNIFIED_PALETTE`) with deterministic bilingual category mapping.
   - Two-pass collision-free allocation algorithm with 12% dynamic theme primary tint blending.
   - 100% color consistency across Daily (`HomeScreen.kt`), Assets (`MoneySeparationChart.kt`), Recurring (`RecurringScreen.kt`), and History (`CategoryBreakdownCard.kt`).
4. **Native Material 3 Expressive Connected Assets Group & Spring Kinetics (`AccountsScreen.kt`)**:
   - Implemented three native `ToggleButton`s with `ButtonGroupDefaults.ConnectedSpaceBetween`, connected shapes, live counts, and localized labels.
   - Added direction-aware sliding spring accent indicator and `LazyColumn.animateItem` transitions.
5. **Icon Shape Personalization System & Global Dynamic Container Shapes (`Shape.kt` & `SettingsSheet.kt`)**:
   - 8 expressive geometric shapes (`IconShapeMode`), hoisted via `LocalIconShape` CompositionLocal, persisted in SharedPreferences, and live in `IconShapePickerDialog`.
6. **Settings Sheet Quiet Dismissal & Daily Donut Chart Categorical Overhaul**:
   - Eliminated unwarranted `"Custom period budget set successfully!"` toast on sheet dismissal; added `showToast` intent flag and change-detection diffing.
   - Replaced collapsed monochrome dynamic theme colors with vibrant semantic category hues.
7. **Performance & Idle Optimization (Waydroid CPU/Battery 0% Idle Settle)**:
   - Eliminated perpetual wave rendering loops on `HeroDailyBudgetCard`, `RestAndSpentBudgetCard`, and `DaysLeftCard`.
   - Replaced custom circular wave track with native Material 3 `CircularWavyProgressIndicator` with tap-to-toggle Flat/Wavy mode.
8. **First-Class Recurring Installments Migration**:
   - Active multi-month installment plans aggregated alongside subscriptions in `RecurringScreen.kt`.
- **Verification Status**: 100% unit tests passing (77/77 tests via `./gradlew testDebugUnitTest`) and fully verified live on Waydroid emulator (`192.168.240.112:5555`).

---

## 2. Inventory of Files Modified & Created

### Core Architecture & State Management
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/ui/FinanceViewModel.kt` | **MODIFIED** | Added `FinanceIntent.DeleteSubscription(sub, deletePastTransactions)` with transaction cleanup and balance reversal; added `FinanceIntent.DeleteInstallmentGroup(groupId, accountId)`; updated `FinanceIntent.SetCustomPeriodBudget` with `showToast: Boolean = false` to prevent false dismiss toasts; added `FinanceIntent.SetIconShape(shape)` with SharedPreferences persistence; fixed `SeedMockData` loading state. |
| `app/src/main/java/com/example/vibefinance/data/DataSeeder.kt` | **MODIFIED** | Seeded sample installment plans and seeded Tx 7: ChatGPT Plus HK$ 160.00 auto-charge linked to credit card account. |
| `app/src/main/java/com/example/vibefinance/data/InMemoryDatabase.kt` | **MODIFIED** | Enhanced in-memory transaction query, group deletion, and transaction deletion balance reversal support. |
| `app/src/main/java/com/example/vibefinance/data/repository/BudgetRepository.kt` | **MODIFIED** | Strict parity for `DISTRIBUTE_EVENLY`, `ADD_TO_NEXT_DAY`, and overdraft budget redistribution logic. |

### UI: Components & Shared Elements (Material 3 Expressive)
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/theme/ChartColors.kt` | **NEW** | Centralized 12-color high-contrast wide-spectrum palette authority (`UNIFIED_PALETTE`), deterministic bilingual category semantic mapping, two-pass collision-free assignment algorithm with 12% dynamic primary tint blending, and unified asset segment tokens. |
| `app/src/main/java/com/example/vibefinance/ui/components/ExpressiveSwipeRow.kt` | **NEW** | Reusable Gmail-style swipe-to-action component featuring 1:1 translation, 4dp floating elevation, dynamic spring icon pop (`1.0f -> 1.25f`), 44dp translucent circular backdrop disc, haptic feedback, and two-direction actions (Delete vs Edit). |
| `app/src/main/java/com/example/vibefinance/ui/components/ExpressiveSwitch.kt` | **NEW** | Material 3 Expressive switch with 52x32dp pill track, morphing thumb (18dp to 24dp), spring translation overshoot, animated checkmark icon, and tactile haptic feedback. |
| `app/src/main/java/com/example/vibefinance/ui/components/AppBrandIcon.kt` | **MODIFIED** | Implemented `VibeFinanceIcon` in pure Jetpack Compose Canvas, translating `ic_launcher_foreground.xml` into a resolution-independent, hardware-accelerated Compose component with ambient shadow, emerald gradient 'V' emblem, upward growth sparkline, and glowing coin node. |
| `app/src/main/java/com/example/vibefinance/ui/components/BudgetPeriodIndicatorCard.kt` | **MODIFIED** | Full-width connected filter buttons eliminating edge shadow artifacts; replaced custom Box track with flat Material 3 `LinearProgressIndicator`. |
| `app/src/main/java/com/example/vibefinance/ui/components/CategoryBreakdownCard.kt` | **MODIFIED** | Integrated unified `ChartColors`, touch drag scrubbing with spring segment expansion, interactive summary card, and spring exit motion. |
| `app/src/main/java/com/example/vibefinance/ui/components/MoneySeparationChart.kt` | **MODIFIED** | Consumes `ChartColors.getAssetSegmentColor` for harmonized cash, bank, and credit/debt segment styling. |
| `app/src/main/java/com/example/vibefinance/ui/components/RollingNumberText.kt` | **MODIFIED** | Smooth animated number and currency transitions without recomposition jitter. |

### UI: Screens & Navigation
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt` | **MODIFIED** | Two-tier `ExpressiveCollapsingTopBar` with separated two-line spring kinetics; predictive back navigation across tabs and FAB menu; integrated native Compose `VibeFinanceIcon`; `ContainedLoadingIndicator`; clean 3-second daily logging sheet. |
| `app/src/main/java/com/example/vibefinance/ui/home/HomeScreen.kt` | **MODIFIED** | Material 3 Expressive daily dashboard with responsive Bento grid scaffolding; integrated unified `ChartColors` in `CategoryDonutChart`. |
| `app/src/main/java/com/example/vibefinance/ui/home/DailySpendingLineChart.kt` | **MODIFIED** | Dual mode toggle (`CUMULATIVE` vs `DAILY`), interactive drag scrubbing, pulsating glow nodes, rolling numbers, and expressive floating tooltip badge. |
| `app/src/main/java/com/example/vibefinance/ui/home/DaysLeftCard.kt` | **MODIFIED** | Deterministic `CircularWavyProgressIndicator` with tap-to-toggle Flat/Wavy mode, 5dp stroke, 0% idle CPU settle. |
| `app/src/main/java/com/example/vibefinance/ui/home/HeroDailyBudgetCard.kt` | **MODIFIED** | Fluid liquid dynamic budget indicator with single 4-5s entrance wave settling to 0% idle CPU; 3-column Bento metrics row. |
| `app/src/main/java/com/example/vibefinance/ui/home/RecalcBudgetSheet.kt` | **MODIFIED** | Overdraft recalculation sheet with spring adjustments and non-looping canvas confetti. |
| `app/src/main/java/com/example/vibefinance/ui/accounts/AccountsScreen.kt` | **MODIFIED** | Native M3 Expressive connected `ToggleButton` group (`ALL`, `CASH_BANK`, `CREDIT_CARDS`), sliding spring accent indicator, and `LazyColumn.animateItem` transitions. |
| `app/src/main/java/com/example/vibefinance/ui/history/HistoryScreen.kt` | **MODIFIED** | Gmail-style `ExpressiveSwipeRow` parity for transaction rows; corrected signed Daily Net footer calculation (+HK$2,300.00 for seeded Yesterday); predictive back handling for category filters and edit sheet. |
| `app/src/main/java/com/example/vibefinance/ui/recurring/RecurringScreen.kt` | **MODIFIED** | Added `findMatchingTransactionsForSubscription` and `DeleteSubscriptionConfirmDialog` (Keep records / Delete records also / Cancel); Gmail-style `ExpressiveSwipeRow` swipe actions; unified `ChartColors` category donut; installment plan aggregation, preview card, and `AddEditSubscriptionSheet`. |

### Theme & Settings (Image Toolbox-Inspired & Expressive Components)
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/theme/Shape.kt` | **MODIFIED** | Added `RosetteShape`, `CloverIconShape` (4-lobed squircle), `ScallopBadgeShape` (12-lobed rosette), `IconShapeMode` enum (8 shapes), and `LocalIconShape` CompositionLocal. |
| `app/src/main/java/com/example/vibefinance/theme/Theme.kt` | **MODIFIED** | Added `animateColorSchemeAsState` wrapping 30+ semantic tokens with M3 Expressive spring physics; provides `LocalIconShape` throughout the Compose tree. |
| `app/src/main/java/com/example/vibefinance/theme/AppearancePalette.kt` | **MODIFIED** | 7 curated palette seeds (`SUNSET`, `AMBER`, `LIME`, `ORIGINAL`, `OCEAN`, `VIOLET`, `ROSE`), contrast presets (Low, Standard, High), and pure-black dark mode override. |
| `app/src/main/java/com/example/vibefinance/ui/settings/SettingsSheet.kt` | **MODIFIED** | Redesigned "自訂" customization section matching Image Toolbox; quiet dismissal fix (no spurious custom budget toast); interactive Item 4 launching `IconShapePickerDialog`; `ExpressiveSwitch` integration. |
| `app/src/main/java/com/example/vibefinance/ui/settings/AppearancePickerSheet.kt` | **MODIFIED** | Modal appearance bottom sheet with inline preview discs, contrast slider, `ExpressiveSwitch` toggles, and spring-scaled scalloped swatches. |
| `app/src/main/java/com/example/vibefinance/ui/settings/IconShapePickerDialog.kt` | **NEW** | Expressive 8-shape geometric picker dialog with tactile haptic feedback and real-time previews. |
| `app/src/main/java/com/example/vibefinance/MainActivity.kt` | **MODIFIED** | Hoisted appearance & icon shape state to `VibeFinanceTheme`; edge-to-edge window decor fits. |

### Localization & Resources
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/res/values/strings.xml` | **MODIFIED** | Base English strings for subscription deletion decisions, chart categories, icon shapes, swipe actions, installment filters, format strings, and toasts. |
| `app/src/main/res/values-zh-rHK/strings.xml` | **NEW / MOD** | Traditional Chinese (Hong Kong) localization. |
| `app/src/main/res/values-zh/strings.xml` | **NEW / MOD** | Simplified Chinese localization. |
| `app/src/main/res/values-zh-rTW/strings.xml` | **NEW / MOD** | Traditional Chinese (Taiwan) localization. |
| `app/src/main/res/values-b+zh+Hant/strings.xml` | **NEW / MOD** | Generic Traditional Chinese localization fallback. |

### Services & Background Workers
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/service/PaymentNotificationListener.kt` | **MODIFIED** | Handled bank/payment notifications with zero wakelocks to prevent device Doze disruption. |

### Unit & Instrumentation Tests (77 Unit Tests Passing)
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/test/java/com/example/vibefinance/ui/recurring/RecurringScreenBentoTest.kt` | **MODIFIED** | 17 tests: mixed-frequency calculations, active installment commitments, `extractInstallmentPlans` grouping, filter modes, `buildCategoryColorMap` collision-free allocation, and `findMatchingTransactionsForSubscription` past payments matching. |
| `app/src/test/java/com/example/vibefinance/ui/accounts/AccountsScreenBentoTest.kt` | **NEW / MOD** | 7 tests: AccountsScreen filtering logic (`ALL`, `CASH_BANK`, `CREDIT_CARDS`), net worth calculations, and group state behavior. |
| `app/src/test/java/com/example/vibefinance/data/repository/BudgetRepositoryTest.kt` | **MODIFIED** | 10 tests: Budget redistribution algorithms (`DISTRIBUTE_EVENLY`, `ADD_TO_NEXT_DAY`), roll-forward calculations, and edge cases. |
| `app/src/test/java/com/example/vibefinance/theme/ExpressiveThemeStressTest.kt` | **NEW** | 12 tests: Palette seed generation, contrast ratios, and dark mode overrides. |
| `app/src/test/java/com/example/vibefinance/service/PaymentNotificationListenerTest.kt` | **MODIFIED** | 6 tests: Bank SMS parsing, notification extraction, and regex matching. |
| `app/src/test/java/com/example/vibefinance/service/DeadlineCheckWorkerTest.kt` | **NEW** | 3 tests: Subscription and installment deadline checks. |
| `app/src/test/java/com/example/vibefinance/ui/home/HomeScreenBentoTest.kt` | **NEW** | 10 tests: Daily budget calculation, hero card state, and stat metrics. |
| `app/src/test/java/com/example/vibefinance/ui/home/HomeScreenBoundaryStressTest.kt` | **NEW** | 7 tests: Budget overflow, negative balances, and boundary conditions. |
| `app/src/test/java/com/example/vibefinance/ui/common/FadingEdgeStressTest.kt` | **NEW** | 5 tests: Horizontal and vertical fading edge calculations. |
| `app/src/androidTest/java/com/example/vibefinance/ui/history/HistoryScreenTest.kt` | **MODIFIED** | 4 on-device tests: short-swipe abort, right-swipe edit, left-swipe delete/undo, and signed Daily Net calculation. |
| `app/src/androidTest/java/com/example/vibefinance/ui/main/AppNavigationTest.kt` | **NEW** | 4 on-device tests: tab navigation, backstack, and sheet handling. |

---

## 3. Important Architectural Details & Conventions

### 1. Installment Commitment Model
- Each installment plan consists of $N$ monthly `TransactionEntity` rows linked by a unique `groupId: String` and matching `totalInstallments: Int`.
- `TransactionEntity.timestamp` reflects the scheduled due date for that specific installment ($i \in \{1 \dots N\}$).
- A plan is deemed **active** if at least one transaction has `timestamp > System.currentTimeMillis()`.
- Installments calculate their remaining unpaid debt as `unpaidTxs.sumOf { it.amount }`.
- In `RecurringScreen`:
  - `calculateRecurringCommitment(subscriptions, installments)` factors active monthly installment debt directly into the Hero card (`HK$ / mo`, `≈ HK$ / yr`, `≈ HK$ / day`) and category donut chart.
  - Backwards-compatibility is maintained via `installments: List<InstallmentPlan> = emptyList()`.

### 2. Material 3 Expressive UI Patterns
- **Connected Button Groups**: Use `ConnectedButtonGroup` for segmented selection with spring morphing (`animateDpAsState` with `Spring.DampingRatioMediumBouncy`).
- **Rolling Digits**: Use `RollingNumberText` for animated number and currency transitions without recomposition jitter.
- **Edge Fading**: Use `Modifier.horizontalFadingEdge(12.dp, 12.dp)` on all horizontally scrollable chip rows (`LazyRow`) to prevent abrupt visual clipping at screen boundaries.
- **Bento Grid**: Use asymmetrical card arrangements with `surfaceVariant.copy(alpha = 0.45f)` container styling and semantic colored borders.

### 3. Unified 4-Page Financial Chart Color System (`ChartColors.kt`)
- **Central Authority**: `theme/ChartColors.kt` acts as the single source of truth across all 4 main tabs (`Daily`, `Assets`, `Recurring`, `History`).
- **High-Contrast Palette (`UNIFIED_PALETTE`)**: 12 wide-spectrum hues (Royal Purple `#8B5CF6`, Emerald Mint `#10B981`, Ocean Blue `#3B82F6`, Warm Amber `#F59E0B`, Hot Rose `#EC4899`, Cyan `#06B6D4`, Coral Orange `#F97316`, Indigo `#6366F1`, Sunflower Gold `#EAB308`, Bright Teal `#14B8A6`, Golden Amber `#FBBF24`, Coral Crimson `#F43F5E`).
- **Bilingual Semantic Mapping (`getSemanticCategoryColor`)**: Automatically maps English, Traditional Chinese, and common financial category terms deterministically to matching colors.
- **Two-Pass Allocation Guarantee (`buildCategoryColorMap`)**: Guarantees zero color collisions even when mixing system categories with arbitrary user-defined custom categories.
- **Dynamic Theme Harmonization**: Subtly blends 12% of `MaterialTheme.colorScheme.primary` into each hue so charts organically harmonize with the active theme while maintaining 88% spectral distinction.
- **Harmonized Asset Tokens**: `getAssetSegmentColor` provides uniform colors for Cash (`#10B981`), Bank Accounts (`#3B82F6`), and Debt/Credit (`#F43F5E`).

### 4. Subscription Deletion & Historical Payment Decision Architecture
- **Payment Record Grounding**: [`findMatchingTransactionsForSubscription(sub, transactions)`](file:///home/ricky/Antigravity_project/finance_app/app/src/main/java/com/example/vibefinance/ui/recurring/RecurringScreen.kt) inspects debit transactions, matching auto-charges (`Auto-charge: ${sub.name}`) and manual descriptions (`contains(sub.name, ignoreCase = true)`), while strictly excluding transfers (`toAccountId == null`).
- **Decision Dialog (`DeleteSubscriptionConfirmDialog`)**:
  - Displays subscription metadata and matching transaction count & total sum.
  - Option 1 (*Keep Records - Recommended*): Cancels future auto-charges and deletes the subscription entity, retaining all historical transactions in reports and account balances.
  - Option 2 (*Delete Records Also*): Deletes the subscription rule and purges matching debit transactions, automatically restoring/reversing account balances.
  - Option 3 (*Cancel*): Safe dismiss; cards spring back elastically.
- **MVI Dispatch**: Handled via `FinanceIntent.DeleteSubscription(sub, deletePastTransactions = Boolean)`.

### 5. Gmail-Style Swipe-to-Action Architecture (`ExpressiveSwipeRow.kt`)
- **1:1 Finger Tracking**: Cards translate along the X-axis (`translationX = currentOffset`) without 3D rotation, angular tilt, or neighbor card distortion.
- **Dynamic Elevation**: Floating card shadow lifts from `0.dp` to `4.dp` when swiping begins.
- **Gmail Kinetic Icon Pop & Backdrop**: Crossing the activation threshold (~38% of card width) triggers a spring scale pop (`1.0f -> 1.25f`) of the action icon and displays a 44dp translucent circular disc (`CircleShape`).
- **Directional Actions**:
  - Swipe Left (Delete): Transitions to `error` red with trash icon; crossing threshold flings card off-screen with vertical collapse.
  - Swipe Right (Edit): Transitions to `tertiary` with pencil edit icon; crossing threshold springs card back and launches the edit sheet.
- **Shared Implementation**: Drives both subscription rows, installment rows, and history transaction items.

### 6. Global Dynamic Icon Shape System (`Shape.kt` / `LocalIconShape`)
- **Shape Tokens**: Defined 8 geometric shapes in `IconShapeMode` (`CLOVER`, `SQUIRCLE`, `ROUNDED_SQUARE`, `CIRCLE`, `SCALLOP`, `FLOWER`, `DIAMOND`, `STAR`).
- **CompositionLocal Provider**: `LocalIconShape` is provided at the root by `VibeFinanceTheme` in `Theme.kt`.
- **Reactive Tree Propagation**: Consumed dynamically by leading icon avatars in `SettingsSheet.kt`, `AppearancePickerSheet.kt`, `HistoryScreen.kt`, and `RecurringScreen.kt`.
- **Persistence**: Persisted via SharedPreferences and hoisted in `FinanceUiState.iconShape`.

### 7. Pure Jetpack Compose Brand Vector Architecture & XML Invariants
- **Zero XML UI Layouts**: Verified 0 XML layout files (`res/layout/`) exist anywhere in `app/src/main/`. All screens, dialogs, bottom sheets, cards, bento grids, and navigation elements are 100% Jetpack Compose.
- **Brand Emblem Conversion**: Converted `ic_launcher_foreground.xml` into a native Jetpack Compose Composable `VibeFinanceIcon` in `AppBrandIcon.kt`.
- **Top Bar Integration**: Replaced generic placeholder `Savings` icon in `ExpressiveCollapsingTopBar` (`MainScreen.kt`) with the resolution-independent `VibeFinanceIcon`, rendering the stylized geometric 'V', emerald gradient, and capital sparkline node natively in Compose.
- **Platform XML Roles**: Preserved platform-mandatory XML descriptors (`AndroidManifest.xml`, `themes.xml` for pre-Compose window decor, `backup_rules.xml`, and launcher mipmaps for the Android OS launcher process).
- **External Reference Repository**: Clarified that `buckwheat/` is an external reference directory and is excluded from compilation (`settings.gradle.kts` only includes `:app`).

---

## 4. Operational Instructions for Future Agents

### Rule 1: Codebase Discovery with `graphify`
- When asking architectural or dependency questions, **do not manually grep the whole repository**.
- Use the knowledge graph at `graphify-out/`:
  - Query: `graphify query "<question or symbol>"`
  - Path: `graphify path "<SymbolA>" "<SymbolB>"`
  - Explain: `graphify explain "<concept>"`
- **Rule**: After modifying code files, always run `graphify update .` to keep the graph up to date.

### Rule 2: Device Testing with `artemis` & Waydroid
- **Device Target**: Always target the Waydroid container (`192.168.240.112:5555`). **NEVER target the user's physical phone** (`R5CX22YGH7A`).
- **ARTEMIS Integration**:
  - Check real-time screen / hierarchy:
    ```json
    mobile_get_device_state(view_type="hierarchy" / "screenshot", device_serial="192.168.240.112:5555")
    ```
  - Autonomous UI workflows:
    ```json
    mobile_run_task(task_desc="...", device_serial="192.168.240.112:5555", model="Flash")
    ```
  - Health & recovery:
    ```json
    mobile_diagnose(device_serial="192.168.240.112:5555", attempt_fix=true)
    ```

### Rule 3: Build & Test Verification Commands
- Compile Kotlin:
  ```bash
  ./gradlew compileDebugKotlin
  ```
- Run unit tests:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- Build debug APK:
  ```bash
  ./gradlew assembleDebug
  ```
- Install to Waydroid:
  ```bash
  adb -s 192.168.240.112:5555 install -r app/build/outputs/apk/debug/app-debug.apk
  ```
- Launch app:
  ```bash
  adb -s 192.168.240.112:5555 shell am start -n com.example.vibefinance/.MainActivity
  ```

---

## 5. Active Workspace Skills & Rules Inventory

| Type | Name / Path | Scope & Focus |
| :--- | :--- | :--- |
| **Skill** | [`.agents/skills/android-design-guidelines/SKILL.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/skills/android-design-guidelines/SKILL.md) | **1,052-line Material Design 3 Platform Manual**: Dynamic Color, semantic color roles, Navigation Bar/Rail/Drawer, Predictive Back, WindowSizeClasses, Foldables, Component Specs, Accessibility (TalkBack, 48dp Touch Targets, Contrast), Gestures, Notification Channels, and Permissions. |
| **Skill** | [`.agents/skills/mobile-android-design/SKILL.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/skills/mobile-android-design/SKILL.md) | **Jetpack Compose Quick-Start & Troubleshooting**: Component templates (`ItemListCard`), state hoisting, `rememberSaveable`, avoiding recomposition loops, and coroutine memory leak prevention. |
| **Skill** | [`.agents/skills/material-3-expressive/SKILL.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/skills/material-3-expressive/SKILL.md) | **M3 Expressive Motion & Bento Layouts**: Liquid progress indicators, spring kinetics (`Spring.DampingRatioMediumBouncy`), rolling number animations, and horizontal edge-fading chip rows. |
| **Skill** | [`.agents/skills/graphify/SKILL.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/skills/graphify/SKILL.md) | **AST Knowledge Graph Discovery**: Querying codebase subgraphs (`graphify query`), dependency paths (`graphify path`), and incremental AST updates (`graphify update .`). |
| **Rule** | [`.agents/rules/M3E.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/rules/M3E.md) | **Enforced Platform Invariants**: M3 Expressive spatial depth, spring motion, accessible >= 48dp touch targets, TalkBack content descriptions, semantic color role pairing, and predictive back navigation. |
| **Rule** | [`.agents/rules/graphify.md`](file:///home/ricky/Antigravity_project/finance_app/.agents/rules/graphify.md) | **Codebase Graph Invariants**: Query graph first before grep, update graph after code changes. |
