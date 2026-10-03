# Way-back route, Part 1: the route home as logic (dispatch 2026-09-28-417, plan task T5)

**Status: built and pushed on `way-back-route`, not merged. Wired to nothing on screen. No phone.** The HUD that uses it (T6) and the route drawn on the map (T7) come after.

**Date:** 2026-10-02/03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-02-03.md`, read on branch `records-after-152` at `3371df1e`. Its rulings: "Rulings on the coder's findings" (continuation 2026-09-28-418, at `9b2cf04c`) and "A correction to ruling 2" (continuation 2026-09-28-419, at `cfec718f`).
**Base:** `origin/main` at `c42d857e`, checked against the remote.
**The owner's go:** to the planner, "let's begin that task", and "25m is fine. We can go with this at first and see how well it behaves." To this session directly, asked because this is a new dispatch: "Yes, build it".
**Builds on:** [`2026-09-11-way-back-route-decisions.md`](2026-09-11-way-back-route-decisions.md) (D1 to D6).

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-02-way-back-route/`.

## What this is, in plain terms

The logic that decides where the needle should point on the way home: a spot about 25 m ahead along the path you walked, not straight at the start. It also gives the distance home along that path, or says "no route" and why. Nothing on screen uses it yet; that is the next step (T6).

## Findings 1 to 4, read at `c42d857e`

### 1. PathHome and its hop bands

- `domain/PathHome.kt` does **not** project the walker onto the path. It places them at the track's **most recent stored point** (owner ruling: a nearest-point projection's error can be short; `:15-24`). The "hop" is the straight line from the walker's fix to that point (`:104`).
- **The hop bands, with hysteresis** (`:123-134`):
  - NONE.
  - COUNTED: entered above 25 m, left below 20 m.
  - FAR: entered above 50 m, left below 45 m (`:164-173`).
  - The caller carries the band from one call to the next.
- **The distance home** is the shortest route through the track joined to itself wherever it passes within 10 m of itself (`SELF_JOIN_EPSILON_METERS`, `domain/TrackSelfJoin.kt:33`; Dijkstra), plus the counted hop and the leg to the origin waypoint (`:160`).
- **A correction to the dispatch:** `pathHome` has one production caller, `ui/track/TrackRecordingViewModel.kt:793`. `ReturnWalkingTime.kt:66` calls it, but `returnWalkingTime` itself has no production caller (searched).

### 2. "Off the route" is HopBand.FAR: confirmed as the only existing line

- It measures the distance from the walker to the **most recent stored point**, not to the path.
- **The track goes on recording during the return,** so the stored track follows the walker even when they stray. A walker who strays 60 m off the outbound path is not "off the route" by this measure: the stray is recorded and becomes part of the route home, and the route then leads back along their own detour. What actually puts the hop into FAR is the gap between the walker and the stored track: rejected fixes, or the lag in finding 4.
- In D5's own terms it is right anyway: the hop is the straight line the walker would have to cross to rejoin walked ground, and FAR is where PathHome already says a straight line stops standing in for walked ground. No better line exists in the code, and none was invented.

### 3. Beside the off-track alert (not reconciled)

The alert (`domain/ReturnWatch.kt`, `domain/DetectOffTrackUseCase.kt`) reads the distance to the **start**, rising by more than 25 m over three raw fixes. The route's "off" reads the distance to the **most recent stored point**. They can disagree in two ways a user would see:

- **(a) "Off track" while the needle points the right way.** Following the route where it bends away from the start (a loop retraced, a switchback) makes the distance to the start rise, so the alert can fire while the HUD shows a route and the needle points along it.
- **(b) "Unable to calculate route" with no alert.** A walker far from the last stored point can be walking straight toward the start.

Both are true by each rule's own definition. What off track means is plan task T21.

### 4. The shape, and the stop it raised: the stored track lags the walker

**The lag.** The service writes points every 30 s or every 20 kept points (`service/TrackRecordingService.kt:317-318`), and the screen re-reads the track every 15 s (`ui/track/TrackRecordingViewModel.kt:1133`). So the most recent stored point can be 30 to 45 s behind the walker: 27 to 40 m at walking pace, more than the 25 m lookahead. A lookahead measured from that point alone would often aim behind a walker heading home.

**The planner's ruling: option B.** The lookahead is (hop + 25 m) along the route from the most recent stored point. The walker has covered at least the straight-line hop of route since that point.

**Not chosen:**
- A, measuring from the stored point alone: it points backwards often.
- C, a nearest-point projection: against the owner's ruling, and the owner's to choose if a walk shows B is not enough.
- D, a faster flush: the service's batching is off limits.

**A finding, as the ruling asked:** the lag is the root cause of both cases below. It comes from the service's batching, which was kept off limits in every dispatch so far.

**The corner guard.** The lookahead stops at the last route point for which every route point **from the walker's assumed place (`hop` metres along the route)** up to it lies within 10 m of the straight line from the walker. This reuses the self-join ruling's 10 m in a second role: that ruling accepts crossing up to 10 m of ground the walker has not walked, and no more. Provisional, with the 25 m.

**A second stop on the way:** as first worded, the guard read from the most recent stored point. That pulled the lookahead back behind the walker whenever the hop passed 10 m, which is exactly the lag case option B exists for. The planner corrected the wording (continuation -419). Two tests' expected points were rewritten for the correction; they are the 30 m-off test and the "lands behind" test below.

**The shape built:** `domain/RouteHome.kt`, plain Kotlin.
- `routeHome(track, current, origin, previousHopBand): RouteHome`.
- `RouteHome.Ahead`: the lookahead point (always a point of the route), the route distance (PathHome's `totalMeters`, from the same search), the hop band to pass back, how far along the route the lookahead is, and whether it aims at the route's end.
- `RouteHome.Withheld`: `OFF_ROUTE` (hop in the far band, with the band passed back for its hysteresis) or `NO_USABLE_POINTS`.
- The route runs from the most recent stored point through the self-joins to the first point, then the origin waypoint when there is one.
- `ROUTE_LOOKAHEAD_METERS = 25.0` is one named constant, marked provisional with the owner's words.

**The refactor, as ruled:** `joinedTrackRoute` in `domain/TrackSelfJoin.kt` is the route search, returning the predecessor chain it already held. `joinedTrackHome` is that with the chain dropped. `TrackSelfJoinTest` and `PathHomeTest` pass unedited.

## Two cases that are not right, kept visible and to check on a walk

1. **The lookahead can still land behind the walker.** This happens when the path winds hard inside the lag: the walker has walked round a U since the last stored point, so the straight-line hop is much shorter than the route covered. In the test the lookahead is about 34 m along the route and the walker about 68 m along, and the needle points back up the U. Test: `a path that winds hard inside the lag - the lookahead lands behind the walker, as ruled`.
2. **A walker beside the route is pointed well up it.** The rule assumes the walker is `hop` metres along the route, not beside it. A walker standing 30 m to the side of the last stored point is pointed about 45 m up the path. Test: `a walker 30 m off the path is still on the route, and is pointed about 45 m up it, as the rule does`.

Neither is softened, by the planner's ruling.

## What landed

| Commit | What |
|---|---|
| `5c9aa3f6` | Tests first, pushed failing, with a skeleton |
| `840e8626` | The build, and the two tests rewritten for the guard's correction |
| this commit | This report and its two index rows |

Under `app/src/main`: `domain/RouteHome.kt` (new) and `domain/TrackSelfJoin.kt` (the refactor). Nothing else. `PathHome`, its callers, the off-track alert and every composable are unchanged.

## Tests, and what each failed with first

`domain/RouteHomeTest`, 12 tests. At `5c9aa3f6` (`t1-tests-first`), with `PathHomeTest` and `TrackSelfJoinTest`: 36 tests, 11 failures, 0 compile errors.
- Nine failed by finding a "withheld" where a route was expected (a `ClassCastException` on the result's type).
- The two off-route tests failed on the reason: `NO_USABLE_POINTS` where `OFF_ROUTE` was expected.
- The "no usable points" test passed against the skeleton, which always withheld for that reason. Its assertion is what the skeleton returned.

| Test | Holds |
|---|---|
| `out and back - the lookahead is 25 m further along the way home, ahead of the walker` | An out-and-back walk |
| `out and back with the stored track 22 m behind the walker - the lookahead is still 25 m ahead of them` | Option B, the lag case |
| `a loop not yet closed - the route goes back the way the walker came, and so does the lookahead` | A loop, open: the retrace ruling |
| `a loop closed - the track joins itself near the start and the lookahead is the start` | A loop, closed |
| `a sharp bend - the line to the lookahead never leaves the walked route by more than 10 m` | A sharp bend: no route point before the lookahead is more than 10 m off the needle's line (here 8.3 m) |
| `a walker 30 m off the path is still on the route, and is pointed about 45 m up it, as the rule does` | 30 m off: still a route; the guard acting. Rewritten for the correction. |
| `a walker 60 m off the path is off the route - withheld, with the reason` | 60 m off: withheld |
| `back from off the route only below 45 m, as PathHome's far band` | The far band's hysteresis |
| `a track too short for 25 m of lookahead aims at the route's end, the origin when there is one` | A short track |
| `a track with no usable points is withheld, with the reason` | No usable points |
| `a path that winds hard inside the lag - the lookahead lands behind the walker, as ruled` | The case that lands behind. Rewritten for the correction. |
| `the route distance is the same number PathHome gives, on every shape` | The distance and band are PathHome's |

Every expected point was worked by hand on a metre grid before the build ran. The build matched each one, including the two rewritten.

## Revert checks

One edit each, from a saved copy; the reverted build's log read first (0 compile errors each time); the working tree confirmed identical to `840e8626` afterwards.

| Folder | The edit | What failed |
|---|---|---|
| `w1-no-hop-offset` | The lookahead measured from the stored point alone (option A) | 3: the lag case, the 30 m-off case and the winding case |
| `w2-no-guard` | No corner guard | 2: the 30 m-off case and the winding case |
| `w3-guard-from-stored-point` | The guard as first worded, read from the stored point | 3: the lag case, the 30 m-off case and the winding case |
| `w4-far-not-withheld` | Off the route not withheld | 2: the 60 m test and the hysteresis test |
| `w5-no-origin-node` | The origin not on the route | 1: the short-track test, aiming 3 m short of the origin |

**What `w2` also shows:** with no guard, the sharp-bend test still passes. A 25 m stretch of route round a right angle cannot stray more than 8.8 m from the needle's line, inside the 10 m allowed. The guard acts on the cases where the walker is not where option B assumes: beside the route, or past a bend inside the lag.

## Suite counts

| Run | Commit | Result |
|---|---|---|
| The four route and path classes (`t2-built`) | the tree that became `840e8626` | 48 tests, 0 failures |
| Full unit suite (`t3-full-suite`) | `840e8626`, clean tree | 422 classes, 3461 tests, 0 failures, 0 errors, 24 skipped |
| `assembleDebug` (`t4-assemble`) | `840e8626` | 0 `e:` lines |

3449 plus the 12 new tests is 3461. The 24 skipped are the same five existing classes. Both owner-held intermittent classes passed. No test was skipped, silenced or weakened.

## Left for T6 and T7

- **T6 (the HUD).**
  - Call `routeHome` every 5 s (D4), from its own job in `TrackRecordingViewModel` beside the track poll, fed the polled track, the last accuracy-gated fix and the origin waypoint. Its tests must stop it inside their bodies, as that class's poll loop requires.
  - Show `routeMeters` as the large figure (D3).
  - Aim the needle at `lookahead`.
  - On `Withheld`, say "Unable to calculate route" with a refresh, and say honestly that a refresh cannot change the answer when the reason is no usable points.
- **T7 (the map):** draw the route. The points are `joinedTrackRoute(...).route`, plus the origin.
- **The cost:** the route search now runs for this as well as for path home, so on the poll it runs twice: once for path home and once here. Measured for path home at about 19 ms on a four-hour track (the join dispatch's report); not measured here. If T6 calls both, one of them can take the other's result.

## What no test reaches

- **Whether 25 m and 10 m feel right on a real path.** Both are provisional, by the owner's words and the ruling.
- **How often the two cases above happen on a walk.**
- **The lag itself on the phone.** The 27 to 40 m figure is read from the two intervals, not measured.

## Disclosures

**Confirmed by reading:** findings 1 to 4, with file and line at `c42d857e`.
**Observed:** every test result and revert check, from the JUnit XML.
**Inferred:** the lag's size in metres, from the two intervals and the project's default walking pace.

**Could not determine:**
- How the needle behaves on a real walk.
- Whether the off-track alert and the route will disagree often enough to confuse.

**Premises that were wrong:**
- The dispatch's "production callers" named `ReturnWalkingTime.kt:66`, which has none of its own.
- The corner guard as first worded fought option B (corrected by the planner).

**Decided beyond scope:** nothing beyond the rulings. Choices inside them:
- The names `RouteHome`, `RouteWithheldReason` and `joinedTrackRoute`.
- `Withheld(OFF_ROUTE)` carries the hop band, so the far band's hysteresis survives a withheld call.
- The test grid is laid out in metres.
