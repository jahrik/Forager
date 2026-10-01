package org.maplibre.android.location;

/**
 * Test-only window onto the SDK's own placement decision, in the SDK's own package because
 * {@code LocationComponentPositionManager} and its {@code update} are package-private (dispatch 2026-09-28-318).
 *
 * <p>{@code LocationLayerController.applyStyle} (13.5.0, read with javap) removes and re-adds the puck's layers only
 * when {@code update} returns {@code true}, and {@code update} returns {@code true} only when the position it is given
 * differs from the one it holds. The probe holds the position the controller was built with, the way
 * {@code initializeComponents} builds it on a style load, and reports what {@code update} says to each later options.
 * The style is {@code null}: {@code update} never touches it.
 */
public final class PuckPlacementProbe {
    private final LocationComponentPositionManager manager;

    /** The position manager as {@code LocationLayerController.initializeComponents} builds it from {@code initial}. */
    public PuckPlacementProbe(LocationComponentOptions initial) {
        manager = new LocationComponentPositionManager(null, initial.layerAbove(), initial.layerBelow(), initial.bearingOnTop());
    }

    /** Whether the SDK would take its puck layers out and add them again at {@code options}' position. */
    public boolean wouldReplaceLayers(LocationComponentOptions options) {
        return manager.update(options.layerAbove(), options.layerBelow(), options.bearingOnTop());
    }
}
