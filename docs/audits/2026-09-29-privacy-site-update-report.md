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
