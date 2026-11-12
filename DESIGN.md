# VibeFinance Design System & UI/UX Specification (M3E)

> **Material 3 Expressive (M3E) • Kinetic Motion • Tactile Feedback • Adaptive Financial Typography**
> 
> *Design specification version: 1.2.0 — updated 2026-11-07. Documentation version only; not an app release.*
>
> This is the target design contract, not a claim that every existing screen already implements it. Morphe and ImageToolbox references below are observed project choices, not official Material 3 token requirements. User-approved behavior and accessibility take precedence over generic recipes in this document or older M3E guidance.

---

## 1. Design Philosophy & Brand Identity

VibeFinance reimagines personal finance management through Google's **Material 3 Expressive (M3E)** design language. Rather than presenting numbers as dry accounting spreadsheets, the application transforms financial tracking into an engaging, tactile, and visually reassuring ritual.

```
                     ┌──────────────────────────────────────────────┐
                     │         Material 3 Expressive (M3E)          │
                     │          meets Modern Personal Finance       │
                     └──────────────────────┬───────────────────────┘
                                            │
         ┌──────────────────────────────────┼──────────────────────────────────┐
         │                                  │                                  │
         ▼                                  ▼                                  ▼
┌──────────────────┐               ┌──────────────────┐               ┌──────────────────┐
│ Fluid Kinetics   │               │ Organic Geometry │               │ Multi-Sensory    │
│ & Spring Motion  │               │ & Specular Light │               │ Tactile Haptics  │
│ • FancyEasing    │               │ • Squircle       │               │ • VibeHaptics    │
│ • Bouncy springs │               │ • Rim lighting   │               │ • Press scales   │
│ • 120 FPS GPU    │               │ • Bento Grids    │               │ • Pill confirms  │
└──────────────────┘               └──────────────────┘               └──────────────────┘
```

### Core Tenets
1. **Financial Confidence First:** Clear amounts, account meaning and predictable actions take priority over decoration. Large Bento surfaces retain soft 24dp+ corners; compact controls and grouped rows use their own smaller shape tokens.
2. **Purposeful Motion:** Animate user-driven changes with a short, consistent rhythm. Stable values must stay still on tab return. Reduced/Minimal motion, initial rendering and noninteractive content may use restrained fades or instant updates.
3. **Semantic Color:** App surfaces and controls use Material You roles. Harmonize category accents where appropriate; preserve emoji colors, payment-network/bank branding, user card images and established cartographic colors.
4. **Adaptive Financial Legibility:** Monetary values and metrics must never truncate (`$1,23...`). Typography automatically measures container space and dynamically scales to fit all screen sizes.
5. **Proportionate Feedback:** Meaningful actions use one clear visual response and optional calibrated `VibeHaptics`; avoid duplicate pulses or several competing press animations.

### Morphe Reference: Adoption Boundary

The local Morphe Manager v1.35.0 (`da8545c9`) analysis is recorded in [Morphe UI analysis](docs/research/morphe-ui-analysis-2026-11-07.md). The findings come from source and bundled screenshots, not measured frame timings or a usability study.

| Reference pattern | VibeFinance decision |
| :--- | :--- |
| Shared spacing, shape and motion families | Adopt as a design discipline; avoid per-screen arbitrary values. |
| 16/24/32dp adaptive margins; clear title/supporting-text hierarchy | Adopt for new/refined layouts; keep existing component contracts when modifying unrelated UI. |
| Low shadow, tonal surfaces and optional fine outlines | Adopt for ordinary analytical/settings surfaces. Reserve rich gradients for selected hero/card surfaces. |
| 220ms local transitions, 320ms navigation, asymmetric counter fades | Use as reference profiles through existing motion helpers; do not replace native sheet, predictive Back or validated gesture behavior. |
| Morphe 560dp content cap and 80dp app cards | Do not impose globally. Financial dashboards, editors and large-font transaction rows need different widths/heights. |
| Morphe 0.97 press scale and indication-free home cards | Selective only. Preserve VibeFinance's shape-only transaction controls and gray bounded ripple on informational cards. |
| Animated circles/parallax and translucent colored cards | Optional decoration only; default financial reading surfaces stay quiet. No new background animation is required. |
| Morphe palette hex values and 3:1 contrast helper | Reference evidence only. Keep VibeFinance dynamic/custom themes and validate normal text at 4.5:1. |

This update changes documentation only. Runtime adoption must be incremental and separately verified; it must never alter financial calculations, balances, persistence or synchronization behavior.

### ImageToolbox Reference: Native Foundation, Shared Expressive Adapters

The local ImageToolbox checkout (`e73e1461e8`) is analyzed in [ImageToolbox UI analysis](docs/research/imagetoolbox-ui-analysis-2026-11-07.md). Its Material3 dependency is1.5.0-alpha29. Findings are source-level observations, not a recommendation to upgrade VibeFinance dependencies.

**Construction priority:** native Material/platform behavior → existing VibeFinance shared component → thin theme/shape/feedback adapter → custom drawing or gestures only for a verified unmet need. Morphe supplies useful visual rhythm; ImageToolbox demonstrates how shared adapters can carry that rhythm into Material components. Neither repository's entire implementation should be copied as a package.

| Observed ImageToolbox implementation | VibeFinance adoption rule |
| :--- | :--- |
| EnhancedButton delegates to Material3 OutlinedButton and shares InteractionSource with shapeByInteraction | Prefer native buttons with a thin shared wrapper. Keep the same source for press shape, native ripple and focus; no second clickable or parallel gesture recognizer. |
| MaterialExpressiveTheme injects modifiedShapes and CustomMotionScheme | Centralize shapes/motion through our existing theme and helpers. These are custom project specs, not official M3E constants. |
| ShapeDefaults.byIndex uses outer16dp/inner4dp; ExpandableItem defaults20dp and pressed6dp | Learn grouping and same-shape clipping. Keep VibeFinance's established20dp grouped outer corners and fast family-specific press shapes; do not force every pressed corner to6dp. |
| container combines shape, fill, clipping, border and optional shadow | Reuse existing VibeFinance container/press helpers; do not create another parallel styling system. Match visible shape and indication bounds when expanded as well as collapsed. |
| SettingGroupItem uses surfaceContainer blended toward surfaceContainerLowest by0.4 | Use restrained semantic surface grouping; treat the blend as a reference, not a required tint overlay on every card. |
| PreferenceItem title16sp/18sp Medium; subtitle12sp/14sp | Adopt hierarchy and weight allocation; retain readable localized body sizes and font scaling rather than shrinking all financial supporting text to12sp. |
| Shared hapticsClickable delegates foundation.clickable and LocalIndication | Preserve native indication/role semantics, but route through VibeHaptics and avoid duplicate pulses. Existing gray informational-card press feedback remains. |

#### Native Semantics and Interaction Guardrails
- Preserve real `enabled`, `selected`, `Role.Switch`/toggleable state and accessible action labels. Do not keep a disabled button enabled underneath a transparent blocking Surface.
- Keep minimum48dp touch regions; do not globally disable Material minimum-interactive-size enforcement. Verify nested targets and keyboard/focus operation explicitly.
- Retain native drag, cancellation and predictive Back handling. Do not copy ImageToolbox's custom release/long-press recognizers or unconditional `clearFocus()` into transaction entry, which must retain keyboard continuity.
- ImageToolbox's `shapeByInteraction` domain default has200ms state throttling; VibeFinance keypad/rapid connected actions must not inherit it.
- ImageToolbox's button-group press can resize layout/weights. Preserve VibeFinance's connected dimensions, stable outer corners and absence of layout shake rather than reproducing neighbor-width changes by default.
- ImageToolbox offers multiple switch implementations; its MaterialYou branch bridges AndroidView/MaterialSwitch and a Compose placeholder. VibeFinance keeps its single native Material3 AppSwitch family; no style proliferation or AndroidView bridge solely to imitate appearance.
- Its modal-sheet wrapper uses native ModalBottomSheet in Preview but a separate modal-sheet implementation at runtime. Keep VibeFinance's existing AppModalBottomSheet and verify runtime behavior; a successful Preview is not runtime parity evidence.

#### Motion Evidence: Use the Dispatch Pattern, Not Every Speed

ImageToolbox's actual custom MotionScheme returns defaultSpatial=tween400ms with FancyTransitionEasing; fastSpatial=spring(damping0.6, stiffness800); slowSpatial=spring(0.8,200); defaultEffects=spring(1.0,1600); fastEffects=tween300ms and slowEffects=tween500ms. A separately declared default-spatial380 stiffness is not used by that tween. Its shape helper uses spring(0.4, Spring.StiffnessLow).

These are alternative source profiles, not replacements for the Morphe-inspired local220ms/navigation~320ms references below or the current VibeFinance motion scheme. In particular, do not globally import its600ms horizontal transition or500ms expand/700ms collapse into frequently used financial controls. Keep existing user-approved right-side navigation and native predictive Back. Opacity/color effects must remain valid; do not apply a spatial overshoot curve indiscriminately to alpha.

#### Preview-First Component Refinement
- Keep previewable UI functions separate from ViewModels, notification access, real account stores and cloud services. Use immutable synthetic sample data.
- Preview native/default, selected, expanded, pressed/focused where supported, disabled, loading and error states; include Traditional Chinese, large monetary values and empty content.
- Compare compact/medium/expanded layouts, light/dark/custom palettes and fontScale1.3–1.5 through fixture-local configuration only.
- Interactive/animation Preview helps tune local shapes and spacing. Verify native sheets, IME continuity, Back cancellation, accessibility, haptics and frame performance on Waydroid before claiming completion.

---

## 2. Foundations & Design Tokens

### A. Expressive Geometry & Shapes
Use expressive geometry according to component role. A section, a compact filter and an edge-docked drawer must not all inherit the same silhouette. Keep current user-selected Material shapes; do not introduce custom substitute icons or restore the removed heart option.

| Token / Shape Role | Value / Geometry | Intended Use Case |
| :--- | :--- | :--- |
| **Squircle / Continuous** | `ContinuousRoundedRectangle` | Hero balance cards, prominent Bento pods, keypad buttons |
| **Extra Large Container** | `RoundedCornerShape(28.dp)` | Modal bottom sheets (`topStart`/`topEnd`), primary dialogs |
| **Large Container** | `RoundedCornerShape(24.dp)` | Primary Bento cards, category detail cards, floating search bar |
| **Medium Container** | `RoundedCornerShape(16.dp)` | Inset list items, filter chips, secondary metric tiles |
| **Small Pill / Badge** | `RoundedCornerShape(12.dp)` or `CircleShape` | Currency badges, status tags, connected button segments |
| **Icon Bubble** | `CircleShape` (40.dp ~ 48.dp) | Category icons, action targets in swipe rows |

Primary Bento surfaces retain 24dp+, grouped editor sections retain their established 20dp shape, and secondary controls may use 12–16dp. Morphe's 14/16/18dp family is not a mandate to shrink all VibeFinance corners. Connected segments keep their outer leading/trailing silhouette stable while inner corners respond; preserve existing connected-button height and one-line labels.

### A.1 Spacing, Reading Width and Target Dimensions

| Role | VibeFinance target | Application rule |
| :--- | :--- | :--- |
| Screen horizontal inset | Compact 16dp; Medium 24dp; Expanded 32dp | Width classes: <600dp, 600–839dp, ≥840dp. Apply safe insets once; do not change device density or font scale. |
| Card inner padding | 16dp; 24dp for spacious hero/form sections | Keep related cards' text start edges aligned. |
| Dashboard gutter | 16dp | Grouped form sections retain 12dp gaps; sub-items generally 8–12dp. |
| Icon-to-text gap | 12dp; compact button icon-to-label 8dp | Allow text width to yield before squeezing controls. |
| Title-to-supporting text | 2–4dp | Entire row keeps 12–16dp vertical padding where content permits; do not let supporting text touch a divider. |
| Standard action touch target | At least 48×48dp | A 36/40dp visible pill still needs a verified 48dp hit region. |
| New primary dialog/form action | 48–52dp minimum height | Retain current native/component sizing; grow for large text rather than clipping. |
| Standard action glyph | 24dp; compact glyph 20dp | Visible size and touch target are separate. Preserve original navigation icons. |
| Dashboard feed cap | 1200dp, centered | Reading-width constraint, not a forced single-column layout. |
| Asset editor cap | 840dp | Existing 720dp preview-pane breakpoint and editor behavior remain intact. |

Morphe's 6dp compact-list gaps and fixed 80dp cards fit short app metadata. Transaction rows must accommodate dates, descriptions, reimbursement status and localized currency; use content-aware minimums, not a universal 80dp fixed height. There is no mandated percentage of empty screen space.

#### Grouped Inset Lists (`ShapeDefaults.byIndex`)
When rendering vertical sequences of cards or settings items, corners adapt based on index:
- **First Item:** `RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)`
- **Middle Items:** `RoundedCornerShape(4.dp)`
- **Last Item:** `RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)`
- **Single Item:** `RoundedCornerShape(20.dp)`

---

### B. Dynamic Color System & Surface Roles
App surfaces, borders and controls bind to semantic `MaterialTheme.colorScheme` tokens. Explicit exceptions are licensed/established branding, user media, readable cartographic palettes and properly tested data-series colors; do not replace VibeFinance's palette with Morphe's fixed blue hex values.

```
[ Canvas Base: surfaceContainerLowest / surface ]
      │
      ├── [ Bento Cards & Feeds: surfaceContainer ]
      │         │
      │         ├── [ Sub-sections & Sliders: surfaceContainerHigh ]
      │         │
      │         └── [ Modals & Dropdowns: surfaceContainerHighest ]
      │
      └── [ Highlights & Hero Actions: primaryContainer / primary ]
```

| Semantic Role | Token | Usage in VibeFinance |
| :--- | :--- | :--- |
| **Canvas Base** | `colorScheme.surfaceContainerLowest` | Scaffold background and root screen canvas |
| **Default Surface** | `colorScheme.surface` | Standard fallback background |
| **Card Pods** | `colorScheme.surfaceContainer` | Bento grid cards, budget cards, transaction history rows |
| **Elevated Sections** | `colorScheme.surfaceContainerHigh` | Search bars, slider tracks, filter pill trays |
| **Modal / Dialog** | `colorScheme.surfaceContainerHighest` | Modal bottom sheets, popup menus, alert dialogs |
| **Primary Action** | `colorScheme.primaryContainer` | Key buttons, active selection pills, hero card backgrounds |
| **High Emphasis** | `colorScheme.primary` | Submit buttons, floating action buttons, active toggles |
| **Transfer / Trend** | `colorScheme.tertiary` / `tertiaryContainer` | Cross-account transfers, savings progress markers |
| **Deficit / Alert** | `colorScheme.errorContainer` | Budget overrun warnings, negative delta tags |
| **Subtle Outline** | `colorScheme.outlineVariant` | Hairline card borders (0.5dp ~ 1dp) for AMOLED contrast |

#### Dynamic Color Harmonization (Google HCT)
When integrating category-specific decorative colors, harmonize where useful without changing category identity or overriding explicit user choices:
```kotlin
val harmonizedCategoryColor = categoryColor.harmonize(
    withColor = MaterialTheme.colorScheme.primary
)
```

---

### C. Specular Rim Lighting & Shaders
Ordinary settings/analytics cards use tonal separation, typically 0dp shadow elevation and an optional 1dp outline. Existing semantic surface tokens are preferred over adding a second tonal tint. Rich colored account/hero cards may use the treatment below; it is not universal chrome.

For colored cards, hero gradients, and glassmorphic overlays:
1. **4-Stop Specular Rim Border:** Simulate top-down studio lighting using an asymmetric gradient border:
   - Top-Start: `White.copy(alpha = 0.65f)`
   - Mid-Top: `midColor.copy(alpha = 0.30f)`
   - Mid-Bottom: `endColor.copy(alpha = 0.15f)`
   - Bottom-End: `White.copy(alpha = 0.20f)`
2. **Zero-Reallocation Shader Caching:** Execute brush creation inside `Modifier.drawWithCache`:
   ```kotlin
   Modifier.drawWithCache {
       val borderBrush = Brush.linearGradient(
           colors = listOf(
               Color.White.copy(alpha = 0.65f),
               midColor.copy(alpha = 0.30f),
               endColor.copy(alpha = 0.15f),
               Color.White.copy(alpha = 0.20f)
           ),
           start = Offset(0f, 0f),
           end = Offset(size.width, size.height)
       )
       onDrawWithContent {
           drawContent()
           drawOutline(..., brush = borderBrush)
       }
   }
   ```
3. **Contrast Protection:** Select a readable foreground against the composited fill first. A subtle text shadow may supplement rich gradients; it does not replace contrast validation and is omitted on ordinary neutral text:
   ```kotlin
   style = TextStyle(
       shadow = Shadow(
           color = Color.Black.copy(alpha = 0.40f),
           offset = Offset(0f, 2f),
           blurRadius = 4f
       )
   )
   ```

#### Translucency and Blur Budget
- Morphe-inspired optional glass fills: selected alpha light/dark 0.72/0.55; unselected 0.60/0.35. These are reference starting values, not forced overrides of current VibeFinance controls or ripple opacity.
- Optional rich-card fill stops: 0.70/0.58/0.64 alpha; optional rim width 1–1.5dp. Check contrast after compositing over the actual background.
- Prefer tint and a fine outline for ordinary separation. Do not add full-screen real-time blur merely to imitate a glass appearance.
- Preserve the established shared bar treatment: bottom transition 32dp, top transition 24dp, maximum blur 8dp, theme-aware tint/fallback. Respect user blur controls; avoid stacked broad fades and suspend background blur behind modal sheets.
- Edge fades appear only where content is clipped/scrollable in that direction: no left fade at start, no right fade at end. Do not cover labels or action hit targets.
- Normal text must meet 4.5:1 and large text/non-text essential controls 3:1 as applicable. Morphe's default 3:1 helper is not sufficient proof for VibeFinance body text. Test dynamic, custom and pure-black palettes.

---

### D. Adaptive Financial Typography

Use a calm hierarchy around expressive amounts: setting/list titles use `bodyLarge` with Medium weight, supporting text uses `bodyMedium` with `onSurfaceVariant`, and section titles use the existing title role. Reference body rhythm is 16sp text /24sp line height; preserve localized font metrics and system font scaling. Keep Roboto Flex for the established transaction amount treatment. Do not make every label bold or reduce all supporting text to a fixed low alpha.

> [!IMPORTANT]
> **Zero Truncation Guarantee:** A user's account balance, expenditure total, or monthly budget must never display as `$1,23...` or wrap unexpectedly across lines.

1. **Adaptive Font Scaling Algorithm (`calcAdaptiveFont`):**
   Use `ParagraphIntrinsics` to measure text against container bounds and dynamically downscale the font size until it fits cleanly:
   ```kotlin
   @Composable
   fun calcAdaptiveFont(
       text: String,
       targetWidth: Dp,
       maxFontSize: TextUnit = 38.sp,
       minFontSize: TextUnit = 20.sp
   ): TextUnit {
       // Iteratively scales down font by 0.9f step until width <= targetWidth
   }
   ```
2. **Currency Prefix Baseline Alignment:**
   - Display amounts prominently (e.g., `32.sp` to `42.sp` bold).
   - Display the currency symbol (`HK$ ` or `US$ `) in `primary` theme color at a slightly smaller scale (e.g., `22.sp` to `28.sp` bold) aligned to the numeric baseline.
3. **Tabular Figures:**
   - Use fixed-width / tabular figures where numbers change dynamically to prevent horizontal jitter during transitions.

---

## 3. Transitional Animation & Kinetic Motion

### A. Standard Easing Curves

```
1.0 ┼─────────────────────────╭──────╮  <-- Micro-Overshoot (1.03)
    │                       ╭╯       ╰──
0.8 ┼                     ╭─╯
    │                   ╭─╯
0.6 ┼                 ╭─╯
    │               ╭─╯
0.4 ┼             ╭─╯
    │          ╭──╯
0.2 ┼      ╭───╯
    │  ╭───╯
0.0 ┼──┴─────────────────────────────────
    0.0   0.2   0.4   0.6   0.8   1.0
```

- **`FancyTransitionEasing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)`** remains available for established expressive navigation. Do not impose it on every fade, expand/collapse or native sheet. Native motion, predictive gestures and existing user-approved right-side editor transitions retain their own specs.

### A.1 Motion Profiles Inspired by Morphe

These are implementation targets for future tuning through shared helpers, not a statement that existing VibeFinance durations already match them.

| Purpose | Reference profile | Guardrail |
| :--- | :--- | :--- |
| Local expansion, dialog or selection-color blend | 220ms | `LinearOutSlowInEasing` for gentle settling; `FastOutSlowInEasing` for deliberate spatial movement. |
| Short fade/removal | 180ms | Alpha remains within 0–1; no bouncing opacity. |
| Full-page navigation | About 320ms | Preserve right-side transaction/asset entry; do not copy Morphe's vertical Settings push direction. |
| Counter/label replacement | Enter 200ms, exit 150ms | Stable amount width/baseline; unchanged values do not replay on tab return. |
| Small counter badge | Enter 150ms, exit 100ms | Avoid turning every minor status update into a large slide. |
| List placement | spring stiffness=400f, dampingRatio=0.8f | Slight settle; use non-bouncy sizing where overshoot can clip content. |
| Optional chip entrance | 220ms, 40ms stagger capped at 320ms delay | Initial/new content only; never replay every visible row on ordinary scroll. |

Do not globally retime existing controls in a documentation-only update. Native `AppModalBottomSheet` and predictive Back take precedence; never layer a second transform over a native transition. Keep the keypad's existing fast shape spec rather than applying the 220ms dialog profile.

For `MotionLevel.REDUCED`, suppress decorative overshoot/stagger and use restrained transitions. For `MINIMAL`, snap interactive state changes and remove spatial/bouncy motion. Haptic preferences are independent of motion preferences.

---

### B. Spring Physics Matrix

Always select spring parameters based on interaction scope:

| Interaction Scope | Spring Parameters | Physical Feel & Rationale |
| :--- | :--- | :--- |
| **Optional Touch Downscale** | `dampingRatio = MediumBouncy`<br/>`stiffness = Medium` | `0.97f` only where scale is already approved; not transaction-entry/keypad controls |
| **Digit Counter Roll** | Existing restrained number spec or 200/150ms replacement profile | No baseline shake, repeated entrance or overshoot of the financial value |
| **Playful Gestures** | `dampingRatio = 0.35f`<br/>`stiffness = Low` | Organic, floaty spring for card drag releases and easter eggs |
| **Tab / Screen Switch** | `dampingRatio = NoBouncy`<br/>`stiffness = MediumLow` | **Strictly no oscillation.** Prevents visual vertigo |
| **Page Swiping** | `dampingRatio = NoBouncy`<br/>`stiffness = Medium` | Smooth linear deceleration following user gesture |
| **Color / Alpha Fade** | `dampingRatio = NoBouncy`<br/>`stiffness = Low` | Fades must never overshoot past 1.0 or 0.0 |

---

### C. Draw-Phase Transforms & Frame Budget
Read per-frame transform state inside `Modifier.graphicsLayer` or a draw node where appropriate, keeping transforms out of measurement. This reduces work; it does not guarantee 120 FPS or eliminate all recomposition. Do not read animated values in the parent body before capturing them in a layer lambda:

```kotlin
Modifier.graphicsLayer {
    scaleX = scaleState.value
    scaleY = scaleState.value
    alpha = alphaState.value
}
```

---

### D. Dynamic Alpha Edge Masking (`BlendMode.DstIn`)
Reuse existing shared edge-effect helpers. For a non-destructive alpha mask, use `BlendMode.DstIn`; an offscreen target may be required, but constrain it to the smallest affected region. Never allocate an additional full-screen offscreen layer for every list/chart or confuse an alpha fade with real blur. A standalone example is:

```kotlin
Modifier
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.horizontalGradient(
                0.0f to Color.Transparent,
                0.08f to Color.Black,
                0.92f to Color.Black,
                1.0f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }
```

---

### E. Discrete Target Invariant for Digit Animation
- Never pass high-frequency animated floats directly to `RollingNumberText` or `AnimatedContent`.
- Provide the discrete target string (e.g., `"HK$ 12,450"`) and let each digit position independently animate using localized spring physics.

---

### F. Battery Preservation & Zero Idle CPU Rule
Financial reading surfaces default to a static, theme-aware background. Optional ambient backgrounds must be low contrast and opt-in, pause when not resumed or fully covered, and stop for Reduced/Minimal motion. Morphe's 3.5–5% alpha, 6.5–9.5s circle drift is reference evidence, not a requirement to introduce sensor/parallax or continuous rendering.

Custom canvas animations (particle dust, cash bursts, water waves) must terminate their frame loops when dormant:
```kotlin
LaunchedEffect(hasActiveParticles) {
    if (!hasActiveParticles) return@LaunchedEffect // Sleeping immediately
    while (isActive && hasActiveParticles) {
        withFrameMillis { now -> ... }
    }
}
```

---

## 4. Tactile Feedback & Multi-Sensory Haptics

### Universal `VibeHaptics` Rule
Never invoke `view.performHapticFeedback(...)` directly in UI code. All haptics must route through `VibeHaptics` or `rememberVibeHapticFeedback()` to honor the user's configured strength (`OFF`, `GENTLE`, `STANDARD`, `STRONG`).

### Semantic Haptic Mapping

| Action | `VibeHaptics` Call | Physical Sensation |
| :--- | :--- | :--- |
| **Button Tap / Pill Click** | `VibeHaptics.lightTick(view)` | Crisp, micro-tick tactile confirmation |
| **Segment / Switch Toggle** | `VibeHaptics.toggle(view)` | Distinct on/off mechanical flip |
| **Number Pad Key Tap** | `VibeHaptics.selection(view)` | Mechanical switch press feel |
| **Slider Notch Step** | `VibeHaptics.lightTick(view)` | Physical detent click when passing step interval |
| **Swipe Action Threshold (25%)** | `VibeHaptics.heavyLongPress(view)` | Deep tactile notch signaling release to act |
| **Save / Success Confirmation** | `VibeHaptics.successConfirm(view)` | Ascending double-pulse confirmation |
| **Budget Deficit / Danger Alert** | `VibeHaptics.warningAlert(view)` | Sharp double-buzz caution warning |

---

## 5. Core Component Specifications

### 1. Bento Grid Architecture
- **Purpose:** Organizes analytical summaries (Total Net Worth, Monthly Budget Progress, Top Expense Categories, Quick Action shortcuts).
- **Layout:** Non-symmetric card spans with `16.dp` gutter spacing.
- **Card Hierarchy:** Hero card spans full width; secondary tiles span 1:1 or 2:1 ratios with unified `.container()` styling.

### 2. Inset Grouped Lists (`ShapeDefaults.byIndex`)
- **Padding:** `16.dp` horizontal screen inset.
- **Corner Animation:** Corners dynamically animate (`CornerSize.animate()`) when items reorder, insert, or delete.
- **Divider:** `outlineVariant` at restrained alpha; standard grouped-settings divider has 16dp inset from the card edges. Use a title-aligned inset (often around56dp) only for the list variant designed for it. Keep 12–16dp row padding around text and do not change all dividers to full-bleed.

### 3. Morphing Keypad (`KeyboardButton`)
- **Normal State:** Circular shape (`CircleShape`) or `RoundedCornerShape(24.dp)`.
- **Pressed State:** Use the existing fast Compose shape morph with `VibeHaptics.selection(view)`. No whole-button scale-down, translation or long 220ms shape tween. Keep the user's more-rounded resting shape and existing connected-button dimensions.

### 3.1 Interactive Feedback Families
- Settings cards/subcards: preserve their existing Compose shape morph and bounded rounded ripple, including expanded state. Keep the outer container stable.
- Informational cards (spending trend, rhythm, calendar, category analytics): no navigation implied; click can still provide the requested gray bounded ripple, shape response and optional `VibeHaptics`.
- Hover is distinct from press: only actionable areas show hover feedback; always clip it to the real shape. Do not give the entire non-navigating chart a hover highlight.
- Ordinary buttons may retain native Material ripple and optional subtle 0.97 scale where already appropriate. Choose feedback per family; do not stack scale, translation, shape morph and multiple haptic pulses on every press.
- Route all haptics through `VibeHaptics`; do not copy Morphe's direct `performHapticFeedback` calls. Keyboard and nested picker events must not produce duplicate clicks on release.

### 4. Financial Charts
- **Donut / Breakdown Charts:** Must enforce a minimum sweep angle (`minSweepAngle = 28f`) so micro-categories remain visible and tappable.
- **Budget Health Gradient:** Continuous color blending from `Emerald Green` (healthy) $\to$ `Amber Orange` (warning, 75%-90%) $\to$ `Crimson Red` (overrun, >100%).

### 5. Inline Action Confirmations (`PillConfirmationState`)
- **Principle:** Prefer inline spring expansion over floating Toasts or Snackbars.
- **Flow:** When the user taps "Copy", "Save", or "Reset", the button morphs horizontally into a confirmation pill displaying a checkmark and "Copied!", then automatically springs back after 1.8 seconds.

---

## 6. Screen-by-Screen Blueprint

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Home / Dashboard                                         │
│    • ScrollBlurContainer Top Bar                            │
│    • Hero Net Worth Bento Card with Specular Rim Lighting   │
│    • RollingNumberText balance counters                     │
│    • Budget Progress Pod with dynamic health gradient       │
│    • Recent Transactions Inset List                         │
├─────────────────────────────────────────────────────────────┤
│ 2. Quick Transaction Entry                                  │
│    • Full-screen editor, established right-side transition │
│    • Adaptive monetary display (calcAdaptiveFont)           │
│    • ExpressiveConnectedButtonGroup (Expense/Income/Transfer)│
│    • Morphing numeric keypad with tactile VibeHaptics       │
│    • Native modal sheets only for nested pickers           │
├─────────────────────────────────────────────────────────────┤
│ 3. Analytics & Radar                                        │
│    • MoneySeparationChart with minSweepAngle protection     │
│    • Period Switcher with animated pill indicators          │
│    • Category breakdown cards with HCT harmonized colors    │
├─────────────────────────────────────────────────────────────┤
│ 4. Accounts & Settings                                      │
│    • Grouped Inset Cards (ShapeDefaults.byIndex)            │
│    • Tactile switches (AppSwitch) & Sliders with notch ticks│
│    • Dynamic theme & haptic intensity selectors             │
│    • Full-screen asset editor; Settings drawer from right  │
└─────────────────────────────────────────────────────────────┘
```

### Adaptive Navigation and Stable Editors
- Portrait windows below840dp retain bottom navigation, including medium-width foldables/windowed views. Landscape width≥600dp and large windows≥840dp may use the established navigation rail.
- Preserve rail action placement, scrolling support and existing expand/menu behavior. Do not sacrifice financial card width merely to copy Morphe's layout.
- The physical right-edge Settings drawer stays docked: exposed left corners28dp, right bezel corners0dp; keep the established safe scrim margin and LTR content inside scoped RTL anchoring.
- Asset/card and transaction editors remain full-screen. Preserve drafts, validation, IME behavior and keyboard continuity across expense/income/transfer. Do not replace them with bottom sheets to match an old blueprint.
- Preserve the existing square Days Left tile and matched adjacent-budget-card height; use content-aware arrangements for all other metrics and large text.

---

## 7. Quality Bar & Verification Checklist

Before approving any UI modification or new screen in VibeFinance, verify:

- [ ] **Semantic Color & Contrast:** App chrome uses semantic roles; branding/media/map exceptions are intentional. Validate normal text4.5:1 and essential controls/large text3:1 against actual composited backgrounds.
- [ ] **Adaptive Font Measurement:** Monetary values scale using `calcAdaptiveFont` with no ellipses or line wraps.
- [ ] **Motion Rhythm:** Use the correct family profile; preserve native sheets, right-side editors, fast keypad shape response and Reduced/Minimal settings. Stable amounts do not replay on tab return.
- [ ] **Frame Budget:** Per-frame transforms read state in layers/draw nodes, brushes are cached, and no unnecessary offscreen/blur layers stack. Measure frame performance before claiming120 FPS.
- [ ] **VibeHaptics Integration:** No direct `view.performHapticFeedback()` calls exist; `VibeHaptics` is used throughout.
- [ ] **Accessible Targets:** Interactive touch targets meet or exceed `48.dp × 48.dp`.
- [ ] **AMOLED Contrast:** Pure black mode retains visible card boundaries via `outlineVariant` hairline borders.
- [ ] **Reduced Motion Support:** All complex transitions check `LocalExperience.current.motion` and gracefully degrade when reduced.
- [ ] **Zero Idle CPU:** Canvas loops and particle tickers terminate immediately when elements expire.
- [ ] **Spacing & Adaptivity:** Verify narrow widths, fontScale1.3–1.5 fixtures and wide panes without modifying the device's real scale; keep one-line chips and readable supporting-text/divider gaps.
- [ ] **No Financial Side Effects:** A visual refactor changes no balances, transaction types, budget formulas, migrations or sync semantics. Never use production account mutations to test styling.
- [ ] **Incremental Adoption:** Compare one component family before/after in light/dark/custom themes and keyboard/expanded/pressed states before rolling the style across all screens. Follow modify.md; this document alone does not authorize a release or imply UI implementation.
- [ ] **Native Component Integrity:** Thin wrappers retain native enabled/selected/toggle roles, minimum touch targets, focus and cancellation. A single InteractionSource drives shape and indication; no transparent disabled overlay or redundant gesture interception.
- [ ] **Preview / Runtime Separation:** Synthetic previews cover component variants without real financial services; runtime sheet, keyboard and predictive Back behavior is checked independently of preview-only fallback paths.
