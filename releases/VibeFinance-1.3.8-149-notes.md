# VibeFinance 1.3.8 (149) — Phone release installation

Date: 2026-10-29 (Asia/Hong_Kong)

## Changes included

- History Income/Money back editing with linked original expenses and repayment limits.
- Larger Buckwheat-inspired Roboto Flex transaction amounts with adaptive width fitting.
- Readable full-width allowed-app title/count above a separate horizontally scrollable app preview row.

## Build and verification

- User explicitly authorized this release build and installation on their phone.
- `./gradlew assembleRelease testDebugUnitTest` passed. Existing 421-unit-test results remain green (zero failures/errors/skips; unit task up to date).
- Signed release APK with R8 minification/resource shrinking enabled; version metadata and APK signature verified.
- SHA-256: `33005156bdaa526a29461536b841d9ccac80033ae12167f5a43519e2e575ced0`.
- Target: the only connected physical phone, Samsung SM-S9280 at `10.9.46.50:38229`; Waydroid was not the installation target.
- Read the existing APK certificate without accessing private finance files. It matches the new release certificate.
- `adb install -r` succeeded, updating installed 1.3.0/141 debug to 1.3.8/149 release without uninstall/clear-storage. Package flags confirm DEBUGGABLE is absent.
- Native cold launch succeeded: Status ok, 512ms activity launch (observed ADB timing, not a benchmark).
- No account/transaction/settings mutations or device-scale changes were performed. No source-code changes or second version increment for packaging.

## Limits

This phone deployment verified installation/version/signature and launch. It did not repeat the full UI regression suite on the phone or compare every stored financial record. Preceding Waydroid memory-only coverage includes 23 typography/mode/guided-entry checks and 10 settings/drawer checks. No public release, store upload, commit or push.
