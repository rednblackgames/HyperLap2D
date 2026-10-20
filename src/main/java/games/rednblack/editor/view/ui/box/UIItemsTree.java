package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.widget.VisTree;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Items tree with roomier rows and a hand-drawn look in the manner of Spine's tree: the {@code tree-plus}/
 * {@code tree-minus} atlas regions are no longer painted. Thin parent→child connector lines are drawn with
 * {@link ShapeDrawer} instead, ending in a small triangle inside a ring that sits on the line as the expander, and
 * the lines leading from the root down to the selection are drawn in the theme blue. Lines and expanders go over
 * the rows' hover and selection, so a highlighted row never cuts the tree. Icons are drawn white and tinted by kind
 * ({@link UIItemsTreeNode#iconColor}); hidden items are dimmed. Opening or closing a node by hand unfolds or
 * folds its children instead of making them appear or vanish at once. It also supports drag &amp; drop to reorder z-index: because z-index is
 * confined per layer, while dragging every node outside the dragged node's layer is dimmed and only
 * same-layer siblings are valid drop targets.
 * <p>
 * It declares itself {@link Cullable} so the scroll pane around it hands it the part that is on screen.
 * {@code Tree} already skips the rows outside that area and so does the group drawing their labels; only
 * the connector lines are drawn here, and they are skipped the same way. Without it a scene of a thousand
 * items draws every row of it, every frame, however little of the panel is showing.
 */
public class UIItemsTree extends VisTree<UIItemsTreeNode, UIItemsTreeValue> implements Cullable {

    /** Notified when a drag ends on a valid drop; carries same-layer entity ids in z-ascending order. */
    public interface DropListener {
        void onDrop(Array<String> orderedEntityIdsZAscending);
    }

    private static final float PADDING_LEFT = 4f;
    private static final float PADDING_RIGHT = 6f;
    private static final float Y_SPACING = 9f;      // roomier rows
    private static final float INDENT_SPACING = 16f;
    private static final float ICON_SPACING_LEFT = 6f;     // room between the expander's ring and the icon
    private static final float ICON_SPACING_RIGHT = 6f;

    /** The expander: a ring the lines stop at, and inside it a triangle, from its centre to the tip and to the base. */
    private static final float RING_RADIUS = 5.5f;
    private static final float TRIANGLE_TIP = 2.6f;
    private static final float TRIANGLE_BASE = 1.9f;
    private static final Color EXPANDER_COLOR = new Color(0.62f, 0.63f, 0.64f, 1f);

    /** How long the children take to unfold or fold. */
    private static final float EXPAND_DURATION = 0.18f;

    /** Thin guide lines from a parent down to its children. */
    private static final Color LINE_COLOR = new Color(1f, 1f, 1f, 0.22f);
    private static final float LINE_THICKNESS = 1f;
    /** The lines and triangles on the way from the root to the selected items. */
    private static final Color PATH_COLOR = new Color(0.105f, 0.631f, 0.886f, 1f); // theme blue #1BA1E2

    private static final float DIMMED_ALPHA = 0.3f;
    private static final float PLACEHOLDER_ALPHA = 0.35f;   // in-place row of the picked-up node
    private static final Color DROP_COLOR = new Color(0.105f, 0.631f, 0.886f, 1f); // theme blue #1BA1E2
    private static final float DROP_THICKNESS = 2f;

    /** Floating ghost that follows the cursor while dragging. */
    private static final Color GHOST_BG = new Color(0.16f, 0.16f, 0.16f, 0.9f);
    private static final float GHOST_ALPHA = 0.95f;

    private ShapeDrawer shapeDrawer;
    /** The selected nodes and everything above them, gathered once a frame. */
    private final ObjectSet<UIItemsTreeNode> onPath = new ObjectSet<>();
    /** The node unfolding or folding its children, and how far along it is; null when none is. */
    private UIItemsTreeNode expandingNode;
    private boolean folding;
    private float expandTime;
    /** The node under a press, from touch down until the click is handled: only a click is animated. */
    private UIItemsTreeNode pressedNode;
    private final Array<UIItemsTreeNode> unfolding = new Array<>();
    /** The alpha coming down from above for this frame: the drawn parts have to follow the panel fading. */
    private float drawAlpha = 1f;
    private final Color shapeColor = new Color();

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
        addExpandAnimation();
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

        drawAlpha = parentAlpha * getColor().a;
        collectPath();

        // rows first, hover and selection included; the tree goes over them so they never cut it
        super.draw(batch, parentAlpha);

        float prevColor = shapeDrawer.getPackedColor();
        float prevThickness = shapeDrawer.getDefaultLineWidth();
        shapeDrawer.setDefaultLineWidth(LINE_THICKNESS);
        drawConnectorLines(getRootNodes(), PADDING_LEFT, plusMinusWidth());
        drawExpanders(getRootNodes(), PADDING_LEFT, plusMinusWidth());
        shapeDrawer.setDefaultLineWidth(prevThickness);
        shapeDrawer.setColor(prevColor);

        if (draggingNode != null && dropValid) {
            float ay = getY() + dropIndicatorY;
            prevColor = setShapeColor(DROP_COLOR);
            prevThickness = shapeDrawer.getDefaultLineWidth();
            shapeDrawer.setDefaultLineWidth(DROP_THICKNESS);
            shapeDrawer.line(getX() + 2f, ay, getX() + getWidth() - 2f, ay);
            shapeDrawer.setDefaultLineWidth(prevThickness);
            shapeDrawer.setColor(prevColor);
        }
    }

    /**
     * Takes the colour the way it has to be drawn right now. A drawable or a label is handed the
     * alpha of everything above it; a shape is drawn straight onto the batch, so it is applied here
     * or the tree stays behind when the panel fades away.
     */
    private float setShapeColor(Color color) {
        shapeColor.set(color);
        shapeColor.a *= drawAlpha;
        return shapeDrawer.setColor(shapeColor);
    }

    private void collectPath() {
        onPath.clear();
        for (UIItemsTreeNode node : getSelection()) {
            for (UIItemsTreeNode n = node; n != null && onPath.add(n); n = n.getParent()) {
            }
        }
    }

    /** The middle of the pixel the coordinate falls in, so a one pixel line stays sharp. */
    private static float snap(float v) {
        return (float) Math.floor(v) + 0.5f;
    }

    /**
     * Draws the elbow guide lines from every expanded parent to each of its children, then the part of them
     * leading to the selection over them in the path colour.
     */
    private void drawConnectorLines(Array<UIItemsTreeNode> nodes, float indent, float pmw) {
        final float treeX = getX(), treeY = getY();

        //the area the scroll pane is showing, in the same coordinates the lines are drawn in
        Rectangle culling = getCullingArea();
        final float cullBottom = culling == null ? Float.NEGATIVE_INFINITY : treeY + culling.y;
        final float cullTop = culling == null ? Float.POSITIVE_INFINITY : cullBottom + culling.height;

        for (UIItemsTreeNode node : nodes) {
            Array<UIItemsTreeNode> children = node.getChildren();
            if (!node.isExpanded() || children.size == 0) continue;

            float verticalX = snap(treeX + indent + pmw / 2f);
            float parentCenterY = treeY + node.getActor().getY() + node.getHeight() / 2f;
            float childIndentX = treeX + indent + INDENT_SPACING;        // left edge of the children's expand column
            float childExpanderCenterX = childIndentX + pmw / 2f;
            float childIconLeftX = childIndentX + pmw + ICON_SPACING_LEFT;

            UIItemsTreeNode last = children.get(children.size - 1);
            float lastChildCenterY = snap(treeY + last.getActor().getY() + last.getHeight() / 2f);

            //nothing of this family is on screen: its children may still be, so keep walking
            if (lastChildCenterY > cullTop || parentCenterY < cullBottom) {
                drawConnectorLines(children, indent + INDENT_SPACING, pmw);
                continue;
            }

            // the vertical leaves from the bottom of the parent's ring
            parentCenterY = snap(parentCenterY) - RING_RADIUS;
            setShapeColor(LINE_COLOR);
            shapeDrawer.line(verticalX, parentCenterY, verticalX, lastChildCenterY);
            UIItemsTreeNode lastOnPath = null;
            for (UIItemsTreeNode child : children) {
                drawStub(child, verticalX, childExpanderCenterX, childIconLeftX);
                if (onPath.contains(child)) lastOnPath = child;
            }

            if (lastOnPath != null) {
                setShapeColor(PATH_COLOR);
                float pathEndY = snap(treeY + lastOnPath.getActor().getY() + lastOnPath.getHeight() / 2f);
                shapeDrawer.line(verticalX, parentCenterY, verticalX, pathEndY);
                for (UIItemsTreeNode child : children) {
                    if (onPath.contains(child)) drawStub(child, verticalX, childExpanderCenterX, childIconLeftX);
                }
            }

            drawConnectorLines(children, indent + INDENT_SPACING, pmw);
        }
    }

    /** The short line from the parent's vertical to a child: up to its ring, or up to its icon. */
    private void drawStub(UIItemsTreeNode child, float verticalX, float expanderCenterX, float iconLeftX) {
        float y = snap(getY() + child.getActor().getY() + child.getHeight() / 2f);
        float endX = child.getChildren().size > 0 ? snap(expanderCenterX) - RING_RADIUS : iconLeftX - 2f;
        shapeDrawer.line(verticalX, y, endX, y);
    }

    /** The expanders of every node with children that is showing, over the lines that reach them. */
    private void drawExpanders(Array<UIItemsTreeNode> nodes, float indent, float pmw) {
        Rectangle culling = getCullingArea();
        for (UIItemsTreeNode node : nodes) {
            if (node.getChildren().size == 0) continue;
            float rowY = node.getActor().getY();
            if (culling == null || (rowY + node.getHeight() >= culling.y && rowY <= culling.y + culling.height))
                drawExpander(node, snap(getX() + indent + pmw / 2f), snap(getY() + rowY + node.getHeight() / 2f));
            if (node.isExpanded()) drawExpanders(node.getChildren(), indent + INDENT_SPACING, pmw);
        }
    }

    private void drawExpander(UIItemsTreeNode node, float cx, float cy) {
        setShapeColor(onPath.contains(node) ? PATH_COLOR : EXPANDER_COLOR);
        shapeDrawer.circle(cx, cy, RING_RADIUS - LINE_THICKNESS / 2f, LINE_THICKNESS);

        // each triangle shifted so its centroid, not its bounding box, sits in the middle of the ring
        float tip = TRIANGLE_TIP, base = TRIANGLE_BASE, shift = (2f * base - tip) / 3f;
        if (node.isExpanded()) {
            shapeDrawer.filledTriangle(cx - base - 0.4f, cy + base - shift, cx + base + 0.4f, cy + base - shift, cx, cy - tip - shift);
        } else {
            shapeDrawer.filledTriangle(cx - base + shift, cy + base + 0.4f, cx - base + shift, cy - base - 0.4f, cx + tip + shift, cy);
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
        // deliberately empty: the atlas plus/minus is replaced by the expanders drawn after the lines
    }

    @Override
    protected void drawIcon(UIItemsTreeNode node, Drawable icon, Batch batch, float x, float y) {
        // the batch carries the row's colour (dimmed while dragging): the kind's tint goes on top of it
        Color row = batch.getColor();
        float r = row.r, g = row.g, b = row.b, a = row.a;
        Color tint = node.iconColor;
        batch.setColor(r * tint.r, g * tint.g, b * tint.b, a * tint.a * (node.isHidden() ? UIItemsTreeNode.HIDDEN_ALPHA : 1f));
        super.drawIcon(node, icon, batch, x, y);
        batch.setColor(r, g, b, a);
    }

    // ---------------------------------------------------------------------------------------------
    // Expand animation
    // ---------------------------------------------------------------------------------------------

    /**
     * Watches the clicks the tree itself expands and collapses on: only a node opened or closed by hand is
     * animated, not the ones the panel opens while it builds itself again.
     */
    private void addExpandAnimation() {
        addListener(new ClickListener() {
            private boolean wasExpanded;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                pressedNode = getNodeAt(y);
                wasExpanded = pressedNode != null && pressedNode.isExpanded();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                // the tree's own listener came first and has opened the node already
                UIItemsTreeNode node = pressedNode;
                if (node != null && !wasExpanded && node.isExpanded() && node.getChildren().size > 0)
                    startAnimation(node, false, 0f);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                pressedNode = null;
            }
        });
    }

    /**
     * Asked by a node the tree is about to close: when it is the one clicked, its children fold away first and
     * it closes at the end ({@link UIItemsTreeNode#collapseNow()}). Clicked again while still unfolding or
     * folding, it turns back from where it is.
     */
    boolean foldInstead(UIItemsTreeNode node) {
        if (node != pressedNode || node.getChildren().size == 0) return false;
        if (node == expandingNode && folding) {
            // closing, clicked to open again: unfold from how far it is still open
            startAnimation(node, false, openTimeFor(unfolded()));
            return true;
        }
        float open = node == expandingNode ? unfolded() : 1f;
        startAnimation(node, true, openTimeFor(1f - open));
        return true;
    }

    /** The time at which the eased curve reaches the given amount, to turn back without a jump. */
    private static float openTimeFor(float amount) {
        // pow2Out(t) = 1 - (1 - t)^2
        return (1f - (float) Math.sqrt(1f - Math.min(1f, Math.max(0f, amount)))) * EXPAND_DURATION;
    }

    private void startAnimation(UIItemsTreeNode node, boolean fold, float time) {
        if (node != expandingNode) finishExpanding();
        expandingNode = node;
        folding = fold;
        expandTime = time;
        invalidateHierarchy();
    }

    private void finishExpanding() {
        if (expandingNode == null) return;
        if (draggingNode == null) {
            for (UIItemsTreeNode n : unfolding) {
                Color c = n.getActor().getColor();
                n.getActor().setColor(c.r, c.g, c.b, 1f);
            }
        }
        unfolding.clear();
        UIItemsTreeNode node = expandingNode;
        expandingNode = null;
        if (folding) node.collapseNow();
        folding = false;
        invalidateHierarchy();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (expandingNode == null) return;
        // the panel built itself again, or the node was closed meanwhile
        if (expandingNode.getTree() != this || !expandingNode.isExpanded()) {
            finishExpanding();
            return;
        }
        expandTime += delta;
        if (expandTime >= EXPAND_DURATION) finishExpanding();
        else invalidateHierarchy();
    }

    /** How open the animated node's children are, from 0 (folded under it) to 1. */
    private float unfolded() {
        float eased = Interpolation.pow2Out.apply(Math.min(1f, expandTime / EXPAND_DURATION));
        return folding ? 1f - eased : eased;
    }

    /** The height the unfolding children will take once open, which is not given yet. */
    private float stillFolded() {
        if (expandingNode == null) return 0f;
        return subtreeHeight(expandingNode.getChildren()) * (1f - unfolded());
    }

    private static float subtreeHeight(Array<UIItemsTreeNode> nodes) {
        float height = 0f;
        for (UIItemsTreeNode node : nodes) {
            height += node.getHeight() + Y_SPACING;
            if (node.isExpanded()) height += subtreeHeight(node.getChildren());
        }
        return height;
    }

    @Override
    public float getPrefHeight() {
        float height = super.getPrefHeight();
        return height - stillFolded();
    }

    /**
     * Lays the rows out as if all were open, then folds the unfolding children up under their parent: their
     * distances from it shrink with what is still folded, and the rows below them come up by as much.
     */
    @Override
    public void layout() {
        super.layout();
        if (expandingNode == null) return;

        float folded = stillFolded(), open = unfolded();
        float parentY = expandingNode.getActor().getY();
        unfolding.clear();
        collectShowing(expandingNode.getChildren(), unfolding);
        for (UIItemsTreeNode n : unfolding) {
            float y = n.getActor().getY();
            n.getActor().setY(parentY - (parentY - y) * open);
            if (draggingNode == null) {
                Color c = n.getActor().getColor();
                n.getActor().setColor(c.r, c.g, c.b, open);
            }
        }
        shiftRowsBelow(getRootNodes(), folded, false);
    }

    private static void collectShowing(Array<UIItemsTreeNode> nodes, Array<UIItemsTreeNode> out) {
        for (UIItemsTreeNode node : nodes) {
            out.add(node);
            if (node.isExpanded()) collectShowing(node.getChildren(), out);
        }
    }

    /** Moves up every row after the unfolding children. Returns whether they were passed already. */
    private boolean shiftRowsBelow(Array<UIItemsTreeNode> nodes, float by, boolean past) {
        for (UIItemsTreeNode node : nodes) {
            if (past) node.getActor().moveBy(0f, by);
            if (node == expandingNode) {
                past = true;   // its children are the unfolding ones, placed already
                continue;
            }
            if (node.isExpanded()) past = shiftRowsBelow(node.getChildren(), by, past);
        }
        return past;
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
                Color tint = node.iconColor;
                batch.setColor(tint.r, tint.g, tint.b, GHOST_ALPHA);
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
