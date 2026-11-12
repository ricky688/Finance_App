# Samsung release scrolling investigation — 2026-10-31

Question: why does the installed release still lag despite R8?

Device: Samsung SM-S9280. Trace: `/tmp/vibe-phone-scroll.perfetto-trace` (local only, 15 seconds). Two asset-page upward swipes; no financial mutation or device-scale changes. Trace collected from the installed release before source changes.

## Evidence

- Existing cumulative gfxinfo: 257 frames, 59 missed deadlines (22.96%); p50 14ms, p90 81ms, p95 133ms, p99 650ms. GPU p95 7ms. This includes prior interactions, not just controlled scrolling.
- Trace: lazy prefetch composition 10 slices, total187.60ms, max34.77ms; idle-frame prefetch max40.73ms; urgent prefetch max34.26ms; Compose recompose max28.63ms. Nested slices overlap: totals must not be added together.
- Two main-thread ImageDecoder decodes, max3.61ms. Haze effect update max0.31ms; this does not measure its complete downstream rendering cost.
- This points to UI composition/prefetch pressure, not proof that history count or blur alone causes the lag. No app method sampling or per-composable tracing was available, so the precise remainder of composition cost is unresolved.

## First correction

Account thumbnails and linked-app icons now load through produceState + Dispatchers.IO, removing blocking file decode and PackageManager work from composition. Existing icon cache remains. Removed the separate synchronous package-existence lookup: icon loading already checks availability. R8/resource shrinking remain configured. No blanket effect removal or density change.

## Reproducible queries

Load with the android-profiler skill's pinned trace_processor wrapper:

```sh
trace_processor server unix --name vibe-scroll --daemonize /tmp/vibe-phone-scroll.perfetto-trace
```

```sql
INCLUDE PERFETTO MODULE slices.with_context;
SELECT * FROM thread_slice LIMIT 0;
```

```sql
SELECT s.name, count(*) AS n,
       round(sum(s.dur)/1e6, 2) AS total_ms,
       round(max(s.dur)/1e6, 2) AS max_ms
FROM thread_slice s
WHERE s.process_name = 'com.ricky688.vibefinance'
  AND s.is_main_thread = 1 AND s.dur > 0
GROUP BY s.name
ORDER BY total_ms DESC LIMIT 22;
```

## Validation

`./gradlew testDebugUnitTest assembleDebug` passed in1m22s; 429unit tests, zero failures/errors/skips. Graph refreshed; diff whitespace check passed. No new interaction tests, handset installation or release artifact.

## Limits / next measurement

No before/after release benchmark yet; do not claim a percentage improvement. Debug timing would not establish release performance. Need a newly authorized release build and identical warm scrolling passes, including Daily, History and Recurring, before calling the reported phone lag resolved. Full high-resolution rendering, card composition/typography, prefetch work and absence of an app-specific baseline profile remain investigation candidates.

## Authorized release follow-up

User authorized release/install. R8 release build passed;153/1.3.12 installed in place on Samsung and version/non-debuggable status checked. Four identical swipes after gfxinfo reset, starting at the Assets top (verified with ARTEMIS):

| Single pass | Missed-deadline frames | p50 | p90 | p95 | p99 |
|---|---:|---:|---:|---:|---:|
| Old152 |35/431 (8.12%) |16ms|22ms|25ms|48ms|
| New153 |20/497 (4.02%) |13ms|16ms|18ms|28ms|

Local raw outputs: `/tmp/vibe-1.3.11-controlled-gfx.txt` and `/tmp/vibe-1.3.12-controlled-gfx.txt`. Warm/cache state and scheduling are not strictly controlled; one pass is directional evidence only. Remaining jank exists and other pages were not benchmarked. No claim of120fps or complete resolution.
