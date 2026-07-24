package games.rednblack.editor.view.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.utils.MenuIcons;
import games.rednblack.editor.view.ui.widget.CustomMenu;

public abstract class H2DMenu extends CustomMenu {

    protected final Array<MenuItem> itemsList = new Array<>();

    public H2DMenu(String title) {
        super(title);
        Cell<Label> labelCell = openButton.getLabelCell();
        labelCell.width(openButton.getWidth() + 14);
    }

    /**
     * Resolves a menu-item icon from the shared VisUI skin atlas. Returns {@code null} when the
     * region is missing so the item simply renders without an icon instead of crashing.
     */
    protected static Drawable icon(String region) {
        return MenuIcons.get(region);
    }

    @Override
    public void addItem(MenuItem item) {
        item.getImageCell().pad(5);
        item.getShortcutCell().padLeft(40);
        super.addItem(item);
        itemsList.add(item);
    }

    public abstract void setProjectOpen(boolean open);
}
