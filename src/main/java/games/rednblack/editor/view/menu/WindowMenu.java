package games.rednblack.editor.view.menu;

import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;

import static games.rednblack.h2d.common.MenuAPI.WINDOW_MENU;

public class WindowMenu extends H2DMenu {

    public static final String SPRITE_ANIMATIONS_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".SPRITE_ANIMATIONS_EDITOR_OPEN";
    public static final String CUSTOM_VARIABLES_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".CUSTOM_VARIABLES_EDITOR_OPEN";

    private final MenuItem customVars;
    private final MenuItem animations;

    public WindowMenu() {
        super("Window");
        customVars = new MenuItem("Custom Variables", new MenuItemListener(CUSTOM_VARIABLES_EDITOR_OPEN, null, WINDOW_MENU));
        animations = new MenuItem("Sprite Animations", new MenuItemListener(SPRITE_ANIMATIONS_EDITOR_OPEN, null, WINDOW_MENU));
        addItem(customVars);
        addItem(animations);
    }

    @Override
    public void setProjectOpen(boolean open) {
        customVars.setDisabled(!open);
        animations.setDisabled(!open);
    }

}