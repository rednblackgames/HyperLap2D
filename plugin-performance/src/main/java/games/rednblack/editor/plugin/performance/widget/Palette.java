package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;

/**
 * The panel reads at a glance or not at all, so it keeps to a handful of colours: one per series, three
 * for how a number is doing, and greys for everything that is only there to be read past.
 */
public final class Palette {

    /** The scene's own cost. The editor's accent blue, because this is the subject. */
    public static final Color RUNTIME = new Color(0x1ba1e2ff);
    public static final Color RUNTIME_DIM = new Color(0x1ba1e255);

    /** What the editor costs on top. Kept cool and quiet: it is context, not the point. */
    public static final Color EDITOR = new Color(0x8e7cc3ff);
    public static final Color EDITOR_DIM = new Color(0x8e7cc355);

    /** Clearing, swapping, the driver: the part of the frame nobody asked for. */
    public static final Color OTHER = new Color(0x5a6470ff);

    public static final Color OK = new Color(0x4caf6eff);
    public static final Color WARN = new Color(0xe6a23cff);
    public static final Color BAD = new Color(0xe05252ff);

    public static final Color TEXT = new Color(0xdededeff);
    public static final Color TEXT_MUTED = new Color(0x8a8f98ff);

    public static final Color PANEL_BG = new Color(0x00000044);
    /** Opaque enough to read a line of text off the chart it covers. */
    public static final Color TOOLTIP_BG = new Color(0x161a1ef2);
    public static final Color TILE_BG = new Color(0xffffff0e);
    public static final Color GRID = new Color(0xffffff14);
    public static final Color BUDGET = new Color(0xe6a23c88);

    /** Logic, interpolation and render systems, in the order {@code SystemProfiler} buckets them. */
    public static final Color[] BUCKET = {
            new Color(0x1ba1e2ff),
            new Color(0x4caf6eff),
            new Color(0x8e7cc3ff)
    };

    private Palette() {
    }

    /** Green below the budget, amber up to half again, red past it. */
    public static Color state(float value, float budget) {
        if (value <= budget) return OK;
        if (value <= budget * 1.5f) return WARN;
        return BAD;
    }
}
