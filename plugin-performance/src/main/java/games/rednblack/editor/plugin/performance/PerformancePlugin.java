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

    private final PerformancePanelMediator performancePanelMediator;

    public PerformancePlugin() {
        super(CLASS_NAME);
        performancePanelMediator = new PerformancePanelMediator(this);
    }

    @Override
    public void initPlugin() {
        facade.registerMediator(performancePanelMediator);
        TextureAtlas pluginAtlas = loadPluginAtlas(ATLAS_NAME);
        pluginAPI.addMenuItem(MenuAPI.WINDOW_MENU, "Performance", PANEL_OPEN, atlasDrawable(pluginAtlas, "icon-menu-performance"));
    }
}
