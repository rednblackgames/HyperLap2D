package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.widget.VisTable;

public class UIItemsTreeNode extends Tree.Node<UIItemsTreeNode, UIItemsTreeValue, VisTable> {

    public UIItemsTreeNode(VisTable actor) {
        super(actor);
    }
}
