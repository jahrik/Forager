# Topo-night tests (dispatch 2026-09-28-336): completion report

Coder model: `claude-sonnet-5-5`. Base: `origin/journal-redesign` at `1a43ba5c` (the dispatch named `9d200b03` or later; `git merge-base --is-ancestor` confirms it is an ancestor). Worktree `forager-wt/topo-night-tests`, branch `topo-night-tests`. **No production code and no test file changed; the deliverable is two findings.**

## Gap 1: `SightingsMap` passes `nightMode` to the caption

**Finding: not testable without a production change. The owner ruled option C: no code change; known untested wiring, device-only.** Owner, verbatim, relayed by the planner: "C".

Evidence it cannot be a unit test:
- `SightingsMap.kt:293-297` builds `MapView(context).apply { onCreate(null) }` inside `remember`, unconditionally, before the caption at `:1012` is reached.
- Three tests record that constructing MapLibre's native-backed objects throws `UnsatisfiedLinkError` off a device, Robolectric included: `SightingsMapOverlayDataTest.kt:37`, `BasemapStyleTest.kt:29`, `MapLibreInitializerTest.kt:17`.
- `git grep "SightingsMap("` in `app/src/test` and `app/src/androidTest` finds no caller; every screen test composes a stub slot. (Read, not run: I did not attempt to compose the real `SightingsMap` under Gradle.)
- The options put to the planner and owner: A, extract the caption into its own composable (production change; the revert at `SightingsMap`'s call to it would still pass, so the gap moves up a level); B, a `MapView` seam (looks infeasible); C, accept as device-only. C chosen.

Device evidence so far: terminal -333 records "The owner's device check saw the caption" on the build installed by device-note -332 (1.0.2198+gd072859b, S22). Device-note -332 itself only records the install; the sighting is in -333.

No new test, so no revert run for this gap.

## Gap 2: the crossfade's own test

**Finding: nothing is uncovered. No test added.**

The hard-switch variant: `BasemapStyles.kt`'s `nightStyleJsonWithBasemapBelow` with the Street layer's `maxzoom` at `NIGHT_FADE_START_ZOOM` (9.5) and the topo layer's paint the plain `NIGHT_RASTER_PAINT` (no `raster-opacity`); the same shape as `b1fc980d`'s, made as a two-line edit of today's file rather than a checkout of the old file (see disclosure 1).

Revert run (saved copy `/tmp/tn336/BasemapStyles.kt.forward`, sha256 `b8321b06c62b4b74...`; reverted file applied by copy; `./gradlew :app:testDebugUnitTest --tests '*TopoNightStreetSwitchTest'`):
- Build log: 0 `e: ` lines (and the XML dir was deleted first, so the XML cannot be a stale run).
- 13 run, **2 failed**, 11 passed.
- `night topo is the Street layer under the topo layer, overlapping 9_5 to 9_7, V1 on both`: `street layer maxzoom: it stays drawn until the fade has finished expected:<9.7> but was:<9.5>`.
- `which layers the style draws on each side of the crossfade`: `at 9.5 both, Street under expected:<[basemap-street, basemap]> but was:<[basemap]>`.
- Both messages are ones only this edit produces. Passing on the hard switch: `across the crossfade the blended map stays dark, pixel for pixel`, `at every sampled map zoom ... one layer`, `the fading topo layer is never drawn on tile level 10 or shallower`, the opacity-evaluator test and the rest. That confirms -310's note that the pixelwise blend test cannot tell the two designs apart.
- Forward state restored by copying the saved file back: sha256 identical, `git status` clean afterwards.

Why that is not a gap: "between 9.5 and 9.7 both layers contribute" is held twice, by the document test (maxzoom 9.7, topo minzoom 9.5, `raster-opacity` interpolate 0 at 9.5 to 1 at 9.7, opacity values at 9.4, 9.5, 9.6, 9.7, 12.0) and by the which-layers test (both drawn at 9.5 and 9.699, one at 9.499 and 9.7). The blend test is not a fade test and does not claim to be.

## Suite

Forward state (`1a43ba5c`, unchanged tree), `./gradlew :app:testDebugUnitTest`, 0 `e: ` lines, counts summed from the JUnit XML: **396 files, 3225 tests, 0 failures, 0 errors, 24 skipped.** The album flakes and `DiagnosticsPanelTest` did not fail in this run; that is not evidence they are fixed. XML: `docs/audits/data/2026-09-30-topo-night-tests/full-suite-xml.tgz`; the revert run's class XML beside it.

## The four disclosures

**Confirmed vs inferred.** Confirmed by run: the two failing messages, the 11 passes, the 0 compile errors, the restored hash, the suite counts. Read, not run: that no test composes the real `SightingsMap`, and that `MapView` construction fails under Robolectric (taken from three test comments and `:293-297`).

**Could not determine.** Whether option B (a `MapView` seam) is feasible; not tried. Whether the owner's device check looked at the caption itself rather than only the crossfade: -333 says it did, I have no other source.

**Premises that were wrong.**
- The dispatch's base `9d200b03` was stale; the head was `1a43ba5c`. Not a problem, recorded.
- The dispatch's "the owner's device check saw both credits" and the planner's pointer to device-note -332: -332 is the install note; the sighting is stated in -333.
- The dispatch's gap 1 test ("through the real `SightingsMap`") could not be written as specified, hence the stop.

**Decided beyond scope.** I made the hard-switch variant by editing today's file (two lines) instead of restoring `b1fc980d`'s whole `BasemapStyles.kt`, because that file has since gained the `nightMode` credits and fade constants; the failing behaviour is the same, but the variant is mine, not the old commit's literal text. Nothing else.
