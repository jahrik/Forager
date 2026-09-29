# Photo export to the Gallery: completion report (intent `2026-09-28-126`)

**Status: pre-registration only.** Nothing has been built or run when this part was pushed; it stays unchanged. Later sections are added below it.

**Role and model.** Device-free build coder in `/home/zynergy-labs/Zynergy/forager-wt/photo-export`, branch `photo-export`, cut from `origin/journal-redesign`. The session is configured `claude-sonnet-5-5`; **which model
served the turns I cannot read**, so it is unverified. I write no `RECORD.md`, index, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.

## The dispatch, and a ruling that changes it

The governing file is `prompts/preserved/2026-09-29-10.md` on `origin/journal-redesign` (the launch prompt names intent `2026-09-28-126`; `RECORD.md` gives its dispatch-file as `preserved/2026-09-29-10.md`; there is no
`2026-09-28-126.md`). Its build, verbatim where it matters: "**On the photo viewer (`PhotoViewerDialog`),** add a "Save to Gallery" action. On Android 10 and above (API 29+) it inserts the photo into MediaStore under
`Pictures/Forager` (the "Forager" album), with **no storage permission**. **On API 26-28, the same action reads "Save to folder"** and opens the Storage Access Framework's create-document picker, with the file written where the
user chose. **Add no storage permission.** ... For an import, which is deliberately unscrubbed (`FilePhotoStore.kt:125-132`), strip GPS from the **exported copy only**, never from the stored file, and state how."

**A gap I found and the owner ruled on, in this session, directly (not through the planner).** `scrubPhotoMetadata` rewrites JPEGs only and returns `NotAJpeg` for anything else (`photo/PhotoMetadataScrub.kt`, its first branch),
while the import picker is `ImageOnly` (`ui/log/PhotoAcquisitionLaunchers.kt:139`) and the store names every file `.jpg` (`FilePhotoStore.kt:120`). So the dispatch's "strip GPS from an import's exported copy" cannot be done for a
HEIC, PNG or WebP import with the code that exists. I stopped and put the options to the owner (fail closed; strip PNG/WebP and refuse the rest; export as-is). **The owner's answer, verbatim:**

> "Imported photos taken outside the app are not within our scope. They can use a scrubbing app to remove it if they want it removed. All photos taken inside the app are scrubbed either way and that's our scope"

I asked one confirming question, since that could mean either "export imports unchanged" or "do not offer Save on imports"; the answer, verbatim: **"Export imports unchanged (Recommended)"**, whose stated meaning was: "Same action on every photo. An import's exported
copy is a byte-for-byte copy of the stored file, so it may keep location or other metadata; the user can scrub it with another app."

**Consequence for the build, recorded so the planner can rule on the conflict with the dispatch text:** the exporter writes the stored file's bytes unchanged for every photo. The dispatch's strip step and its test "an exported import has no GPS" are
**dropped on the owner's word**; the other half of that test, "the stored file is byte-unchanged", stays, and becomes "the exported bytes equal the stored bytes, and the stored file is unchanged". It also means an imported photo may carry location into the Gallery;
that follows from the ruling and I did not add a warning (new copy is an abort condition). Ruling 2 A in the plan ("Exported photos carry no location") is unchanged for captures and no longer holds for imports; **the plan is the planner's to reconcile.**

## Premises checked at my base (`b586eb6`; `a3221b4` is an ancestor)

1. **Base:** `origin/journal-redesign` at `b586eb6`; `a3221b4` is an ancestor (verified). The launch prompt says "at `a3221b4` or later".
2. **Stale line numbers (a finding):** `FilePhotoStore.kt:120` (the `.jpg` name) is right; `:125-132` (the scrub comment and call) is right (`:125` comment, `:132` the call). The premise pulse's `PhotoAcquisitionLaunchers.kt:139` for the picker is now the `pickPhotos.launch(PickVisualMediaRequest(ImageOnly))` line near :139 (read).
3. **No storage permission is declared and none is needed** (read): `AndroidManifest.xml:4-49` lists INTERNET, location, CAMERA, ACCESS_MEDIA_LOCATION, foreground-service, notifications, VIBRATE; no storage or media permission. A MediaStore insert into `Pictures/` by the owning app needs none on API 29+; the SAF create-document intent needs none at any level (platform knowledge, not observed on a device).
4. **`<queries>`:** the manifest has a `<queries>` block at `:59`; `ACTION_CREATE_DOCUMENT` is resolved by the system picker and needs no entry (inferred; the manifest stays untouched unless a test proves otherwise).
5. **The viewer** (`ui/log/PhotoViewerDialog.kt`): one `Dialog`; the only controls are a Close `IconButton` at `TopStart` and, for more than one photo, a Previous / counter / Next row at `BottomCenter`, all inside `safeDrawing` insets. `ViewerControl` is a 48 dp `IconButton` with a scrim glyph and a `contentDescription`. **The natural place for one more control is `TopEnd`, the mirror of Close.** Five hosts call it with `(photos, initialIndex, onDismiss)` (`EntriesAlbum.kt:181`, `LogEntryDetailScreen.kt:288`, `LogEntryReportScreen.kt:199`, `PhotoGalleryScreen.kt:162`, `MapBubble.kt:329`); the design keeps that signature and gives the dialog an optional exporter parameter with a default, so no host changes.
6. **Message surface:** the repo's transient messages are `Toast`s (`AvailabilityCompactMapUi.kt:530-541`, `AvailabilityPureFunctions.kt:165-218`); the viewer is a `Dialog` with no scaffold. I use a `Toast`.
7. **Test tooling:** Robolectric 4.16.1; the `sdk = [36]` jar is cached; the SDK 28 jar is not, but the network resolves it (`curl` to Maven Central answered 200). No existing test registers a MediaStore provider (`git grep registerProvider|ShadowMediaStore` in `src/test`: none), so a small fake provider is a new fixture.

## Design (mine to state, the dispatch's to rule if wrong)

- **`photo/PhotoExporter.kt`:** `interface PhotoExporter` (the seam CLAUDE.md asks for) and `FilePhotoExporter(context)`; `saveToGallery(relativePath)`, `suggestDocument(relativePath)` (sniffed name and MIME for the picker), `saveToDocument(relativePath, uri)`. Every failure is logged at WARN in the exporter and returned as a `Result.failure`; nothing is swallowed.
- **MIME from the content:** a header sniff (JPEG, PNG, GIF, WebP, HEIC/HEIF, AVIF, BMP). **An unrecognised header is a failure, not a guess**: no default `image/jpeg` (CLAUDE.md, Errors and failure paths). File name `forager-photo-<yyyyMMdd-HHmmss of the photo's own createdAt>.<ext>` (extension from the sniff).
- **MediaStore path (API 29+):** insert `DISPLAY_NAME`, `MIME_TYPE`, `RELATIVE_PATH = "Pictures/Forager"`, `DATE_TAKEN` (the photo's `createdAtEpochMillis`), `IS_PENDING = 1`; copy the stored bytes; set `IS_PENDING = 0`. On any failure or cancellation the half-written row is deleted. No location column is written.
- **Folder path (API 26-28):** the control's description is "Save to folder"; a tap sniffs, then launches `ACTION_CREATE_DOCUMENT` with the sniffed type and `EXTRA_TITLE`; a returned URI receives the byte copy; a cancelled picker shows nothing.
- **Messages, exactly as approved and no others:** "Saved to Gallery", "Saved", "Couldn't save that photo.".
- **Control:** an icon-only `ViewerControl` at `TopEnd` whose `contentDescription` is "Save to Gallery" (API 29+) or "Save to folder" (26-28), disabled while a save is in flight. No visible new text.

## Predictions, written before any edit

| Test (through the real `PhotoViewerDialog`, Robolectric) | Predicted at base | Expected failure |
|---|---|---|
| API 36, JPEG content: tap "Save to Gallery" inserts `Pictures/Forager`, `image/jpeg`, a `.jpg` name, the stored bytes, and shows "Saved to Gallery" | **fail** | no node with content description "Save to Gallery" |
| API 36, PNG / HEIC / WebP bytes in a `.jpg`-named file: `image/png` / `image/heic` / `image/webp` and a matching extension | **fail** (x3) | same |
| API 36, an "import" with a GPS EXIF: exported bytes equal the stored bytes and the stored file is unchanged | **fail** | same |
| API 36, the store refuses the insert: "Couldn't save that photo." and a WARN log | **fail** | same |
| API 36, bytes that are no image: "Couldn't save that photo.", a WARN log, no insert | **fail** | same |
| API 28: the control reads "Save to folder", a tap launches `ACTION_CREATE_DOCUMENT` with the sniffed type and a matching `EXTRA_TITLE`, and inserts nothing into MediaStore | **fail** | no node "Save to folder" |
| API 28: the picker returns a URI, the bytes are written there and "Saved" shows; a failing write shows "Couldn't save that photo." | **fail** (x2) | same |

All of these are expected to **fail at base because the control does not exist**; each is written so its failure is that missing node, not a compile error (they use only the viewer, MediaStore's public API and a fake provider).
After the build all pass. **Predicted growth: +10 to +11** (the planner predicted 6 to 12). **Revert checks (planned):** (1) MIME from the file name instead of the sniff (fails the PNG, HEIC and WebP tests); (2) `RELATIVE_PATH` changed
(fails the album test); (3) the failure message/log removed (fails the failure tests); each restored from a copy saved before the edit, the build log checked for compile errors, result files checked newer than the run's start.

**Device-only, listed not run:** the photo appearing in the S22 Gallery's "Forager" album; whether the Gallery shows it with the right date; that an import's exported copy keeps whatever location it carried (now the owner's ruling); on an API 26-28 device, the folder picker and the written file.
Robolectric's fake provider is not MediaStore, and it reports zero insets, so none of this proves the Gallery shows the photo.
