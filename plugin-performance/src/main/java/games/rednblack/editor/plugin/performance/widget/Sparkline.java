package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import games.rednblack.editor.plugin.performance.data.Metric;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The shape of a number over the last few seconds, small enough to sit under the number itself. It says
 * whether what you are reading is where it has been, which a single value never can.
 */
public class Sparkline extends Widget {

    private final Color fill = new Color();
    private final Color line = new Color();

    private Metric metric;

    public Sparkline() {
        setSeriesColor(Palette.RUNTIME);
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public void setSeriesColor(Color color) {
        line.set(color);
        fill.set(color).mul(1f, 1f, 1f, 0.28f);
    }

    @Override
    public float getPrefHeight() {
        return 20f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer drawer = Shapes.get(batch);
        if (drawer == null || metric == null || metric.count() < 2) return;

        float width = getWidth();
        float height = getHeight();
        float x = getX();
        float y = getY();

        //a sparkline is about movement, not magnitude: scaled from zero, a steady 170 fps is a solid block
        float low = metric.min();
        float range = metric.max() - low;
        boolean flat = range < Math.max(0.0001f, Math.abs(metric.max()) * 0.02f);

        int samples = metric.count();
        float previousX = 0f, previousY = 0f;

        for (int column = 0; column < width; column++) {
            int framesAgo = (int) ((width - 1 - column) * (samples - 1) / Math.max(1f, width - 1));
            float value = metric.get(framesAgo);
            float normalized = flat ? 0.5f : (value - low) / range;
            float barHeight = 2f + normalized * (height - 4f);

            drawer.filledRectangle(x + column, y, 1f, barHeight, fill);

            float pointX = x + column;
            float pointY = y + barHeight;
            if (column > 0) drawer.line(previousX, previousY, pointX, pointY, line, 1f);
            previousX = pointX;
            previousY = pointY;
        }

        batch.setColor(Color.WHITE);
    }
}
