# VibeFinance Design System & UI/UX Specification (M3E)

> **Material 3 Expressive (M3E) • Kinetic Motion • Tactile Feedback • Adaptive Financial Typography**
> 
> *Version: 1.0.0 — Single Source of Truth (SSOT) for VibeFinance Design System*

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
1. **Expressive Over Utility:** Surfaces are soft, organic, and inviting (`RoundedCornerShape(24.dp)`+, Squircles). Sharp corners and flat full-bleed rectangles are eliminated.
2. **Kinetic & Alive:** Static, instantaneous jumps are strictly forbidden. Every state transition, sheet expansion, and number change flows naturally with physics-based springs and micro-overshoot easing.
3. **Harmonized Dynamism:** All surfaces, borders, and charts derive their colors from Material You dynamic tokens harmonized via Google HCT algorithms (`harmonize()`).
4. **Adaptive Financial Legibility:** Monetary values and metrics must never truncate (`$1,23...`). Typography automatically measures container space and dynamically scales to fit all screen sizes.
5. **Multi-Sensory Confidence:** Every meaningful user interaction is reinforced through synchronized motion and calibrated mechanical haptics (`VibeHaptics`).

---

## 2. Foundations & Design Tokens

### A. Expressive Geometry & Shapes
VibeFinance avoids primitive rectangles in favor of continuous curvature (superellipses) and rounded container pods.

| Token / Shape Role | Value / Geometry | Intended Use Case |
| :--- | :--- | :--- |
| **Squircle / Continuous** | `ContinuousRoundedRectangle` | Hero balance cards, prominent Bento pods, keypad buttons |
| **Extra Large Container** | `RoundedCornerShape(28.dp)` | Modal bottom sheets (`topStart`/`topEnd`), primary dialogs |
| **Large Container** | `RoundedCornerShape(24.dp)` | Primary Bento cards, category detail cards, floating search bar |
| **Medium Container** | `RoundedCornerShape(16.dp)` | Inset list items, filter chips, secondary metric tiles |
| **Small Pill / Badge** | `RoundedCornerShape(12.dp)` or `CircleShape` | Currency badges, status tags, connected button segments |
| **Icon Bubble** | `CircleShape` (40.dp ~ 48.dp) | Category icons, action targets in swipe rows |

#### Grouped Inset Lists (`ShapeDefaults.byIndex`)
When rendering vertical sequences of cards or settings items, corners adapt based on index:
- **First Item:** `RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)`
- **Middle Items:** `RoundedCornerShape(4.dp)`
- **Last Item:** `RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)`
- **Single Item:** `RoundedCornerShape(20.dp)`

---

### B. Dynamic Color System & Surface Roles
Surfaces must never use hardcoded hex values. Bind all elements to semantic `MaterialTheme.colorScheme` tokens:

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
When integrating category-specific colors (e.g., Food, Travel, Entertainment) or custom user palettes, always harmonize them with the current dynamic theme:
```kotlin
val harmonizedCategoryColor = categoryColor.harmonize(
    withColor = MaterialTheme.colorScheme.primary
)
```

---

### C. Specular Rim Lighting & Shaders
For colored cards, hero gradients, and glassmorphic overlays:
1. **4-Stop Specular Rim Border:** Simulate top-down studio lighting using an asymmetric gradient border:
   - Top-Start: `White.copy(alpha = 0.65f)`
   - Mid-Top: `White.copy(alpha = 0.35f)`
   - Mid-Bottom: `White.copy(alpha = 0.15f)`
   - Bottom-End: `White.copy(alpha = 0.20f)`
2. **Zero-Reallocation Shader Caching:** Execute brush creation inside `Modifier.drawWithCache`:
   ```kotlin
   Modifier.drawWithCache {
       val borderBrush = Brush.linearGradient(
           colors = listOf(
               Color.White.copy(alpha = 0.65f),
               Color.White.copy(alpha = 0.35f),
               Color.White.copy(alpha = 0.15f),
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
3. **Contrast Protection:** When placing white or light text over rich gradients, apply a subtle drop shadow:
   ```kotlin
   style = TextStyle(
       shadow = Shadow(
           color = Color.Black.copy(alpha = 0.40f),
           offset = Offset(0f, 2f),
           blurRadius = 4f
       )
   )
   ```

---

### D. Adaptive Financial Typography

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

- **`FancyTransitionEasing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)`**: Standard easing for navigation transitions, modal bottom sheets, expand/collapse cards, and floating dialogs. Provides an organic micro-overshoot.

---

### B. Spring Physics Matrix

Always select spring parameters based on interaction scope:

| Interaction Scope | Spring Parameters | Physical Feel & Rationale |
| :--- | :--- | :--- |
| **Touch Downscale** | `dampingRatio = MediumBouncy`<br/>`stiffness = Medium` | Snappy `0.97f` scale-down with quick elastic recovery |
| **Digit Counter Roll** | `dampingRatio = MediumBouncy`<br/>`stiffness = MediumLow` | Playful cascading wheel roll for balance updates |
| **Playful Gestures** | `dampingRatio = 0.35f`<br/>`stiffness = Low` | Organic, floaty spring for card drag releases and easter eggs |
| **Tab / Screen Switch** | `dampingRatio = NoBouncy`<br/>`stiffness = MediumLow` | **Strictly no oscillation.** Prevents visual vertigo |
| **Page Swiping** | `dampingRatio = NoBouncy`<br/>`stiffness = Medium` | Smooth linear deceleration following user gesture |
| **Color / Alpha Fade** | `dampingRatio = NoBouncy`<br/>`stiffness = Low` | Fades must never overshoot past 1.0 or 0.0 |

---

### C. 120 FPS GPU Layering Invariant
All high-frequency gesture transforms (scaling, translations, rotations, alpha fades) must execute inside `Modifier.graphicsLayer` or `DrawModifierNode` (`withTransform { scale(...) }`). This bypasses the Compose layout and recomposition pipelines:

```kotlin
Modifier.graphicsLayer {
    scaleX = animatedScale
    scaleY = animatedScale
    alpha = animatedAlpha
}
```

---

### D. Dynamic Alpha Edge Masking (`BlendMode.DstIn`)
When fading the edges of scrollable rows, chips, or financial charts, never overlay a solid gradient (which breaks in dark/AMOLED modes). Use `BlendMode.DstIn` on an offscreen compositing layer:

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
- **Divider:** `outlineVariant.copy(alpha = 0.5f)` hairline divider inset by `56.dp` (aligned with title text, past the icon bubble).

### 3. Morphing Keypad (`KeyboardButton`)
- **Normal State:** Circular shape (`CircleShape`) or `RoundedCornerShape(24.dp)`.
- **Pressed State:** Smoothly morphs corner radius down to `16.dp` while scaling down to `0.94f` with `VibeHaptics.selection(view)`.

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
│    • AppModalBottomSheet (28.dp top corners)                │
│    • Adaptive monetary display (calcAdaptiveFont)           │
│    • ExpressiveConnectedButtonGroup (Expense/Income/Transfer)│
│    • Morphing numeric keypad with tactile VibeHaptics       │
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
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Quality Bar & Verification Checklist

Before approving any UI modification or new screen in VibeFinance, verify:

- [ ] **No Hardcoded Hex Colors:** Every surface, border, and text references `MaterialTheme.colorScheme.*`.
- [ ] **Adaptive Font Measurement:** Monetary values scale using `calcAdaptiveFont` with no ellipses or line wraps.
- [ ] **Spring Kinetics:** Animations use `FancyTransitionEasing` or standard spring specs (bouncy for micro-press, non-bouncy for tabs).
- [ ] **120 FPS GPU Safety:** Scale, translation, and alpha animations are encapsulated in `Modifier.graphicsLayer`.
- [ ] **VibeHaptics Integration:** No direct `view.performHapticFeedback()` calls exist; `VibeHaptics` is used throughout.
- [ ] **Accessible Targets:** Interactive touch targets meet or exceed `48.dp × 48.dp`.
- [ ] **AMOLED Contrast:** Pure black mode retains visible card boundaries via `outlineVariant` hairline borders.
- [ ] **Reduced Motion Support:** All complex transitions check `LocalExperience.current.motion` and gracefully degrade when reduced.
- [ ] **Zero Idle CPU:** Canvas loops and particle tickers terminate immediately when elements expire.
