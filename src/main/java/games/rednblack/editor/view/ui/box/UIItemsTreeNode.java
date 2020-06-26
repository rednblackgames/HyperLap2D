package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.widget.VisLabel;

public class UIItemsTreeNode extends Tree.Node<UIItemsTreeNode, UIItemsTreeValue, VisLabel> {

    public UIItemsTreeNode(VisLabel actor) {
        super(actor);
    }
}
