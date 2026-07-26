package games.rednblack.editor.view.ui.settings;

import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.puremvc.Facade;

import java.io.File;

public class PluginsSettings extends SettingsNodeValue<String> {

    public PluginsSettings(Facade facade) {
        super("Plugins", facade);

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale();

        grid.wideCentered(grid.valueLabel("Choose a plugin to change its settings"));

        grid.section("Directories");
        boolean first = true;
        for (File pluginDir : settingsManager.pluginDirs) {
            grid.row(first ? "Plugins" : "", grid.valueLabelEllipsized(pluginDir.getAbsolutePath()));
            first = false;
        }
        grid.row("Cache", grid.valueLabelEllipsized(settingsManager.cacheDir.getAbsolutePath()));
    }

    @Override
    public void translateSettingsToView() {

    }

    @Override
    public void translateViewToSettings() {

    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public boolean requireRestart() {
        return false;
    }
}
