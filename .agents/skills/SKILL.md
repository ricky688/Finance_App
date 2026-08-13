---
name: material-3
description: Material Design 3 and M3 Expressive UI design guidelines for Android Jetpack Compose and Web.
---

# Material 3 & M3 Expressive Guidelines

When generating UI code or designing components, strictly adhere to Material Design 3 standards:

## 1. Color System & Dynamic Color
* Use `MaterialTheme.colorScheme` tokens (e.g., `primary`, `onPrimary`, `surfaceContainer`, `outlineVariant`).
* Avoid hardcoded hex color values.
* Respect light/dark mode and Dynamic Color (`dynamicLightColorScheme` / `dynamicDarkColorScheme`).

## 2. Expressive Typography & Motion
* Use `MaterialTheme.typography` scale (Display, Headline, Title, Body, Label).
* Implement fluid, physics-based motion with standard cubic-bezier curves for transitions.

## 3. Components & Expressive UI
* Prefer M3 Expressive components: Floating Action Buttons, Split Buttons, Segmented Buttons, and Bottom Sheets.
* Apply correct shape tokens using `MaterialTheme.shapes` (e.g., `extraSmall`, `medium`, `extraLarge`).

## 4. Adaptive Layouts
* Design responsively for Compact, Medium, and Expanded screen sizes using `WindowSizeClass`.
