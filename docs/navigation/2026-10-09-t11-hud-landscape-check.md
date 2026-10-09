# T11: the navigation display (HUD) in landscape, a check and a report

Dispatch 2026-10-09-01 (RECORD intent -764, plan task T11). Branch `t11-hud-landscape`, cut from `origin/main` at `c81b326d`
(PR #207), which the session verified against the remote before starting. **No production code was changed.** Written
2026-10-09 (UTC) by the T11 coder.

## In short

The HUD never overlaps the search bar any more, in any state, window, rotation or font, headless or on the S22: the -694
cap holds. Four real problems remain, in order of what they cost a walker:

1. **At large font the distance disappears.** At font 2.0 the turn words ("Sharp right · 169°") take the first row and leave
   the distance column 7 dp wide, so the distance home ("1280 ft") is drawn as "…" alone. On the S22 at font 2.0 "≈ 700 ft"
   shows as "••". At font 1.3 in the 780 dp window the distance is cut too ("1280 ft" to 4 characters). The needle stays;
   the number that says how far is gone.
2. **The status line is lost before that, at smaller sizes.** "≈ 1250 ft straight", "Last fix 45 s ago" and "Approaching"
   are cut to 0 to 5 characters at 780 dp even at font 1.0 to 1.3, and at 823 dp from font 1.3. "Last fix 45 s ago" is the
   line that tells a walker the distance is stale.
3. **The HUD grows down into the central third.** Every extra line adds a row: the sundown line (+16 dp at font 1.0), Back by
   (+16), "Try again" (+40). On the S22, which adds a 30 dp status bar Robolectric does not, a HUD with only the sundown line
   already reaches about 10 dp into the central third at font 1.0 (captured, both rotations); at font 2.0 about 58 dp.
4. **With the icon cluster on the rail side, the HUD covers the cluster's top.** The cluster's top limit is the search
   bar's bottom, not the HUD's, and the HUD is composed after it, so the top 41 to 109 dp of the cluster (its first one to two
   buttons, Fullscreen and then Reset north) are drawn under the HUD and cannot be touched. Headless only; the default side
   (punch-hole) is clear.

Also confirmed on the S22, already logged in -694: at font 2.0 the Record/Stop pill is pushed past the bottom edge (28 of its
48 dp visible) and the rail's "Tools" label is cut. New at font 2.0 on the S22: the display's grid reference is cut
("10T ER 28688 …"), against the planner's -699 extension that the coordinates stay whole.

## How it was measured

**Headless** (`HudLandscapeT11MeasurementTest.kt`, `HudLandscapeT11TurnTest.kt`, Robolectric with native graphics, through
the real `AvailabilityScreen`). The fix (through the ViewModel's live gate), the waypoint navigation
(`AvailabilityViewModel.onNavigateToWaypoint`) and the cluster's stored landscape side go through the real
`AvailabilityViewModel`. The recording side (recording, Return, the route home, the recording's sundown line, Back by) is
passed as the screen's own parameters, as every landscape test in this package does; `TrackRecordingViewModel` is not in
the loop. Windows `w823dp-h384dp-land-xxhdpi` and `w780dp-h360dp-land-xxhdpi`; ROTATION_90 and ROTATION_270; fonts 1.0,
1.3 and 2.0; the cluster on its default (punch-hole) side and on the rail side: 24 compositions, each stepping through 13
states in walk order, 312 measured rows. The tests are a **measurement harness, not guards**: their only assertions are
positive controls that each named state is that state (for example "Arrived" in the large slot for arrived, "45 s" in the
status for stale). All 26 tests passed on main's code at 02:28Z, run twice; the second run is the one recorded here (it adds
the feet state). The raw `T11|` lines are in the JUnit XML system-out.

**S22** (SM-S908U, 1440 × 3088 at 600 dpi, so 823.5 × 384 dp in landscape). The installed build was 1.0.3111+gaae61004.
`git diff aae61004 c81b326d -- app/` is empty, so main's app code was already on the phone and no install was made (a
deviation from "install main with install -r", chosen to avoid a second Gradle build on 1.9 GB free; the code is
identical). The only settings touched were the font scale (1.0 → 2.0 → 1.0) and `user_rotation` (0 → 1 → 3 → 1 → 0), both
read back as 1.0 and 0 at the end; auto-rotate was left off as found. The one state reachable at a desk without a fake
location was navigating to an existing waypoint ("Walk test", about 700 ft away), which was started from its bubble and
ended with the HUD's X. No recording was started and no data was written. Bounds come from `uiautomator dump` (text and
button bounds, px / 3.75) and from the screenshots; the HUD's own edges are inferred from them (its box carries no
accessibility node). Captures: `~/Zynergy/device-evidence/2026-10-09-t11/` (files 00 to 10, png with xml), off GitHub.

## The table

Every state × window × font. **Rotation 90 and 270 measured identical headless** (all 156 pairs compared field by field,
0 differences): with no insets reported, nothing in the HUD's size reads the rotation, only its side. The cluster's side
changes no HUD figure; its overlap is given in its own table below. Central third: x 1/3 to 2/3 and y 1/3 to 2/3 of the
window (128 dp from the top at 384, 120 at 360). Headless tops are 0; **on the S22 every bottom is about 30 dp lower** (the
status bar), so a headless clearance under about +30 dp is inside the central third on the phone.

States: 01 Return tapped, no fix; 02 the route home with data part B's longest lines in metres, 02f the same in feet
("Sharp right · 169°", "1280 ft" "by trail", "≈ 1250 ft straight", 9843 ft); 03 "Unable to calculate route" with "Try
again"; 04 fix 45 s old; 05 the recording's sundown line, longest form ("Sunset 10:58 PM · start back by 10:48"); 06 the
same with Back by in its last hour; 08 to 11 navigating to a waypoint with no recording, far (1.1 km), approaching (50 m),
approaching and 45 s stale, arrived (5 m); 12 far, in the evening, with the screen's own sundown line. 00 and 07 (not
navigating; the strip) are in the raw output.

Against the search bar, the rail, the Record/Return pill and the default-side cluster the HUD is **clear in every row**
(touching edges at most), so those columns are not repeated.

#### 823 × 384 dp, font 1.0 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 359.0 × 92.0 | +36 | none |
| 02-return-longest | 359.0 × 92.0 | +36 | none |
| 02f-return-longest-feet | 359.0 × 92.0 | +36 | none |
| 03-return-unavailable-retry | 359.0 × 132.0 | **-4** | none |
| 04-return-stale-45s | 359.0 × 92.0 | +36 | none |
| 05-return-sundown | 359.0 × 108.0 | +20 | none |
| 06-return-sundown-backby-longest | 359.0 × 124.0 | +4 | none |
| 08-waypoint-far | 359.0 × 92.0 | +36 | none |
| 09-waypoint-approaching | 359.0 × 92.0 | +36 | none |
| 10-waypoint-approaching-stale-45s | 359.0 × 92.0 | +36 | none |
| 11-waypoint-arrived | 359.0 × 92.0 | +36 | none |
| 12-waypoint-far-screen-sundown | 359.0 × 108.0 | +20 | none |

#### 823 × 384 dp, font 1.3 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 359.0 × 95.3 | +33 | <Location services unavailable> 22/29 |
| 02-return-longest | 359.0 × 95.3 | +33 | <≈ 400 m straight> 3/16 |
| 02f-return-longest-feet | 359.0 × 95.3 | +33 | <≈ 1250 ft straight> 3/18 |
| 03-return-unavailable-retry | 359.0 × 134.0 | **-6** | none |
| 04-return-stale-45s | 359.0 × 95.3 | +33 | <Last fix 45 s ago> 3/17 |
| 05-return-sundown | 359.0 × 116.3 | +12 | <≈ 1250 ft straight> 3/18 |
| 06-return-sundown-backby-longest | 359.0 × 137.3 | **-9** | <≈ 1250 ft straight> 3/18 |
| 08-waypoint-far | 359.0 × 95.3 | +33 | none |
| 09-waypoint-approaching | 359.0 × 95.3 | +33 | none |
| 10-waypoint-approaching-stale-45s | 359.0 × 95.3 | +33 | none |
| 11-waypoint-arrived | 359.0 × 95.3 | +33 | none |
| 12-waypoint-far-screen-sundown | 359.0 × 116.3 | +12 | none |

#### 823 × 384 dp, font 2.0 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 333.0 × 118.0 | +10 | <Location services unavailable> 9/29 |
| 02-return-longest | 333.0 × 122.0 | +6 | <390 m> 0/5; <By trail> 1/8; <≈ 400 m straight> 0/16 |
| 02f-return-longest-feet | 333.0 × 122.0 | +6 | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18 |
| 03-return-unavailable-retry | 333.0 × 160.0 | **-32** | <Unable to calculate route> 14/25; <≈ 1250 ft straight> 15/18 |
| 04-return-stale-45s | 333.0 × 122.0 | +6 | <1280 ft> 0/7; <By trail> 1/8; <Last fix 45 s ago> 0/17 |
| 05-return-sundown | 333.0 × 154.0 | **-26** | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18; <Sunset 10:58 PM · start back by 10:48> 25/37 |
| 06-return-sundown-backby-longest | 333.0 × 186.0 | **-58** | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18; <Sunset 10:58 PM · start back by 10:48> 25/37 |
| 08-waypoint-far | 333.0 × 122.0 | +6 | <Straight> 7/8 |
| 09-waypoint-approaching | 333.0 × 122.0 | +6 | <≈ 150 ft> 4/8; <Straight> 7/8; <Approaching> 0/11 |
| 10-waypoint-approaching-stale-45s | 333.0 × 122.0 | +6 | <≈ 150 ft> 4/8; <Straight> 7/8; <Last fix 45 s ago> 0/17 |
| 11-waypoint-arrived | 333.0 × 122.0 | +6 | none |
| 12-waypoint-far-screen-sundown | 333.0 × 154.0 | **-26** | <Straight> 7/8; <Sunset 1:46 AM · in 1 h 10 min · dark 2:16> 25/42 |

#### 780 × 360 dp, font 1.0 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 318.0 × 92.0 | +28 | <Location services unavailable> 25/29 |
| 02-return-longest | 318.0 × 92.0 | +28 | <≈ 400 m straight> 14/16 |
| 02f-return-longest-feet | 318.0 × 92.0 | +28 | <≈ 1250 ft straight> 4/18 |
| 03-return-unavailable-retry | 318.0 × 124.0 | **-4** | none |
| 04-return-stale-45s | 318.0 × 92.0 | +28 | <Last fix 45 s ago> 5/17 |
| 05-return-sundown | 318.0 × 108.0 | +12 | <≈ 1250 ft straight> 4/18 |
| 06-return-sundown-backby-longest | 318.0 × 124.0 | **-4** | <≈ 1250 ft straight> 4/18 |
| 08-waypoint-far | 318.0 × 92.0 | +28 | none |
| 09-waypoint-approaching | 318.0 × 92.0 | +28 | none |
| 10-waypoint-approaching-stale-45s | 318.0 × 92.0 | +28 | none |
| 11-waypoint-arrived | 318.0 × 92.0 | +28 | none |
| 12-waypoint-far-screen-sundown | 318.0 × 108.0 | +12 | none |

#### 780 × 360 dp, font 1.3 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 318.0 × 95.3 | +25 | <Location services unavailable> 18/29 |
| 02-return-longest | 318.0 × 95.3 | +25 | <≈ 400 m straight> 0/16 |
| 02f-return-longest-feet | 318.0 × 95.3 | +25 | <1280 ft> 4/7; <≈ 1250 ft straight> 0/18 |
| 03-return-unavailable-retry | 318.0 × 134.0 | **-14** | <Unable to calculate route> 22/25 |
| 04-return-stale-45s | 318.0 × 95.3 | +25 | <1280 ft> 4/7; <Last fix 45 s ago> 0/17 |
| 05-return-sundown | 318.0 × 116.3 | +4 | <1280 ft> 4/7; <≈ 1250 ft straight> 0/18 |
| 06-return-sundown-backby-longest | 318.0 × 137.3 | **-17** | <1280 ft> 4/7; <≈ 1250 ft straight> 0/18 |
| 08-waypoint-far | 318.0 × 95.3 | +25 | none |
| 09-waypoint-approaching | 318.0 × 95.3 | +25 | <Approaching> 4/11 |
| 10-waypoint-approaching-stale-45s | 318.0 × 95.3 | +25 | <Last fix 45 s ago> 4/17 |
| 11-waypoint-arrived | 318.0 × 95.3 | +25 | none |
| 12-waypoint-far-screen-sundown | 318.0 × 116.3 | +4 | <Sunset 1:46 AM · in 1 h 10 min · dark 2:16> 39/42 |

#### 780 × 360 dp, font 2.0 (rotation 90 and 270 identical)

| State | HUD w × h (dp) | Clearance above central third (dp; negative = into it) | Text cut (text, characters drawn of total) |
|---|---|---|---|
| 01-return-nofix | 292.0 × 118.0 | +2 | <Location services unavailable> 6/29 |
| 02-return-longest | 292.0 × 122.0 | **-2** | <390 m> 0/5; <By trail> 1/8; <≈ 400 m straight> 0/16; <10T ER 24991 40768> 16/18 |
| 02f-return-longest-feet | 292.0 × 122.0 | **-2** | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18; <10T ER 24991 40768> 16/18 |
| 03-return-unavailable-retry | 292.0 × 160.0 | **-40** | <Unable to calculate route> 10/25; <≈ 1250 ft straight> 11/18; <10T ER 24991 40768> 16/18 |
| 04-return-stale-45s | 292.0 × 122.0 | **-2** | <1280 ft> 0/7; <By trail> 1/8; <Last fix 45 s ago> 0/17; <10T ER 24991 40768> 16/18 |
| 05-return-sundown | 292.0 × 154.0 | **-34** | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18; <10T ER 24991 40768> 16/18; <Sunset 10:58 PM · start back by 10:48> 21/37 |
| 06-return-sundown-backby-longest | 292.0 × 186.0 | **-66** | <1280 ft> 0/7; <By trail> 1/8; <≈ 1250 ft straight> 0/18; <10T ER 24991 40768> 16/18; <Sunset 10:58 PM · start back by 10:48> 21/37 |
| 08-waypoint-far | 292.0 × 122.0 | **-2** | <0.7 mi> 2/6; <Straight> 4/8; <10T ER 24991 40768> 16/18 |
| 09-waypoint-approaching | 292.0 × 122.0 | **-2** | <≈ 150 ft> 2/8; <Straight> 4/8; <Approaching> 0/11; <10T ER 24987 41829> 16/18 |
| 10-waypoint-approaching-stale-45s | 292.0 × 122.0 | **-2** | <≈ 150 ft> 2/8; <Straight> 4/8; <Last fix 45 s ago> 0/17; <10T ER 24987 41829> 16/18 |
| 11-waypoint-arrived | 292.0 × 122.0 | **-2** | <10T ER 24987 41874> 16/18 |
| 12-waypoint-far-screen-sundown | 292.0 × 154.0 | **-34** | <0.7 mi> 2/6; <Straight> 4/8; <10T ER 24991 40768> 16/18; <Sunset 1:46 AM · in 1 h 10 min · dark 2:16> 22/42 |

#### The cluster on the rail side (stored `landscapeOnPortSide = true`), rotation 90 and 270 identical

The HUD is composed after the cluster, so where they overlap the HUD is drawn on top and takes the touches.

| Window, font | HUD over the cluster's top, plain HUD (01, 02f) | With sundown (12) | With sundown and Back by (06) | "Try again" (03) |
|---|---|---|---|---|
| 823, 1.0 | 96 × 47 dp | 96 × 63 | 96 × 79 | 96 × 87 |
| 823, 1.3 | 96 × 40 | 96 × 61 | 96 × 82 | 96 × 79 |
| 823, 2.0 | 96 × 41 to 45 | 96 × 77 | 96 × 109 | 96 × 83 |
| 780, 1.0 | 96 × 47 | 96 × 63 | 96 × 79 | 96 × 79 |
| 780, 1.3 | 96 × 40 | 96 × 61 | 96 × 82 | 96 × 79 |
| 780, 2.0 | 96 × 41 to 45 | 96 × 77 | 96 × 109 | 96 × 83 |

#### After turns (`HudLandscapeT11TurnTest`, 823 × 384, ROTATION_90)

Portrait navigating, turn to landscape, back to portrait, turn again, stop and restart the return in landscape: the HUD's
bounds are the same on the first and the second turn and after the restart ([384, 0, 743, 92] at font 1.0; [384, 0, 717, 122]
at font 2.0). The -760 failure (a measure kept through portrait) does not reach the HUD, which is placed from constants
(`besideLandscapeSearchBar`), not from a measured size. One side observation: while navigating, the search bar is 45 dp tall
at font 1.0 and 77 dp at font 2.0, against 36 and 37.3 dp when it meets the strip; the HUD sits beside it, not under it, so
this changes nothing here.

## What each finding means for the walker

The enumeration rule: each cut or overlap above, by what it hides.

- **Distance drawn as "…" or "••"** (font 2.0 everywhere; font 1.3 at 780; the S22 at font 2.0): the walker cannot read how
  far home or to the waypoint is. The needle still shows which way. This is the display's main figure, and it is what the
  owner ruled must stay big ("Numbers stay big and instant", -656). **Serious.**
- **"By trail" / "Straight" cut to 1 to 7 characters**: the walker cannot tell whether a figure is along the trail or as the
  crow flies. Only matters where the figure itself shows; at font 2.0 it does not. **Moderate.**
- **The status line cut to nothing**: "≈ 1250 ft straight" (the second distance, along the straight line), "Last fix 45 s
  ago" (the warning that the figure is stale), "Approaching". At font 1.0 in the 780 window the stale warning shows 5 of
  17 characters ("Last…"). A walker can be reading a distance a minute old without being told. **Serious for the stale
  line**, moderate for the others.
- **"Location services unavailable" cut** (6 to 25 of 29): the first words survive at font 1.0 to 1.3 ("Location
  serv…"); at font 2.0 "Locati…". Readable as "something about location" at best. **Moderate.**
- **The grid reference cut** (16 of 18 at 780, font 2.0; "10T ER 28688 …" on the S22 at font 2.0): the reference read out
  to get help loses its last digits, so the position given is wrong by up to a kilometre. **Serious when it is needed**, and
  it contradicts the -699 extension's promise.
- **The sundown line cut** (21 to 39 of 37 to 42 at font 1.3 to 2.0): the start-back time ("· start back by 10:48") is the
  part dropped. The short S22 form ("Dark since 7:06 PM") fit whole. **Moderate.**
- **Into the central third**: the HUD covers map **ahead of the walker** in the heading-up view, not the walker's own dot
  (on the S22 the dot sat at about y 252 dp, the HUD's bottom at 138 dp at font 1.0 and about 186 dp at font 2.0). The
  needle and the X are never covered by anything; the HUD is on top of everything but the rail, the bottom chrome and the
  pickers, none of which it reaches. **Cosmetic to moderate**, depending on how much of the trail ahead the owner wants
  seen; this is the owner's own S10 rule.
- **Over the rail-side cluster**: the cluster's top button (Fullscreen), and with a taller HUD the next one (Reset north),
  is drawn under the HUD and cannot be tapped. Only when the walker has dragged the cluster to the rail side. **Moderate**:
  a control the walker put there stops working without saying so.
- **Record/Stop pill past the bottom edge and "Tools" cut at font 2.0** (S22, both rotations): the pill shows 28 of 48 dp,
  still tappable on its visible part; on the 780 headless window its gap to the edge is 0. The attribution caption also runs
  across the pill. **Moderate** (logged in -694, not part of the HUD).
- **Heading dropped from the second row on the S22 at font 1.0**, and "Straight ·" ending in a separator with nothing after
  it: the first is -713's rule working (labels, then the heading, give way to the coordinates); the needle still shows the
  facing. The dangling "·" is **cosmetic**.

## The S22 against the headless numbers

| | Headless 823, font 1.0 | S22, font 1.0 | Headless 823, font 2.0 | S22, font 2.0 |
|---|---|---|---|---|
| HUD height, waypoint + sundown line | 108 dp | ≈ 108 dp (30.1 → ≈ 138.4) | 154 dp | ≈ 155 dp (30.1 → ≈ 185.6) |
| Bottom against the central third (128) | 20 dp clear | **≈ 10 dp into it** | 26 dp into it | **≈ 58 dp into it** |
| HUD width | 359 dp | ≈ 280 to 290 dp | 333 dp | ≈ 255 to 290 dp |
| Search bar overlap | none | none (rot 90: bar ends ≈ 404 dp, HUD starts ≈ 404) | none | none |
| Distance | whole | whole ("≈ 700 ft") | "…" | "••" |
| Kind | first row | moved to the status line, "Straight ·" | "By trail" 1/8 | "S·" |
| Grid reference | whole | whole | whole | **cut, "10T ER 28688 …"** |
| Heading on the second row | shown | **dropped** | dropped | dropped |

**Heights agree** once the S22's 30.1 dp status bar is added (inferred from the map's top edge at 113 px). **Widths do
not**: on the phone the HUD is 70 to 80 dp narrower than headless 823, because the system navigation bar and the cut-out
band are inside `controlsPadding` there and Robolectric reports neither (CLAUDE.md, "Known pitfalls"). So the S22 at font
1.0 behaves like headless 780 at font 1.0 to 1.3, and the headless 823 rows **understate** every cut on the S22. This is why
the grid reference is whole headless at 823 and cut on the phone.

Not reproduced on the S22, for lack of the state at a desk: the route home (needs a recording), stale and lost fixes,
approaching and arrived, "Try again", Back by. Not checked on the S22: the cluster on the rail side (moving it would change
the owner's stored placement), and the -761 picture cover with the HUD up (the cover is drawn inside the map's own view,
`SightingsMap`, under all chrome, so by construction it cannot cover the HUD; not exercised).

Side observation, not investigated (outside T11): after each font-scale change while navigating, the map came back at
continent scale (captures 06 and 07). Whether the font change recreates the screen and loses the camera is unverified.

## What the owner could capture on the walk instead

On the L3 walk (-763), with Record on and then Return, in landscape, both ways round:
1. Font 1.0, the route home in its ordinary state: one screenshot. Then the same once the fix is stale (the S22 under canopy
   will give one): does "Last fix … ago" show whole?
2. If the evening comes: the sundown line plus Back by in its last hour, one screenshot, to see how far the display reaches
   down the map.
3. At font 2.0 (Settings, Display, Font size; back to normal after), one screenshot of the route home: is the distance
   readable at all?
4. If the route ever fails: "Unable to calculate route" with "Try again".
Nothing here needs a fake location; all of it needs the walk.

## Tablet (not checked; for the owner to decide)

A tablet is a big phone now (RECORD "Tablets as a big phone"). What a tablet check would add: whether the landscape branch
applies at all (the short-landscape layout keys on `isShortWindow()`; a tablet in landscape is likely not short, so it
would show the portrait-style full-width HUD), and if so, whether a 600 to 800 dp tall window leaves the HUD clear of the
central third with room to spare. Headless, it is one more window in this harness (for example `w1280dp-h800dp-land`); on
the owner's tablet, one screenshot while navigating in each orientation. Neither was done.

## Options for each real problem

Priced first by what each asks of the walker ("Don't make me think"), then by size.

**P1. The distance disappears at large font (and at 1.3 in narrow windows).**
- **A. The figure is measured first; the turn words give way.** The turn column shortens to the degrees ("169°"), then to
  the needle alone, before the distance loses a character. The walker always sees how far, and the needle still says which
  way. Asks nothing of the walker. Small: the same "measure, then drop" the second row already uses (`readoutsFitBeside`).
  **Recommended.**
- B. Let the first row wrap to two lines at large font. The walker sees everything, but the display grows another 30 to 40
  dp into the map, which makes P3 worse. Small.
- C. Leave it. The walker at font 2.0 navigates by needle alone and must guess the distance. Nothing to build.

**P2. The status line is cut to nothing.**
- **A. A priority order with a short form for each line**, as "Approaching ·" already has (-694): "Last fix 45 s ago" →
  "45 s old"; "≈ 1250 ft straight" → "≈ 1250 ft"; and the stale warning outranks the straight line when both want the line.
  The walker always sees the warning that matters. Small to medium (measured fits, as the existing one). **Recommended.**
- B. Show staleness without words: dim the distance (already done past 30 s) and add a small clock mark. Asks the walker to
  learn a sign. Small.
- C. Leave it.

**P3. The HUD grows into the central third (on the S22, from the first extra line at font 1.0).**
- **A. One evening line, not two.** When both are shown, "Dark since 7:06 PM · Back by 7:30 PM" on one line (shortening
  with "…" as now), and "Try again" beside "Unable to calculate route" instead of under it. Saves 16 to 40 dp, asks nothing
  of the walker. At font 1.0 on the S22 that is still about 10 dp into the third with one line. Small. **Recommended, with
  B for the owner to rule on.**
- B. Rule that the navigating HUD is exempt from the central-third rule in landscape (it covers map ahead, never the walker's
  dot or the needle). Nothing to build; the owner's S10 ruling is the one to change.
- C. Cap the landscape HUD at a fixed height and drop the lowest lines first (Back by, then sundown). The walker loses the
  evening lines in landscape, the ones that say when to turn back. Not recommended.

**P4. The HUD covers the cluster's top when the cluster is on the rail side.**
- A. While navigating, the cluster's top limit on the rail side is the HUD's bottom, not the search bar's. The cluster
  moves down; at 296 dp tall in a 384 dp window with a 92 to 186 dp HUD it no longer fits, so it would also have to scroll or
  shrink. Asks nothing of the walker but changes the cluster's size. Medium.
- B. While navigating, show the cluster on the punch-hole side and put it back after. The walker's chosen side is not kept,
  which CLAUDE.md's UX default calls a bug unless the owner says otherwise. Small.
- C. Draw the cluster over the HUD. The HUD's rail-side end would be covered instead: at ROTATION_90 its right end (the X
  and the three dots), at ROTATION_270 its left end (the north arrow and the needle). Worse. Not recommended.
- **This one is a stop-and-ask**: it trades the owner's "what the user has set survives" against a working control.
  **Recommendation: A if the owner accepts a shorter cluster while navigating, else B with the owner's exception recorded.**

**P5. The grid reference is cut at font 2.0 on the S22.** The -699 extension says it stays whole; at the S22's real width
there is not room for it at font 2.0. **A (recommended):** let it take the second row whole, moving the three-dot button
to the first row's end at large font (the X stays). B: allow it to shrink its type by one step before cutting. C: accept "…".

**P6. The Record/Stop pill and "Tools" at font 2.0.** Outside the HUD; logged in -694 and confirmed here. No option priced
in this report.

## Disclosures

### Confirmed vs inferred

- Confirmed (headless, read from the JUnit XML): every figure in the tables; rotation 90 and 270 equal; the cap keeps the
  HUD off the search bar in all 312 rows; the HUD's bounds are equal after a first and a second turn.
- Confirmed (S22 captures, uiautomator bounds): the HUD with a waypoint and "Dark since 7:06 PM" at font 1.0 and 2.0, both
  rotations; distance "••", "S·", grid reference cut, Record/Stop pill past the bottom edge and "Tools" cut at font 2.0;
  heading dropped and "Straight ·" at font 1.0; no search bar overlap.
- Inferred: the HUD's own edges on the S22 (its box has no accessibility node; from text bounds, button bounds and pixels,
  ± 10 dp); the 30.1 dp status bar; the identity of the cluster buttons under the HUD (from the S22 cluster order, not
  measured with the cluster on the rail side); that the -761 cover cannot reach the HUD (from where it is drawn, not
  exercised); that the headless widths understate the S22's because of insets (consistent with every number, not proven).

### Could not determine

- The route home, stale and lost fixes, arrival, "Try again" and Back by on the S22 (need a recording or a walk).
- How the cluster behaves on the rail side on the S22.
- Why the map came back at continent scale after a font change.
- The tablet.

### Premises that were wrong

- "-685 Amendment 2: at font 1.0 the 360 dp display overlapped the search bar": no longer true on main. The cap
  (`besideLandscapeSearchBar`, -694/-699) holds it to 359 dp at 823 and 318 at 780 headless, and on the S22 it starts where
  the bar ends.
- "The 160 dp HUD crosses the central third": the plain HUD is 92 dp at font 1.0 now; 160 dp is reached only with "Try
  again" at font 2.0. It is the extra lines, not the base, that cross.
- "-759 and -761 changed the landscape bar": -759/-760 changed the strip's remembered size, which the HUD does not read;
  -761 is drawn inside the map view. Neither changes the HUD (turn test above).
- The dispatch's "install main with install -r": main's app code was already installed (empty `app/` diff), so nothing was
  installed.
- The harness's own first run measured in metres, because the test fixture's stored unit is metric. Data part B's longest is
  in feet; the feet state (02f) was added and the suite re-run. The metric rows are kept.

### Decided beyond scope

- No install on the S22 (above), and navigation to an existing waypoint started and ended on the S22; no recording started,
  so nothing written to its database.
- `user_rotation` changed and restored, besides the font scale the dispatch named.
- No guard tests: the two classes are labelled measurement harnesses. Guards should follow the owner's choice of options.
