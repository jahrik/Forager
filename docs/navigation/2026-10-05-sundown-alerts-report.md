# Sundown alerts from the recording service (dispatch 2026-09-28-516) — report

**Status: in progress. Verify-before-building done and reported to the owner; the walk comparison
below is a stop-and-ask, waiting on the owner. Nothing is built yet.**

Dispatch: `prompts/preserved/2026-10-04-11.md` on `records-after-168`. Branch `sundown-alerts`, cut
from `origin/main` at `d4bd00bf` (confirmed unmoved before any citation was acted on).

## The walk comparison (verify item 1)

### How the walks were got

The owner said the walks are on the S22 (not the S26 Ultra the dispatch assumed) and approved
copying the app's database off it. The installed build is debuggable, so `forager.db` and its
`-wal`/`-shm` were copied with `adb exec-out run-as … cat` into a scratch folder outside the
repository (`/tmp/s516`). **No position is in this repository**; only the figures below are.

The estimate was run with the **real** `returnWalkingTime`, not a re-implementation: the domain
files it depends on (`ReturnWalkingTime.kt`, `PathHome.kt`, `MovingPace.kt`, `NetworkProviderFix.kt`,
`NavigationReadout.kt`, `GeoDistance.kt`, `TrackSelfJoin.kt` and five model files, as at
`d4bd00bf`) were compiled outside Gradle with the cached Kotlin 2.2.10 compiler, beside a harness
kept in `/tmp/s516` that rebuilds each track the way the read seam does (network fixes excluded,
`excludedPointCount` carried) and calls the function at a quarter, half, three quarters and all of
the way out. Not a Gradle run (the dispatch's one-build-at-a-time rule concerns Gradle); the
project builds with Kotlin 2.3.10, so the compiler differs by a minor version.

### The sample

- **61 tracks** on the S22. **58** are empty or one point (most from 2026-10-04, one-point or
  zero-point starts). **Two** more are under 3.1 minutes and under 30 m from their start.
- **Three usable walks**, all on 2026-10-03: 23.6, 16.4 and 17.5 minutes; 190–220 points; one
  network fix among them; each has a start marker; each ends 5–9 m from its first point.
- **None has a clear turnaround in the dispatch's sense — none retraces its way out.** Taking the
  point furthest from the start as the turnaround:

| Walk | Way out, along the track | Way back, along the track | Way back's median / 90th-percentile distance from the way out | Time at the far end (within 40 m) |
|---|---|---|---|---|
| A | 339 m, 7.9 min | 989 m, 15.6 min | 60 m / 157 m | 0.9 min |
| B | 351 m, 4.8 min | 892 m, 11.6 min | 35 m / 162 m | 7.4 min |
| C | 305 m, 3.7 min | 955 m, 13.8 min | 81 m / 169 m | 0.8 min |

They are loops: the way back is about three times the way out, and most of it is tens of metres
off it.

### What the estimate said

| Walk | Point on the way out | Straight-line from start | Estimate | Label | Pace used |
|---|---|---|---|---|---|
| A | ¼ / ½ / ¾ / turnaround | 71 / 169 / 231 / 311 m | 1.5 / 3.3 / 4.8 / 6.3 min | at least (all) | default 0.89 m/s (all) |
| B | ¼ / ½ / ¾ / turnaround | 80 / 176 / 227 / 268 m | 1.6 / 3.4 / 4.8 / 6.4 min | at least (all) | default (all) |
| C | ¼ / ½ / ¾ / turnaround | 67 / 148 / 208 / 278 m | 1.3 / 2.8 / 4.3 / 5.7 min | at least (all) | default (all) |

Actual walk back from the turnaround: 15.6, 11.6 and 13.8 min (15.1, 11.0 and 13.3 from leaving
the far end). Every estimate was **"at least"** for the one reason `NO_MEASURED_PACE`: moving time
(intervals whose implied speed is at least 0.5 m/s, `MovingPace.kt:116`) never reached the
five-minute bar (`MovingPace.kt:287`) on these short, slow ways out. The default pace, 0.89 m/s
(3.2 km/h), is slower than the phone's own Doppler speed while moving on these walks (median 1.13,
1.23 and 1.24 m/s), which is the direction the default is meant to err.

### What this can and cannot show

- **Can:** the estimate runs end to end on real S22 recordings, through the read seam's rules,
  without a withheld result, and degrades exactly as designed ("at least", default pace) when the
  way out is short. The default pace is conservative against this walker's measured moving speed.
- **Cannot:** whether the estimate is right for a walk back that retraces the way out, because no
  walk here does. The 6-minute "at least" against 12–16 minutes actual is a different route home,
  not an estimate error, and is no evidence either way. Nothing about measured pace (never
  reached), about walks longer than 25 minutes, or about woods.
- **One reading for the alert copy:** on all three walks the walker went home a longer way than
  they came. A leave-by built on the walk back *along the track* would have been 6–10 minutes
  optimistic for these walks, and "Leaving now gets you back before dark" would have overstated it.

## Verify items 2–5

Reported to the owner by message on 2026-10-04 and summarised here when the build lands.

## Premises that were wrong

- **The walks are on the S22**, not the S26 Ultra (owner, 2026-10-04).
- **The service has no tick of its own.** It has a 30 s flush loop (`TrackRecordingService.kt:159-164`)
  and the fix stream; an alert due at a time needs a timed check.
- **The service's raw stream does not carry the provider.** `AndroidLocationTracker.kt:71` requests
  GPS and network, as the owner said, but `toFix()` (`:89-97`) drops the provider and
  `LocationFix.Update` has no field for it (`LocationTracker.kt:45-53`). The only way to tell them
  apart in the stream today is the timestamp rule (`NetworkProviderFix.kt:48`), which the off-track
  judge already uses (`OffTrackJudge.kt:61,70`). -510's report reached the same finding
  (`origin/approximate-position`, its report line 36-37).
- **Nothing reads a last-known position today** (no `getLastKnownLocation` in `main/`). -510's
  report proposes one, as a new owned interface, not yet built (its report line 47-49).
- `getAlertsEnabled`, `DecideSundownAlertUseCase` and `returnWalkingTime` have no production caller.

## Decided beyond scope

- **Which fixes the alerts use (owner, 2026-10-04, in the coder's window).** Walk-back time: GPS
  fixes only, told apart by provider in the service's raw stream (it carries network fixes too,
  `AndroidLocationTracker.kt:71`), not the 50 m gate, so this stays clear of -510. Sunset time: any
  reading, network fixes and the last known position included ("Sunset moves about 4 s per km, and
  GPS-only would mean no sundown alert at all under canopy or indoors"); an owner-approved
  exception to -510's "never decides anything", for the sunset time only. With no fresh GPS fix:
  the dispatch's fallback (sunset minus margin, walk back unknown).
- **Arrival (owner, 2026-10-04: option B).** While heading back, the background uses the HUD's
  arrival rule (`hasArrived`, `NavigationReadout.kt:61`) against the start and stops all three
  alerts once it is met; without Return, only stopping the recording silences them.
