# VibeFinance Wear 1.4.3 (193) — 2026-11-08

- Emphasizes the complete daily allowance with adaptive large numerals; negative daily values use a warning color.
- Groups the full period amount and days left into connected surfaces; uses native dynamic colors with a green fallback.
- Uses a less dominant native tonal refresh button, disables duplicate requests, and explains valid cached values quietly when offline.
- Simplifies the Tile to today's amount, days and freshness. Existing expiry, privacy, financial calculations, sync protocol and Reduced/Minimal motion behavior are preserved.
- Supports API 36+ and targets API 37. This APK is for watches only; do not install it over the phone application.

## Optimized artifact

`VibeFinance-Wear-1.4.3-193-release.apk`: 2,474,043 bytes (2.36 MiB), compared with 34,933,925 bytes for the same-version debug APK (about 92.9% smaller). R8 code optimization and resource shrinking are enabled with the standard optimized rules. This is a size comparison, not a measured speed or battery improvement. The existing companion signing identity is retained; the release is not debuggable.

The adjacent `.apk.sha256` verifies integrity. The `-mapping.zip` contains the matching R8 diagnostics for crash retracing. No store release, commit or push was performed.

## Validation

Phone `testDebugUnitTest` (571) and Wear cache tests (4) passed; the unchanged contract suite (6) was checked as up-to-date. Phone and Wear `assembleDebug` / `assembleDebugAndroidTest`, Wear `assembleRelease` and release vital lint passed. API 37 real-Composable fixture regressions passed 8/8, including large fonts, narrow bounds, full one-line amounts, semantic negative color, offline state and the real refresh control.

The release was installed with `adb install --no-streaming -r` on the user-authorized SM-L310 / RFGYB4298FN; package metadata confirms 1.4.3 (193), min SDK 36, target SDK 37 and the original private data directory. No account edits, storage clearing or system scaling changes were made. The user previously confirmed paired sync; ARTEMIS also observed the ready budget screen on the physical watch. This does not constitute an automated paired-device E2E suite.

The latest completed API 36 fixture run passed 7/8: the last test lost Compose roots while the watch entered Dozing. A final Activity-only wake configuration compiled and passed API 37 (8/8, 11.944 seconds), but its physical rerun was blocked by offline wireless ADB. The final signed release had already been successfully restored; the later failed hardware precheck made no installation changes. Do not interpret this as a complete API 36 pass. Detailed results are in PROGRESS.md. Tile rendering on the watch and quantitative frame-time / battery profiling were not verified. The personal phone was not updated again for this Wear-only UI change.
