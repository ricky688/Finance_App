---
name: material-3-expressive-design
description: Guidelines and architectural rules for implementing Material 3 Expressive Design and Material You dynamic aesthetics in Jetpack Compose.
version: 1.4.0
tags: [android, jetpack-compose, material-3, material-you, motion-design, responsive-design, ui-ux, morphe-design, haptics, accessibility]
---

# Material 3 Expressive & Responsive Design Rules

## 1. Core Design Philosophy
When designing or refactoring UI components in VibeFinance, strictly adhere to **Material 3 Expressive** principles:
- **Spatial Depth & Geometry:** Utilize expressive shapes (`CircleShape`, `RoundedCornerShape(24.dp)` or higher) and floating container bubbles rather than flat, full-bleed rectangles.
- **Kinetics & Physics-Based Motion:** Avoid rigid linear animations. Always use spring physics (`spring(dampingRatio, stiffness)`) for touch responses and interactive state transitions.
- **Dynamic Color (Material You):** Decouple layout structure from hardcoded colors. Always bind component surfaces and content tints to semantic tokens (`MaterialTheme.colorScheme.*`).
- **Responsive Adaptability:** Design with flexible, content-aware boundaries that adapt seamlessly across screen sizes and orientations without clipping interactive controls.
- **Zero Idle CPU Overhead (Battery Preservation):** Any custom canvas or physics animation (e.g. pond fish, water waves, particle dust) must maintain a dormant/sleeping state when idle. Never leave continuous frame tickers running in the background.
- **Accessible Motion Reduction:** Strictly respect user motion preferences (`LocalExperience.current.motion`). When motion is reduced, suppress playful bouncy springs and replace spatial translations with clean instant renders or subtle alpha crossfades.

---

## 2. Component Construction & Layout Rules

### A. Layouts & Container Scaffolding
1. **Asymmetrical & Bento Grid Hierarchy:** Group analytical and dashboard metrics using Bento Grid structures with clear visual hierarchy, varying card spans, and consistent inner padding (`16.dp` to `24.dp`).
2. **Decoupled Gesture Tracks (`ExpressiveSwipeRow`):** For swipeable or dismissible rows (`SwipeToDismissBox` / `ExpressiveSwipeRow`):
   - Keep the outer sliding track background neutral and adaptive (`surfaceVariant` or `surfaceContainer`).
   - Encapsulate action icons inside standalone circular/pill-shaped bubble containers (`CircleShape`) rather than stretching full-width colored boxes.
   - Enforce a 25% activation threshold (`positionalThreshold = { totalDistance -> totalDistance * 0.25f }`) paired with `VibeHaptics.heavyLongPress`.
3. **Modal Bottom Sheets & Inset Ergonomics (`AppModalBottomSheet`):**
   - Use large corner radiuses (`RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`) with smooth spring entry/exit transitions.
   - Handle system insets properly: use `WindowInsets.ime` and `WindowInsets.navigationBars` to ensure action buttons sit comfortably above the keyboard without double-padding or clipping.
4. **Connected Button Groups (`ExpressiveConnectedButtonGroup`):**
   - When placing connected segment switchers (e.g. Period Switcher, Habit/Month Switcher, Transaction Type Switcher):
     - **Leading Item:** Large outer corners (`20.dp` ~ `24.dp`), small inner corners (`4.dp` ~ `6.dp`).
     - **Middle Items:** Small uniform inner corners (`4.dp` ~ `6.dp`).
     - **Trailing Item:** Small inner corners (`4.dp` ~ `6.dp`), large outer corners (`20.dp` ~ `24.dp`).
     - **Connected Spacing:** Use `ButtonGroupDefaults.ConnectedSpaceBetween` (0dp or 2dp divider).
     - **Selection State:** Highlight selected item with `colorScheme.primaryContainer` (or `primary`), accompanied by an animated checkmark icon (`Icons.Default.Check`) that smoothly expands horizontally (`expandHorizontally() + fadeIn()`).
5. **Translucent Glass & Scroll Blur Container (`ScrollBlurContainer`):**
   - Top App Bars and Bottom Navigation Bars floating over scrollable feeds should utilize dynamic backdrop blur and subtle alpha tinting rather than harsh solid borders.
6. **Specular Rim Lighting & Shader Caching for Glass Cards (Morphe Pattern):**
   - For rich cards with colorful or branded gradients, simulate specular rim lighting using an asymmetric gradient border (`White.copy(alpha = 0.65f)` at top-start down to `0.15f~0.20f` at bottom-end).
   - Encapsulate brush creation and drawing within `Modifier.drawWithCache` to avoid reallocations on scroll.
   - Protect text legibility on gradient surfaces with subtle drop shadows (`Shadow(Color.Black.copy(0.4f), Offset(0f, 2f), blurRadius = 4f)`).
7. **Alpha Masking for Edge Fade (`BlendMode.DstIn`):**
   - When fading out scrollable list or row edges, use `BlendMode.DstIn` on an offscreen compositing layer (`CompositingStrategy.Offscreen`) rather than overlaying a solid-colored gradient. This ensures seamless fading across dynamic themes, pure-black modes, and glass backgrounds.
8. **Inline Action Confirmations (Pill Confirmation over Toasts):**
   - Prefer inline pill spring expansion and crossfade label swaps over disruptive floating Toasts/Snackbars for contextual actions (e.g., Copy, Save, Reset).
9. **Ergonomic Reading Constraint Limits:**
   - Enforce a content max-width container (`560.dp`) for primary feeds and cards to ensure optimal scanning comfort on tablets and unfolded foldables.

---

### B. Material You Dynamic Token Mapping (M3 Expressive Surface Hierarchy)
Never use hardcoded hex values (e.g., `Color(0xFF...)`) for primary UI surfaces. Map all components to dynamic semantic tokens and Material 3 Expressive container elevations:

| Surface / Component Role | Semantic Token | Recommended Usage in VibeFinance |
| :--- | :--- | :--- |
| **Activity / Canvas Base** | `MaterialTheme.colorScheme.surface` or `surfaceContainerLowest` | Background of the entire scaffold or screen canvas |
| **Primary Dashboard Cards** | `MaterialTheme.colorScheme.surfaceContainer` | Bento grid cards, budget cards, transaction history rows |
| **Elevated Sections / Sliders** | `MaterialTheme.colorScheme.surfaceContainerHigh` | Search bars, slider tracks, filter pill trays |
| **Modal Sheets & Dialogs** | `MaterialTheme.colorScheme.surfaceContainerHighest` | Modal bottom sheets, popup menus, confirmation dialogs |
| **Primary Action Container** | `MaterialTheme.colorScheme.primaryContainer` | Key buttons, active selection pills, hero card fills |
| **Primary Action Content** | `MaterialTheme.colorScheme.onPrimaryContainer` | Text and icons placed on primaryContainer |
| **High-Emphasis Action** | `MaterialTheme.colorScheme.primary` / `onPrimary` | Primary submission button (e.g. Save Expense, Confirm) |
| **Complementary Flow (Tertiary)**| `MaterialTheme.colorScheme.tertiary` / `tertiaryContainer` | Cross-account transfer arrows, liquid asset growth tags |
| **Destructive Action** | `MaterialTheme.colorScheme.errorContainer` / `onErrorContainer` | Delete actions, overdue alerts, deficit warnings |
| **Subtle Divider / Outline** | `MaterialTheme.colorScheme.outlineVariant` | Subtle card borders, connected button dividers |

> [!IMPORTANT]
> **AMOLED / Pure Black Theme Guardrail:** When pure black theme is active, maintain container contrast using `outlineVariant` hairline borders (0.5dp ~ 1dp) so that `surfaceContainer` cards do not disappear into the black background.

---

### C. Responsive Layout & Overflow Guardrails
1. **Compact Screen Squeeze Protection (< 380dp Screen Width):**
   - When placing a card header title next to action controls (such as connected button groups or icon buttons) in a horizontal `Row`:
     - Apply `Modifier.weight(1f, fill = false)` to the title to prevent it from pushing action buttons off-screen.
     - Never truncate essential card headers. If space is insufficient, allow the button group to collapse into compact icons or stack vertically.
2. **Preventing Chip & Preset Boundary Clipping:**
   - Never place dynamic lists of chips, filter tags, or popular presets inside fixed-width containers or unconstrained horizontal rows without scrolling or wrapping.
   - Use `FlowRow` for wrapped presets or `LazyRow` for horizontally scrollable lists with explicit content padding (`PaddingValues(horizontal = 16.dp)`).
   - For horizontally scrollable rows, apply dynamic edge-fade gradients via `Modifier.drawWithContent` with a horizontal fade gradient mask (`BlendMode.DstIn`).
3. **Large Display Typography & Amount Formatting:**
   - Display amounts prominently (e.g. `32sp` ~ `42sp` bold font).
   - Place the currency prefix (e.g. `HK$ ` in `primary` theme color, `24sp` ~ `32sp` bold) directly alongside the amount with a clear horizontal baseline alignment.
   - Use tabular numbers or fixed-width digits where possible to prevent container width jitter as amounts update.
4. **Dynamic Font Scaling Protection:**
   - Test layouts against large system fonts (`fontScale >= 1.3f`). Chip rows and badge labels must never vertically clip text or overlap neighboring rows.
5. **Adaptive Breakpoints (`WindowWidthSizeClass`):**
   - **Compact (< 600dp):** Single-column Bento matrix, Bottom Navigation Bar, Modal Bottom Sheet.
   - **Medium (600dp - 840dp):** 2-column Bento grid, Navigation Rail, expandable side sheets.
   - **Expanded (> 840dp):** 3-column / asymmetric multi-pane layouts, persistent side navigation drawer, two-pane master-detail view.

---

## 3. Motion & Animation Standards

### A. Spring Kinetics: The "Bouncy vs. Non-Bouncy" Navigation Rule
Always choose spring parameters based on whether the interaction is **micro-interactive** or **macro-navigational**:

| Motion Scope | Recommended Spring Configuration | Rationale |
| :--- | :--- | :--- |
| **Touch / Button Press** | `dampingRatio = Spring.DampingRatioMediumBouncy`<br/>`stiffness = Spring.StiffnessMedium` | Snappy tactile pop (`0.97f` scale) with organic physical bounce |
| **Digit Counter Roll** | `dampingRatio = Spring.DampingRatioMediumBouncy`<br/>`stiffness = Spring.StiffnessMediumLow` | Independent per-digit playful rolling motion |
| **View / Tab Switching** | `dampingRatio = Spring.DampingRatioNoBouncy`<br/>`stiffness = Spring.StiffnessMediumLow` | **Strictly no bouncy oscillation.** Eliminates visual vertigo when switching screens or calendar views |
| **Pager Transitions** | `dampingRatio = Spring.DampingRatioNoBouncy`<br/>`stiffness = Spring.StiffnessMedium` | Smooth linear deceleration for horizontal page swipes |
| **Color & Alpha Fade** | `dampingRatio = Spring.DampingRatioNoBouncy`<br/>`stiffness = Spring.StiffnessLow` | Pure fade transitions must never oscillate past 1.0 or 0.0 |
| **Ambient Liquid Waves** | `stiffness = Spring.StiffnessLow`<br/>`duration = 4000ms - 5000ms` | Calm, non-intrusive organic fluid movement |

### B. GPU Layer Optimization (`graphicsLayer`)
To ensure seamless 120Hz frame rates, isolate high-frequency gesture transformations (scaling, fading, translation) inside `Modifier.graphicsLayer` to skip unnecessary layout remeasure passes:

```kotlin
Modifier.graphicsLayer {
    scaleX = animatedScale
    scaleY = animatedScale
    alpha = animatedAlpha
}
```

### C. Spring Motion & Animation Invariants
1. **Modifier Chaining Order on Sized Layers:**
   - When animating progress or shape dimensions with spring physics, never place `.background(...)` before dimension constraints (`fillMaxWidth`, `fillMaxHeight`).
   - Calling `.matchParentSize().background(...).fillMaxWidth(fraction)` draws across 100% of the parent bounds because `.background()` executes before `.fillMaxWidth()`.
   - Instead, wrap the animated layer inside an inner Box:
     ```kotlin
     Box(modifier = Modifier.matchParentSize()) {
         Box(
             modifier = Modifier
                 .fillMaxHeight()
                 .fillMaxWidth(animatedFraction.coerceIn(0.001f, 1f))
                 .background(color, shape = WavyShape(...))
         )
     }
     ```
2. **Discrete Target Invariant for Digit Transitions:**
   - Never feed high-frequency continuous animated float values into `RollingNumberText` or digit-level `AnimatedContent`. Frame-by-frame updates (60-120 fps) interrupt re-entrant digit transitions, causing flickering or frozen digits.
   - Pass the discrete target value (e.g., `String.format(Locale.US, "HK$ %,.0f", targetAmount)`) and let each digit spring-animate independently.
3. **Zero Idle CPU Lifecycle Guardrail:**
   - For all custom Canvas drawing loops (`LaunchedEffect` with frame tickers):
     ```kotlin
     LaunchedEffect(hasActiveElements) {
         if (!hasActiveElements) return@LaunchedEffect // Sleeps immediately
         while (isActive && hasActiveElements) {
             withFrameMillis { now -> ... }
         }
     }
     ```
   - Automatically purge expired elements (ripples, dust, fishes) so the coroutine terminates and leaves CPU usage at 0% when idle.

---

## 4. Tactile & Haptic Integration (VibeHaptics Engine)

> [!CRITICAL]
> **Universal `VibeHaptics` Mandate:**
> Never invoke `LocalHapticFeedback.current.performHapticFeedback(...)` or `view.performHapticFeedback(...)` directly in UI components. Direct platform calls completely bypass the user's configured strength setting (`HapticStrength.GENTLE` / `STANDARD` / `STRONG` / `OFF`).
> Always use `rememberVibeHapticFeedback()` or call the semantic methods in `com.example.vibefinance.util.VibeHaptics`.

### A. Semantic Haptic Mapping Table
| Action / Gesture | Recommended Call | Description & Physical Feel |
| :--- | :--- | :--- |
| **Button Tap / Pill Selection** | `VibeHaptics.lightTick(view)` or `selection(view)` | Clean, sharp, micro-tick confirmation |
| **Switch / Segment Toggle** | `VibeHaptics.toggle(view)` | Distinct on/off click sensation |
| **Swipe Activation Threshold** | `VibeHaptics.heavyLongPress(view)` | Deep tactile "notch" reached at 25% drag distance |
| **Save / Success / Boost** | `VibeHaptics.successConfirm(view)` | Ascending two-step confirmation pulse |
| **Warning / Insufficient Balance** | `VibeHaptics.warningAlert(view)` | Two rapid cautionary alert taps |

### B. Tactile Button Press (`ExpressivePressedShape`)
- Standardize interactive click targets with `.bouncyClickable` or `pressScale(pressedScale = 0.97f)`.
- Use `ExpressivePressedShape` to simultaneously inset bounds and increase corner curvature on press:
  ```kotlin
  val scale by animateFloatAsState(
      targetValue = if (isPressed) 0.97f else 1f,
      animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium
      ),
      label = "pressScale"
  )
  ```
- Accompany click events with `VibeHaptics.lightTick(view)` for authentic physical key response.

---

## 5. Defensive Testing, Accessibility & Platform Invariants

1. **Semantic Test Tags:** Always inject explicit `testTag` modifiers into interactive nodes (e.g., `Modifier.testTag("history_item_${item.id}")`) to ensure UI test robustness without relying on brittle localized strings.
2. **Accessible Touch Targets:** Maintain minimum accessible touch targets of at least `48.dp` (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`) for all clickable chips, icon buttons, and switches.
3. **TalkBack & Content Descriptions:** Every interactive control (Icon, IconButton, FloatingActionButton, clickable Card) must provide a localized `contentDescription` via string resources. Use `contentDescription = null` exclusively for purely decorative accents.
4. **Color Role Pairing & Contrast:** Every foreground element must use the corresponding `on` color role for its background (`onPrimary` on `primary`, `onPrimaryContainer` on `primaryContainer`, `onSurface` on `surface`, `onErrorContainer` on `errorContainer`) to guarantee WCAG AA-compliant contrast (minimum 4.5:1 for body text, 3:1 for large display metrics).
5. **Predictive Back & Navigation:**
   - In Compose apps, use `BackHandler` (from `androidx.activity.compose`) for back navigation and back-stack handling.
   - Do not suppress the system back animation.
   - Never intercept back with confirmation dialogs unless there is unsaved form data.
   - Follow navigation component guidelines: Navigation Bar for 3-5 destinations on compact screens (< 600dp), Navigation Rail for medium/expanded screens (>= 600dp).
6. **Honoring Reduced Motion (`LocalExperience`):**
   - Components with complex entrance transitions (e.g. `ContentEntrance`) must check:
     ```kotlin
     val reducedMotion = LocalExperience.current.motion != MotionLevel.FULL
     ```
   - When `reducedMotion` is true, immediately render content without delays, translation offsets, or bouncy scaling.
