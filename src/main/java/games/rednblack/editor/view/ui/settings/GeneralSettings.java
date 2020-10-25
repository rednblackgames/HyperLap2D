package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.vo.EditorConfigVO;

public class GeneralSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox autoSaving;
    private final VisCheckBox enablePlugins;
    private VisSelectBox<String> filterKeyMapping;

    public GeneralSettings() {
        super("General", HyperLap2DFacade.getInstance());

        getContentTable().add("Editor").left().row();
        getContentTable().addSeparator();
        autoSaving = StandardWidgetsFactory.createCheckBox("Save changes automatically [EXPERIMENTAL]");
        getContentTable().add(autoSaving).left().padTop(5).padLeft(8).row();

        getContentTable().add(getKeyMappingTable()).left().padTop(5).row();

        getContentTable().add("Plugins").left().padTop(10).row();
        getContentTable().addSeparator();
        enablePlugins = StandardWidgetsFactory.createCheckBox("Enable plugins [Require restart]");
        getContentTable().add(enablePlugins).left().padTop(5).padLeft(8).row();
    }

    private Actor getKeyMappingTable() {
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        VisTable mappingTable = new VisTable();
        mappingTable.add("Key mapping:").padLeft(8);
        filterKeyMapping = StandardWidgetsFactory.createSelectBox(String.class);
        filterKeyMapping.setItems(settingsManager.getKeyMappingFiles());
        mappingTable.add(filterKeyMapping).padLeft(8);
        mappingTable.add("[Require restart]").padLeft(8);

        return mappingTable;
    }

    @Override
    public void translateSettingsToView() {
        autoSaving.setChecked(getSettings().autoSave);
        enablePlugins.setChecked(getSettings().enablePlugins);
        filterKeyMapping.setSelected(getSettings().keyBindingLayout);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().autoSave = autoSaving.isChecked();
        getSettings().enablePlugins = enablePlugins.isChecked();
        getSettings().keyBindingLayout = filterKeyMapping.getSelected();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().autoSave != autoSaving.isChecked()
                || getSettings().enablePlugins != enablePlugins.isChecked()
                || !getSettings().keyBindingLayout.equals(filterKeyMapping.getSelected());
    }
}
