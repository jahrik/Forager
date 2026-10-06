# Forager Navigator: the plan for the rest

**Status: a plan, filed at the owner's word. Nothing is built and nothing is dispatched.** Each task below becomes its own dispatch file when the owner says go.

**Date:** 2026-10-01 (local), written by the planner.
**Base:** `main` `0dded057`. Every file and line below was read at that commit unless marked "audit". App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`.
**Builds on:** [`forager-navigator-plan.md`](forager-navigator-plan.md) (the plan), [`2026-09-28-navigator-plan-status-audit.md`](2026-09-28-navigator-plan-status-audit.md) (the audit, read at `pre-main` `352b708`), and the September decision records in this folder. None of them is edited.

**This is a claim about the past.** The audit is three days and one large merge (PR #140) older than this file. The planner re-checked the claims this plan leans on (listed under "Checked today"). Everything marked "audit" was not re-read, and the first step of any task that rests on it is to re-read it.

## The owner's scope

All verbatim, with the time each was said (UTC).

- 2026-10-01 22:24: "Next release we're finishing the nacigation projext we started before beta opened."
- 2026-10-01 22:25: "We can finish navigation since it's half started."
- 2026-10-02 01:43: "Let's plan the rest of the navigation plan that's been set in the repo".
- 2026-10-02 01:49, to the planner's four proposed stages (sundown; way back; navigate to a waypoint; turnaround with walk-back time) and the question of what else is in: "1 Let's do 1 to 4 and any leftover or later phase. This will be our short release, it shouldn't take that long".
- The same message, on the return screen: "2 partly, the starting UI is in, it can be changed if needed, but the arrow points toward the start point rather than the path you walked back."
- The same message, on filing this plan: "3 yes write it and file it there".

So everything left in the Navigator plan is in scope: the four stages, the first phase's leftovers, and Phases 1.5, 2, 3, 4 and the look-alike page.

## Size, stated plainly

This is eleven stages, not one feature, and it is too big for a single breakdown. So:

- **Stages A to F are broken into tasks below.** They are app-only work on decisions already made, or small leftovers. This is the part that fits "a short release".
- **Stages G to K are listed with what blocks each,** and get their own breakdown when reached. Breaking them down now would mean guessing at decisions nobody has made. Two of them (terrain, land context) need a data pipeline on the server side that does not exist, and are not short.

Where the release is cut is the owner's call and is asked under "Open for the owner".

## Checked today, by who calls what

| Claim | Evidence at `0dded057` |
|---|---|
| The sundown countdown is calculated and reaches no screen | Set at `ui/track/TrackRecordingViewModel.kt:495`. No file under `ui/` reads `sundownCountdown`. |
| The two sundown alerts never fire | `domain/DecideSundownAlertUseCase.kt:46` has no caller. `alert/AndroidAlertDelivery.kt:55-60` can post both kinds; nothing asks it to. |
| That is deliberate | The comment above `updateSundown` (`ui/track/TrackRecordingViewModel.kt:486-488`): the screen's loop "stops when the task is swiped away" and is "exactly why alert delivery must not be hung here". |
| The darkness margin is fixed at its default | The preferences repository is built at `AppContainer.kt:258` and passed nowhere; the ViewModel's default is a constant (`TrackRecordingViewModel.kt:147`). No setting exists. |
| The privacy policy describes the sundown alerts | `docs/legal/privacy-policy.md:147-149`. |
| The off-track alert is decided in the screen's loop | `TrackRecordingViewModel.kt:833`. So it is lost when the app is swiped away. |
| The way-back route has no code | No lookahead, route distance or "Unable to calculate route" anywhere in `app/src/main`. The HUD shows the straight line and "Path home" (`MainActivity.kt:626`). |
| Waypoint navigation has no picker | `navigationTarget` is only ever the start point (`MainActivity.kt:625`); `isNavigating` is `isReturning` (`ui/availability/AvailabilityScreen.kt:793`). |
| Walk-back time has no caller | `returnWalkingTime(` appears only at `domain/ReturnWalkingTime.kt:57`. |
| No check-in timer, no battery-exemption check | No `AlarmManager`, check-in, overdue or `isIgnoringBatteryOptimizations` in `app/src/main`. |
| GPX import is unreachable | No caller of `GpxCodec.decode` in `app/src/main`. |
| The recording mode is hard-coded | `MainActivity.kt:602`, `HIGH_ACCURACY`. |
| The coordinate-format toggle resets | `remember`, not saved: `ui/availability/AvailabilityCompactMapUi.kt:532`. |
| Phases 1.5, 2, 3 and 4 are unbuilt | No `SiteContext`, `FieldTrip`, `raster-dem`, `hillshade`, PAD-US or MVUM in `app/src/main`. |

**Changed since the audit:**
- **A track can now be deleted.** `DeleteTrackUseCase` is passed in at `MainActivity.kt:206`. The audit's "deletion not reachable" no longer holds.
- **A journal backup exists** (`data/backup/`). The audit's "no find or journal export exists" needs re-reading before Stage F.
- **The separate wide layout is gone.** `AvailabilityWideLayoutUi.kt` no longer exists; one layout serves both orientations, with a rail in landscape (`ui/availability/AvailabilityCompactScaffold.kt:574`). So the audit's "the wide layout has no HUD" no longer describes the code. How the HUD looks in landscape and on a tablet is not verified.

## Order, and why

1. **Stage A first: alerts move into the recording service.** The planner first proposed this last. The code says otherwise: the sundown alerts were left unwired on purpose until alerts stop depending on the screen. The owner approved the move on 2026-09-11, "deferred to after the beta" ([`2026-09-11-sundown-phase1-progress-report.md`](2026-09-11-sundown-phase1-progress-report.md), "The service move"). It is also the task most likely to overturn the plan, so it goes first.
2. **Stage B: sundown.** Mostly built; it makes the privacy policy true.
3. **Stage C: the way back.** Six owner decisions, no code. It is what the owner described on 2026-10-02: the arrow points at the start, not along the walked path.
4. **Stage D: navigate to a waypoint.** Needs a step path from the owner first.
5. **Stage E: walk-back time in the turnaround alert.** Gated on recorded walks that do not exist yet.
6. **Stage F: the first phase's leftovers.** Small, independent, can ride with whichever stage touches the same screen.
7. **Stages G to K: the later phases.**

## Tasks

Format: what it depends on, what it does, the check that proves it landed, and whether real hardware is needed.

### Stage A. Alerts live in the recording service

**T1. Off-track is decided and posted from the recording service**
- Depends on: none.
- Does: the off-track decision moves out of the screen's loop into `service/TrackRecordingService`, so an app swiped away mid-recording still alerts.
- Verify before building: re-read `domain/AlertDelivery.kt`'s header and the service's fix handling, and report where the decision can live.
- Verify: a test that delivers the alert with no screen alive, through the service's own entry point; a revert check naming this edit.
- Device-only: yes. Record on the S22, swipe the app away, confirm the alert still arrives.

### Stage B. Sundown

**T2. The turnaround and sunset alerts fire**
- Depends on: T1.
- Does: the service asks `DecideSundownAlertUseCase` each tick and posts one alert at sunset minus the darkness margin and one at sunset. Both override a silenced phone (owner, 2026-09-11). Recording only.
- Verify: a service test on a fake clock: two posts, once each, none when not recording.
- Device-only: yes. The alarm sound through a silenced phone; two separate notification categories in system settings.

**T3. The countdown on the recording screen**
- Depends on: none (the state already reaches `TrackRecordingUiState`).
- Does: a row carrying the countdown to sunset, with civil dusk as the second marker, on the top strip before navigating and in the HUD while navigating. The copy states the limit once (a clear, flat horizon) and never says there is enough time.
- Blocked by: owner decision 1 below.
- Verify: real touches sampled across the row's bounds; a long-press on the map still reaches the map; the row's fill at the map-chrome 80%; honest text for no position yet, the sun not setting, and the sun staying down.
- Device-only: yes. Where the row sits against the real top inset.

**T4. Darkness margin and alerts on/off in Settings**
- Depends on: T2.
- Does: the margin (default one hour) and the alerts switch (default on) become settings, read by the service and the screen.
- Verify: changing the margin moves the turnaround time; both survive a restart.
- Device-only: no.

### Stage C. The way back

Decisions D1 to D6: [`2026-09-11-way-back-route-decisions.md`](2026-09-11-way-back-route-decisions.md). Route-following is for the return to the start only.

**T5. The route home, as logic**
- Depends on: none.
- Does: from the walked track, a point ahead along the route for the needle, the distance along the route, and a withheld state with its reason when the walker is off the route. Recomputed every 5 s.
- Verify before building: re-read `domain/PathHome.kt`'s two thresholds (25 m and 50 m in the decision record). The lookahead starts at 25 m and is provisional (owner decision 2 below).
- Verify: unit tests on an out-and-back, a loop and a bend; a revert check.
- Device-only: no.

**T6. The HUD follows the route**
- Depends on: T5.
- Does: the needle aims along the route; route distance becomes the large figure; the straight line keeps the status line. When the route is withheld, the large slot reads "Unable to calculate route" with a refresh.
- Verify: each state driven through the real "start return" entry point.
- Device-only: yes. Walk a loop on the S22; watch the needle at bends and for jitter.

**T7. The route drawn on the map, and arrival shown on the target**
- Depends on: T5.
- Does: the route is drawn during the return; the start point's glyph changes form (not colour alone) when the walker is inside twice their own accuracy, reusing the existing approach rule.
- Verify: map touches still pass through; Reduce Motion keeps the state readable.
- Device-only: yes.

### Stage D. Navigate to a waypoint

**T8. A chosen waypoint becomes the target**
- Depends on: owner decision 3 below (the step path).
- Does: navigating to a waypoint is its own mode: chosen by the user, never automatic, straight-line bearing, and it overrules the return while active (owner, D2).
- Verify: choose a waypoint through the real callback and the HUD targets it; leave the mode and the return is as it was.
- Device-only: yes.

**T9. Navigate offered from the waypoint's bubble, its details sheet and Records**
- Depends on: T8.
- Does: In-app Navigate appears beside Directions in the places deferred on 2026-09-27 (`docs/plans/journal-redesign.md`, "In-app Navigate deferred").
- Verify: real coordinate taps on each entry; Back retraces the way in.
- Device-only: yes, on the S22, and on the tablet for landscape.

**T10. A dropped waypoint is linked to the recording it was dropped in**
- Depends on: none.
- Does: closes the audit's incidental finding (a user-dropped waypoint carries no track link).
- Verify before building: re-read `addWaypoint` (`ui/track/TrackRecordingViewModel.kt:631`).
- Verify: drop a waypoint while recording, through the real entry point; it carries the track's id and something reads it.
- Device-only: no.

**T11. The HUD in landscape and on a tablet**
- Depends on: T6.
- Does: a device check first. A fix only for what the check shows.
- Device-only: yes.

### Stage E. Walk-back time in the turnaround alert

**T12. Recorded walks with a real way back**
- Depends on: owner decision 4 below.
- Does: collects out-and-back walks, compares the estimate with the actual return time and states the sample. This is the gate the owner's 2026-09-11 decisions set.
- Device-only: yes. The walks themselves.

**T13. The turnaround alert uses walk-back time**
- Depends on: T2, T12.
- Does: the alert fires at sunset minus the darkness margin minus walking time, with the allowance for stops as its own visible term. Records by name that this replaces the plan's §4 cut of any time estimate (audit decision 2).
- Verify: `returnWalkingTime` gains a production caller; fake-clock tests for each "at least" state.
- Device-only: yes.

### Stage F. The first phase's leftovers

| Task | Does | Blocked by |
|---|---|---|
| **T14. Off-track alert's two conditions** | The wording says it is a local phone reminder, not a monitored service; a setup-time check that the alert can fire, pointing to the battery setting. | Verify first which battery-setting route needs no restricted permission (`scripts/verify-policy-permissions.sh`). |
| **T15. Overdue check-in timer** | A timer the user sets, which alerts when it passes. | Owner decision 5. Exact-alarm permissions need a policy check. |
| **T16. GPX import** | A file picked by the user becomes a track in Records. | None. Verify by importing the app's own export. |
| **T17. Recording battery mode** | A choice of mode that also changes how often the phone is asked for a fix. | Measure on the S22 first; no saving is claimed without it. |
| **T18. Coordinate format remembered** | The toggle survives a tab change. | None. |
| **T19. One source for the navigation disclaimer** | "Straight-line bearing; not a walking route" lives in one place. | None. |
| **T20. Offline readiness states** | Available, partial, not downloaded, not covered. | The own-tiles scope (`docs/plans/own-map-tiles.md`) and audit decision 3. |

### Stages G to K. The later phases

Each gets its own breakdown when reached.

| Stage | What | What blocks a breakdown today |
|---|---|---|
| **G. Phase 1.5** | Site context on a find; a find linked to a waypoint or track point; capture position kept apart from a corrected one; sensitive-location controls; privacy-safe export. | Audit decision 4 (three unreconciled positions on tracks leaving the phone). Which site-context fields the user enters and which come from terrain data (Stage H). The backup added since the audit must be re-read. |
| **H. Phase 2, terrain** | Hillshade, slope, aspect, water, elevation profile. | A server-side elevation pipeline that does not exist, and the own-tiles scope B choice it overlaps. The elevation profile alone needs no pipeline and can be pulled forward. |
| **I. Phase 3, land context** | Public-land ownership, forest roads and trails, designations, layer cards, "what am I standing in". | A server-side vector pipeline that does not exist. It would draw through the map-layers framework. The plan's private-parcel data is a paid dataset and stays out unless the owner says otherwise. |
| **J. Phase 4, trip** | Party, emergency contact, checklists, gear, weather snapshot, trip brief, fire perimeters. | Audit decision 1 (`FieldTrip`), and the trip-planner rework the owner named for this release, which has no design yet. |
| **K. Look-alike page** | A lookup by name of known toxic look-alikes. | Never scoped. The species index carries no look-alike data (audit), and no source for it is chosen. |

## Open for the owner

Each names the task it blocks. None is decided here.

1. **T3:** one top strip with two states, or the countdown mounted in two places. Put to the owner on 2026-09-11 and not answered in the record. To be brought as a step path.
2. **T5:** the lookahead distance. 25 m is proposed in the decision record and "not decided here. Put to the owner."
3. **T8, T9:** the step path for navigating to a waypoint: where it is offered, whether a recording must be running, what Back does, what ends it.
4. **T12:** whether any recorded walk with a real way back exists, on the owner's phone or a tester's. The planner asked on 2026-10-02 and the answer was about the screen, so this is still open.
5. **T15:** the check-in timer's step path.
6. **The release cut:** stages A to F as the short release with G to K following, or everything held for one release. The planner's sizing is under "Size, stated plainly".
7. **The audit's five decisions,** still open except as noted: `FieldTrip` (Stage J); walking time against §4 (settled by the 2026-09-11 rulings, recorded by T13); offline paths (T20); tracks leaving the phone (Stage G); the off-track alert's conditions (the owner's "any leftover" brings them in as T14).

## Device-only, by construction

A green test suite is not evidence for any of these: the row against the real top inset (T3); the alarm through a silenced phone and the two notification categories (T2); alerts surviving a swipe-away (T1); the needle on a real walk (T6, T8); landscape and the tablet (T9, T11).

## Disclosure

**Confirmed by reading the code today:** every row under "Checked today" and "Changed since the audit".

**From the audit, not re-read:** the state of Phases 1.5 to 4 beyond the searches above, the offline-readiness states, the track-statistics display, the species index's fields.

**Could not determine:**
- Whether the HUD is usable in landscape and on a tablet.
- Whether any recorded walk with a way back exists.
- How large Stages H and I are. No pipeline exists to measure against.

**Premises that were wrong:**
- The planner's own first order put the service move last. The code's comment on why the sundown alerts are unwired puts it first.
- The planner told the owner the HUD was missing on wide screens, from the audit. The file the audit named no longer exists.

**Decided beyond scope:** nothing. The order is proposed; every open item above is the owner's.

## Addendum, 2026-10-01 (local), after the plan was merged: two of the open items answered

Appended by the planner. Nothing above is changed. Recorded in `RECORD.md` decision 2026-09-28-399.

- **Open item 6, the release cut, is decided.** The owner, verbatim (2026-10-02T02:17Z): "A to F first and G to K next sounds good". Stages A to F (tasks T1 to T20) are the short release. Stages G to K follow in the next.
- **Open item 4, recorded walks, is answered.** Asked whether anyone has recorded a walk out and back with the app, and on whose phone, the owner said (2026-10-02T02:17Z): "Yes, and the S26 ultra". So T12 has data to start from, on the owner's own phone.
  - **Not known:** how many walks there are, and whether each has a clear turnaround. T12 checks that first and states what the sample can and cannot show.
  - **The tracks stay out of this repository.** It is public and they are real places. The owner exports them as GPX to a folder outside the repository; only the comparison figures and the size of the sample are filed.
- **Still open:** items 1, 2, 3 and 5, each brought to the owner when its stage comes up, and the audit's decisions under item 7 that wait on Stages G and J.
- **No task has a go.** The first by the plan's order is T1.

## Addendum, 2026-10-02 (UTC): T1's shape, and a new task, T21

Appended by the planner. Nothing above is changed. Recorded in `RECORD.md` decision 2026-09-28-402.

- **T1's shape is decided: the shared tracker** (shape B of [`2026-10-02-alerts-in-service-prebuild-report.md`](2026-10-02-alerts-in-service-prebuild-report.md), on branch `alerts-in-service` when this was written). The owner, verbatim (2026-10-02T04:30Z): "Option B".
- **T1 moves the off-track check as it is.** The owner, the same message: "Move as is, adjust as necessary. We need the definition to fit the user, not the software. So if the user benefits from a longer project, I'll buy that as an investment".
- **T1 now also covers the reopened app.** The owner confirmed the second path on 2026-10-02T02:59Z (`RECORD.md` continuation 2026-09-28-401): a reopened app shows the recording still running, and the return HUD if a return was under way.

**T21. What "off track" means, defined for the user** (new, Stage C)
- Depends on: T1. Sits beside T5 to T7, since the way-back route brings its own "you have left the path you walked in".
- Why: the Part 1 report's flag 2. Today's check compares three readings about one second apart and needs more than 25 m gained, so by arithmetic steady walking cannot trip it and one stray reading can. Inferred from the code and the one-second timing in `docs/audits/2026-09-07-fix-log-walk-findings.md`; not observed.
- Does: starts from what a walker heading back needs to be told and when, put to the owner as step paths. Then a rule that fits it, with its numbers taken from recorded walks and not chosen in advance.
- Blocked by: the owner's answers to those step paths, and recorded walks to measure against (open item 4).
- Verify: the rule run over recorded walks, stating the sample: it alerts where the walker did stray and stays quiet where they did not.
- Device-only: yes. A real walk, in both directions.
- Not decided here: whether the rule replaces today's check or the way-back route's off-route state takes its place.

## Addendum, 2026-10-03 (UTC): open items 1 and 3 answered, and T18 and T6 started

Appended by the planner. Nothing above is changed. Recorded in `RECORD.md` decision 2026-09-28-421.

Put to the owner as choices on 2026-10-03 and answered with the planner's recommendation each time:
- **Open item 1, T3, where the sundown countdown sits:** "Strip, then HUD": in the top strip while recording, and inside the HUD once Return is tapped. Not one combined strip.
- **Open item 3, T8 and T9, navigating to a waypoint:** offered from the waypoint's bubble, its details sheet and its row in Records; it works **without a recording running**; Back from the navigating HUD **stops navigating**, one step at a time as Back does elsewhere in the app.
- **T18, the coordinate format:** remembered across tab changes. Started as dispatch 2026-09-28-422.
- **T6, the HUD follows the route:** started as dispatch 2026-09-28-423, after T18.

Still open: item 5 (the check-in timer's step path) and the audit's decisions under item 7 that wait on Stages G and J.

## Addendum, 2026-10-03 (UTC), later: a new task, T22, the navigation camera view

Appended by the planner. Nothing above is changed. Recorded in `RECORD.md` decision 2026-09-28-427.

The owner asked, verbatim: "Can the camera view in the app change to a more navigation friendly view, like in GPS apps when navigating?" Today the map follows the walker north-up and flat (`ui/map/SightingsMap.kt:973`, `CameraMode.TRACKING`). Asked three questions, the owner chose the planner's recommendation each time: **"Automatically when navigating"**, **"Yes, facing-up"**, **"A gentle tilt"**.

**T22. The navigation camera view** (Stage C, after T6)
- Does: while returning, and later while navigating to a waypoint (T8), the map follows the walker turned so the way they face is up, using the compass, with a gentle tilt and the walker placed a little below the centre. It switches on by itself and goes back to the normal map when navigation ends.
- Facing comes from the compass, not GPS: at walking pace GPS direction is too noisy (`docs/navigation/2026-10-02-off-route-detection-research.md`, section c).
- Open, to be put to the owner as a step path before it is dispatched: what happens when the walker pans the map, how they get back to the view, and what the map does when the compass reports itself unreliable (the compass-reliability work already detects that).
- Device-only: yes. A walk.

**T22, the tilt refined by the owner** (2026-10-03, verbatim): "Have the tilt be enough to focus on the path ahead, not so gentle that it's a cosmetic tilt." So the tilt is strong enough to put the way ahead in front of the walker, not a slight lean. The planner's starting figure: about 45° (the map library allows 0° to 60°; car navigation sits near the top), a named constant tuned on a walk. Recorded in `RECORD.md` 2026-09-28-428.

**T22, the owner's answers on panning, returning and the compass** (2026-10-03, verbatim): "1 yes" (dragging the map stops it following); "2 Have a "Return to Route" button appear when panning away"; "3 give a notice first that the compass is calculating if it's stuck, after retrying to get it back, then the north up view." Recorded in `RECORD.md` 2026-09-28-429. The step path built from them is put back to the owner for confirmation before T22 is dispatched.

## Addendum, 2026-10-06 (UTC): the build list, with location fusion added as track L

The owner, verbatim: "Let's add it to the table so it doesn't get lost, and so I can keep track of it." Location fusion was never one of this plan's tasks; it ran as its own track through the record (-509, -511, -512, -519, -526, -538). It is added here as **track L**, so the one list holds everything being built. Status as of `main` at `4b72b25c` (PR #174, -516), checked task by task against the record and the code (the not-started items by a code read, as no record entry exists for them).

**Stages A to F (the short release)**

| Done | Task | Status |
|---|---|---|
| ✅ | T1 Off-track alert from the recording service | On main (PRs #148 to #150). Not yet seen firing on a walk with the app swiped away (-400 open) |
| ✅ | T2 Turnaround and sunset alerts fire | On main (PR #174, -516) |
| ⬜ | T3 Sunset countdown on the recording screen | Not started; placement decided, strip then HUD (-421) |
| ⬜ | T4 Darkness margin and alerts on/off in Settings | Not started; the stored values exist, nothing sets them |
| ✅ | T5 The route home, as logic | On main (PR #153) |
| ✅ | T6 The HUD follows the route | On main (PR #154) |
| ✅ | T7 Route drawn on the map, arrival shown | On main (PR #167) |
| ✅ | T8 A chosen waypoint becomes the target | On main (PR #168) |
| ✅ | T9 Navigate offered from bubble, details sheet and Records | On main (PR #168) |
| ⬜ | T10 A dropped waypoint linked to its recording | Not started |
| ⬜ | T11 The HUD in landscape and on a tablet | Not started; the owner's tablet check first |
| ✅ | T12 Recorded walks with a real way back | On main (PR #174); closed by the owner's ruling on two walks (-550, -553) |
| ✅ | T13 Turnaround alert uses walk-back time | On main (PR #174, -516) |
| ⬜ | T14 Off-track alert's wording and battery-setting check | Not started |
| ⬜ | T15 Overdue check-in timer | Not started; needs the owner's step path |
| ⬜ | T16 GPX import | Not started |
| ⬜ | T17 Recording battery mode | Not started |
| ✅ | T18 Coordinate format remembered | On main (PR #157) |
| ⬜ | T19 One source for the navigation disclaimer | Not started; possibly stale, its wording is no longer in the app |
| ⬜ | T20 Offline readiness states | Not started |
| ✅ | T21 What "off track" means, for the walker | On main (PR #156) |
| ✅ | T22 Navigation camera view | On main (PR #155, follow-ups -440, -457) |

**Track L, location fusion (added 2026-10-06).** The order is the owner's (-512, -526): data first, then the filter, built and tuned against real walks before it touches the live map.

| Done | Task | Status |
|---|---|---|
| ⬜ | L1 Every fix carries its true source; nothing that acts on position uses a network fix | Written as dispatch -527 (`prompts/preserved/2026-10-05-01.md`); can start now -516 has merged |
| ⬜ | L2 Walk logger: a debug-only record of everything the phone senses on a walk | Written as dispatch -532 (`prompts/preserved/2026-10-05-02.md`, with -533's Amendment 1); runs beside L1 |
| ⬜ | L3 Woods walks with both phones, logs collected | The owner's walks, after L1 and L2 |
| ⬜ | L4 The fusion filter (EKF), built and tuned against the walk logs, off the live map | Not started; -538's four questions wait on the logs ("May as well wait for a fuller picture before answering anything") |
| ⬜ | L5 Judge GPS fixes by satellite status | Only if L3's logs show fixes reporting the 3.79 m floor while really off (-526) |
| ⬜ | L6 The filter drives the live map | Not started; the owner's decision after L4 |

Research behind it: `docs/navigation/2026-10-04-location-fusion-research.md`. The failures it answers: `docs/navigation/2026-10-05-location-failure-fallbacks.md` (-519). Abandoned, not part of it: ageing fixes on the since-boot clock (-554).

**Outside both lists:** -549, units-follow (elevation in feet and soil temperature in °F under Imperial), written and not sent. Stages G to K: not started.
