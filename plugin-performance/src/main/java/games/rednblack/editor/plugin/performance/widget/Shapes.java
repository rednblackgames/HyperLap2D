package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * One shape drawer for the whole panel. Every widget draws with the stage's batch, so they can share it,
 * and sharing keeps the panel to a single texture - which is the least a profiler owes the frame it measures.
 */
public final class Shapes {

    private static final float HALF_PI = MathUtils.PI * 0.5f;

    private static TextureRegion pixel;
    private static ShapeDrawer drawer;
    private static Batch owner;

    private Shapes() {
    }

    /** Hands over the white region the drawer paints with, taken from the plugin atlas. */
    public static void setPixel(TextureRegion region) {
        //a few pixels in from the edge: linear filtering would otherwise bleed the neighbours in
        pixel = region == null ? null : new TextureRegion(region, 2, 2, 4, 4);
        drawer = null;
        owner = null;
    }

    /** @return the shared drawer, or null when the plugin atlas could not be loaded */
    public static ShapeDrawer get(Batch batch) {
        if (pixel == null) return null;

        if (drawer == null || owner != batch) {
            drawer = new ShapeDrawer(batch, pixel);
            owner = batch;
        }
        return drawer;
    }

    /**
     * A rectangle with the corners taken off.
     * <p>
     * The pieces are cut so that none of them overlap: these fills are translucent, and anywhere two of
     * them covered the same pixel that pixel would come out twice as dark. That is why the corners are
     * quarter sectors confined to their own square rather than whole circles.
     */
    public static void roundedRect(ShapeDrawer drawer, float x, float y, float width, float height, float radius, Color color) {
        float r = Math.min(radius, Math.min(width, height) * 0.5f);
        if (r <= 1f) {
            drawer.filledRectangle(x, y, width, height, color);
            return;
        }

        drawer.filledRectangle(x + r, y, width - r - r, height, color);
        drawer.filledRectangle(x, y + r, r, height - r - r, color);
        drawer.filledRectangle(x + width - r, y + r, r, height - r - r, color);

        //a quarter circle this small is a few triangles at most, and there are hundreds of them a frame
        int sides = Math.max(2, Math.round(r * 0.9f));

        drawer.setColor(color);
        drawer.sector(x + r, y + r, r, MathUtils.PI, HALF_PI, sides);
        drawer.sector(x + width - r, y + r, r, MathUtils.PI + HALF_PI, HALF_PI, sides);
        drawer.sector(x + width - r, y + height - r, r, 0f, HALF_PI, sides);
        drawer.sector(x + r, y + height - r, r, HALF_PI, HALF_PI, sides);
    }

    /** A line broken into dashes, for the marks that are a reference and not a measurement. */
    public static void dashedLine(ShapeDrawer drawer, float x1, float y, float x2, float dash, float gap, Color color) {
        drawer.setColor(color);
        for (float x = x1; x < x2; x += dash + gap) {
            drawer.filledRectangle(x, y, Math.min(dash, x2 - x), 1f, color);
        }
    }
}
