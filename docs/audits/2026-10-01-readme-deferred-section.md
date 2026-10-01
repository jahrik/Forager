# README section, deferred for the owner's review (removed from the README)

This was the last section of `README.md` as merged at `65a3436b` (dispatch 2026-09-28-379). It was removed from the README at the owner's word, 2026-10-01: "Remove the what is not verified area and move it. Some of it contains items I may consider features that incorrectly reports it as an issue. So defer that for now."

**It is deferred for the owner's review and is not a list of faults.** Some of its items may be intended behaviour. Nothing in it is to be acted on or cited as an issue until the owner has gone through it. The eight items below are exactly as they were in the README; none of them has been edited. Three items have been added below them, under their own dated heading, at the owner's word. The README does not link to this file.

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

## Added at the owner's word, 2026-10-01

These three are not part of the README section above. They are work the owner wants recorded as to do, after the release. Nothing in them has been started: no code has been removed, no survey of the codebase has been made, and no bug list has been written.

- **The README is severely stale and needs to be checked against the actual code.** The owner: "Readme needs to be checked against actual code sometime. Add to the known issues section that the readme is severely stale". What is known so far: the Search bullet named Plants and Lichens as categories to pick; both are defined in the code (`domain/model/TaxonFilter.kt:61` and `:70`) and the search does not offer them (the owner: "We only do Fungi now. Plants and Lichen were removed"). The claims table in `docs/audits/2026-10-01-readme-refresh-completion-report.md` tied each statement to where its words appear in the code, not to where a user reaches them, so it did not catch this. The Search bullet is corrected; the rest of the README has not been checked for reachability.
- **The codebase needs an overhaul, including removing the dead code that is not functional.** The owner: "the codebase needs an overhaul and remove all strings of dead code that aren't functional, and identify and create a bug list". **This is a starting list, not a survey.** Examples known so far: `TaxonFilter.PLANTS` (`domain/model/TaxonFilter.kt:61`) and `TaxonFilter.LICHENS` (`:70`), defined and not offered, with no use under `app/src/main` outside comments (search saved at `~/Zynergy/device-evidence/2026-10-01-readme-reachability/caller-search.txt`); the fan's dots source and layer (`ui/map/FanOutLayers.kt:98` for the layer, and the `SIGHTINGS` branch at `:233`), which serve a case that cannot occur today because a fan cannot hold a sighting (`docs/audits/2026-10-01-fan-flicker-completion-report.md`, the "Sightings" item). The reachability pass that was stopped had established nothing beyond the first of these.
- **A bug list is to be identified and created.** The owner: "identify and create a bug list". None exists yet as one document. Where today's known items are recorded: the eight items above this heading (deferred for the owner's review, not a list of faults); `RECORD.md`'s terminal entry 2026-09-28-390 (the bubble's placement over an open fan, as a known issue); and the addendum at the end of `docs/audits/2026-10-01-bubble-paths-completion-report.md` (the phone case it was seen in).
