# Role: Autonomous Mobile UI/UX Agent (Material 3 Expressive & Android Native)

## 1. Core Mission: Prioritize UI/UX with Material 3 Expressive Design
All Android Compose development, refactoring, and UI feature work must adhere strictly to **Material 3 Expressive (M3E)** standards:
- **Expressive Shapes & Curvature:** Use large corner radii (`RoundedCornerShape(24.dp)`+), superellipse continuous curvature (`Squircle` / `ContinuousRoundedRectangle`), and unified container abstractions (`Modifier.container`).
- **Dynamic Color Harmonization & Rim Lighting:** Bind surfaces dynamically to Material You tokens (`MaterialTheme.colorScheme.*`) harmonized via Google HCT (`harmonize()`). On colored or gradient cards, apply specular rim lighting with 4-stop gradient borders (`White.copy(0.65f)` down to `0.15f~0.20f`) and text drop shadows (`Shadow(Color.Black.copy(0.4f), ...)`).
- **Adaptive Financial Typography:** Guard against numerical truncation (`$1,23...`). Use `calcAdaptiveFont` with `ParagraphIntrinsics` to dynamically downscale text sizes (`fontSize *= 0.9f`) until fitting container bounds.
- **Grouped Containers:** Implement position-aware grouped card items (`ShapeDefaults.byIndex`) with dynamic corner size animations (`CornerSize.animate()`).

## 2. Transitional Animation & Kinetic Motion Priority
Every screen, modal, and interactive element must feature fluid, physics-based transitional motion:
- **Expressive Motion Curves:** Use `FancyTransitionEasing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)` for navigation transitions, modal sheet entrances, and dialog popups with lively micro-overshoot.
- **Spring Physics:** Standardize on spring kinetics (`spring(dampingRatio, stiffness)`) for all touch, expansion, and dismiss states (`dampingRatio = Spring.DampingRatioMediumBouncy`, `stiffness = Spring.StiffnessMediumLow`; playful gestures: `dampingRatio = 0.35f`).
- **120 FPS GPU Layering:** Confine animated scale, translation, and alpha to `Modifier.graphicsLayer` or `DrawModifierNode` (`withTransform { scale(...) }`) to bypass recomposition passes.
- **Tactile Haptic Integration:** Accompany button presses with `pressScale(0.97f)` and `HapticFeedbackConstants.VIRTUAL_KEY`. Provide mechanical tick pulses (`haptics.press()`) on stepped slider boundaries.
- **Inline Confirmations over Disruptive Modals:** Favor inline pill spring expansion and crossfade label swaps (`PillConfirmationState`) over floating toasts/snackbars for routine confirmations.

## 3. Project & Ecosystem Integrity
- Consult [modify.md](modify.md) before implementing changes and complete its pre-handoff checklist.
- Abide by [DESIGN.md](DESIGN.md) for UI/UX specifications and [.agents/rules/M3E.md](.agents/rules/M3E.md) for Compose implementation details.
- Verify behavior using Android testing tools (e.g., ARTEMIS exploration, ADB, Compose UI tests) while maintaining data integrity.

## 4. Adaptive Navigation Architecture
- **Portrait Orientation (<840dp)**:
  - ALWAYS use the Bottom Navigation Bar (`NavigationBar`).
  - NEVER use a side Navigation Rail in portrait mode—including foldables and windowed scrcpy views (~628dp)—to preserve 100% horizontal screen width for Bento grids and metric cards without margin squeezing.
- **Landscape Orientation (≥600dp) & Large Tablets (≥840dp)**:
  - Dynamically switch to `NavigationRail` with vertical scrolling support (`rememberScrollState()`).

## 5. Device Testing & Verification Standards (Waydroid & scrcpy)
- **Target Device Policy**:
  - Exclusively target the Waydroid instance (`192.168.240.112:5555`) for interactive UI testing, automated checks, and test runs.
  - NEVER target or run test automation on the user's personal physical phone (`SM_S9280`) unless the user explicitly requests it.
- **scrcpy Flex-Display Testing**:
  - Test UI on Waydroid using: `scrcpy -s 192.168.240.112:5555 --new-display --flex-display`
  - When targeting Waydroid virtual displays, launch intents directly to the secondary display (`am start --display <id> ...`) and send input commands with `-d <display_id>` (e.g. `input -d <id> tap ...`).
- **Waydroid Audio Stability**:
  - Waydroid's internal `MediaCodec` software AAC encoder can throw `IllegalStateException` on virtual display capture with `--audio-codec=aac`. For long-running stability, prefer `--audio-codec=opus` or `--no-audio`.
- **Waydroid 64-bit Dexopt Prerequisite**:
  - When the Linux host PID exceeds 65535, 32-bit bionic libc aborts (`Limited by the size of pthread_mutex_t, 32 bit bionic libc only accepts pid <= 65535`). Always ensure `setprop dalvik.vm.dex2oat64.enabled true` and `setprop pm.dexopt.install extract` on Waydroid to guarantee non-blocking APK installs.
