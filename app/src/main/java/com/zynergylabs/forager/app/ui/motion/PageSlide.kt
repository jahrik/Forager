package com.zynergylabs.forager.app.ui.motion

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

/*
 * Motion Part 3, item 1 (dispatch 2026-09-28-676; the owner, RECORD -651: Journal pages "Slide in, slide back (Recommended)":
 * "Opened pages slide in from the right; Back slides them out to the right"). A page you open slides in from the right over the
 * one beneath, which stays where it is; going back, the page slides out to the right and uncovers the one beneath, retracing the
 * way in (CLAUDE.md memory: "Back retraces the way in").
 *
 * **What a leaving page does.** From its first leaving frame, a page that is going (the one being covered on the way in, the one
 * sliding out on the way back) takes no touch ([leavingTakesNoTouches], Part 1), is gone from the semantics tree but for its
 * leaving marker (Part 2's fix (a): a screen reader finds the arriving page alone), and its Back handlers are off (Part 2,
 * Amendment 1, item 5), so a Back pressed mid-slide is the arriving page's. These are the same three things [TabCrossfade] does to
 * a leaving tab, from the same pieces.
 *
 * **The arriving page takes touches where it is drawn.** The slide moves the page by placement, not by drawing alone, because two
 * Journal pages host a live map (the entry report and the find's location picker), and a hosted `View` follows placement, not a
 * drawn offset (see [holdWhileLeaving]). So for the length of a slide-in, the arriving page's controls are where they are drawn,
 * on their way to where they settle. Their settled touch areas are unchanged. This is the one place this part moves a touch area
 * while it moves, and it is reported as a stop for the owner rather than decided here.
 *
 * **Not verified anywhere** (nothing here has been compiled or run): that [ExitTransition.None] keeps the covered page composed
 * and drawn, still, until the slide-in ends. That rests on AnimatedContent removing an outgoing page only when its whole transition
 * has finished, which waits for the incoming slide; read as my understanding of the library, not from its source in this session.
 * `JournalPageSlideTest` reads the covered page mid-slide, so it will show it.
 */

/** Whether a page is the one leaving a [PageSlide] or a [SlideOverPage], readable in tests. */
val PageLeavingKey = SemanticsPropertyKey<Boolean>("PageLeaving")
var SemanticsPropertyReceiver.pageLeaving by PageLeavingKey

/**
 * One page at a time, sliding to the next when [targetState] changes, on [MotionTokens.pageSlideSpec]. Deeper pages
 * ([depthOf] larger) are further in: going to a page at least as deep as the current one slides it in from the right over the
 * current one; going to a shallower one slides the current page out to the right, uncovering it. Pages with the same [contentKey]
 * are one page: a change between them updates it in place, with no slide (an open find whose fields change, for instance).
 *
 * [content] must draw from the page it is handed, never from the caller's current state: while two pages are on screen the leaving
 * one is drawn from its own value. That is why a page carries what it shows (the open entry, say), so a report sliding out after
 * its entry has closed can still be drawn.
 *
 * Every page is drawn on [pageColor], opaque, so the page sliding in covers the one beneath rather than showing it through
 * (the pages themselves draw no background; the screen behind them did, before two of them shared it). The default is what the
 * app's Scaffold draws behind its content. Not for a surface over the map: an opaque page there would break the map chrome's 80%
 * rule, which is why the Tools drawer's pages do not slide (motion Part 3, Amendment 1's stops).
 *
 * Clipped to its own bounds, so a page sliding in from the right is not drawn over whatever sits beside the Journal (the landscape
 * rail). Under reduced motion ([LocalReduceMotion]) a page changes at once and no leaving page is kept (docs/motion-spec.md §4).
 */
@Composable
fun <T> PageSlide(
    targetState: T,
    depthOf: (T) -> Int,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it },
    pageColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (T) -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    val slide = MotionTokens.pageSlideSpec<IntOffset>()
    val inertBack = rememberInertBackOwner()
    val targetKey = contentKey(targetState)
    AnimatedContent(
        targetState = targetState,
        modifier = modifier.clipToBounds(),
        transitionSpec = { pageSlideTransform(reduceMotion, depthOf(initialState), depthOf(targetState), slide) },
        contentAlignment = Alignment.TopStart,
        contentKey = contentKey,
        label = "pageSlide",
    ) { page ->
        LeavingPageFrame(leaving = contentKey(page) != targetKey, inertBack = inertBack, pageColor = pageColor) { content(page) }
    }
}

/**
 * The transform [PageSlide] uses between a page at [fromDepth] and one at [toDepth]: in from the right over the current page when
 * going as deep or deeper, the current page out to the right when going back. No size transform: pages fill the same box, and a
 * size animation would re-measure a hosted map every frame. `internal` for its own test.
 */
internal fun pageSlideTransform(
    reduceMotion: Boolean,
    fromDepth: Int,
    toDepth: Int,
    slide: FiniteAnimationSpec<IntOffset>,
): ContentTransform = when {
    reduceMotion -> ContentTransform(EnterTransition.None, ExitTransition.None, targetContentZIndex = 0f, sizeTransform = null)
    // In: the new page slides in over the one beneath, which stays still until it is covered.
    toDepth >= fromDepth -> ContentTransform(
        targetContentEnter = slideInHorizontally(animationSpec = slide) { fullWidth -> fullWidth },
        initialContentExit = ExitTransition.None,
        targetContentZIndex = PAGE_ON_TOP,
        sizeTransform = null,
    )
    // Back: the page slides out to the right, drawn over the page it uncovers, which is in place from the first frame.
    else -> ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = slideOutHorizontally(animationSpec = slide) { fullWidth -> fullWidth },
        targetContentZIndex = PAGE_UNDERNEATH,
        sizeTransform = null,
    )
}

/** Z orders for [pageSlideTransform]: an arriving page goes over what it covers, and a page uncovered by Back goes under the one leaving. */
private const val PAGE_ON_TOP = 1f
private const val PAGE_UNDERNEATH = -1f

/**
 * A page that slides in from the right **over** content that stays composed beneath it ([visible] turning true), and back out to
 * the right ([visible] turning false), uncovering it: the find opened over the Journal from a map bubble (scout F3), whose view
 * beneath must come back exactly as it was. It shows at once, without a slide, if it is already [visible] when first composed (a
 * return to the Journal tab with the find still open).
 *
 * While it slides in, nothing beneath it takes a touch, over the whole of this box, the covered part and the part not yet covered
 * alike: the content beneath is the page being covered, which takes no touch on the way in (as [PageSlide]'s covered page does),
 * and the find over the view swallowed every touch over its full area before this existed (`JournalTab.kt`, its opaque Surface).
 * While it slides out it takes no touch itself, so the view it uncovers has every touch from the start. Under reduced motion it
 * appears and goes at once.
 */
@Composable
fun SlideOverPage(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = LocalReduceMotion.current
    val slide = MotionTokens.pageSlideSpec<IntOffset>()
    val inertBack = rememberInertBackOwner()
    // A fresh state settled at the current value on every change under reduced motion, so nothing plays (as TabChromeFade).
    val state = remember(if (reduceMotion) visible else null) { MutableTransitionState(visible) }
    state.targetState = visible
    val entering = visible && !state.isIdle
    Box(modifier = modifier.fillMaxSize()) {
        if (entering) {
            // Over the content beneath, under the arriving page: takes every touch the page does not, until it has arrived.
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    },
            )
        }
        AnimatedVisibility(
            visibleState = state,
            modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(animationSpec = slide) { fullWidth -> fullWidth },
            exit = slideOutHorizontally(animationSpec = slide) { fullWidth -> fullWidth },
            label = "slideOverPage",
        ) {
            LeavingPageFrame(leaving = !visible, inertBack = inertBack, pageColor = Color.Transparent) { content() }
        }
    }
}

/**
 * A page's frame: while [leaving], no touch, no semantics but the leaving marker, and Back handlers that nothing presses. One
 * provider call either way (an empty set while not leaving), so starting to leave does not rebuild the page.
 */
@Composable
private fun LeavingPageFrame(leaving: Boolean, inertBack: InertBack, pageColor: Color, content: @Composable () -> Unit) {
    val inertProvided: Array<ProvidedValue<*>> = if (leaving) {
        arrayOf(
            LocalNavigationEventDispatcherOwner provides inertBack,
            LocalOnBackPressedDispatcherOwner provides inertBack,
        )
    } else {
        emptyArray<ProvidedValue<*>>()
    }
    CompositionLocalProvider(*inertProvided) {
        Box(
            Modifier
                .fillMaxSize()
                .background(pageColor)
                .leavingTakesNoTouches(leaving)
                .then(if (leaving) Modifier.clearAndSetSemantics { pageLeaving = true } else Modifier.semantics { pageLeaving = false }),
        ) { content() }
    }
}
