# Walk findings: why the new off-track rule stayed silent, and why the line leaves the street (dispatch 2026-09-28-439, report only)

**Status: report only. Nothing under `app/src/main` changed.** Branch `walk-findings`.

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-05.md` on `origin/dispatch-439`.
**Base:** `origin/main` at `bb0b559e`. The dispatch named `093b28d6`; PR #157 (T18) landed between, and touches nothing read here.
**The owner's go,** to this session directly: "Yes, start -439", and, for the copy, "Yes, copy the track".
**The walk:** 2026-10-03, about 20:01–20:18Z, on build `1.0.2539+g7c7e25c5` (T21's).

Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-walk-t21/`: the planner's read-only log copy, and one read-only copy of the S22's database (`db-copy/`). **The walk's positions stay there;** this repository is public. Only times, distances and counts are below.

## In plain terms

**No buzz:** the rule did what it was built to do, given when Return was actually tapped.
- The phone's own screenshot record shows Return had **not** been tapped by 20:07:54Z, a minute and a half into the detour.
- The rule measures "off track" from everything walked before Return. So the part of the detour walked before Return counted as the path, and walking back along that same street read as being on it.
- Tapped any time up to 20:08:50, the rule alerts (checked through the app's own code). Tapped from 20:08:55 on, it stays silent.
- The phone woke, and Forager came to the front, at 20:09:29–31, right at the detour's far end. That is most likely when Return was tapped.
- The owner remembers tapping it before the detour. That conflicts with the screenshot, and nothing on the phone records the tap itself (see "Could not determine").

**The line off the street** is not the app thinning the readings, and not jitter. The line is smooth and closely sampled, but displaced by several metres to about 15 m: the kind of error a phone's GPS makes between houses. Only matching the line to the mapped street or path network would correct that, and that is what -446 is set to test.

## Part A: why no alert

### What the phone recorded

- **The track:** `3ba03878`. 194 stored points, 20:01:25Z to 20:17:48Z. Every point is GPS (none sub-second). Accuracy is 3.79–5.89 m, with points every 5 s (at most 10 s).
- **The owner's notes:** none. Asked what "The rest of the report is on the phone's tracks" meant, the owner chose "The line itself". The track and its automatic Start/End markers carry no name or note, and today has no journal entries or finds.
- **The shape, from the stored points:**
  - Out to 268 m from the start at 20:06:12.
  - Then a detour off the outbound line, beyond 40 m from it by 20:06:47 and 167 m at its far end at 20:09:07.
  - Back along the detour's own street, 15–17 m from its outward line, from about 20:09:40.
  - Rejoining the outbound path at 20:12:48, then home along it: within 0–7 m, with one 27 m spur at 20:16:08.
- **The screenshots, dated by the system log:**
  - A screenshot service started at 20:07:54. That is the owner's "1:07" screenshot, which shows the coordinate strip and **no HUD**, so no return was under way.
  - Forager regained focus at 20:07:59, 20:09:31 and 20:10:11.
  - The screen was on from 20:04:49 to 20:09:10, off for two short spells, and on again from 20:09:29.
  - The "1:10" screenshot shows the HUD.
- **The app's own log lines** (main buffer) start at 20:12:10Z: the buffer rotated, and anything before that is gone. That includes any record of Return, and T22's `ForagerNavView` line, which is written when the navigation view is applied at Return. Its first surviving line, at 20:13:31, may be a later re-application.
- **Delivery:** in the system buffer, which covers the whole walk, there is no notification 1002 and no vibration from Forager's uid. Nothing was posted or attempted.

### The replay (see the failure first)

The stored track was run through the real `ReturnWatch` and `OffTrackJudge`, in a throwaway test that read the evidence folder and was deleted uncommitted (`a1-replay-real-watch`). A Python replay gives the same answers.

| Return at | Path (kept points) | Alerts |
|---|---|---|
| 20:06:12 (the far end of the main walk) | 55 | 1, at 20:07:07 |
| 20:07:55 (the earliest the screenshot allows) | 75 | 1, at 20:08:52 |
| 20:08:50 | 86 | 1, at 20:09:38 |
| 20:08:55 | 87 | 0 |
| 20:09:31 (the phone woken, Forager in front) | 94 | 0 |
| 20:10:11 | 102 | 0 |

### The candidates

1. **Geometry: not the cause.** Today's detour reaches 167 m from the path walked before it; the last walk's reached 175 m. With Return at the far end of the main walk, today's walk alerts as the last one did, at 20:07:07. "Same detour" holds in the measurements.
2. **When Return was tapped: the cause, on the evidence.**
   - No return was under way at 20:07:54, by the screenshot.
   - Any Return from 20:08:55 on gives silence by the rule. The phone woke at 20:09:29, Forager came to the front at 20:09:31, and the HUD was on screen by "1:10".
   - So Return most likely came at about 20:09:31, at the detour's far end. The path then includes the detour's way out, and its way back runs 15–17 m from it, inside the 40 m line.
   - **In conflict:** the owner answered, asked in this window, "B: before the detour". The screenshot at 20:07:54 says no return was under way more than a minute after the detour began. Both can be true only if a Return tapped earlier did not take, or was ended, before 20:07:54. Nothing recorded on the phone shows either.
3. **The judge never decided: not supported.**
   - The timestamp rule misclassifies 1 of 387 GPS fixes as network, and 1 of 60 network fixes as GPS (counted against the log's own provider lines). Neither can silence the rule.
   - The start, the path and the fixes reaching the watch cannot be checked directly: the main log before 20:12:10 is gone. The replay shows the stored data would decide.
4. **Decided but not delivered: no sign of it.** The system log has no notification post or vibration from Forager. Had the judge decided "off", the delivery would have posted 1002 (`alert/AndroidAlertDelivery.kt`), which the system log records.

**The finding, then:** the rule's own definition. "The path I walked", taken at Return, makes the alert depend on when Return is tapped relative to a detour, and a walker cannot see that dependence. Reported, not fixed (the dispatch).

## Part B: why the line leaves the street

**What the data shows:**
- **Reported accuracy is a floor, not a measurement.** 188 of 194 stored points report exactly 3.79 m, and only 7 distinct values occur. The last walk did the same (median 3.79 m). This phone's GPS accuracy cannot tell a good reading from a displaced one, so neither the off-track line's accuracy term nor any accuracy-based rejection can act on it.
- **The sampler is not cutting corners by much.**
  - Kept points are a median 6.3 m and 5 s apart, at most 10.1 m and 10 s.
  - Between 20:12:10 and 20:17:48, where the log overlaps the track, the phone delivered 338 GPS fixes and the sampler kept 68: about 1 in 5, by its 5 s minimum (`domain/model/TrackRecordingMode.kt:27`, `HIGH_ACCURACY`).
  - A straight segment between two points 6–10 m apart can cut a corner by only a few metres. The crossing drawn "halfway beyond the intersection" is larger than that, so it is the positions, not the joins between them.
- **The line is smooth, so the error is a displacement, not noise.** Each point's offset from the chord through its neighbours two either side is a median 1.2 m, 90th percentile 5.8 m, max 8.8 m.
- **The two directions of one street disagree.** The detour's way back runs 15–17 m from its way out, and on the last walk the way home ran 10–17 m from the way out. If the owner kept to one sidewalk both ways, that is GPS bias changing with direction: the body shading the sky, and reflections off houses. If they crossed, it is the street's width. **This cannot be told from the data.**
- **How far the line is from the street, and whether the basemap is offset:** cannot be measured from what is here. Either needs the mapped street geometry, and fetching it for these positions from an online service would send the owner's location out. Not done.

**Options, each with what it changes for the walker, its cost, and where it is wrong:**

| Option | What the walker sees | Cost | Where it is wrong |
|---|---|---|---|
| Keep more points at turns (a heading-change trigger in the sampler) | Corners a few metres tighter | Small; more stored points | The data shows corner cuts of only a few metres. It does not move a displaced line back onto the street. |
| Smooth the drawn line (stored track unchanged) | A slightly calmer line | Small | The line is already smooth (median 1.2 m); smoothing rounds real corners and leaves the displacement. |
| Reject outlier readings | Little, here | Small | This phone's accuracy figure is a constant 3.79 m, so there is nothing to reject on; network readings are already left out. |
| Average the raw 1 Hz fixes into each kept point | Random scatter cut | Moderate | Averaging does not remove a shared bias, and this error is mostly bias. |
| Snap to streets and paths: map matching against the mapped network, footways and trails included, not the nearest road | The line on the street or sidewalk actually walked, and on the trail in the woods | Large: offline network data, matching logic, and handling for where the map has no path | Wrong where the map is wrong or missing a path; snapping to roads only would pull a woodland trail onto the nearest road, which is why it must include paths. This is -446's item 8. |
| Draw an uncertainty band | The line with a halo | Small | Makes the user interpret it, against "Don't make me think"; it explains the error rather than removing it. |

**What this walk does and does not show** (CLAUDE.md, "real data showing the case"):
- **It shows** a displaced, well-sampled, smooth line, in a residential street, on one phone.
- **It does not show** woodland behaviour, other phones, or the line's distance from the true street.
- **So:** none of the cheap options is supported by this data. The only one aimed at a displaced line is map matching, which -446 is to test before anyone decides.

## Disclosure

**Confirmed (from the data):**
- No return was under way at 20:07:54Z (screenshot service start in the system log; the owner's screenshot shows no HUD).
- The replay through the real `ReturnWatch`: an alert for Return up to 20:08:50, silence from 20:08:55 on.
- No off-track notification or vibration was posted.
- The detour's peak distance (167 m, against the last walk's 175 m).
- Accuracy is reported as 3.79 m on 188 of 194 points.
- Sampling is 1 in 5 raw GPS fixes where the log overlaps, with points 6.3 m and 5 s apart (median).

**Inferred:**
- That Return was tapped at about 20:09:31, from the screen waking and Forager's focus then. That is the most likely time, not a recorded one.
- That the line's error is GPS bias, not sampling or noise, from its smoothness and spacing.

**Could not determine:**
- **When Return was tapped.** The app's main log before 20:12:10 rotated away, and the database records no Return.
- **Why the owner remembers tapping Return before the detour,** when no return was under way at 20:07:54. An earlier tap that did not take, or was ended, would fit, but leaves no record.
- How far the line is from the street, and whether the basemap is offset (needs street geometry).
- Which sidewalk was walked on each leg.

**Premises that were wrong:**
- The dispatch read "the detour visible at 1:15 to 1:18" as a cul-de-sac loop then. The stored track places the large detour at 20:06:30–20:12:48 (1:06–1:12 pm local). The later screenshots show it because the whole track is drawn. The only excursion after 20:13 is a 27 m spur at about 20:16 (1:16 pm).
- The base was `bb0b559e`, not `093b28d6` (PR #157 landed in between).

**Decided beyond scope:** nothing built or changed. Two suggestions for the owner and planner, not acted on:
- **Record when Return is tapped and what the off-track rule decided,** somewhere that survives log rotation. Today's question could not be settled without it.
- **Decide whether "the path I walked" should be taken at Return,** or at the far point of the walk, or should exclude a recent excursion. As built, the alert depends on Return's timing relative to a detour, which a walker cannot see.
