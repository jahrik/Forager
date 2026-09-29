# Privacy and data-sharing changes for the site (planner, 2026-09-29)

The owner asked, verbatim: "Make a report for all privacy and data sharing changes for me to update the site with".

**What this compares.** The live pages are `privacy/index.html` and `delete-data/index.html` on zynergy-site `origin/main` (`0688e4d`). Both say "Last updated: 11 September 2026". They are compared against the app's code:
- **`main`:** `faf2f88f`, 2026-09-26.
- **`journal-redesign`:** `44aba8f6`, the head this report was written on.

Each item says which of the two carries it. "On `main`" means it is already in the app today, so the page is already out of date there. "Journal PR" means it ships when `journal-redesign` merges.

**How it was checked.** Everything here was read from code at those commits, with file and line given, except where marked *unverified*. Suggested wording is a draft for the owner to edit. Nothing on the site has been changed.

---

## A. Already wrong today (on `main`)

### A1. "There is no export or import feature yet"
- **Where on the site:** privacy, "Android's own backup is switched off", the note at its end.
- **What the code does:** tracks export as GPX files through Android's share sheet.
  - Code: `TrackExportPanel.kt`, with `shareGpxIntent` using `ACTION_SEND`.
  - Caller on `main`: `RecordsTab.kt:237`, Records → Recorded tracks.
  - The user picks where each file goes, for example Files, email or another app.
- **Suggested:** "You can export a recorded track as a GPX file from Records → Recorded tracks. Android's share sheet sends it wherever you choose. Once shared, that copy is outside Forager, and it contains the track's coordinates and times."

### A2. "Photos and location metadata": the in-app camera paragraph
- **What the site says:** "Forager does not run an EXIF-stripping step" and "the camera app writes into a file in Forager's own directory … whatever GPS tags the camera app wrote are in the stored file."
- **What the code does:**
  - Photos taken in the app use Forager's own camera, not an outside camera app (`CameraCapturePhotoSource.kt:30`).
  - Each capture is stripped of all metadata except orientation, by an allowlist (`FilePhotoStore.kt:132`, which calls `scrubPhotoMetadata`; the design is in `PhotoMetadataScrub.kt:10-60`).
  - Only three segment kinds are kept: JFIF, the ICC colour profile, and the orientation. GPS, the camera model, times, thumbnails, XMP and IPTC are all removed.
  - This is a JPEG-only step. In-app captures are JPEG.
- **Suggested:** "Photos taken with Forager's camera are stripped of all embedded metadata when they are saved: location, camera details, timestamps, thumbnails, everything. Only the orientation is put back, so the photo displays the right way up. The coordinate of a find is kept in the app's database, not in the photo."

### A3. "Photos and location metadata": imports
This is still broadly right, but needs sharpening:
- Imports are **not** stripped, by the owner's ruling (see B2). The stored copy is a byte copy of what the picker hands over (`FilePhotoStore.kt:37-65`).
- On Android 10 and later, the platform removes GPS from that copy unless the app opts in. Forager does not opt in for the copy.
- On Android 8–9 there is no such removal, so an import can keep its GPS.
- Forager separately reads an import's original date and location, using `ACCESS_MEDIA_LOCATION` and `setRequireOriginal` (`FilePhotoStore.kt:160-167`). It does this to date and place the find. That reading goes into the app's database only.

The existing text is roughly this. Keep it, and add B2.

### A4. Permissions: "Camera: taking a photo for a journal entry"
Still true. Only the context changed: it is now Forager's own camera (A2). No wording change is needed, unless the page elsewhere mentions a camera app.

---

## B. Ships with the Journal PR (`journal-redesign` only)

### B1. Save to Gallery (photo export)
- **Built:** `edb74209`, `photo/PhotoExporter.kt`.
- **What it does:** on the photo viewer, the user can save a copy of a photo.
  - **Android 10 and later:** into the phone's Gallery, in a "Forager" album (Pictures/Forager).
  - **Android 8–9:** into a folder the user picks.
- **What the saved copy contains:**
  - The photo's time is written as the Gallery's "date taken".
  - No location is ever written by Forager.
- **Permissions:** none added.
- **Consequence for the site:** a saved copy is outside Forager's private storage. Other apps with photo access can read it, and uninstalling Forager does not remove it.

### B2. Imported photos keep their own metadata when saved to the Gallery
- **The owner's ruling, verbatim:** "Imported photos taken outside the app are not within our scope. They can use a scrubbing app to remove it if they want it removed. All photos taken inside the app are scrubbed either way and that's our scope". Recorded in `RECORD.md` 2026-09-28-131 and in the plan.
- **What the code does:** the saved copy of an import is byte-for-byte the stored file. So if the import still had GPS (Android 8–9, see A3), the Gallery copy has it too.
- **Suggested:** "Saving a photo you took with Forager's camera gives a copy with no location. Saving a photo you imported gives an exact copy of what was imported. If that photo carried a location or other details, so does the copy. Use a metadata-removal app if you want them gone."

### B3. On-device journal backup and restore
Built at `fbdaf0c7`. Rulings are still being applied, record 2026-09-28-133.

- **What a backup holds.** It is one `.zip` file containing a database snapshot and the photo files:
  - the journal: entries, finds and their coordinates;
  - photos;
  - tracks with every GPS point;
  - waypoints;
  - offline-region details (not the map tiles);
  - planned trips, by the owner's ruling of 2026-09-29.
  - Settings are **not** included.
- **Where it goes:** a file the user saves where they choose, through Android's file picker. That could be the phone, an SD card, or a cloud folder if the user picks one.
  - **Forager never uploads it.** A cloud provider the user picks receives it through Android's picker, under that provider's terms.
- **It is not encrypted.** No encryption code exists in `data/backup/`. Anyone with the file can read the journal, photos and GPS tracks in it.
- **Scheduled backup:** daily, weekly or monthly, into a folder the user picks. It is **off by default**, and only the user can turn it on. Old backup files are never deleted by the app.
- **Restore** reads a backup file the user picks, either replacing the phone's journal or merging with it.
- **Suggested wording, core:** "You can back up your journal to a file and restore it later, on this phone or a new one. The backup contains your entries, finds and their locations, photos, recorded tracks with every GPS point, waypoints and planned trips. You choose where it is saved. Forager does not upload it, and it is not encrypted, so keep it somewhere you trust. If you save it to a cloud folder, that service holds a copy under its own terms. Scheduled backups are off unless you turn them on."

### B4. New permissions, pulled in by the scheduling library
Forager's own manifest is unchanged between `main` and `journal-redesign`, and the permission lists are identical. But the **merged** manifest adds permissions from a library.

- **Checked in:** the merged debug manifest built at `journal-redesign` (`app/build/intermediates/merged_manifest/debug/.../AndroidManifest.xml`), which lists:
  - `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`;
  - a `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` that is internal to the app, so it needs no text.
- **Where they come from:**
  - The backup coder read the first three from WorkManager's library (`androidx.work` 2.12.0).
  - **`ACCESS_WIFI_STATE`'s source was not traced.** It may already be in `main`'s build, from MapLibre. I did not build `main` to compare. *Unverified.*
- **Suggested lines for the permissions list:**
  - "Run at startup, keep awake: used only so a scheduled backup you turned on can run at its time, including after a restart. With scheduled backup off, they do nothing."
  - "Network state: read by the scheduling and map libraries to know whether the phone is online. It is not used to send anything."

### B5. Deletion: "Nothing is left behind" stops being true
- **On the delete-data page:**
  - "Uninstalling removes everything … Nothing is left behind on the device."
  - "There is no cloud copy to survive the uninstall."
- **On the privacy page:** "What stays on your device … It is deleted when you uninstall Forager or clear its data."
- **Why they stop being true:** backup files (B3) and Gallery copies (B1) are ordinary files outside the app. They survive uninstalling and clearing data.
- **Suggested addition to delete-data:** "Uninstalling does not remove files you saved outside the app: backup files, and photos you saved to your Gallery. Delete those in your Files or Gallery app. Scheduled backups write a new file each time and never delete old ones, so check the folder you chose."
- **Keep:** `allowBackup="false"` is unchanged (`AndroidManifest.xml:134`). There is still no Google automatic backup. The new backup is one the user makes deliberately.

### B6. Wording that stays true, for reassurance
- **Recipients unchanged:** the network hosts in the code are the same as the site's table: iNaturalist, Open-Meteo and its archive, OSM, OpenTopoMap, USGS, MapLibre demo tiles, and the Worker. The only other https strings are a GPX namespace identifier (`GpxCodec.kt:66`), which is not a request, and a Play Store link for iNaturalist.
- **No new trackers:** no analytics, ads or account.
- **Crash traces:** still shared only by the user (`CrashLogPanel.kt:201`).
- **Background location:** none.

---

## C. Not decided yet (may change the text)

- **How a scheduled backup that fails tells the user:** the owner ruled "inform user … and offer to try again". With no screen open, that may mean a notification. If so, "Notifications" in the permissions list gains a use. Not ruled.
- **Restored offline regions:** listed as "not downloaded", with a re-download. A re-download is the same tile traffic as the original download, which the site already describes. No new text is expected.
- **Play Data safety form:** probably unchanged, because backup and export are user-initiated and on-device. That is the owner's call against Google's current definitions, and I have not checked it.

## D. The "Last updated" date
Both pages need a new date when edited. The delete-data page should also say that the backup feature now exists, next to its `allowBackup` paragraph, so the two are not confused.

# Live privacy page against main (second planner, 2026-09-29)

**Asked for.** The owner, verbatim, in the second planner session [05172f]: "2 do an audit, note the changes needed, and file it in the audit doc in PR 140 / 3 I want the website privacy policy changed at least so it reflects the app / 4 add to #2 / 5 add to #2". Item 4 was the site agent's notes; item 5 was the app changes L1 flagged.

**What was compared.** The live page, zynergy-site `privacy/index.html` at `origin/main` `0688e4d` ("Last updated: 11 September 2026"), against the app testers have: Forager `main` `faf2f88f`, the Worker in `server/pmtiles-worker/` at the same commit, and the site's `functions/` at `0688e4d`. Section B above and Part 2 of L1's addendum describe PR #140 and were left out. L1's addendum is on `legal-drafts` (`a0cf46ed`) and is not yet in this branch.

**How it was checked.** Two read-only passes at `faf2f88f`: one re-derived each item of L1's Part 1 on `main`; the other went through the live page sentence by sentence and searched `main` for data the page does not mention. I re-read these myself before relying on them:
- the photo-location setting (`PhotoLocationPreferenceRepository.kt:3-33`, label at `AvailabilitySettingsUi.kt:497`);
- the labels "Crash Logs" (`CrashLogPanel.kt:85`) and "Recorded Tracks" (`RecordsTab.kt:205`);
- the GPX cache naming (`TrackGpxExporter.kt:18-24`, `:46-80`);
- Directions (`AvailabilityPureFunctions.kt:141-176`);
- a find's delete keeping its photos (`DeleteMushroomLogEntryUseCase.kt`, doc comment) and the gallery delete (`MushroomLogViewModel.kt:912-935`);
- the entry's own copies (`CartographyEntryEntity.kt:90-111`);
- the Worker's onward fetch (`server/pmtiles-worker/src/index.ts:127-136`, `:202-246`, `:320-321`, `:371`);
- MapLibre's permissions, read from the manifest inside the `org.maplibre.gl:android-sdk:13.5.0` AAR in the Gradle cache, the version `gradle/libs.versions.toml:37` pins.

Other line numbers are from the passes, at `faf2f88f`. `K/` is `app/src/main/java/com/zynergylabs/forager/app/`.

**The change.** Made on zynergy-site branch `worktree-bridge-cse_01JoYc3mJXVwBwWBLeCWaqSz`, commit `9bc0d45`, pushed and not merged. Publishing it is the owner's call. The site agent's notes (`prompts/preserved/2026-09-29-43.md`, 1b) tell the site agent not to edit `privacy/index.html` in parallel.

## Changes made to the live privacy page

| # | Section | Was (live `0688e4d`) | Now (`9bc0d45`) | Why (at `faf2f88f`) |
|---|---|---|---|---|
| 1 | Header | "Last updated: 11 September 2026" | 29 September 2026 | Section D above |
| 2 | Short version | "they are logged by the services that receive them" | "the services that receive them may log them" | Our own Worker is one of them and writes no logs (`server/pmtiles-worker/wrangler.toml:12-13`); what a third party logs is not visible from code |
| 3 | What stays on your device | Tracks: "latitude, longitude, timestamp, elevation, accuracy, speed" | Adds the track's name, start and end times, and speed accuracy | `K/data/local/TrackPointEntity.kt:35-41`, `TrackEntity.kt:27-30` |
| 4 | same | Not mentioned | Waypoints: position, altitude, name, note, time | `K/data/local/WaypointEntity.kt:18-34` |
| 5 | same | Not mentioned | Planned trips: name, place, date | `PlannedTripEntity.kt:16-23` |
| 6 | same | "Journal and log entries, including any coordinate attached to a find." | Adds: an entry keeps its own copy of the coordinates of the waypoints and regions it lists | `CartographyEntryEntity.kt:90-111` |
| 7 | same | "Photos taken in the app or imported from your gallery." | Adds the place and time on record for each, pointing to the photos section | `LogPhotoEntity.kt:25-27` |
| 8 | same | "Downloaded offline map regions." | Adds each region's name, centre and radius | `OfflineRegionEntity.kt:31-37` |
| 9 | same | Not mentioned | The last five searches (place, radius, month, filter, the species list returned), kept so a search answers offline; the least recently used is replaced | `RoomSearchCacheRepository.kt:71`; `CachedSearchEntity.kt:35-55`; `CachedSearchDao.kt:57-63` |
| 10 | same | "Preferences such as units and map mode." | Units, and the last-chosen offline-region centre and radius. Map mode is not saved | `AvailabilityScreen.kt:780` (map mode held in `remember`); `DataStoreMapPreferencesRepository.kt:43-48`, `:77-79` |
| 11 | same | Crash traces listed under "private storage, readable by no other app", shared "from the app's crash screen" | Set apart: Forager's folder in shared storage, reachable from the file manager on purpose, newest ten kept, contents named; shared from Settings → Crash Logs | `K/crash/CrashFileStore.kt:76-77`, `:69-71`, `:84`, `:34-40`; `K/ui/crash/CrashLogPanel.kt:85`, `:199-206`; `AvailabilitySettingsUi.kt:289`. That other apps can read it on older Android versions is platform behaviour, not code |
| 12 | same | "None of this is transmitted by the app." | The app uploads none of it; it leaves only when the user sends it: an exported track, a shared crash trace, Directions | `K/ui/track/TrackExportPanel.kt:154-184`; `CrashLogPanel.kt:199-206`; `AvailabilityPureFunctions.kt:141-176` |
| 13 | Android's own backup | Note: "There is no export or import feature yet." | New "Exporting a track": GPX from Records → Recorded Tracks through the share sheet; what the file holds; a copy stays in the cache and a re-export replaces it; no import; entries and photos cannot be exported | `TrackExportPanel.kt:113-120`; `K/ui/log/RecordsTab.kt:205`, `:237`; `K/domain/GpxCodec.kt:103-114`, `:130-150`, `:164-177`; `K/export/TrackGpxExporter.kt:18-24`, `:78`. `GpxCodec.decode` has no caller |
| 14 | What is transmitted | Not mentioned | Directions hands a trip's or waypoint's name and coordinates to the maps app chosen | `AvailabilityPureFunctions.kt:141-176` (a `geo:` URI with the name as its label) |
| 15 | same | "Once a region is downloaded, viewing the map inside it makes no tile requests at all." | Removed. A downloaded region does not stop the Maps tab loading tiles online | `AvailabilityScreen.kt:792`; `K/ui/map/MapSlot.kt:100` (`useOfflineTiles = false`); `BasemapStyles.kt:121-122`. MapLibre's own cache was not checked |
| 16 | The map tile service | "no third-party tile vendor sees the requests your device makes to it" | The device's requests go to the Worker. For zoom 15, beyond our archive, the Worker fetches from `build.protomaps.com` itself and keeps a copy in R2; the fetch carries nothing from the device's request | `src/index.ts:161` (the build URL), `:320-321` and `:371` (above the archive's maximum zoom, up to 15), `:202-246` (a fresh `FetchSource`, then `BUCKET.put` at `:239-243`) |
| 17 | same | "We also hold no plan that would let us export or retrieve per-request logs from Cloudflare." | Removed | It depends on the Cloudflare account's plan, which code cannot show. The site agent's notes already said to leave it out |
| 18 | same | "We keep no logs of these requests." | Kept, with what is kept that is not a log: Cloudflare's edge cache, keyed by tile address, and the zoom 15 copies in R2. Neither records who asked | `src/index.ts:268`, `:292` (edge cache); `:239-243` (R2) |
| 19 | Photos | "Forager does not run an EXIF-stripping step"; "the camera app writes into a file"; its GPS tags "are in the stored file" | Captures from Forager's own camera are stripped by an allowlist: orientation (written back), colour profile and two small technical headers kept; on failure the photo is kept unchanged and the failure logged | `K/photo/CameraXCaptureSession.kt:379-380`; `K/photo/FilePhotoStore.kt:132`; `K/photo/PhotoMetadataScrub.kt:227-234`, `:98-103`, `:65-72`, `:106-113`. This is section A2 as corrected by L1's item 8 |
| 20 | Photos | Imports: "the platform itself redacts GPS tags from the copy the app reads" | Worded as what Forager does (stores what Android hands over, does not ask for the original, does not strip) and what Android normally does on 10 and later | `FilePhotoStore.kt:121-123`. The Android 10 redaction is platform behaviour, not confirmed on a device |
| 21 | Photos | "A find's coordinate ... is stored in the app's own database" | The place and time of every photo are in the database: for captures, the position at the time if "Automatically Save Location to Photos" is on (on by default; off stops every automatic capture, photos and finds alike); for imports, the date and location read from the original with `ACCESS_MEDIA_LOCATION` | `K/domain/PhotoLocationPreferenceRepository.kt:3-33`; `AvailabilitySettingsUi.kt:497`; `K/ui/log/MushroomLogViewModel.kt:835-840`; `FilePhotoStore.kt:143-145`, `:164-178` (`setRequireOriginal` at `:167`) |
| 22 | Photos | Note: "This matters if you export or share a photo yourself." | Forager has no way to share or export a photo | In release the only share intents are the crash log and GPX (`CrashLogPanel.kt:201`, `TrackExportPanel.kt:179`). Debug builds can share a photo (open item 4) |
| 23 | Permissions | Location's uses | Adds the photo and find position under that setting, and that both GPS and network positions are requested | `K/location/AndroidLocationTracker.kt:71-74`; row 21 |
| 24 | Permissions | "Camera", "Notifications, vibrate, foreground service" and "Internet" without constants | Constants named; Camera is Forager's own camera; on Android 13 and later the notification permission is asked when a recording starts | `app/src/main/AndroidManifest.xml:4`, `:23`, `:41`, `:43`, `:49`; `K/MainActivity.kt:502-513` |
| 25 | Permissions | Not mentioned | `ACCESS_NETWORK_STATE` and `ACCESS_WIFI_STATE`, added by MapLibre and not declared by Forager | The MapLibre 13.5.0 AAR's manifest declares both. Forager's manifest declares nine: `INTERNET`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`, `CAMERA`, `ACCESS_MEDIA_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `VIBRATE`. `main` was not built, so a permission another library adds would not show here. With L1's item 14 this closes B4's "not traced" item for `main` |
| 26 | Permissions | "Recording will not work without it, but the rest of the app will." | Adds: revoking location does not stop the requests, because typed coordinates and the map still send them | `K/ui/availability/AvailabilityViewModel.kt:363-374`; `K/ui/map/Basemap.kt:148`, `:162`, `:171` |
| 27 | No analytics | "Nothing about your use of the app is reported to us unless ..." | "Beyond the map requests our tile service receives (above), nothing ... reaches us unless ..." | `K/map/MapLibreOfflineMapRepository.kt:97-111`; `K/map/OfflineStyle.kt:18` |
| 28 | Beta signup | "... and not enough to recover the address it came from." | Clause removed; adds that the hash rows are not deleted automatically | zynergy-site `functions/api/beta-signup.js:233-237` (a constant fallback salt when `RATE_LIMIT_SALT` is unset). No `DELETE` appears anywhere in `functions/`, although `db/schema.sql:20-22` calls the table prunable |
| 29 | Your control | "Delete individual entries in the app." | Names what can be deleted one by one (entries, finds, photos, waypoints, regions, trips); a find's delete keeps its photos; tracks, crash traces, the last five searches and the GPX cache copy cannot be deleted one by one yet | Entries `K/ui/log/CartographyViewModel.kt:447-465`; finds `MushroomLogViewModel.kt:620-632`; photos `:912-935`; waypoints `AvailabilityTripsWaypointsUi.kt:209-233`; regions `AvailabilityOfflineMapsUi.kt:380-406`; trips `AvailabilityViewModel.kt:707-719`; `DeleteMushroomLogEntryUseCase.kt` (keeps the photos). Tracks: `DeleteTrackUseCase` is built at `K/AppContainer.kt:255` and never called. Crash traces `CrashFileStore.kt:58`, `:84`; searches `CachedSearchDao.kt:44-50`; the GPX copy `TrackGpxExporter.kt:78` |

## Corrections to L1's Part 1, read against `main`
L1 wrote Part 1 at `f645e8f9` and marked it as checked on `main`. Re-derived on `faf2f88f`:
- **Item 7 is wrong for `main`.** There is no Undo in `main`: no `PendingDeleteSnackbar.kt`, and `K/ui/log/CartographyEntryReportScreen.kt:96` says "there is no trash/undo yet". Deletes take effect at once, some after a confirm dialog, so the live delete-data page's "takes effect immediately" is true today. The Undo arrives with PR #140 and belongs in Part 2.
- **Item 4, partly.** The GPX file holds more than listed: each point's accuracy, speed, speed accuracy and the rule that excluded it (`GpxCodec.kt:137-141`), and each waypoint's time and IDs. On `main` no track can be deleted, so "deleting the track does not remove it" does not apply yet. The cache holds one file per track, and a re-export overwrites it (`TrackGpxExporter.kt:18-24`).
- **Item 11, partly.** Removing the sentence is right, and the case is stronger than "only a network capture can show it": on `main` the Maps tab always loads the online raster style (row 15).
- **Item 14, partly.** The MapLibre AAR confirms both permissions for `main`. WorkManager's are PR #140 only.
- **Item 16, partly.** `main` has more exceptions than the two named: tracks cannot be deleted at all, and crash traces cannot be deleted in the app. The live delete-data page lists recorded tracks as deletable (`delete-data/index.html:58`), which is not true on `main`.
- Part 1's line numbers are at `f645e8f9` and differ on `main`; the table above gives `main`'s.

## Kept on the page, although code cannot confirm them
Left as they were, as statements of practice, platform behaviour or account settings. Each can be removed with one edit:
- the Play Data safety pointer, and "location is collected and shared" (Play Console);
- what `allowBackup="false"` turns off (`AndroidManifest.xml:134`). One pass noted, from memory and unverified, that for apps targeting API 31 and later (Forager targets 37, `app/build.gradle.kts:249`) some devices may not honour an opt-out of device-to-device transfer. If so, "does not hand it to another device" is too strong. Check Android's documentation before the next publish;
- Android's GPS redaction on imports (row 20);
- what uninstalling and Clear data remove;
- Cloudflare as processor, the retention statement, and "Nothing is sold, shared for advertising, or used for profiling";
- the beta templates, "Nobody else receives it", "not used for any mailing beyond the beta", the signup retention, and "not directed at children".

## The owner's items 4 and 5
- **4, the site agent's notes** (`prompts/preserved/2026-09-29-43.md`): amended by the record planner (`bfe9d34d`, then `f5a2e7e3` and later dispatch notes in the record). Its 1b now says the live privacy page is being changed on this branch and not to edit `privacy/index.html`.
- **5, the app changes L1 flagged.** The owner ruled in -214 (`38b03e1f`): backups leave out the recent searches, and exported GPX files are cleaned from the cache after sharing. Both are dispatched as F5 (-216, `445b292b`). L1's flag 6, the comment at `AppContainer.kt:220` on `journal-redesign` saying a launch that never opens Backup never starts WorkManager while the merged manifest auto-initialises it, was not ruled.
  - When F5 reaches the app testers have, the page's GPX cache-copy sentences ("Exporting a track", "Your control") must change. The recent-searches point only matters once backups ship with PR #140.

## Open for the owner
1. **The delete-data page was not changed** (the owner asked for "at least" the privacy policy). Against `main` it needs:
   - the recorded-tracks line: tracks cannot be deleted on `main` (`AppContainer.kt:255`);
   - the last five searches and the GPX cache copy, as things with no delete;
   - "denying or revoking the location permission ends them" (row 26);
   - crash traces.

   The ruling to keep that line ("They can be deleted now. So we keep it.", in the site agent's notes) holds for PR #140, not for `main`.
2. **Removing a signup** deletes the D1 row by hand, since there is no delete endpoint. The notification email sent to support@ through Resend is a separate copy, and the hash row stays (`beta-signup.js:151-190`). The page's "Removing it deletes the record" is true of the row; whether to say what happens to the email is the owner's call.
3. **Turnstile.** If `TURNSTILE_SECRET` is set, the raw IP goes to Cloudflare Turnstile (`beta-signup.js:202-220`). The widget is commented out (`beta-signup/index.html:255-262`), and a repository cannot show whether the secret is set. If it is ever switched on, "Your IP address is not stored" needs a sentence about Turnstile.
4. **Which build testers get.** Debug builds write a diagnostics log to external storage and can share a stored photo (`app/src/debug/.../DiagnosticsLog.kt:99-103`, `DiagnosticsPanel.kt:291-296`); release builds make these no-ops (`app/src/release/.../DebugDiagnostics.kt:13-45`). The page describes release. If testers get debug builds, it needs more.
5. **Not disclosed, judged minor:**
   - release builds log each fix's provider, accuracy, speed and time at debug level, with no coordinates (`AndroidLocationTracker.kt:53-61`);
   - temporary camera files are swept (`CameraCaptureFiles.kt:48-75`);
   - MapLibre's ambient tile cache is not limited (no `setMaximumAmbientCacheSize` call). That is the library's default, unverified;
   - whether the network location provider sends Wi-Fi or cell data to Google is platform behaviour, unverified.
6. **The export token in a query string.** `/api/beta-export` also accepts the bearer token as `?token=` (`functions/api/beta-export.js:32-38`). This is not a privacy-page matter, but a token in a URL can end up in logs.
7. **Deployed state.** A repository cannot show whether the deployed Worker matches `server/pmtiles-worker/`, and that file's own comment says the zoom 15 path is "NOT VERIFIED AGAINST REAL INFRASTRUCTURE" (`src/index.ts:138`).
