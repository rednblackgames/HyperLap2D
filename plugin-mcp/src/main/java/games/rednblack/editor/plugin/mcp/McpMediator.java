package games.rednblack.editor.plugin.mcp;

import games.rednblack.editor.plugin.mcp.settings.McpSettingsVO;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/** Starts/stops the MCP server live (settings toggle) and stops it on editor shutdown. */
public class McpMediator extends Mediator<Object> {
    public static final String NAME = McpMediator.class.getCanonicalName();

    private final McpPlugin plugin;

    public McpMediator(McpPlugin plugin) {
        super(NAME, new Object());
        this.plugin = plugin;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(McpPluginSettingsNotifications.SERVER_TOGGLE, MsgAPI.DISPOSE);
    }

    @Override
    public void handleNotification(INotification notification) {
        String name = notification.getName();
        if (McpPluginSettingsNotifications.SERVER_TOGGLE.equals(name)) {
            McpSettingsVO vo = (McpSettingsVO) notification.getBody();
            if (vo == null) return;
            if (vo.mcpEnabled) {
                plugin.startServer(vo.port);
            } else {
                plugin.stopServer();
            }
        } else if (MsgAPI.DISPOSE.equals(name)) {
            // Editor is shutting down — release the HTTP server so its threads don't keep the JVM alive.
            plugin.stopServer();
        }
    }
}