# Project Directives & UI/UX Standards

## 1. Prime Directive: Prioritize UI/UX with Material 3 Expressive Design
Every modification, new screen, component refactoring, or feature addition MUST prioritize world-class **UI/UX Aesthetics** following **Material 3 Expressive (M3E)** principles:
- **Expressive Geometry & Surfaces:** Use large organic rounded corners (`RoundedCornerShape(24.dp)`+), superellipse continuous curvature (`Squircle` / `ContinuousRoundedRectangle`), and the universal container pattern (`Modifier.container`).
- **Dynamic Color Harmonization & Rim Lighting:** Leverage Material You dynamic color tokens harmonized via Google HCT (`harmonize()`). On colored/gradient cards, apply specular rim lighting (asymmetric 4-stop gradient borders) and soft drop shadows on text to guarantee contrast.
- **Adaptive Financial Typography:** Never truncate monetary values or critical metrics with ellipses (`$1,23...`). Use intrinsic paragraph measurement to dynamically scale text sizes down (`calcAdaptiveFont`) so numbers fit cleanly across all display sizes.
- **Visual Grouping & Hierarchy:** Use Bento grid structures and grouped inset lists (`ShapeDefaults.byIndex`) with dynamic corner size animation.

## 2. Transitional Animation & Kinetic Motion Priority
Static, sudden, or linear UI transitions are strictly forbidden. All interactive states and navigations MUST incorporate fluid transitional motion:
- **Expressive Overshoot Curves:** Use `FancyTransitionEasing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)` for navigation pushes, sheet entrances, and content transforms to provide a lively micro-overshoot feel.
- **Physics-Driven Spring Kinetics (official M3E tokens, 2026-11-20):** use `VibeMotion` (`theme/Motion.kt`) or `MaterialTheme.motionScheme`, whose values are the `MotionScheme.expressive()` springs from material3 1.5.0. Do not hand-tune `spring(dampingRatio, stiffness)` for new UI.
  - Spatial (position, size, shape morph; slight overshoot): `VibeMotion.fastSpatial()` for small controls (0.6/800), `defaultSpatial()` for most UI (0.8/380), `slowSpatial()` for full-screen and large containers (0.8/200).
  - Effects (colour, opacity; no bounce): `fastEffects()` (1.0/3800), `defaultEffects()` (1.0/1600), `slowEffects()` (1.0/800).
  - With the minimal-motion setting, spatial specs use the standard non-bouncy values automatically.
- **Container transforms & predictive back:** pages that open from an element use `LocalSharedTransitionScope` (provided by `ExpressiveSharedTransitionRoot` in `AdaptiveMainFrame`). Full-screen pages use `rememberPredictiveBackPage` + `Modifier.predictiveBackPage` (scale to 90%, 8dp drift, 28dp corners) instead of a plain `BackHandler`.
- **⚠️ IMPORTANT — Shared Element Transitions (user requirement, 2026-11-20):** whenever one state or page changes into another that shows the **same element** (detail ↔ edit, list item ↔ detail, card ↔ editor), that element must **stay or move/reshape to its new position**. It must never be removed and re-created, and nothing may pop in.
  - Use Compose shared element transitions: `SharedTransitionLayout` together with `Modifier.sharedTransitionBounds(key)` (looks different on each side, e.g. text → input field; content is scaled down only, start-aligned, with a slow cross-fade) or `Modifier.sharedTransitionElement(key)` (identical content), both in `ui/motion/ExpressiveTransitions.kt`. Provide `LocalSharedContentScope` and `LocalIsSharedTargetPage` per `AnimatedContent` page.
  - A dialog is a separate window, so it needs its own `SharedTransitionLayout` and its own `LocalSharedTransitionScope`. See `MorphingTransactionDialog`.
  - Heavy views (maps) move as one instance through movable content (`SharedMapHost` / `SharedTransactionMap`), so they never reload mid-transition.
  - Elements that exist on one side only slide a short distance and fade on `VibeMotion` springs; bounds use `VibeMotion.slowSpatial()`.
  - Keep both layouts' element order unless the user asks otherwise. Size the destination so the shared elements are visible without scrolling (compact spacing on phones). If content still overflows, wrap the scroll in `ScrollBlurContainer` so the overflowing edge fades.
- **Zero-Recomposition GPU Layering:** Execute high-frequency scaling, dragging, and fading inside `Modifier.graphicsLayer` or `DrawModifierNode` (`withTransform { scale(...) }`) to maintain 120 FPS without triggering recomposition.
- **Micro-Interactions & Multi-Sensory Tactile Feedback:**
  - Standardize button tap downscaling (`pressScale(0.97f)`) paired with `HapticFeedbackConstants.VIRTUAL_KEY`.
  - Stepped scrubbers and sliders must provide mechanical notch feedback (`haptics.press()`).
  - Prefer inline pill spring expansion confirmation (`PillConfirmationState`) over disruptive floating Toasts.
- **Seamless Edge Transitions:** Apply non-destructive alpha edge fades (`BlendMode.DstIn` on `CompositingStrategy.Offscreen`) on scrolling lists.

## 3. Workflow & Verification Rules
- Before changing this app, read [modify.md](modify.md) and follow its update checklist.
- Consult [DESIGN.md](DESIGN.md) for the design system specification and [.agents/rules/M3E.md](.agents/rules/M3E.md) for Compose implementation details.
- Maintain established dataset integrity and device compatibility.
- The user's instructions and established preferences take precedence.

## 4. Adaptive Navigation Architecture
- Follow Google's official navigation suite rule (`NavigationSuiteScaffoldDefaults.navigationSuiteType`), computed from the frame size in `AdaptiveMainFrame` (user decision, 2026-11-19):
  - **Compact width (<600dp), compact height (<480dp, e.g. phone landscape) or tabletop posture**: the bottom `NavigationBar` (haze glass, staggered pop, scroll-hide). Keep `NavigationBar` until `ShortNavigationBar` is fixed: in material3 1.5.0-alpha01 it lays out only its first item.
  - **Otherwise (>=600dp width with >=480dp height, including portrait foldables and tablets)**: collapsed 96dp rail using `WideNavigationRailItem` in three zones: logo and FAB on top, destinations vertically centred, utility dock at the bottom. The rail controls and the content card are inset for `WindowInsets.safeDrawing` (the rail background stays edge-to-edge), and short windows fall back to vertical scrolling (`rememberScrollState()`).
- Read the active type from `LocalNavigationSuiteType`; do not add separate orientation-based navigation switches.

## 5. Device Testing & Verification Standards (Waydroid & scrcpy)
- **Target Device Policy**:
  - **Authorized personal-phone update (2026-11-08):** the user explicitly requested ADB updating of VibeFinance on SM-S9280 / R5CX22YGH7A from 1.3.44 to the prepared 1.4.2 (192), with retained data, followed by sharing-option/companion verification. This exception is limited to that app and task; no uninstall, data clearing, financial edits or unrelated personal-phone automation.
  - **Authorized personal watch (2026-11-08):** SM-L310, hardware serial RFGYB4298FN, Android 16 / API 36. Use its explicitly confirmed ADB endpoint (currently 192.168.0.57:33443) for Wear-specific checks; re-identify the same hardware if wireless debugging changes its port. Personal-phone operations require their own explicit authorization. Preserve data and system font/density/resolution.
  - **Wear OS companion exception (user-approved):** use dedicated Wear OS 7 / API 37 and Google Play Android 17 pairing emulators with fixtures for Wear-specific checks. Existing phone regressions continue to use Waydroid. This does not authorize personal phone/watch automation or changing system font, density or resolution; size/font cases remain app-scoped.
  - Exclusively target the Waydroid instance (`192.168.240.112:5555`) for interactive UI testing, automated checks, and test runs.
  - NEVER target or run test automation on the user's personal physical phone (`SM_S9280`) unless the user explicitly requests it.
- **scrcpy Flex-Display Testing**:
  - Test UI on Waydroid using: `scrcpy -s 192.168.240.112:5555 --new-display --flex-display`
  - When targeting Waydroid virtual displays, launch intents directly to the secondary display (`am start --display <id> ...`) and send input commands with `-d <display_id>` (e.g. `input -d <id> tap ...`).
- **Waydroid Audio Stability**:
  - Waydroid's internal `MediaCodec` software AAC encoder can throw `IllegalStateException` on virtual display capture with `--audio-codec=aac`. For long-running stability, prefer `--audio-codec=opus` or `--no-audio`.
- **Waydroid 64-bit Dexopt Prerequisite**:
  - When the Linux host PID exceeds 65535, 32-bit bionic libc aborts (`Limited by the size of pthread_mutex_t, 32 bit bionic libc only accepts pid <= 65535`). Always ensure `setprop dalvik.vm.dex2oat64.enabled true` and `setprop pm.dexopt.install extract` on Waydroid to guarantee non-blocking APK installs.
