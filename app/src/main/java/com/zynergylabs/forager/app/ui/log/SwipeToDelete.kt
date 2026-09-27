package com.zynergylabs.forager.app.ui.log

/*
 * Journal redesign J4 put a single-stage `SwipeToDeleteRow` (Material 3's `SwipeToDismissBox`) here
 * for waypoint and offline-region rows. J4b (owner ruling "Lists swipe, grids long-press
 * (Recommended)", `prompts/preserved/2026-09-27-23.md`, L6) replaced it with the two-stage
 * [TwoStageSwipeRow], which rests open with its actions before a full swipe deletes;
 * `SwipeToDismissBox` has no resting open state, so the old row was removed rather than kept beside
 * the new one. The tag and the action label stay here, unchanged, so J4's tests and callers keep
 * finding rows the same way.
 */

/** The custom accessibility action's label for Delete on every swipeable row. */
internal const val DELETE_ACTION_LABEL = "Delete"

/** The test tag of a swipeable Records row — the same in its single-type chip and in the All logbook, which never show at once. */
internal fun swipeToDeleteTag(type: RecordType, recordId: String): String = "records-swipe-${type.tagName()}-$recordId"
