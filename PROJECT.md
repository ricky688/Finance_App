# Project: VibeFinance Core Hard Override UI/UX Layout & Interaction Specifications

## Architecture
VibeFinance is a modern Android personal finance application built with Jetpack Compose, Kotlin Coroutines/Flow, Room DB + reactive `InMemoryDatabase`, Hilt DI, AndroidX Compose Material 3 Expressive, and AndroidX Adaptive Navigation 3.

### Module Boundaries & Ownership
- `ui/home/`: `DaysLeftCard.kt`, `RestAndSpentBudgetCard.kt`, `HomeScreen.kt` (Home budget cards, aspect ratio, text measurement, responsive stacking).
- `ui/main/`: `AdaptiveRecordScene.kt` (Dynamic 724dp dual-pane, separating hinge layout, state-aware BackHandler), `MainScreen.kt` (Transaction entry sheet, compact landscape dual-column layout, touch target protection).
- `ui/history/` & `ui/accounts/`: Consumers of `AdaptiveRecordScene` providing active dialog states.
- `ui/theme/` & `ui/common/`: Shared styling and components (`ExpressiveConnectedButtonGroup.kt`).
- `test/` & `androidTest/`: Unit tests and connected tests on Waydroid (`192.168.240.112:5555`).

## Feature Inventory
Every feature identified from survey analysis and user requirements:

| # | Feature | Description | Milestone | Source | Status |
|---|---------|-------------|-----------|--------|--------|
| 1 | F1: DaysLeftCard 1:1 Aspect Ratio | Apply `Modifier.aspectRatio(1f)` directly on `Card` in `DaysLeftCard.kt`; maintain equal height with adjacent card in side-by-side row | M1 | Survey (R1) | PLANNED |
| 2 | F2: Bento Responsive Stacking | In `HomeScreen.kt`, adaptively switch from `Row` to `Column` on narrow screen (<340dp), high fontScale, or amount overflow at 12sp floor; `RestAndSpentBudgetCard` takes `fillMaxWidth()`, `DaysLeftCard` is centered and size-constrained between 120dp-156dp square | M1 | Survey (R1) | PLANNED |
| 3 | F3: Large Negative Amounts Signed Format & Baseline | In `RestAndSpentBudgetCard.kt`, format negative numbers as `-$...` (not `$-...`); maintain single text flow (single Text / AnnotatedString) for stable baseline without vertical jumping | M1 | Survey (R2) | PLANNED |
| 4 | F4: TextMeasurer Adaptive Sizing (12sp Floor) | In `RestAndSpentBudgetCard.kt`, adaptively scale down font size using `TextMeasurer` with a strict 12sp floor; trigger parent stacking if space is insufficient; strictly forbid line wrapping or truncation ellipsis | M1 | Survey (R2) | PLANNED |
| 5 | F5: Dynamic Dual-Pane 724dp Threshold | In `AdaptiveRecordScene.kt`, replace hardcoded 800dp with dynamic threshold 724dp (list 340dp + detail 360dp + gap 24dp) in the absence of a hinge | M2 | Survey (R3) | PLANNED |
| 6 | F6: Folding Feature Separating Hinge Partitioning | In `AdaptiveRecordScene.kt`, detect vertical separating hinges via `WindowAdaptiveInfo.windowPosture.hingeList`; strictly partition panes to `bounds.left` and `bounds.right` without spanning the hinge | M2 | Survey (R3) | PLANNED |
| 7 | F7: State-Aware Back Navigation Priority | In `AdaptiveRecordScene.kt`, enable back interception only when `!canShowBoth`, `selectedId != null`, `!hasActiveDialog`, and `!isImeVisible`; system IME and active dialogs consume back first | M2 | Survey (R5) | PLANNED |
| 8 | F8: Landscape Short Height Dual-Column Layout | In `MainScreen.kt` (`AddExpenseSheetContent`), adapt to dual columns when `maxHeight < 480dp && maxWidth >= 600dp` (Left: TopBar + 48dp mode switch + Amount; Right: Scrollable Bento cards + bottom-right fixed submit/keypad) | M3 | Survey (R4) | PLANNED |
| 9 | F9: 48dp Minimum Touch Target Protection | Maintain minimum 48dp touch target for all buttons (remediate Clear button from 32dp to 48dp); strictly preserve existing 48dp height of Connected Buttons | M3 | Survey (R4) | PLANNED |
| 10 | F10: Project Hygiene, Version Bump & Device Verification | Target exclusively Waydroid `192.168.240.112:5555`; set prerequisite props (`dex2oat64`, `extract`); bump version from 186/1.3.45 to 187/1.3.46 in `build.gradle.kts`, `modify.md`, `PROGRESS.md`, `README.md`; run `graphify update .`; pass `git diff --check` cleanly; no git commit/push | M4 | Survey (R6) | PLANNED |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Home Budget Bento & Adaptive Monetary Typography | `DaysLeftCard.kt`, `RestAndSpentBudgetCard.kt`, `HomeScreen.kt` (R1 & R2) | none | PLANNED |
| M2 | Adaptive Scene Dual-Pane (724dp/Hinge) & State-Aware Back Handler | `AdaptiveRecordScene.kt`, `HistoryScreen.kt`, `AccountsScreen.kt` (R3 & R5) | none | PLANNED |
| M3 | Transaction Entry Compact Landscape & Touch Target Protection | `MainScreen.kt` (`AddExpenseSheetContent`), `ExpressiveConnectedButtonGroup.kt` (R4) | none | PLANNED |
| M4 | E2E Integration, Waydroid Verification & Project Hygiene | Version 187/1.3.46, full test suite pass on Waydroid `192.168.240.112:5555`, `graphify update .`, `git diff --check` (R6) | M1, M2, M3 | PLANNED |

## Interface Contracts

### Home Bento (`HomeScreen.kt` ↔ `DaysLeftCard.kt` & `RestAndSpentBudgetCard.kt`)
- `DaysLeftCard(daysLeft: Int, totalDays: Int, modifier: Modifier)`:
  - Internally applies `Modifier.aspectRatio(1f)` directly on `Card`.
  - When in side-by-side mode, row sets equal height via `Modifier.height(daysSide)`.
  - When in vertical stacking mode, card is centered horizontally and size is constrained between `120.dp` and `156.dp`.
- `RestAndSpentBudgetCard(...)`:
  - Renders single text flow with signed format: `-$12,345.67` or `$12,345.67`.
  - Uses `rememberTextMeasurer` to dynamically scale font size down to `12.sp` minimum floor.
  - Informs parent container when space is insufficient at 12sp to trigger vertical stacking.

### Adaptive Record Scene (`AdaptiveRecordScene.kt` ↔ `HistoryScreen.kt` / `AccountsScreen.kt`)
- `AdaptiveRecordScene(..., hasActiveDialog: Boolean = false, adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo())`:
  - `canShowBoth`: `true` if separating vertical hinge exists, OR `maxWidth >= 724.dp` without hinge.
  - Hinge partitioning: panes partitioned to `[0, hinge.bounds.left]` and `[hinge.bounds.right, screenWidth]`.
  - `BackHandler(enabled = !canShowBoth && selectedId != null && !hasActiveDialog && !isImeVisible)`:
    - If `isImeVisible == true` or `hasActiveDialog == true`, back interception is disabled.

### Transaction Entry Sheet (`MainScreen.kt` `AddExpenseSheetContent`)
- Checks `isLandscapeCompact = maxHeight < 480.dp && maxWidth >= 600.dp`.
  - If true: renders dual-column layout.
  - `TransactionModeSwitcher`: strictly retains `Modifier.heightIn(min = 48.dp)`.
  - Clear button: `Modifier.size(48.dp)` with 20dp icon.

## Code Layout
- Production Source: `app/src/main/java/com/example/vibefinance/`
- Unit Tests: `app/src/test/java/com/example/vibefinance/`
- Instrumentation Tests: `app/src/androidTest/java/com/example/vibefinance/`
- Target Device: strictly Waydroid instance `192.168.240.112:5555`
