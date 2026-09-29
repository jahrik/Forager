# Photo export and journal backup: premise pulse (read-only, at `a7343f5`)

**Date:** 2026-09-29. **Read at:** `origin/journal-redesign` `a7343f5`, through `git show` and `git grep`. No build, no device.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with citations kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless stated. Labels: [read] code or docs, [observed] a git command's output, [inferred] reasoning.

**Why:** the owner, verbatim: "Let's also add an export option for photos to the device, and on-device backup for journal entries."

## Headline: the owner already ruled on this, 2026-09-09

- `android:allowBackup="false"` is permanent. Restore is to be "handled by an in-app export/import the project builds itself" (`AndroidManifest.xml:79-82`, `:134`; `docs/audits/2026-09-09-backup-ruling-superseded-alternatives.md:10-17`) [read].
- The owner's reasoning then: "Google sells cloud backup as a service… The export stays local unless the user chooses to move it" (`AndroidManifest.xml:97-100`; ruling doc `:14-17`) [read].
- **None of it is built.** There is no export, import, backup, restore, zip or database copy in `app/src/main` [observed]. The manifest says so at `:112-114`.
- **Still open from that ruling:** how a user on a new phone finds the import (ruling doc `:117-120`).

## Photos

- **Storage:** app-private `filesDir/photos/<uuid>.jpg` (`photo/FilePhotoStore.kt:100,119-120,142,191`). Never in the cache or MediaStore. `relativePath` is stored relative to `filesDir` (`:22-23`) [read].
- **Every file is named `.jpg`** whatever its real type (`:120`), and the picker may return HEIC or PNG (`ui/log/PhotoAcquisitionLaunchers.kt:139`). An export cannot trust the extension for the MIME type [inferred].
- **Capture:** CameraX writes to `filesDir/captures/`, then persist copies the file into `photos/` and deletes the scratch file (`photo/CameraCaptureFiles.kt:48-53`; `FilePhotoStore.kt:102-110,121-123`) [read].
- **Import:** the system photo picker, `PickMultipleVisualMedia`, which needs no permission. `ACCESS_MEDIA_LOCATION` is requested on API 29+ (`PhotoAcquisitionLaunchers.kt:105-117,133-138`) [read].
- **Never written to the shared gallery:** no MediaStore insert, no SAF [observed].
- **EXIF:**
  - Captures are scrubbed to orientation, ICC and Adobe only (`photo/PhotoMetadataScrub.kt:10-11,50-60,74-107`). If the scrub fails, a WARN is logged (`:66-72,110`).
  - A capture's location lives only in `log_photos.latitude/longitude`, gated by "Automatically Save Location to Photos" (`domain/PhotoLocationPreferenceRepository.kt:3-32`).
  - Imports are deliberately untouched (`FilePhotoStore.kt:125-132`, owner 2026-09-14). On API 29+ the platform redacts GPS from the byte copy; on 26-28 GPS can remain (`:36-42,59-70`) [read].
  - **An exported photo today** [inferred]: a capture carries no GPS. An API 29+ import carries no GPS but keeps its other EXIF. An API 26-28 import can carry GPS. Putting a location in an export would mean writing it back from the database.
- **References:**
  - `log_photos` rows (`data/local/LogPhotoEntity.kt:19-28`);
  - finds, through `log_entry_photos` (`:48-56`);
  - entries, through `cartography_entry_photo_refs` (`CartographyEntryEntity.kt:166-175`).

  There is no `@ForeignKey` (`LogPhotoEntity.kt:33-38`) [read].

## Existing share and export

- **FileProvider** at `${applicationId}.fileprovider` (manifest `:176-184`). Release paths are `captures/`, `external-files/crashes/` and `cache/tracks/` (`res/xml/file_paths.xml:21-23`). `photos/` is not exposed in release (`:4-6`); debug adds it (`app/src/debug/res/xml/file_paths.xml:26-28`) [read].
- **Share intents:**
  - the GPX share, `ui/track/TrackExportPanel.kt:215-245` (from Records' tracks and the details sheet, `ui/log/RecordDetailsSheet.kt:264-268`);
  - the crash-report share, `ui/crash/CrashLogPanel.kt:200-206`;
  - a debug-only Diagnostics share of photos and logs.

  **There is no photo share and no journal export in release** [observed].
- **GPX:** written to `cacheDir/tracks/…gpx`, named by the start time, so a second export overwrites the first. It carries the filtered track, that track's waypoints and the full raw record (`export/TrackGpxExporter.kt:21-67,78`). It is shared out, never saved. There is no GPX import: `GpxCodec.decode` has no caller [read, observed].
- **Auto-backup** is off: `allowBackup=false`, with no extraction or full-backup rules (`res/xml/` holds only `file_paths.xml`) [read, observed].
- **"Import" is already the photo-picker button's label** (`ui/log/PhotoGalleryScreen.kt:115` and four more places). A backup "Import" would collide with it [inferred].

## What a journal backup would hold

- **`forager.db`**, version 16, 15 entities (`data/local/ForagerDatabase.kt:149-169`):
  - entries and the five ref tables with their snapshots (`CartographyEntryEntity.kt:19-175`);
  - finds, including drafts (`MushroomLogEntryEntity.kt:42-177`), and `log_entry_photos`;
  - `log_photos` plus the files in `photos/`;
  - `tracks` and `track_points`;
  - `waypoints`;
  - `offline_regions`, whose key is MapLibre's native region id (`OfflineRegionEntity.kt:13-17,30`). Rows restored without MapLibre's store point at regions that do not exist [inferred].
- **Tiles** are in `files/mbgl-offline.db`, observed on devices (`docs/audits/2026-09-28-offline-regions-startup-pulse.md`). The code believes they are in `filesDir/maplibre-offline` (`map/MapLibreStorage.kt:57`). That file also holds the ambient tile cache.
- **Also in the database but not journal data:** `planned_trips`, and `cached_searches` (which can be rebuilt). `fungi_index.db` is an asset copy.
- **Migrations** `3_4` to `15_16` are registered; schemas 4 to 16 are exported. The destructive fallback is debug-only (`ForagerDatabase.kt:130-147,196-205`). Restoring a backup newer than the app needs its own handling [inferred].
- **WAL:** there is no `setJournalMode`, so Room's default, WAL, applies [inferred]. A plain file copy risks a torn snapshot, as the manifest notes (`:91-95`).
- **DataStore:** seven files (map, distance unit, theme, sundown, photo location, camera grid, camera orientation; `data/repository/DataStore*Repository.kt`) [read].

## Permissions and platform

- `minSdk` 26, `targetSdk`/`compileSdk` 37 (`app/build.gradle.kts:226,248-249`) [read].
- **No storage or media permission is declared** [observed].
- **Shared storage** [inferred, platform knowledge]:
  - A MediaStore insert into Pictures/ needs no permission on 29+, but needs WRITE_EXTERNAL_STORAGE (max 28) on 26-28, which is not declared.
  - The Storage Access Framework (create document, open tree) needs no permission at any level, and lets the user choose a folder or a cloud provider.

## Records that bear on it

- **Rulings:**
  - 2026-09-09, the backup ruling above (rulings 1 and 2 superseded, ruling doc `:19-38`);
  - 2026-09-14, imports untouched and captures scrubbed;
  - 2026-09-14, one location flag;
  - an older plan's "If and when export happens" (`docs/plans/journal-trips-and-offline-regions.md:208-233`): tracks never leave the device, find coordinates are obscured. The shipped GPX share already contradicts that.
- **User documents to update when export/import ships:** `docs/legal/privacy-policy.md:39-51`, `docs/beta/README.md:54-62`, `docs/legal/delete-data.md:24-28`.
- **Stale claims at this sha:**
  - The privacy policy `:114`, `:120-122` and `:143-144`, and beta README `:49-52`, on EXIF, the camera app and camera permission, predate the 2026-09-14 in-app camera and scrub.
  - The data inventory `:39`, `:428`, `:430`, `:431`, `:433` and `:486`.
  - The manifest `:93-94` counts migrations and schemas.
  - `file_paths.xml:17`.
  - `MapLibreStorage.kt:10-19`.

## Tensions for the owner [inferred]

- A backup in app-private storage dies with an uninstall.
- A backup in shared storage, or through SAF, survives uninstall. That contradicts `delete-data.md:24-25` and `privacy-policy.md:169-170` unless they change.
- "On-device backup" and the 2026-09-09 "export you trigger, the file goes where you send it" are compatible but not the same thing. Manual export, a scheduled on-device copy, or both, is unmade.

## Could not determine

- Whether `setRequireOriginal` works on photo-picker URIs. Imports may silently get no coordinate.
- What the picker leaves in an import's bytes on 29+.
- The real size of a journal's photos.
- Whether copying `mbgl-offline.db` between devices is safe.
- The S26's store location.
- Whether the S22 has `files/datastore/`.
- Whether the GPX and crash shares handle "no receiving app".
