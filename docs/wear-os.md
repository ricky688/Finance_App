# VibeFinance Wear OS companion

The companion is a read-only budget app and Tile for **Wear OS on Android 16 (API 36) and later**, including Wear OS 7 / Android 17 (API 37). It shows available-today and remaining-period amounts, days left, and the snapshot's update time. Transaction entry, account management, complications, a watch face and standalone cloud sync are outside this version.

Planned features and phone/watch bank-income confirmation requirements are tracked in [Wear OS 功能待辦](wear-os-todo.md). They are not implemented in the current read-only companion.

## Build and setup

- `:wear` is a separate APK; `:wear-contract` contains the display-only binary contract. The phone's `app/build.gradle.kts` remains the version source. Both APKs use `com.ricky688.vibefinance` and the same debug signing configuration for Data Layer authentication.
- Wear uses minimum SDK 36 and compile/target SDK 37, Wear Compose Material 3 1.7.1, Tiles 1.6.2 and ProtoLayout 1.4.2. The shared build uses AGP 9.1.1 and Gradle 9.3.1. The phone retains min SDK 24 and target/compile SDK 36.
- Pair a dedicated Google Play Android emulator with a Wear OS 7 emulator using Android Studio's Wear pairing assistant. Install the phone APK only on the phone side and the Wear APK only on the watch side. Do not install the watch APK over the phone app: they intentionally have the same application ID.
- On the phone open Settings → Data and privacy → Share budget with Wear OS. It is off by default. Add the Budget Tile from the watch's Tile picker. Tap the Tile to open the app and refresh.
- Use only test accounts/fixtures for emulator pairing. Personal-device automation requires explicit user authorization. The user has authorized the SM-L310 watch, serial RFGYB4298FN (wireless ADB endpoint may change); this does not authorize personal-phone automation.

## Data and privacy contract

The phone directly subscribes to the existing `BudgetRepository`; no budget algorithm is copied to the watch. The watch receives already formatted HKD strings, remaining days, status, calculation date/time zone, revision, motion preference and validity times. Today amounts follow the phone hero's whole-dollar format; period amounts retain two decimals. The watch neither receives transactions/account details nor writes financial data.

`DataClient` carries the latest snapshot at `/vibefinance/budget/v1`. `MessageClient` requests a refresh at `/vibefinance/budget/refresh/v1`. The watch accepts one phone advertising `vibefinance_budget_phone_v1`, validates the contract and rejects replayed revisions. Unknown or malformed packets are rejected without logging monetary values.

While enabled, phone process observations publish budget/transaction changes and a 15-minute WorkManager job refreshes snapshots in the background. A refresh request also schedules phone work without requiring an open Activity. Each refresh resubscribes to the production calculation so crossing midnight does not depend on a transaction being added. Android may delay background work; the app does not promise real-time delivery.

Turning off sharing, hiding phone amounts or enabling App lock publishes a payload without money. Cached amounts expire after at most 15 minutes, at the phone's next midnight, or at the budget period boundary, whichever comes first. The watch checks wall time, elapsed time and boot count. Expiry removes the amount on cache access but retains its replay watermark in private no-backup storage. A valid Tile timeline ends at expiry and falls back to a layout containing no amount, even without a refresh callback. A stopped app cannot physically erase its file at the exact expiration instant; expired bytes are removed on the next access.

An offline watch cannot receive immediate revocation. A previously received snapshot can remain visible until its original expiry, for no more than 15 minutes. System scheduling can also delay Tile redraw requests. Data Layer may relay end-to-end encrypted data through Google servers; this is not a Bluetooth-only feature.

## Layout and verification

The app uses Wear Material 3 scaffolds and a scrollable list. Money occupies its own single line and is measured before shrinking, with a readable 14sp floor. Exceptional long values remain horizontally scrollable in the app. A Tile that cannot fit an amount uses “View amount” instead of clipping or abbreviating it. The Tile accounts for its requested font scale and shows only the daily amount, remaining days and update time; full period details remain in the app. Reduced/Minimal snapshots omit the added list surface transformations; no looping animation or new haptics is added.

Build checks:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest \
  :wear:testDebugUnitTest :wear:assembleDebug :wear:assembleDebugAndroidTest :wear-contract:test
```

Tests call the production contract, presentation mapper, repository and cache. Host cache-policy tests use Robolectric SDK 36 without an app manifest because the installed stable Robolectric does not support API 37; these tests do not establish Wear OS 7 runtime compatibility. Their Java launcher is 21, while shipped Kotlin/JVM code stays at 17.

Wear-only instrumentation uses Espresso 3.8.0-alpha03 and runner 1.8.0-alpha03 because stable Espresso 3.7 still calls `InputManager.getInstance()`, removed in API 37. These dependencies are confined to androidTest and are not shipped in the companion APK.

`BudgetScreenTest` uses the actual Wear Composables and in-memory snapshots to check the refresh callback, expired state, full amount text and measured single-line bounds at normal and app-scoped large fonts. It does not simulate or establish phone/watch pairing.

`WearSharingPreferenceTest` uses the actual phone sharing Composable and in-memory preferences. It covers the native toggle, persistence failure and bounds at 320dp / fontScale 1.5 without changing any device settings.

Before authoring further device interaction tests, explore the actual target screens with ARTEMIS. Phone regressions use Waydroid `192.168.240.112:5555`; Wear OS 7 and dedicated Android 17 pairing emulators, plus the explicitly authorized SM-L310 / RFGYB4298FN watch (API 36), are the user-approved exception. Do not change system text size, density or resolution. Round-screen, large-font, Tile expiry, process restart, offline/reconnect and privacy-revocation checks must use production components and fixtures; unit tests are not full E2E coverage.

### Investment checkpoint

After runtime and pairing checks pass, try the companion for seven days. Continue investment if it is used on at least five days, typically twice daily, the amount is understandable within three seconds and stale-state/battery issues do not interfere. Fewer than three active days is a reason to stop expanding the feature. Consider a complication next; quick expense entry requires a separate write/sync design.

## Verified results — 2026-11-08, v1.4.0 (190)

- Phone unit tests: 571 passed. Wear cache-policy tests: 4 passed. Contract tests: 6 passed. Both modules' debug app and androidTest APK builds succeeded.
- Waydroid production sharing-component tests: 3 passed, plus 5 existing adaptive-navigation tests (8 total). Wear OS 7 / API 37 production Composable tests: 4 passed, including actual text-layout results and bounds at app-scoped large fonts.
- ARTEMIS observed the actual phone privacy entry, Wear app and Budget entry/preview in the Wear OS 7 system Widget catalog. The catalog entry alone does not verify final Tile addition, money rendering or timeline expiry.
- Dedicated Wear OS 7 and Google Play Android 17 AVDs booted using official Android CLI headless mode. The ARTEMIS default launcher requires a graphical display unavailable in this session. New AVD userdata sizes were set before use to 2GB/4GB to fit host storage; their font size, density and resolution were not changed.
- Real Data Layer pairing, offline/reconnect/background delivery, live Tile expiry and physical-watch battery behavior remain unverified. The fresh phone emulator has no official watch companion installed and no completed pairing setup. Complete that setup with a dedicated test account before treating this as a paired-device release candidate; never send credentials through chat.
- D8 9.1.31 emits an API 37 support warning with the documented AGP 9.1.1 toolchain. APK builds and the API 37 UI suite pass, but this warning remains a toolchain limitation to investigate before release. No release artifact was produced.

## API 36 update — 2026-11-08, v1.4.2 (192)

Minimum SDK is now **36**, while compile/target SDK remain **37**. Phone/Wear debug and androidTest builds pass, as do 571 phone, 4 cache and 6 contract unit tests. The API-37-minimum D8 warning recorded above for build 190 did not occur in the new Wear build with minimum SDK 36.

The user explicitly authorized physical-watch testing on SM-L310 / RFGYB4298FN. The API 36 watch successfully installed build 192 and passed all four `BudgetScreenTest` cases in 8.379 seconds with in-memory fixtures. ARTEMIS observed the actual Traditional Chinese app. No font, density, resolution, financial records or personal-phone settings were changed.

For phone sharing, open the full VibeFinance **System settings → Data and privacy** panel. `Share budget with Wear OS` is part of the phone APK as well as the source; installing only the watch APK cannot add this setting to an older phone installation. Current physical phone version and real paired synchronization still need verification. The user reported no sharing option on the phone, so it must not be assumed to have been enabled. Personal-phone inspection/update needs explicit authorization under the project device policy.

### Personal-phone update follow-up

The user subsequently authorized ADB updating of the SM-S9280 / R5CX22YGH7A. Its installed version was verified as 1.3.44 (185), explaining the missing feature. The prepared phone APK was successfully installed with non-streaming replacement installation (`-r`); package metadata now confirms 1.4.2 (192) and the same app data directory. No uninstall, storage clear or financial record editing was performed. No user financial data snapshot was copied to the project.

The sharing string and actual Settings call are present in this phone APK. Open the main screen's settings gear → Data and privacy → Share budget with Wear OS, then refresh the watch. The phone was not left in the VibeFinance settings screen during verification, so actual opt-in and end-to-end synchronization are not claimed as verified. The personal-phone authorization is limited to this update and companion verification.

## 1.4.3 (193): Wear reading hierarchy and optimized release

The ready screen leads with “Available today” and a larger numeral role; negative daily amounts use the semantic error color. The complete period amount and days left use connected secondary surfaces. Money remains single-line with measured shrinking and a 14sp floor, then horizontal scrolling for exceptional values. Native tonal refresh keeps a 48dp-or-larger target, disables while updating, and valid offline snapshots retain their amounts with a quiet cache notice. System dynamic colors are used when available, with a green fallback. Existing expiry, privacy and Reduced/Minimal motion behavior remain unchanged.

The Tile now focuses on the daily amount, days left and freshness. Its existing money-free expiry timeline and tap-to-open behavior remain intact.

Wear release builds enable R8 optimization and resource shrinking with `proguard-android-optimize.txt`, use the existing companion signing identity, and continue to support API 36+ with target 37. Build with `./gradlew :wear:assembleRelease`. The phone's finance, storage and synchronization implementations were not changed for this update. The user reported that paired synchronization worked after the preceding phone update; that report is separate from automated fixture-based checks.

`BudgetScreenTest` additionally measures the actual ready-screen hierarchy and horizontal bounds at app-scoped 220dp/1.0 and 168dp/1.5 configurations, checks the native refresh target, offline amount retention, and suppression of duplicate clicks while refreshing. These are component regressions, not a full paired-device E2E suite.

Final validation for 193: API 37 component regressions passed 8/8. The latest completed physical API 36 run passed 7/8, with the offline-state test losing its Compose hierarchy during Dozing. The final test-only wake configuration was built and validated on API 37; a physical retry was blocked by offline wireless ADB. The signed release was restored and verified on the watch before that failed retry. See the versioned release notes for artifact size, checksum/mapping and limitations.
