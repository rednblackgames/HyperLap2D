package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.SettingsNodeValue;

public class PluginsSettings extends SettingsNodeValue<String> {

    public PluginsSettings() {
        super("Plugins", HyperLap2DFacade.getInstance());

        VisLabel visLabel = StandardWidgetsFactory.createLabel("Choose a PlugIn to change settings", "default", Align.center);
        getContentTable().add(visLabel).center().expand().fill().grow();
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
