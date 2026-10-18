package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.AutoLayoutComponent;
import games.rednblack.editor.renderer.components.AutoLayoutItemComponent;
import games.rednblack.editor.renderer.components.AutoLayoutOwnSizeComponent;
import games.rednblack.editor.renderer.components.BoundingBoxComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.AutoLayoutSolver;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

/**
 * What {@link AutoLayoutPreview} draws: a composite's auto layout and its children as rectangles,
 * read from the scene every frame so the preview follows any change, whoever makes it.
 *
 * A composite with nothing to lay out gets a few made-up children, so the settings still show.
 */
public class AutoLayoutPreviewData {

    public final AutoLayoutComponent settings = new AutoLayoutComponent();
    public float width, height;
    public final Array<AutoLayoutSolver.Item> items = new Array<>();
    /** index in {@link #items} of the child to point out, -1 for none */
    public int highlighted = -1;
    /** children left out of the layout, where they are */
    public final Array<Rectangle> ignored = new Array<>();
    public boolean sample;
    public boolean valid;

    private final Array<AutoLayoutSolver.Item> itemPool = new Array<>();
    private final Array<Rectangle> rectanglePool = new Array<>();
    private final Vector2 measured = new Vector2();

    /** Reads the composite, pointing out one of its children, -1 for none. */
    public void fill(int container, int highlightedChild) {
        clear();
        AutoLayoutComponent layout = SandboxComponentRetriever.get(container, AutoLayoutComponent.class);
        DimensionsComponent dimensions = SandboxComponentRetriever.get(container, DimensionsComponent.class);
        NodeComponent node = SandboxComponentRetriever.get(container, NodeComponent.class);
        if (layout == null || dimensions == null || node == null) return;

        copy(layout, settings);
        width = dimensions.width;
        height = dimensions.height;

        for (int child : node.children) {
            TransformComponent transform = SandboxComponentRetriever.get(child, TransformComponent.class);
            DimensionsComponent childDimensions = SandboxComponentRetriever.get(child, DimensionsComponent.class);
            if (transform == null || childDimensions == null) continue;
            MainItemComponent mainItem = SandboxComponentRetriever.get(child, MainItemComponent.class);
            if (settings.skipInvisible && mainItem != null && !mainItem.visible) continue;

            BoundingBoxComponent box = SandboxComponentRetriever.get(child, BoundingBoxComponent.class);
            float left = box != null ? box.parentLocalAABB.x : 0;
            float bottom = box != null ? box.parentLocalAABB.y : 0;
            float footprintWidth = box != null ? box.parentLocalAABB.width : childDimensions.width;
            float footprintHeight = box != null ? box.parentLocalAABB.height : childDimensions.height;

            AutoLayoutItemComponent settingsOfChild = SandboxComponentRetriever.get(child, AutoLayoutItemComponent.class);
            if (settingsOfChild != null && settingsOfChild.ignore) {
                ignored.add(obtainRectangle().set(transform.x + left, transform.y + bottom, footprintWidth, footprintHeight));
                continue;
            }

            AutoLayoutSolver.Item item = obtainItem().set(footprintWidth, footprintHeight);
            boolean upright = transform.rotation == 0;
            if (settingsOfChild != null) {
                item.grow = upright ? settingsOfChild.grow : 0;
                item.alignSelf = settingsOfChild.alignSelf;
            }
            // what a child the layout sizes needs, the way the layout itself works it out
            if (upright) {
                AutoLayoutComponent childLayout = SandboxComponentRetriever.get(child, AutoLayoutComponent.class);
                AutoLayoutOwnSizeComponent own = SandboxComponentRetriever.get(child, AutoLayoutOwnSizeComponent.class);
                float scaleX = Math.abs(transform.scaleX), scaleY = Math.abs(transform.scaleY);
                item.contentWidth = (childLayout != null && childLayout.fitWidth ? childLayout.measuredWidth
                        : AutoLayoutOwnSizeComponent.ownWidth(own, childDimensions)) * scaleX;
                item.contentHeight = (childLayout != null && childLayout.fitHeight ? childLayout.measuredHeight
                        : AutoLayoutOwnSizeComponent.ownHeight(own, childDimensions)) * scaleY;
            } else {
                item.contentWidth = footprintWidth;
                item.contentHeight = footprintHeight;
            }
            if (child == highlightedChild) highlighted = items.size;
            items.add(item);
        }

        if (items.size == 0) addSamples();
        valid = width > 0 || height > 0 || items.size > 0;
    }

    /** Made-up children, sized after the composite, for a layout that has nothing to place yet. */
    private void addSamples() {
        sample = true;
        float w = width > 0 ? width : 100;
        float h = height > 0 ? height : 60;
        float[][] sizes = settings.mode == AutoLayoutComponent.Mode.GRID
                ? new float[][]{{0.2f, 0.25f}, {0.16f, 0.32f}, {0.22f, 0.2f}, {0.18f, 0.3f}, {0.2f, 0.24f}}
                : settings.mode == AutoLayoutComponent.Mode.ROW
                ? new float[][]{{0.2f, 0.45f}, {0.14f, 0.7f}, {0.26f, 0.3f}}
                : new float[][]{{0.45f, 0.2f}, {0.7f, 0.14f}, {0.3f, 0.26f}};
        for (float[] size : sizes) {
            items.add(obtainItem().set(w * size[0], h * size[1]));
        }

        // a composite fitting its content would be as large as the made-up children
        if (settings.fitWidth || settings.fitHeight) {
            AutoLayoutSolver.measure(settings, items, measured);
            if (settings.fitWidth) width = measured.x;
            if (settings.fitHeight) height = measured.y;
        }
    }

    private void clear() {
        itemPool.addAll(items);
        items.clear();
        rectanglePool.addAll(ignored);
        ignored.clear();
        highlighted = -1;
        sample = false;
        valid = false;
        width = height = 0;
    }

    private AutoLayoutSolver.Item obtainItem() {
        return itemPool.size > 0 ? itemPool.pop() : new AutoLayoutSolver.Item();
    }

    private Rectangle obtainRectangle() {
        return rectanglePool.size > 0 ? rectanglePool.pop() : new Rectangle();
    }

    private static void copy(AutoLayoutComponent from, AutoLayoutComponent to) {
        to.mode = from.mode;
        to.padLeft = from.padLeft;
        to.padRight = from.padRight;
        to.padTop = from.padTop;
        to.padBottom = from.padBottom;
        to.gap = from.gap;
        to.rowGap = from.rowGap;
        to.justify = from.justify;
        to.align = from.align;
        to.reverse = from.reverse;
        to.columns = from.columns;
        to.cellWidth = from.cellWidth;
        to.cellHeight = from.cellHeight;
        to.cellAlignX = from.cellAlignX;
        to.cellAlignY = from.cellAlignY;
        to.columnMajor = from.columnMajor;
        to.fitWidth = from.fitWidth;
        to.fitHeight = from.fitHeight;
        to.skipInvisible = from.skipInvisible;
    }
}
