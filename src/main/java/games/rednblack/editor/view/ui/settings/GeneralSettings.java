package games.rednblack.editor.view.ui.settings;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;

public class GeneralSettings extends SettingsDialog.SettingsNodeValue<Object> {

    private final VisCheckBox autoSaving;

    public GeneralSettings() {
        super("General");

        autoSaving = StandardWidgetsFactory.createCheckBox("Save changes automatically");
        getContentTable().add(autoSaving).row();
    }

    @Override
    public void translateSettingsToView() {

    }

    @Override
    public void translateViewToSettings() {

    }

    @Override
    public boolean validateSettings() {
        return false;
    }
}
