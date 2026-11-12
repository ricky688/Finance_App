# VibeFinance 1.3.11 (152) — Phone release

Date: 2026-10-29 (Asia/Hong_Kong)

## Fixes

- Repayment expense eligibility and durable validation compare local calendar dates rather than precise times. A posted expense later on the repayment date can be linked without rewriting the receipt ID/time. Future sources and excessive repayments remain blocked at write boundaries.
- Original-expense selection uses a floating bottom-end Close action instead of a footer row, with safe bottom spacing so the last result remains reachable.

## Build and deployment

- User explicitly authorized the release build and phone installation. No second version increment or source change for packaging.
- `./gradlew assembleRelease testDebugUnitTest` passed; existing 429 unit results remain green (unit task up to date). Previous debug verification passed 18 Waydroid memory-only interaction checks.
- R8 minification/resource shrinking enabled. Metadata and matching signature verified before installation.
- APK SHA-256: `0b1cf725f83de25693e52c438c7cbde2d06d0763f9a306ad7b2e542b8d6b6b8c`.
- `adb install -r` succeeded on Samsung SM-S9280 (`10.9.69.73:43885`), updating 1.3.10/151 release to 1.3.11/152 release without uninstalling or clearing storage.
- Installed version and absence of DEBUGGABLE confirmed. Native cold launch returned Status ok, 483ms observed activity time (not a performance benchmark).
- No public publication/upload, commit, push, device-scale change or manual finance/settings mutation.

## Limits

No full physical-phone UI regression or record-by-record data comparison was performed for deployment. Calendar-day interpretation follows the current device time zone; cross-time-zone restore behavior was not separately verified. Android 24–30 appearance remains untested on hardware.
