package games.rednblack.editor.view.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import games.rednblack.editor.view.ui.widget.CustomMenu;

public abstract class H2DMenu extends CustomMenu {

    public H2DMenu(String title) {
        super(title);
        Cell<Label> labelCell = openButton.getLabelCell();
        labelCell.width(openButton.getWidth() + 14);
    }

    public abstract void setProjectOpen(boolean open);
}
