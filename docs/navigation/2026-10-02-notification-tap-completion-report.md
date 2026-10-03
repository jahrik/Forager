# Completion report: the recording notification's tap no longer stacks a second copy of the screen (dispatch 2026-09-28-415)

**Status: built, pushed on `notification-tap`, and confirmed on the S22. Not merged. The planner merges.**

**Date:** 2026-10-02 (UTC).
**Dispatch:** `prompts/preserved/2026-10-02-02.md`, read on branch `records-after-151` at `4f364ed3`.
**Base:** `origin/main` at `3e82b5ca`, checked against the remote. The dispatch was written against `009d8951`; `main` had since taken a merge of records and a report.
**The owner's go:** to the planner, "Fix the fault now, I'll do off track alert first". To this session directly, asked because this is a new dispatch and not an amendment: "Yes, build it", and for the phone, "Yes, run the phone step".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-02-notification-tap/`.

## What changed for the user

With Forager open and a recording running: press Home, tap the recording notification. The screen you already had comes back, as you left it. Before, a second copy of the screen was put on top of the first, and Back from it landed on the older copy.

With Forager swiped away, the tap still opens the app, and the screen shows the recording.

## The diagnosis: confirmed, with one small correction

- **Confirmed (read).** The recording notification's tap was a `PendingIntent.getActivity` on `Intent(this, MainActivity::class.java)` with no flags (`service/TrackRecordingService.kt:229-234` at the base).
- **Confirmed (read).** `MainActivity` declares no launch mode (`AndroidManifest.xml:157-166`), so it is a standard activity.
- **Confirmed (read).** The backup notification's tap sets `FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP` (`data/backup/AndroidBackupNotifier.kt:65-70`).
- **Confirmed (searched).** Those are the only two `PendingIntent.getActivity` calls in `app/src/main`, and the only two places an intent for `MainActivity` is built.
- **Corrected.** The dispatch says `AndroidBackupNotifierTest` asserts on the backup notification's flags. It asserts the component and the extra, not the flags. The new test here does assert them.
- **Any other route to a second `MainActivity`:** none inside the app. `MainActivity` is exported, as a launcher activity has to be, so another app could start it with an explicit intent. Not looked into further.

## What landed

| Commit | What |
|---|---|
| `e89516d9` | The test, pushed failing. Nothing under `app/src/main` changed. |
| `c5c77acc` | The fix. |
| this commit | This report and its two index rows. |

**The fix is one intent:** `service/TrackRecordingService.kt`, in `buildNotification`. The intent behind the notification's tap now carries `FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP`, the same two the backup notification sets, with a comment saying why. Nothing else under `app/src/main` changed: not the manifest, not the notification's text, channel or Stop action, not `MainActivity`.

**`MainActivity.onNewIntent`** (`MainActivity.kt:290-294`) now receives this tap when the screen already exists. It keeps the intent and looks for the backup extra, which this intent does not carry, so it does nothing more. Read, and consistent with what the phone showed.

## The test, and what it failed with

`service/TrackRecordingNotificationTapTest` (new, in its own class so that `TrackRecordingServiceTest` is not edited): `a tap on the recording notification opens MainActivity asking for the existing screen, not a new copy on top`. It starts the real service through `onStartCommand`, takes the notification the service posted to `startForeground`, and reads the intent behind its tap: the component, and the two flags.

**At `e89516d9` it failed with:** `…Flags were 0x0 expected:<603979776> but was:<0>` (`t1-test-first`: 3 classes, 12 tests, 1 failure, 0 compile errors).

**What a flags test shows, said plainly:** what is asked of the platform, not what the platform does. Robolectric has no task stack to bring forward. Whether the phone then shows one screen is the phone step below.

## Revert check

`r1-no-flags`: the flags removed from that one intent, from a saved copy and not from git. The reverted build's log was read first: 0 compile errors. The test failed with the same message, `the tap must ask for the MainActivity that already exists (SINGLE_TOP and CLEAR_TOP)…`. Restored from the saved copy, and the working tree confirmed identical to `c5c77acc` afterwards.

## Suite counts

From the JUnit XML, results directory cleared first.

| Run | Commit | Result |
|---|---|---|
| Affected classes (`t2-built-targeted`) | the tree that became `c5c77acc` | 4 classes, 21 tests, 0 failures |
| Full unit suite (`t3-full-suite`) | `c5c77acc`, clean tree | 421 classes, 3449 tests, 0 failures, 0 errors, 24 skipped |
| `assembleDebug` (`t4-assemble`) | `c5c77acc` | 0 `e:` lines |

3448 plus the 1 new test is 3449. The 24 skipped are the same five existing classes. Both owner-held intermittent classes passed. No test was skipped, silenced or weakened.

## The phone step, on the S22

Evidence in `~/Zynergy/device-evidence/2026-10-02-notification-tap/phone/`. Upright before every tap and the swipe; every control found in a fresh dump.

**The build installed:** `1.0.2490+gc5c77acc`, versionCode 2490, sha256 `60719f6ccb58f399b45ba7df6407489eb2888976cadef4943c25346b17f35d65`, with `adb install -r` over the debug build already there. Nothing was wiped: `firstInstallTime` is unchanged.

| Step | What was done | Result | Evidence |
|---|---|---|---|
| Baseline, 19:08:57Z | Read-only | `1.0.2485+g7f45b99c` installed, Maps tab, no service | `step0-*` |
| Install, 19:09:14Z | `adb install -r` | "Success"; versionName `1.0.2490+gc5c77acc` | `step1-install.txt` |
| Launch | Launcher intent | Maps tab, one `MainActivity` (`t261`), pid 2496 | `step2-launched.*` |
| Record, 19:09:25Z | One tap on "Start recording track" | Service foreground, notification posted | `step3-record-tap.txt`, `step3-recording.*` |
| **The step.** Home 19:09:34Z; shade expanded; one tap on the body of Forager's notification, 19:09:43Z | | **Pass. One `MainActivity` record in the task** (`sz=1`), the same record as before the tap, and the screen shows "Stop recording track". On the build before the fix this was two. | `step4-shade.*`, `step5-body-tap.txt`, `step5-after-tap.*`, `step5-after-tap-activities.txt` |
| **The second observation,** run because the first passed. One swipe of Forager away from recents, 19:10:15Z; shade expanded; one tap on the notification's body, 19:10:27Z | | The task was gone after the swipe, with the service still foreground. The tap opened **one** screen (a new task, `t262`, `sz=1`) showing "Stop recording track". | `step6-swipe.txt`, `step6-recents.*`, `step6-swiped-*`, `step7-shade.*`, `step7-body-tap.txt`, `step7-after-tap.*` |
| Stop, 19:10:37Z | One tap on "Stop recording track" on the screen | Service not listed, notification gone; the screen offers "Start recording track" | `step8-stop-tap.txt`, `end-state.*` |

**The log,** read by the app's pid (`logcat-pid2496.txt`, 2,394 lines, with 206 `ForagerFix` lines as the control that the capture sees the app's own lines): no "Ignoring a start" line, and no line from `TrackRecordingService`, `TrackRecordingViewModel` or `EndAbandonedTracks`.

**End state, 19:10:45Z:** the fixed debug build `1.0.2490+gc5c77acc` on the phone; Forager in front on the Maps tab offering "Start recording track"; no service; no Forager notification; shade closed; upright. One more ended track is on the phone, started 12:09 PM on its clock.

## What is not shown

- **Back after the tap.** The dispatch's path ends "Back does what it did before the tap". The check does not allow Back, so that was not pressed. With one screen in the task there is no older copy for Back to land on; what Back then does is the app's ordinary handling. **Inferred.**
- **That the screen comes back "as you left it"** beyond what was read: it is the same activity record, and it showed the recording. A tab, a scroll position or an open sheet was not set up before the tap.
- **The tap from a screen other than the home screen,** or with another app in front.
- **An unplugged phone.**

## Disclosures

**Confirmed by observation:** the test's failure and pass; the revert check; the suite counts; the number of `MainActivity` records before and after the tap, from saved `dumpsys` output.

**Read:** the diagnosis, and `onNewIntent`.

**Inferred:** what Back does after the tap.

**Could not determine:** whether another app starting `MainActivity` directly could still stack a copy. Nothing in this app does.

**Premises that were wrong:** one, small: `AndroidBackupNotifierTest` does not assert the backup notification's flags.

**Decided beyond scope:** nothing. The second observation was run as the dispatch allows. No position is written here, and the screenshots stay in the local evidence folder.
