# s22-fails completion report (dispatch 2026-09-28-318): DRAFT, in progress

Base: `origin/journal-redesign` at `1a43ba5c`. Branch `s22-fails`. Coder session ran as Sonnet 5.5 (`claude-sonnet-5-5`, from the session configuration; not read back from the session).

## Production seams added by `44b85497` (requested by the planner)

All are behaviour-neutral at that commit; the fix commits change them.

| Seam | Where | Why needed | Neutral because |
|---|---|---|---|
| `puckReplacementOptions(alreadyInitialised, options)` | `SightingsMap.kt:1455` | Fail 3 needs a pure function that says what to apply before the real options on a swap, so the SDK's placement decision can be asked about it headless. | Returns `null`, so the `?.let` at `SightingsMap.kt:1504` applies nothing. |
| `val options = liveLocationComponentOptions(context)` hoisted in `activateLiveLocationIfPermitted` | `SightingsMap.kt:1503` | The same options object feeds the seam and the activation. | The activation receives the identical options as before. |
| `drawOrder` parameter on `fanFrameCollections` | `FanOutLayers.kt:188` (call site `SightingsMap.kt:807`) | Fail 5's key is a layer's place in the live draw order. | Defaulted to `MAP_LAYER_REGISTRY`, so `FanOutLayersTest`'s 4-argument calls compile unchanged; unused at that commit. |
| `FanOutIds.SORT_KEY_PROPERTY` | `FanOutLayers.kt:67` | Names the feature property the sort key is written to. | A constant nothing reads at that commit. |
| `fanIconLayerProperties()` | `FanOutLayers.kt:106` (used at `:99`) | A `SymbolLayer` cannot be built under Robolectric, a `PropertyValue` can, so the test reads the layer's properties from here. | The same four properties, moved out of the local `symbols` helper, in the same order. |

Test-only: `app/src/test/java/org/maplibre/android/location/PuckPlacementProbe.java`, in the SDK's package because `LocationComponentPositionManager.update` is package-private.

Not yet verified: that this commit compiles and that its two new test classes fail as predicted. That needs the Gradle slot.
