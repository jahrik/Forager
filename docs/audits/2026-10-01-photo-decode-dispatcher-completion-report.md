# Photo-decode dispatcher (dispatch 2026-09-28-351) — completion report

- **Coder model:** `claude-sonnet-5-5`, configured; the planner read it from the session record (configured and last served).
- **Base:** `origin/journal-redesign` at `e9dfa9db4de668e5f15d6357a1bc2d49ccd7521d` (head when started; re-fetched before the final merge step: unchanged, `git pull --no-rebase` said "Already up to date"). Branch `photo-decode-dispatcher`, pushed there only. Not merged.
- **Owner's ruling (verbatim, from the dispatch):** "Go with option C from your fix question", then "Fix 1": skip reproducing, fix, judge by CI.

**The fix is not proven against the CI failure.** It is proven only against the thread it removes: under the test default the decode, the state write's apply and the layout passes run on the main thread. CI judges it afterwards.

## 1. Premises (read from the tree at `e9dfa9db`)

- `DecodedPhoto.kt:66`: `bitmap = withContext(Dispatchers.IO) { ... }` inside `LaunchedEffect(relativePath)`, after -317's single-`Image` change. Confirmed as the dispatch described.
- Other `Dispatchers.IO` hops inside a composable or `LaunchedEffect` in `main/` (grep of `Dispatchers.(IO|Default)`), **report only, unchanged:** `ui/track/TrackExportPanel.kt:263` (`exportAndShareTrack`, a suspend function called from the panel), `ui/crash/CrashLogPanel.kt:153` (`LaunchedEffect(file)`), `ui/log/PhotoViewerDialog.kt:328` (`LaunchedEffect(relativePath)` in `ZoomablePhoto`). Every other hit is a repository, service, ViewModel parameter or `ForagerApplication` scope, not composable. **Consequence: the viewer is not covered by this fix** and keeps its IO resume under test; a test that opens the viewer can still, in principle, hit the same race. The planner takes this to the owner.
- Test-wide default mechanisms that existed: **no** `robolectric.properties` anywhere, no test `Application`, no shared JUnit rule (every class builds its own `createComposeRule`). `ForagerApplication` is `final` and is the real application under Robolectric. Robolectric 4.16.1 provides `org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin` (`onSetupApplicationState()`, called per test), found by `ServiceLoader`; `pluginapi` is a compile-scope dependency of `robolectric`.
- Tests that would deadlock under a main-thread decode: every user of `GatedBitmapFactoryShadow`/`GatedDecode`: `DecodedPhotoGestureTest`, `DecodedPhotoSemanticsTest`, `DecodedPhotoPlaceholderSizeTest` (all three call `GatedDecode.holdDecodes`; grep of `app/src/test`). `DecodedPhotoTest` and `JournalTabTest` (`waitUntil` at `:482`, `:500`) wait for a decode but do not block it; they would change meaning only if `waitUntil` did not idle the main looper. The full suite is the evidence they did not (section 6).

## 2. The seam, the test default, the opt-in

- **Seam:** `ui/log/PhotoDecodeDispatcher.kt`, an `internal object` with `current` (`override ?: Dispatchers.IO`, read at each decode) and `override(dispatcher?)`. `DecodedPhoto` uses `PhotoDecodeDispatcher.current`. Production never calls `override`. No public API, size, semantics or production timing changes. Rejected: a `CompositionLocal` or a parameter (a default for every existing test would mean editing every `setContent` or every caller), and a "running under Robolectric" check in production code.
- **Test default:** `PhotoDecodeTestLifecycle` (a `TestEnvironmentLifecyclePlugin`) registered in `app/src/test/resources/META-INF/services/org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin`; it installs `PhotoDecodeTestEnvironment.mainLooperDispatcher` before every Robolectric test, so it also undoes a previous test's opt-in. Rejected: a test `Application` named in `robolectric.properties` (would replace the real `ForagerApplication`, which is final).
- **Which dispatcher:** a small `CoroutineDispatcher` that `Handler(Looper.getMainLooper()).post`s the block. Not `Dispatchers.Main`/`.immediate`: 19 test files call `setMain` (grep), and a `StandardTestDispatcher` there would stop decodes silently. Posted rather than immediate so a test still sees the placeholder first, as it would with a real IO decode. Why it guarantees the resume thread: the harness's `FrameDeferredContinuation.resumeWith` resumes on whatever thread calls it and never examines the thread (-348 §1), and the decode body now runs on main, so the call comes from main.
- **Opt-in:** `PhotoDecodeTestEnvironment.useBackgroundDecode()` sets `Dispatchers.IO`. It is called from `GatedDecode.holdDecodes` (`GatedDecodeSupport.kt`), so the three gated classes are converted without editing a class. No other test is converted.
- **Open risk, checked:** whether a lifecycle plugin runs in the classloader that holds the app's classes (otherwise it would set a second copy of the object and do nothing). `PhotoDecodeThreadTest` red at `fdf6c0a2` and green at the tip shows it does.

## 3. What landed

| Commit | What |
|---|---|
| `fdf6c0a2` | seam, `DecodedPhoto` uses it, `PhotoDecodeThreadTest`, recording shadow, test environment (no default installed yet) |
| `ec7c87a0` | lifecycle plugin + service file, `holdDecodes` opts in |
| this report's commit | report, README row, evidence XML in `docs/audits/data/2026-10-01-photo-decode-dispatcher/` |

## 4. Tests and revert checks

`PhotoDecodeThreadTest`: (a) production default is `Dispatchers.IO`; (b) under the default the decode thread, every snapshot-apply thread (an apply observer registered in the test) and every layout-pass thread of the photo's modifier chain are `Looper.getMainLooper().thread`, with reachability asserts (one decode, apply notifications present, at least two layout passes); (c) after the opt-in, the decode thread is not main.

Every run: `e:` lines counted in the log (0 each time), XML timestamp read (all 2026-10-01T03:17 to 03:26Z, after each run began), reverts restored from copies saved first (`revert.sh` in the data directory), `git status` empty and `git diff` empty afterwards.

| Run | Result |
|---|---|
| Thread test at `fdf6c0a2` (no default) | **red for the right reason:** `the decode ran on [DefaultDispatcher-worker-3], not the main thread`; (a) and (c) pass (see below) |
| Tip | 3/3 green |
| Revert: service file removed | same message, `DefaultDispatcher-worker-3` |
| Revert: `useBackgroundDecode()` made a no-op | (c) fails: `the opt-in decoded on the main thread; it should be a background worker`; (a), (b) pass |
| Tip, `DecodedPhotoGestureTest` | 5/5 green |
| Revert -317 (pre-`851e28fd` `DecodedPhoto`, seam kept) | 5/5 fail: `The component with TestTag = 'photo-viewer' is not displayed!` (four taps) and `'tile-options-delete' is not displayed!` (the long-press): the lost-gesture failures, so the gate still places the swap mid-gesture |

Caveats: (a) and the opt-in test (c) pass at `fdf6c0a2` too, by design (nothing overrides, and a worker is the default there); (c) bites only against the default, which is why the no-op-opt-in revert exists. In the red run, (b) stopped at its first assertion (decode thread), so the apply/layout assertions were not shown to bite on their own; the decode-thread assertion is the one demonstrated red.

## 5. Suite and build

Full unit suite at the tip (`ec7c87a0`), XML saved before any other run: **406 classes, 3272 tests, 24 skipped, 0 failures, 0 errors** (summed from the XML, timestamps 03:22:41 to 03:26:40Z). The 24 skipped were not compared with the base; the repo has 51 `@Ignore`/`@Ignore(` text matches in `app/src/test`, unreliable as a count of skipped tests. `assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines.

## 6. The four disclosures

**Confirmed vs inferred.** *Confirmed (run):* the thread results above; the suite and build; that the plugin runs in the app's sandbox. *Inferred:* that this removes the CI flake (the mechanism is -349/-348's inference, never reproduced); that the harness resumes on the thread that calls it (bytecode reading, -348, not re-read by me); production safety (`AndroidUiDispatcher`, not run on a device).
**Could not determine.** Whether the flake rate drops (needs CI); whether any test relied on a decode landing while the main looper was busy (the suite is green, which covers only what the suite exercises); the 24 skipped tests' origin.
**Premises that were wrong.** None in the dispatch. The dispatch's list of tests "that might change meaning" (`DecodedPhotoTest`, `JournalTabTest`) did not: both stayed green unchanged.
**Decided beyond scope.** The opt-in lives in `GatedDecode.holdDecodes` rather than per class, to avoid editing three tests; `git branch --unset-upstream` was a no-op error (the new branch had none). Nothing else.

## 7. Device-only (nothing here was run on a device)

On a build from this branch, production decodes on `Dispatchers.IO` as before, so expect no change: photos still load in the album tiles, the find editor, the find report and the viewer, with no visible delay and no placeholder that stays. Not covered by a unit test: that the production default stays IO outside the tests (the test (a) covers the provider, not an installed build).
