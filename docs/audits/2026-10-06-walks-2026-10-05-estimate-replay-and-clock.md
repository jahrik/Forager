# The 2026-10-05 walks: the 4:49 PM walk's estimate replay, and the noon walk read on its own clock

**Dispatch:** 2026-09-28-544 (`prompts/preserved/2026-10-06-01.md`, `aa0ed917`). **RECORD:** -545 (finding,
continues -540). **Written by** the laptop coder window `session_012X8w7DNXZcsSgjKy4BZkk8`, which pulled the
tracks and ran the replay, on 2026-10-06 (UTC).

**What this is:** evidence toward -516's release gate, **not a pass**; the gate stays open (below). Figures
only: times, distances, speeds, counts. No position is in this repository. Times are local (UTC−7) unless
marked Z.

**A stop, and the owner's word on it.** Verify item 2c (below) was disproved beyond the S22 case the dispatch
named, which is one of the dispatch's stops. It was reported by message, and the owner answered: "Go on as you
proposed: write the doc as dispatched, with item 2c reported as disproved and left open, not as part of the
reading."

## State

- **Build on both phones:** 1.0.2763+gbf34fc87 (debug, -516's `sundown-alerts` with `main` `e1395fc9` merged in).
- **Branch heads read:** `records-after-173` `aa0ed917` (this dispatch, on `efd8e2ff`); `main` `e1395fc9`;
  `sundown-alerts` `d85b4ae2` (unchanged by this work).
- **Phones:** S22 Ultra `R5CT321008R` (test phone, no SIM); S26 Ultra `R5GYC4CYJ3X` (the owner's daily phone).
- **Three walks, both phones each time:**

| Walk | Real time | Phone clock | Shape |
|---|---|---|---|
| Noon | about 12:35 to 1:21 PM | **about 4 h 09 min ahead** (on purpose, -540, -541 "1 yes"); reads 4:45 to 5:30 PM | not analysed here beyond its clock |
| 4:32 PM | 4:32:44 to 4:48:24 | correct (item 2e) | a loop: the way back a median 45 m (S22) and 44 m (S26) off the way out |
| 4:49 PM | 4:49:56 to 5:14:28 | correct (item 2e) | **a clean out-and-back**: the way back a median 3 m off the way out on both phones |

The owner sent screenshots from two of these walks in one batch. To the question "Is that right?" (the 5:13 PM
screenshots from the noon walk, the 5:03 and 5:07 PM ones from the 4:49 walk), the owner answered "Yes, two walks".

## The noon walk on its own clock (verify items 2 and 3)

**The reading, confirmed:** the track's start, end and markers carry the **phone clock** (4 h 09 min ahead);
its GPS points carry **satellite time** (real time). The offset was taken from the rows: track start minus first
GPS point. That includes the 0.6 to 0.9 s each evening track took from start to first fix (item 2e), so the
clock offset itself is about a second less. Rounded figures such as -540's 249 and 250 min were not used.

| | S22 | S26 |
|---|---|---|
| Track start (phone clock) | 16:45:19.131 | 16:45:38.654 |
| Origin marker | 16:45:19.723 | 16:45:39.215 |
| First GPS point (satellite time) | 12:35:58.000 | 12:35:58.000 |
| **Offset used** (start − first GPS) | **4 h 09 m 21.131 s** | **4 h 09 m 40.654 s** |
| Track end (phone clock) | 17:30:14.176 | 17:30:30.378 |
| End marker | 17:30:14.153 | 17:30:30.359 |
| End − offset (item 2b) | 13:20:53.0 (20:20:53Z) | 13:20:49.7 (20:20:50Z) |
| Last GPS point | 13:16:08.000 | 12:59:32.000 |
| End after last GPS point | 285 s | 1,278 s |
| Points: total / GPS-class / network-class | 245 / 243 / 2 | 229 / 224 / 5 |

- **2a holds:** start minus the offset lands at the first GPS point (by construction), and the two phones' offsets
  differ by 19.5 s, which is how far apart the two recordings were started.
- **2b holds:** both ends, minus their own offsets, fall within 3 s of each other at about 20:20:50Z, after each
  phone's last GPS point. That fits the owner stopping both together.
- **2e holds:** on the 4:32 and 4:49 tracks the first GPS point is 0.64 to 0.93 s after the start, on both phones
  (S22 +0.899 s and +0.931 s; S26 +0.783 s and +0.643 s). Both clocks were correct by 4:32 PM. The evening tracks'
  five network-class points (S26: three on the 4:32 walk, two on the 4:49 walk; S22: none) also read correct time.
- **2f:** the handoff (`docs/audits/2026-10-05-planner-handoff.md`) says "Both phones were still recording at 20:25 UTC".
  Item 2b puts both ends at about 20:20:50Z, **about 4 minutes earlier** (4 m 07 s on the S22, 4 m 10 s on the S26). The handoff is not edited.

### Item 2c: the planner's prediction was wrong, and is left open

The dispatch predicted that **every network-class point reads phone-clock time**. That was the planner's
prediction, and **it was wrong. 6 of the 7 network-class points carry real time.** Only one, the S26's last stored
point, carries the phone clock: **17:09:19.156 phone clock, 12:59:38.5 real**, 6.5 s after the S26's last GPS point
(12:59:32), at the end of its 209 s GPS gap from 12:56:03.

Classified by the timestamp rule (`domain/NetworkProviderFix.kt:48`, `timestampEpochMillis % 1_000L != 0L`), each point
is shown below with the GPS points on either side of it in storage order. Storage order is monotonic in time on both
tracks.

| Phone | Network-class point | Before / after it in storage order | Reads |
|---|---|---|---|
| S22 | 12:40:08.433 | 12:39:58 / 12:40:14 | real time |
| S22 | 13:00:28.346 | 12:59:44 / 13:01:31 | real time |
| S26 | 12:39:28.875 | 12:39:05 / 12:39:34 | real time |
| S26 | 12:39:49.409 | 12:39:44 / 12:39:55 | real time |
| S26 | 12:40:09.958 | 12:40:00 / 12:40:15 | real time |
| S26 | 12:47:54.199 | 12:47:41 / 12:48:00 | real time |
| S26 | 17:09:19.156 | 12:59:32 / (last point) | **phone clock** (12:59:38.5 real) |

**2d: the S22 exception is not an exception.** The dispatch expected the S22's two network-class points, ending at
13:16:08, to break the pattern. They match four of the S26's five instead.

**In tension with -540, and not resolved here.** -540's finding infers that, with the clock ahead, the display fell
back to "the newest network reading, stamped by the phone's own clock and so fresh". If the six points above are true
network fixes, they were stamped in real time, and so they would have read as about 249 minutes old too. Only the one
at 12:59:38.5 real would have read as fresh. Equally, they may not be network fixes at all, but GPS or fused fixes with
a millisecond stamp that the timestamp rule misreads. **The saved evidence cannot settle which:**

- no log reaches back to noon (next section);
- the stored rows carry no provider (the raw stream carries none, as -516's report records).

-527 (fix-provider) and -532 (walk-logger) will record each fix's true source and its time. -540 is not edited.

### How far back the logs reach (item 3)

- **Logcat, both phones:**
  - No line is stamped before 15:05:53 (S22) or 16:14:05 (S26), and none after the dump times, 17:22:49 and 17:22:50.
  - There is no jump over 10 minutes between consecutive lines in file order.
  - Entries from the noon walk would carry phone-clock stamps between 16:45 and 17:30. The walk's last eight minutes
    (stamps 17:23 to 17:30) would sort after the dump time, and there are none.
  - The log store evicts its oldest entries first, so **no log reaches back to real 1:04 PM, and finding no noon post
    there means nothing.** Caveat: the log store also thins out the busiest app first, which this argument does not
    rule out.
- **Provider log lines** (`ForagerFix`) survive only from 17:07:47 (S22) and 17:07:28 (S26), real time.
- **Notification dumps:**
  - These are snapshots taken at about 17:24 real time.
  - The S26 holds the 4:49 walk's alert, created 17:03:27.055.
  - The S22 holds no sundown entry.
  - Neither reaches noon.
- **The coder's 00:29 UTC statement**, that each phone's system log holds "one sundown post, at 5:03, and nothing at
  5:13", holds only for real 5:13 PM. The noon walk's 5:13 PM (phone clock) alert, real about 1:04 PM, is outside every
  log. The posts read: S26 17:03:26.846, S22 17:03:33.328.
- **The "4:45:12" line was written at real 4:45:12 PM.**
  - The 00:43 UTC reading was "the app came to the front at 4:45:12 during the 4:32 recording".
  - That line belongs to app process 19813, started at 16:32:08 for the 4:32 recording.
  - That recording ran on a correct clock: its start, 16:32:45.101, is 0.9 s before its first GPS point.
  - So its closeness to the noon track's phone-clock start (16:45:19) is a 7-second coincidence.

### The withdrawn chat claim

At 00:43 UTC the coder reported, in its window, "a track on both phones whose start time is wrong". It said its 4:45 PM
start was paired with points from about 12:35 to 1:16 PM, that the S26 added a point to it at 5:09:19 during the 4:49
walk, and that it looked like "a recording bug in its own right". The planner passed it to the owner as a possible bug
in released code.

**The claim is withdrawn.** That track is the noon walk recorded with the phone clock 4 h 09 min ahead:

- its start, end and markers are on the phone clock;
- its GPS points are on satellite time;
- the "5:09:19" point is the S26's single phone-clock network-class point, at 12:59:38.5 real, written during the noon
  walk, not the 4:49 walk.

The claim's two supports were each a coincidence of clock and real time: this point, and the "4:45:12" line above.
Nothing in the tracks or logs points to a recording bug.

## The 4:49 walk's estimate replay

### Method

- **The code:** the walk-back estimate's source, copied unchanged from `bf34fc87`, the installed build.
  - Domain files: `ReturnWalkingTime.kt`, `PathHome.kt`, `MovingPace.kt`, `NetworkProviderFix.kt`, `NavigationReadout.kt`,
    `GeoDistance.kt`, `TrackSelfJoin.kt`.
  - Model files: `GeoBoundingBox`, `LatLng`, `Region`, `Track`, `TrackPoint`, `TrackPointRecord`, `Waypoint`,
    `WaypointDesignation`, `TrackRecordingMode`.
  - Each was re-checked byte-identical to `bf34fc87` with `cmp` before the final run.
- **The compile:** off Gradle, with the project's Kotlin 2.3.10 (`kotlin-compiler-embeddable-2.3.10` from the Gradle cache),
  JVM target 17. No Gradle run.
- **The harness** follows `SundownWatch` (`domain/SundownWatch.kt` at `bf34fc87`):
  - an evaluation every 15 s from the recording's start;
  - the track as the read seam gives it, with network-class points excluded and their count carried;
  - only the points already saved, on the service's 30 s cadence (`TrackRecordingService.kt`, `FLUSH_INTERVAL_MILLIS`);
  - the origin marker as the start;
  - freshness from `fixFreshness`;
  - the hop band carried between evaluations.
- **"Actual"** is how long the walker took to get home from where they passed the same spot on the way back. For each
  point on the way out, it is the way-back point nearest to it, then the time from there to the track's last point.
  The two were 2 to 12 m apart in every row below.
- **Limits:**
  - The phone uses its newest raw GPS fix, while the database keeps points about 5 s apart, so the harness's "current
    position" can be up to 5 s old.
  - The harness's evaluations are counted from the recording start, not from the phone's own timer.

### Check against the phones

The replay reproduces the 5:03 alert:

| | S22 | S26 |
|---|---|---|
| Alert posted (logcat) | 17:03:33.328 | 17:03:26.846 |
| Replay at the nearest evaluation | 17:03:27: 9.9 min, "at least" | 17:03:26: 9.9 min, "at least" |
| Rounded up as the notification does | 10 min | 10 min |
| The notification said | "at least 10 min", start by 5:32 | "at least 10 min", start by 5:33 |

The start-by time is sunset, minus the 60-minute default margin, minus the unrounded walk back. The one-minute
difference between the phones depends on the sunset's seconds and the exact walk back at the phone's own evaluation,
neither of which the replay pins down. The replay is consistent with both, and does not prove the exact minute.

### By time on the way out

Turnaround (the farthest point from the start): S22 17:04:22, 622 m out; S26 17:04:23, 618 m out. Walk back from the
turnaround: 10.1 min on both phones.

| Point on the way out (by time) | S22 estimate | S22 actual | S26 estimate | S26 actual |
|---|---|---|---|---|
| ¼ (16:53:27 / 16:53:26; still within 10 m of the start) | at least 0.2 min | 0.0 | at least 0.2 min | 0.0 |
| ½ (16:57:12 / 16:57:11; 206 m / 204 m out) | at least 4.9 min | 3.2 | at least 4.7 min | 3.1 |
| ¾ (17:00:42 / 17:00:41; 406 m / 398 m out) | at least 6.8 min | 6.4 | at least 6.9 min | 6.3 |
| Turnaround (17:04:27 / 17:04:26) | at least 10.8 min | 10.0 | at least 11.0 min | 10.0 |

### By distance out

Each row is the first evaluation at which the walker was at least a quarter, half or three quarters of the
turnaround's distance from the start. This was done in the harness only. The copied app code is unchanged.

| Point on the way out (by distance) | S22: when, how far | S22 estimate / actual | S26: when, how far | S26 estimate / actual |
|---|---|---|---|---|
| ¼ (S22 ≥ 156 m, S26 ≥ 155 m) | 16:56:12, 166 m | at least 3.5 / 2.4 min | 16:56:11, 161 m | at least 3.2 / 2.4 min |
| ½ (≥ 311 m, ≥ 309 m) | 16:59:27, 322 m | at least 5.7 / 5.2 min | 16:59:26, 312 m | at least 5.8 / 5.0 min |
| ¾ (≥ 467 m, ≥ 464 m) | 17:01:57, 484 m | at least 8.3 / 7.7 min | 17:01:56, 473 m | at least 8.4 / 7.5 min |
| Turnaround | 17:04:27, 623 m | at least 10.8 / 10.0 min | 17:04:26, 619 m | at least 11.0 / 10.0 min |

### What the replay shows

- **On the way out, the estimate was never under the actual time,** by time or by distance.
  - Before 16:58:57 it used the default pace, 0.89 m/s (`DEFAULT_MOVING_SPEED_METERS_PER_SECOND`, `MovingPace.kt:275`).
    That is slower than the walker, so it ran long: about 1.5 times the actual at half time, and 1.3 to 1.5 times at
    the quarter by distance.
  - After 16:58:57 it used the measured Doppler pace (S22 1.31 to 1.32 m/s, S26 1.21 to 1.25 m/s) and ran long by
    8 to 16% at the half and three-quarter marks, by time and by distance, and by 8 to 10% at the turnaround.
- **The distance home was measured well.** At the turnaround it put the way back at 853 m (S22) and 824 m (S26). The
  walker's way back, along the track, was 833 m and 817 m. The safety margin came from the pace, which was under the
  walker's real pace on the way back: 1.37 m/s (S22) and 1.35 m/s (S26).
- **One expected oddity:** at the switch to the measured pace (16:58:57) the estimate fell, from 6.2 to 5.2 min on the
  S22 and from 5.9 to 5.3 min on the S26, while the walker was further out. That is the faster measured pace replacing
  the default, and it is what the "freshly measured" label covers.
- **The label stayed "at least" on the whole way out,** and changed to "about" at 17:08:57, on the way back (item 4).

**What it cannot show:**

- A walk of 30 minutes or more (this one was 24.5).
- The estimate in woods or under canopy.
- The leave-by or sunset alerts.
- Anything about the noon walk's estimate, which read "unknown" because the clock made every GPS fix look lost (-540).

## The alerts

- **The heads-up sounded or buzzed on silenced phones, on both phones.** The coder asked: "Did the 5:03 alert sound or
  buzz with the phones on silent?" The owner answered: "2 yes" (00:37 UTC).
- **The leave-by and sunset alerts have not yet been seen on a correct clock.** On the 4:49 walk the leave-by was due at
  about 5:32 and the recording stopped at 5:14:28. The noon walk's alert was the heads-up, with the walk back unknown.

## -516's release gate

This is **evidence toward it, not a pass.** The gate needs one out-and-back walk of 30 minutes or more, coming back the
way out. This walk was a clean out-and-back but **24.5 minutes**. **The gate stays open.** `sundown-alerts` and its
-516 report are unchanged.

## Item 4: how the "at least" label combines its two bars

At `bf34fc87`:

- **Under 5 minutes of moving: the default pace, "at least".**
  - `MEASURED_PACE_MIN_MOVING_MILLIS = 5L * 60L * 1_000L` (`domain/MovingPace.kt:287`).
  - Below it the pace source is `PaceSource.DEFAULT` (`MovingPace.kt:186-188`).
  - That adds `DegradeReason.NO_MEASURED_PACE` (`domain/ReturnWalkingTime.kt:71`).
- **From 5 to 15 minutes: the measured pace (Doppler first, then point differencing), still "at least".**
  - `MEASURED_PACE_SETTLED_MOVING_MILLIS = 15L * 60L * 1_000L` (`MovingPace.kt:296`).
  - A measured pace with less moving time than that adds `DegradeReason.PACE_FRESHLY_MEASURED` (`ReturnWalkingTime.kt:73`).
- **From 15 minutes on: "about"**, unless another reason applies: the label is "at least" whenever any reason is present (`isAtLeast`, `ReturnWalkingTime.kt:106`).
- Both bars count the moving time of the measurement the pace came from (`MovingPace.kt:193-195`).

So the planner's guess is right. On the 4:49 walk the switch to the measured pace came at 16:58:57 (about 5 min of
moving), and "about" at 17:08:57 (15 min of moving). So on a walk where the start-by time arrives before 15 minutes of
moving, the heads-up and leave-by alerts will say "at least".

## Item 5: two map observations (read, not fixed)

Both screenshots show phone time 5:14 PM on both phones. **They are inferred to be from the 4:49 walk** (real 5:14 PM;
the recording stopped at 5:14:28), because "Arrived" needs a GPS fix that has not read as lost
(`ui/availability/NavigationHud.kt:442-449`), and on the noon walk every GPS fix read as lost (-540).

- **a. "Arrived" next to "Straight line ≈ 50 ft" is consistent, not a bug.**
  - Arrival counts within the larger of 15 m and twice the fix's reported accuracy (`domain/NavigationReadout.kt:61-62`;
    `ARRIVAL_MIN_RADIUS_METERS = 15.0` at `:65`; `APPROACHING_ACCURACY_MULTIPLIER = 2.0` at `:51`).
  - At the 3.8 m accuracy both phones logged at 17:12, that is 15 m, about 49 ft.
  - The straight-line figure is rounded to the nearest step at or above the accuracy (`domain/model/DistanceUnit.kt:128-132`,
    steps at `:139`): 3.8 m is 12.4 ft, so the step is 50 ft.
  - "≈ 50 ft" therefore covers anything from 25 to 75 ft, and the 49 ft arrival radius sits inside it.
  - It is a reading oddity, not a bug.
- **b. "114 m" is the fix's elevation, always in metres.**
  - The HUD writes `"${it.roundToInt()} m"` (`NavigationHud.kt:549`); the compass strip does the same
    (`ui/availability/AvailabilityMapControlsUi.kt:530`), and the difference from the start (`:582`).
  - None of them reads the units setting.
  - **Not new:** recorded as an open owner decision in `docs/navigation/2026-09-07-return-estimate-prebuild-report.md:237-244`
    (the units preference names one dimension, distance, while elevation, temperature and precipitation default to metric).
  - A finding for the owner, not a fix.

## Where the evidence is

`~/Zynergy/device-evidence/2026-10-05-sundown-walks/`, on the laptop, **not in any repository**, because it holds
positions:

- both phones' track databases;
- both logcat and notification dumps;
- the replay (`est/`: copied source, harness, CSVs, outputs);
- the by-distance run (`est-by-distance/`: the same app source, the harness with the distance marks and an
  evaluation-window print added, and its outputs).

The by-distance run's non-distance output lines are identical to the original run's. The session scratch folder it was
copied from is left in place.

## Not verified

- **What the six real-time network-class points are** (item 2c). It needs each fix's provider and time, which no saved
  log holds for noon. -527 and -532 would record them.
- **Whether the log store's thinning of the busiest app left noon entries** in any buffer. The reach argument assumes
  oldest-first eviction.
- **The start-by minute's exact value per phone** (5:32 against 5:33). The replay is consistent with both.
- **The by-time and by-distance readings rest on one walk of 24.5 minutes, on suburban streets.**
