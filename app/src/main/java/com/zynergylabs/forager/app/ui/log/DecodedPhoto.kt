package com.zynergylabs.forager.app.ui.log

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.zynergylabs.forager.app.photo.oriented
import com.zynergylabs.forager.app.photo.readPhotoOrientation
import java.io.File
import kotlinx.coroutines.withContext

/**
 * Decodes a photo off app-private storage and renders it, filling [modifier]'s given size either
 * way — or a plain placeholder box if decoding fails (a missing file, a corrupt image, an I/O
 * error). Workstream G2 (`docs/plans/pr26-rework.md`): this is the single decode-and-render
 * implementation `LogEntryDetailScreen`'s `LogPhotoThumbnail`, `LogGalleryScreen`'s
 * (former) `GalleryCoverThumbnail`, and `LogEntryReportScreen`'s `ReportPhotoThumbnail` each
 * hand-rolled separately before this — one of them cross-referenced the other two in its own doc
 * comment as "same decode pattern," confirming the duplication was known, not accidental drift.
 * Deliberately just the decode-and-render piece, not a full tile: every prior call site wraps this
 * in its own outer `Box` for sizing (a fixed-dp square, or `fillMaxSize` inside an
 * already-sized parent) and, in [LogPhotoThumbnail]'s case, an overlaid remove button — none of
 * that belongs inside a component meant to be reused by call sites with different chrome around
 * the same underlying photo. This shape is also why G3 can add a selection affordance later (a
 * checkmark overlay, a clickable modifier) by wrapping this in its own `Box`, without touching
 * decoding itself — the dispatch's own requirement that this stay extensible without gaining
 * selection now.
 *
 * A decode failure is logged, never silently swallowed the way all three predecessors did — via
 * raw [Log.w], the same choice [MushroomLogViewModel] already made deliberately for this feature
 * area, not the DI'd `ErrorLog` seam [AvailabilityViewModel]/`TrackRecordingViewModel` use
 * elsewhere: this is a `@Composable`, not a ViewModel, so there's no constructor to inject a seam
 * into, and Robolectric's `ShadowLog` already lets a test assert on a raw `Log.w` call without one.
 *
 * **Callers must give it bounded size constraints** (`fillMaxSize`, `size`, or a width and a
 * height). Until the photo loads this draws the same `Image` with a flat-colour painter so that a
 * caller's click stays on one layout node (see the comment at the `Image`), and an `Image` whose
 * painter has no intrinsic size fills whatever bounded space it is given, where the old placeholder
 * `Box` wrapped to nothing. Every call site in `main/` already bounds it; an unsized call would show
 * a placeholder that fills its parent and then jumps to the photo's size.
 */
@Composable
internal fun DecodedPhoto(
    relativePath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Log photo",
) {
    val context = LocalContext.current
    var bitmap by remember(relativePath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(relativePath) {
        bitmap = withContext(PhotoDecodeDispatcher.current) {
            runCatching {
                val file = File(context.filesDir, relativePath)
                val options = BitmapFactory.Options().apply { inSampleSize = DECODE_SAMPLE_SIZE }
                val decoded = BitmapFactory.decodeFile(file.absolutePath, options)
                    ?: error("BitmapFactory.decodeFile returned null for '$relativePath'")
                // EXIF-orientation-display dispatch: BitmapFactory ignores the orientation tag, so
                // a capture stored in the sensor's landscape frame came out sideways here. Turned
                // at display time on the sampled bitmap (see Bitmap.oriented for the cost); the
                // file is never rewritten. Every thumbnail site in the app goes through this one
                // decode, so this is the one fix for all of them.
                decoded.oriented(readPhotoOrientation(file))
            }.onFailure { error ->
                Log.w(TAG, "Couldn't decode photo at '$relativePath'.", error)
            }.getOrNull()?.asImageBitmap()
        }
    }

    // One Image for both states, only its painter (and whether it is described) changes. Before
    // dispatch 2026-09-28-317 a placeholder Box and an Image were two different layout nodes carrying
    // the same caller modifier, so a caller's clickable/combinedClickable was detached and re-attached
    // when the decode landed, and a tap or long-press in flight at that moment was lost
    // (docs/audits/2026-09-30-ci-flake-diagnosis.md). Rejected: a wrapper Box at each call site, or an
    // outer node here with a switching child, because either moves the click above the Image and
    // the merged node loses Role.Image (a parent's role wins the merge). This keeps the click and the
    // Image on one node, so the semantics are what they were: no description and no role while
    // loading (Image adds both only for a non-null description), description plus role Image once
    // loaded. DecodedPhotoSemanticsTest pins that against the old build's dumps.
    val loaded = bitmap
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val painter = remember(loaded, placeholderColor) {
        if (loaded != null) BitmapPainter(loaded, filterQuality = FilterQuality.Low) else ColorPainter(placeholderColor)
    }
    Image(
        painter = painter,
        contentDescription = if (loaded != null) contentDescription else null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

/** Internal, not private, so [DecodedPhotoTest] derives its expected rendered sizes from the value actually used. */
internal const val DECODE_SAMPLE_SIZE = 4
private const val TAG = "LogPhotoDecode"
