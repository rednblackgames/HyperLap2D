package games.rednblack.editor.plugin.mcp.settings;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.plugin.mcp.McpPlugin;
import games.rednblack.editor.plugin.mcp.McpPluginSettingsNotifications;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

/**
 * Settings panel nested under Settings -> Plugins -> "MCP Server". Exposes an enable
 * checkbox and a port spinner; on Apply it persists the value and sends a live
 * toggle notification so the MCP server starts/stops without a restart.
 */
public class McpServerSettings extends SettingsNodeValue<McpSettingsVO> {

    private final H2DPluginAdapter plugin;
    private final VisCheckBox enableCheckbox;
    private final Spinner portSpinner;
    private boolean loaded = false;

    public McpServerSettings(Facade facade, H2DPluginAdapter plugin, McpSettingsVO vo) {
        super("MCP Server", facade);
        this.plugin = plugin;
        setSettings(vo);

        getContentTable().add("MCP Server").left().row();
        getContentTable().addSeparator();

        enableCheckbox = StandardWidgetsFactory.createCheckBox("Enable MCP server (localhost only)");
        getContentTable().add(enableCheckbox).left().padTop(5).padLeft(8).row();

        getContentTable().add("Port").left().padTop(8).padLeft(8).row();
        int initialPort = vo != null ? vo.port : McpPlugin.DEFAULT_PORT;
        portSpinner = StandardWidgetsFactory.createNumberSelector(initialPort, 1, 65535);
        getContentTable().add(portSpinner).left().padLeft(8).row();
    }

    @Override
    public void translateSettingsToView() {
        loaded = true;
        enableCheckbox.setChecked(getSettings().mcpEnabled);
        try {
            ((IntSpinnerModel) portSpinner.getModel()).setValue(getSettings().port);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void translateViewToSettings() {
        getSettings().mcpEnabled = enableCheckbox.isChecked();
        try {
            getSettings().port = ((IntSpinnerModel) portSpinner.getModel()).getValue();
        } catch (Throwable ignored) {
        }
        getSettings().toStorage(plugin.getStorage());
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
        // Live toggle: the plugin's mediator starts/stops the server immediately.
        facade.sendNotification(McpPluginSettingsNotifications.SERVER_TOGGLE, getSettings());
    }

    @Override
    public boolean validateSettings() {
        return loaded && (getSettings().mcpEnabled != enableCheckbox.isChecked()
                || getSettings().port != currentPort());
    }

    private int currentPort() {
        try {
            return ((IntSpinnerModel) portSpinner.getModel()).getValue();
        } catch (Throwable t) {
            return getSettings().port;
        }
    }

    @Override
    public boolean requireRestart() {
        return false;
    }
}