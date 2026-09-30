# Album gesture fix (dispatch 2026-09-28-317): completion report (draft, stopped to ask)

**Status: stopped before building, at the dispatch's stop-and-ask on semantics.** Both approaches the
dispatch offers change what a screen reader reads on the affected photos (section 3). A third approach
that does not is described there. Nothing is built yet, and no Gradle has run.

- **Base:** `origin/journal-redesign` at `b90164b2`, its head when this started. That is later than the
  dispatch's `cf8f1000`, which is an ancestor (`git merge-base --is-ancestor`).
- **Worktree / branch:** `~/Zynergy/forager-wt/album-gesture`, branch `album-gesture`.
- **Diagnosis read:** -296's report, `origin/ci-flake` `3246ead8`.

## 1. The mechanism, in this tree

- `DecodedPhoto.kt:55-81`: `remember(relativePath) { mutableStateOf<ImageBitmap?>(null) }` and a
  `LaunchedEffect` that decodes on `Dispatchers.IO` (`:57-74`). Then two different layout nodes carry the
  caller's `modifier`: `Image(…, modifier = modifier)` once loaded (`:78`), and
  `Box(modifier = modifier.background(…))` before (`:80`). As -296 found.
- `AlbumPhotoTile` (`EntriesAlbum.kt:246-261`) puts the gesture on that modifier in both branches (below).

## 2. Every `DecodedPhoto` call site in `main/` (`git grep -n "DecodedPhoto(" -- app/src/main`)

| Call site | Gesture on the swapped node? |
|---|---|
| `EntriesAlbum.kt:248-251`, album tile, no delete wired | **Yes**: `.clickable(onClickLabel = "Open full screen", onClick = onOpen)` |
| `EntriesAlbum.kt:256-259`, album tile, delete wired (the phone's and, since J6a, the wide tree's `LogPanel`) | **Yes**: `.tileClickable(…)`, the tap and long-press of the CI failures |
| `LogEntryDetailScreen.kt:320-323`, a find's photo thumbnail in the editor | **Yes**: `.clickable(onClickLabel = "Open full screen", onClick = onOpen)` |
| `LogEntryReportScreen.kt:321-324`, a find's report thumbnail | **Yes**: `.size(88.dp).clickable(…)` |
| `CartographyEntryCard.kt:327-331`, the entry card's hero | No: `fillMaxWidth().height().testTag()`. Any click is on the card, a stable ancestor |
| `SidewaysEntryCard.kt:203-207`, the sideways card's slot | No: `size().clip().testTag()` |
| `CartographyEntryEditScreen.kt:450` (`KeptPhotoOrUnavailable`), called from `CartographyEntryEditScreen.kt:418` and `CartographyEntryReportScreen.kt:633` | No: both callers pass `Modifier.size(KEPT_PHOTO_SIZE_DP.dp)` |
| `FindsGalleryScreen.kt:256`, the find tile's cover | No: `fillMaxSize()`. The tile's click is on its card, an ancestor that is not swapped |
| `PullPhotoPickerScreen.kt:109-112`, the picker tile | No: `fillMaxSize()`. `Card(onClick = …)` at `:103-104` is the stable ancestor |
| `MapBubble.kt:385` and `:396`, bubble thumbnails | No: `Modifier.size(BUBBLE_THUMBNAIL_SIZE)` |

**Not a `DecodedPhoto` site:** `PhotoViewerDialog` has its own decode (`PhotoViewerDialog.kt:328`). Its
gestures (`:379-401`) are on the loaded `Image` only, keyed on the bitmap. Before the load it shows a
spinner (`:419`) with no gesture, so there is no gesture in flight to lose. It is out of the rule's scope.

**Four affected sites:** two in the album, and a find's thumbnail in the editor and in the report.

## 3. The stop: both offered approaches change screen-reader semantics

Read from the compiled classes in the build's resolved versions (no source jars in the Gradle cache):
- `Image` sets `contentDescription` **and `role = Role.Image`** on its own node
  (`androidx/compose/foundation/ImageKt`, foundation-android 1.12.1, `setContentDescription`, then
  `Role$Companion.getImage` and `setRole`).
- `Role`'s merge policy keeps the **parent's** value (`SemanticsProperties.Role$lambda$0`, ui-android
  1.12.1: `aload_0; areturn`). A child's role does not survive merging into a parent that has none.

Today the click and the `Image`'s semantics are on **one node**, so the merged tile reads "Log photo",
role Image, with the "Open full screen" action. Under either offered approach the click moves to a node
above the `Image`, and the merged node **loses role Image**. TalkBack would stop saying "Image" on the four
affected photos. That is inferred from the merge policy and not yet run, but it is exactly the dispatch's
stop-and-ask condition.

- **(a) Wrapping `Box` at each affected site (-296's trial):** role lost. Adding `role = Role.Image` to the
  wrapper would restore it once loaded, but it would also announce an image role on the empty placeholder
  before the decode lands. That is a smaller change of its own.
- **(b) `DecodedPhoto` as one stable outer node with a switching child:** role lost at every call site
  with a gesture. It also changes layout: a caller with no size (`DecodedPhotoTest`) would get a
  `fillMaxSize` child in a wrap-content box. It also moves `testTag`/`clip` off the `Image`.
- **(c) Not offered by the dispatch:** `DecodedPhoto` always composes the **same `Image`**, and only its
  painter changes, a flat `surfaceVariant` `ColorPainter` until the bitmap arrives, then a
  `BitmapPainter`, with `contentDescription = null` until loaded. The layout node is never replaced, so
  the gesture survives at every call site at once. Semantics before and after the load are what they are
  today (no description or role on the placeholder; description plus role Image once loaded). So are size
  (a `ColorPainter` has no intrinsic size, like the empty `Box`) and `testTag` placement. Differences are
  inferred, not run. The `Image` clips to bounds where the `Box` did not, with no visible effect on a flat
  colour. The gesture survives only if Compose updates that node's modifier chain in place when the
  semantics element is added after the caller's modifiers. The regression tests would prove or disprove
  that.

## 4. The test seam (no production change)

`DecodedPhoto` has no injection seam (`Dispatchers.IO` and `BitmapFactory.decodeFile` are called directly,
`DecodedPhoto.kt:58-62`). Robolectric's shadow mechanism is an existing, test-only seam. A test-only
`@Implements(BitmapFactory::class)` shadow, registered with `@Config(shadows = …)`, whose `decodeFile`
waits on a latch the test opens, puts the swap between `down` and `up` with no production hook. That is
not built yet.

## 5. `JournalTabTest` From Album

The assertion is at `JournalTabTest.kt:495` in this tree (the dispatch said about `:472`). Today it waits
only for the picker's decode (`:482-484`, `waitUntil` on any `Log photo` node). After the pick at `:491` it
asserts the edit form's photo at `:495`, unwaited.
