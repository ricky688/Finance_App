# VibeFinance 1.3.14 (155) Release Notes

### Core Updates
- **Subscription Payment Cycle Tracking**:
  - Direct toggle on subscription cards and in `AddEditSubscriptionSheet` to mark as Paid (`isPaid = true`) or Due/Unpaid (`isPaid = false`).
  - Durable persistence across app restarts and automatic cycle reset for next billing date upon advance.
  - New filter tabs for Unpaid ("⏳ 待繳") and Paid ("✓ 已繳") with dynamic badge counts.
- **Motion Studio Aesthetics & Animation System**:
  - Strict motion rules: Ease-Out (`FastOutSlowInEasing` / `EmphasizedDecelerate`) on all entrances, expansions, and particle bursts; Ease-In (`FastOutLinearInEasing` / `EmphasizedAccelerate`) on all exits, dismissals, and deletions.
  - Three.quarks Particle VFX: Radiant 14-particle celebration explosion upon marking subscription as paid (zero idle CPU overhead).
  - Three GPU Pathtracer Specular Rim Lighting: 4-stop emerald gradient rim borders on paid chips.
  - Jolt Physics: Tactile micro-bounce on press (`pressScale(0.92f)`), spring physics overshoot, and smooth icon morphing.
  - 15 Bespoke Category Visuals with custom animated Canvas stories: food knife/fork double clink with sparks, coffee filling with rising steam, shopping drop with squash & stretch, car engine ignition launch, flight mode aerial loop, home warm light & chimney puff, bills dollar sign popup, health ECG heartbeat pulse, school book page fluttering, fitness double lift, gift lid pop open, gaming combo twitch, movies double clapper, pets cat waddle walk, and work golden stack heave.
- **Performance & Optimization**:
  - Full R8 minification and resource shrinking enabled (`isMinifyEnabled = true`, `isShrinkResources = true`).
  - APK size: ~4.08 MB.
  - All 433 unit tests green.
