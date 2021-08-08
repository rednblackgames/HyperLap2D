package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

public class UIItemsTreeNode extends Tree.Node<UIItemsTreeNode, UIItemsTreeValue, VisTable> implements Pool.Poolable {
    VisLabel label;
    Cell<VisLabel> lblCell;

    public UIItemsTreeNode() {
        super(new VisTable());

        label = new VisLabel("", "default");
        setColor(Color.WHITE);
        lblCell = getActor().add(label);

        setValue(new UIItemsTreeValue());
    }

    public void setNodeValue(int entityId, int zIndex) {
        getValue().setEntityId(entityId);
        getValue().setzIndex(zIndex);
    }

    public void setColor(Color color) {
        label.setColor(color);
    }

    public void setColor(float r, float g, float b, float a) {
        label.setColor(r, g, b, a);
    }

    public void setName(String name) {
        label.setText(name);
    }

    public void setPad(float top, float left, float bottom, float right) {
        lblCell.pad(top, left, bottom, right);
    }

    @Override
    public void reset() {
        getValue().reset();

        setExpanded(false);
        setIcon(null);
        clearChildren();
        remove();
    }
}
