package games.rednblack.editor.plugin.mcp.settings;

import java.util.Map;

/** Persisted MCP server settings, stored in the plugin's pluginStorage map. */
public class McpSettingsVO {
    public boolean mcpEnabled = false;
    public int port = 8765;

    public void fromStorage(Map<String, Object> settings) {
        Object enabled = settings.get("mcpEnabled");
        if (enabled instanceof Boolean) mcpEnabled = (Boolean) enabled;
        Object p = settings.get("port");
        if (p instanceof Number) port = ((Number) p).intValue();
    }

    public void toStorage(Map<String, Object> settings) {
        settings.put("mcpEnabled", mcpEnabled);
        settings.put("port", port);
    }
}