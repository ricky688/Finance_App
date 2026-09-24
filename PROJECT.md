# Project: VibeFinance Android Material 3 Expressive Optimization

## Architecture
VibeFinance is a modern Android personal finance application built with Jetpack Compose, Kotlin Coroutines/Flow, Room DB + reactive `InMemoryDatabase`, Hilt DI, and Material 3 Expressive components (`androidx.compose.material3:1.5.0-alpha01`).

### Module & Package Boundaries
- `ui/theme/`: Color palettes (`Color.kt`), Theme provider (`Theme.kt`), Typography (`Type.kt`), Shapes (`Shape.kt`).
- `ui/common/`: Shared Compose modifiers and components (`BouncyClickable.kt`, `FadingEdge.kt`, `RollingNumberText.kt`, `LiquidProgressBar.kt`).
- `ui/home/`: Daily Home screen (`HomeScreen.kt`), Hero daily budget card (`HeroDailyBudgetCard.kt`), bento cards (`WholeBudgetCard.kt`, `RestAndSpentBudgetCard.kt`, `DaysLeftCard.kt`, `MinMaxSpentCard.kt`), recalculation sheet (`RecalcBudgetSheet.kt`), new period sheet (`NewPeriodBudgetSheet.kt`).
- `ui/accounts/`: Net asset value, account cards, asset filter chips, account CRUD dialogs (`AccountsScreen.kt`).
- `ui/recurring/`: Recurring subscriptions hero, quick-add carousel, timeline, subscription bottom sheet (`RecurringScreen.kt`).
- `ui/history/`: Transaction history, budget period indicator, category breakdown, swipe-to-edit/delete (`HistoryScreen.kt`).
- `ui/settings/`: Preferences, theme toggles, notification logging, database reset (`SettingsSheet.kt`).
- `data/`: Room DB (`VibeFinanceDatabase.kt`), reactive singleton (`InMemoryDatabase.kt`), Repositories (`BudgetRepository.kt`, `TransactionRepository.kt`, `AccountRepository.kt`, `SubscriptionRepository.kt`).
- `service/` & `ai/`: WorkManager workers (`DeadlineCheckWorker.kt`), Notification listeners (`PaymentNotificationListener.kt`), Pacing forecasting (`VibeForecastEngine.kt`).

## Feature Inventory
Every feature identified from survey analysis and user requirements:

| # | Feature | Description | Milestone | Source | Status |
|---|---------|-------------|-----------|--------|--------|
| 1 | Expressive Shapes Foundation | Centralized M3 Expressive shape scale (`Shape.kt` with 8dp-28dp corner tokens) bound to `MaterialTheme` | M1 | Survey (Explorer 1 & 3) | DONE |
| 2 | Expressive Typography Scale | Complete 15-token M3 typography scale with Inter & JetBrains Mono pairing in `Type.kt` | M1 | Survey (Explorer 1) | DONE |
| 3 | Shared Edge-Fading Modifiers | Reusable `horizontalFadingEdge` and `verticalFadingEdge` with `DstIn` alpha mask in `ui/common/FadingEdge.kt` | M1 | Survey (Explorer 3) | DONE |
| 4 | Responsive Guardrails | Adaptive layout scaling and compact width detection (< 360dp/400dp) | M1 | Survey (Explorer 1 & 3) | DONE |
| 5 | Floating Pill Top Bar Overhaul | Responsive horizontal layout with adaptive padding and auto-sizing text to prevent truncation | M2 | Survey (Explorer 1) | DONE |
| 6 | Hero Daily Budget Card Overhaul | Responsive 3-column metrics Bento row, auto-scaled `RollingNumberText`, dynamic M3 semantic colors | M2 | Survey (Explorer 1 & 3) | DONE |
| 7 | Bento Matrix Overhaul | Responsive padding and auto-sizing in `MinMaxSpentCard.kt`, `RestAndSpentBudgetCard.kt`, `DaysLeftCard.kt` | M2 | Survey (Explorer 1) | DONE |
| 8 | Daily Calculation Parity | 100% mathematical preservation of `DISTRIBUTE_EVENLY`, `ADD_TO_NEXT_DAY`, and overdraft recalculation | M2 | Survey (Explorer 2) | DONE |
| 9 | Accounts Screen Bento & Chips | Net Asset Value Bento layout, edge-fading chip row, semantic M3 container tokens | M3 | Survey (Explorer 1 & 3) | PLANNED |
| 10 | Recurring Screen Polish | Recurring hero summary, quick-add carousel edge fading, spring kinetics on swipe actions | M3 | Survey (Explorer 1 & 3) | PLANNED |
| 11 | History Screen Responsive Grouping | Responsive segmented button group in `BudgetPeriodIndicatorCard`, category breakdown, test tag preservation | M3 | Survey (Explorer 1 & 2) | PLANNED |
| 12 | Accounting & State Invariants | Preservation of CC debt vs Cash/Bank asset calculations, auto-charge triggers, transaction CRUD rules | M3 | Survey (Explorer 2) | PLANNED |
| 13 | Add Expense / Income Sheet Overhaul | Responsive amount pill header, edge-fading category chips, spring tactile keypad | M4 | Survey (Explorer 1 & 3) | PLANNED |
| 14 | Budget Sheets Overhaul | Semantic M3 container tokens, spring card selections, smooth counters in `RecalcBudgetSheet` & `NewPeriodBudgetSheet` | M4 | Survey (Explorer 1) | PLANNED |
| 15 | Settings Sheet Polish | M3 container tokens (`surfaceContainer`, `outlineVariant`) and unified elevations | M4 | Survey (Explorer 1) | PLANNED |
| 16 | Waydroid Emulator Verification | 100% verification on Waydroid (`-s 192.168.240.112:5555`) across all 4 tabs and action sheets | M4 | Survey (Explorer 3) | PLANNED |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Design System Tokens & Shared Components | `theme/Shape.kt`, `theme/Theme.kt`, `theme/Type.kt`, `ui/common/FadingEdge.kt` | none | DONE (Passed Gate Iteration 1: 39 unit/stress tests pass, CLEAN audit) |
| M2 | Daily Home Screen & Top Bar Overhaul | `ui/home/`, `ui/main/MainScreen.kt` (Top Bar, Hero Card, Bento matrix) | M1 | DONE (Passed Gate Iteration 2: 57 tests pass, 0 clipped nodes on Waydroid, CLEAN audit) |
| M3 | Assets, Recurring & History Overhaul | `ui/accounts/`, `ui/recurring/`, `ui/history/`, `ui/components/` | M1, M2 | IN_PROGRESS |
| M4 | Action Sheets, Full E2E & Waydroid Verification | `ui/home/RecalcBudgetSheet.kt`, `NewPeriodBudgetSheet.kt`, `SettingsSheet.kt`, Waydroid verification | M1, M2, M3 | PLANNED |

## Interface Contracts

### Theme & Design Tokens (`ui/theme/` ↔ UI Components)
- `MaterialTheme.shapes`:
  - `extraSmall`: `RoundedCornerShape(8.dp)`
  - `small`: `RoundedCornerShape(12.dp)`
  - `medium`: `RoundedCornerShape(16.dp)`
  - `large`: `RoundedCornerShape(24.dp)`
  - `extraLarge`: `RoundedCornerShape(28.dp)`
- `MaterialTheme.typography`:
  - Full set of 15 standard M3 tokens with `fontFamily = InterFontFamily` (and `JetBrainsMonoFontFamily` for numbers/code).

### Shared Modifiers (`ui/common/FadingEdge.kt` ↔ Screen Chip Rows)
- `Modifier.horizontalFadingEdge(startFadeWidth: Dp = 16.dp, endFadeWidth: Dp = 16.dp): Modifier`:
  - Applies `drawWithContent` gradient mask with `CompositingStrategy.Offscreen` and `BlendMode.DstIn`.
- `Modifier.verticalFadingEdge(topFadeHeight: Dp = 16.dp, bottomFadeHeight: Dp = 16.dp): Modifier`:
  - Applies vertical gradient mask for lists and sheet bodies.

### Financial Calculation Invariants (`data/repository/` ↔ `ui/`)
- `BudgetRepository.calculateDailyBudget`:
  - `DISTRIBUTE_EVENLY`: `periodRemainingBeforeToday / daysLeft`.
  - `ADD_TO_NEXT_DAY`: `standardBaseDaily + yesterdayLeftover`.
  - Overdraft: `newDailyBudget = remainingAfterToday / futureDays`.
- `TransactionRepository`:
  - CC accounts: expenses increase balance (debt), transfers decrease balance.
  - Cash/Bank accounts: expenses decrease balance (assets), transfers decrease balance.

## Code Layout
- Production Source: `app/src/main/java/com/example/vibefinance/`
- Unit Tests: `app/src/test/java/com/example/vibefinance/`
- Instrumentation Tests: `app/src/androidTest/java/com/example/vibefinance/`
- Build Output: `app/build/outputs/apk/debug/app-debug.apk`
- Emulator Target: strictly `192.168.240.112:5555` via ADB
