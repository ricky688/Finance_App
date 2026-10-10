# App update checklist

Read this file before making changes to VibeFinance. Complete the applicable items before reporting an app update as finished.

## Version numbers

- For each delivered update that changes app behavior, UI, persisted data, or shipped resources, increment `versionCode` in `app/build.gradle.kts` and advance `versionName`. Use the next patch version for routine fixes; choose a minor or major version when the scope warrants it.
- Read the current values from the working checkout. Never assume a version from an earlier chat, and account for updates already made by another agent.
- Bump once per delivered update, not once per file edit, build attempt, test run, or device installation. Documentation-only or test-only changes do not require an app version bump.
- Keep `app/build.gradle.kts` as the source of version metadata. Display the installed package version in the Settings footer; never hardcode a separate version in translated strings.
- When producing a release artifact, use its actual version in the APK filename and release notes. Verify the installed version after deployment.

## Code, resources, and saved data

- Preserve existing work in the shared checkout. Do not undo unrelated edits or change scope without a reason.
- Update affected translations and remove resources/imports made unused by the change.
- Keep the app's data, settings, card images, customization, app routing, and ID/reference integrity intact. Introduce migrations or backup-schema changes only when the persisted format actually changes, and retain compatibility where possible.
- For UI changes, retain established component sizing, emoji colors, one-line chip labels, theme behavior, and accessibility unless the user requests a change.
- Use `AppModalBottomSheet` for standard Material modal sheets so their native slide/scrim easing stays consistent and motion settings are respected. Preserve the motion scheme of sheet contents.
- Do not change the phone/emulator's text scale, display density, resolution, or other scaling settings. Use app-scoped test configuration when a large-font check is needed.

## Verification and deployment

- Do not create release APKs/AABs, publish releases, or upload to Google Play without the user's explicit permission. Local debug builds/testing are separate.

- Run the build and relevant existing checks for the changed behavior. Add tests only when they provide useful regression coverage.
- Before authoring interaction tests, explore the actual screens with ARTEMIS, following the user's mobile-testing rules. Use verified interactions and explicit waits.
- Use the user's selected device. Ask for a choice when device selection is ambiguous and no previous selection applies.
- Prefer the imported workbook dataset for applicable financial checks: 868 transactions, 14 accounts, and HK$52,831.49 total expenses. Preserve the dataset; restore any records deliberately changed during testing.
- Install updates with replacement that retains app data. Do not uninstall, clear storage, or reset the dataset to deploy an update.
- Report success only after the build/install/test operation finishes. State what was actually checked and any remaining limitation.

## Documentation and handoff

- Add a dated entry to `PROGRESS.md` with the delivered version, changes, validation, and any material limitation.
- Update `README.md` or other user-facing documentation when the change makes it inaccurate; historical release entries should remain historical.
- Run `graphify update .` after code changes, following `.agents/rules/graphify.md`.
- Run `git diff --check` and inspect the final diff for accidental changes.
- In the final response, state the outcome and delivered version, mention deployment when performed, and link relevant new documentation.
- Do not commit, push, publish a release, or send messages to others unless the user has authorized it.
