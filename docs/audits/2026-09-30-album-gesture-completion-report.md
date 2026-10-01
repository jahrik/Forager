# Album gesture fix (dispatch 2026-09-28-317): completion report

**Status: done, with one open item.** A tap or long-press on a photo drawn by `DecodedPhoto` now reaches its
handler whether or not the decode lands during the gesture. `JournalTabTest`'s From Album test waits for the
edit form's decode. **Open:** 1 of 12 pinned runs had one failure in `DecodedPhotoTest` (a test I did not
write), section 9. The dispatch expected 0, so the pinned result is not clean.

- **Model:** the coder ran as `claude-sonnet-5-5` (the session's configured model; not independently read back).
- **Bases (both recorded):** the earlier -317 coder's `b90164b2` (its draft report `be3bcc18`, superseded by
  this file; it stays in history), then `origin/journal-redesign` `e495a6ce` merged in with
  `git pull --no-rebase` (`0aae2fbe`), and again `0608ab47` (`d8ea3b65`; the second merge brought only
  `RECORD.md`, so no run was repeated). The dispatch's own base was `210a0109`, an ancestor of both.
- **Branch / worktree:** `album-gesture`, `~/Zynergy/forager-wt/album-gesture`. The dispatch's `worktree add -b`
  failed because the branch and worktree already existed from the earlier coder; the planner ruled "resume it".
  The earlier draft was treated as unverified input (section 1).
- **Rulings applied:** option (c) (planner); option A for the one size difference (planner, record -342; the
  owner confirmed it in -343).
- **Evidence** is outside the repo: `~/Zynergy/forager-wt/album-gesture-evidence/<run>/` holds each run's
  `build.log` and JUnit XML, the saved base and fixed `DecodedPhoto.kt`, and the two runner scripts.

## 1. The earlier draft's claims: confirmed and corrected

| Draft claim (`be3bcc18`) | Verdict |
|---|---|
| Mechanism: `DecodedPhoto` swaps a `Box` for an `Image`, the gesture rides the swapped node | **Confirmed** by reading `DecodedPhoto.kt` at the base, and by the tests below: 5 of 5 fail at base on a swap placed between `down` and `up`. |
| `Image` sets `contentDescription` and `Role.Image`; `Role`'s merge keeps the parent's | **Confirmed** from the pinned classes (foundation-android 1.12.1, ui-android 1.12.1, `javap`): the painter overload of `Image` takes the semantics branch only when `contentDescription != null`, and `SemanticsProperties.Role$lambda$0` returns its first argument. **Not run:** I did not test that (a) or (b) would lose the role; that is inference from the merge policy, so I rejected them on that reading. |
| The call-site audit (4 sites carry a gesture) | **Confirmed** by re-reading, section 2, and tested at all four. |
| (c) leaves size "what they are today (a `ColorPainter` has no intrinsic size, like the empty `Box`)" | **Corrected.** An unsized placeholder is 0x0 as a `Box` and fills its bounds as an `Image` (measured, section 6). Sized placeholders are the same. |
| (c) "clips to bounds ... with no visible effect on a flat colour" | **Not checked.** `Image` adds `clipToBounds` to the placeholder; I looked for no visible effect. |
| `JournalTabTest` assertion at `:495`, waits only for the picker | **Confirmed** (the dispatch's `:472` was `ci-flake`'s line). |
| Test seam: a Robolectric `@Implements(BitmapFactory)` shadow with a latch | **Confirmed workable**; built, section 5. |
| `PhotoViewerDialog` is not a `DecodedPhoto` site | **Not re-read.** It was outside the rule and I did not open it. |

## 2. Every `DecodedPhoto(` call site in `main/` (11 calls, `git grep -n "DecodedPhoto("`)

All eleven pass a modifier that fixes both axes. Re-read by me at `0608ab47`.

| # | Call site | Modifier it passes | Gesture on the swapped node? |
|---|---|---|---|
| 1 | `EntriesAlbum.kt:248` (tile, no delete wired) | `fillMaxSize().clickable("Open full screen")` | **Yes** |
| 2 | `EntriesAlbum.kt:256` (tile, delete wired; the phone's and the wide tree's `LogPanel`) | `fillMaxSize().tileClickable(...)` (tap + long-press) | **Yes** |
| 3 | `LogEntryDetailScreen.kt:320` (find editor thumbnail) | `fillMaxSize().clickable("Open full screen")`, in a `Box(size(88.dp))` | **Yes** |
| 4 | `LogEntryReportScreen.kt:321` (find report thumbnail) | `size(88.dp).clickable("Open full screen")` | **Yes** |
| 5 | `CartographyEntryCard.kt:327` (entry hero) | `fillMaxWidth().height(ENTRY_HERO_HEIGHT).testTag` | No (any click is on the card) |
| 6 | `SidewaysEntryCard.kt:203` (slot) | `size(ENTRY_SLOT_SIZE).clip(shape).testTag` | No |
| 7 | `CartographyEntryEditScreen.kt:450` (`KeptPhotoOrUnavailable`, passes its `modifier` through) | callers `:418-421` and `CartographyEntryReportScreen.kt:633-636` both pass `Modifier.size(KEPT_PHOTO_SIZE_DP.dp)` (88) | No |
| 8 | `FindsGalleryScreen.kt:256` (find tile cover) | `fillMaxSize()`, in a `Box(fillMaxWidth().weight(1f))` in a `Column(fillMaxSize())` | No (the card is the click) |
| 9 | `PullPhotoPickerScreen.kt:109` (picker tile) | `fillMaxSize()`, in `Column(fillMaxSize())` in `Card(fillMaxWidth().aspectRatio(1f))` | No (`Card(onClick)` is the stable ancestor) |
| 10 | `MapBubble.kt:385` (bubble cover) | `size(BUBBLE_THUMBNAIL_SIZE)` | No |
| 11 | `MapBubble.kt:396` (bubble photo) | `size(BUBBLE_THUMBNAIL_SIZE)` | No |

The draft listed ten rows (it folded the two bubble calls). None is unsized, so the stop condition did not
trigger. The wide tree (J6) reaches the album through `EntriesAlbum` (rows 1-2).

## 3. The mechanism and the fix

`DecodedPhoto` drew a `Box(modifier)` while loading and an `Image(..., modifier)` once loaded, two different
layout nodes carrying the caller's modifier, so a caller's `clickable`/`combinedClickable` was detached and a
new one attached when the decode landed (`ci-flake` diagnosis, -296).

**Fix (option c, planner-ruled):** one `Image` in both states; only the painter changes (a flat
`surfaceVariant` `ColorPainter` until the bitmap arrives, then a `BitmapPainter` with `FilterQuality.Low`, as
`Image(bitmap)` used), and `contentDescription` is passed only once loaded. The layout node, and the caller's
click on it, never changes. `DecodedPhoto.kt` only; the call sites are untouched.

**Alternatives rejected:**
- *(a) a wrapper `Box` at each of the four sites* (-296's trial): fixes the gesture but puts the click above the
  `Image`, so the merged tile loses `Role.Image` (the parent's role wins the merge). Adding an explicit role
  would announce "image" on the empty placeholder.
- *(b) one outer node in `DecodedPhoto` with a switching child*: the same role loss at every gesture site, and
  it moves `testTag`/`clip` and `DecodedPhotoTest`'s size expectations.

## 4. The semantics proof (the planner's condition)

`DecodedPhotoSemanticsTest` dumps, for each of the four gesture sites (rows 1-4) and each of {not yet loaded,
loaded}: the merged node's whole semantics config, the same node unmerged, and its unmerged children. The
expected dumps are the **unfixed build's**, taken from the failure message of the first run (the goldens were a
marker until then). Read off them:

- **Not yet loaded (base):** no `ContentDescription`, no `Role`; `OnClick(label="Open full screen")`,
  `Focused`, `RequestFocus`, `Shape`; the wired tile also has `OnLongClick(label="Options for photo")` and
  `CustomActions=[Delete]`. Zero unmerged children.
- **Loaded (base):** the same plus exactly `ContentDescription=[Log photo]` and `Role=Image`.

With (c) the class passes against them, 4 of 4 (run 5, the 12 pinned runs, the full suite). This class is
expected to pass identically before and after the fix; that is its claim. Its bite is shown by a mutant that
passes the description while loading (run 7): all four fail on the reachability guard ("the placeholder must be
showing for the not-yet-loaded dump"; a described placeholder is exactly a `Log photo` node), and the existing
`DecodedPhotoTest` also failed. **The mutant stopped at that guard, so the golden comparison itself was not
shown to fail on a changed config; a role-only mutant was not run.**

## 5. The regression tests

`GatedDecodeSupport.kt` (`GatedDecode`, Kotlin) and `GatedBitmapFactoryShadow.java` (Java, because a Robolectric
static shadow needs a real static) are a **test-only** shadow of `BitmapFactory.decodeFile` that holds the
decode until the test releases it. No production hook. `DecodedPhotoGestureTest` (5 tests) puts the swap
between `down` and `up` for: the album tile tap with delete wired, the same with it unwired, the album
long-press (swap before the timeout), the find editor thumbnail tap, and the find report thumbnail tap. The
manual clock keeps the long-press timeout from expiring while the swap is applied. Each test first asserts
**reachability** (no `Log photo` node at `down`, one after the swap), so a swap that missed the gesture fails
loudly, not green.

**Pushed failing, as required:** run 2 (unfixed `DecodedPhoto`, state `b1bc6ab8`'s parent plus import fixes):
5 of 5 failed, four with `The component with TestTag = 'photo-viewer' is not displayed!` and one with
`... 'tile-options-delete' is not displayed!`, the lost-gesture outcomes -296 found on CI. Reachability held first.

The harness changed twice after that: a missing import (compile errors, no result read), and in run 3 the
long-press arm still failed with the fix because the timeout was advanced on event time and not on the main
clock; run 4 then showed a swap-publication race (the IO result had not reached the main thread within 8
frames). Both are fixed in the test file (clock advance; idling the main looper for 150 ms real time with no
clock moving). Because that changed the harness after the "fails at base" run, the revert check was repeated
with the final tests (section 7).

## 6. The one behaviour change: an unsized placeholder (owner-confirmed option A)

`DecodedPhotoPlaceholderSizeTest` (new) measures the not-yet-loaded size for four modifier shapes. Measured at
the unfixed build (run 8) and with (c) (run 9):

| Shape | Base `Box` | (c) |
|---|---|---|
| `fillMaxSize` in a 100x60 parent | 100x60 | 100x60 |
| `size(88.dp)` | 88x88 | 88x88 |
| `fillMaxWidth().height(50.dp)` | 320x50 | 320x50 |
| **no size modifier** | **0x0** | **320x470** (fills the root's bounds) |

Cause (inferred from the numbers, not read from source): an `Image` with a `ColorPainter` has no intrinsic size
and fills bounded constraints; an empty `Box` wraps to nothing. All eleven production calls fix both axes
(section 2), so no shipped photo changes size.

**The assertion I changed**, in my own new `DecodedPhotoPlaceholderSizeTest` only: the test formerly named
``no size at all`` (now ``no size at all - the placeholder now fills its bounds, where the old Box was 0x0``),
`assertEquals("0.0x0.0", ...)` became `assertEquals("320.0x470.0", ...)`. `DecodedPhotoTest` is **unchanged**
(it has an unsized call but waits for the load before measuring, and passed). This is a **deliberate behaviour
change for an unsized placeholder only, not a fix to a test.** It is not the assertion-weakening CLAUDE.md
forbids only because no call site is affected, and the test's comment says so. `DecodedPhoto`'s KDoc now says
callers must give it bounded size constraints, and why.

## 7. The revert check (from a saved copy, build log checked)

Saved the fixed `DecodedPhoto.kt` before editing (`DecodedPhoto.kt.fixed`, sha256 prefix `f14eae99cddc206a`),
put the base copy in, ran `*DecodedPhotoGestureTest`, `*DecodedPhotoSemanticsTest`, `*DecodedPhotoTest`
(run 6), restored from the saved copy (not from git), and confirmed after: `git status` clean, `git diff HEAD`
empty, hash equal.

- **Compile check:** 0 `e:` lines in that build log; XML timestamps `2026-09-30T23:51:41Z`..`23:51:51Z`, fresh.
- **Result:** 13 tests, 5 failures, exactly the five gesture tests, with the messages above (four viewer, one
  `tile-options-delete`). Each is a failure this revert can produce (the lost gesture); none belongs to a
  different edit. The 4 semantics and 4 `DecodedPhotoTest` tests passed at base, which also shows the goldens
  match the base build they were taken from.
- **With the fix** the same classes pass (run 5: 9 of 9).
- **Not reverted:** the From Album wait. I never reproduced that failure (it needs the edit form's decode to
  land late), so I have no evidence from this branch that the wait is what stops it. -296 confirmed the cause
  with a scratch probe, and the test now waits for the node.

## 8. The From Album wait

`JournalTabTest` "From Album on the edit form opens the picker and pulls the selected photo into the entry":
the assertion (`onNodeWithContentDescription("Log photo").assertIsDisplayed()`, the last line) is
**unchanged**. Before it sits `waitUntil(conditionDescription = "the edit form's pulled-in photo ('Log photo')
to finish decoding", timeoutMillis = 5_000)` on `onAllNodesWithContentDescription("Log photo")` having a node.
It had waited only for the picker's decode. No `Thread.sleep`, no production change for this. `JournalTabTest`
passed 12 of 12 pinned runs (18 tests each).

## 9. Pinned runs, full suite, build

All pinned with `taskset -c 0,1 ./gradlew --no-daemon` (the worker inherits the pin), `LC_ALL=C.UTF-8`, a disk
and no-other-Gradle check before each, 0 `e:` lines in every build log, fresh XML timestamps each run. Classes:
the three new ones, `DecodedPhotoTest`, `JournalPendingDeleteTest` (52), `JournalTabTest` (18); 87 tests per run.

| | Runs | Failures |
|---|---|---|
| 12 pinned, all six classes (`pin-1`..`pin-12`, 2026-09-30T23:57Z..2026-10-01T00:07Z) | 12 | **1 run, 1 test** |
| `JournalPendingDeleteTest` within them | 12 | 0 |
| `JournalTabTest` within them | 12 | 0 |
| my regression tests within them | 12 | 0 |
| `DecodedPhotoTest` alone, one CPU (`dpt-fixed-1..12`) | 12 | 0 |

**The failure:** run `pin-3`, `DecodedPhotoTest` "contentDescription is passed through":
`CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
Expected: SDK 36 Main Thread ... Calling: DefaultDispatcher-worker-2`. The stack (kept in
`pin-3/TEST-...DecodedPhotoTest.xml`) goes from `PainterElement.update` (`PainterModifier.kt:122`) through
`invalidateMeasurement` to `View.requestLayout`, inside the recomposer's `applyChanges`, which the Compose
test's `ApplyingContinuationInterceptor` had resumed on the IO worker (the `withContext(Dispatchers.IO)` return).
The painter update in place is the path (c) adds. Not reproduced in 12 further runs of the class alone, nor in
the full suite. No comparison against the unfixed build under the same mixed pinned condition was run, so I do
not know whether it is new. **Nothing was silenced or changed for it.** In production the decode returns on
`Dispatchers.Main`, so I infer a test-harness effect; that is inference.

**Baseline for the pinned condition:** I did not re-measure the unfixed build's rate. -296's figure (4 in 18
pinned) is the only base number, and it is for a different base.

**Full suite** (one run, unpinned, `full1`, fixed build, `ac815871`): **399 XML files, 3238 tests, 0 failures,
0 errors, 24 skipped**, 0 `e:` lines, XML `2026-10-01T00:16:58Z`..`00:22:36Z`. No stall, no thread dump needed.
`DiagnosticsPanelTest` did not fail. The count is not reconciled against -296's 3182 (a different base, plus
my 13 new tests).
**`assembleDebug`:** BUILD SUCCESSFUL, 0 `e:` lines (`assemble.log`).

## 10. What landed (hashes)

| Commit | What |
|---|---|
| `1277f433` | gated-decode seam, gesture + semantics tests (unrun), From Album wait |
| `b1bc6ab8` | imports fixed; base semantics recorded as goldens (run 2 had the 5 gesture failures) |
| `5ae824f1` | the fix: `DecodedPhoto` composes one `Image` |
| `baa98ee9` | gesture-test harness: settle the IO result, long-press on the main clock |
| `124d3f4f` | `DecodedPhotoPlaceholderSizeTest`; the unsized difference found and reported |
| `ac815871` | unsized expectation changed (deliberate), `DecodedPhoto` KDoc; **the code the pinned runs, suite and build ran on** |
| `d8ea3b65` | merge of `origin/journal-redesign` `0608ab47` (`RECORD.md` only) |

## 11. The four disclosures

**Confirmed (observed):** the five gesture failures at base and their five passes with the fix; the revert
check as in section 7; the base and fixed semantics dumps being equal; the unsized size change (0x0 to 320x470)
and the three unchanged shapes; the Role merge policy and the null-description branch from the pinned classes;
full suite 3238/0/0/24; `assembleDebug`; 11 of 12 clean pinned runs.

**Inferred, not confirmed:** that (a) and (b) would lose `Role.Image` (merge policy only); that the
`CalledFromWrongThreadException` is a test-harness effect and not a regression from (c); that the unsized fill
comes from the painter having no intrinsic size; that a real device drops a gesture the same way (-296's
inference, still untested on a device).

**Could not determine:** whether the `DecodedPhotoTest` failure in `pin-3` is new with (c) (no base comparison
under the same condition, and it did not recur in 12 further class runs and one suite); the unfixed build's
pinned rate in this session; whether the From Album wait changes any outcome (the failure was never reproduced
here).

**Premises that were wrong:**
- The dispatch's worktree creation: the branch and worktree already existed from an earlier session.
- The earlier draft's "(c) leaves size as it is": an unsized placeholder changes (section 6).
- The planner's "ten" call sites: eleven calls (the draft's ten rows folded the two bubble calls).
- My first harness was not deterministic past the first run: a missing clock advance for the long-press and a
  swap-publication race, both fixed (section 5) and re-proved by the revert check.

**Decided beyond scope:** nothing outside the dispatch. The `DecodedPhoto` KDoc and the size test were the
planner's conditions. I did not touch `DecodedPhotoTest`, `JournalPendingDeleteTest`, or any other existing
test except the From Album wait the dispatch asked for. No device, no adb, no CI run, no merge into
`journal-redesign`. At the disk floor (1967 MB) I paused and deleted nothing; the planner freed space.

## 12. Device-only (not run; Robolectric reports no real touch timing or storage)

On a build with this fix, tap and long-press album photos **straight after opening the album**: on a cold start,
repeated, in portrait, in landscape, and on the tablet tree (J6, SM-X800). Not one gesture is ignored. Also tap a
photo thumbnail in a find's editor and in its report straight after opening them. With TalkBack on an album
tile: it should read "Log photo, image" only after the photo has loaded, and keep "Open full screen" and
"Options for photo" before and after, as before.

**After merge, CI needs about 6 consecutive green runs** to separate the fix from luck at the old rate (-296 §7);
the planner tracks that.

## Addendum: the run-3 `DecodedPhotoTest` failure (owner: "Option 1", diagnose before any merge)

**Status: not diagnosed to a cause.** What the harness does is established; why it threw once in 12 is not, and
no fix was made. Nothing in `DecodedPhoto`, `DecodedPhotoTest` or any existing test was changed for it.

**Which thread drives recomposition in that test (read from the ui-test 1.12.1 classes with `javap`; the test,
`DecodedPhotoTest.kt:105-116`, uses `setContent` + `waitUntil`, no `runTest`, no `setMain`):**
- `ComposeUiTest_androidKt.createDefaultTestDispatcher` returns `UnconfinedTestDispatcher` when
  `useStandardTestDispatcherForComposition` is false. That the rule's default is false is **inferred from the
  trace**, not read.
- The effect context is that dispatcher wrapped in `ApplyingContinuationInterceptor(FrameDeferringContinuationInterceptor(..))`.
  `FrameDeferredContinuation.resumeWith` queues the continuation only while `isDeferringContinuations` is true
  (the main thread inside a frame or idle step); otherwise it resumes inline on the calling thread, and
  `SendApplyContinuation.resumeWith` then calls `Snapshot.sendApplyNotifications()`.
- The `pin-3` trace (kept in `pin-3/`) runs, newest first: `PainterElement.update` (`PainterModifier.kt:122`) to
  `invalidateMeasurement` to `AndroidComposeView.scheduleMeasureAndLayout` to `View.requestLayout`, inside
  `CompositionImpl.applyChanges` and `TestMonotonicFrameClock.performFrame`, resumed by `SendApplyContinuation`
  (:52, :53) and `FrameDeferredContinuation.resumeWith` (:187) from `DispatchedCoroutine.afterResume`, on
  `DefaultDispatcher-worker-2`. So in that run the effect resumed inline on the IO worker and the whole
  recompose-and-apply ran there.
- **Production:** `WindowRecomposer_androidKt` builds the window recomposer on
  `AndroidUiDispatcher.Companion.getCurrentThread`; `AndroidUiDispatcher` is a real `CoroutineDispatcher` whose
  `dispatch` posts to the thread's handler (no `isDispatchNeeded` override in its member list), so the
  post-decode continuation goes back to the UI thread. I infer no production risk from this; **not run on a device**.

**Hypotheses (two, as the planner bounded):** H1, the harness exposes the unfixed build to the same race, at a
lower rate or none seen; H2, the old swap's apply does not call `View.requestLayout` synchronously and (c)'s
in-place painter update does. A third was not formed.

**Probes (scratch, `950d64ec` and its instrumentation commit, both reverted; results kept in the evidence
folder, `probe-fixed-1..6`, `probe-fixed-instr`, `probe2-fixed-1..4`):**
- *Probe 1* (decode released while the test thread sleeps, 6 runs on the fixed build): 6 of 6 passed, 0 compile
  errors, fresh timestamps. H1 and H2 both predict an exception on the fixed build here, so **neither prediction
  held, which means the probe did not force the failing condition.**
- *Instrumented* (a global-write observer and a `layout` hook, test-only): the `bitmap` state write ran on
  `DefaultDispatcher-worker-3` (the IO thread, inline resume confirmed), but both measure passes ran on the main
  thread. So the probe reached the inline state write but not the inline recompose-and-apply.
- *Probe 2* (40 photos, decodes released together by a helper thread while the test thread was inside
  `waitUntil`, 4 runs on the fixed build): in each run 40 `bitmap` writes ran on 40 different IO workers and 10 on
  the main thread, and the test completed with **no exception, 0 of 160 inline writes**. The inline state write is
  the harness's normal behaviour; the exception needs the apply to run there too, and 160 attempts never did.
- **Base not probed.** The inline write comes from `DecodedPhoto`'s `LaunchedEffect`, unchanged by (c), so a
  base run of a probe that does not fail on the fixed build could not separate H1 from H2 (0 against 0). Per the
  planner's instruction, the 35-run same-condition comparison was **not started**.

**What is established:** the inline resume on the IO worker is the unconfined-dispatcher harness's normal path in
this test class, seen in 164 of 164 instrumented attempts; the `pin-3` failure is that path continuing into
`applyChanges` on the worker; the failure site is the painter update that (c) adds; the old swap's apply was not
instrumented. **Not established:** whether the unfixed build can fail the same way, and what puts the apply on the
worker only sometimes (it happened 1 in 12 in the mixed pinned set, 0 in 12 class-only runs, 0 in the suite and 0
in the 22 probe runs). H1 and H2 are both undecided.

**Proposed fix:** none yet, because the cause is not found. The one option I can see for the code, writing
`bitmap` through `withContext(Dispatchers.Main)`, would change production behaviour to accommodate a harness
that production does not use, so I did not try it. The options for the planner and owner: (1) accept it and watch
CI, with the observed rate (1 in 12 pinned mixed, Wilson 95% 1.5% to 35.4%) as the thing to watch; (2) the 35-run
same-condition comparison on base and fixed (sizing in the message of record: n = ceil(ln 0.05 / ln(11/12)) = 35
to see a failure with 95% probability at 1/12; 0 of 35 excludes a rate above 9.9%), which would at least say
whether the unfixed build ever fails this way; (3) capture the failing run's thread for the *apply* (a
`layout` hook in a copy of `DecodedPhotoTest`) while repeating the pinned set, since the apply thread is the one
datum the failing run has and the probes lack.

**Reverted:** the scratch probe, by `git revert` (history kept), and the app tree compared with `ac815871` shows
no difference (`git diff ac815871 HEAD -- app` empty), `DecodedPhoto.kt` hash `5eab4b57b30a1511` = the saved copy.
Last Gradle run: probe 2, fixed build 4 of 4; nothing is running.
