package games.rednblack.editor.plugin.mcp.tools;

import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import games.rednblack.h2d.common.plugins.PluginAPI;
import games.rednblack.puremvc.Facade;

/**
 * Shared context handed to every tool. Reads the facade/plugin API lazily from the
 * plugin, because {@code H2DPluginAdapter.setAPI(...)} (which populates them) runs
 * after the plugin's field initializers / constructor.
 */
public class McpContext {
    private final H2DPluginAdapter plugin;

    public McpContext(H2DPluginAdapter plugin) {
        this.plugin = plugin;
    }

    public Facade facade() {
        return plugin.facade;
    }

    public PluginAPI api() {
        return plugin.getAPI();
    }
}