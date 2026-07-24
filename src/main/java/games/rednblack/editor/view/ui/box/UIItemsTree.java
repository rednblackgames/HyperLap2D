package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTree;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Items tree with roomier rows and a hand-drawn look: the {@code tree-plus}/{@code tree-minus} atlas
 * regions are no longer painted — instead a chevron (right when collapsed, down when expanded) and
 * light parent→child connector lines are drawn at runtime with {@link ShapeDrawer}.
 * <p>
 * The style plus/minus drawables are still needed as invisible spacers: scene2d {@link com.badlogic.gdx.scenes.scene2d.ui.Tree}
 * reads their {@code getMinWidth()} to size the expand column during layout. Only the drawing is replaced
 * ({@link #drawExpandIcon}); the connector lines are added in {@link #draw} behind the node actors.
 */
public class UIItemsTree extends VisTree<UIItemsTreeNode, UIItemsTreeValue> {

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

    private ShapeDrawer shapeDrawer;

    public UIItemsTree() {
        setPadding(PADDING_LEFT, PADDING_RIGHT);
        setYSpacing(Y_SPACING);
        setIndentSpacing(INDENT_SPACING);
        setIconSpacing(ICON_SPACING_LEFT, ICON_SPACING_RIGHT);
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
}
