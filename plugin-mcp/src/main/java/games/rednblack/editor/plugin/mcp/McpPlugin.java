package games.rednblack.editor.plugin.mcp;

import games.rednblack.editor.plugin.mcp.server.McpHttpServer;
import games.rednblack.editor.plugin.mcp.settings.McpServerSettings;
import games.rednblack.editor.plugin.mcp.settings.McpSettingsVO;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.ToolRegistry;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

/**
 * HyperLap2D plugin that runs an MCP (Model Context Protocol) server inside the editor,
 * served over Streamable HTTP on the JDK built-in HttpServer. MCP clients (e.g. Claude
 * Code) connect to http://127.0.0.1:&lt;port&gt;/mcp and drive the editor via tools.
 *
 * The server is enabled/disabled from Settings -&gt; Plugins -&gt; "MCP Server"; the toggle
 * takes effect live (no restart). Read tools run in-plugin via PluginAPI/runtime; ops
 * that need editor-core access (screenshot, loaded-asset listing, and later validated
 * component edits) are bridged through the editor-core RemoteOpsMediator via MsgAPI
 * notifications carrying {@link games.rednblack.h2d.common.remote.RemoteHandle}s.
 */
@Implementation(authors = "fgnm", version = "0.0.1")
public class McpPlugin extends H2DPluginAdapter {
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.mcp";

    public static final int DEFAULT_PORT = 8765;

    private final McpSettingsVO settingsVO = new McpSettingsVO();
    private final McpContext context = new McpContext(this);
    private final ToolRegistry tools = new ToolRegistry(context);
    private final McpHttpServer server = new McpHttpServer(tools);
    private McpMediator mediator;

    public McpPlugin() {
        super(CLASS_NAME);
    }

    @Override
    public void initPlugin() {
        System.out.println("[MCP] plugin initPlugin called");

        settingsVO.fromStorage(getStorage());

        mediator = new McpMediator(this);
        facade.registerMediator(mediator);

        McpServerSettings settings = new McpServerSettings(facade, this, settingsVO);
        facade.sendNotification(MsgAPI.ADD_PLUGIN_SETTINGS, settings);

        if (settingsVO.mcpEnabled) {
            startServer(settingsVO.port);
        }

        // Safety net: if the normal DISPOSE path isn't reached, still release the HTTP server at JVM exit.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer, "h2d-mcp-shutdown"));
    }

    public void startServer(int port) {
        try {
            server.start(port);
        } catch (Throwable t) {
            System.err.println("[MCP] failed to start server: " + t.getMessage());
        }
    }

    public void stopServer() {
        server.stop();
    }
}