package games.rednblack.editor.plugin.performance;

import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

@Implementation(authors = "azakhary", version = "0.0.1")
public class PerformancePlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.performance";

    public static final String PANEL_OPEN = CLASS_NAME + ".PANEL_OPEN";
    public static final String WINDOWS_MENU = "games.rednblack.editor.view.HyperLap2DMenuBar.WINDOW_MENU";

    private final PerformancePanelMediator performancePanelMediator;

    public PerformancePlugin() {
        performancePanelMediator = new PerformancePanelMediator(this);
    }

    @Override
    public void initPlugin() {
        facade.registerMediator(performancePanelMediator);
        pluginAPI.addMenuItem(WINDOWS_MENU, "Performance", PANEL_OPEN);
    }
}
