# 2026-09-27: offline-maps picker snaps back to the device location, diagnosis

Read-only diagnosis by a pulse agent in the cloud planner session, at `origin/pre-main` `352b708` and `origin/journal-redesign` `36330db`. The files involved are identical at both refs. No build, no device. Condensed by the planner; "inferred" is the pulse's own marking.

**The owner's report, verbatim:** "A bug in offline maps: when panning the map to choose a region to download, upon release of the lan, it always returns the point to my location"

## Finding: no release-triggered recenter; two mechanisms that snap after a pan

The picker is `CentrePinLocationPicker` (`ui/map/CentrePinLocationPicker.kt:73-157`), called from `OfflineMapsPanel` (`ui/availability/AvailabilityOfflineMapsUi.kt:161-173`) with `pickerRegion = Region(pickedLat ?: defaultCenter.lat, pickedLng ?: defaultCenter.lng, radiusKm)` (:146-150) and `defaultCenter = uiState.offlineMapPickerDefaultCenter ?: OFFLINE_MAP_PICKER_DEFAULT_CENTER` (:129).

The pieces:
- `CentrePinLocationPicker.kt:113`: `var cameraCenter by remember(region) { ... }`. Any change to `region` resets the "Pin at:" point, and what OK confirms, to `region`'s centre.
- `ui/map/SightingsMap.kt:482-515`: the data and camera effect, keyed on `region`, moves the camera to `region`'s centre at `zoomForRadiusKm` whenever the camera is not tracking and the target differs from the last one applied (guard :759-763).
- Live location tracking is on for the picker's map (default `trackLiveLocation = true`, `MapSlot.kt:79`); on first activation it eases to the device at zoom 16 (`SightingsMap.kt:850-853`). A pan ends tracking (MapLibre 13.5.0 bytecode: `onMoveBegin` sets camera mode NONE when tracking-gestures management is off, which the app never turns on).
- `AvailabilityViewModel.onOfflineMapsOpened` (:765-773) fetches the device location and writes `offlineMapPickerDefaultCenter`; the fetch races GPS against network with a 20 s timeout (`AndroidLocationProvider.kt:30, 66-106`), so it lands 1-20 s after opening.
- Camera idle writes only the picker's local `cameraCenter` (`SightingsMap.kt:342-350`, picker :139); nothing feeds it back into `region`, so there is no idle-to-recenter loop.

**M1 (inferred, timing-dependent).** Open Offline maps (location fetch starts), the map flies to the device, the user pans (tracking ends), then the fix arrives: `defaultCenter` changes, `pickerRegion` changes, the pin resets and the camera snaps to the device at a radius zoom (9, 10.5, 12 or 13, `SightingsMap.kt:1303-1308`). Once per entry, only if the fix is slower than the first pan.

**M2 (definite from the code).** Any other change to `region` before OK, notably moving the radius slider (:199-207): the camera jumps to `defaultCenter` (the device after a fix) and the pin resets. Every time, but after a slider drag, not a pan.

**What separates them:** the zoom after the snap (a radius zoom for M1/M2; tracking would keep zoom 16), and timing (M1 only the first release within about 20 s of opening, never with location off; M2 only after the slider). If it truly snaps on every release, neither is it; the remaining suspect is `SightingsMap` being recreated with a fresh `MapView`, for which no code path was found.

## The find-location picker has the same bug, worse (inferred)

`JournalTab.kt:309-320` and `LogPanel.kt:276-278` pass `region = findLocationPickerRegion(deviceLocation, ...)` with `deviceLocation = uiState.liveFix`, which updates on every accepted fix, about once a second (`AndroidLocationTracker.kt:74, 103`). After a pan ends tracking, each new fix resets the pin and snaps the camera, so OK can confirm the device location instead of the panned point. Introduced by `2ed232cd` (2026-09-13). The main-map overlay pickers (plan trip, log a find here, drop waypoint, set search location) re-centre only on a search-region change or locate-me; not this bug.

## Opening Offline maps during a download deletes it (mechanism confirmed from code)

`MapLibreOfflineMapRepository.download()` registers the region with MapLibre at once (:109) and writes the Room row only on completion (:120-131). `listRegions()` deletes every MapLibre region whose status is not complete (:151, :172-176), and a region mid-download is not complete (`completedResourceCount < requiredResourceCount`, AAR bytecode). `listRegions()` runs on entering the Offline maps sub-tab (`AvailabilityViewModel.kt:772`), after another region's delete (:1143), at ViewModel init (:133), and from two use cases (`GetTripReportOfflineRegionsUseCase.kt:26`, `GetCartographyEntryOfflineRegionUseCase.kt:39`). On `journal-redesign`, returning to the Journal tab with the Offline maps chip still selected re-fires it too. What the user sees could not be determined (native behaviour on delete mid-download): a stuck "Downloading N / M tiles", or "Couldn't download offline maps."

## Tests

No test pans this picker with a real touch; map tests stub `MapSlot` and never compose `SightingsMap` (`SightingsMapOverlayDataTest.kt:28-32`, `CentrePinLocationPickerTest.kt:43-46`). A test that fails today at the real entry point (`OfflineMapsPanel`): hold `AvailabilityUiState` in state, use a stub `MapSlot` that records every `region` and turns a real swipe into an `onCameraIdle(P)`; then change `offlineMapPickerDefaultCenter` (M1) or drag the real slider (M2), and assert "Pin at:" still shows P, OK reports P, and the region handed to the map did not move.

## When

The combination has existed since `aa60f2d4` (2026-08-24), about five weeks; before it the offline picker used long-press and a re-centre did not move the picked point.
