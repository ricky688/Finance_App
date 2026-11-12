# Test Infrastructure Specification: VibeFinance Core Hard Override UI/UX Track

## 1. Test Philosophy & Engineering Principles

This testing infrastructure establishes an opaque-box, contract-driven verification harness for the 5 core hard override UI/UX layout and interaction specifications (R1 through R5) in VibeFinance.

### Core Tenets
1. **Opaque-Box Specification Verification**: Tests verify observable behavior, mathematical constraints, and layout geometry against the authoritative requirements in `PROJECT.md` and `ORIGINAL_REQUEST.md`, independent of internal implementation details.
2. **Deterministic & Self-Contained**: Every test manages its own state, executes deterministically without temporal coupling or order dependencies, and cleans up completely after execution.
3. **Four-Tier Test Architecture**:
   - **Tier 1 (Feature Coverage)**: Direct baseline verification of all primary functional and layout requirements (>= 5 tests per feature).
   - **Tier 2 (Boundary Value Analysis & Stress)**: Explicit boundary probing at threshold points (320dp, 723dp vs 724dp, 479dp vs 480dp, font scale 2.0x, extreme monetary values).
   - **Tier 3 (Cross-Feature Pairwise Combinations)**: Combinatorial matrix testing of intersecting dimensions (viewport width, font scale, amount size, folding hinge, IME visibility, active dialogs).
   - **Tier 4 (Real-World Application Scenarios)**: High-fidelity end-to-end user workflows reflecting authentic device interaction patterns.
4. **Progressive Testability & Backward Compatibility**: Test contracts are verifiable using features from the current milestone and completed dependencies, without breaking legacy test suites.
5. **Strict Device Governance**: Automated and connected tests exclusively target the Waydroid instance (`192.168.240.112:5555`). Physical user hardware (`SM_S9280`) is strictly protected from automated test execution.

---

## 2. Requirement-to-Test Matrix (Tiers 1–4)

| Req | Feature Description | Tier 1 (Feature) | Tier 2 (Boundary) | Tier 3 (Pairwise) | Tier 4 (Real-World) | Total Tests |
|:---:|:---|:---:|:---:|:---:|:---:|:---:|
| **R1** | DaysLeftCard 1:1 Aspect Ratio & Bento Responsive Stacking | 5 | 5 | 3 | 2 | 15 |
| **R2** | Large Negative Monetary Amounts Single Line & Stable Baseline | 5 | 5 | 3 | 2 | 15 |
| **R3** | Dual-Pane Partition Threshold (724dp) & Folding Feature Partition | 5 | 5 | 3 | 2 | 15 |
| **R4** | Landscape Compact Dual-Column & 48dp Touch Target Protection | 5 | 5 | 3 | 2 | 15 |
| **R5** | State-Aware Back Navigation Priority (IME & Dialog Protection) | 5 | 5 | 3 | 2 | 15 |
| **Total** | **Comprehensive UI/UX Hard Override Suite** | **25** | **25** | **15** | **10** | **75** |

---

## 3. Tier Breakdown & Partitioning Specifications

### Tier 1: Feature Coverage (>= 5 tests per feature)
- **R1 (DaysLeftCard & Bento Stacking)**:
  - `T1_R1_01`: `DaysLeftCard` enforces 1:1 aspect ratio geometry (`width == height`).
  - `T1_R1_02`: In side-by-side Bento layout, `DaysLeftCard` and `RestAndSpentBudgetCard` share equal height (`daysSide`).
  - `T1_R1_03`: Bento layout uses horizontal `Row` on standard screen widths (>= 340dp) with normal font scale (1.0x).
  - `T1_R1_04`: Bento layout switches to vertical `Column` when screen width is constrained (< 340dp).
  - `T1_R1_05`: In vertical `Column` layout, `DaysLeftCard` is centered and size-constrained between 120dp and 156dp (never stretched to full width).
- **R2 (Monetary Typography & Baseline)**:
  - `T1_R2_01`: Negative monetary amount is formatted with leading minus sign before currency symbol (`-$...`).
  - `T1_R2_02`: Negative sign, currency symbol, and amount digits reside in a single continuous text flow to prevent baseline jitter.
  - `T1_R2_03`: `TextMeasurer` adaptively scales font size downward when available width is restricted.
  - `T1_R2_04`: Font size downscaling adheres to a strict minimum legibility floor of 12sp.
  - `T1_R2_05`: When amount string cannot fit within available width even at 12sp, container vertical stacking is requested.
- **R3 (Dual-Pane Threshold & Folding Hinge)**:
  - `T1_R3_01`: Dual-pane mode (`canShowBoth = true`) activates dynamically at width >= 724dp without hinge.
  - `T1_R3_02`: Single-pane mode (`canShowBoth = false`) is maintained for widths < 724dp without hinge.
  - `T1_R3_03`: Presence of a vertical separating hinge overrides screen width and activates dual-pane mode.
  - `T1_R3_04`: Left pane partition strictly stops at `hinge.bounds.left`.
  - `T1_R3_05`: Right pane partition strictly begins at `hinge.bounds.right`, ensuring content never crosses the physical hinge.
- **R4 (Landscape Compact & 48dp Touch Targets)**:
  - `T1_R4_01`: Dual-column layout activates in landscape compact conditions (`maxHeight < 480dp && maxWidth >= 600dp`).
  - `T1_R4_02`: Left column accommodates top bar, mode switcher, and amount field; right column contains category/account selectors and keypad.
  - `T1_R4_03`: Connected Buttons mode switcher strictly preserves 48dp minimum height.
  - `T1_R4_04`: Amount Clear button enforces minimum 48dp x 48dp touch target bounds.
  - `T1_R4_05`: Portrait and regular landscape layouts (`maxHeight >= 480dp`) remain in single-column format.
- **R5 (State-Aware Back Navigation)**:
  - `T1_R5_01`: `BackHandler` is disabled (`enabled = false`) when `canShowBoth == true` (dual-pane mode).
  - `T1_R5_02`: `BackHandler` is disabled (`enabled = false`) when no detail record is selected (`selectedId == null`).
  - `T1_R5_03`: `BackHandler` is enabled (`enabled = true`) in single-pane mode when detail is selected, no dialog is active, and IME is hidden.
  - `T1_R5_04`: `BackHandler` is disabled (`enabled = false`) whenever software keyboard (`WindowInsets.isImeVisible`) is active.
  - `T1_R5_05`: `BackHandler` is disabled (`enabled = false`) whenever an active dialog or picker overlay is displayed.

---

### Tier 2: Boundary Value Analysis & Extreme Stress (>= 5 tests per feature)
- **R1 (Boundary & Stress)**:
  - `T2_R1_01`: Ultra-narrow viewport (320dp) clamping: `DaysLeftCard` size clamped to exactly >= 120dp.
  - `T2_R1_02`: Extreme font scale (2.0x) triggers vertical stacking even on standard 400dp width.
  - `T2_R1_03`: Giant tablet viewport (1200dp) clamping: `DaysLeftCard` size in vertical mode clamped to <= 156dp.
  - `T2_R1_04`: Boundary zero days left (`daysLeft = 0, totalDays = 30`) preserves 1:1 geometry and 100% progress without division error.
  - `T2_R1_05`: Overdue negative days left (`daysLeft = -5, totalDays = 30`) clamps progress cleanly without negative dimensions or crash.
- **R2 (Boundary & Stress)**:
  - `T2_R2_01`: Huge negative amount `-$999,999,999.99` single-line rendering without ellipsis or line break.
  - `T2_R2_02`: Exact zero boundary `$0.00` and micro-negative `-$0.01` formatting and baseline stability.
  - `T2_R2_03`: Extreme font scale (2.0x) downscaling convergence to 12sp floor.
  - `T2_R2_04`: Critical width threshold test: 1 pixel below minimum width at 12sp correctly signals overflow.
  - `T2_R2_05`: Huge positive amount `$999,999,999.99` single-line formatting and baseline alignment parity.
- **R3 (Boundary & Stress)**:
  - `T2_R3_01`: Exact width boundary: 723dp (`canShowBoth = false`) vs 724dp (`canShowBoth = true`).
  - `T2_R3_02`: Asymmetric separating hinge (left: 300dp, hinge: 32dp, right: 468dp) strictly partitions to [0..300] and [332..800].
  - `T2_R3_03`: Flat non-separating hinge does not trigger dual-pane when width is < 724dp.
  - `T2_R3_04`: Horizontal separating hinge does not trigger vertical dual-pane partition.
  - `T2_R3_05`: Expanded breakpoint boundary at 840dp maintains dual-pane with default preferred widths.
- **R4 (Boundary & Stress)**:
  - `T2_R4_01`: Exact height boundary: 479dp (dual-column) vs 480dp (single-column) at 600dp width.
  - `T2_R4_02`: Exact width boundary: 599dp (single-column) vs 600dp (dual-column) at 479dp height.
  - `T2_R4_03`: Extreme compact landscape height (320dp height) maintains accessible controls and >= 48dp touch targets.
  - `T2_R4_04`: Connected button touch targets remain >= 48dp with empty or single-character labels.
  - `T2_R4_05`: Clear button hit-test precision verifies 48dp touch target bounds accept clicks outside 20dp icon.
- **R5 (Boundary & Stress)**:
  - `T2_R5_01`: Simultaneous IME active AND Dialog active: `BackHandler.enabled` strictly false.
  - `T2_R5_02`: IME closes while Dialog remains open: `BackHandler.enabled` remains strictly false.
  - `T2_R5_03`: Dialog closes while IME remains open: `BackHandler.enabled` remains strictly false.
  - `T2_R5_04`: Simultaneous closure of IME and Dialog in single-pane with detail selected: instantaneous transition to `enabled = true`.
  - `T2_R5_05`: Rapid detail selection/deselection alternating sequence preserves state synchronization without leaking handlers.

---

### Tier 3: Cross-Feature Pairwise Combinations
1. **Narrow Width (320dp) + Huge Negative Amount (`-$999,999,999.99`)**: Bento switches to vertical stacking, amount renders on 100% width with 12sp floor, `DaysLeftCard` is centered and square.
2. **Extreme Font Scale (2.0x) + Huge Negative Amount**: Verifies `TextMeasurer` downscaling in stacked Bento layout without text overlap.
3. **Vertical Separating Hinge + Landscape Compact Height (720dp x 400dp)**: Verifies hinge partitioning coexisting with dual-column transaction entry.
4. **Dual-Pane Active (724dp) + Active Dialog in Detail**: Verifies `BackHandler` remains disabled and dual-pane scaffolding is stable.
5. **Dual-Pane Active (724dp) + IME Active in Detail**: Verifies system keyboard consumes back press before any navigation change.
6. **Landscape Compact (640dp x 420dp) + 48dp Connected Buttons + 48dp Clear Button**: Verifies complete touch target compliance across all dual-column controls.
7. **Single-Pane Mode (< 724dp) + Detail Selected + Clean State**: Verifies back press navigation successfully exits detail to list.
8. **Extreme Font Scale (2.0x) + Landscape Compact (640dp x 400dp)**: Verifies two-column scroll container prevents clipping of buttons or inputs.
9. **723dp Width + Huge Negative Amount**: Verifies single-pane layout remains active while amount scaling accommodates the narrower container.
10. **724dp Dynamic Threshold + Separating Hinge Overlap**: Verifies hinge bounds partition takes strict precedence over default 724dp divider math.

---

### Tier 4: Real-World Application Workload Scenarios
- **Scenario 1 (Overdraft Mobile Workload)**: Daily spending exceeds budget on 360dp phone at 1.25x font scale. Verifies `-$3,450.00` single-line display, square `DaysLeftCard`, and smooth user comprehension.
- **Scenario 2 (Foldable Unfold Workload)**: User unfolds foldable device during transaction review. Verifies instantaneous transition from single-pane list to dual-pane list-detail partitioned cleanly across the physical crease.
- **Scenario 3 (Transaction Note Editing & Back Interception Workload)**: User inspects transaction, opens note editor, software keyboard appears, user taps system back button. IME dismisses while detail screen remains visible; subsequent back tap returns to list.
- **Scenario 4 (In-Vehicle Landscape Quick Add Workload)**: Phone mounted in landscape (640dp x 420dp). User opens entry sheet, switches mode via 48dp connected button, types typo amount, taps 48dp clear button, enters correct amount, and taps keypad submit.
- **Scenario 5 (Extreme Financial Variance & Accessibility Workload)**: User with high negative balance and large accessibility font scale (1.5x) browses accounts and budget, verifying no truncated numbers (`$1,23...`) or broken aspect ratios across all views.

---

## 4. Execution Commands & Verification Pipelines

### JVM Unit Test Pipeline
```bash
./gradlew testDebugUnitTest --tests "com.example.vibefinance.ui.e2e.*"
```

### Full Test Suite Compilation
```bash
./gradlew compileDebugUnitTestKotlin compileDebugAndroidTestKotlin
```

### Waydroid Connected Test Pipeline
```bash
adb -s 192.168.240.112:5555 shell setprop dalvik.vm.dex2oat64.enabled true
adb -s 192.168.240.112:5555 shell setprop pm.dexopt.install extract
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.vibefinance.ui.e2e.ConnectedE2ELayoutAndInteractionTest
```

### Static Analysis & Graphify Update
```bash
graphify update .
git diff --check
```
