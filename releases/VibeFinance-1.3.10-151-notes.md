# VibeFinance 1.3.10 (151) — Authorized phone release

Date: 2026-10-29 (Asia/Hong_Kong)

## Included changes

- Original-expense calendar-day and text search filters for Add Income and History Money back editing; selecting a day filters expenses without changing receipt dates.
- Search-result fade/placement animation, stable viewport, saved filters and native calendar cancellation/keyboard handling.
- Shared overflow-aware top/bottom fade and progressive blur for repayment search and the account-selection sheet, respecting global blur/motion preferences and older-Android fallback.
- Includes the preceding adaptive amount typography and readable Settings app-summary fixes.

## Verification and installation

- User explicitly authorized release creation and installation on their phone.
- `./gradlew assembleRelease testDebugUnitTest` passed. Existing 426 unit results remain green (unit task up to date); the preceding debug update passed 16 Waydroid interaction regressions.
- R8 minification/resource shrinking enabled; release version metadata and signature verified. No source change or additional version increment for packaging.
- SHA-256: `2f7955884e46d94ae219b1681e8e36b73fa0180b5aaae27d5d0e8f1be6db1076`.
- Installed with `adb -s 10.9.46.50:38229 install -r` on Samsung SM-S9280; installer returned Success. Same certificate as the installed debug APK, retaining saved data without uninstalling/clearing storage.
- Package reports versionName 1.3.10 / versionCode 151, with DEBUGGABLE absent.
- Launch command returned Status ok and delivered the intent to the already-running top activity. This was not a measured cold launch.
- No financial/settings/device-scale mutations, publication/upload, commit or push.

## Limits

This release deployment checked build/signature/install/package flags and launch handling. The full UI regression suite was not rerun on the physical phone, nor was every saved record compared. Prior unit/Waydroid coverage is documented in PROGRESS.md.
