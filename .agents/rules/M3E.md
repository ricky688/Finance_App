---
name: material-3-expressive-design
description: Guidelines and architectural rules for implementing Material 3 Expressive Design and Material You dynamic aesthetics in Jetpack Compose.
version: 1.1.0
tags: [android, jetpack-compose, material-3, material-you, motion-design, responsive-design, ui-ux]
---

# Material 3 Expressive & Responsive Design Rules

## 1. Core Design Philosophy
When designing or refactoring UI components, prioritize **Material 3 Expressive** principles:
- **Spatial Depth & Geometry:** Utilize expressive shapes (`CircleShape`, `RoundedCornerShape(24.dp)` or higher) and floating container bubbles rather than flat, full-bleed rectangles.
- **Kinetics & Physics-Based Motion:** Avoid rigid linear animations. Always use spring physics (`spring(dampingRatio, stiffness)`) for touch responses and interactive state transitions.
- **Dynamic Color (Material You):** Decouple layout structure from hardcoded colors. Always bind component surfaces and content tints to semantic tokens (`MaterialTheme.colorScheme.*`).
- **Responsive Adaptability:** Design with flexible, content-aware boundaries that adapt seamlessly across screen sizes and orientations without clipping interactive controls.

---

## 2. Component Construction & Layout Rules

### A. Layouts & Container Scaffolding
1. **Asymmetrical & Bento Grid Hierarchy:** Group analytical and dashboard metrics using Bento Grid structures with clear visual hierarchy, varying card spans, and consistent inner padding (`16.dp` to `24.dp`).
2. **Decoupled Gesture Tracks:** For swipeable or dismissible rows (`SwipeToDismissBox`):
   - Keep the outer sliding track background neutral and adaptive (`surfaceVariant` or `surfaceContainer`).
   - Encapsulate action icons inside standalone circular/pill-shaped bubble containers (`CircleShape`) rather than stretching full-width colored boxes.
3. **Modal Bottom Sheets & Dialogs:** Use large corner radiuses (`RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`) with smooth spring entry/exit transitions.

### B. Material You Dynamic Token Mapping
Never use hardcoded hex values (e.g., `Color(0xFF...)`) for primary UI surfaces. Map all components to dynamic semantic tokens:

| Component Role | Semantic Token |
| :--- | :--- |
| Primary Action / Emphasized Containers | `MaterialTheme.colorScheme.primaryContainer` |
| Primary Action Content / Icons | `MaterialTheme.colorScheme.onPrimaryContainer` |
| Destructive Action Container | `MaterialTheme.colorScheme.errorContainer` |
| Destructive Action Content / Icons | `MaterialTheme.colorScheme.onErrorContainer` |
| Neutral Track / Inactive Surface | `MaterialTheme.colorScheme.surfaceVariant` |
| Subtle Border / Outline | `MaterialTheme.colorScheme.outlineVariant` |

### C. Responsive Layout & Overflow Guardrails
1. **Preventing Chip & Preset Boundary Clipping:**
   - Never place dynamic lists of chips, filter tags, or popular presets inside fixed-width containers or unconstrained horizontal rows without scrolling or wrapping.
   - Use `FlowRow` for wrapped presets or `LazyRow` for horizontally scrollable lists with explicit content padding (`PaddingValues(horizontal = 16.dp)`).
   - For horizontally scrollable rows, apply dynamic edge-fade gradients via `Modifier.drawWithContent` with a horizontal fade gradient mask to visibly hint at offscreen elements.
2. **Adaptive Breakpoints (`WindowWidthSizeClass`):**
   - **Compact (< 600dp):** Single-column Bento matrix, Bottom Navigation Bar, Modal Bottom Sheet.
   - **Medium (600dp - 840dp):** 2-column Bento grid, Navigation Rail, expandable side sheets.
   - **Expanded (> 840dp):** 3-column / asymmetric multi-pane layouts, persistent side navigation drawer, two-pane master-detail view.

---

## 3. Motion & Animation Standards

### A. Spring Kinetics
Always use `spring()` animation specs for scale, translation, and dimension transitions:
- **Touch / Active Feedback:** `dampingRatio = Spring.DampingRatioMediumBouncy`, `stiffness = Spring.StiffnessMediumLow` (produces a subtle, snappy overshoot pop).
- **Spatial Collapse / Dismissal:** Use Material 3 Emphasized Easing (`CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)`) with durations between `300ms` and `400ms`.

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
                 .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                 .background(color, shape = WavyShape(...))
         )
     }
     ```
2. **Discrete Target Invariant for Digit Transitions:**
   - Never feed high-frequency continuous animated float values into `RollingNumberText` or digit-level `AnimatedContent`. Frame-by-frame updates (60-120 fps) interrupt re-entrant digit transitions, causing flickering or frozen digits.
   - Pass the discrete target value (e.g., `String.format(Locale.US, "HK$ %,.0f", targetAmount)`) and let each digit spring-animate independently (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessMediumLow`).
3. **Damping Ratio Standards:**
   - **Color & Alpha:** `Spring.DampingRatioNoBouncy` with `Spring.StiffnessLow`.
   - **Scale, Geometry & Bounds:** `Spring.DampingRatioMediumBouncy` or `Spring.DampingRatioLowBouncy`.
   - **Interactive Touch:** `Spring.StiffnessMediumLow` for immediate haptic-aligned response.
   - **Ambient Liquid / Background:** `Spring.StiffnessLow` for organic fluid motion.

---

## 4. Tactile & Haptic Integration
For gesture-driven actions (such as swipe-to-edit, swipe-to-delete, or button presses):
1. **Positional Threshold:** Set activation thresholds to 25% (`positionalThreshold = { totalDistance -> totalDistance * 0.25f }`).
2. **Haptic Feedback:** Trigger `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)` or `HapticFeedbackType.TextHandleMove` at the exact activation threshold.
3. **Tactile Button Press:** Use `.bouncyClickable` to provide a subtle spring scale (e.g., 0.95x) on press with haptic confirmation.

---

## 5. Defensive Testing, Accessibility & Platform Invariants
1. **Semantic Test Tags:** Always inject explicit `testTag` modifiers into interactive nodes (e.g., `Modifier.testTag("history_item_${item.id}")`) to ensure UI test robustness without relying on brittle localized strings.
2. **Accessible Touch Targets:** Maintain minimum accessible touch targets of at least `48.dp` (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`) for all clickable chips, icon buttons, and switches.
3. **TalkBack & Content Descriptions:** Every interactive control (Icon, IconButton, FloatingActionButton, clickable Card) must provide a localized `contentDescription` via string resources. Use `contentDescription = null` exclusively for purely decorative accents.
4. **Color Role Pairing & Contrast:** Every foreground element must use the corresponding `on` color role for its background (`onPrimary` on `primary`, `onPrimaryContainer` on `primaryContainer`, `onSurface` on `surface`, `onErrorContainer` on `errorContainer`) to guarantee WCAG-compliant contrast.
5. **Predictive Back & Navigation:**
   - In Compose apps, use `BackHandler` (from `androidx.activity.compose`) for back navigation and back-stack handling.
   - Do not suppress the system back animation.
   - Never intercept back with confirmation dialogs unless there is unsaved form data.
   - Follow navigation component guidelines: Navigation Bar for 3-5 destinations on compact screens (< 600dp), Navigation Rail for medium/expanded screens (>= 600dp).

