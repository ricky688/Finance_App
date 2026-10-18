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
- **Physics-Driven Spring Kinetics:** Standardize on spring physics (`spring(dampingRatio, stiffness)`):
  - Touch/Active: `dampingRatio = Spring.DampingRatioMediumBouncy`, `stiffness = Spring.StiffnessMediumLow`.
  - Playful component gestures: `springySpec(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)`.
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
