package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTree;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Items tree with roomier rows and a hand-drawn look: the {@code tree-plus}/{@code tree-minus} atlas
 * regions are no longer painted — a chevron and light parent→child connector lines are drawn with
 * {@link ShapeDrawer} instead. It also supports drag &amp; drop to reorder z-index: because z-index is
 * confined per layer, while dragging every node outside the dragged node's layer is dimmed and only
 * same-layer siblings are valid drop targets.
 */
public class UIItemsTree extends VisTree<UIItemsTreeNode, UIItemsTreeValue> {

    /** Notified when a drag ends on a valid drop; carries same-layer entity ids in z-ascending order. */
    public interface DropListener {
        void onDrop(Array<String> orderedEntityIdsZAscending);
    }

    private static final float PADDING_LEFT = 4f;
    private static final float PADDING_RIGHT = 6f;
    private static final float Y_SPACING = 9f;      // roomier rows
    private static final float INDENT_SPACING = 16f;
    private static final float ICON_SPACING_LEFT = 2f;
    private static final float ICON_SPACING_RIGHT = 6f;

    /** Chevron arm length from its vertex. */
    private static final float CHEVRON_ARM = 3.2f;
    private static final float CHEVRON_THICKNESS = 1.6f;
    private static final Color CHEVRON_COLOR = new Color(0.78f, 0.79f, 0.80f, 1f);

    /** Light guide lines from a parent down to its children. */
    private static final Color LINE_COLOR = new Color(1f, 1f, 1f, 0.13f);
    private static final float LINE_THICKNESS = 1f;

    private static final float DIMMED_ALPHA = 0.3f;
    private static final float PLACEHOLDER_ALPHA = 0.35f;   // in-place row of the picked-up node
    private static final Color DROP_COLOR = new Color(0.105f, 0.631f, 0.886f, 1f); // theme blue #1BA1E2
    private static final float DROP_THICKNESS = 2f;

    /** Floating ghost that follows the cursor while dragging. */
    private static final Color GHOST_BG = new Color(0.16f, 0.16f, 0.16f, 0.9f);
    private static final float GHOST_ALPHA = 0.95f;

    private ShapeDrawer shapeDrawer;

    private DropListener dropListener;
    private UIItemsTreeNode draggingNode;
    private String draggingLayer = "";
    private final Array<String> pendingOrder = new Array<>();
    private final Array<UIItemsTreeNode> dragSiblings = new Array<>();   // reused during drag, no per-move alloc
    private boolean dropValid;
    private float dropIndicatorY;

    /** The floating ghost lives on the Stage so it is not clipped by the panel's scroll scissor. */
    private final Ghost ghost = new Ghost();
    private final Vector2 tmpVec = new Vector2();

    public UIItemsTree() {
        setPadding(PADDING_LEFT, PADDING_RIGHT);
        setYSpacing(Y_SPACING);
        setIndentSpacing(INDENT_SPACING);
        setIconSpacing(ICON_SPACING_LEFT, ICON_SPACING_RIGHT);
        addDragSupport();
    }

    public void setDropListener(DropListener dropListener) {
        this.dropListener = dropListener;
    }

    private float plusMinusWidth() {
        Drawable plus = getStyle().plus, minus = getStyle().minus;
        float pw = plus == null ? 0 : plus.getMinWidth();
        float mw = minus == null ? 0 : minus.getMinWidth();
        return Math.max(pw, mw);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null)
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion);

        float prevColor = shapeDrawer.setColor(LINE_COLOR);
        float prevThickness = shapeDrawer.getDefaultLineWidth();
        shapeDrawer.setDefaultLineWidth(LINE_THICKNESS);
        drawConnectorLines(getRootNodes(), PADDING_LEFT, plusMinusWidth());
        shapeDrawer.setDefaultLineWidth(prevThickness);
        shapeDrawer.setColor(prevColor);

        super.draw(batch, parentAlpha);

        if (draggingNode != null && dropValid) {
            float ay = getY() + dropIndicatorY;
            prevColor = shapeDrawer.setColor(DROP_COLOR);
            prevThickness = shapeDrawer.getDefaultLineWidth();
            shapeDrawer.setDefaultLineWidth(DROP_THICKNESS);
            shapeDrawer.line(getX() + 2f, ay, getX() + getWidth() - 2f, ay);
            shapeDrawer.setDefaultLineWidth(prevThickness);
            shapeDrawer.setColor(prevColor);
        }
    }

    /** Draws the elbow guide lines from every expanded parent to each of its children. */
    private void drawConnectorLines(Array<UIItemsTreeNode> nodes, float indent, float pmw) {
        final float treeX = getX(), treeY = getY();
        for (UIItemsTreeNode node : nodes) {
            Array<UIItemsTreeNode> children = node.getChildren();
            if (!node.isExpanded() || children.size == 0) continue;

            float verticalX = treeX + indent + pmw / 2f;
            float parentCenterY = treeY + node.getActor().getY() + node.getHeight() / 2f;
            float childIndentX = treeX + indent + INDENT_SPACING;        // left edge of the children's expand column
            float childChevronCenterX = childIndentX + pmw / 2f;
            float childIconLeftX = childIndentX + pmw + ICON_SPACING_LEFT;

            UIItemsTreeNode last = children.get(children.size - 1);
            float lastChildCenterY = treeY + last.getActor().getY() + last.getHeight() / 2f;
            // start the vertical just below the parent chevron so the line never cuts through it
            shapeDrawer.line(verticalX, parentCenterY - CHEVRON_ARM, verticalX, lastChildCenterY);

            for (UIItemsTreeNode child : children) {
                float childCenterY = treeY + child.getActor().getY() + child.getHeight() / 2f;
                // stop just before an expandable child's chevron; reach the icon for leaf nodes
                float endX = child.getChildren().size > 0
                        ? childChevronCenterX - CHEVRON_ARM
                        : childIconLeftX - 1f;
                shapeDrawer.line(verticalX, childCenterY, endX, childCenterY);
            }

            drawConnectorLines(children, indent + INDENT_SPACING, pmw);
        }
    }

    @Override
    protected void drawOver(UIItemsTreeNode node, Drawable over, Batch batch, float x, float y, float width, float height) {
        // hide the hover highlight while dragging — it's distracting during a drag&drop
        if (draggingNode != null) return;
        super.drawOver(node, over, batch, x, y, width, height);
    }

    @Override
    protected void drawExpandIcon(UIItemsTreeNode node, Drawable expandIcon, Batch batch, float x, float y) {
        // deliberately not calling super: the atlas plus/minus is replaced by the drawn chevron,
        // sized and centred inside the space the drawable reserved.
        float cx = x + expandIcon.getMinWidth() / 2f;
        float cy = y + expandIcon.getMinHeight() / 2f;

        float prevColor = shapeDrawer.setColor(CHEVRON_COLOR);
        float prevThickness = shapeDrawer.getDefaultLineWidth();
        shapeDrawer.setDefaultLineWidth(CHEVRON_THICKNESS);

        float a = CHEVRON_ARM;
        if (node.isExpanded()) {
            // v : vertex below centre, arms rising outwards
            shapeDrawer.line(cx - a, cy + a / 2f, cx, cy - a / 2f);
            shapeDrawer.line(cx, cy - a / 2f, cx + a, cy + a / 2f);
        } else {
            // > : vertex right of centre, arms opening to the left
            shapeDrawer.line(cx - a / 2f, cy + a, cx + a / 2f, cy);
            shapeDrawer.line(cx + a / 2f, cy, cx - a / 2f, cy - a);
        }

        shapeDrawer.setDefaultLineWidth(prevThickness);
        shapeDrawer.setColor(prevColor);
    }

    // ---------------------------------------------------------------------------------------------
    // Drag & drop (z-index reorder within the dragged node's layer)
    // ---------------------------------------------------------------------------------------------

    private void addDragSupport() {
        addListener(new DragListener() {
            {
                setTapSquareSize(8f);
            }

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                draggingNode = null;
                dropValid = false;
                UIItemsTreeNode node = getNodeAt(y);
                if (node == null || node.getParent() == null) return; // never drag the scene root
                draggingNode = node;
                draggingLayer = node.getValue().layerName;
                setNodesDimmed(true);

                ghost.node = node;
                moveGhost(x, y);
                if (getStage() != null) getStage().addActor(ghost);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (draggingNode == null) return;
                moveGhost(x, y);
                computeDrop(y);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                if (draggingNode == null) return;
                if (dropValid && dropListener != null && pendingOrder.size > 0)
                    dropListener.onDrop(new Array<>(pendingOrder));
                setNodesDimmed(false);
                ghost.remove();
                ghost.node = null;
                draggingNode = null;
                dropValid = false;
            }
        });
    }

    /** Places the ghost at the given tree-local point, converted to Stage coordinates. */
    private void moveGhost(float localX, float localY) {
        localToStageCoordinates(tmpVec.set(localX, localY));
        ghost.setPosition(tmpVec.x, tmpVec.y);
    }

    /** Floating drag chip rendered on the Stage (icon + label), never clipped by the panel scissor. */
    private static class Ghost extends Actor {
        private UIItemsTreeNode node;
        private ShapeDrawer sd;
        private final GlyphLayout layout = new GlyphLayout();
        private final Color tmp = new Color();

        Ghost() {
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (node == null) return;
            if (sd == null) sd = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion);

            float gx = getX() + 12f, cy = getY();
            Drawable icon = node.getIcon();
            float iconW = icon != null ? icon.getMinWidth() : 0f;
            float iconH = icon != null ? icon.getMinHeight() : 0f;
            float gap = iconW > 0 ? 4f : 0f;

            BitmapFont font = node.label.getStyle().font;
            CharSequence text = node.label.getText();
            layout.setText(font, text);
            float textW = layout.width, textH = layout.height;

            float chipH = Math.max(iconH, textH) + 8f;
            float chipW = iconW + gap + textW + 12f;

            float prev = sd.setColor(GHOST_BG);
            sd.filledRectangle(gx - 6f, cy - chipH / 2f, chipW, chipH);
            sd.setColor(prev);

            if (icon != null) {
                batch.setColor(1f, 1f, 1f, GHOST_ALPHA);
                icon.draw(batch, gx, cy - iconH / 2f, iconW, iconH);
            }

            Color lc = node.label.getColor();
            tmp.set(font.getColor());
            font.setColor(lc.r, lc.g, lc.b, GHOST_ALPHA);
            font.draw(batch, text, gx + iconW + gap, cy + textH / 2f);
            font.setColor(tmp);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /** Recomputes the drop position (which same-layer gap the pointer is over) from the pointer Y. */
    private void computeDrop(float pointerY) {
        dropValid = false;
        UIItemsTreeNode parent = draggingNode.getParent();
        if (parent == null) return;

        // same-layer siblings (excluding the dragged node), ordered z-ascending
        Array<UIItemsTreeNode> others = dragSiblings;
        others.clear();
        for (UIItemsTreeNode sibling : parent.getChildren()) {
            if (sibling != draggingNode && draggingLayer.equals(sibling.getValue().layerName))
                others.add(sibling);
        }
        if (others.size == 0) return;
        others.sort((a, b) -> Integer.compare(a.getValue().zIndex, b.getValue().zIndex));

        // insertion index = number of same-layer siblings sitting visually below the pointer
        int insertIndex = 0;
        for (UIItemsTreeNode o : others) {
            float centerY = o.getActor().getY() + o.getHeight() / 2f;
            if (centerY < pointerY) insertIndex++;
        }

        // no-op if the dragged node would land back at its current rank
        int currentIndex = 0;
        for (UIItemsTreeNode o : others)
            if (o.getValue().zIndex < draggingNode.getValue().zIndex) currentIndex++;
        if (insertIndex == currentIndex) return;

        pendingOrder.clear();
        for (int i = 0; i < others.size; i++) {
            if (i == insertIndex) pendingOrder.add(draggingNode.getValue().entityId);
            pendingOrder.add(others.get(i).getValue().entityId);
        }
        if (insertIndex >= others.size) pendingOrder.add(draggingNode.getValue().entityId);

        if (insertIndex == 0) {
            UIItemsTreeNode ref = others.get(0); // lowest z, bottom-most on screen
            dropIndicatorY = ref.getActor().getY() - Y_SPACING / 2f;
        } else {
            UIItemsTreeNode ref = others.get(insertIndex - 1); // neighbour just below the gap
            dropIndicatorY = ref.getActor().getY() + ref.getHeight() + Y_SPACING / 2f;
        }
        dropValid = true;
    }

    private void setNodesDimmed(boolean dim) {
        dimNodes(getRootNodes(), dim);
    }

    private void dimNodes(Array<UIItemsTreeNode> nodes, boolean dim) {
        for (UIItemsTreeNode node : nodes) {
            float alpha = 1f;
            if (dim) {
                if (node == draggingNode)
                    alpha = PLACEHOLDER_ALPHA;   // the picked-up node stays in place as a faded placeholder
                else if (node.getParent() != null && !draggingLayer.equals(node.getValue().layerName))
                    alpha = DIMMED_ALPHA;
            }
            Color c = node.getActor().getColor();
            node.getActor().setColor(c.r, c.g, c.b, alpha);
            if (node.getChildren().size > 0) dimNodes(node.getChildren(), dim);
        }
    }
}
