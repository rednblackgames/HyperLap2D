package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.utils.AutoLayoutSolver;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A small picture of an auto layout, drawn the way the layout works it out: the composite with its
 * padding, the gaps between the children, the children themselves, and the order they go in.
 *
 * Growing children are told apart from the others, stretched ones carry a mark across, the sides a
 * composite fits to its content have inward arrows, and children left out of the layout are only
 * outlined where they are. Made-up children, when there are none, are drawn faint.
 */
public class AutoLayoutPreview extends Actor {

    private static final float PAD = 10f;
    private static final float MARK = 4f;

    private final Color backgroundColor = new Color(0f, 0f, 0f, 0.18f);
    private final Color frameColor = new Color(1f, 1f, 1f, 0.06f);
    private final Color containerFill = new Color(1f, 1f, 1f, 0.035f);
    private final Color containerLine = new Color(1f, 1f, 1f, 0.5f);
    private final Color contentLine = new Color(1f, 1f, 1f, 0.16f);
    private final Color paddingColor = new Color(0.96f, 0.62f, 0.26f, 0.2f);
    private final Color fitColor = new Color(0.96f, 0.62f, 0.26f, 0.95f);
    private final Color gapColor = new Color(0.72f, 0.48f, 1f, 0.22f);
    private final Color gapLine = new Color(0.72f, 0.48f, 1f, 0.55f);
    private final Color cellLine = new Color(1f, 1f, 1f, 0.14f);
    private final Color itemColor = new Color(0.36f, 0.62f, 1f, 1f);
    private final Color growColor = new Color(0.25f, 0.82f, 0.68f, 1f);
    private final Color highlightColor = new Color(1f, 0.62f, 0.25f, 1f);
    private final Color markColor = new Color(1f, 1f, 1f, 0.75f);
    private final Color flowColor = new Color(1f, 1f, 1f, 0.4f);
    private final Color ignoredColor = new Color(1f, 1f, 1f, 0.35f);
    private final Color tmp = new Color();

    private final AutoLayoutPreviewData data;
    private Runnable refresher;
    private ShapeDrawer sd;

    private final Rectangle bounds = new Rectangle();
    private final Rectangle rect = new Rectangle();
    private final Vector2 cell = new Vector2();
    private final Array<AutoLayoutSolver.Item> order = new Array<>();
    private float scale, offsetX, offsetY;

    public AutoLayoutPreview(AutoLayoutPreviewData data) {
        this.data = data;
    }

    /** Called before every drawing, to read the scene again. */
    public void setRefresher(Runnable refresher) {
        this.refresher = refresher;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null && sd == null) {
            sd = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sd == null) return;
        if (refresher != null) refresher.run();
        sd.update();

        sd.setColor(backgroundColor);
        sd.filledRectangle(getX(), getY(), getWidth(), getHeight());
        sd.setColor(frameColor);
        sd.rectangle(getX(), getY(), getWidth(), getHeight(), 1f);

        if (!data.valid) return;

        AutoLayoutComponent layout = data.settings;
        AutoLayoutSolver.arrange(layout, data.width, data.height, data.items);
        if (!fit()) return;

        drawContainer(layout);
        if (layout.mode == AutoLayoutComponent.Mode.GRID) drawGrid(layout);
        else drawGaps(layout);
        drawItems(layout);
        drawFlow(layout);
        drawIgnored();
        drawFitMarks(layout);
    }

    /** Scales the composite and everything around it into the widget. */
    private boolean fit() {
        bounds.set(0, 0, data.width, data.height);
        for (AutoLayoutSolver.Item item : data.items) {
            bounds.merge(rect.set(item.x, item.y, item.outWidth, item.outHeight));
        }
        for (Rectangle ignored : data.ignored) bounds.merge(ignored);
        if (bounds.width <= 0 || bounds.height <= 0) return false;

        float roomW = getWidth() - PAD * 2;
        float roomH = getHeight() - PAD * 2;
        scale = Math.min(roomW / bounds.width, roomH / bounds.height);
        offsetX = getX() + (getWidth() - bounds.width * scale) / 2 - bounds.x * scale;
        offsetY = getY() + (getHeight() - bounds.height * scale) / 2 - bounds.y * scale;
        return true;
    }

    private float sx(float x) {
        return offsetX + x * scale;
    }

    private float sy(float y) {
        return offsetY + y * scale;
    }

    private void drawContainer(AutoLayoutComponent layout) {
        float w = data.width, h = data.height;
        sd.setColor(containerFill);
        fill(0, 0, w, h);

        // padding: a band around the content
        sd.setColor(paddingColor);
        float innerBottom = Math.min(layout.padBottom, h);
        float innerTop = Math.max(innerBottom, h - layout.padTop);
        fill(0, 0, w, innerBottom);
        fill(0, innerTop, w, h - innerTop);
        fill(0, innerBottom, Math.min(layout.padLeft, w), innerTop - innerBottom);
        float right = Math.max(0, w - layout.padRight);
        fill(right, innerBottom, w - right, innerTop - innerBottom);

        sd.setColor(contentLine);
        dashedRect(Math.min(layout.padLeft, w), innerBottom, right - Math.min(layout.padLeft, w), innerTop - innerBottom, 1f, 3f);

        sd.setColor(containerLine);
        sd.rectangle(sx(0), sy(0), w * scale, h * scale, 1f);
    }

    /** The room between neighbours along a row or a column, across the content. */
    private void drawGaps(AutoLayoutComponent layout) {
        boolean row = layout.mode == AutoLayoutComponent.Mode.ROW;
        layoutOrder(layout);
        float crossStart = row ? layout.padBottom : layout.padLeft;
        float crossEnd = row ? data.height - layout.padTop : data.width - layout.padRight;
        if (crossEnd <= crossStart) return;

        for (int i = 0; i + 1 < order.size; i++) {
            AutoLayoutSolver.Item a = order.get(i);
            AutoLayoutSolver.Item b = order.get(i + 1);
            if (row) {
                float from = a.x + a.outWidth, to = b.x;
                if (to - from <= 0) continue;
                gapBand(from, crossStart, to - from, crossEnd - crossStart, true);
            } else {
                float from = b.y + b.outHeight, to = a.y;
                if (to - from <= 0) continue;
                gapBand(crossStart, from, crossEnd - crossStart, to - from, false);
            }
        }
    }

    /** The cells of the grid, dashed, and the gaps between its columns and rows. */
    private void drawGrid(AutoLayoutComponent layout) {
        int n = data.items.size;
        if (n == 0) return;
        AutoLayoutSolver.cellSize(layout, data.items, cell);
        int columns = AutoLayoutSolver.gridColumns(layout, n);
        int rows = AutoLayoutSolver.gridRows(layout, n);
        float top = data.height - layout.padTop;
        float gridWidth = columns * cell.x + (columns - 1) * layout.gap;
        float gridHeight = rows * cell.y + (rows - 1) * layout.rowGap;

        for (int c = 0; c + 1 < columns; c++) {
            float x = layout.padLeft + (c + 1) * cell.x + c * layout.gap;
            if (layout.gap > 0) gapBand(x, top - gridHeight, layout.gap, gridHeight, true);
        }
        for (int r = 0; r + 1 < rows; r++) {
            float y = top - (r + 1) * cell.y - r * layout.rowGap;
            if (layout.rowGap > 0) gapBand(layout.padLeft, y - layout.rowGap, gridWidth, layout.rowGap, false);
        }

        sd.setColor(cellLine);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                float x = layout.padLeft + c * (cell.x + layout.gap);
                float y = top - r * (cell.y + layout.rowGap) - cell.y;
                dashedRect(x, y, cell.x, cell.y, 1f, 2f);
            }
        }
    }

    private void gapBand(float x, float y, float w, float h, boolean vertical) {
        sd.setColor(gapColor);
        fill(x, y, w, h);
        // a line down the middle, so even a thin gap reads as one
        sd.setColor(gapLine);
        if (vertical) dashed(sx(x + w / 2), sy(y), sx(x + w / 2), sy(y + h), 1f, 2f);
        else dashed(sx(x), sy(y + h / 2), sx(x + w), sy(y + h / 2), 1f, 2f);
    }

    private void drawItems(AutoLayoutComponent layout) {
        boolean anyHighlighted = data.highlighted >= 0;
        for (int i = 0; i < data.items.size; i++) {
            AutoLayoutSolver.Item item = data.items.get(i);
            boolean highlighted = i == data.highlighted;
            Color base = highlighted ? highlightColor : item.grow > 0 && layout.mode != AutoLayoutComponent.Mode.GRID ? growColor : itemColor;

            float fillAlpha = data.sample ? 0.18f : anyHighlighted && !highlighted ? 0.22f : 0.45f;
            float lineAlpha = data.sample ? 0.5f : anyHighlighted && !highlighted ? 0.45f : 0.95f;

            sd.setColor(tmp.set(base).mul(1, 1, 1, fillAlpha));
            fill(item.x, item.y, item.outWidth, item.outHeight);
            sd.setColor(tmp.set(base).mul(1, 1, 1, lineAlpha));
            if (data.sample) dashedRect(item.x, item.y, item.outWidth, item.outHeight, 1f, 3f);
            else sd.rectangle(sx(item.x), sy(item.y), item.outWidth * scale, item.outHeight * scale, 1f);

            sd.setColor(markColor);
            if (sizesWidth(layout, item)) doubleArrow(item, true);
            if (sizesHeight(layout, item)) doubleArrow(item, false);
        }
    }

    private boolean sizesWidth(AutoLayoutComponent layout, AutoLayoutSolver.Item item) {
        return AutoLayoutSolver.sizesWidth(layout, item.grow, item.alignSelf);
    }

    private boolean sizesHeight(AutoLayoutComponent layout, AutoLayoutSolver.Item item) {
        return AutoLayoutSolver.sizesHeight(layout, item.grow, item.alignSelf);
    }

    /** An arrow pointing both ways across the child: the layout gives it its size on that axis. */
    private void doubleArrow(AutoLayoutSolver.Item item, boolean horizontal) {
        float x = sx(item.x), y = sy(item.y), w = item.outWidth * scale, h = item.outHeight * scale;
        float inset = 3f;
        if (horizontal) {
            if (w < MARK * 4) return;
            float cy = y + h / 2;
            float from = x + inset, to = x + w - inset;
            sd.line(from, cy, to, cy, 1f);
            sd.filledTriangle(from, cy, from + MARK, cy + MARK * 0.6f, from + MARK, cy - MARK * 0.6f);
            sd.filledTriangle(to, cy, to - MARK, cy + MARK * 0.6f, to - MARK, cy - MARK * 0.6f);
        } else {
            if (h < MARK * 4) return;
            float cx = x + w / 2;
            float from = y + inset, to = y + h - inset;
            sd.line(cx, from, cx, to, 1f);
            sd.filledTriangle(cx, from, cx - MARK * 0.6f, from + MARK, cx + MARK * 0.6f, from + MARK);
            sd.filledTriangle(cx, to, cx - MARK * 0.6f, to - MARK, cx + MARK * 0.6f, to - MARK);
        }
    }

    /** A line through the children in the order the layout takes them, ending in an arrow. */
    private void drawFlow(AutoLayoutComponent layout) {
        layoutOrder(layout);
        if (order.size < 2) return;

        sd.setColor(flowColor);
        float px = 0, py = 0, lx = 0, ly = 0;
        for (int i = 0; i < order.size; i++) {
            AutoLayoutSolver.Item item = order.get(i);
            float cx = sx(item.x + item.outWidth / 2), cy = sy(item.y + item.outHeight / 2);
            if (i > 0) {
                sd.line(px, py, cx, cy, 1f);
                lx = px;
                ly = py;
            }
            sd.filledCircle(cx, cy, i == 0 ? 2.5f : 1.5f);
            px = cx;
            py = cy;
        }

        float angle = MathUtils.atan2(py - ly, px - lx);
        float back = MARK * 1.6f;
        float bx = px - MathUtils.cos(angle) * back, by = py - MathUtils.sin(angle) * back;
        float nx = -MathUtils.sin(angle) * MARK * 0.8f, ny = MathUtils.cos(angle) * MARK * 0.8f;
        sd.filledTriangle(px, py, bx + nx, by + ny, bx - nx, by - ny);
    }

    private void drawIgnored() {
        sd.setColor(ignoredColor);
        for (Rectangle ignored : data.ignored) {
            dashedRect(ignored.x, ignored.y, ignored.width, ignored.height, 1f, 3f);
        }
    }

    /** Arrows pointing in on the sides the composite fits to its content. */
    private void drawFitMarks(AutoLayoutComponent layout) {
        sd.setColor(fitColor);
        float x0 = sx(0), y0 = sy(0), x1 = sx(data.width), y1 = sy(data.height);
        float cx = (x0 + x1) / 2, cy = (y0 + y1) / 2;
        if (layout.fitWidth) {
            sd.filledTriangle(x0 - MARK, cy + MARK, x0 - MARK, cy - MARK, x0 + 1, cy);
            sd.filledTriangle(x1 + MARK, cy + MARK, x1 + MARK, cy - MARK, x1 - 1, cy);
        }
        if (layout.fitHeight) {
            sd.filledTriangle(cx - MARK, y1 + MARK, cx + MARK, y1 + MARK, cx, y1 - 1);
            sd.filledTriangle(cx - MARK, y0 - MARK, cx + MARK, y0 - MARK, cx, y0 + 1);
        }
    }

    /** The children in the order the layout places them: a reversed row starts from its last. */
    private void layoutOrder(AutoLayoutComponent layout) {
        order.clear();
        order.addAll(data.items);
        if (layout.reverse && layout.mode != AutoLayoutComponent.Mode.GRID) order.reverse();
    }

    private void fill(float x, float y, float w, float h) {
        if (w <= 0 || h <= 0) return;
        sd.filledRectangle(sx(x), sy(y), w * scale, h * scale);
    }

    private void dashedRect(float x, float y, float w, float h, float lineWidth, float dash) {
        if (w <= 0 || h <= 0) return;
        float x0 = sx(x), y0 = sy(y), x1 = sx(x + w), y1 = sy(y + h);
        dashed(x0, y0, x1, y0, lineWidth, dash);
        dashed(x1, y0, x1, y1, lineWidth, dash);
        dashed(x1, y1, x0, y1, lineWidth, dash);
        dashed(x0, y1, x0, y0, lineWidth, dash);
    }

    private void dashed(float x1, float y1, float x2, float y2, float lineWidth, float dash) {
        float dx = x2 - x1, dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001f) return;
        float nx = dx / length, ny = dy / length;
        for (float d = 0; d < length; d += dash * 2) {
            float e = Math.min(d + dash, length);
            sd.line(x1 + nx * d, y1 + ny * d, x1 + nx * e, y1 + ny * e, lineWidth);
        }
    }
}
