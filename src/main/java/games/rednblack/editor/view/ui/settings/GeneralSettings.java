package games.rednblack.editor.view.ui.settings;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.vo.EditorConfigVO;

public class GeneralSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox autoSaving;
    private final VisCheckBox enablePlugins;

    public GeneralSettings() {
        super("General", HyperLap2DFacade.getInstance());

        autoSaving = StandardWidgetsFactory.createCheckBox("Save changes automatically [EXPERIMENTAL]");
        getContentTable().add(autoSaving).left().row();

        getContentTable().add("Plugins").left().padTop(10).row();
        getContentTable().addSeparator();
        enablePlugins = StandardWidgetsFactory.createCheckBox("Enable plugins [Require restart]");
        getContentTable().add(enablePlugins).left().padTop(5).padLeft(8).row();
    }

    @Override
    public void translateSettingsToView() {
        autoSaving.setChecked(getSettings().autoSave);
        enablePlugins.setChecked(getSettings().enablePlugins);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().autoSave = autoSaving.isChecked();
        getSettings().enablePlugins = enablePlugins.isChecked();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().autoSave != autoSaving.isChecked()
                || getSettings().enablePlugins != enablePlugins.isChecked();
    }
}
