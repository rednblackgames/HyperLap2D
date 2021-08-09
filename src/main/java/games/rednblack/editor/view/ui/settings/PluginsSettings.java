package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.io.File;

public class PluginsSettings extends SettingsNodeValue<String> {

    public PluginsSettings() {
        super("Plugins", HyperLap2DFacade.getInstance());

        VisLabel visLabel = StandardWidgetsFactory.createLabel("Choose a PlugIn to change settings", "default", Align.center);
        getContentTable().add(visLabel).center().expand().fill().grow().row();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        getContentTable().add("Plugin directory : ").left().row();
        for (File pluginDir : settingsManager.pluginDirs) {
            getContentTable().add(pluginDir.getAbsolutePath()).left().padLeft(10).row();
        }
        getContentTable().add("Cache directory : ").left().row();
        getContentTable().add(settingsManager.cacheDir.getAbsolutePath()).left().padLeft(10).row();
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
}
