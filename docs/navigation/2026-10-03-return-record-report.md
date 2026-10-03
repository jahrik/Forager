# A lasting record of Return taps and the off-track rule's decisions (dispatch 2026-09-28-451)

**Status: built and pushed on `return-record`, not merged. No phone.** Nothing a walker sees changed.

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-08.md`.
**Base:** `origin/main` at `cb035035` (PR #159, -440), checked against the remote. `main` at `bdb27f03` (PR #160, -457) was merged in at `8db6eb3c`, on the planner's word.
**The owner's words:**
- "Yes, start -451", to this session directly.
- On the delivery, "Report and catch", given in the planner's window and relayed by the planner.

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-return-record/`.

## In plain terms

1. The phone now keeps a short diary of each Return:
   - when it was tapped, or refused;
   - when it ended, and why;
   - when the off-track rule decided you had gone off, and when it re-armed;
   - whether the alert's notification was posted and the phone vibrated, and if not, why.
2. The diary is a small file in the app's own storage. The system log's rotation does not touch it, so the next walk can be read back afterwards.
3. It holds no positions: times, recording ids, decisions and reasons only.
4. **One change in behaviour, the owner's choice:** a failure inside the alert's notification or vibration no longer stops the recording from measuring. Before, an error there ended the fix collection for the rest of the walk. Now it is caught, logged, and written to the diary, and the walk goes on. What the alert shows and how it vibrates is unchanged.

## Verified before building (reported by message first)

1. **A file, not Room.**
   - Nothing joins these entries to tracks or filters by them. It is a flat diagnostic log that a person reads.
   - So CLAUDE.md's split puts it in a file: no migration, and no read path in the app.
   - The planner accepted this.
2. **Where.**
   - `filesDir/return-record.log`.
   - It outlives the process and log rotation. It is removed only by uninstalling the app or clearing its data.
   - In a debug build: `adb shell run-as com.zynergylabs.forager.app cat files/return-record.log`.
3. **An operating limit:** 512 KB.
   - Past it, the oldest half of the lines is dropped.
   - At about 70 bytes a line, that is several thousand entries. A recording writes a handful.
4. **Who writes.** The record is fed by `ReturnWatch`, the one place every Return and every decision already passes through.
   - `ReturnWatch`'s ownership, threading and lock are unchanged.
   - Each entry is decided under the lock and written after it, beside the alert's delivery, which was already outside the lock.
5. **The delivery question.**
   - The dispatch said "Do not touch… the alert's delivery". It also asked whether the delivery posted its notification and vibration, "or failed and why".
   - The delivery returned nothing, and an exception from it propagated out of `ReturnWatch.onFix` into the recording service's fix collection, which ended it.
   - I reported both by message. The owner chose "Report and catch", relayed by the planner:
     - `deliver` returns the outcome;
     - an exception from notify or vibrate is caught and reported, not thrown;
     - the exception is logged.
     - "Nothing else in delivery changes."

## What was built

- **`domain/ReturnRecord.kt`** (new):
  - The interface `ReturnRecord`, and `NoReturnRecord`, the default that keeps nothing.
  - The entries: `ReturnStarted`, `ReturnRefused` (names the recording the watch is begun for), `ReturnEnded` (with `BY_WALKER`, `RECORDING_STOPPED` or `SERVICE_DESTROYED`), `WentOffTrack` and `ReArmed` (each with its deciding reading's own time), and `AlertDelivered` (with its outcome, or none when the delivery cannot say).
- **`domain/ReturnWatch.kt`:**
  - Takes a `ReturnRecord`.
  - Writes an entry from `startReturn`, `stopReturn`, `end` (only when a return was under way) and `onFix`.
  - `onFix` delivers the alert through `deliverReporting`, then writes the outcome.
- **`domain/AlertDelivery.kt`:**
  - `deliverReporting` is a default method that delivers and returns `null`, so the existing test deliveries are unchanged.
  - Adds `AlertDeliveryOutcome`: notification posted or not, and why; vibration issued or not, and why.
- **`alert/AndroidAlertDelivery.kt`:**
  - `deliverReporting` posts the notification, then vibrates, each in its own `try`.
  - A denied POST_NOTIFICATIONS is reported as "POST_NOTIFICATIONS denied".
  - An exception is logged (tag `AlertDelivery`) and reported by its class name.
  - `deliver` calls it.
  - An internal constructor takes the post and vibrate functions as a seam for tests. The public one is unchanged for callers.
  - `postOffTrackNotification` and `postSundownNotification` now return whether they posted. What they post is unchanged.
- **`data/diagnostics/FileReturnRecord.kt`** (new): appends one line per entry, with its UTC time. It trims to the limit. A failed write is logged (tag `ReturnRecord`), never thrown.
- **`AppContainer.kt`:** builds the file record and hands it to the watch.

**The catch applies to every alert kind,** not only off-track: the sundown turnaround and sunset alerts go through the same `deliver`. The ruling named notify and vibrate without a kind, and one delivery serves all three. The record writes outcomes only for off-track alerts, since only the watch writes to it.

**A line looks like this** (from the tests):

```
2026-10-03T20:00:00.000Z return-started track=track-1
2026-10-03T20:00:00.000Z went-off-track track=track-1 reading=2026-10-03T20:00:17.000Z
2026-10-03T20:00:00.000Z alert-delivery track=track-1 notification=not-posted(SecurityException) vibration=done
2026-10-03T20:00:00.000Z return-ended track=track-1 reason=by-walker
```

## Tests

**Tests first, pushed failing, in two rounds:**
1. **The record** (`4cca32e5`, against stubs; run `t1-tests-first`): 8 tests, 7 failing.
   - The one that passed, "ending with no return under way writes nothing about a return", expects nothing to be written, which the stub also did. Revert w05 bites it.
2. **The delivery** (`a60351f3`, against a stub delivery that neither reported nor caught; run `t3-delivery-tests-first`): the 5 new `AlertDeliveryOutcomeTest` tests failed.
   - Two other new tests in that commit passed, because the code they test was written in the same commit: the record's delivery line, and the watch writing the outcome.
   - Reverts v05 and v04 bite them.

New, in three new classes; no existing test file changed:
- **`domain/ReturnWatchRecordTest`** (6), through the real `ReturnWatch`:
  - a Return writes an entry, and the walker ending it writes another;
  - stopping the recording, or the service being destroyed, ends a return with its reason;
  - ending with no return under way writes nothing about a return;
  - a refused Return is written as refused, naming the watched recording;
  - the delivery's outcome is written after the decision;
  - going off writes the decision with its reading's time, and coming back on writes re-armed.
- **`data/diagnostics/FileReturnRecordTest`** (4), on a real file:
  - each entry is one line with its UTC time, kind and track, and a second instance (standing in for a new process after the log rotated) appends to it;
  - the decisions carry their reading's time, and a refusal names the watched recording;
  - a delivery's outcome is one line, reported or not;
  - past the limit the oldest half is dropped and the newest kept.
- **`alert/AlertDeliveryOutcomeTest`** (5), Robolectric:
  - a delivered alert reports posted and vibrated, with POST_NOTIFICATIONS granted, since SDK 36 checks it;
  - a denied permission is reported, and the vibration still runs;
  - a throwing notify is caught and reported, and the vibration still runs;
  - a throwing vibration is caught and reported;
  - **a throwing notify no longer ends the fix collection:** the real `ReturnWatch` is fed six fixes through `onFix`, as the service does. The fourth goes off and its notify throws, the last is still measured, and the record has the outcome.

**Device-only:** the file on the phone, and reading it with `run-as`. Not tried; no phone step in this dispatch.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD after each check.

All 14 compiled with 0 errors and failed for their own edit.

| Check | One edit | Fails |
|---|---|---|
| w01 | a taken Return not written | the Return entry test; the going-off sequence (it starts with the Return) |
| w02 | a refusal not written | the refusal test |
| w03 | the walker's end not written | the Return entry test |
| w04 | every end written as "recording stopped" | the stop-or-destroyed test |
| w05 | an end written with no return under way | the "writes nothing" test |
| w06 | going off not written | the going-off sequence |
| w07 | re-armed never written | the going-off sequence |
| w08 | the file overwritten, not appended | the one-line-per-entry test; the decisions test |
| w09 | the trim keeps the oldest half | the limit test ("the oldest is gone") |
| v01 | the notify's catch narrowed to another exception | both throwing-notify tests, by the thrown `SecurityException` |
| v02 | the vibration's catch narrowed | the throwing-vibration test, by the thrown `IllegalStateException` |
| v03 | the denial not reported | the denied-permission test |
| v04 | the outcome not written by the watch | the watch's outcome test; the going-off sequence; the collection test ("List is empty", as it reads the outcome from the record) |
| v05 | the outcome dropped from the record's line | the delivery-line test |

## Suite

| Run | Tree | Result |
|---|---|---|
| Record, tests first (`t1-tests-first`) | `cb035035` + `4cca32e5` | 2 classes, 8 tests, 7 failures, as expected |
| Record, built (`t2-built`) | before `b7eb1c74` | 3 classes, 33 tests, 0 failures |
| Delivery, tests first (`t3-delivery-tests-first`) | `a60351f3` | 4 classes, 21 tests, 5 failures, as expected |
| Delivery, built (`t4-delivery-built`) | before `ffddca3f` | 5 classes, 46 tests, 0 failures |
| Full suite (`t5-full-suite`) | `ffddca3f` | 429 classes, 3532 tests, 0 failures, 0 errors, 24 skipped |
| Full suite, main merged (`t6-full-suite-merged`) | `8db6eb3c` | 430 classes, 3537 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled:**
- `t6`: -457's own full suite on its merged tree was 3,522 tests. This branch adds 6 + 4 + 5 = 15 in three new classes, which gives 3,537. `git diff --name-status origin/main..HEAD -- app/src/test` lists only those three files, all added.
- `t5`: -440's base of 3,517, plus 15, is 3,532.

## Disclosure

**Confirmed vs inferred.**
- Confirmed by tests: what each entry carries; that the file appends across instances and trims to its limit; and that a throwing notify or vibration is caught, reported and logged, and the collection goes on.
- Inferred, not tried: that the file survives on the phone and reads with `run-as` in a debug build. That is Android's documented behaviour for `filesDir` and debuggable builds, but no phone was used.
- Also inferred: that a failed delivery used to end the collection on the phone. That rests on reading `ReturnWatch.onFix` and the service's collector, not on a crash seen there.

**Could not determine.** Whether any delivery has ever thrown on a phone. Nothing recorded it before this record.

**Premises that were wrong.**
- The dispatch's "Do not touch… the alert's delivery" could not stand beside "whether the alert's delivery posted its notification and vibration, or failed and why": the delivery could not say.
- Reported before building, and resolved by the owner's "Report and catch".

**Decided beyond scope.**
1. **The catch covers the sundown alerts too.** They share the one delivery.
2. **No distances.** The dispatch allowed them. The entries carry times, ids, decisions and reasons, and a distance would add nothing the decision does not already say.
3. **The end reason distinguishes the service being destroyed from the recording being stopped**, because `end` is called for both and the record would otherwise blur them.
4. **The test seam.** `AndroidAlertDelivery` gained an internal constructor taking the post and vibrate functions. Without it a throwing notify cannot be produced in a test.
