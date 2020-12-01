package games.rednblack.editor.view.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.view.ui.widget.CustomMenu;

public abstract class H2DMenu extends CustomMenu {

    protected final Array<MenuItem> itemsList = new Array<>();

    public H2DMenu(String title) {
        super(title);
        Cell<Label> labelCell = openButton.getLabelCell();
        labelCell.width(openButton.getWidth() + 14);
    }

    @Override
    public void addItem(MenuItem item) {
        item.getShortcutCell().padLeft(40);
        super.addItem(item);
        itemsList.add(item);
    }

    public abstract void setProjectOpen(boolean open);
}
