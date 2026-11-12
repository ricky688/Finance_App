# VibeFinance 1.3.5 (146) — Release verification

Date: 2026-10-29 (Asia/Hong_Kong)

## Artifact

- Signed APK: `VibeFinance-1.3.5-146-release.apk` (R8 minification and resource shrinking enabled).
- Same version as the completed debug update; no additional behavior change or version increment.
- APK signature verified and matched the previously installed debug build. Installed in place on Waydroid `192.168.240.112:5555` without uninstalling or clearing data. Package flags confirm a non-debuggable installation.
- `./gradlew assembleRelease testDebugUnitTest` passed. The existing 421-unit-test results remain green (zero failures/errors/skips); the unit task was up to date. This project exposes no `testReleaseUnitTest` task.
- Not published or uploaded. APK SHA-256: `462462e39c0f15c6c4c7cf859fff4c94f5d90aaa7cf7689d9368c3891f50f857`.

## Observed release smoke checks

ARTEMIS screenshots/hierarchy plus direct ADB actions verified the actual installed release:

- Cold launch and relaunch complete successfully (229ms initial launch; 586ms final relaunch, observed ADB timings only).
- Daily, Assets, Recurring and History render. Assets scrolls, collapses its toolbar and restores bottom navigation on upward return.
- Assets → Add Account opens the full-screen editor with preview/basic fields; closed without saving.
- Recurring → Add Subscription opens with Daily/Weekly/Monthly/Yearly and Repeat every (1–999) controls; closed without saving.
- Add Expense opens with the native numeric keyboard. Input `12.34` survives switching Expense → Income → Transfer → Income, with keyboard retained until explicitly dismissed.
- Income → Money back toggles on and shows the original-expense selector and correct empty-state message. Save repayment remains disabled without an eligible expense/account. Draft discarded by closing; no transaction saved.
- Settings drawer and Color scheme sheet open/render/dismiss; existing setting values left unchanged.
- Final force-stop/relaunch returns to Daily. No app crash or ANR found in captured logs; only scrcpy recording-tool audio/opus encoder crashes appeared in the crash buffer.

## Limits

Waydroid has zero accounts and transactions, so release testing could not exercise editing an existing linked repayment or saving against the imported ledger. Those paths have the preceding debug memory-only regression coverage (23 UI passes, one imported-device-data skip), plus durable-edit unit coverage; they are not claimed as release save tests. No physical-phone, cloud-provider or performance benchmark verification in this build.

ARTEMIS Pro and Flash autonomous jobs made no action progress and were stopped after diagnostics; their reports are not counted as passed tests. Direct ARTEMIS observations and ADB actions supplied the smoke results above. No persistent finance/settings mutations, device-scale changes, source-code edits, commit or push were performed for this release build.
