package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.plugin.performance.data.Fmt;
import games.rednblack.editor.plugin.performance.data.Metric;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.editor.plugin.performance.data.Scope;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * The last five seconds of frames, stacked: what the scene cost, what the editor added on top, and what the
 * frame spent outside either.
 * <p>
 * The budget line is the point of the whole chart - a frame time means nothing until you can see where it
 * has to fit. Columns are drawn one pixel wide and, when there are more frames than pixels, each column
 * keeps the worst frame it covers: an average would quietly hide the very spikes worth finding.
 */
public class FrameGraph extends Widget {

    private static final float MIN_SCALE = 2f;

    private final PerformanceModel model;
    private final StringBuilder text = new StringBuilder(64);
    private final GlyphLayout layout = new GlyphLayout();
    private final BitmapFont font;

    private Scope scope = Scope.BOTH;
    private float scale = MIN_SCALE;
    private float hoverX = -1f;

    public FrameGraph(PerformanceModel model) {
        this.model = model;
        this.font = VisUI.getSkin().getFont("small-font");

        addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                hoverX = x;
                return false;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (pointer == -1) hoverX = -1f;
            }
        });
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public float getPrefHeight() {
        return 150f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer drawer = Shapes.get(batch);
        if (drawer == null) return;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        Shapes.roundedRect(drawer, x, y, width, height, 6f, Palette.PANEL_BG);

        Metric total = model.series(PerformanceModel.SERIES_MS, Scope.BOTH);
        int samples = total.count();
        if (samples < 2) {
            batch.setColor(Color.WHITE);
            return;
        }

        updateScale(total);
        drawGrid(drawer, batch, x, y, width, height);
        drawColumns(drawer, x, y, width, height, samples);
        drawBudget(drawer, batch, x, y, width, height);
        drawLegend(batch, x, y, width, height);
        drawHover(drawer, batch, x, y, width, height, samples);

        batch.setColor(Color.WHITE);
    }

    private void updateScale(Metric total) {
        float peak = scope == Scope.BOTH ? total.max() : model.series(PerformanceModel.SERIES_MS, scope).max();
        float target = Math.max(peak * 1.15f, Math.max(model.budgetMillis() * 1.4f, MIN_SCALE));
        //up quickly so a spike is never clipped, down slowly so the chart does not breathe
        scale += (target - scale) * (target > scale ? 0.4f : 0.02f);
    }

    private void drawGrid(ShapeDrawer drawer, Batch batch, float x, float y, float width, float height) {
        float step = niceStep(scale / 4f);

        font.setColor(Palette.TEXT_MUTED);
        for (float value = step; value < scale; value += step) {
            float lineY = y + value / scale * height;
            if (lineY > y + height - 2f) break;

            drawer.filledRectangle(x + 1f, lineY, width - 2f, 1f, Palette.GRID);

            Fmt.clear(text);
            Fmt.number(text, value, value < 10f ? 1 : 0);
            layout.setText(font, text);
            font.draw(batch, text, x + width - layout.width - 5f, lineY + layout.height + 3f);
        }
        font.setColor(Color.WHITE);
    }

    private void drawColumns(ShapeDrawer drawer, float x, float y, float width, float height, int samples) {
        Metric frame = model.series(PerformanceModel.SERIES_MS, Scope.BOTH);
        Metric runtime = model.series(PerformanceModel.SERIES_MS, Scope.RUNTIME);
        Metric editor = model.series(PerformanceModel.SERIES_MS, Scope.EDITOR);

        int columns = columnsFor(width, samples);
        if (columns <= 0) return;
        float columnWidth = width / columns;

        for (int column = 0; column < columns; column++) {
            int newest = frameIndex(column, columns, samples);
            int oldest = frameIndex(column - 1, columns, samples);

            int worst = newest;
            float worstValue = -1f;
            for (int framesAgo = newest; framesAgo <= oldest && framesAgo < samples; framesAgo++) {
                float value = layerTotal(frame, runtime, editor, framesAgo);
                if (value > worstValue) {
                    worstValue = value;
                    worst = framesAgo;
                }
            }

            float columnX = x + column * columnWidth;
            float bottom = y;

            if (scope != Scope.EDITOR) {
                bottom += bar(drawer, columnX, columnWidth, bottom, y + height, runtime.get(worst), height, Palette.RUNTIME);
            }
            if (scope != Scope.RUNTIME) {
                bottom += bar(drawer, columnX, columnWidth, bottom, y + height, editor.get(worst), height, Palette.EDITOR);
            }
            if (scope == Scope.BOTH) {
                float rest = frame.get(worst) - runtime.get(worst) - editor.get(worst);
                if (rest > 0f) bar(drawer, columnX, columnWidth, bottom, y + height, rest, height, Palette.OTHER);
            }
        }
    }

    private float layerTotal(Metric frame, Metric runtime, Metric editor, int framesAgo) {
        switch (scope) {
            case RUNTIME:
                return runtime.get(framesAgo);
            case EDITOR:
                return editor.get(framesAgo);
            default:
                return frame.get(framesAgo);
        }
    }

    private float bar(ShapeDrawer drawer, float columnX, float columnWidth, float bottom, float ceiling, float value, float height, Color color) {
        float barHeight = value / scale * height;
        if (barHeight <= 0f) return 0f;
        if (bottom + barHeight > ceiling) barHeight = ceiling - bottom;
        if (barHeight <= 0f) return 0f;

        drawer.filledRectangle(columnX, bottom, columnWidth, barHeight, color);
        return barHeight;
    }

    /** Never more columns than there are frames to show: past that it is the same frame drawn twice. */
    private static int columnsFor(float width, int samples) {
        return Math.max(0, Math.min((int) width, samples));
    }

    private void drawBudget(ShapeDrawer drawer, Batch batch, float x, float y, float width, float height) {
        float budgetY = y + model.budgetMillis() / scale * height;
        if (budgetY > y + height - 1f) return;

        Shapes.dashedLine(drawer, x + 2f, budgetY, x + width - 2f, 5f, 4f, Palette.BUDGET);

        Fmt.clear(text);
        Fmt.number(text, model.budgetMillis(), 1);
        text.append(" ms budget");
        font.setColor(Palette.WARN);
        layout.setText(font, text);
        font.draw(batch, text, x + 6f, budgetY + layout.height + 4f);
        font.setColor(Color.WHITE);
    }

    private void drawLegend(Batch batch, float x, float y, float width, float height) {
        float legendY = y + height - 6f;
        float legendX = x + 6f;

        if (scope != Scope.EDITOR) legendX = legendEntry(batch, legendX, legendY, "Scene", Palette.RUNTIME);
        if (scope != Scope.RUNTIME) legendX = legendEntry(batch, legendX, legendY, "Editor", Palette.EDITOR);
        if (scope == Scope.BOTH) legendEntry(batch, legendX, legendY, "Other", Palette.OTHER);
    }

    private float legendEntry(Batch batch, float x, float y, String label, Color color) {
        ShapeDrawer drawer = Shapes.get(batch);
        drawer.filledRectangle(x, y - 7f, 7f, 7f, color);

        font.setColor(Palette.TEXT_MUTED);
        layout.setText(font, label);
        font.draw(batch, label, x + 11f, y);
        font.setColor(Color.WHITE);

        return x + 11f + layout.width + 12f;
    }

    private void drawHover(ShapeDrawer drawer, Batch batch, float x, float y, float width, float height, int samples) {
        if (hoverX < 0f || hoverX > width) return;

        int columns = columnsFor(width, samples);
        if (columns <= 0) return;

        float columnWidth = width / columns;
        int column = Math.max(0, Math.min(columns - 1, (int) (hoverX / columnWidth)));
        int framesAgo = frameIndex(column, columns, samples);

        Metric frame = model.series(PerformanceModel.SERIES_MS, Scope.BOTH);
        Metric runtime = model.series(PerformanceModel.SERIES_MS, Scope.RUNTIME);
        Metric editor = model.series(PerformanceModel.SERIES_MS, Scope.EDITOR);
        Metric draws = model.series(PerformanceModel.SERIES_DRAW_CALLS, Scope.RUNTIME);

        drawer.filledRectangle(x + column * columnWidth, y + 1f, Math.max(1f, columnWidth), height - 2f, Palette.TEXT_MUTED);

        Fmt.clear(text);
        text.append("frame ");
        Fmt.number(text, frame.get(framesAgo), 2);
        text.append(" ms   scene ");
        Fmt.number(text, runtime.get(framesAgo), 2);
        text.append("   editor ");
        Fmt.number(text, editor.get(framesAgo), 2);
        text.append("   ");
        Fmt.number(text, draws.get(framesAgo), 0);
        text.append(" draws");

        layout.setText(font, text);
        float boxWidth = layout.width + 14f;
        float boxHeight = layout.height + 12f;
        float boxX = Math.min(Math.max(x + hoverX - boxWidth * 0.5f, x + 2f), x + width - boxWidth - 2f);
        float boxY = y + height - boxHeight - 18f;

        Shapes.roundedRect(drawer, boxX, boxY, boxWidth, boxHeight, 4f, Palette.TOOLTIP_BG);

        font.setColor(Palette.TEXT);
        font.draw(batch, text, boxX + 7f, boxY + boxHeight - 6f);
        font.setColor(Color.WHITE);
    }

    /** Newest frame on the right. Column -1 is asked for the oldest frame a column covers. */
    private int frameIndex(int column, int columns, int samples) {
        if (columns <= 1) return 0;
        int index = (int) ((columns - 1 - column) * (samples - 1L) / (columns - 1));
        return Math.max(0, Math.min(samples - 1, index));
    }

    private static float niceStep(float raw) {
        if (raw <= 0f) return 1f;

        float magnitude = (float) Math.pow(10, Math.floor(Math.log10(raw)));
        float normalized = raw / magnitude;
        if (normalized < 1.5f) return magnitude;
        if (normalized < 3.5f) return 2f * magnitude;
        if (normalized < 7.5f) return 5f * magnitude;
        return 10f * magnitude;
    }
}
