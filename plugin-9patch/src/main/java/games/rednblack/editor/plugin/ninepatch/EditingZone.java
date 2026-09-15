package games.rednblack.editor.plugin.ninepatch;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.data.TenPatchVO;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Editing canvas of the stretch areas of a 9-patch, drawn with ShapeDrawer like the rest of the editor.
 * <p>
 * The graphic sits on a checkerboard with a ruler on top (horizontal areas) and on the left (vertical
 * areas). Every area is a translucent band over the graphic with a guide line at each side and a handle
 * on its ruler. Interactions:
 * <ul>
 * <li>drag a guide or a ruler handle to resize an area (it never crosses its neighbours)</li>
 * <li>drag on an empty part of a ruler to create an area</li>
 * <li>click a band to select the area, right click a guide or handle to remove it</li>
 * <li>drag empty space to pan, wheel to zoom around the mouse</li>
 * </ul>
 * Horizontal areas are counted from the left and vertical areas from the bottom of the graphic, the same
 * convention {@link games.rednblack.editor.renderer.tenpatch.TenPatchDrawable} uses.
 *
 * Created by azakhary on 8/18/2015.
 */
public class EditingZone extends Actor {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    /** Thickness of the rulers. */
    private static final float RULER = 20f;
    /** Inset of the area markers inside their ruler, and width of the caps standing for the handles. */
    private static final float MARKER_INSET = 3f;
    private static final float CAP_WIDTH = 4f;
    private static final float HIT_RADIUS = 6f;
    private static final float MIN_ZOOM = 0.125f;
    private static final float MAX_ZOOM = 48f;
    private static final float INITIAL_MAX_ZOOM = 8f;
    private static final float GRID_MIN_ZOOM = 6f;
    private static final float CHECKER_CELL = 8f;
    private static final float FIT_PAD = 24f;

    private static final Color BG = new Color(0.16f, 0.16f, 0.16f, 1f);
    private static final Color CHECKER_A = new Color(0.22f, 0.22f, 0.22f, 1f);
    private static final Color CHECKER_B = new Color(0.28f, 0.28f, 0.28f, 1f);
    private static final Color RULER_BG = new Color(0.11f, 0.11f, 0.11f, 1f);
    private static final Color RULER_TICK = new Color(1f, 1f, 1f, 0.35f);
    private static final Color RULER_TEXT = new Color(1f, 1f, 1f, 0.6f);
    private static final Color GRID = new Color(1f, 1f, 1f, 0.07f);
    private static final Color IMAGE_BORDER = new Color(1f, 1f, 1f, 0.3f);
    private static final Color H_COLOR = new Color(0.3f, 0.55f, 1f, 1f);
    private static final Color V_COLOR = new Color(0.3f, 0.85f, 0.45f, 1f);
    private static final Color HOVER_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color LABEL_BG = new Color(0.05f, 0.05f, 0.05f, 0.85f);

    private static final float BAND_ALPHA = 0.16f;
    private static final float BAND_SELECTED_ALPHA = 0.3f;
    private static final float GUIDE_ALPHA = 0.6f;

    private static Texture whiteTexture;
    private static TextureRegion whiteRegion;

    private ShapeDrawer sd;
    private BitmapFont font;
    private final Color tmp = new Color();
    private final GlyphLayout glyphs = new GlyphLayout();

    private TextureRegion texture;
    private float zoom = 1f;
    private final Vector2 shift = new Vector2();
    private boolean showGrid = true;

    /** Pairs of inclusive pixel indexes, horizontal from the left, vertical from the bottom. */
    private final IntArray horizontal = new IntArray();
    private final IntArray vertical = new IntArray();

    /** Guide under the mouse: axis, pair index and side (start or end); axis is -1 when none. */
    private int overAxis = -1, overPair = -1;
    private boolean overEnd = false;
    /** Area selected by a click, editable from the side panel. */
    private int selectedAxis = -1, selectedPair = -1;
    /** Graphic pixel under the mouse, -1 when outside the graphic. */
    private int hoverPixelX = -1, hoverPixelY = -1;
    private float mouseX, mouseY;
    private boolean mouseInside;
    /** Area being created by dragging on a ruler, axis -1 when idle. */
    private int createAxis = -1, createStart, createEnd;

    public interface Listener {
        /** Stretch areas changed. */
        default void changed() {
        }

        /** The selected area changed (also fired when the selection is cleared). */
        default void selectionChanged() {
        }

        /** Zoom or pan changed. */
        default void viewChanged() {
        }

        /** Human readable description of what is under the mouse. */
        default void statusChanged(String status) {
        }
    }

    private Listener listener;

    /** 1x1 white region for ShapeDrawer; the plugin does not see the editor's shared white pixel. */
    public static TextureRegion whiteRegion() {
        if (whiteRegion == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whiteTexture = new Texture(pixmap);
            pixmap.dispose();
            whiteRegion = new TextureRegion(whiteTexture);
        }
        return whiteRegion;
    }

    /** Checkerboard filling a rectangle, the classic transparency background. */
    public static void drawCheckerboard(ShapeDrawer sd, float x, float y, float width, float height, float alpha) {
        Color a = new Color(CHECKER_A);
        Color b = new Color(CHECKER_B);
        a.a *= alpha;
        b.a *= alpha;
        sd.setColor(a);
        sd.filledRectangle(x, y, width, height);
        sd.setColor(b);
        int columns = MathUtils.ceil(width / CHECKER_CELL);
        int rows = MathUtils.ceil(height / CHECKER_CELL);
        for (int row = 0; row < rows; row++) {
            for (int column = (row & 1); column < columns; column += 2) {
                float cx = x + column * CHECKER_CELL;
                float cy = y + row * CHECKER_CELL;
                float cw = Math.min(CHECKER_CELL, x + width - cx);
                float ch = Math.min(CHECKER_CELL, y + height - cy);
                if (cw > 0 && ch > 0) sd.filledRectangle(cx, cy, cw, ch);
            }
        }
    }

    public EditingZone() {
        addListener(new InputListener() {
            private static final int NONE = 0, GUIDE = 1, PAN = 2, CREATE = 3;
            private int mode = NONE;
            private final Vector2 lastPoint = new Vector2();
            private int dragAxis = -1, dragPair = -1;
            private boolean dragEnd = false;
            private int createAnchor;
            private boolean dirty;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (texture == null) return false;
                getStage().setScrollFocus(EditingZone.this);
                updateMouse(x, y);
                dirty = false;

                if (guideCollision(x, y)) {
                    if (button == Input.Buttons.RIGHT) {
                        removeArea(overAxis, overPair);
                        overAxis = -1;
                        mode = NONE;
                        notifyChanged();
                        notifySelection();
                        updateStatus();
                        return true;
                    }
                    mode = GUIDE;
                    dragAxis = overAxis;
                    dragPair = overPair;
                    dragEnd = overEnd;
                    select(dragAxis, dragPair);
                    return true;
                }

                int rulerAxis = rulerAt(x, y);
                if (rulerAxis >= 0 && button == Input.Buttons.LEFT) {
                    int size = sizeOf(rulerAxis);
                    int pixel = MathUtils.clamp(pixelAt(rulerAxis, x, y), 0, size - 1);
                    int pair = pairContaining(rulerAxis, pixel);
                    if (pair >= 0) {
                        select(rulerAxis, pair);
                        mode = NONE;
                    } else {
                        mode = CREATE;
                        createAxis = rulerAxis;
                        createAnchor = pixel;
                        createStart = pixel;
                        createEnd = pixel;
                    }
                    return true;
                }

                if (button == Input.Buttons.LEFT && hoverPixelX >= 0) {
                    int hPair = pairContaining(HORIZONTAL, hoverPixelX);
                    int vPair = pairContaining(VERTICAL, hoverPixelY);
                    if (hPair >= 0 && vPair >= 0) {
                        // inside both bands: keep the axis already selected, otherwise prefer horizontal
                        if (selectedAxis == VERTICAL) select(VERTICAL, vPair); else select(HORIZONTAL, hPair);
                        mode = PAN;
                        lastPoint.set(x, y);
                        return true;
                    }
                    if (hPair >= 0) {
                        select(HORIZONTAL, hPair);
                    } else if (vPair >= 0) {
                        select(VERTICAL, vPair);
                    }
                }

                mode = PAN;
                lastPoint.set(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                updateMouse(x, y);
                switch (mode) {
                    case GUIDE:
                        dragGuide(dragAxis, dragPair, dragEnd, x, y);
                        overAxis = dragAxis;
                        overPair = dragPair;
                        overEnd = dragEnd;
                        dirty = true;
                        break;
                    case CREATE: {
                        int size = sizeOf(createAxis);
                        int pixel = MathUtils.clamp(pixelAt(createAxis, x, y), 0, size - 1);
                        int start = Math.min(createAnchor, pixel);
                        int end = Math.max(createAnchor, pixel);
                        // stay inside the gap the anchor sits in
                        IntArray areas = areasOf(createAxis);
                        int min = 0, max = size - 1;
                        for (int i = 0; i + 1 < areas.size; i += 2) {
                            if (areas.get(i + 1) < createAnchor) min = Math.max(min, areas.get(i + 1) + 1);
                            if (areas.get(i) > createAnchor) max = Math.min(max, areas.get(i) - 1);
                        }
                        createStart = Math.max(start, min);
                        createEnd = Math.min(end, max);
                        break;
                    }
                    case PAN:
                        shift.add(x - lastPoint.x, y - lastPoint.y);
                        lastPoint.set(x, y);
                        if (listener != null) listener.viewChanged();
                        break;
                    default:
                        break;
                }
                updateStatus();
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (mode == CREATE) {
                    int axis = createAxis;
                    int start = createStart, end = createEnd;
                    createAxis = -1;
                    if (axis >= 0 && end >= start) {
                        int pair = insertArea(axis, start, end);
                        select(axis, pair);
                        notifyChanged();
                    }
                } else if (mode == GUIDE && dirty) {
                    notifyChanged();
                }
                mode = NONE;
                updateMouse(x, y);
                updateStatus();
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                updateMouse(x, y);
                if (!guideCollision(x, y)) overAxis = -1;
                updateStatus();
                return false;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                mouseInside = true;
                if (getStage() != null) getStage().setScrollFocus(EditingZone.this);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    mouseInside = false;
                    overAxis = -1;
                    hoverPixelX = -1;
                    hoverPixelY = -1;
                    updateStatus();
                }
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                zoomAt(amountY < 0 ? 1.15f : 1f / 1.15f, x, y);
                updateMouse(x, y);
                updateStatus();
                return true;
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void notifyChanged() {
        if (listener != null) listener.changed();
    }

    private void notifySelection() {
        if (listener != null) listener.selectionChanged();
    }

    private void updateStatus() {
        if (listener == null) return;
        listener.statusChanged(buildStatus());
    }

    private String buildStatus() {
        if (texture == null) return "";
        StringBuilder status = new StringBuilder();
        if (hoverPixelX >= 0) {
            status.append("x: ").append(hoverPixelX).append("   y: ").append(hoverPixelY);
        }
        int axis = -1, pair = -1;
        if (overAxis >= 0) {
            axis = overAxis;
            pair = overPair;
        } else if (createAxis >= 0) {
            if (status.length() > 0) status.append("   ");
            status.append(axisName(createAxis)).append(" new area  ").append(createStart).append(" - ").append(createEnd)
                    .append("  (").append(createEnd - createStart + 1).append(" px)");
        } else if (hoverPixelX >= 0) {
            int hPair = pairContaining(HORIZONTAL, hoverPixelX);
            int vPair = pairContaining(VERTICAL, hoverPixelY);
            if (hPair >= 0 && (vPair < 0 || selectedAxis != VERTICAL)) {
                axis = HORIZONTAL;
                pair = hPair;
            } else if (vPair >= 0) {
                axis = VERTICAL;
                pair = vPair;
            }
        }
        if (axis >= 0) {
            if (status.length() > 0) status.append("   ");
            status.append(areaName(axis, pair)).append("  ").append(areaDescription(axis, pair));
        }
        return status.toString();
    }

    private static String axisName(int axis) {
        return axis == HORIZONTAL ? "Horizontal" : "Vertical";
    }

    /** Short name of an area, e.g. "H2" for the second horizontal area. */
    public static String areaName(int axis, int pair) {
        return (axis == HORIZONTAL ? "H" : "V") + (pair + 1);
    }

    private String areaDescription(int axis, int pair) {
        IntArray areas = areasOf(axis);
        int start = areas.get(pair * 2);
        int end = areas.get(pair * 2 + 1);
        return start + " - " + end + "  (" + (end - start + 1) + " px)";
    }

    // ------------------------------------------------------------------ model

    /**
     * @param texture region to edit
     * @param vo      stretch areas in pixels of {@code texture}
     */
    public void setTexture(TextureRegion texture, TenPatchVO vo) {
        this.texture = texture;
        horizontal.clear();
        vertical.clear();
        if (vo.horizontalStretchAreas != null) horizontal.addAll(vo.horizontalStretchAreas);
        if (vo.verticalStretchAreas != null) vertical.addAll(vo.verticalStretchAreas);
        sanitize(horizontal, texture.getRegionWidth());
        sanitize(vertical, texture.getRegionHeight());
        selectedAxis = -1;
        selectedPair = -1;
        overAxis = -1;
        createAxis = -1;
        fit(INITIAL_MAX_ZOOM);
        notifySelection();
    }

    public int[] getHorizontalStretchAreas() {
        return horizontal.toArray();
    }

    public int[] getVerticalStretchAreas() {
        return vertical.toArray();
    }

    public int getAreaCount(int axis) {
        return areasOf(axis).size / 2;
    }

    public int getSelectedAxis() {
        return selectedAxis;
    }

    public int getSelectedPair() {
        return selectedPair;
    }

    /** Start and end of the selected area, null when nothing is selected. */
    public int[] getSelectedArea() {
        if (selectedAxis < 0) return null;
        IntArray areas = areasOf(selectedAxis);
        if (selectedPair * 2 + 1 >= areas.size) return null;
        return new int[]{areas.get(selectedPair * 2), areas.get(selectedPair * 2 + 1)};
    }

    /** Bounds of the selected area on its axis: the pixels it may grow to without touching its neighbours. */
    public int[] getSelectedAreaLimits() {
        if (selectedAxis < 0) return null;
        IntArray areas = areasOf(selectedAxis);
        int index = selectedPair * 2;
        if (index + 1 >= areas.size) return null;
        int min = index - 1 >= 0 ? areas.get(index - 1) + 1 : 0;
        int max = index + 2 < areas.size ? areas.get(index + 2) - 1 : sizeOf(selectedAxis) - 1;
        return new int[]{min, max};
    }

    /** Resizes the selected area, clamped to its neighbours. Returns false when nothing is selected. */
    public boolean setSelectedArea(int start, int end) {
        int[] limits = getSelectedAreaLimits();
        if (limits == null) return false;
        IntArray areas = areasOf(selectedAxis);
        start = MathUtils.clamp(start, limits[0], limits[1]);
        end = MathUtils.clamp(end, start, limits[1]);
        areas.set(selectedPair * 2, start);
        areas.set(selectedPair * 2 + 1, end);
        notifyChanged();
        return true;
    }

    private void select(int axis, int pair) {
        if (selectedAxis == axis && selectedPair == pair) return;
        selectedAxis = axis;
        selectedPair = pair;
        notifySelection();
    }

    public void clearSelection() {
        select(-1, -1);
    }

    /** Adds an area in the middle of the widest non stretching span of the axis and selects it. */
    public boolean addArea(int axis) {
        IntArray areas = areasOf(axis);
        int size = sizeOf(axis);
        int bestStart = -1, bestLength = 0;
        int previousEnd = -1;
        for (int i = 0; i <= areas.size; i += 2) {
            int gapStart = previousEnd + 1;
            int gapEnd = i < areas.size ? areas.get(i) - 1 : size - 1;
            int length = gapEnd - gapStart + 1;
            if (length > bestLength) {
                bestLength = length;
                bestStart = gapStart;
            }
            if (i < areas.size) previousEnd = areas.get(i + 1);
        }
        if (bestLength <= 0) return false;

        int start, end;
        if (bestLength >= 3) {
            int third = bestLength / 3;
            start = bestStart + third;
            end = bestStart + bestLength - 1 - third;
        } else {
            start = bestStart;
            end = bestStart;
        }
        int pair = insertArea(axis, start, end);
        select(axis, pair);
        notifyChanged();
        return true;
    }

    /** Removes the selected area, or the last one of the axis when nothing of that axis is selected. */
    public boolean removeArea(int axis) {
        int pair = selectedAxis == axis ? selectedPair : getAreaCount(axis) - 1;
        boolean removed = removeArea(axis, pair);
        if (removed) {
            if (selectedAxis == axis) select(-1, -1);
            notifyChanged();
        }
        return removed;
    }

    /** Inserts a pair keeping the array sorted, returns its index. */
    private int insertArea(int axis, int start, int end) {
        IntArray areas = areasOf(axis);
        int insertAt = areas.size;
        for (int i = 0; i < areas.size; i += 2) {
            if (areas.get(i) > start) {
                insertAt = i;
                break;
            }
        }
        areas.insert(insertAt, end);
        areas.insert(insertAt, start);
        return insertAt / 2;
    }

    private boolean removeArea(int axis, int pair) {
        if (axis < 0) return false;
        IntArray areas = areasOf(axis);
        if (pair < 0 || pair * 2 + 1 >= areas.size) return false;
        areas.removeRange(pair * 2, pair * 2 + 1);
        if (selectedAxis == axis) {
            if (selectedPair == pair) select(-1, -1);
            else if (selectedPair > pair) selectedPair--;
        }
        return true;
    }

    private IntArray areasOf(int axis) {
        return axis == HORIZONTAL ? horizontal : vertical;
    }

    private int sizeOf(int axis) {
        return axis == HORIZONTAL ? texture.getRegionWidth() : texture.getRegionHeight();
    }

    private int pairContaining(int axis, int pixel) {
        if (pixel < 0) return -1;
        IntArray areas = areasOf(axis);
        for (int i = 0; i + 1 < areas.size; i += 2) {
            if (pixel >= areas.get(i) && pixel <= areas.get(i + 1)) return i / 2;
        }
        return -1;
    }

    /** Clamps pairs inside the graphic, sorted and non overlapping. */
    private static void sanitize(IntArray areas, int size) {
        IntArray result = new IntArray(areas.size);
        int previousEnd = -1;
        for (int i = 0; i + 1 < areas.size; i += 2) {
            int start = Math.max(areas.get(i), previousEnd + 1);
            int end = Math.min(Math.max(areas.get(i + 1), start), size - 1);
            if (start > size - 1 || start > end) continue;
            result.add(start);
            result.add(end);
            previousEnd = end;
        }
        areas.clear();
        areas.addAll(result);
    }

    private void dragGuide(int axis, int pair, boolean end, float x, float y) {
        IntArray areas = areasOf(axis);
        int size = sizeOf(axis);
        float origin = axis == HORIZONTAL ? imageLeft() : imageBottom();
        float coordinate = axis == HORIZONTAL ? x : y;

        // pixel boundary closest to the mouse, in graphic pixels
        int boundary = Math.round((coordinate - origin) / zoom);

        int startIndex = pair * 2;
        if (end) {
            int min = areas.get(startIndex);
            int max = startIndex + 2 < areas.size ? areas.get(startIndex + 2) - 1 : size - 1;
            areas.set(startIndex + 1, MathUtils.clamp(boundary - 1, min, max));
        } else {
            int min = startIndex - 1 >= 0 ? areas.get(startIndex - 1) + 1 : 0;
            int max = areas.get(startIndex + 1);
            areas.set(startIndex, MathUtils.clamp(boundary, min, max));
        }
    }

    // ------------------------------------------------------------------ geometry

    private float areaWidth() {
        return getWidth() - RULER;
    }

    private float areaHeight() {
        return getHeight() - RULER;
    }

    /** Left edge of the graphic in actor coordinates. */
    private float imageLeft() {
        return RULER + areaWidth() / 2f + shift.x - texture.getRegionWidth() * zoom / 2f;
    }

    /** Bottom edge of the graphic in actor coordinates. */
    private float imageBottom() {
        return areaHeight() / 2f + shift.y - texture.getRegionHeight() * zoom / 2f;
    }

    private float guidePosition(int axis, int pair, boolean end) {
        IntArray areas = areasOf(axis);
        float origin = axis == HORIZONTAL ? imageLeft() : imageBottom();
        int value = areas.get(pair * 2 + (end ? 1 : 0));
        return origin + (end ? value + 1 : value) * zoom;
    }

    /** Graphic pixel index along an axis for actor coordinates, may be outside the graphic. */
    private int pixelAt(int axis, float x, float y) {
        float origin = axis == HORIZONTAL ? imageLeft() : imageBottom();
        float coordinate = axis == HORIZONTAL ? x : y;
        return MathUtils.floor((coordinate - origin) / zoom);
    }

    /** Which ruler the actor coordinates are in, -1 when none. */
    private int rulerAt(float x, float y) {
        if (y >= areaHeight() && x >= RULER) return HORIZONTAL;
        if (x < RULER && y < areaHeight()) return VERTICAL;
        return -1;
    }

    private void updateMouse(float x, float y) {
        mouseX = x;
        mouseY = y;
        mouseInside = x >= 0 && y >= 0 && x <= getWidth() && y <= getHeight();
        if (texture == null) return;
        int px = pixelAt(HORIZONTAL, x, y);
        int py = pixelAt(VERTICAL, x, y);
        boolean inside = x >= RULER && y < areaHeight()
                && px >= 0 && px < texture.getRegionWidth() && py >= 0 && py < texture.getRegionHeight();
        hoverPixelX = inside ? px : -1;
        hoverPixelY = inside ? py : -1;
    }

    /**
     * Looks for a guide (line or ruler handle) under the given actor coordinates and stores it in the
     * {@code over*} fields.
     *
     * @return true when a guide was hit
     */
    private boolean guideCollision(float x, float y) {
        float best = HIT_RADIUS;
        boolean hit = false;
        boolean inHorizontalRuler = rulerAt(x, y) == HORIZONTAL;
        boolean inVerticalRuler = rulerAt(x, y) == VERTICAL;
        for (int i = 0; i < horizontal.size / 2; i++) {
            for (int side = 0; side < 2; side++) {
                if (inVerticalRuler) continue;
                float distance = Math.abs(guidePosition(HORIZONTAL, i, side == 1) - x);
                if (distance <= best) {
                    best = distance;
                    overAxis = HORIZONTAL;
                    overPair = i;
                    overEnd = side == 1;
                    hit = true;
                }
            }
        }
        for (int i = 0; i < vertical.size / 2; i++) {
            for (int side = 0; side < 2; side++) {
                if (inHorizontalRuler) continue;
                float distance = Math.abs(guidePosition(VERTICAL, i, side == 1) - y);
                if (distance <= best) {
                    best = distance;
                    overAxis = VERTICAL;
                    overPair = i;
                    overEnd = side == 1;
                    hit = true;
                }
            }
        }
        return hit;
    }

    // ------------------------------------------------------------------ view

    public float getZoom() {
        return zoom;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    /** Zooms keeping the graphic pixel under the given actor coordinates in place. */
    public void zoomAt(float factor, float x, float y) {
        if (texture == null) return;
        float newZoom = MathUtils.clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
        float px = (x - imageLeft()) / zoom;
        float py = (y - imageBottom()) / zoom;
        zoom = newZoom;
        shift.x = (x - px * zoom) - (RULER + areaWidth() / 2f - texture.getRegionWidth() * zoom / 2f);
        shift.y = (y - py * zoom) - (areaHeight() / 2f - texture.getRegionHeight() * zoom / 2f);
        if (listener != null) listener.viewChanged();
    }

    /** Zooms around the centre of the canvas. */
    public void zoomBy(float factor) {
        zoomAt(factor, RULER + areaWidth() / 2f, areaHeight() / 2f);
    }

    /** Shows the whole graphic, as big as the canvas allows up to {@code maxZoom}. */
    public void fit(float maxZoom) {
        if (texture == null) return;
        float zx = (areaWidth() - FIT_PAD * 2) / texture.getRegionWidth();
        float zy = (areaHeight() - FIT_PAD * 2) / texture.getRegionHeight();
        zoom = MathUtils.clamp(Math.min(zx, zy), MIN_ZOOM, maxZoom);
        shift.set(0, 0);
        if (listener != null) listener.viewChanged();
    }

    public void fit() {
        fit(MAX_ZOOM);
    }

    /** One graphic pixel per screen pixel, centred. */
    public void resetZoom() {
        zoom = 1f;
        shift.set(0, 0);
        if (listener != null) listener.viewChanged();
    }

    // ------------------------------------------------------------------ drawing

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            sd = new ShapeDrawer(stage.getBatch(), whiteRegion());
            font = VisUI.getSkin().getFont("small-font");
        }
    }

    private Color faded(Color color, float alpha) {
        tmp.set(color);
        tmp.a *= alpha;
        return tmp;
    }

    private Color axisColor(int axis) {
        return axis == HORIZONTAL ? H_COLOR : V_COLOR;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sd == null || texture == null) return;
        float alpha = parentAlpha * getColor().a;
        if (alpha <= 0f) return;
        sd.update();

        // ShapeDrawer geometry sits in the batch until it is flushed, so every scissor change is
        // preceded by a flush or the shapes would be drawn once the scissor is gone
        batch.flush();
        if (!clipBegin(getX(), getY(), getWidth(), getHeight())) return;

        float x = getX(), y = getY();
        float rw = texture.getRegionWidth() * zoom;
        float rh = texture.getRegionHeight() * zoom;
        float ix = x + imageLeft();
        float iy = y + imageBottom();

        // background of the canvas
        sd.setColor(faded(BG, alpha));
        sd.filledRectangle(x, y, getWidth(), getHeight());

        // checkerboard under the graphic only, clipped to the canvas
        float cx = Math.max(ix, x + RULER), cy = Math.max(iy, y);
        float cx2 = Math.min(ix + rw, x + getWidth()), cy2 = Math.min(iy + rh, y + areaHeight());
        if (cx2 > cx && cy2 > cy) {
            drawCheckerboard(sd, cx, cy, cx2 - cx, cy2 - cy, alpha);
        }

        // the graphic, nearest filtered so pixels stay crisp while zoomed in
        Texture tex = texture.getTexture();
        Texture.TextureFilter minFilter = tex.getMinFilter();
        Texture.TextureFilter magFilter = tex.getMagFilter();
        batch.flush();
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, ix, iy, rw, rh);
        batch.flush();
        tex.setFilter(minFilter, magFilter);
        batch.setColor(Color.WHITE);

        // rulers hide anything drawn under them, so clip the image area for the overlays
        batch.flush();
        clipEnd();
        if (clipBegin(x + RULER, y, areaWidth(), areaHeight())) {
            drawGrid(alpha, ix, iy, rw, rh);
            drawBands(alpha, ix, iy, rw, rh);
            sd.setColor(faded(IMAGE_BORDER, alpha));
            sd.rectangle(ix, iy, rw, rh, 1f);
            drawGuides(alpha, x, y);
            batch.flush();
            clipEnd();
        }

        if (clipBegin(x, y, getWidth(), getHeight())) {
            drawRulers(batch, alpha, x, y, ix, iy, rw, rh);
            drawHoverLabel(batch, alpha, x, y);
            batch.flush();
            clipEnd();
        }
    }

    private void drawGrid(float alpha, float ix, float iy, float rw, float rh) {
        if (!showGrid || zoom < GRID_MIN_ZOOM) return;
        sd.setColor(faded(GRID, alpha));
        float x0 = getX() + RULER, x1 = getX() + getWidth();
        float y0 = getY(), y1 = getY() + areaHeight();
        for (int i = 1; i < texture.getRegionWidth(); i++) {
            float gx = ix + i * zoom;
            if (gx < x0 || gx > x1) continue;
            sd.line(gx, Math.max(iy, y0), gx, Math.min(iy + rh, y1), 1f);
        }
        for (int i = 1; i < texture.getRegionHeight(); i++) {
            float gy = iy + i * zoom;
            if (gy < y0 || gy > y1) continue;
            sd.line(Math.max(ix, x0), gy, Math.min(ix + rw, x1), gy, 1f);
        }
    }

    private void drawBands(float alpha, float ix, float iy, float rw, float rh) {
        for (int axis = 0; axis < 2; axis++) {
            IntArray areas = areasOf(axis);
            Color color = axisColor(axis);
            for (int i = 0; i < areas.size / 2; i++) {
                boolean selected = selectedAxis == axis && selectedPair == i;
                float origin = axis == HORIZONTAL ? getX() : getY();
                float start = origin + guidePosition(axis, i, false);
                float end = origin + guidePosition(axis, i, true);
                tmp.set(color);
                tmp.a = (selected ? BAND_SELECTED_ALPHA : BAND_ALPHA) * alpha;
                sd.setColor(tmp);
                if (axis == HORIZONTAL) sd.filledRectangle(start, iy, end - start, rh);
                else sd.filledRectangle(ix, start, rw, end - start);
            }
        }
        // area being created
        if (createAxis >= 0) {
            float origin = createAxis == HORIZONTAL ? getX() + imageLeft() : getY() + imageBottom();
            float start = origin + createStart * zoom;
            float end = origin + (createEnd + 1) * zoom;
            tmp.set(HOVER_COLOR);
            tmp.a = 0.25f * alpha;
            sd.setColor(tmp);
            if (createAxis == HORIZONTAL) sd.filledRectangle(start, iy, end - start, rh);
            else sd.filledRectangle(ix, start, rw, end - start);
        }
    }

    private void drawGuides(float alpha, float x, float y) {
        float y0 = y, y1 = y + areaHeight();
        float x0 = x + RULER, x1 = x + getWidth();
        for (int axis = 0; axis < 2; axis++) {
            IntArray areas = areasOf(axis);
            for (int i = 0; i < areas.size / 2; i++) {
                for (int side = 0; side < 2; side++) {
                    boolean end = side == 1;
                    boolean over = overAxis == axis && overPair == i && overEnd == end;
                    boolean selected = selectedAxis == axis && selectedPair == i;
                    tmp.set(over ? HOVER_COLOR : axisColor(axis));
                    tmp.a = (over ? 1f : selected ? 0.95f : GUIDE_ALPHA) * alpha;
                    sd.setColor(tmp);
                    float width = over || selected ? 2f : 1f;
                    float position = guidePosition(axis, i, end);
                    if (axis == HORIZONTAL) sd.line(x + position, y0, x + position, y1, width);
                    else sd.line(x0, y + position, x1, y + position, width);
                }
            }
        }
    }

    private void drawRulers(Batch batch, float alpha, float x, float y, float ix, float iy, float rw, float rh) {
        float top = y + areaHeight();
        // strips, including the corner where they meet so the graphic never shows through it
        sd.setColor(faded(RULER_BG, alpha));
        sd.filledRectangle(x, top, getWidth(), RULER);
        sd.filledRectangle(x, y, RULER, areaHeight());
        sd.setColor(faded(RULER_TICK, alpha));
        sd.line(x + RULER, top, x + getWidth(), top, 1f);
        sd.line(x + RULER, y, x + RULER, top, 1f);

        // ticks and numbers
        int step = tickStep();
        font.setColor(faded(RULER_TEXT, alpha));
        for (int i = 0; i <= texture.getRegionWidth(); i += step) {
            float tx = ix + i * zoom;
            if (tx < x + RULER || tx > x + getWidth()) continue;
            sd.setColor(faded(RULER_TICK, alpha));
            sd.line(tx, top, tx, top + 5f, 1f);
            glyphs.setText(font, horizontalLabel(i));
            font.draw(batch, glyphs, tx + 2f, top + RULER - 3f);
        }
        for (int i = 0; i <= texture.getRegionHeight(); i += step) {
            float ty = iy + i * zoom;
            if (ty < y || ty > top) continue;
            sd.setColor(faded(RULER_TICK, alpha));
            sd.line(x + RULER - 5f, ty, x + RULER, ty, 1f);
            // digits stacked one per line, centred on the tick, like the editor's rulers
            glyphs.setText(font, verticalLabel(i));
            font.draw(batch, glyphs, x + 4f, ty + glyphs.height / 2f);
        }
        font.setColor(Color.WHITE);

        // area markers
        for (int axis = 0; axis < 2; axis++) {
            IntArray areas = areasOf(axis);
            for (int i = 0; i < areas.size / 2; i++) {
                drawRulerMarker(alpha, x, y, top, axis, i);
            }
        }
        if (createAxis >= 0) {
            float origin = createAxis == HORIZONTAL ? imageLeft() : imageBottom();
            float start = origin + createStart * zoom;
            float end = origin + (createEnd + 1) * zoom;
            sd.setColor(faded(HOVER_COLOR, alpha));
            if (createAxis == HORIZONTAL) sd.filledRectangle(x + start, top + MARKER_INSET, end - start, RULER - MARKER_INSET * 2);
            else sd.filledRectangle(x + MARKER_INSET, y + start, RULER - MARKER_INSET * 2, end - start);
        }
    }

    /**
     * Marker of an area on its ruler: a bar as tall as the ruler with a bright cap at each end standing
     * for the handles. Adjacent areas simply touch, nothing overlaps.
     */
    private void drawRulerMarker(float alpha, float x, float y, float top, int axis, int pair) {
        boolean selected = selectedAxis == axis && selectedPair == pair;
        float start = guidePosition(axis, pair, false);
        float end = guidePosition(axis, pair, true);
        float inset = MARKER_INSET;
        float thickness = RULER - inset * 2;

        tmp.set(axisColor(axis));
        tmp.a = (selected ? 0.95f : 0.6f) * alpha;
        sd.setColor(tmp);
        if (axis == HORIZONTAL) sd.filledRectangle(x + start, top + inset, end - start, thickness);
        else sd.filledRectangle(x + inset, y + start, thickness, end - start);

        for (int side = 0; side < 2; side++) {
            boolean over = overAxis == axis && overPair == pair && overEnd == (side == 1);
            float position = side == 0 ? start : end;
            float capWidth = over ? CAP_WIDTH + 2f : CAP_WIDTH;
            if (over) tmp.set(HOVER_COLOR);
            else tmp.set(axisColor(axis)).lerp(Color.WHITE, 0.5f);
            tmp.a = alpha;
            sd.setColor(tmp);
            // caps are inset so the first cap of an area and the last of its neighbour stay distinct
            float capStart = side == 0 ? position : position - capWidth;
            if (axis == HORIZONTAL) sd.filledRectangle(x + capStart, top + inset - 1f, capWidth, thickness + 2f);
            else sd.filledRectangle(x + inset - 1f, y + capStart, thickness + 2f, capWidth);
        }
    }

    /** Ruler texts are built once per value and reused every frame, so drawing allocates nothing. */
    private final IntMap<String> horizontalLabels = new IntMap<>();
    private final IntMap<String> verticalLabels = new IntMap<>();
    private final StringBuilder labelBuilder = new StringBuilder();

    private String horizontalLabel(int value) {
        String text = horizontalLabels.get(value);
        if (text == null) {
            text = Integer.toString(value);
            horizontalLabels.put(value, text);
        }
        return text;
    }

    /** The digits of a value one per line, for the vertical ruler. */
    private String verticalLabel(int value) {
        String text = verticalLabels.get(value);
        if (text == null) {
            String digits = Integer.toString(value);
            labelBuilder.setLength(0);
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0) labelBuilder.append('\n');
                labelBuilder.append(digits.charAt(i));
            }
            text = labelBuilder.toString();
            verticalLabels.put(value, text);
        }
        return text;
    }

    /** Ruler step in graphic pixels so that labels are at least 40 screen pixels apart. */
    private int tickStep() {
        int[] steps = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000};
        for (int step : steps) {
            if (step * zoom >= 40f) return step;
        }
        return steps[steps.length - 1];
    }

    private void drawHoverLabel(Batch batch, float alpha, float x, float y) {
        String text = null;
        if (createAxis >= 0) {
            text = createStart + " - " + createEnd + "  (" + (createEnd - createStart + 1) + " px)";
        } else if (overAxis >= 0 && overPair * 2 + 1 < areasOf(overAxis).size) {
            text = areaName(overAxis, overPair) + "   " + areaDescription(overAxis, overPair);
        }
        if (text == null || !mouseInside) return;

        glyphs.setText(font, text);
        float pad = 4f;
        float w = glyphs.width + pad * 2, h = glyphs.height + pad * 2;
        float lx = MathUtils.clamp(mouseX + 14f, RULER, getWidth() - w);
        float ly = MathUtils.clamp(mouseY + 14f, 0, areaHeight() - h);
        sd.setColor(faded(LABEL_BG, alpha));
        sd.filledRectangle(x + lx, y + ly, w, h);
        sd.setColor(faded(createAxis >= 0 ? HOVER_COLOR : axisColor(overAxis), alpha));
        sd.rectangle(x + lx, y + ly, w, h, 1f);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, glyphs, x + lx + pad, y + ly + h - pad);
        font.setColor(Color.WHITE);
    }
}
