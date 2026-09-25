package games.rednblack.editor.plugin.performance;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import games.rednblack.h2d.common.MenuAPI;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

@Implementation(authors = "azakhary", version = "0.0.1")
public class PerformancePlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.performance";

    public static final String PANEL_OPEN = CLASS_NAME + ".PANEL_OPEN";

    /** Base name of the atlas packed from {@code assets/textures} and bundled in the plugin jar. */
    private static final String ATLAS_NAME = "performance";

    public PerformancePlugin() {
        super(CLASS_NAME);
    }

    @Override
    public void initPlugin() {
        //the window draws from the atlas, so it cannot be built before the API can extract it
        TextureAtlas pluginAtlas = loadPluginAtlas(ATLAS_NAME);

        facade.registerMediator(new PerformanceWindowMediator(this, pluginAtlas));
        pluginAPI.addMenuItem(MenuAPI.WINDOW_MENU, "Performance", PANEL_OPEN, atlasDrawable(pluginAtlas, "icon-menu-performance"));
    }
}
