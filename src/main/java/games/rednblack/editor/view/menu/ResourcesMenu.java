package games.rednblack.editor.view.menu;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.h2d.common.MsgAPI;

import static games.rednblack.h2d.common.MenuAPI.FILE_MENU;
import static games.rednblack.h2d.common.MenuAPI.RESOURCE_MENU;

public class ResourcesMenu extends H2DMenu {

    public static final String IMPORT_TO_LIBRARY = HyperLap2DMenuBar.prefix + ".IMPORT_TO_LIBRARY";
    public static final String CREATE_PLACEHOLDER = HyperLap2DMenuBar.prefix + ".CREATE_PLACEHOLDER";
    public static final String CREATE_NOISE = HyperLap2DMenuBar.prefix + ".CREATE_NOISE";

    public ResourcesMenu() {
        super("Resources");
        MenuItem importToLibrary = new MenuItem("Import Resources...", new MenuItemListener(IMPORT_TO_LIBRARY, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.IMPORT_TO_LIBRARY));
        MenuItem placeholders = new MenuItem("Create Placeholder...", new MenuItemListener(CREATE_PLACEHOLDER, null, RESOURCE_MENU));
        MenuItem noise = new MenuItem("Create Perlin Noise...", new MenuItemListener(CREATE_NOISE, null, RESOURCE_MENU));
        MenuItem repack = new MenuItem("Repack Assets", new MenuItemListener(MsgAPI.ACTION_REPACK, null, RESOURCE_MENU));

        addItem(importToLibrary);
        addSeparator();
        addItem(repack);
        addSeparator();
        addItem(placeholders);
        addItem(noise);
    }

    @Override
    public void setProjectOpen(boolean open) {
        for (MenuItem menuItem : new Array.ArrayIterator<>(itemsList))
            menuItem.setDisabled(!open);
    }
}
