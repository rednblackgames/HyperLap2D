package games.rednblack.editor.view.ui.settings;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.EditorConfigVO;

public class GeneralSettings extends SettingsDialog.SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox autoSaving;

    public GeneralSettings() {
        super("General");

        autoSaving = StandardWidgetsFactory.createCheckBox("Save changes automatically [EXPERIMENTAL]");
        getContentTable().add(autoSaving).row();
    }

    @Override
    public void translateSettingsToView() {
        autoSaving.setChecked(getSettings().autoSave);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().autoSave = autoSaving.isChecked();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().autoSave != autoSaving.isChecked();
    }
}
