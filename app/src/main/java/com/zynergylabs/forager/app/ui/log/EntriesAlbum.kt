package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.PhotoSource
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The album view of Entries — journal redesign J2, T3 (plan J3): every gallery photo, grouped by
 * day ([groupAlbumByDay]: newest day first, unknown dates last), in 3 columns with 3 dp gaps.
 *
 * **Why a new composable, not a change to [PhotoGalleryScreen].** [PhotoGalleryScreen] is also the
 * wide tree's drawer panel (`AvailabilityScreen.kt`, `DrawerPanel.PhotoGallery`), which the
 * dispatch says must keep working and may keep its look until J6. Leaving it untouched keeps that
 * caller exactly as it was; this view reuses its parts instead: the same acquisition launchers
 * ([rememberPhotoAcquisitionLaunchers]), the same Take photo/Import actions (now behind the Add photo button), the same full-screen viewer
 * ([PhotoViewerDialog], stepping through the photos in the order shown here) and the same delete
 * confirmation ([GalleryPhotoDeleteDialog], extracted from [PhotoGalleryScreen] for this).
 *
 * **Badges** ([AlbumAttachmentBadges], added by the second J2 coder): one for a photo a journal
 * entry keeps, a distinct one for a photo attached to a find, both when both. The first coder found
 * no visible reference count on the tile to replace; the count stays only in the delete dialog.
 *
 * **Add photo** ([AddPhotoButton], also the second coder's): the floating button's menu of Take
 * photo and Import replaced the album's own Camera/Import row.
 */
@Composable
internal fun EntriesAlbum(
    photos: List<GalleryPhoto>,
    isLoading: Boolean,
    onDeletePhoto: (GalleryPhoto) -> Unit,
    /** Opens the in-app camera for the Album — see [InAppCameraHost]. */
    onOpenCamera: () -> Unit,
    /** A photo acquired via Camera or Import here, added to the gallery standalone. */
    onAddGalleryPhoto: (PhotoSource) -> Unit,
    modifier: Modifier = Modifier,
    loadErrorMessage: String? = null,
    /** How many Cartography entries keep each photo (by id); read by the delete confirmation. */
    cartographyEntryReferenceCounts: Map<String, Int> = emptyMap(),
) {
    val photoAcquisition = rememberPhotoAcquisitionLaunchers(onAddGalleryPhoto, onOpenCamera)
    // The id, not the index, and saveable — as PhotoGalleryScreen's own viewer state.
    var viewingPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    val days = remember(photos) { groupAlbumByDay(photos) }
    val shownInOrder = remember(days) { days.flatMap { it.photos } }

    Box(modifier = modifier.fillMaxSize().testTag(ENTRIES_ALBUM_TAG)) {
        when {
            isLoading && photos.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            photos.isEmpty() && loadErrorMessage != null -> Text(
                loadErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            )

            photos.isEmpty() -> Text(
                "No photos yet. Use Add photo to take or import one.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(ALBUM_COLUMNS),
                modifier = Modifier.fillMaxSize(),
                // FAB_CLEARANCE at the bottom, as the timeline: the last row scrolls clear of Add photo.
                contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = FAB_CLEARANCE),
                horizontalArrangement = Arrangement.spacedBy(ALBUM_GAP),
                verticalArrangement = Arrangement.spacedBy(ALBUM_GAP),
            ) {
                for (day in days) {
                    item(key = "day-${day.date}", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            day.date?.let(ALBUM_DAY_FORMAT::format) ?: "Date unknown",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = Spacing.sm).testTag(albumDayTestTag(day.date)),
                        )
                    }
                    items(day.photos, key = { it.photo.id }) { galleryPhoto ->
                        AlbumPhotoTile(
                            galleryPhoto = galleryPhoto,
                            onOpen = { viewingPhotoId = galleryPhoto.photo.id },
                            onDelete = { onDeletePhoto(galleryPhoto) },
                            cartographyEntryCount = cartographyEntryReferenceCounts[galleryPhoto.photo.id] ?: 0,
                        )
                    }
                }
            }
        }

        AddPhotoButton(
            onTakePhoto = photoAcquisition.launchCamera,
            onImport = photoAcquisition.launchGallery,
            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg),
        )
    }

    val viewingIndex = viewingPhotoId?.let { id -> shownInOrder.indexOfFirst { it.photo.id == id } }?.takeIf { it >= 0 }
    if (viewingIndex != null) {
        PhotoViewerDialog(photos = shownInOrder.map { it.photo }, initialIndex = viewingIndex, onDismiss = { viewingPhotoId = null })
    }
}

/**
 * The album's floating button, "📷 Add photo" (plan J7; owner ruling "Menu of both (Recommended)",
 * `prompts/preserved/2026-09-27-19.md`): it opens a small menu of Take photo and Import, which call
 * the same two actions the album's Camera/Import row called before this replaced it
 * ([PhotoAcquisitionLaunchers.launchCamera], [PhotoAcquisitionLaunchers.launchGallery]). A
 * `DropdownMenu` anchored to the button, the stable Material 3 menu; the Expressive FAB menu is
 * Understory step 5's (plan, "Components"). The same tag as the timeline's "New entry" button, since
 * exactly one of the two is on screen at a time.
 */
@Composable
private fun AddPhotoButton(onTakePhoto: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        // The content-lambda overload, for the same reason as the timeline's button: the (icon, text)
        // overload clears the label out of the merged semantics under material3 1.5.0-alpha26.
        ExtendedFloatingActionButton(
            onClick = { menuOpen = true },
            modifier = Modifier.testTag(ENTRIES_FAB_TAG),
        ) {
            Icon(Icons.Filled.AddAPhoto, contentDescription = null)
            Spacer(Modifier.width(Spacing.md))
            Text("Add photo")
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Take photo") },
                leadingIcon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                onClick = { menuOpen = false; onTakePhoto() },
                modifier = Modifier.testTag(ENTRIES_FAB_MENU_TAKE_PHOTO_TAG),
            )
            DropdownMenuItem(
                text = { Text("Import") },
                leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                onClick = { menuOpen = false; onImport() },
                modifier = Modifier.testTag(ENTRIES_FAB_MENU_IMPORT_TAG),
            )
        }
    }
}

internal const val ENTRIES_FAB_MENU_TAKE_PHOTO_TAG = "entries-fab-menu-take-photo"
internal const val ENTRIES_FAB_MENU_IMPORT_TAG = "entries-fab-menu-import"

/** A square album tile: the photo opens the viewer; the delete button in its corner asks first, as on [PhotoGalleryScreen]'s tiles. */
@Composable
private fun AlbumPhotoTile(
    galleryPhoto: GalleryPhoto,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    cartographyEntryCount: Int,
) {
    var confirmingDelete by remember(galleryPhoto.photo.id) { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).testTag(albumPhotoTestTag(galleryPhoto.photo.id))) {
        DecodedPhoto(
            relativePath = galleryPhoto.photo.relativePath,
            modifier = Modifier.fillMaxSize().clickable(onClickLabel = "Open full screen", onClick = onOpen),
        )
        IconButton(onClick = { confirmingDelete = true }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete this photo")
        }
        AlbumAttachmentBadges(
            photoId = galleryPhoto.photo.id,
            attachedToEntry = cartographyEntryCount > 0,
            attachedToFind = galleryPhoto.referencingEntryIds.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomStart).padding(ALBUM_BADGE_INSET),
        )
    }
    if (confirmingDelete) {
        GalleryPhotoDeleteDialog(
            galleryPhoto = galleryPhoto,
            cartographyEntryCount = cartographyEntryCount,
            onConfirm = { confirmingDelete = false; onDelete() },
            onDismiss = { confirmingDelete = false },
        )
    }
}

/**
 * The album's two attachment badges (owner ruling "Two badges", `prompts/preserved/2026-09-27-19.md`):
 * one for a photo a journal (Cartography) entry keeps, a distinct one for a photo attached to a find,
 * both when both. Each is its own node with a content description, so a screen reader announces it
 * on the tile.
 *
 * Where each fact comes from, both already on the album's inputs, no new read:
 * - **Journal entry:** `cartographyEntryReferenceCounts` (`MushroomLogUiState.cartographyEntryPhotoReferenceCounts`,
 *   loaded in `MushroomLogViewModel.loadGalleryPhotos` through `GetEntryReferenceCountUseCase.forPhoto`
 *   and `CartographyEntryDao.countEntriesReferencingPhoto`, which counts committed entries only,
 *   `isDraft = 0`).
 * - **Find:** [GalleryPhoto.referencingEntryIds], the join over `log_entry_photos` in
 *   `RoomMushroomLogRepository.getAllPhotos`. That table holds draft finds' rows too (a draft find is
 *   a standalone row), so a photo attached only to an unfinished find carries this badge; the delete
 *   dialog's "N entries" count reads the same list the same way.
 *
 * The link icon is the plan's 🔗 for "attached to an entry"; the find badge takes the Finds chip's
 * icon and its J6 colour role ([RecordTypeStyle], Finds), so it reads as the same kind of thing the
 * Records chips call a find. Neither is touchable: touches on them fall through to the photo.
 */
@Composable
private fun AlbumAttachmentBadges(
    photoId: String,
    attachedToEntry: Boolean,
    attachedToFind: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!attachedToEntry && !attachedToFind) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(ALBUM_BADGE_INSET)) {
        if (attachedToEntry) {
            AlbumBadge(
                icon = Icons.Filled.Link,
                description = ALBUM_ENTRY_BADGE_DESCRIPTION,
                container = MaterialTheme.colorScheme.surfaceContainerHighest,
                content = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(albumEntryBadgeTestTag(photoId)),
            )
        }
        if (attachedToFind) {
            val finds = RecordTypeStyle.colors(RecordType.FINDS)
            AlbumBadge(
                icon = Icons.Filled.Eco,
                description = ALBUM_FIND_BADGE_DESCRIPTION,
                container = finds.container,
                content = finds.accent,
                modifier = Modifier.testTag(albumFindBadgeTestTag(photoId)),
            )
        }
    }
}

@Composable
private fun AlbumBadge(icon: ImageVector, description: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Box(
        // The description sits on the badge's own node, not the icon inside it, so the badge is one
        // thing a screen reader announces and one thing a test finds by its tag.
        modifier = modifier.size(ALBUM_BADGE_SIZE).background(container, CircleShape).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(ALBUM_BADGE_ICON_SIZE))
    }
}

private val ALBUM_BADGE_SIZE = 22.dp
private val ALBUM_BADGE_ICON_SIZE = 14.dp
private val ALBUM_BADGE_INSET = 4.dp

internal const val ALBUM_ENTRY_BADGE_DESCRIPTION = "Attached to a journal entry"
internal const val ALBUM_FIND_BADGE_DESCRIPTION = "Attached to a find"

internal fun albumEntryBadgeTestTag(photoId: String): String = "entries-album-badge-entry-$photoId"

internal fun albumFindBadgeTestTag(photoId: String): String = "entries-album-badge-find-$photoId"

/** Plan J3: 3 columns in compact portrait. (Short windows get 5 in J5, L6; not built here.) */
private const val ALBUM_COLUMNS = 3

/** Plan J3: 3 dp between tiles, both ways. */
private val ALBUM_GAP = 3.dp

private val ALBUM_DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

internal const val ENTRIES_ALBUM_TAG = "entries-album"

internal fun albumDayTestTag(date: LocalDate?): String = if (date == null) "entries-album-day-unknown" else "entries-album-day-$date"

internal fun albumPhotoTestTag(photoId: String): String = "entries-album-photo-$photoId"
