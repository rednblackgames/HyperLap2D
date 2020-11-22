package games.rednblack.editor.view.menu;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;

import static games.rednblack.h2d.common.MenuAPI.RESOURCE_MENU;

public class ResourceMenu extends H2DMenu {

    public static final String CREATE_PLACEHOLDER = HyperLap2DMenuBar.prefix + ".CREATE_PLACEHOLDER";
    public static final String CREATE_NOISE = HyperLap2DMenuBar.prefix + ".CREATE_NOISE";

    public ResourceMenu() {
        super("Resource");
        MenuItem placeholders = new MenuItem("Create Placeholder...", new MenuItemListener(CREATE_PLACEHOLDER, null, RESOURCE_MENU));
        MenuItem noise = new MenuItem("Create Noise", new MenuItemListener(CREATE_NOISE, null, RESOURCE_MENU));
        addItem(placeholders);
        addItem(noise);
    }

    @Override
    public void setProjectOpen(boolean open) {
        for (MenuItem menuItem : new Array.ArrayIterator<>(itemsList))
            menuItem.setDisabled(!open);
    }
}
