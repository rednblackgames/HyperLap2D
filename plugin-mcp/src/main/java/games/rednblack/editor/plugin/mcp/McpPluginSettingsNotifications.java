package games.rednblack.editor.plugin.mcp;

/** Internal plugin notification names (not part of MsgAPI; plugin-internal only). */
public final class McpPluginSettingsNotifications {
    private McpPluginSettingsNotifications() {}

    /** Body: the current {@link games.rednblack.editor.plugin.mcp.settings.McpSettingsVO}. */
    public static final String SERVER_TOGGLE =
            McpPlugin.CLASS_NAME + ".SERVER_TOGGLE";
}