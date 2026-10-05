# Location failures and their fallbacks

Written by the laptop planner on 2026-10-05 (UTC), for the next plan session, at the owner's request ("Save the table above so we can revisit it at the next plan session"). Status read against `main` at `d4bd00bf` and the records on `records-after-168` up to RECORD -520. A status is a claim about that moment; re-check it before planning on it.

## The principle it serves

The owner, verbatim (RECORD -519): "I want to use all of the available resources to get it right for the user. If network isn't available I want to be confident I'm not sending people to get stranded. And that includes removing reliance on network data, but not total removal of collection at all. My goal is that for each failure, there is another method available that can help fill the gaps".

## The table

**Exists** means on main at `d4bd00bf`. **Underway** means dispatched to a coder and not merged. **Planned** means decided and not dispatched. **Gap** means nothing fills it.

| When this fails | What fills the gap | Status | Where it is recorded |
|---|---|---|---|
| GPS under canopy | Steps and compass carry the position | Planned (the fusion filter) | -509, -511, -512; [research](2026-10-04-location-fusion-research.md) |
| GPS and steps both run out | The position shows as approximate, honestly | Planned (the planner's provisional 5 min or 300 m, not derived) | -512 |
| No GPS, network available | Approximate position, shown but never trusted | Underway (-510) | -508, -510; `prompts/preserved/2026-10-04-10.md` |
| No GPS, no network | Last known position, greyed, with its age | Underway (-510) | -508, -510 |
| Location lost while navigating | The line stays, turned grey | Exists | -507 |
| Compass disturbed | Gyroscope (relative heading), and GPS direction of travel while moving | Planned (the fusion filter) | [research](2026-10-04-location-fusion-research.md) |
| Step length wrong (slope, phone in a bag) | Learned from GPS; GPS speed checks it | Planned; GPS speed exists (`domain/MovingPace.kt`) | -513 |
| Pace not yet measured | A deliberately slow default, shown as "at least" | Exists (`domain/ReturnWalkingTime.kt`, not yet surfaced) | -513 |
| Walk back cannot be estimated | Alert at sunset minus the darkness margin, "walk back unknown" | Underway (-516) | -515, -516; `prompts/preserved/2026-10-04-11.md` |
| No signal at all | Sunset is calculated on the phone and needs no connection | Exists (`domain/SunCrossing.kt`, `domain/CivilTwilight.kt`) | -518 (sunset may use any reading) |
| App swiped away | The recording service keeps running and alerting | Exists (off-track alert, plan T1); sundown alerts underway (-516) | -400 era records, -516 |
| **Battery running low** | **Nothing** | **Gap**, deferred to the update after map tiling and navigation | -519, -520 |

## How the table was built, and its limits

- **Read on main:** the files named in the "Where" column. The battery gap rests on a search only: no BatteryManager, battery broadcast or PowerManager reference under `app/src/main` (-519). Not traced further.
- **Not read:** whether the off-track alert's survival after a swipe-away holds on every device; the fusion filter's figures, which come from the research report, whose sources the planner did not re-check.
- **Not exhaustive.** The rows are the failures raised in the conversation of 2026-10-04/05. The next plan session should ask what is missing, for example the phone's clock, storage running out, or the offline map not covering where the walker went.

## For the next plan session

1. Re-read each row's status against main as it is then.
2. Add the failures this table does not have.
3. Battery: the options put to the owner were a low-battery warning set against the walk back ("at this rate the battery lasts about 1 h 10, and the walk back is 1 h 30") and GPS less often when the battery is low. Neither was chosen.
