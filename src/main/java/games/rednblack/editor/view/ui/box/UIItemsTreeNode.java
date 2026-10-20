package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.H2DTextTooltip;

public class UIItemsTreeNode extends Tree.Node<UIItemsTreeNode, UIItemsTreeValue, VisTable> implements Pool.Poolable {
    /** How much of a hidden item still shows: it stays readable, but clearly out of the picture. */
    static final float HIDDEN_ALPHA = 0.4f;

    VisLabel label;
    Cell<VisLabel> lblCell;
    H2DTextTooltip tooltip;
    /** The tint of the icon, which is drawn white: it tells the kind of item. */
    final Color iconColor = new Color(Color.WHITE);
    private final Color labelColor = new Color(Color.WHITE);
    private boolean hidden;

    public UIItemsTreeNode() {
        super(new VisTable());

        label = new VisLabel("", "default");
        setColor(Color.WHITE);
        lblCell = getActor().add(label);

        setValue(new UIItemsTreeValue());

        tooltip = StandardWidgetsFactory.addTooltip(getActor(), "");
    }

    public void setNodeValue(String entityId, int zIndex) {
        getValue().setEntityId(entityId);
        getValue().setzIndex(zIndex);
    }

    public void setColor(Color color) {
        labelColor.set(color);
        applyLabelColor();
    }

    public void setColor(float r, float g, float b, float a) {
        labelColor.set(r, g, b, a);
        applyLabelColor();
    }

    /** A hidden item, or one inside something hidden, is dimmed, name and icon both. */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        applyLabelColor();
    }

    public boolean isHidden() {
        return hidden;
    }

    private void applyLabelColor() {
        label.setColor(labelColor.r, labelColor.g, labelColor.b, labelColor.a * (hidden ? HIDDEN_ALPHA : 1f));
    }

    /** Closed by a click, the tree first folds the children away and then calls {@link #collapseNow()}. */
    @Override
    public void setExpanded(boolean expanded) {
        if (!expanded && isExpanded() && getTree() instanceof UIItemsTree tree && tree.foldInstead(this)) return;
        super.setExpanded(expanded);
    }

    /** Closes for real, once the children have folded away. */
    void collapseNow() {
        super.setExpanded(false);
    }

    public void setName(String name, String layer) {
        label.setText(name);
        tooltip.setText(layer);
    }

    public void setPad(float top, float left, float bottom, float right) {
        lblCell.pad(top, left, bottom, right);
    }

    @Override
    public void reset() {
        getValue().reset();

        setExpanded(false);
        setIcon(null);
        iconColor.set(Color.WHITE);
        setHidden(false);
        clearChildren();
        remove();
    }
}
