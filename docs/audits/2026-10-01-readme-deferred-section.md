# README section, deferred for the owner's review (removed from the README)

This was the last section of `README.md` as merged at `65a3436b` (dispatch 2026-09-28-379). It was removed from the README at the owner's word, 2026-10-01: "Remove the what is not verified area and move it. Some of it contains items I may consider features that incorrectly reports it as an issue. So defer that for now."

**It is deferred for the owner's review and is not a list of faults.** Some of its items may be intended behaviour. Nothing in it is to be acted on or cited as an issue until the owner has gone through it. The eight items below are exactly as they were in the README; none has been edited. The README does not link to this file.

---

## What is not verified, or is known to be limited

- Device checks on an S22 Ultra are recorded in `docs/audits/` and `RECORD.md`. A green
  unit-test run shows nothing that depends on real window insets, a real `MapView` or a moving phone;
  `CLAUDE.md` lists these.
- That a fan stays open while the map follows a moving location is covered by unit tests and by device runs
  and the owner's own walk, recorded in `docs/audits/2026-10-01-fan-holds-completion-report.md`.
- There is no real forecast data source (above). The species ranking is history only.
- A known issue, not fixed in this release: a bubble over an open fan is placed again for its record's own position
  when the map settles, which for a fanned record is the middle of the stack, so it can sit there and not beside its
  icon. Seen on the S22 on the return from a find's page; expected, not observed, while the map follows the location.
- Not run on a phone: tapping another stack with a bubble showing, and Back from a journal entry opened from a
  "kept in" line. Both are held by unit and screen tests.
- A photo taken with the camera has its metadata stripped except its orientation (`photo/FilePhotoStore.kt`);
  a photo imported from the gallery is stored as it came.
- Offline maps come from the project's own tile worker; its status is in `server/pmtiles-worker/README.md`.
- Some documents under `data/` and `docs/` still describe things as not yet built that have since been built
  (for example `data/species-index/README.md`); the report that goes with this README lists the ones found.
