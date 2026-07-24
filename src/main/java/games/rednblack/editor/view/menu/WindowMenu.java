package games.rednblack.editor.view.menu;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;

import static games.rednblack.h2d.common.MenuAPI.WINDOW_MENU;

public class WindowMenu extends H2DMenu {

    public static final String SPRITE_ANIMATIONS_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".SPRITE_ANIMATIONS_EDITOR_OPEN";
    public static final String CUSTOM_VARIABLES_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".CUSTOM_VARIABLES_EDITOR_OPEN";
    public static final String TAGS_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".TAGS_EDITOR_OPEN";
    public static final String SHADER_UNIFORMS_EDITOR_OPEN = HyperLap2DMenuBar.prefix + ".SHADER_UNIFORMS_EDITOR_OPEN";

    public WindowMenu() {
        super("Window");
        MenuItem customVars = new MenuItem("Custom Variables", icon("icon-menu-custom-vars"), new MenuItemListener(CUSTOM_VARIABLES_EDITOR_OPEN, null, WINDOW_MENU));
        MenuItem tags = new MenuItem("Tags", icon("icon-menu-tags"), new MenuItemListener(TAGS_EDITOR_OPEN, null, WINDOW_MENU));
        MenuItem animations = new MenuItem("Sprite Animations", icon("icon-menu-sprite-anim"), new MenuItemListener(SPRITE_ANIMATIONS_EDITOR_OPEN, null, WINDOW_MENU));
        MenuItem shaderUniforms = new MenuItem("Shader Uniforms", icon("icon-menu-uniforms"), new MenuItemListener(SHADER_UNIFORMS_EDITOR_OPEN, null, WINDOW_MENU));

        addItem(tags);
        addItem(customVars);
        addItem(animations);
        addItem(shaderUniforms);
    }

    @Override
    public void setProjectOpen(boolean open) {
        for (MenuItem menuItem : new Array.ArrayIterator<>(itemsList))
            menuItem.setDisabled(!open);
    }
}