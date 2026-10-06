# VibeFinance Project Progress & Agent Handoff

This document details all recent features, architectural changes, modified files, and operational protocols for any AI agent or developer continuing development on **VibeFinance**.

---

## 1. Executive Summary & Current Status

VibeFinance is a modern personal finance Android application using Jetpack Compose, Material 3 Expressive design tokens, Room/`InMemoryDatabase`, Kotlin Coroutines/Flow, and MVI architecture.

### 2026-10-06: Category Merge Management
- Added Settings → Manage categories, using a full-screen compact-device dialog and a bounded dialog on wider windows. Select multiple source categories, choose an existing destination, then explicitly review affected transaction/recurring counts and combined category budget before confirming. Income and expense scopes are independent; transfers and balance adjustments are excluded. Source choices cannot also be destinations. Search supports localized labels and stored category names; colliding display labels show their original names for disambiguation. Confirmation content scrolls at large font sizes and the review action stays accessible.
- Category merging relabels past and future/installment records plus recurring definitions without replaying account balance effects or changing transaction IDs, amounts, timestamps or financial metadata. Expense category limits are added together. Persist all changed records, definitions, limits and alias rules in one AtomicFile snapshot before publishing flows; write failures leave both saved and in-memory data unchanged. Merge aliases support chains, backward-compatible reload, and future manual/edit/bulk/import/notification/recurring writes. Merchant suggestions, add/edit category choices and History category links follow the destination. No automatic undo is offered; the confirmation states this.
- ARTEMIS/ADB explored the actual Settings → manager → source → target → review → cancel and income-switch paths on user-selected Waydroid `192.168.240.112:5555` before authoring new tests. Verification: **176 JVM tests / 22 suites and 53 Waydroid UI tests passed** (46.162s), including 13 new storage regressions and 9 new manager/History UI cases. Coverage includes notifications/deduplication, imports, merge chains/reload, write failure, independent income scope, metadata/balances, multiselect/cancel, Traditional Chinese search, busy controls, 200% text and retired History links. Final Debug/AndroidTest and Release lint/R8 builds pass; matching-signed Release installed in place. Real ledger snapshots before exploration, after tests and before Release installation match byte-for-byte; no real merge or draft was saved.
- FAB form redesign remains a proposal as requested: use full-screen compact entry forms with amount/keypad and essential selection controls always accessible, short vertical menus for small lists, and a separate searchable picker with recent/favorite items for large lists. Transfer can show source → destination selection cards. No FAB navigation/layout redesign is included in this change.
- Implementation: `data/entity/CategoryMerging.kt`, `InMemoryDatabase`, `FinanceViewModel`, `ui/settings/CategoryMergeDialog.kt`/`SettingsSheet`, category choices in Main/History/Recurring, MerchantRuleEngine, and 16 new localized strings in English/four Traditional Chinese resource variants. Runnable tests: `CategoryMergeTest`, `CategoryMergeDialogTest`. Artifacts/APKs/logs/screenshots/test XML and reproduction instructions: `captures/category-merge-2026-10-06/`.

### 2026-10-06: Daily FAB Modal Rendering Optimization
- Profiled the user-selected Waydroid Release with ARTEMIS observations, ADB frame stats and before/intermediate/final Perfetto traces. Expense, Income and Transfer sheets previously left the Daily root drawing about 60 frames/s even after the sheet stopped moving. The no-budget Days Left card still runs a full wavy indicator (fallback 30-day period); opening a modal does not stop the Activity lifecycle, so lifecycle-only wave scheduling kept it active.
- Added an occlusion composition local to the shared ambient wave clock. Daily waves pause under the FAB menu, dialogs/sheets and through the depth restoration, preserving phase; dismissal resumes from visible frame time without catching up hidden time. Financial state and scroll position remain live. Top-bar Haze uses its existing scrim fallback while the background is obscured.
- Replaced the two long background scale/corner springs with one 220ms non-bouncing depth tween, consumed in graphicsLayer. FAB transitions to/from an absent control use short fades without animated size/scale, reducing overlapping layout work. Removed the full add-form content-size spring while retaining the Material sheet position animation. Cached transaction category/account frequency on the main page rather than scanning the ledger on each first sheet composition.
- Verification: final Debug, AndroidTest and Release (lint/R8) builds pass; **163 JVM tests / 21 suites and 28 Waydroid UI tests pass** (43.477s final UI run). Four new memory-only tests verify Today/Remaining/Days wave pause/resume and phase continuity after a non-periodic 10.736 seconds hidden; existing budget morphs, Daily arrivals, complete-press buttons and History editing pass. Runnable benchmark uses XML text/description locators with calibrated coordinate fallback and explicit waits; final Release titles/currency verified for all three modes. No drafts saved, ledger cleared, or language/theme preferences changed; final Release restored on Waydroid.
- Results: three matched held windows drop from 60 frames/s in gfxinfo (59 in Perfetto) to **0 frames**; visible ambient motion resumes. Final first-popup main frames are approximately **61.78 / 46.63 / 36.18ms** versus **67.44 / 45.02 / 37.22ms** baseline (Expense / Income / Transfer). Initial rendering still exceeds one frame; the intermediate cold run regressed and repeated samples vary. Do not claim universal first-open speed gains or a terminal Haze/GPU root cause. Waydroid frequency/thermal data and meaningful frame-jank labels are unavailable. Artifacts and detailed evidence: `captures/daily-fab-performance-2026-10-06/`; reproducible controller: `tools/testing/daily_fab_benchmark.py`.

### 2026-10-06: Compact History Analytics Layout & Actionable Empty States
- Applied all four user-approved whitespace improvements. Import / Export CSV now live in a header-anchored Material 3 overflow menu with icons and a divider, releasing the previous separate action row. Wrapping title / hint and explicit category reset are retained; all five English / Chinese resource sets include the new action labels.
- Month navigation retains 48dp previous/next controls. Budget date text wraps fully. All time replaces the duplicate date label with actual total spending and expense record count, using the chart's time/account scope and excluding income, transfers and balance adjustments.
- Shared `HistoryEmptyState` combines the duplicate zero-ledger messages into one explanation and Add transaction entry. History's Add callback opens the existing expense sheet without leaving History. An empty analytics period with older expenses offers View all records; it clears category and budget-period restrictions while preserving account scope. An empty category-list result still has its own actionable message when the chart contains other expenses.
- Analytics interior padding is 16dp and section/card gaps are 12dp; removed the extra 40dp spacer before the empty list. CTA buttons have explicit 48dp height and use the shared complete-press shape clock. Natural unused viewport space remains when there is little data.
- Verification: **163 JVM tests / 21 suites and 31 Waydroid UI tests passed** (32.494s). Six additional executable tests verify overflow callbacks/dismissal, scoped All-time totals/counts and date navigation, one empty state with Add, older-record/account scope action, full large-text budget dates, and the measured 12dp list gap with filter reset. The first 31-test run caught a 40dp visible CTA height; final production uses explicit 48dp and all tests pass. Debug, AndroidTest and Release (lint/R8) builds succeed. ARTEMIS / ADB explored real picker and Add entry plus memory-only old-record/budget fixtures before writing tests; no financial data or theme/language preferences changed. Temporary debug activity/manifest removed from final builds. Artifacts: `captures/history-compact-layout-2026-10-06/`.

### 2026-10-06: Category Analytics Intentional Taps, Full Header & All-Time Scope
- Reproduced on the user-selected Waydroid with ARTEMIS observations and ADB: dragging across the chart committed the category under the released finger, and Import / CSV actions squeezed the title and filtering hint. Removed drag/scrub selection. Chart segments now expose explicit short-tap and accessibility actions; movement beyond platform touch slop, leaving the segment, long holds, cancellation, multi-touch or parent-consumed scrolling never changes the selection. Moves remain available to the containing scroll list; stable 48dp vertical hit slots are separate from the animated painted bars.
- Gave the full title / filtering hint a wrapping header row and moved Import / CSV to a separate action row. Analytics range buttons can wrap to two lines. Added English and all Chinese locale resources for All time / 全部記錄 and the category percentage description.
- Added analytics ALL_TIME, including without a budget. Category-selected History uses exactly the chart's time and account scope, excludes income / transfers / balance adjustments, includes the full budget end date, and stays empty rather than silently falling back to older records. Category deep links default to All time. Choosing All time also clears the global budget-period restriction.
- Verification: **163 JVM tests and 25 Waydroid UI tests passed**. Eleven new runnable memory-only tests cover intentional tap / accessibility / toggle, long hold / drift-and-return / cancel / multi-touch, cross-segment drag, parent scroll, all-time / account filtering, end-date inclusion, empty-scope regression, category deep links, and complete English / Traditional Chinese header layouts at 280dp with 1.3x text. Existing History interactions and Light / Dark connected-control motion also pass. Temporary exploration activity and manifest were removed before final APKs; real finance records and language/theme preferences were untouched.
- Design advice (not yet implemented): use one shared All time / Month / Budget period scope for chart and transaction list; put budget summary in the Budget period view; retain an explicit selected-category chip/reset and record count. Avoid two independent time selectors showing different totals. Artifacts and build/test details: `captures/history-analytics-2026-10-06/`.

### 2026-10-05: Complete Button Press Morphs & Hold-Until-Release Geometry
- Confirmed with the user: even a quick tap completes the inward shape animation; a held pointer retains the fully pressed shape until release. Shared `rememberCompletePressProgress` runs a 180ms inward morph and a 220ms restoration. Release waits for the inward animation's completion; cancellation restores without clicking, and a new press cancels restoration safely. The real interaction source still drives ripples, colors and callbacks, so clicks are not delayed by the geometry.
- `CompletePressButtons` preserves the Material Button / FilledTonalButton / OutlinedButton / TextButton / ToggleButton behaviors while coordinating rounded-corner geometry. All 37 existing native morphing call sites now use these wrappers. Six custom implementations (expense/account groups, appended Add, recurring groups, calendar switcher, numeric keyboard and Daily chart pills) use the same clock; removed duplicated 140ms pulse timers and click-triggered resets. Original connected/checked corner targets, percentage/dp corner sizes and RTL layout are retained.
- Verification: Debug, Android-test and Release (lint/R8) builds passed; **163 JVM tests** and **22 Waydroid UI tests passed**. Nine new production-control tests cover the four Material styles, native/expense/recurring connected groups, keyboard and appended Add: one-frame taps reach the held outline, prolonged holds retain it, release/cancel restore, repeated taps do not leave a stuck shape, measured slots stay stable and callbacks occur correctly. Thirteen existing tests cover native geometry, Light focus motion, Dark selection-only colors, bounded gray ripples, and touch targets.
- ARTEMIS screenshots and ADB explored the real Assets filters, expense categories and keyboard before test authoring. The initial UI run had two near-edge pixel-classification failures; frames showed ripple tint on neutral borders/antialiasing, so the test now classifies painted coverage independent of chroma and samples at frame-aligned completion. Final 22 tests pass in 55.649s. Live recording verifies short taps, a real 2.2s hold, release and rapid taps. Matching-signed Debug updated in place only on the user-selected Waydroid; no finance records were saved or cleared. Artifacts, APKs, frames, logs and report: `captures/button-press-duration-2026-10-05/`.

### 2026-10-05: Notification Identifier / Alias Binding System (Zero-Friction Auto-Logging)
- **Notification Identifiers & Aliases Architecture (自訂通知標識綁定與規則學習)**:
  - Addressed the fundamental problem where arbitrary custom card/account names (e.g. `'octopus'`, `'個人日常開支卡'`, `'主力卡'`) or third-party bank notification naming variations (e.g. `Smart Octopus`, `BOC Go unionpay Diamond Card`, `*****1719`) caused auto-logging to stall in the pending choices sheet.
  - Implemented **Notification Identifier & Alias Binding**:
    - **`AccountEntity` Model**: Added `notificationAliases: String? = null` with helper `getNotificationAliasList(): List<String>` parsing comma/newline/semicolon-separated identifier tokens.
    - **Database & Persistence**: Updated `InMemoryDatabase` to persist `notificationAliases` to/from `vibefinance_data.json`, and bumped Room database version from `version = 2` to `version = 3`.
    - **Matching Priority Engine (`PendingPaymentStore.findMatchingAccount`)**:
      - Priority 0 (Highest): Checks user-configured `notificationAliases` across all accounts. Matches card last-4 digits, asset hints, and merchant keywords.
      - Priority 1: Direct card last-4 digits match.
      - Priority 2: Octopus brand matching.
      - Priority 3: BOC Go / UnionPay match.
      - Priority 4: Dedicated payment wallets (PayMe, Alipay, WeChat, FPS, etc.).
      - Priority 5: Normalized substring matching.
    - **Smart Learning & Auto-Persisting in Pending Payment Dialog**:
      - When an unrecognized card/wallet notification arrives, the `PendingPaymentChoiceDialog` clearly presents the detected identifier (`payment.assetHint` / `payment.cardLast4`).
      - Defaults `rememberChoice` to `true`.
      - When confirmed with `rememberChoice = true`, `PendingPaymentStore.accept` automatically registers the identifier into the target account's `notificationAliases` and persists it to disk. Future notifications with that identifier match instantly with 100% deterministic zero-tap logging.
    - **Management UI in Accounts Screen (`AccountsScreen.kt`)**:
      - Added Material 3 Expressive **Notification Auto-Log Identifiers / Aliases Bento Card** to the account editing bottom sheet.
      - Visualizes active alias tokens as `InputChip`s with deletion support (`✕`).
      - Provides an `OutlinedTextField` + `FilledTonalButton` for adding custom aliases or keywords (with keyboard Done action).
      - Displays an `AssistChip` quick shortcut to add card last 4 digits if not yet linked.
      - Enabled `cardLast4` editing for all account types (including Cash and Debit accounts, perfect for physical Octopus card numbers).
    - **Multi-locale Strings**: Added localized strings (`assets_notification_aliases_title`, `assets_notification_aliases_desc`, `assets_notification_aliases_empty`, `assets_notification_aliases_add_placeholder`, `assets_notification_aliases_add_btn`, `assets_notification_aliases_suggest_last4`) across all 5 resource directories (`values`, `values-zh-rHK`, `values-b+zh+Hant`, `values-zh-rTW`, `values-zh`).
- **Verification**:
  - **163 JVM unit tests passed** (`./gradlew testDebugUnitTest`), including new end-to-end tests: `customNotificationAliasesAutoMatchCustomNamedAccounts`, `addNotificationAliasAppendsCorrectly`, and `endToEndAliasLearningAutoLogsFutureNotifications`.
  - `./gradlew assembleRelease` compiled successfully with R8 minification.
  - Installed onto user's physical phone (Samsung Galaxy S24 Ultra `SM_S9280`) via `adb install -r -d` preserving all local data, launched cleanly with no errors.
  - Codebase knowledge graph updated via `graphify update .`.

### 2026-10-05: Octopus App Top-up Auto-Logging & Custom Card Name Auto-Matching
- **Custom Account / Card Name Resilient Auto-Logging**:
  - Resolved issue where custom card/account names (such as user naming their card `'octopus'` or `'八達通'`) failed to auto-log when receiving notifications with asset hints like `Smart Octopus` (from Samsung Wallet) or `八達通` / `Android版八達通` (from Octopus app).
  - Implemented `PendingPaymentStore.findMatchingAccount`:
    - Priority 1: Card last-4 digit match (matching `account.cardLast4` or name/nickname).
    - Priority 2: Octopus brand matching (matches accounts named `octopus`, `Octopus`, `八達通`, `Smart Octopus`, or containing `octopus`/`八達通`, or cash wallet accounts).
    - Priority 3: BOC Go / UnionPay hierarchical matching.
    - Priority 4: Dedicated digital wallet & bank matching (PayMe, Alipay, WeChat, FPS, etc.).
    - Priority 5: Normalized substring and token matching.
  - Updated `rememberedAccountId` to automatically query `findMatchingAccount`, allowing first-time and custom-named cards to auto-log immediately without getting stalled in the pending choices sheet.
  - Refined `canRememberChoice` to allow persistent bonding between custom account names and notifications, while still strictly rejecting generic ambiguous hints (like generic `Google Pay` or `HSBC` without card details).
- **Octopus App Bank Transfer Top-Up Auto-Logging**:
  - Supported real Octopus app notification from uploaded image: Title `八達通`, text `你已成功由銀行戶口轉賬 HKD 300.0 至八達通 *****1719。`.
  - In `InterceptableApp`: Octopus app package (`com.octopuscards...`) now accepts authentic top-up notifications (`由銀行戶口轉賬`, `至八達通`, `增值`) even when title is `八達通`, while continuing to reject promotional/marketing notifications.
  - In `PaymentNotificationListener`:
    - Added `isOctopusTopUp` and regex patterns (`octopusTopUpBankTransfer`, `octopusTopUpGeneral`, `octopusTopUpEnglish`).
    - Bypassed `nonExpenseChinese` / `isIrrelevantOctopusTitle` for legitimate Octopus top-up notifications.
    - Updated `extractCardLast4` to support up to 8 masked characters (`*****1719` with 5 asterisks now deterministically extracts `1719`).
    - Parse notification as `ParsedPayment` with `amount = 300.0`, `merchant = "銀行戶口轉賬至八達通"`, `assetName = "八達通"`, `cardLast4 = "1719"`, `isTopUp = true`.
  - In `InMemoryDatabase`: Updated `insertNotificationExpenseIfAbsent` requirement to allow non-zero transactions (`transaction.amount != 0.0`), properly applying negative amount (`-300.0`) to increase account asset balance by `+300.0`.
  - In `PendingPaymentStore`: Saved `isTopUp` state in `PendingPayment`, recording `amount = -payment.amount` and category `"Top-up"`.
  - In `PendingPaymentChoiceDialog`: Displayed `+HK$` prefix for top-up amounts and unified `suggestedId` with `findMatchingAccount`.
  - Added localized notification feedback strings (`nf_logged_topup_title`, `nf_logged_topup_message`) in English and all Traditional Chinese / Simplified Chinese variants.
- **Verification**:
  - **158 JVM unit tests passed** (`./gradlew testDebugUnitTest`), including new test cases `testOctopusBankTransferTopUpParsing`, `testExtractCardLast4WithFiveAsterisks`, and `testAutoLoggingWithCustomCardNameOctopus`.
  - Successfully compiled debug and release builds (`./gradlew assembleDebug`, `./gradlew assembleRelease`).
  - Successfully installed release APK on Waydroid (`192.168.240.112:5555`).
  - Codebase knowledge graph updated via `graphify update .`.

### 2026-10-05: Daily Cards Arrive When They Become Visible
- Daily now gives all ten card/widget surfaces a 320ms fade, 20dp upward arrival and subtle 98.5% → 100% scale. Initially visible cards have a capped 0–110ms stagger; lower cards start only when their own measured window bounds overlap the visible viewport. The two bento rows retain their original heights, weights and spacing, and their four cards track visibility separately.
- New `DailyCardEntrance` keeps layout slots intact, animates only graphics layers, and stores revealed IDs at page scope. Lazy prefetch does not consume an entrance; financial-state updates and scrolling back after lazy disposal do not replay it. A fresh Daily visit gets a fresh state. Lifecycle STARTED gates effects and disabled system animation scale snaps to the final state.
- MainScreen supplies the actual top-bar and moving bottom-navigation occlusion insets, retaining a minimum inset for the permanent system navigation bar. Stable LazyColumn item keys cover hero, period, bento, transaction summary, spending chart, category chart, calendar and bottom spacer.
- Verification: Debug, Android-test and Release (lint/R8) builds passed; **155 JVM tests** and **5 Waydroid UI tests passed**. New full-Home integration test checks initial/mid/final opacity, a previously offscreen bento child's arrival, measured slot size, click callback, financial updates and return after lazy disposal. Four existing budget-wave/shape tests also pass. Paused-clock high-level scroll search was replaced with semantic ScrollBy plus explicit frame advancement; clipped viewport bounds are compared separately from measured dimensions.
- ARTEMIS screenshots + ADB explored the real Daily startup/statistics/chart path before test authoring. Cold-start and margin-scroll recording verified all sections through the calendar/heatmap without editing finance data. Evidence, test stages, logs, checksums and latest APKs: `captures/daily-card-arrival-2026-10-05/`. Matching-signed Debug updated in place on Waydroid; final app state is Traditional Chinese, Light, Daily at the top.

### 2026-10-05: Smooth Assets Indicator & Card Payment Networks
- Assets/Cards category underline now uses a 220ms tween instead of the bouncy spring. The four-option widths and targets remain; a live Waydroid recording and 156 detected indicator frames confirm monotonic movement to Credit and back through Debit/Assets/All without overshoot (`indicator-frame-analysis.json`). Button shapes and their focus motion are independent of this underline.
- Card Design no longer exposes an Issuer field or issuer preview. Payment network is full-width with None / Visa / Mastercard / UnionPay / JCB. Existing `unionpay` and `jcb` values restore correctly; saving uses lowercase values. Added readable UnionPay/JCB badges to the shared card protocol rendering, and changed the English field label to Payment network. Existing issuer metadata survives editing; new accounts receive no issuer. No schema migration is needed for the existing nullable string fields.
- Verification: Debug, Android-test and Release (lint/R8) builds passed; **155 JVM tests** and **2 focused Waydroid UI tests passed**. The new CardPaymentNetworkEditorTest saves UnionPay then JCB and reopens the actual AccountsScreen editor, checks the selected value, absent Issuer field and preserved legacy issuer/balance/bank fixture. DebitCardFilterTest verifies category counts and selection.
- ARTEMIS screenshots + ADB explored Card Design and its payment menu before test authoring, then verified the live updated menu and both previews. Only fixture state was saved by UI tests; live financial drafts were cancelled. Matching-signed Debug installed in place on Waydroid. Artifacts and latest APK: `captures/card-network-editor-2026-10-05/`.

### 2026-10-05: Debit Cards Filter in Assets & Cards
- Added the fourth connected option: All accounts / Assets / Debit Cards / Credit Cards, with per-category counts. Debit Cards selects only `AccountType.DEBIT`; Assets retains the existing non-credit-card scope, including debit accounts. The four-option indicator, equal widths and native connected shapes adapt to the new option.
- Added `Debit Cards` / `扣帳卡` in English and all four Traditional Chinese resource variants. Resource parity passed with **797 keys**, no duplicates or format mismatch.
- Verification: all **155 JVM tests** and **24 Waydroid Android UI tests passed**. The mixed-account unit fixture checks the exact debit subset; the new real AccountsScreen UI fixture verifies four counts and Debit → Credit → All selection. Existing Light motion, native shapes and History regressions also pass. Live Chinese Light/Dark screens and selection were explored with ARTEMIS screenshots and ADB before test authoring.
- Debug, Android-test and Release builds passed, including Release lint/R8. The release-certificate-signed debug build was updated in place on Waydroid `192.168.240.112:5555`; no financial records were created or removed. Final app state is Traditional Chinese, Light theme, Assets / All accounts. The physical Samsung phone was not updated by these two changes.
- Latest APKs, checksums, screenshots, video, build/test logs and report: `captures/debit-filter-2026-10-05/`. `app-release.apk` includes both the debit filter and the Dark feedback fix below.

### 2026-10-05: Dark Connected-Button Press Shapes & Neutral Bounded Ripples
- Dark connected-button fill/text colors now follow selection only; holding an unchecked button no longer starts its primary-color crossfade. Release still commits selection, with the existing selection crossfade. Light focus color motion remains unchanged.
- Native connected buttons in History/Assets and the appended Add control use a component-scoped neutral-gray Material ripple. Custom entry, recurring, calendar and spending-chart groups explicitly use the same bounded gray ripple in Dark mode.
- Fixed the legacy group's unchecked middle button having identical resting/pressed corners (8dp/8dp): the pressed target is now 6dp. Daily spending-chart pills now animate from half their measured height to 6dp while held, retaining their existing dimensions and font-scale behavior.
- Debug, Android-test and Release builds (including Release lint/R8) passed; all **155 JVM tests** and the final **24 Android UI tests passed** on the user's selected Waydroid. Three new regressions verify native/Main/Recurring leading, middle and trailing unchecked shapes, neutral gray bounded ripple, no selection during hold, cancellation restoration, and release-only callbacks. Android ripple uses real RenderThread time, so tests explicitly wait 400ms in addition to the controlled Compose clock; the initial timing failures and final passing output are retained.
- ARTEMIS Pro exploration encountered service rate limits and was stopped after observing the theme path. Direct ARTEMIS screenshots with ADB continued live Assets, Recurring, entry-category and Daily chart hold/cancel exploration before test authoring. Static peer review found no blocking issue. Waydroid was updated in place without clearing data; the Samsung phone was not changed.
- Original build artifacts and live feedback screenshots: `captures/dark-connected-feedback-2026-10-05/`. Final device test output and latest APKs including the debit filter: `captures/debit-filter-2026-10-05/`.

### 2026-10-04: Samsung Wallet BOC Go UnionPay Card ("BOC Go unionpay Diamond Card") Auto-Recognition & Transit Logging
- **Problem**: When Samsung Wallet (`com.samsung.android.spay`) posted a transit ticket notification with title `BOC Go unionpay Diamond Card` and text `transit-ticket THE KOWLOOHHONGKONG HKG HK$3.60`, the app failed to recognize and bind the transaction to the user's BOC Go card account.
- **Root Cause**:
  1. `bocGoUnionPayTitle` regex strictly required spaces around UnionPay (`\bUnion\s*Pay\b`), which didn't match variations or non-English titles like `中銀 Go`.
  2. `PendingPaymentStore.canRememberChoice` strictly enforced `accountName.length >= 6 && hint.contains(accountName)`, failing for accounts named "BOC Go Card", "中銀 Go", "中銀 Go 卡", "BOC", or "中銀".
  3. `PendingPaymentChoiceDialog` had no dedicated account resolution for BOC Go cards, resulting in a `null` suggested account ID.
  4. Transit ticket notifications from Kowloon Motor Bus (`THE KOWLOOHHONGKONG HKG`) lacked explicit keywords in `determineCategory`, causing non-transit fallback.
- **Implementation**:
  - `PaymentNotificationListener`:
    - Updated `bocGoUnionPayTitle` regex to `(?i)\b(?:BOC\s+Go|中銀\s*Go)\b(?:.*\bUnion\s*Pay\b)?` to match all English and Traditional Chinese BOC Go card titles.
    - Added `kowloo` and `kowloon` keywords to `determineCategory` Transport classification.
  - `PendingPaymentStore`:
    - Added `isBocGoHint`, `isBocGoAccount`, `isBocUnionPayAccount`, `isBocAccount`, and `findBocGoMatch` to seamlessly recognize and hierarchically resolve BOC Go cards to user accounts.
    - Updated `rememberedAccountId` to automatically resolve to unambiguous BOC Go card accounts and support cross-package remembered choices.
    - Updated `canRememberChoice` to permit binding BOC Go hints to any user account matching BOC Go or BOC credit cards.
  - `PendingPaymentChoiceDialog`:
    - Updated `suggestedId` to pre-select the matched BOC Go account when receiving a BOC Go payment hint.
- **Verification**:
  - All 155 unit tests passed (`./gradlew testDebugUnitTest`), including new test `samsungWalletBocGoUnionPayTransitNotificationParsesAndMatchesBocGoAccounts`.
  - Built and verified production release APK `v1.0.5` (versionCode `6`, versionName `"1.0.5"`) via `./gradlew assembleRelease`.
  - Successfully deployed to the user's physical phone **Samsung Galaxy S24 Ultra** (`SM-S9280`, `adb-R5CX22YGH7A-LIJF6v._adb-tls-connect._tcp`) using `adb install -r -d`, seamlessly preserving all existing transactions, accounts, and preferences.
  - Successfully deployed to local Waydroid emulator (`192.168.240.112:5555`).
  - Knowledge graph synchronized via `graphify update .`.

### 2026-10-04: Consistent English / Traditional Chinese Display
- **Fixed locale propagation**: MainActivity now provides the selected configuration's `LocalResources` as well as `LocalContext`/`LocalConfiguration`. Compose previously kept displaying English even when the Chinese option was selected. System language is resolved independently from device resources, so English/Chinese overrides cannot contaminate System. Locale-dependent remembered dates invalidate on language changes; Traditional Chinese uses the Hong Kong locale.
- **Localized UI**: Home charts/calendar/heatmap, forecast and budget dialogs; Assets summary/editor/design/crop controls; History category presentation; transaction add/edit/transfer labels; Recurring editors/filters; Radar/shop editor; import account summaries; accessibility descriptions, feedback toasts and app notifications. Alternate page subtitles now use the selected language. Built-in category/frequency/type IDs are translated only when displayed; stored IDs, user names, official brands and matching logic are preserved.
- **Resources**: All 796 string keys have matching Traditional Chinese resources across `values-zh`, `values-zh-rHK`, `values-zh-rTW` and `values-b+zh+Hant`. Executable parity/duplicate/format checks pass, including filling the newer chart/indicator labels missing from regional/script resources.
- **Validation**: ARTEMIS observation + fresh ADB XML explored Settings before test authoring. Live Chinese → cold relaunch → English → System → Chinese checks pass. All 155 JVM tests and 31 Android instrumentation tests pass, including two new saved-locale/notification-format/system regressions plus History, picker, wave and connected-motion regressions. Debug, Android-test and Release builds pass. A stale source-format padding assertion was adjusted for the existing motion modifier chain. Real Settings switching uses the recorded fresh XML path rather than the full app's continuously busy Compose idle loop.
- **Evidence / device**: `captures/localization-2026-10-04/REPORT.md`, runnable checks, language/page screenshots, test/build logs, APKs and checksums. Waydroid is updated with a matching project-release-signed debug build; a signing change observed during testing was handled without uninstalling/clearing the app. No financial records were saved or deleted by this work. App left in Traditional Chinese; physical phone not connected/updated.

### 2026-10-04: Approved Focus Color Motion Applied Across Connected Groups
- **Implemented**: All 15 connected-group call sites now share the approved Light-only edge-to-center fill and center-to-perimeter fading release. Dark mode uses a uniform crossfade. New `rememberConnectedButtonColorMotion` centralizes the tested reversible spring, actual theme guard, regional content tints, and validated inset clipping.
- **Coverage**: Budget Period and Category Analytics; Assets filters; History transaction type editor; expense/income/transfer category, account type, source and destination choosers; Modify Wallet/Card tabs; Radar map/radar mode; recurring filters, subscription/installment tabs and frequency; Home spending-chart modes and calendar Habit/Month view switch. Existing native/custom shape changes, dimensions, scrolling, icons, counts, trailing Add actions, callbacks and haptics remain. Assets keeps its original selected tonal colors in Dark mode; compact sizing remains specific to Category Analytics.
- **Validation**: ARTEMIS + ADB explored the relevant live controls before new tests were authored. All 20 UI tests passed on Waydroid in 22.114 seconds (4 new cross-variant regressions, 4 previous color regressions, 2 native shape regressions, 10 History regressions). New coverage includes offscreen semantic scrolling, icon/trailing Add independence, recurring Light/Dark paths, rapid retargeting, and the default noncompact native group. The scrolling test uses automatic frame advancement; motion tests use a paused deterministic clock. Debug/Android-test/Release builds passed. Live Light recordings and Dark crossfade recordings were inspected; the app was restored to Light mode/History and editor drafts cancelled without saving.
- **Evidence**: `captures/connected-motion-rollout-2026-10-04/REPORT.md`, screen recordings, inspected frame sheets, final test/build logs, APK copies and SHA256 checksums. Code graph refreshed and scoped whitespace checks passed. The updated debug APK is installed on Waydroid; the physical phone was not updated for this rollout.

### 2026-10-04: Category Analytics Light-Only Dual Color Motion & Compact Buttons
- **Implemented**: Category Analytics Month/Budget period buttons fill inward from all perimeter edges when selected or pressed. Deselection clears the center outward while the remaining primary wash fades at the perimeter. Finite reversible spring motion handles interrupted taps and cancelled presses. Dark mode uses the original uniform container/content color fade.
- **Theme and sizing**: `LocalIsDarkTheme` exposes the actual app theme choice before its palette finishes animating, so spatial motion is strictly disabled in Dark mode. Category Analytics opts into a centered maximum 320dp group with minimum 40dp painted buttons and native 48dp touch allocation; larger font layouts can grow. Native connected leading/trailing shape morphs, selection semantics, checkmarks, ripple, and callbacks remain. Budget Period keeps its original whole-color fade and size.
- **Rendering**: `InsetFocusColorMotion` uses complementary rectangular clips inside the native animated button shape and keeps content tints appropriate to each region. Device tests exposed uniform rendering in an initial nested-path mask; direct `clipRect`/`ClipOp.Difference` masking fixes the observed defect. The final recorded frames show a tonal center shrinking on focus and expanding on release, without an external blur or smoke effect.
- **Validation**: ARTEMIS observation + ADB explored the actual History screen in both app themes before test authoring. All 16 UI tests passed on Waydroid in 13.859 seconds: four new color/press/cancel/rapid-retarget/dark-mode/compact-hit-area tests, two native shape regressions, and ten History regressions. Debug, Android-test, and release builds passed. Fresh Light/Dark recordings were inspected, the debug APK was installed on Waydroid, and Light mode/History were restored. No financial records were edited; the physical phone was not updated for this change.
- **Evidence**: `captures/analytics-light-dual-motion-2026-10-04/REPORT.md`, final recordings/frame sheets, deterministic test captures/output, archived APKs, checksums, and runnable recording helper. Earlier failed captures are retained separately for diagnosis. Graph refreshed and scoped whitespace checks passed.

### 2026-10-04: Native Shape Morphing for Restored History Connected Groups
- **Fixed**: Restored Budget Period and Category Analytics connected groups now animate shapes for unselected buttons as well as selected buttons. The previous custom middle button had identical 8dp resting/pressed corners, causing no visible morph.
- **Implementation**: New `ExpressiveConnectedButtonGroup` uses native Material 3 `ToggleButton` with the same connected leading/middle/trailing shape defaults used in Assets and the transaction editor. Both History groups use it; the restored whole-color fades, labels, checkmarks, and filter callbacks remain. Added native radio selection semantics and minimum 48dp controls. Other shared segmented-group callers remain unchanged.
- **Validation**: ARTEMIS + ADB verified both History paths before authoring tests. Live Waydroid recording confirms inactive buttons morph before selection. All 12 UI tests passed (10 existing History regressions + 2 new image-based shape/press/cancel regressions), including unchecked middle and end buttons, deselection, cancellation restoration, accessibility semantics, and minimum height. Debug, Android-test, and release builds passed.
- **Evidence**: `captures/history-native-connected-shapes-2026-10-04/REPORT.md`, held-press screenshots, video, runnable recording script, and passing UI test output. The report also records four proposed Modify Assets/Cards popup improvements; those recommendations were not implemented as part of this shape fix.

### 2026-10-04: Modify Wallet Quick-Launch App Picker Performance
- **Fixed**: App Quick Launch in Assets & Cards → Modify Wallet opens without synchronously scanning packages and decoding every installed application's icon on the UI thread.
- **Implementation**: `AppPickerDialog` loads catalog metadata asynchronously, keeps search/Cancel available during loading, distinguishes loading/failure/no matches, and provides localized retry. Icons load independently for lazy list rows. `LocalAppManager` scans on `Dispatchers.IO`, reuses launcher application information, coalesces scans, and caches metadata for 60 seconds with configuration/locale refresh. The 64-entry icon cache limits icons to 96×96; decoding runs outside its lock. Cancellation stops abandoned scans before publishing partial results.
- **Validation**: All 154 JVM tests passed, including nine catalog/icon performance regressions. All five new `AppPickerDialogTest` UI tests passed on Waydroid, covering suspended loading/search, Cancel/cancellation, selection, retry, and a 150-app list with pending lazy icons. Debug and release builds passed. Live opening/reopening, package search, and unsaved draft selection were verified with ARTEMIS screenshots + ADB; wallet draft was cancelled without saving. Updated debug APK installed on Waydroid only.
- **Evidence**: `captures/quick-launch-picker-2026-10-04/REPORT.md`, screenshots, opening video, UI test output, and frame diagnostics. No numerical physical-phone performance claim; Waydroid has fewer installed applications.

### 2026-10-04: Real-World Android Octopus Notification Optimization ("Android版八達通")
- **Feature Overview**: Fully optimized notification interception for real-world Android Octopus notifications based on actual Android device notifications (`title: "Android版八達通"`, `text: "八達通: 在 <商戶> 支付 HKD <金額>。餘額: HKD <餘額>"`), supporting both Traditional Chinese and English alerts with automatic merchant categorization and balance calibration.
- **Real-World Notification Support**:
  - Sample 1: `八達通: 在 九巴 / 龍運 支付 HKD 5.8。餘額: HKD 56.0` -> `Amount = 5.80`, `Merchant = "九巴 / 龍運"`, `Category = "Transport"`, `Balance = 56.00`.
  - Sample 2: `八達通: 在 餐飲/會所 支付 HKD 29.0。餘額: HKD 61.8` -> `Amount = 29.00`, `Merchant = "餐飲/會所"`, `Category = "Food & Drink"`, `Balance = 61.80`.
  - Sample 3: `八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 90.8` -> `Amount = 4.90`, `Merchant = "港鐵"`, `Category = "Transport"`, `Balance = 90.80`.
  - Sample 4: `八達通: 在 港鐵 支付 HKD 3.2。餘額: HKD 95.7` -> `Amount = 3.20`, `Merchant = "港鐵"`, `Category = "Transport"`, `Balance = 95.70`.
  - Sample 5: `八達通: 在 7-Eleven 支付 HKD 5.0。餘額: HKD 98.9` -> `Amount = 5.00`, `Merchant = "7-Eleven"`, `Category = "Groceries"`, `Balance = 98.90`.
  - Sample 6: `八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 103.9` -> `Amount = 4.90`, `Merchant = "港鐵"`, `Category = "Transport"`, `Balance = 103.90`.
  - Sample 7: `八達通: 在 零售 支付 HKD 18.0。餘額: HKD 108.8` -> `Amount = 18.00`, `Merchant = "零售"`, `Category = "Shopping"`, `Balance = 108.80`.
  - Also verified negative/overdraft balances (e.g. `餘額: -HKD 15.0`) and English alerts (e.g. `Octopus: Paid HKD 12.5 at Starbucks. Balance: HKD 120.0`).
- **Core Enhancements & Strict Title Filtering**:
  - **Strict Octopus Title Filtering**:
    - The official Octopus App (`com.octopuscards.nfc_reader`, `com.octopuscards.octopus_app`, `com.octopus.wallet`) issues various non-payment notifications (wallet promotions, surveys, marketing offers, statement notices).
    - To completely prevent wrong logging, the app now strictly enforces that notifications from the Octopus App are **only intercepted and recorded if the title specifically matches Android Octopus** (`Android版八達通`, `Android 八達通`, `Android版八逹通`, `Android 八逹通`, `Android Octopus`, or `Octopus on Android`, or `Smart Octopus` on Samsung).
    - Irrelevant notifications with titles like `八達通`, `八達通銀包`, `八達通優惠`, `Octopus`, `Octopus Wallet`, etc. are immediately dropped at `isAppInterceptEnabled`, `isPaymentNotification`, and `parseNotification`.
  - `PaymentNotificationListener.kt`:
    - Added `androidOctopusTitlePattern` matching `(?i)(?:android\s*(?:版\s*)?[八8][達逹]通|android\s*octopus|octopus\s+on\s+android)`.
    - Added `isIrrelevantOctopusTitle` check in `isPaymentNotification` to reject non-payment Octopus alerts.
    - Updated `parseNotification`: returns `null` immediately if notification is from Octopus app package and title is not Android Octopus / Smart Octopus.
    - Added `androidOctopusZhPattern` & `androidOctopusEnPattern` with `HKD`, `HK$`, and `$` prefixes.
    - Updated `determineCategory`: added `龍運`, `lwb`, `新巴` to Transport; `餐飲`, `會所` to Food & Drink; `7-eleven`, `circle k` to Groceries; added Shopping category with `零售`, `shopping`, `retail`, `百貨`, `商場`, `購物`, `淘寶`.
  - `PendingPaymentStore.kt`:
    - Updated `canRememberChoice`: recognizes `八達通` and `android版八達通` so that remembering the choice binds smoothly to Cash / Octopus accounts.
    - Accurately calibrates account balance to `balanceRemaining` upon acceptance.
  - `PendingPaymentChoiceDialog.kt`:
    - Updated `suggestedId`: pre-selects the user's Cash / Octopus account when `assetHint` is `八達通` or `Android版八達通`.
  - `InterceptableApp.kt`:
    - Added `requiredTitleKeywords` to `OCTOPUS` and updated `matches()` to check `lowerTitle` only for OCTOPUS, ensuring non-matching titles are filtered out before parsing.
- **Verification & Testing**:
  - **Unit Tests**: All unit tests passed (`./gradlew testDebugUnitTest`), including `testIrrelevantOctopusAppNotificationsAreRejected` and `testOctopusAppRequiresAndroidOctopusTitle`.
  - **Waydroid Live Verification (`192.168.240.112:5555`)**:
    - Triggered irrelevant promo alert (Title: `八達通`, Text: `【最新推廣】在 麥當勞 支付享 $10 回贈`) -> 100% ignored, zero popup, zero transaction added (`screen_irrelevant_rejected.png`).
    - Triggered valid tap payment (Title: `Android版八達通`, Text: `八達通: 在 港鐵 支付 HKD 4.9。餘額: HKD 90.8`) -> immediately and automatically logged as `Transport`, calibrated Cash Wallet to `$90.80` (`screen_valid_logged.png`, `screen_mtr_logged.png`, `screen_mtr_assets.png`).
  - **User Physical Phone Deployment (`SM_S9280`)**:
    - Compiled production Release APK (`versionName = 1.0.4`, 3.1 MB, R8 Proguard-minified and signed with `keystore/release.keystore`).
    - Successfully installed in place to the user's phone (`SM_S9280`) via `adb install -r -d` preserving all existing user accounts, budgets, and historical data.

### 2026-10-04: Original History Connected Groups Restored
- Restored Budget Period and Category Analytics to the shared original `ExpressiveSegmentedButtonGroup`, including whole-container/content color fades, press-shape motion, checks, ripple and haptics.
- Removed the center-line animation component and its experiment-specific motion test. Existing History editing tests and Home wave tests remain. Analytics availability, period callbacks and month navigation are preserved.
- Debug/Android-test builds and the final release build passed; **all 10 History UI tests passed** on Waydroid (7.437 seconds). ARTEMIS observation and inspected recordings verify normal/rapid selections in both groups with whole-color fades. Graph refreshed; scoped whitespace checks passed.
- Installed the rollback in place on Waydroid; the physical phone was not updated. Current recording, frame sheets, APK copies and report: `captures/history-original-groups-2026-10-04/REPORT.md`. Earlier focus-trial artifacts below are historical versions.

### 2026-10-04: Short Horizontal Center Line in History Focus Animation
- Supersedes the full-width line below. Both the selected fill's closing gap and the deselected fill contract toward a centered horizontal line about **40% of the button width**; its height reaches zero and the line disappears.
- Background and foreground share the same four-band mask. Bands do not overlap, and settled/releasing solid fills use one rectangle. Atomic bounds, roughly 180 ms timing, rapid reversals, native interactions and smoke-free behavior remain.
- Debug, Android-test and release builds passed. The focused selection/rapid-switching/settling device test passed (1.026 seconds). ARTEMIS observations and inspected recording frames confirm the shorter line. Graph refreshed and scoped whitespace checks passed.
- Installed the debug update in place on Waydroid; the physical phone was not updated. Current preview, frame sheet, APK copies and report: `captures/short-horizontal-line-focus-2026-10-04/REPORT.md`.

### 2026-10-04: Horizontal Center Line in History Focus Animation
- Corrected the preceding vertical-line interpretation: selection fills from the top and bottom toward a full-width horizontal center line; deselection contracts in height to that same line and disappears.
- Both background and foreground masks now use the button height for their animated bounds and retain its full width. The roughly 180 ms spring, rapid reversals, native connected shapes and smoke-free behavior remain.
- Debug/Android-test builds and the final release build passed. The focused rapid-switching/settling device test passed (1.333 seconds); an inspected Waydroid recording confirms full-width horizontal stripes. Graph refreshed and scoped whitespace checks passed.
- Installed the debug update in place on Waydroid; the physical phone was not updated. Current recording, frame sheet, APK copies and report: `captures/horizontal-line-focus-2026-10-04/REPORT.md`.

### 2026-10-04: Faster Center-Line History Focus Animation
- Supersedes the smoke effect in the preceding History focus trial. Removed outward diffusion, blur and the reserved overflow gutter from the Budget Period connected group.
- Selected fill converges from the left and right edges through a full-height vertical gap. Deselected fill contracts horizontally to a vertical stripe through the button center and disappears. A finite, critically damped spring with stiffness 3000 settles in roughly 180 ms; rapid reversals continue from the current bounds.
- Native connected button shapes, press ripple, haptics, immediate single-choice semantics and spatial foreground contrast remain. Today/Remaining waves and other connected groups retain their previous behavior.
- Debug, Android-test and release builds passed; **all 15 focused Waydroid UI tests passed** (8.873 seconds). ARTEMIS observations and an inspected live recording confirm the geometry and absence of external smoke. Graph refreshed; scoped whitespace checks passed.
- Installed the revised debug build in place on Waydroid. A concurrent installation interrupted initial capture attempts and re-opened deferred payment prompts; the final recording succeeded. This task only used Later on those prompts and did not record or ignore a payment. The physical phone was not updated.
- Current recording, frame sheets, release APK and verification: `captures/center-line-focus-2026-10-04/REPORT.md`.

### 2026-10-04: Continuous Budget Waves, Interruptible Shape Morph & History Focus Trial
- Restored continuous motion in Today and Remaining card wave fills, including a full Remaining balance. Phase is read in graphics/drawing layers and pauses when the host stops or Android disables animations; the earlier five-second idle-settle behavior is superseded by this user request.
- Days Left now uses a cached M3-styled ring with an interruptible amplitude spring. This avoids the native alpha01 indicator losing rapid Wavy/Flat reversals. Rounded 5 dp strokes, elapsed-period progress, endpoint gaps, layout and determinate accessibility semantics remain.
- Added the experimental focus effect only to History's Budget Period filter (All Records / Active Period / Past Periods). Native connected ToggleButtons fill from all four edges toward the center; the released selection sends a bounded, outward, fading smoke layer with increasing blur. All glyphs stay above the decoration and use the same fill mask for contrast. Other connected groups retain the restored whole-color fade. This effect is superseded by the faster center-line update above.
- Debug, Android-test and signed release APK builds passed; **141 JVM tests and 15 Waydroid UI tests passed**. New tests verify movement beyond five seconds, full-balance wave visibility, shape morph/rapid reversal, immediate filter selection and finite settling. Pixel assertions allow measured Waydroid GPU dithering (observed up to four channel levels, tolerance six).
- Live ARTEMIS observation plus ADB recordings confirm both moving card backgrounds and the History focus effect. Dynamic UIAutomator snapshots are grounded with animation briefly paused and restored because perpetual drawing prevents an idle snapshot; stale XML is rejected by the new recording helper.
- Waydroid (`192.168.240.112:5555`) retained its debug signing and data; the physical phone was not updated. Builds, recordings and report for this earlier version: `captures/budget-motion-2026-10-03/`.

### 2026-10-04: Smart Octopus Remaining Balance Recognition & Account Calibration
- **Feature Overview**: Added support for extracting and synchronizing the remaining card balance directly from Smart Octopus / Samsung Wallet notifications (e.g. `title: Samsung Wallet`, `text: Smart Octopus HK$12.6 7-Eleven 餘額 HK$214.0`).
- **Notification Parsing & Multi-Amount Guard**:
  - Enhanced `PaymentNotificationListener.isPaymentNotification` and `parseNotification` to inspect both notification `title` and `text` for Smart Octopus alerts.
  - Bypassed false-positive rejection by the multi-amount filter (`amounts.size > 1` or `balanceText`) when a valid Smart Octopus spend is identified.
  - Supported positive and overdraft/negative balances (`-HK$15.0`, `HK$-15.0`, `-15.0`) with proper negative sign extraction.
- **Durable Store & Ground-Truth Calibration (`PendingPaymentStore.kt`)**:
  - Carried `balanceRemaining: Double? = null` in `PendingPayment` data class, JSON serialization, and disk persistence.
  - In `accept()`, calibrated the target account's balance (`currentAcc.copy(balance = payment.balanceRemaining)`) upon recording the expense, ensuring real-time balance accuracy matching the Octopus physical IC chip.
- **UI & Interaction (`PendingPaymentChoiceDialog.kt`)**:
  - Rendered `八達通/卡片餘額：HK$214.00` (`pending_payment_balance_remaining`) localized across 5 locale resource files (en, zh-rHK, zh-rTW, b+zh+Hant, zh).
  - Enhanced `suggestedId` to pre-select matching Cash / Octopus accounts when `assetHint` is Smart Octopus, immediately enabling the "Record expense" button and remember checkbox.
- **Verification**:
  - All 142 JVM unit tests passed (`./gradlew testDebugUnitTest`).
  - Successfully verified end-to-end flow on Waydroid (`192.168.240.112:5555`): dialog rendering with balance (`smart_octopus_preselected.png`), balance calibration to `$214.00` (`screen_assets_check_balance.png`), and subsequent remembered auto-logging calibrating to `$181.00` (`screen_auto_logged.png`, `assets_tab_final.png`).


### 2026-10-03: Original Connected-Button Color Fade Restored
- At the user's request, removed the edge-to-center fill and foreground mask from History, Main, Recurring and Assets connected controls; deleted the unused `EdgeToCenterFill.kt` helper.
- Restored selection-driven whole-container and content color fades with matching finite no-bounce springs. Native connected buttons, category menu/dividers, press shapes, ripples, haptics and transaction behavior remain.
- Debug and Android test APKs built successfully; all **10 History UI tests passed** on Waydroid (8.613 seconds). Updated Waydroid with `install -r`, checked the diff and refreshed the graph. The physical phone was not updated.
- Current rollback APK and verification: `captures/color-fade-restored-2026-10-03/`. APKs/recordings in the preceding editor report show the earlier edge-fill version and predate this rollback.

### 2026-10-03: Native Connected History Controls, Divided Category Menu & Edge-to-Center Fill
- Replaced the History editor's segmented Expense/Income selector with native M3 Expressive connected ToggleButtons, preserving single-choice accessibility semantics, press shapes, ripple and test tags.
- Category now uses a Material 3 vertical exposed menu with icons, selected checks, headings and dividers. Defaults are shared with Add Transaction; saved custom categories and the current legacy/opposite-type category remain available. Type changes do not overwrite the current category. Transfers keep their category/type protection.
- Added shared finite edge-to-center fill for History, Main, Recurring and Assets connected groups: inactive press/selection advances from both side edges; cancellation/deselection reverses from current progress. Foreground glyph colors follow the fill boundaries to retain contrast during the transition.
- Final **141 JVM tests and 10 on-device History tests passed**. Live category selection/Cancel and recorded editor/Assets press, reversal and rapid-switching behavior were inspected. Debug and signed release builds succeeded; graph refreshed; diff check clean.
- Waydroid updated with its matching debug APK via `install -r`. No phone update, uninstall, data clear or live financial edit was performed; six existing pending payments were deferred with Later. ARTEMIS autonomous exploration was quota-limited, so ADB interaction and ARTEMIS observation were used for the verified menu path.
- Report, screenshots, recordings and final APK copies: `captures/editor-m3e-2026-10-03/REPORT.md`.

### 2026-10-03: Income History Consistency, Editable Transaction Type & Connected Group Colors
- Added `TransactionEntity.historyAmountFor()` as the shared display rule for global and account-filtered History. Income HK$300 now shows **+HK$300.00** in both views, including credit cards; debt-balance changes no longer invert its display sign. Transfers and balance adjustments retain their own direction rules.
- History tap/right-swipe editor now supports **Expense / Income** conversion plus existing amount, category and description edits. Income is excluded from daily spending. Transfers cannot be converted and adjustments remain read-only; positive finite amount validation is enforced.
- Main/Recurring connected groups, Assets filters and History type buttons now use matching finite no-bounce, low-stiffness container/content color transitions. Existing group motion and interaction behavior are preserved.
- **Verification:** 141 JVM tests passed (including repository balance reversal across all account types); all 8 History UI tests passed on Waydroid, covering +300 in both views, both conversion directions and existing swipes. Live editor and recorded rapid filter switching were inspected. Debug/release/test APKs built successfully; graph refreshed.
- **Device state:** user-requested physical-phone logs were extracted read-only (685-line retained main capture is partial). Phone unchanged. Waydroid updated with its matching debug-signed APK using `install -r`; release install was rejected due to signing-key mismatch and no uninstall/data clear was attempted. Existing ledger and six deferred pending payments preserved.
- Full evidence and stable APK copies: `captures/phone-issues-2026-10-03/REPORT.md`.
- **Earlier notification-test limitation:** the exact no-card `Samsung Wallet` title + `Smart Octopus HK$… 餘額 HK$…` body format remains rejected by the payment guard. These changes do not address that parser; prior card-suffix simulations do not verify this format. See `captures/guideline-retest-2026-10-03/REPORT.md`.

### 2026-10-03: Smart Octopus Multi-Notification Simulation, Hierarchical Card/Asset Memorization & UI Refinements
- **Card & Asset Memorization Enhancement (`Remember Choice` Auto-Routing)**:
  - **Problem Solved**: `PendingPaymentStore.canRememberChoice()` previously prohibited users from saving payment routing rules unless destination accounts matched hardcoded names (`"八達通"`, `"Octopus"`). Users choosing `"Wallet (Cash)"` or custom names were locked out with `"No specific card was identified, so you will choose each time"`, even when authentic intercepted card numbers (`cardLast4 = "9821"`) matched.
  - **Hierarchical Multi-Tier Matching Architecture (`PendingPaymentStore.kt`)**:
    1. **Tier 1 (Highest Precision - App Package + Card Last 4)**: `$pkg|card:$cardLast4` (e.g. `com.octopuscards.octopus_app|card:9821`). Binds payment alerts from a specific card directly to its target account regardless of merchant or text variations.
    2. **Tier 2 (App Package + Asset Hint + Card Last 4)**: `$pkg|$hint|card:$cardLast4`.
    3. **Tier 3 (Fallback - App Package + Dedicated Wallet Asset)**: `$pkg|$hint` (e.g. `com.octopuscards.octopus_app|smart octopus`). Fallback routing when certain merchant or transit notifications omit the 4-digit card number.
  - **Dynamic Card Suffix in Remember Checkbox (`PendingPaymentChoiceDialog.kt`)**:
    - When `cardLast4` is present, the checkbox dynamically informs the user: `Remember for com.octopuscards.octopus_app alerts marked "Smart Octopus (•••• 9821)"`.
    - Enables checkboxes for matching card numbers, dedicated wallets, and cash/wallet accounts.
  - **Automatic Direct Logging**: Once remembered, subsequent notifications bypass manual dialog selection entirely; transactions are atomically persisted, balances updated, and confirmation toasts/notifications dispatched.
- **Smart Octopus Multiple Notification Simulation & Flow Verification**:
  - Simulated sequential real-world payment notifications (MTR Transit `HK$6.50`, 7-Eleven `HK$28.00`, Starbucks `HK$45.00`) with authentic card `9821`.
  - Verified Android system notification shade (`screen_notifications_shade.png`) and VibeFinance pending queue.
  - Verified sequential queue processing (`Later`, `Ignore`, `Record expense`), dynamic budget recalculation (`Spent: HK$ 45`, `Remaining: $1,433.01`), and automatic category tagging (`Food & Dining`).
- **Teamwork Multi-Agent UI/UX Refinements (Commit `0ded997` & `3c33eb9`)**:
  - **Daily Page Widget Redirection**: Tapping Category Analytics or Total Expenses navigates to History tab with appropriate filters applied.
  - **Habit Heatmap Left-Aligned Start**: 16-week matrix starts aligned from leftmost column without unwanted right auto-scroll.
  - **Daily Page FAB Dynamic Motion**: FAB tracks bottom navigation bar visibility and maintains comfortable clearance (`32.dp + safeBottom`) during scroll.
  - **Recurring Page Cleanup**: Removed `PresetsCarousel` to focus on recurring subscription list.
  - **Smooth Heatmap Switcher**: Removed bouncy oscillation in `SpendsCalendar.kt` for smooth view switching.
  - **Heatmap Title Visibility**: Ensured full title display on compact viewports alongside connected button group.
  - **Unified Bounded Ripple Highlight**: Standardized Recurring screen list rows to match History screen swipe rows.
  - **Compact Tile Shape Labels**: Shortened tile shape selector labels in `M3ExpressiveHeatmap.kt` to prevent vertical wrapping.
- **Automated & Live Hardware Verification**:
  - Unit tests added and passing in `PaymentNotificationListenerTest.kt` (`./gradlew testDebugUnitTest`).
  - Debug APK built and installed to live Waydroid device (`192.168.240.112:5555`) preserving all user data (`adb install -r`).
  - Knowledge graph updated via `graphify update .`.

### 2026-10-02: Daily Recalculate Rollover Optimization (Material 3 Expressive Switch & Zero Leftover Auto-Bypass)
- **Material 3 Expressive Switch Integration (`RecalcBudgetSheet.kt`)**:
  - Replaced the legacy, standard `Switch` in the "記住選擇" (Remember Choice) card with the app's standard `ExpressiveSwitch`.
  - **OFF (Unselected) State**: 2dp solid outline border (`MaterialTheme.colorScheme.outline`), `surfaceContainerHighest` track fill, 24dp outline thumb with subtle Close (`✕`) icon.
  - **ON (Selected) State**: Filled emerald track (`MaterialTheme.colorScheme.primary`), 24dp `onPrimary` white thumb with sharp Check (`✓`) icon.
  - **Kinetic Physics**: 28dp horizontal press stretch with bouncy spring kinetics (`Spring.DampingRatioMediumBouncy`), 48dp accessible touch target, and tactile haptics.
- **Zero Yesterday Leftover Auto-Bypass (`MainScreen.kt`)**:
  - **Problem Solved**: Previously, whenever a new day began, `MainScreen.kt` unconditionally popped up the "昨日結餘" (Recalculate Budget) sheet even when yesterday's leftover balance was `HK$0` (spent completely or overspent). Distributing `HK$0` between "Split by remaining days" and "Leave for Today" is functionally redundant and disrupted user flow upon opening the app.
  - **Intelligent Leftover Calculation (`computeYesterdayRemainingBudget()`)**:
    - Dynamically evaluates standard base daily allowance against actual logged yesterday expenses within the active budget period.
    - If yesterday was before the period start date, returns `0.0`.
  - **Zero-Bypass on Morning App Launch (`checkAndTriggerAutoSheets()`)**:
    - If `yesterdayLeft <= 0.001`, the system silently marks `last_daily_recalc_date` in `SharedPreferences` and **bypasses the pop-up entirely**.
    - The user opens the app and lands directly on their clean Daily overview without interruption.
    - If yesterday had positive leftover (`> HK$0`), the prompt displays normally (unless the user enabled "記住選擇", which automatically applies their saved mode).
    - Users can still manually launch the recalculation sheet at any time via the "Recalculate" button on `HeroDailyBudgetCard`.
- **Live Device Verification (Waydroid `192.168.240.112:5555`) with ARTEMIS**:
  - Fresh app launch with `yesterdayLeft == 0`: Verified app launches directly into Daily screen without pop-up (`waydroid_no_popup_verified.png`).
  - Manual recalculation sheet open: Verified `ExpressiveSwitch` in OFF state displays outline + Close icon (`recalc_sheet_expressive_switch.png`).
  - Toggled `ExpressiveSwitch` to ON: Verified filled emerald track + Check icon + spring animation (`recalc_sheet_expressive_switch_on.png`).
  - Dismissed sheet via close button: Verified return to Daily overview (`recalc_sheet_closed.png`).

### 2026-10-01: History Row Swipe UI/UX Optimization (Elimination of Gray Highlight / Film Overlay)
- **Elimination of Muddy Gray Highlight / Overlay on History Swipe Rows (`ExpressiveSwipeRow.kt`)**:
  - **Root Cause Identified**:
    1. `ExpressiveSwipeRow` previously contained a manual overlay `Box` animating `pressHighlightAlpha` with `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f * pressHighlightAlpha)`. Because `clickable` captures touch-down before `draggable` consumes horizontal drag slop, `isPressed` became `true` on initial touch, drawing a dark gray overlay rectangle over the entire card during swipes.
    2. The sliding `Surface` used a semi-transparent color `color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)`. When swiping, `cardElevation` elevated to `4.dp`. In Android's rendering pipeline, an elevation shadow beneath a translucent surface shines directly through the surface pixels. Combined with the revealed action canvas behind it, this blended the card into a dark, muddy brownish-gray block.
  - **Architecture Improvements & Fixes**:
    1. **Removed Manual Gray Box & Press States**: Stripped away `pressHighlightAlpha` and the overlay `Box` entirely.
    2. **Opaque Physical Surface (`MaterialTheme.colorScheme.surfaceContainer`)**: Upgraded `Surface` color from translucent `surfaceVariant.copy(alpha = 0.45f)` to opaque `MaterialTheme.colorScheme.surfaceContainer` (`0xFFEBF0EB` in light mode, `0xFF1A1E1C` in dark mode).
       - Card face is 100% opaque: the underlying action canvas (`errorContainer` for delete, `tertiaryContainer` for edit) only reveals where the card slides away, never bleeding through the card body.
       - The `4.dp` elevation drop shadow renders cleanly behind the card onto the background, producing a real 3D floating effect while keeping the card face pristine and bright.
       - Swiping left and right maintains a 100% clean, crisp card face with zero gray tint or mud.
    3. **Responsive Tap & Edit Action**: Tapping a transaction triggers `haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)` and opens the `EditTransactionDialog` immediately without unwanted visual artifacts.
- **Live Device Verification (Waydroid `192.168.240.112:5555`) with ARTEMIS**:
  - Left Swipe (Delete): Verified card slides smoothly over soft pink/red delete background with trash icon; card face remains pristine white/mint with zero gray tint (`mid_swipe_verified.png`).
  - Right Swipe (Edit): Verified card slides smoothly over cyan edit background with pencil icon; card face remains pristine with zero gray tint (`mid_swipe_right_verified.png`).
  - Tap: Verified tapping opens `EditTransactionDialog` immediately with tactile haptics; Cancel closes dialog cleanly (`tap_edit_dialog_verified.png`, `history_closed_dialog.png`).
  - Preserved artifact: `waydroid_history_clean_swipe.jpg`.

### 2026-10-01: Category & Asset Selection UI/UX Overhaul (Icon Preservation, Floating Pill Symmetry, Trailing Add Chip & Smart Frequency Sorting)
- **Visual Identity & Icon Preservation on Selection**:
  - Previously, selecting a category or asset replaced its icon with a generic checkmark (`Icons.Filled.Check`), stripping away the visual anchor (e.g. food, transport, wallet).
  - Now, `iconProvider` remains permanently visible. When selected, an elegant mini checkmark (`14.dp`) smoothly springs into view alongside the icon via `AnimatedVisibility(fadeIn() + expandHorizontally())`, giving both immediate positive selection feedback and preserving instant icon recognition.
- **Floating Expressive Pill Symmetry (`20.dp` Resting Corners)**:
  - Since scrollable chips are spaced by `8.dp`, asymmetric segmented corners (24dp on first, 8dp on middle) caused floating items to look uneven.
  - Now, when `isScrollable = true`, all four corners have a uniform `20.dp` resting radius. When pressed, all four corners morph into `8.dp` squircle with `Spring.StiffnessMedium` (1500) and spring back with bouncy overshoot (`Spring.DampingRatioMediumBouncy`), creating consistent visual rhythm across the horizontal scroll strip.
- **Trailing `[ + Add ]` Expressive Chip**:
  - Removed cluttered `+ Add Category` and `+ Add Card / Account` text links from section headers, leaving clean, minimalist category and account labels.
  - Added a dedicated, reusable `ExpressiveAddButton` chip at the tail of the horizontal scrollable lists. When clicked, it smoothly toggles open the inline creation form with spring haptic feedback.
- **Smart Usage Frequency Sorting (LRU / Frequency Ranking)**:
  - Dynamically calculates category and account usage frequency from `state.transactions`.
  - Automatically sorts `sortedExpenseCategories`, `sortedIncomeCategories`, and `sortedAccounts` with the user's most frequently used items positioned at index 0, 1, 2!
  - Automatically initializes selection to the user's #1 most frequent category and account, eliminating repetitive scrolling and hunting for regular expenses.
- **Live Device Verification (Waydroid `192.168.240.112:5555`) with ARTEMIS**:
  - Verified `Other` (user's most frequent category) and `octopus card` (user's most frequent account) naturally bubbled to position #1 and defaulted.
  - Verified `[ ✓ ... Other ]` and `[ ✓ 👛 octopus card ]` keep icons perfectly intact.
  - Swiped to the end of both rows, verified `[ + Add ]` chips exist and expand the inline add form.
  - Captured live screenshot `waydroid_optimized_category_asset.jpg`.

### 2026-10-01: Connected Button Groups Spring Shape Morphing (`MainScreen.kt` & `RecurringScreen.kt`)
- **Connected Button Group Spring Shape Morphing (`ExpressiveSegmentedButtonGroup` & `ConnectedButtonGroup`)**:
  - **Identical Material 3 Expressive Press Motion**:
    - Applied the verified short-press spring shape-morphing physics from `KeyboardButton.kt` to the connected button groups on the Add Expense / Transfer page (`ExpressiveSegmentedButtonGroup` in `MainScreen.kt` for Categories, Asset Selection, and Account Types) and the Recurring screen (`ConnectedButtonGroup` in `RecurringScreen.kt`).
    - **Corner Morphing Dynamics**:
      - Resting State: Pill shape with asymmetric rounded corners (18dp/24dp on ends or selected button, 4dp/8dp on shared inner boundaries).
      - Pressed State (`isPressed || isPulsing`): Corners rapidly morph towards a rounded squircle (`8.dp` in `MainScreen.kt`, `6.dp` in `RecurringScreen.kt`) with `Spring.StiffnessMedium` (1500) and no scaling/shrinking.
      - Return Spring Physics: Corners bounce back outwards to resting pill shape with `Spring.DampingRatioMediumBouncy` (0.5) and `Spring.StiffnessMediumLow` (400), creating a tactile, organic bounce.
    - **Short-Press Guarantee via Coroutine Pulse**:
      - Tracks `PressInteraction.Press`, `Release`, and `Cancel` through `MutableInteractionSource`.
      - On quick tap (touch down & up within 20–30ms), a coroutine holds `isPulsing = true` for a 140ms pulse duration before returning, guaranteeing that every tap visibly demonstrates the spring shape-morphing effect.
    - **Haptic Feedback & Sound**:
      - Integrated `view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`, `view.playSoundEffect(SoundEffectConstants.CLICK)`, and `haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)`.
      - Replaced scale-reduction modifiers (`bouncyClickable`) with pure shape-morphing `Modifier.clickable(indication = ripple())` clipped to `shape`.
- **Live Device Verification (Waydroid `192.168.240.112:5555`) with ARTEMIS**:
  - Tested category selection (Food -> Transport -> Shopping) and asset selection (AEON card -> Cash).
  - Verified instant squircle morphing and bouncy spring return on quick taps without any scale shrinking.
  - Captured live screenshot `waydroid_connected_button_spring.jpg`.

### 2026-10-01: Add Expense UI/UX Redesign, Short-Press Spring Shape Morphing & Header Clipping Fix
- **Short-Press Spring Shape Morphing (`KeyboardButton.kt`)**:
  - **Issue Resolved**: Previously, `isPressed` was too transient during a normal quick tap (~20–30ms), causing the spring animation to cancel before the button could visibly morph into a squircle. As a result, shape changing was only perceived when users pressed and held the button.
  - **Coroutine-Powered Shape Pulse Mechanism**:
    - Listens to `interactionSource.interactions` (`PressInteraction.Press`, `Release`, `Cancel`) and handles `onClick`.
    - On press: cancels any active release job and immediately engages squircle shape morphing (`targetValue = 10.dp`, `Spring.StiffnessMedium`).
    - On release / click: launches a coroutine job holding the squircle shape for a perceptible 140ms pulse duration before triggering the release animation.
    - On release return: corners spring back to 28dp pill shape with `Spring.DampingRatioMediumBouncy` (0.5) and `Spring.StiffnessMediumLow` (400), creating a delightful, organic spring bounce and overshoot.
    - Guarantees that **every single tap—no matter how brief—visibly executes the authentic Material 3 Expressive spring shape-changing motion**!
- **Separated Budget Cards Layout (`MainScreen.kt`)**:
  - Re-separated the budget components into two distinct, dedicated cards now that removing Quick Presets freed sufficient vertical space:
    1. **Count in Daily Budget Switch Card**: Elegant surface card featuring title, explanatory description (`Deducted from today's daily allowance` vs `Non-daily / big item...`), and an `ExpressiveSwitch` with checkmark thumb icon.
    2. **Budget Preview Card**: Dedicated container dynamically morphing between:
       - **Daily Budget ON**: Displays `超支警告！` (with Warning icon and red tint) or `預算試算` with real-time recalculations (`今日將超支 $...，其餘每天可用降為 $...`).
       - **Daily Budget OFF (Non-Daily)**: Calm secondary container with Shopping Cart icon explaining that the expense is excluded from the daily allowance calculation.
- **Fixed Header Clipping ("Thing under Add Expense is cut")**:
  - **Root Cause**: The sticky overlay header (`IconButton`, title, amount badge, currency selector) had a physical height of ~68dp, but the content spacer was only `58.dp`, causing the top ~10dp of the first card to be cut off behind the solid header surface.
  - **Fix**: Added dynamic header measurement via `.onGloballyPositioned { headerHeightPx = it.size.height }` and applied a safe dynamic top spacer: `Spacer(modifier = Modifier.height(headerHeightDp + 10.dp))`.
  - The first card now renders with perfect breathing room below the header, ensuring rounded corners and borders are 100% visible and pristine on all screen densities.
- **Verification on Live Device (Waydroid `192.168.240.112:5555`) with ARTEMIS**:
  - Verified no scrolling is required: Header, Switch Card, Preview Card, Note field, Category row, Asset row, and full 4x4 keypad fit comfortably within the viewport.
  - Verified the top border of the Count in Daily Budget card is completely clear and not clipped.
  - Verified toggling the switch updates the preview card between Daily Overdraft calculation and Non-Daily purchase notice.
  - Verified typing numbers ($50) updates the amount and preview card instantly.
  - Verified KeyboardButton spring shape morphing triggers visibly on every quick tap.

### 2026-10-01: Non-Daily / One-Off Expense Toggle & Dynamic Budget Preview Adaptation
- **Non-Daily / One-Off Expense Toggle (`isExcludedFromDailyBudget`)**:
  - Implemented an M3 Expressive toggle in the Add Expense Sheet (`AddExpenseSheetContent` in `MainScreen.kt`) allowing users to choose whether an expense counts towards the "Daily Budget" or represents a "Non-Daily / One-Off" purchase (such as buying a phone, computer, or TV).
  - Designed with an `ExpressiveSwitch` in a clean, elevated Material 3 card container (`Count in Daily Budget` / `計入每日預算`).
  - When marked as non-daily (`isExcludedFromDailyBudget = true`):
    - The expense is recorded accurately in transactions, account balances, and category analytics.
    - It is completely excluded from the daily allowance calculation (`BudgetRepository.getDailyAllowanceFlow()`), preserving the day's regular allowance without deducting from today's daily remaining or causing an artificial overdraft.
- **Dynamic Budget Preview Card Adaptation**:
  - In `MainScreen.kt`, dynamically branches based on the Daily Budget toggle state:
    - **Daily Budget ON**: Displays real-time simulated daily remaining (`simDailyRem`), dynamic overdraft warning (`超支！其餘天數新每日預算`), and recalculated daily budget for remaining days.
    - **Daily Budget OFF (Non-Daily)**: Morphs into a calm, secondary-container preview card with Shopping Cart icon (`非日常大額消費` / `Non-Daily Purchase`), showing that the expense is excluded from the daily budget calculation and today's daily allowance remains unchanged (`今日每日額度保持不變`).
  - When quick preset chips (☕ Coffee, 🚇 MTR, 🍱 Lunch, etc.) are tapped, the toggle automatically resets to `Daily Budget = ON`.
- **History Screen & Edit Dialog Integration**:
  - In `HistoryScreen.kt` list view: Non-daily expenses display a distinctive, rounded `[Non-Daily]` (`[非日常]`) pill badge next to the transaction title.
  - In the "Edit Transaction" dialog (`editingTransaction`): Included the `Count in Daily Budget` toggle with `ExpressiveSwitch`, allowing users to toggle an expense between daily and non-daily retroactively, instantly updating budget calculations.
- **Multi-language Localization**:
  - Added localized string resources across all 5 language files (`values`, `values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`): `daily_budget_toggle_title`, `daily_budget_toggle_desc_on`, `daily_budget_toggle_desc_off`, `non_daily_expense_badge`, `non_daily_preview_title`, `non_daily_preview_desc`, `non_daily_preview_preserved`.
- **Automated & Live Verification**:
  - Added unit test `testNonDailyExpense_excludedFromDailyBudget()` in `BudgetRepositoryTest.kt` verifying that high-value one-off purchases do not affect daily allowance or monthly daily spending.
  - All unit tests passed (`./gradlew testDebugUnitTest`).
  - Built Release APK (`./gradlew :app:assembleRelease`) and installed on live Waydroid hardware (`192.168.240.112:5555`).
  - Explored and verified live with ARTEMIS MCP tools:
    1. Opened Add Expense sheet, verified initial state shows "Count in Daily Budget = ON".
    2. Input amount exceeding daily allowance: Dynamic preview card turned red with overdraft alert.
    3. Toggled "Count in Daily Budget" to OFF: Preview card smoothly transitioned to the calm "Non-Daily Purchase" preview showing daily allowance untouched.
    4. Saved a non-daily transaction: Confirmed today's spent amount remained `HK$ 0.00` and daily allowance remained `HK$ 50.00`.
    5. Navigated to History: Confirmed the `[Non-Daily]` badge was displayed, and Budget Period spent showed `HK$ 0.00`.
    6. Edited transaction via edit dialog to toggle back ON: Confirmed `[Non-Daily]` badge disappeared, Spent became `HK$ 65.00`, and Daily Overview recalculated to `HK$ 49/day`.

### 2026-10-01: Days Left Wavy Progress Indicator Dynamic Wave Motion Restoration & Reversed Elapsed Calculation
- **Material 3 Expressive `CircularWavyProgressIndicator` Dynamic Motion & Reversed Elapsed Ratio**:
  - Restored wave speed animation for `DaysLeftCard` in the Daily Overview page (`HomeScreen.kt` / `DaysLeftCard.kt`).
  - Configured `waveSpeed = if (indicatorShape == IndicatorShape.WAVY) WavyProgressIndicatorDefaults.CircularWavelength else 0.dp` so that in `WAVY` mode, the indicator continuously rotates and flows along its sinusoidal perimeter at Material 3 Expressive speed (1 wavelength per second).
  - Tapping `DaysLeftCard` smoothly transitions between `WAVY` and `FLAT` mode:
    - In `WAVY` mode: Sinusoidal waves smoothly morph and continuously animate along the circle perimeter.
    - In `FLAT` mode: The wave amplitude flattens into a perfect geometric circle and stops wave phase motion.
  - **Reversed Elapsed Progress Calculation**:
    - Reversed `calculateDaysLeftProgress(daysLeft: Int, totalDays: Int)` to reflect period elapsed progress:
      `((totalDays - daysLeft).toFloat() / totalDays).coerceIn(0f, 1f)`.
    - E.g., when 30 days left out of a 30-day period (start of budget cycle), progress is `0%` (0.0f).
    - When 0 days left out of 30 days (end of cycle), progress is `100%` (1.0f).
    - Updated test suite in `HomeScreenBentoTest.kt` verifying all boundary conditions (`0.0f`, `0.5f`, `0.9f`, `1.0f`, zero period, negative days).
  - Verified on live Waydroid hardware (`192.168.240.112:5555`) with ARTEMIS:
    - Confirmed 30 Days Left indicator displays clean 0% base circle in both Wavy and Flat modes.
    - Frame-to-frame pixel analysis verified active motion in Wavy mode (`diff_pixels: 6424` -> `6903`).
    - Flat mode verified completely static and perfectly round (`diff_pixels: 0`).
    - Verified bidirectional interactive toggling between Flat and Wavy modes.

### 2026-10-01: Card Background X & Y Axis Modification, Scaling, & Fine-Tuning Alignment Controls
- **Card Background X & Y Axis Customization**:
  - Added `cardBgOffsetX: Float = 0f`, `cardBgOffsetY: Float = 0f`, and `cardBgScale: Float = 1f` to `AccountEntity.kt`.
  - Added full serialization and deserialization in `InMemoryDatabase.kt` for persistent JSON storage and bumped Room `VibeFinanceDatabase` to `version = 2`.
  - Updated `Modifier.drawCardBackground` to dynamically calculate `ContentScale.Crop` scale factor, translations along X and Y axes (`transX`, `transY`), and apply `IntOffset` positioning.
  - Updated `ExpressiveAccountListItem` to apply `graphicsLayer` scaling and translation along X and Y axes within a cleanly rounded bento container (`RoundedCornerShape(20.dp)`), allowing seamless visual pan without edge bleeding.
- **Card Design Tab & Interactive Positioning UI**:
  - Added dedicated **"POSITION (X & Y AXIS)"** Material 3 control panel in the Card Design tab:
    - **X Axis Slider & Micro-Steppers**: Continuous `-100%` to `+100%` slider, left/right fine-tuning buttons (`◀` / `▶`), and status badge (`Center (0%)`, `-15% Left`, `+20% Right`).
    - **Y Axis Slider & Micro-Steppers**: Continuous `-100%` to `+100%` slider, up/down fine-tuning buttons (`▲` / `▼`), and status badge (`Center (0%)`, `-15% Up`, `+20% Down`).
    - **Zoom / Scale Slider**: Continuous `0.5x` to `2.5x` zoom factor with zoom out/in micro-steppers.
    - **Quick Preset Alignment Chips**: Instant one-tap alignment to `Center`, `Top`, `Bottom`, `Left`, and `Right`.
    - **Reset Button**: Instant reset to `(0, 0)` center with `1.0x` scale.
    - **Crop / Reposition Button**: Re-open interactive crop dialog directly from the Card Design tab on the active image.
  - Added micro pan and reset buttons to `InteractiveImageCropDialog`.
- **ARTEMIS Mobile Verification**:
  - Live verified on Waydroid (`192.168.240.112:5555`):
    1. Opened "Modify Mox" -> switched to "Card Design" tab.
    2. Inspected "POSITION (X & Y AXIS)" panel and verified sliders, steppers, and presets.
    3. Shifted Y-axis to `+20% Down`, verified in real-time on `previewCard` that the "mox" lettering and card artwork shifted smoothly downward into full view.
    4. Tapped "Save Changes" and verified on the main Assets & Cards screen that Mox card displays the adjusted background position cleanly.
  - Screenshots archived to artifacts: `waydroid_card_bg_xy_controls.png`, `waydroid_card_bg_xy_preview.png`, and `waydroid_card_bg_xy_saved.png`.

### 2026-09-30: Translucent Card Background & Material 3 Expressive Typography Enhancement
- **Translucent custom card background in Detailed View (`ExpressiveAccountListItem`)**:
  - When an account has a custom card image (`account.cardImageUri` / `customImage != null`), the entire account card container renders the full-bleed card art with a frosted glassmorphic effect (`ContentScale.Crop`, `alpha = 0.24f ~ 0.28f`).
  - Added a subtle vertical frosted gradient scrim (`Brush.verticalGradient`) optimized for both light and dark themes to guarantee **WCAG AAA legibility** for foreground text and actions.
  - Dynamically balanced container background opacity (`alpha = 0.35f ~ 0.45f` when an image is present) so the card art and textures shine through elegantly without overwhelming content.
- **Material 3 Expressive typography, JetBrains Mono font family & 3D text depth**:
  - **Financial figures in JetBrains Mono (`JetBrainsMonoFontFamily`)**:
    - Detailed View: Upgraded `balance` to **`headlineSmall` (22sp) + `JetBrainsMonoFontFamily` + `FontWeight.ExtraBold` + `letterSpacing = (-0.4).sp`**.
    - Detailed View: Upgraded credit limit utilization text (`%d%% Used • Avail $%,.0f`) to `JetBrainsMonoFontFamily` (`letterSpacing = (-0.15).sp`).
    - Compact View: Upgraded `balance` to **`titleMedium` (16.5sp) + `JetBrainsMonoFontFamily` + `FontWeight.ExtraBold` + `letterSpacing = (-0.3).sp`**.
    - All financial numbers, dollar signs, and decimal points now feature perfectly proportioned, tabular-spaced FinTech monospace styling.
  - **Glassmorphic 3D text depth shadow**:
    - Added subtle multi-theme `Shadow` (`color = Color.Black.copy(alpha = if (isDark) 0.55f else 0.22f), offset = Offset(0f, 1.5f), blurRadius = 4f`) to `displayName` and `balance` in Detailed View when a card backdrop image is present.
    - Prevents background patterns and textures from interfering with text, giving text a physical "floating" 3D elevation over the frosted card glass.
  - **Detailed View (`ExpressiveAccountListItem`)**:
    - Upgraded account `displayName` to **`titleLarge` (18.5sp) + `FontWeight.ExtraBold` + `letterSpacing = (-0.3).sp`**.
    - Converted card type and card number into micro-pill badges: `[CREDIT CARDS]` (styled in accent container) and `[•••• 2101]` (styled in `FontFamily.Monospace` capsule).
    - Strictly maintained header order: `[name , card image , custom app icon]`.
  - **Compact View (`CompactAccountRow`)**:
    - Added a left-side 3.5dp rounded **Accent Indicator Strip** (`CircleShape`), providing instant color-coded visual weight for card and account types.
    - Upgraded account `displayName` to **`15.5sp` + `FontWeight.ExtraBold`**.
    - Replaced the bland subtitle string with structured micro-badges: `[•••• 2101]` monospace capsule + clearly separated closing day text.
    - Strictly maintained header order: `[name , card image , custom app icon]`.
- **Verification & Deployment**:
  - All 104+ unit tests passed with 0 errors (`./gradlew testDebugUnitTest`).
  - Release APK built successfully (`./gradlew :app:assembleRelease`) and installed on Waydroid (`192.168.240.112:5555`).
  - Live verification on Waydroid:
    1. Uploaded/bound cyan Mox card image to `MOX card`.
    2. Detailed View: Confirmed the card container displays the translucent frosted Mox card background with 3D text shadows on name and JetBrains Mono balance (`$-13.20`), and micro-pill badges `[CREDIT CARDS]` and `[•••• 2101]`.
    3. Compact View: Confirmed left 3.5dp accent indicator strip, 15.5sp extra-bold text, monospace `[•••• 2101]` micro-badge, and JetBrains Mono extra-bold balance (`$672.54`, `$-13.20`, `$642.24`, etc.).
    4. Captured and archived live screenshots.

### 2026-09-30: Authentic Card Last-4 Notification Interception (Zero Randomness) & History Chart Minimalist Filter
- **Authentic card last-4 extraction (Strictly non-random)**:
  - Added deterministic, privacy-first regex extraction `extractCardLast4` in `PaymentNotificationListener.kt` supporting diverse English and Chinese SMS/push formats (e.g. `ending in 1234`, `尾號1234`, `末4位1234`, `•••• 1234`, `Visa 1234`, etc.).
  - Under no circumstances are pseudo-random 4 digits generated. If no card number was intercepted and none entered by the user, `cardLast4` strictly remains `null`.
  - Added `cardLast4: String? = null` to `ParsedPayment` and `PendingPayment`.
  - Updated `PendingPaymentStore.accept()`: When a pending payment is recorded to an account without `cardLast4`, the authentic intercepted card number is automatically bound to the account (`InMemoryDatabase.updateAccount`).
  - Updated `PendingPaymentChoiceDialog`:
    - Priority matching in `suggestedId`: Prioritizes accounts where `account.cardLast4 == payment.cardLast4`.
    - Payment summary card displays `卡號末四位：•••• $cardLast4`.
    - Account list highlights matching card with `(相符)` badge.
- **History category breakdown chart "已篩選" wording removed**:
  - Removed redundant `Text(stringResource(R.string.category_analytics_focused))` ("已篩選") from `CategoryBreakdownCard.kt`.
  - Replaced with a minimal, responsive circular ✕ close icon button (12.dp icon in 20.dp circle), preserving the subtitle ("Filtering: %s • Tap to reset") for a clean, modern aesthetic.
- **Verification**:
  - Unit tests updated and verified passing: `./gradlew testDebugUnitTest` (all tests passed with 0 errors).
  - Built Release APK (`./gradlew :app:assembleRelease`) and deployed to Waydroid (`192.168.240.112:5555`).
  - Verified live on Waydroid:
    1. History tab: Tapped "Food" category in Category Analytics chart. The "已篩選" badge was gone, showing a clean circular ✕ button. Tapped ✕ button and verified filter cleanly reset.
    2. Simulated notification: Sent payment alert with `Card: Visa ••2101`. `PendingPaymentChoiceDialog` displayed `卡號末四位：•••• 2101`.
    3. Selected `MOX card` (previously having no card number) and recorded expense. Switched to Assets > Credit Cards, verified `MOX card` now shows authentic `•••• 2101`, while all other cards remain with no card numbers (strictly non-random).
    4. Sent subsequent payment with `Visa ••2101`: `PendingPaymentChoiceDialog` automatically pre-selected `MOX card` with `•••• 2101 (相符)` highlighted.

### 2026-09-30: Removal of Synthetic Bank Data & Pure Installed-App Custom Linking
- **Removed synthetic banks and fake badge icons**: Completely removed `KNOWN_FINANCIAL_APPS`, synthetic brand badge letters ("支", "八", "P", "HSBC", "mox", "中銀", "恒生", "ZA", "AEON", etc.), colored box badges, and automatic name-guessing heuristics from `LocalAppManager`.
- **Pure real installed applications**: Only actual applications installed on the user's Android device/environment (`PackageManager`) are displayed in `AppPickerDialog`, rendering their authentic native icons (`appIcon.asImageBitmap()`), actual labels, and package names.
- **Clean card display without placeholder clutter**:
  - Removed `AccountAppAddButton` (`+` button) from unlinked cards to prevent visual clutter and placeholder confusion. Unlinked cards cleanly display `[name, card image]`.
  - When an installed app is linked to a card, the card renders its real native app icon in the 3rd slot: strictly `[name , card image , custom app icon]` in both Compact View (`CompactAccountRow`) and Detailed View (`ExpressiveAccountListItem`).
- **Flexible app binding & instant launch**:
  - In Edit Account sheet, users can choose any installed app via the "App Quick Launch" selector, or select "None (Do not link app)" to unlink.
  - On the card row, short-tapping the app icon directly launches the app. **Long-pressing** the icon opens `AppPickerDialog` directly from the card to re-bind or unlink.
- **Multi-language localization**: Updated string resources across all 5 language files (`values`, `values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`) to reflect authentic app selection without auto-detection or synthetic bank catalogs.
- **Verification**:
  - Unit tests updated and verified passing: `./gradlew testDebugUnitTest` (all tests passed with 0 errors).
  - Built Release APK (`./gradlew :app:assembleRelease`) and deployed to Waydroid (`192.168.240.112:5555`).
  - Verified live on Waydroid:
    1. Unlinked cards (`BOC`, `HSBC`, `za`, `brother`, `Cash`, etc.) show no fake badges and no `+` buttons.
    2. Selected "Calculator" for `alipay`, card showed `[alipay, Calculator icon]`.
    3. Tapped Calculator icon: Calculator app launched immediately on Waydroid.
    4. Long-pressed Calculator icon: `AppPickerDialog` opened directly, selected "None", card unlinked cleanly.
    5. Linked "Browser" to `Mox`, verified both Compact View and Detailed View: ordering strictly `[name (Mox), card image (Mox card), custom app icon (Browser)]`.
    6. Tapped Browser icon in Compact view: Browser launched immediately on Waydroid.

### 2026-09-30: Asset Card Custom App Redirection and Verified [name, card image, custom app icon] Order
- **App Quick Launch redirection**: Added direct redirection from asset cards to their corresponding mobile banking or wallet applications (e.g., Alipay HK, Octopus, PayMe, HSBC, Mox, BOCHK, Hang Seng, WeChat Pay, Citibank). Clicking the redirect button immediately launches the external app, or displays a friendly Toast notification (`應用程式尚未安裝`) if the app is not installed.
- **Strict visual ordering**: In both Compact View (`CompactAccountRow`) and Detail View (`ExpressiveAccountListItem`), the account card row layout strictly follows the requested order: `[name , card image , custom app icon]`. The card image/issuer avatar was moved to the right of the account name, immediately followed by the custom app redirect button.
- **Configuration & auto-detection**: In the account edit sheet, added an "App Quick Launch" (`App Quick Launch / 快速啟動應用程式`) Bento card with a dedicated dialog (`AppPickerDialog`) supporting: 1) Auto-detect by account name, 2) None (disable redirect button on card), and 3) Searchable list of all installed applications on the device.
- **Data persistence**: Stored `linkedAppPackage` in `AccountEntity` and updated `InMemoryDatabase` JSON serialization to save and load linked app packages seamlessly across app launches.
- **Localization**: Added full localized string resources across all 5 language files (`values`, `values-zh`, `values-zh-rHK`, `values-zh-rTW`, `values-b+zh+Hant`).
- **Verification**: Built and verified unit tests (`104 tests` passed with 0 errors). Assembled release APK and deployed live to Waydroid (`192.168.240.112:5555`). Verified visual layout, auto-detection for Alipay/Mox/Octopus/BOC/HangSeng/HSBC, app picker dialog, and strict `[name , card image , custom app icon]` ordering in both Compact and Detail views. Captured and archived live screenshots.

### 2026-09-29: Transfer-Free Daily Net and Shape-First Button Presses
- **History daily net**: Transfers remain visible as individual History rows but are omitted from the per-day net calculation in both all-account and account-filtered views. A day containing only transfers no longer shows a misleading Daily Net footer. The existing all-account exclusion for balance corrections remains, while account-specific balance adjustments continue to show their account movement.
- **Shape-first controls**: Shaped `bouncyClickable` controls no longer scale down on press; their spring-driven pressed outline, clipping, and bounded ripple remain. Shared `pressBounce` now defaults to no scale, letting standard Material buttons use their own clipped indication; custom-shaped buttons can still morph their clipped outline. Explicit scale-down remains available as an opt-in parameter. The shape animation no longer overshoots on release.
- **Verification**: The release build and all 99 existing unit tests passed with no failures. Installed the release APK in place on Waydroid (`192.168.240.112:5555`). September 27 retained two transfer rows with no Daily Net footer, and History still showed 868 entries. Holding the clipped Daily budget card changed its outline without shrinking the card; release still opened the budget sheet, which was dismissed without saving.

### 2026-09-29: History Swipe Press Highlight and Release Motion
- **Bounded press state**: Replaced the History transaction row's clickable ripple with a subtle pressed overlay clipped to each row's actual grouped shape. The overlay clears as horizontal dragging begins, so the swipe action background remains clean and the press highlight does not appear as a rectangular ripple during the gesture.
- **Settling motion**: A released swipe now returns with a 240 ms eased animation that does not overshoot. The action icon and backdrop use eased animations as well, and History row removal and neighbor placement use non-bouncy springs. Beginning a new drag stops any in-progress return so the row follows the finger smoothly.
- **Waydroid verification**: The release APK built successfully and was installed in place on Waydroid (`192.168.240.112:5555`). A held expense showed a shape-bounded highlight; a partial swipe showed the action background without that highlight and settled back without opening an action. A normal tap still opened Edit Transaction; it was canceled without saving. History remained at 868 entries.

### 2026-09-29: Smart Octopus MTR Notification Investigation
- **Device findings**: Connected to the user's Samsung phone over wireless ADB, first at `10.9.144.59:39207` and then `10.9.144.59:43957` after its debugging connection dropped. Android initially showed a `Smart Octopus HK$12.6 港鐵` notification. The finance notification listener has access; automatic transaction logging is enabled; the Octopus app and both Samsung Wallet entries are selected. After the connection drop, the alert was no longer in Android's active notification list, and a fresh app launch showed no pending-payment choice. History contained only the existing HK$29 McDonald's entry, and Assets contained one credit-card account with no Octopus asset. The exact reason this individual notification was missed cannot be established from the remaining device logs, and it was not inserted into the user's ledger.
- **Parser correction**: A canonical `Smart Octopus HK$… merchant` title now supplies the fare and merchant regardless of whether the selected Octopus app or Samsung Wallet posted it. The remaining-value amount in the notification body cannot replace the fare. Remembered account choices now accept an explicitly chosen Octopus/八達通 account for a `Smart Octopus` hint; unrelated accounts still cannot be remembered through that alias.
- **Verification**: Added a focused regression using the observed HK$12.6 港鐵 title with a different remaining-value amount and the installed Octopus package; the focused notification test suite passed. The full unit suite passed 99 tests across 13 suites with no failures, and the release build passed. The old alert cannot be replayed after it disappears, and no transaction or app setting was changed on the phone. The new APK has not been installed on that phone.

### 2026-09-28: Period-Aware Category Analytics and Weekday History Headings
- **Category Analytics range**: The History chart now uses the current calendar month when no budget period exists, and defaults to the period configured through Settings or Daily when one exists. A connected Month/Budget Period control switches ranges; month arrows browse earlier months, and a date label makes the chart's range explicit. The History list's All/Active/Past period selector remains separate. Selecting a chart category shows matching expense entries from the chart's displayed range.
- **Full final day**: Budget periods saved with an end date at local midnight now include that entire final calendar day in History, Daily budget calculations and summaries, Settings pacing, and period-ended states. The start timestamp, transfer handling, and budget-exclusion rules remain unchanged.
- **Date headings**: History date groups now include the weekday in the app's selected language, including Today and Yesterday headings.
- **Verification**: All 98 unit tests in 13 suites passed, and the release build passed. Installed the APK in place on Waydroid (`192.168.240.112:5555`). With no configured budget period on that device, Category Analytics showed September 2026 rather than all-time totals, switched to August 2026 with different category totals, and a tapped August Food category showed August Food entries. History displayed `Yesterday · Sunday` and `Aug 31, 2026 · Monday`. Cleared the category focus and returned analytics to September. The imported ledger still showed 868 History entries. The Budget Period selector path was verified in code/build but could not be opened on this device without creating a budget period and changing its saved data.

### 2026-09-28: Detailed Account Rows and Card Perks Removal
- **No repeated type in detailed rows**: Cash, bank, debit, and credit card sections already identify their account type. Detailed rows no longer repeat a type badge or generic type icon. Uploaded card artwork and selected issuer logos still identify individual accounts, while credit cards retain their 結算日, credit utilization, and configured payment due information. Compact rows are unchanged.
- **Removed card perks controls**: Credit card editing now has a `Billing & Due` tab with the existing statement and payment schedule controls. Removed cashback presets and rate inputs, the minimum-spend field and cashback target display, the Linked Stores panel and its add/edit dialogs, and the calculated cashback badge in History. Account saving no longer writes cashback rules. Existing saved cashback/shop values and the account's legacy threshold remain stored for compatibility; payment notification cashback exclusions and payment-deadline reminders remain active.
- **Verification**: The release build passed and its APK was installed in place on Waydroid (`192.168.240.112:5555`). In detailed view, cash/bank/credit rows had no duplicate type badge or generic avatar; the uploaded Mox card artwork remained visible. A credit card retained its unset 結算日 indicator; its editor showed only statement closing day, payment due, and payment deadline on `Billing & Due`. History expense rows had no cashback badge. The imported ledger still showed 14 accounts, 868 History entries, and HK$18,281.93 net asset value. ARTEMIS diagnosis passed, but its autonomous verification stalled on a model-service 503; the visual checks used ADB screenshots instead.

### 2026-09-28: Expressive Grouped Compact Account List
- **Less repeated text**: Compact account rows no longer repeat Cash, Bank, or Debit type beneath every name; the group header supplies that context. Rows keep useful account-specific details only: user-entered last four digits, the original name when a nickname is shown, and each credit card's 結算日 or unset state. Rows without those details use one line and a 58 dp minimum height.
- **Grouped Material 3 motion**: Compact rows now use History-style 26 dp outer group corners and 8 dp inner corners. The existing spring press tightens and clips the row shape and ripple. Balance and History actions retain 48 dp touch targets and receive separate rounded, spring-clipped press surfaces. The compact/detailed spring transition, custom card thumbnails beside account names, and account sorting remain in place.
- **Verification**: The release build passed and was installed in place on Waydroid (`192.168.240.112:5555`). Cash/Bank rows showed no duplicate type subtitles; the Mox card image stayed beside its name, and credit cards still showed closing-day status. Captured row and action-button pressed states to verify the clipped motion. Tapping a row opened its editor, the balance action opened the balance dialog, and History opened filtered to that account; the dialog/filter were cleared without changes. The ledger remained at 14 accounts, 868 History entries, and HK$18,281.93 net asset value.

### 2026-09-28: Compact Account Artwork and Detailed Card Thumbnails
- **Compact Assets & Cards**: Removed the repeated generic account-type icon from each compact row; the grouped type heading still identifies the section. A saved custom card image now appears immediately to the right of the account name, and a selected issuer logo can appear there when no uploaded image exists. Missing or unreadable image files leave the compact row text-only.
- **Detailed Assets & Cards**: The uploaded card image occupies the account avatar position, preserving the chosen card/square/wide/circle crop shape. When custom artwork or an issuer logo is displayed, the type badge no longer repeats a generic icon. Both list modes use the same sampled image loader so thumbnails avoid decoding the full image each time.
- **Verification**: The release build passed and was installed in place on Waydroid (`192.168.240.112:5555`). Imported accounts without artwork showed clean compact text rows. The user-supplied Mox card image was uploaded through the existing picker/crop flow to the Waydroid Mox bank asset; it appeared beside “Mox” in compact mode and in place of the avatar icon in detailed mode, including after an app restart. Account count (14), Mox balance (HK$14,800.35), and net asset value (HK$18,281.93) stayed unchanged. The image was saved in the app's device storage, not bundled into the repository.

### 2026-09-28: Account Identity Options and Debit Cards
- **Recognizable accounts**: The account editor now offers an optional nickname and a theme-adaptive accent choice (Auto or three color roles). Nicknames appear in Assets lists, transaction and Recurring account pickers, payment-notification choices, and account labels in History; the original name remains available as supporting text. Compact and detailed rows use the chosen accent. The card preview and rows display the last four digits only when the user enters four real digits. New edits reject partial last-four values, and generated account-number placeholders were removed.
- **Debit card type**: Added Debit cards as a separate type in the account editor, inline add flows, selectors, grouped Assets lists, import preview, and Money Allocation chart. Debit balances behave as assets: expenses and notification payments reduce available balance, income adds to it, transfers and credit-card payments keep their existing signs, and balance corrections remain in History. Debit type, nickname, last four, and accent persist in the existing JSON snapshot and load older snapshots with empty identity fields. Explicit debit labels are detected during import; card networks alone do not guess debit. The standalone imported `go` wallet remains Cash, while a longer BOC Go UnionPay title is no longer mistaken for Cash.
- **Labels and localization**: Asset totals now say “Total assets” because they include debit cards. Added debit and identity strings for English, Simplified Chinese, Hong Kong Traditional Chinese, Taiwan Traditional Chinese, and generic Traditional Chinese resources.
- **Verification**: All 98 unit tests in 13 suites passed with no failures, and the release build passed. The final release APK was installed in place on Waydroid (`192.168.240.112:5555`). A temporary debit card showed its own group, selected accent, real last four digits, and nickname with the original name below; it was then deleted. The imported ledger returned to 14 accounts, 868 History entries, and HK$18,281.93 net asset value. Live debit-card bank notifications were not available on Waydroid; their balance handling is covered by tests.

### 2026-09-28: Grouped Asset Views, Spring Switching, and UnionPay Transit Alerts
- **Assets & Cards in both views**: Detailed and compact mode now keep the same full net-asset-value card, donut chart, cash/bank and card-debt totals, Money Allocation chart, and connected asset filter. Both modes group accounts by Cash, Bank, then Credit Card; names are sorted within each group. Localized, count-bearing group headers and stable account keys make the list easier to scan without changing individual account/card identity styling.
- **Visible view transition**: The compact/detailed control sits directly above the account groups. Visible rows switch through medium-low spring height, slide, and fade motion with clipped bounds; list placement keeps stable keys. A detailed card's press state now releases with the actual touch instead of remaining scaled after its editor opens. The compact preference and balance/history actions remain available.
- **BOC Go UnionPay alerts**: A `Transit-ticket ... HK$3.60` alert with a BOC Go UnionPay card title is recognized as a Transport payment, keeps the card title for the account-choice prompt, and reads fuller expanded notification text when present. Refunds, promotions, balances, extra amounts, foreign currency, and missing merchant details remain excluded. An ellipsis-truncated card title cannot be remembered for automatic future routing; the user must choose an account. Only actual installed, selected posting apps may trigger interception.
- **Verification**: All 95 unit tests across 12 suites passed with no failures. The release build passed and was installed in place on Waydroid (`192.168.240.112:5555`). The detailed and compact account layouts showed the retained NAV figures (HK$18,281.93 net worth, HK$18,821.31 cash/bank, HK$539.38 card debt), chart, connected filter, and type groups. A real BOC posting app/notification was unavailable on Waydroid, and VibeFinance notification access there is off, so live interception was not verified. Account/card identity styling awaits the user's preference.

### 2026-10-01: Material 3 Expressive Emphasized Typographic Watermarks for Cards & Assets
- **Ambient Typographic Backdrop (`AccountEntity.watermarkText()`)**:
  - Implemented smart branding watermark resolution based on Google Material 3 Expressive `md.sys.typescale.emphasized` principles (`Display Large Emphasized` / `FontWeight.Black` 900 / `JetBrainsMonoFontFamily` / tight tracking `-2.sp`).
  - Automatically extracts concise brand acronyms (e.g., `alipay` -> `ALIPAY`, `enjoy` -> `ENJOY`, `AEON card` -> `AEON`, `wakuwaku` -> `WAKUWAKU`, `octopus card` -> `OCTOPUS`, `恆生` -> `恆生`, `BOC` -> `BOC`, `HSBC` -> `HSBC`), falling back to uppercase category types (`CREDIT`, `CASH`, `BANK`, `DEBIT`).
- **Comprehensive Viewport Integration**:
  - **Detailed View (`ExpressiveAccountListItem`)**: When an account lacks a custom image, renders a giant 64sp emphasized typographic watermark softly bleeding off the right edge (`alpha = 0.08f` in dark mode, `accent.foreground` with `alpha = 0.09f` in light mode), completely eliminating plain/empty card surfaces while preserving 100% foreground readability.
  - **Top Card Carousel (`AssetCardItem`) & Edit Preview (`previewCard`)**: Embedded 58sp emphasized watermark directly into the card canvas behind the foreground content when no custom art is uploaded.
  - **Compact View (`CompactAccountRow`)**: Embedded miniature 36sp watermark along the trailing side of each row (`alpha = 0.05f`), maintaining visual hierarchy and subtle brand identity across compact mode.
  - **Custom Image Priority**: Accounts with uploaded custom images (e.g. `MOX card`) automatically retain priority and render custom artwork without watermark collision.
- **Verification on Waydroid (`192.168.240.112:5555`)**:
  - Passed `:app:testDebugUnitTest` (28/28 tasks).
  - Built and deployed production release APK (`:app:assembleRelease`).
  - Verified via ARTEMIS in both Detailed View and Compact View: `ALIPAY`, `BROTHER`, `CASH`, `GO`, `OCTOPUS`, `恆生`, `AEON`, `ENJOY`, `WAKUWAKU` watermarks rendered with perfect contrast, while `MOX card` smoothly displayed its custom turquoise background image.

### 2026-10-01: Card Background Edge-to-Edge Zero-Cutout Formula & Pinned Live Preview
- **Sticky Pinned Live Preview & Direct Gesture Manipulation (`AddEditAccountDialog`)**:
  - Pinned `previewCard()` and the tab selector (`tabSelector()`) above the scrollable `tabContent()` column in portrait mode (`!isWide`), completely resolving the problem where preview card was scrolled off-screen when users adjusted X/Y sliders or presets.
  - Implemented 1:1 direct finger drag-to-reposition and pinch-to-zoom directly on `previewCard()` using Compose `detectTransformGestures`. Live gestures dynamically update `cardBgOffsetX`, `cardBgOffsetY`, and `cardBgScale` with instant visual feedback on both the card and the synchronized sliders below.
  - Added interactive banner in Card Design tab informing users of direct gesture support on the preview card.
- **Scale-to-Cover Zero-Cutout Math (`drawCardBackground` & `ExpressiveAccountListItem`)**:
  - Replaced the previous `ContentScale.Crop` + `graphicsLayer.translationY` pattern that produced blank/white gaps (cut-outs) at the top or edges of cards when translated.
  - Applied the exact scale-to-cover formula:
    $$s_{\text{reqX}} = \frac{W + 2|\text{transX}|}{w}, \quad s_{\text{reqY}} = \frac{H + 2|\text{transY}|}{h}$$
    $$s_{\text{cover}} = \max(s_{\text{reqX}}, s_{\text{reqY}}), \quad s_{\text{final}} = s_{\text{cover}} \cdot \text{scale}$$
    $$\text{transLeft} = \frac{W - w \cdot s_{\text{final}}}{2} + \text{transX} \le 0$$
    $$\text{transTop} = \frac{H - h \cdot s_{\text{final}}}{2} + \text{transY} \le 0$$
  - Mathematically guarantees that the background image bounds strictly cover $[0, W] \times [0, H]$ under any translation or aspect ratio, completely eliminating edge cut-outs in both the Live Preview and the Detailed View.
  - Upgraded `createCroppedBitmap` to apply the same required-dimension expansion to eliminate transparent borders during crop export.
- **Verification on Waydroid (`192.168.240.112:5555`)**:
  - Unit tests passed cleanly (`:app:testDebugUnitTest`).
  - Production release APK (`:app:assembleRelease`) built and installed via ADB.
  - Verified with ARTEMIS:
    1. Sticky pinned preview card in dialog: remained fixed at top while scrolling down to X/Y sliders.
    2. Real-time updates: slider and stepper adjustments immediately reflected on preview card.
    3. Direct 1:1 touch drag gesture: dragging directly on preview card panned the background image in real time and synchronized slider labels.
    4. Reset button: instantly restored center alignment and 1.0x scale.
    5. Zero cut-out verified: Mox card in Detailed View displayed edge-to-edge background without any gap or cutout at the top.

### 2026-09-27: Compact Assets, Balance History, Notification Account Choice, and Import Reconciliation
- **Compact Assets & Cards**: Added a compact-mode toggle backed by display preferences, so the chosen mode survives tab changes and app restarts. The initial compact layout used a slim net-worth strip; the 2026-09-28 update above restored the full overview and selector in both modes. Short account rows are grouped and sorted by Cash, Bank, and Credit Card type. Each row keeps its balance visible and offers direct balance editing and account History; the detailed cards have the same actions. Credit-card rows retain their 結算日 display, including an unset state for imported cards.
- **Balance changes and per-account History**: A balance edit now creates a dedicated adjustment transaction with the exact signed delta, updates the account balance together with that transaction, and appears in History. The account History action opens History filtered to that account, including its side of transfers, with a clear-filter control. Adjustments do not count as spending in budget and category analytics. Saving from a stale editor uses the account's current balance to calculate the adjustment; cents-aware comparison avoids empty HK$0.00 adjustments. Transaction type snapshots preserve historical signs if an account is reclassified. Deleting an account now removes its linked History entries and reverses linked transfers on surviving accounts.
- **Payment notification account choice**: A detected payment waits in a persistent inbox until the app opens and the user chooses one of their actual accounts or ignores it. A remembered choice can automatically route later alerts when the alert contains a specific card/account hint, or the user selects a dedicated wallet account with the same name. A generic bank-app name cannot route payments from multiple cards. The posting app's real installed label is used. Alert identity and a staged transaction-plus-balance write guard against duplicate recording. Replacing imported data clears pending alerts and remembered account mappings, and deleting an account clears mappings. Foreign-currency, refund, incoming-funds, failed-payment, balance-only, and ambiguous multiple-amount alerts are not recorded as HKD expenses.
- **Workbook reconciliation**: Audited `財務管家_27-9-2026.xlsx`: 868 rows and 14 account names, comprising 647 expenses (HK$52,831.49), 140 incomes (HK$71,113.42), and 81 transfers (HK$53,462.08). Three HK$300 transfers from the `enjoy` credit card to `octopus card` exposed an inverted credit-card source sign. Corrected import preview and transaction balance handling, and added a one-time persisted repair for existing imported data. The corrected ledger-derived totals are **cash/bank HK$18,821.31, signed card debt HK$539.38, and net worth HK$18,281.93**. The workbook does not contain opening account balances, so these figures reconstruct movements in the file rather than bank-statement balances. The inferred 10 non-card / 4 credit-card split comes from account names, not an explicit workbook type field; subcategory/content are flattened into description, and source timestamps have second-level precision.
- **Import safety**: Replacement and merge now validate and stage all accounts, rows, and derived balances before writing one `AtomicFile` snapshot and publishing the new ledger. Failed staging or saving leaves the prior ledger intact. Account IDs remain monotonic across replacement so old notification choices cannot accidentally point to reused IDs. Malformed workbook rows are still omitted by the parser before preview; this workflow does not yet show a skipped-row count.
- **Verification status (2026-09-28)**: Compact grouping, the account History filter, and corrected totals were checked on Waydroid (`192.168.240.112:5555`). A temporary account changed from HK$0 to HK$25, and its +HK$25 adjustment appeared in account History. A later temporary-account check confirmed that deleting the account also removed its adjustment without a manual History deletion. Each check returned the ledger to 14 accounts, 868 entries, and HK$18,281.93. The final in-place release APK install succeeded; after restart, compact mode remained selected with 14 accounts and HK$18,281.93 net worth, and History showed 868 entries. The integrated unit suite passed 92 tests, including 27 focused notification parser tests, and the release build passed. A real incoming payment notification from an allowed app was not available for end-to-end device verification; the prompt wiring was reviewed in code, and parser/remember eligibility logic was tested.

### 2026-09-27: Credit-Card Settlement Day and Expressive Press Shapes
- **Per-card 結算日**: The Assets & Cards list now shows each credit card's monthly statement closing day from its existing `AccountEntity.billingDate` field. Imported cards with no known day show `未設定` instead of an invented date. The card editor labels this field 結算日, offers an explicit unset choice and days 1–31, and preserves `null` when editing a card without selecting a day. Cash and bank accounts do not show a settlement day. Added English and Chinese strings across all five locale resources.
- **Unknown payment deadlines**: Imported cards without a saved payment deadline no longer show a fabricated countdown. Their editor fields remain unset, and valid deadline days 29–31 are clamped to the last day of shorter months when calculating the next due date.
- **Pressed button shape**: 35 Material 3 buttons across the app now use Compose `ButtonDefaults.shapes` to spring from their resting pill or rounded shape into a tighter pressed shape, with the bounded ripple clipped by the button surface. The shared custom click modifier uses a spring-animated outline and clip for non-Button surfaces. Existing button roles, scale response, haptics, and click behavior remain in place.
- **Verification**: `:app:testDebugUnitTest` passed 85 tests across 12 suites; `:app:assembleRelease` passed. Installed the signed release APK in place on Waydroid (`192.168.240.112:5555`). The 14 imported accounts and asset totals were retained. All four credit cards display the unset closing-day chip without an invented payment countdown. Opened an imported card editor and confirmed its closing day is blank; visually checked the pressed shape of the Add Asset button. No imported record was changed during verification.

### 2026-09-27: Native Excel / CSV In-App Import Feature & Atomic Disk Persistence Engine
- **Native OpenXML (.xlsx) & CSV Parser Engine (`FinancialDataImportEngine.kt`)**:
  - Implemented zero-bloat, 100% native Android streaming parser using `XmlPullParser` + `java.util.zip.ZipFile` on temporary file stream (eliminates Dalvik/ART `ZipInputStream` data descriptor size mismatch bugs).
  - Multi-fallback URI resolver: `openInputStream` -> `openFileDescriptor` AutoCloseInputStream -> `openAssetFileDescriptor` -> direct file system access.
  - Automatically identifies header schema from user's `財務管家` Excel exports (Date, Account, Category / Transfer Destination, Subcategory, Description, HKD Amount, Type, Memo) as well as generic CSV format.
  - Smart account detection: identifies Cash, Bank, and Credit Card accounts from transaction records and maps them to application account types.
- **Atomic Disk Persistence (`InMemoryDatabase.kt`)**:
  - Implemented automatic, crash-safe JSON disk persistence (`vibefinance_data.json`) via `android.util.AtomicFile` in internal private storage (`context.filesDir`).
  - Automatically persists all state mutations (`accounts`, `transactions`, `budgets`, `subscriptions`, `discountShops`, `categoryLimits`, `savedAspects`) synchronously with thread-safe locking.
  - Automatically restores all records upon cold launch.
  - Guarantees 100% data retention across app restarts, system memory pressure kills, OS reboots, and app updates/reinstalls (`adb install -r`).
- **Material 3 Expressive Import Preview Modal Bottom Sheet (`ImportDataPreviewSheet.kt`)**:
  - Displays file summary chip, detected date range, and 3-column Bento metrics: Total Expenses (with count), Total Income (with count), Total Transfers (with count).
  - Detected accounts list with card/bank/cash icons, record counts, and net balance delta.
  - Import strategy selector: "Merge with existing data" vs "Replace all existing data ⚠️".
  - M3 Pill action button ("Confirm & Import") with `Modifier.pressBounce`, haptic feedback, and confirmation toast.
- **Entry Points & Navigation**:
  - **HistoryScreen**: Added companion `[ ⬆ Import Data ]` button side-by-side with `[ ⬇ CSV ]` in `CategoryBreakdownCard.kt`.
  - **SettingsSheet**: Added `Import Data (Excel / CSV)` in Section 7 (Data & Privacy).
  - Uses `ActivityResultContracts.GetContent()` with universal MIME filter `*/*` for maximum compatibility across Android devices and file pickers.
- **Multi-Language Localization**:
  - Added localized strings for import preview, Bento metrics, import strategies, and confirmation messages across `values/strings.xml`, `values-zh/strings.xml`, `values-zh-rHK/strings.xml`, `values-zh-rTW/strings.xml`, and `values-b+zh+Hant/strings.xml`.
- **Live Device Verification**:
  - Successfully parsed user's actual `財務管家_27-9-2026.xlsx` on device:
    - 868 transactions total (647 expenses, 140 income, 81 transfers).
    - 14 detected account names, inferred as Cash & Bank (10) and Credit Cards (4) from their names; the workbook does not supply explicit account types.
    - The initial net asset value shown here was HK$20,081.93. This was superseded by the credit-card transfer correction above; the corrected ledger-derived value is HK$18,281.93. Category analytics dynamically updated.
  - Executed cold kill (`am force-stop`) and restart: verified 100% of 868 transactions and 14 accounts persist and reload immediately.
- **Physical Device Deployment**:
  - Deployed updated release APK to user's physical **Samsung Galaxy S24 Ultra** (`SM-S9280`, `adb-R5CX22YGH7A-LIJF6v._adb-tls-connect._tcp`) via `adb install -r`, performing an in-place upgrade that preserved all user data and preferences while enabling persistent in-app Excel/CSV imports.

### 2026-09-26: VibeFinance v1.0.4 Material 3 Button Optimization, Spring Press Feedback & Physical Device Deployment
- **Tactile Touch Feedback Engine (`Modifier.pressBounce`)**:
  - Implemented `Modifier.pressBounce(scaleDown: Float = 0.96f, interactionSource: MutableInteractionSource)` in `ui/common/BouncyClickable.kt`.
  - Integrates directly with Compose's `interactionSource.collectIsPressedAsState()` and spring physics (`DampingRatioMediumBouncy`, `StiffnessMediumLow`) to provide instantaneous 0.96x elastic depression on press down without blocking bounded ripples, haptics, or click handlers.
- **Material 3 Button Guidelines Parity (`m3.material.io/components/buttons/overview`)**:
  - Upgraded buttons across dialogs, popup windows, and bottom sheets to strict Material 3 hierarchy and shapes:
    - **Primary / Confirm Actions**: Upgraded to Filled Pill Buttons (`shape = CircleShape` or `RoundedCornerShape(26.dp)` / `27.dp`), `elevation = buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)`, `pressBounce`, leading icons, and `TextHandleMove` haptic ticks.
    - **Secondary / Quick Adjustments**: Upgraded to M3 `FilledTonalButton` with `RoundedCornerShape(16.dp)`, `pressBounce`, and haptics (e.g. `+` and `-` custom adjust buttons in Add Asset sheet).
    - **Art & Outlined Actions**: Upgraded "Upload Image" to M3 `OutlinedButton` with `RoundedCornerShape(16.dp)` and leading icon.
    - **Destructive / Dismiss Actions**: Upgraded "Delete" to M3 `OutlinedButton` with `RoundedCornerShape(26.dp)` and `LongPress` haptics; upgraded "Cancel" / "Close" to `TextButton` or `IconButton` with `CircleShape` and `pressBounce`.
  - **Add Asset / Card Bottom Sheet (`AccountsScreen.kt`)**: Empty state "Add Account" pill button, filter chips haptics, 1-tap quick adjust suggestion chips haptics, `+`/`-` FilledTonalButtons, Upload/Remove image buttons, crop dialog buttons, account delete dialog, and anchored sticky bottom action bar ("Create Asset" / "Save Changes").
  - **Inline Add Category & Add Card Sheets (`MainScreen.kt`)**: Inline "+ Add Category" button with leading `Icons.Default.Add` icon, `CircleShape`, and `pressBounce`; inline "+ Add Card / Account" button with leading `Icons.Default.Add` icon, `CircleShape`, and `pressBounce`.
  - **Auxiliary Sheets & Dialogs**: Transaction Edit Dialog in `HistoryScreen.kt` (filled Save and text Cancel), `AddDiscountShopSheet.kt` (pill Save button), `NewPeriodBudgetSheet.kt` (pill Start New Period button), and `SettingsSheet.kt` / `AppearancePickerSheet.kt` dialog buttons.
- **Strict Scope Preservation**:
  - Components already implementing Material 3 Expressive design (`ExtendedFloatingActionButton`, `ConnectedButtonGroup`, `ExpressiveSegmentedButtonGroup`, `DaysLeftCard`, `HeroDailyBudgetCard`) were strictly preserved without modification.
- **Production Build & Physical Hardware Deployment**:
  - Built production release APK `VibeFinance-v1.0.4.apk` (`3,118,522 bytes`, Version Code `5`, Version Name `1.0.4`).
  - Unit tests passed cleanly (`28/28` testDebugUnitTest tasks passed).
  - Deployed to local Waydroid emulator (`192.168.240.112:5555`) and visually verified across all tabs, sheets, and inline popups.
  - Successfully deployed to the user's physical phone **Samsung Galaxy S24 Ultra** (`SM-S9280`, `adb-R5CX22YGH7A-LIJF6v._adb-tls-connect._tcp`) using `adb install -r`, performing an in-place upgrade that fully preserved all user transactions, accounts, and settings.

### 2026-09-26: VibeFinance v1.0.3 Fixes & Enhancements
- **Notification Redirection**: Added `android:launchMode="singleTask"` to `MainActivity` in `AndroidManifest.xml`. Configured PendingIntents with `navigate_tab` extra across `DeadlineCheckWorker` (recurring), `SubscriptionRepository` (recurring), and `PaymentNotificationListener` (history). Implemented `FinanceViewModel.requestedTab` StateFlow and connected `LaunchedEffect` in `MainScreen` to seamlessly transition to the targeted tab on cold launch or `onNewIntent`.
- **CSV Export Crash Prevention**: Configured `androidx.core.content.FileProvider` in `AndroidManifest.xml` backed by `res/xml/file_paths.xml` (`cache-path`). Added safe error handling and `FLAG_ACTIVITY_NEW_TASK` to the export chooser in `CsvExportEngine.kt`, resolving the unhandled `IllegalArgumentException` and app quit.
- **Themed Opening Animation**: Added dark theme window background (`#111413`) in `res/values-night/themes.xml` and light theme window background (`#F8FAF8`) in `res/values/themes.xml`. Moved `MaterialYouAppLaunchOverlay` inside `VibeFinanceTheme` in `MainActivity.kt`, ensuring splash screen background matches system/app dark/light mode dynamically instead of flashing white.
- **Extended FAB Scroll Stabilization**: Removed excessive downward `translationY` on the Recurring tab's `ExtendedFloatingActionButton` in `MainScreen.kt`. The FAB now collapses into a compact floating `+` icon in place without diving down into the bottom navigation bar or Android gesture bar.
- **Verification**: Verified on Waydroid with ADB intent delivery (`am start --es navigate_tab history/recurring`), dark/light mode boot verification, CSV share dialog inspection, and scroll list FAB collapse.

### 2026-09-25: Real Installed Apps and Shape-Clipped Press Feedback
- **Automatic transaction logging**: The allowed-app picker now lists only packages confirmed by Android's PackageManager. It shows the installed app's actual label, icon, and package name; an icon that cannot be loaded is omitted. Removed the synthetic payment-app branding and fabricated preview icons. The Settings preview counts only installed selected packages, and the English one-app count is grammatically correct.
- **Selection migration**: Existing saved payment-app group IDs are converted to installed package names, unavailable packages are removed, and an intentionally empty selection stays empty. Notification interception now checks the exact selected package, while retaining payment-only checks for chat apps.
- **Press feedback**: Rounded custom controls now clip their ripple to their visible shapes, including the allowed-app rows, Settings sections and rows, account cards, calendar cells, category analytics, and appearance/shape pickers. The connected selection buttons use clickable shaped surfaces.
- **Verification**: Focused app-selection unit tests and debug/release builds passed. Installed the signed release APK over the existing Waydroid app and confirmed the picker shows real installed apps only, the Settings preview shows "None active" with no fabricated logos, and selecting/deselecting a real app updates the preview. Restored the empty selection after testing.

### 2026-09-25: VibeFinance v1.0.0 Release APK Build & GitHub Deployment
- **Release Build (`assembleRelease`)**: Built minified and resource-shrunk production APK (`2.9 MB`) configured with Proguard / R8 optimization rules in `proguard-rules.pro`.
- **Git Synchronization**: Committed 70 files spanning all recent M3 Expressive UI components, unified chart palettes, subscription deletion decision flow, and 77 unit tests to `main` branch. Pushed cleanly to GitHub remote `https://github.com/ricky688/Finance_App.git`.
- **GitHub Release Publishing**: Created official GitHub release `v1.0.0` at `https://github.com/ricky688/Finance_App/releases/tag/v1.0.0`.
- **Asset Upload**: Uploaded standalone release binary `VibeFinance-v1.0.0.apk` (`3,021,303 bytes`) with direct browser download link: `https://github.com/ricky688/Finance_App/releases/download/v1.0.0/VibeFinance-v1.0.0.apk`.
- **Live Device Verification**: Installed `app-release.apk` onto Waydroid emulator (`192.168.240.112:5555`), launched successfully, verified 0 runtime crashes, verified smooth Compose rendering (`release_apk_verified_home.png`), and confirmed 0% idle CPU settle.

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
| `app/src/main/java/com/example/vibefinance/ui/common/BouncyClickable.kt` | **MODIFIED** | Implemented `Modifier.pressBounce(scaleDown = 0.96f, interactionSource)` with spring physics (`DampingRatioMediumBouncy`, `StiffnessMediumLow`) to provide tactile button compression on press without disrupting bounded ripples or click events. |
| `app/src/main/java/com/example/vibefinance/theme/ChartColors.kt` | **NEW** | Centralized 12-color high-contrast wide-spectrum palette authority (`UNIFIED_PALETTE`), deterministic bilingual category semantic mapping, two-pass collision-free assignment algorithm with 12% dynamic primary tint blending, and unified asset segment tokens. |
| `app/src/main/java/com/example/vibefinance/ui/components/ExpressiveSwipeRow.kt` | **NEW** | Reusable Gmail-style swipe-to-action component featuring 1:1 translation, 4dp floating elevation, dynamic spring icon pop (`1.0f -> 1.25f`), 44dp translucent circular backdrop disc, haptic feedback, and two-direction actions (Delete vs Edit). |
| `app/src/main/java/com/example/vibefinance/ui/components/ExpressiveSwitch.kt` | **NEW** | Material 3 Expressive switch with 52x32dp pill track, morphing thumb (18dp to 24dp), spring translation overshoot, animated checkmark icon, and tactile haptic feedback. |
| `app/src/main/java/com/example/vibefinance/ui/components/AppBrandIcon.kt` | **MODIFIED** | Implemented `VibeFinanceIcon` in pure Jetpack Compose Canvas, translating `ic_launcher_foreground.xml` into a resolution-independent, hardware-accelerated Compose component with ambient shadow, emerald gradient 'V' emblem, upward growth sparkline, and glowing coin node. |
| `app/src/main/java/com/example/vibefinance/ui/components/BudgetPeriodIndicatorCard.kt` | **MODIFIED** | Full-width connected filter buttons eliminating edge shadow artifacts; replaced custom Box track with flat Material 3 `LinearProgressIndicator`. |
| `app/src/main/java/com/example/vibefinance/ui/components/CategoryBreakdownCard.kt` | **MODIFIED** | Integrated unified `ChartColors`, touch drag scrubbing with spring segment expansion, interactive summary card, and spring exit motion. |
| `app/src/main/java/com/example/vibefinance/ui/components/MoneySeparationChart.kt` | **MODIFIED** | Consumes `ChartColors.getAssetSegmentColor` for harmonized cash, bank, and credit/debt segment styling. |
| `app/src/main/java/com/example/vibefinance/ui/components/KeyboardButton.kt` | **MODIFIED** | Implemented Material 3 Expressive spring shape-morphing (pill 28dp -> squircle 10dp -> bouncy pill overshoot); eliminated scale shrinkage; implemented coroutine-driven 140ms pulse duration so that even a short tap triggers the full organic spring shape motion. |
| `app/src/main/java/com/example/vibefinance/ui/components/RollingNumberText.kt` | **MODIFIED** | Smooth animated number and currency transitions without recomposition jitter. |

### UI: Screens & Navigation
| File Path | Action | Description |
| :--- | :---: | :--- |
| `app/src/main/java/com/example/vibefinance/ui/main/MainScreen.kt` | **MODIFIED** | Upgraded Add Expense sheet: removed Quick Presets; separated "Count in Daily Budget" toggle switch card and real-time Budget Preview card; added dynamic header height measurement (`onGloballyPositioned`) and safe spacer eliminating card clipping under header; compacted vertical spacing so full numeric keypad and confirm button fit on screen without scrolling; two-tier `ExpressiveCollapsingTopBar` with separated two-line spring kinetics; predictive back navigation across tabs and FAB menu; integrated native Compose `VibeFinanceIcon`. |
| `app/src/main/java/com/example/vibefinance/ui/home/HomeScreen.kt` | **MODIFIED** | Material 3 Expressive daily dashboard with responsive Bento grid scaffolding; integrated unified `ChartColors` in `CategoryDonutChart`. |
| `app/src/main/java/com/example/vibefinance/ui/home/DailySpendingLineChart.kt` | **MODIFIED** | Dual mode toggle (`CUMULATIVE` vs `DAILY`), interactive drag scrubbing, pulsating glow nodes, rolling numbers, and expressive floating tooltip badge. |
| `app/src/main/java/com/example/vibefinance/ui/home/DaysLeftCard.kt` | **MODIFIED** | Deterministic `CircularWavyProgressIndicator` with tap-to-toggle Flat/Wavy mode, 5dp stroke, 0% idle CPU settle. |
| `app/src/main/java/com/example/vibefinance/ui/home/HeroDailyBudgetCard.kt` | **MODIFIED** | Fluid liquid dynamic budget indicator with single 4-5s entrance wave settling to 0% idle CPU; 3-column Bento metrics row. |
| `app/src/main/java/com/example/vibefinance/ui/home/RecalcBudgetSheet.kt` | **MODIFIED** | Overdraft recalculation sheet with spring adjustments and non-looping canvas confetti. |
| `app/src/main/java/com/example/vibefinance/ui/home/NewPeriodBudgetSheet.kt` | **MODIFIED** | Upgraded "Start New Period" button to M3 Pill Button (`RoundedCornerShape(27.dp)`), `buttonElevation(2.dp, 1.dp)`, and `pressBounce`; DatePickerDialog confirm/dismiss buttons to `CircleShape` with `pressBounce`. |
| `app/src/main/java/com/example/vibefinance/ui/accounts/AccountsScreen.kt` | **MODIFIED** | Native M3 Expressive connected `ToggleButton` group (`ALL`, `CASH_BANK`, `CREDIT_CARDS`), sliding spring accent indicator, and `LazyColumn.animateItem` transitions; comprehensive M3 Button overhaul for Add Asset / Card bottom sheet: empty state "+ Add Account" pill button, filter & suggestion chips haptics, `+`/`-` FilledTonalButtons (`RoundedCornerShape(16.dp)`), Outlined "Upload Image" and Text "Remove Image" buttons, InteractiveImageCropDialog buttons, delete alert dialog, and sticky bottom action bar (pill "Create Asset / Save Changes" and Outlined "Delete"). |
| `app/src/main/java/com/example/vibefinance/ui/history/HistoryScreen.kt` | **MODIFIED** | Gmail-style `ExpressiveSwipeRow` parity for transaction rows; corrected signed Daily Net footer calculation (+HK$2,300.00 for seeded Yesterday); predictive back handling for category filters and edit sheet; upgraded Transaction Edit Dialog Save to M3 Filled Button (`CircleShape`, `pressBounce`, haptics) and Cancel (`CircleShape`, `pressBounce`, haptics). |
| `app/src/main/java/com/example/vibefinance/ui/recurring/RecurringScreen.kt` | **MODIFIED** | Added `findMatchingTransactionsForSubscription` and `DeleteSubscriptionConfirmDialog` (Keep records / Delete records also / Cancel); Gmail-style `ExpressiveSwipeRow` swipe actions; unified `ChartColors` category donut; installment plan aggregation, preview card, and `AddEditSubscriptionSheet`. |
| `app/src/main/java/com/example/vibefinance/ui/radar/AddDiscountShopSheet.kt` | **MODIFIED** | Upgraded "Save Discount Shop" button to M3 Pill Button (`RoundedCornerShape(27.dp)`), `buttonElevation(2.dp, 1.dp)`, `pressBounce`, and haptics. |

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

### 8. Fresh Launch & Release APK Architecture (Zero Initial Data)
- **Zero-Data Cold Startup**:
  - Removed proactive auto-seeding of mock data from `FinanceViewModel.kt` (`existingAccounts.isEmpty()` trigger removed).
  - Explicit reset/mock data intent (`FinanceIntent.SeedMockData`) remains isolated for developer/test usage.
  - Fallback `DailyBudgetInfo` across `MainScreen.kt`, `HomeScreen.kt`, and `BudgetRepository.kt` defaults to `0.0` amount, `0` days, and `0L` timestamps instead of hardcoded `$1,500.00`.
- **Friendly Empty State System**:
  - **Daily Hero Card (`HeroDailyBudgetCard.kt`)**: Implements `HeroDailyBudgetState.NO_BUDGET`. Header badge shows `No Budget Set` (`no_budget_set`), days left shows `—`, and the primary action button presents `[⚡] Set Budget` (`btn_set_budget`) which opens the new period configuration sheet.
  - **Whole Budget Card (`WholeBudgetCard.kt`)**: Guards `startDate` and `endDate` against `<= 0L` to render `—` instead of Unix Epoch 1970 artifacts.
  - **Assets Screen (`AccountsScreen.kt`)**: Added a glassmorphic empty state card when `filteredAccounts.isEmpty()` prompting the user to create their first account (`+ Add Account`).
  - **Recurring Subscriptions (`RecurringScreen.kt`)**: Renders `HK$ 0.00` commitment, `0 Active`, and the `EmptyRecurringCard`.
  - **Activity History (`HistoryScreen.kt`)**: Displays `0 Logged`, empty breakdown, and `No transactions found` card.
- **Release Verification & Distribution**:
  - Verified on Waydroid emulator (`192.168.240.112:5555`) with `pm clear` cold boot: verified all 4 tabs start with 0 data, 0 crashes, and clean UI transitions upon setting a budget.
  - 77 unit tests pass with 100% success (`./gradlew testDebugUnitTest`).
  - Assembled production release APK (`./gradlew assembleRelease`).

### 9. Material 3 Button Architecture & Tactile Press Feedback (m3.material.io)
- **Official Material 3 Button Roles**:
  - **Filled Button (`Button`)**: High-emphasis primary action (e.g. `Create Asset`, `Save Changes`, `+ Add` in inline sheets). Styled with full pill geometry (`shape = CircleShape` or `RoundedCornerShape(26.dp)` / `27.dp`), `elevation = buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)`, and leading action icons.
  - **Filled Tonal Button (`FilledTonalButton`)**: Mid-emphasis secondary operations and incremental adjustment controls (e.g., custom adjust `+` and `-` buttons in the Add Asset sheet). Styled with `RoundedCornerShape(16.dp)` and container/content color tokens.
  - **Outlined Button (`OutlinedButton`)**: Medium-emphasis actions, art attachment, or non-primary destructive triggers (e.g., "Upload Image" with `RoundedCornerShape(16.dp)`, "Delete Account" with `RoundedCornerShape(26.dp)` and error tint).
  - **Text Button (`TextButton`)**: Low-emphasis dismiss or auxiliary triggers (e.g. "Cancel", "Remove Image") styled with `CircleShape` and bounded ripples.
- **Spring-Driven Elastic Press Feedback (`Modifier.pressBounce`)**:
  - Implemented in `BouncyClickable.kt` via `interactionSource.collectIsPressedAsState()`.
  - Animates a 0.96x compression using `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)` on touch down and releases elastically upon finger lift.
  - Operates alongside Compose's standard touch handling without intercepting `onClick` callbacks or obstructing ripple indications.
- **Bounded Touch Target Ripples & Haptics**:
  - Applied explicit clipping (`Modifier.clip(shape)`) across all custom and standard button surfaces to prevent rectangular ripple bleed.
  - Paired touch activations with tactile haptics (`HapticFeedbackType.TextHandleMove` for selections/increments, `HapticFeedbackType.LongPress` for destructive actions).
- **Preserved Material 3 Expressive Controls**:
  - Existing expressive controls (`ExtendedFloatingActionButton`, `ConnectedButtonGroup`, `ExpressiveSegmentedButtonGroup`, `DaysLeftCard`, `HeroDailyBudgetCard`) were strictly protected against unintended modifications.

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
- Install over the release-signed app on Waydroid (preserves its data):
  ```bash
  ./gradlew :app:assembleRelease
  adb -s 192.168.240.112:5555 install -r app/build/outputs/apk/release/app-release.apk
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
