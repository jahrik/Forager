# Part 2 follow-ups F1: the map and the Journal, completion report (dispatch `2026-09-28-181`)

Coder session, worktree `/home/zynergy-labs/Zynergy/forager-wt/followups-map`, branch `followups-map` (work pushed to `followups-map-wip` while unproven, to `journal-redesign` when proven). Written by the coder; the planner writes the record. This file grows in sections; nothing already in it is rewritten.

**Model.** The session is configured for `claude-sonnet-5-5` (the system prompt names it). I cannot read the serving model from inside the session, so I do not claim it.

## Governing text, quoted verbatim

`prompts/preserved/2026-09-29-32.md` (the dispatch) at `origin/journal-redesign`, read in full. Its rules for me: "Every item below is either ruled or a plain bug. Where an item says "investigate", **report, and do not build, if the fix needs a design choice.**"; "Tests first, pushed failing."; "Revert checks from saved copies, refused on compile errors."; "The full suite from a cleared results directory, at 0 failures."; "**Machine:** the no-Gradle and 2.5 GB memory checks, plus `df -m /` of at least 2048 MB. Never run `--stop`."

`prompts/preserved/2026-09-29-36.md` (continuation `2026-09-28-191`, replacing item 5), quoted whole:

> **Your stop was right,** and the premise was the planner's error (record -190). The owner, verbatim: "Option A", as put in the plan's "Track delete, built like waypoints".
>
> **Item 5, replaced:**
> 1. **Swipe to delete a track** in the Records list (the Tracks and All sub-tabs). Use the same TwoStageSwipeRow, swipeToDeleteTag and Undo pattern as waypoints (RecordsLogbookList.kt:163; TrackRecordingUiState.kt:121; TrackRecordingViewModel.kt:160, at addf7d7a).
> 2. **Delete on a track's details:** on the compact details sheet and on the tablet's details pane. It uses the same pending-delete and Undo, and waypoints' label.
> 3. **Never a recording track.** A track whose `endedAtEpochMillis` is null offers no swipe and no Delete. Test both surfaces.
> 4. **The delete itself** goes through the existing `DeleteTrackUseCase` (AppContainer.kt:329), which detaches the track's waypoints first. It runs only when the Undo window closes, as waypoints do.
> 5. **Journal entries that kept the track** follow the rule for an entry that kept a since-deleted waypoint.
>    - Find that rule in code, with file:line: what happens to a `cartography_entry_*_refs` row and any kept snapshot when its waypoint is deleted.
>    - Apply the same rule to track refs. Test an entry that kept a track, before and after the delete.
>    - **Stop** if waypoints have no such rule, or if the two ref tables differ in a way that makes "the same rule" ambiguous.
> 6. **Tests first, as for your other items.** Include real touches on the swipe and on the sheet's Delete.
>
> Quote this file in your report. Everything else in F1 stands.

The owner's rulings the dispatch names, in `docs/plans/journal-redesign.md`: "Part 2 Session 1's questions and J6a's questions" (the owner, verbatim: "I'll take your recommendations", which answers "'Download Maps' asks first" and "A track's details sheet gets a Delete"); "Tracks by zoom, revised" (verbatim: "2 A, 3 I'll take your recommendations"); "'Download Maps' asks first: the approved copy" (verbatim: "Approve the Download Maps wording as is").

The planner's messages to me, verbatim: at launch, "Base: the commit the planner names at launch: `6992bef5`"; and after my item-5 stop, "Planner [4b12e2]: your item-5 stop was right, and the premise was my error (record -190). The owner chose "Option A". Item 5 is REPLACED by prompts/preserved/2026-09-29-36.md on origin/journal-redesign; quote it in your report."

## Base and premises, checked before building

- **Base.** The launch names `6992bef5`; `git merge-base --is-ancestor 6992bef5 origin/journal-redesign` is true. The remote head when I started was `addf7d7a` ("Dispatch-note 2026-09-28-189: F1 launched"), which contains F2's work (`29d65a26`) and the planner's records; `-189` says "the coder pulls with --no-rebase". The worktree was cut from `origin/journal-redesign` at `addf7d7a`, and I merged the two later planner commits (`4d47409a`, `d4cee933`) with `git merge --no-edit` (no rebase).
- **The first launch of this session named the F2 dispatch (`-182`), which was already closed** (`RECORD.md` `-188`, "Closes: 2026-09-28-182"). I stopped and reported that; this dispatch is F1.
- **Premise wrong, item 5 (the dispatch's "the list's swipe delete" for a track).** Read at `addf7d7a`: `RecordsLogbookList.kt:153-159` renders a track row with no `TwoStageSwipeRow`; `swipeToDeleteTag` is used only for `WAYPOINTS` and `OFFLINE_MAPS` (`RecordsLogbookList.kt:163,181`, `AvailabilityTripsWaypointsUi.kt:217`, `AvailabilityOfflineMapsUi.kt:517`); the only pending-delete state is for waypoints (`TrackRecordingUiState.kt:121`, `TrackRecordingViewModel.kt:160`); `DeleteTrackUseCase` (`AppContainer.kt:329`) has no production caller (its only user is `DeleteTrackUseCaseTest`). So there was no guard to check and the dispatch's own stop rule fired. I stopped that item, told the planner, and the planner replaced it (`-36`, quoted above).
- **Machine, at the start.** `df -m /` 2401 MB, 4538 MB memory available, no Gradle process. It fell to 1948 MB, then 1861, 1784, 1610, 1430, 1368, 1304 MB while I worked (not by anything I wrote: my worktree is ~0.3 MB of source, no build). Below 2048 MB I ran no Gradle. I asked the owner what to do; the owner answered "Wait for space". **Nothing below has been compiled or run** until this line is superseded by a "Resumed" section. That includes every prediction in the next section.

## Pre-registration: tests and predictions (written after the code and before any run; a deviation, see below)

**Deviation.** The rule is to push predictions before building. I wrote the tests first and pushed them (`89782c9f`, `9212a2b9`, on `followups-map-wip`) and the production code after (`1275dd07`), but this section is written after all of it, because no run was possible; nothing has been observed, so no prediction below was informed by a result. The failing state to run is `9212a2b9` (tests plus signature stubs, and for item 8 the behaviour-preserving extraction), checked out detached, before the branch head is run.

**Failing at `9212a2b9` (each for the reason it names):**

| Test class | Predicted failing | Reason |
|---|---|---|
| `TrackWidthByZoomTest` (`app/src/test/.../ui/map/`) | the stops test, the widths test, the hold-and-linear test, the interpolation-expression test, the outline test | `TRACK_WIDTH_ZOOM_STOPS` is still `(11, 0.4), (15, 1)` (`TrackWidthByZoom.kt` at base); the casing-ratio test passes (a ratio, unchanged) |
| `OfflineDownloadConfirmationTest` (`.../ui/availability/`) | all seven | a tap on "Download Maps" calls `onDownloadOfflineMaps` at once (`AvailabilityOfflineMapsUi.kt:241-246` at base); no dialog exists, so "Download this area?" is not found |
| `LandscapeOfflinePickerTest` (`checkPinnedActions`, two tests) | both | the touch on Download Maps now must open the confirmation; at base it downloads |
| `PhotoExporterDateTakenTest` (`.../ui/log/`) | "keeps the record's time after the scan on publish replaces it", "the time is written after the row is published" | `PhotoExporter.kt:69` writes DATE_TAKEN at insert only; nothing writes it after `IS_PENDING` goes to 0. **Passing at base, controls:** "still written at insert", "a record with no time gets none" |
| `ReturnPromptRecreationTest` | "an activity recreation with a pending edit shows no prompt", "two recreations in a row" | on API 28+ `onSaveInstanceState` runs after `onStop`, so the flag ON_STOP sets is saved and the new Activity's ON_RESUME shows the prompt. **Inferred, not read** (I did not read the platform's ordering). If these pass at base, the mechanism is not reproduced in Robolectric and item 8 stops there. **Passing at base:** the three backgrounding tests |
| `MapViewportResizeTest` (`.../ui/map/`) | the two rotation tests | `onViewportResized` is a stub returning `this` at `9212a2b9`. **Passing at base:** "the first measurement is not a resize", "a recomposition with the same size reports nothing" |
| `AvailabilityScreenLayoutTest` (the new fullscreen "i" tests, two subclasses) | "in portrait fullscreen the attribution button clears the navigation bar inset" | `MapRenderMode.attributionBottomInset` is `null` (stub). **Expected passing:** the other three new tests (they pin unchanged behaviour). One risk: the existing test "bottomInset is positive and identical whether or not the map is fullscreen" (`AvailabilityScreenLayoutTest.kt:476`) asserts the caption's inset does not change in fullscreen, yet `AvailabilityCompactScaffold.kt` targets 0 there; I have not reconciled the two and will report what the run shows |
| `TrackDeleteTest` (`.../ui/log/`) | all except the recording-track "no Delete" pane test and "a details pane handed no delete" | the view-model methods are no-op stubs and `RecordsTab` ignores `onDeleteTrack` at `9212a2b9` |
| `TrackDeleteEntryRefsTest` (`.../app/domain/`) | none | **a characterisation, expected to pass at base and after:** the ref rows are never touched by either delete, so it pins the rule, and cannot bite the change. Flagged as CLAUDE.md asks |
| `WideChipRowClusterGuard*Test` (three sizes) | none expected | the dispatch says fix the placement only if it fails; a pass at base is a guard that "passes identically before and after a code change" and is reported as possibly not covering what it claims |

**Pass conditions after the build:** every test above green; the full suite at 0 failures from a cleared results directory, counts from the JUnit XML with every file newer than the run's start; each new test shown to bite by a revert of the behaviour it holds, from a copy saved before editing, refused if the build log has a compile error, with the forward change confirmed present afterwards.

**Not testable here (device-only), predicted before any device exists:** what MapLibre draws and where a finger lands for the "i" (item 2) and the re-projected bubble (item 1); the media scan's actual effect on `datetaken` (item 7); the camera preview in landscape (item 9); real system-bar insets everywhere. The list is in the device-only section, written at the end.
