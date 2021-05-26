package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.vo.EditorConfigVO;

public class GeneralSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox autoSaving, useOpenGL3;
    private final VisCheckBox enablePlugins;
    private VisSelectBox<String> filterKeyMapping;
    private VisSlider uiScaleDensity, msaaSamples;

    public GeneralSettings() {
        super("General", HyperLap2DFacade.getInstance());

        getContentTable().add("Editor").left().row();
        getContentTable().addSeparator();
        autoSaving = StandardWidgetsFactory.createCheckBox("Save changes automatically [EXPERIMENTAL]");
        getContentTable().add(autoSaving).left().padTop(5).padLeft(8).row();

        getContentTable().add(getKeyMappingTable()).left().padTop(5).row();

        getContentTable().add(getUiScaleDensityTable()).left().padTop(5).row();

        getContentTable().add("Plugins").left().padTop(10).row();
        getContentTable().addSeparator();
        enablePlugins = StandardWidgetsFactory.createCheckBox("Enable plugins [Require restart]");
        getContentTable().add(enablePlugins).left().padTop(5).padLeft(8).row();

        getContentTable().add("Performance").left().padTop(10).row();
        getContentTable().addSeparator();
        getContentTable().add(getMassSamplesTable()).left().padTop(5).row();

        useOpenGL3 = StandardWidgetsFactory.createCheckBox("Use OpenGL 3 API [Require restart]");
        getContentTable().add(useOpenGL3).left().padTop(5).padLeft(8).row();
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

    private Actor getUiScaleDensityTable() {
        VisTable scaleTable = new VisTable();

        scaleTable.add("UI Scale Density:").padLeft(8);
        uiScaleDensity = StandardWidgetsFactory.createSlider(0.5f, 1.5f, 0.1f);
        scaleTable.add(uiScaleDensity).padLeft(8);
        VisLabel labelFactor = StandardWidgetsFactory.createLabel("", "default", Align.left);
        scaleTable.add(labelFactor).padLeft(8);
        uiScaleDensity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                labelFactor.setText(getUIScaleDensity() + "x");
            }
        });

        return scaleTable;
    }

    private Actor getMassSamplesTable() {
        VisTable msaaTable = new VisTable();

        msaaTable.add("MSAA Samples:").padLeft(8);
        msaaSamples = StandardWidgetsFactory.createSlider(0, 16, 1);
        msaaTable.add(msaaSamples).padLeft(8);
        VisLabel labelFactor = StandardWidgetsFactory.createLabel("", "default", Align.left);
        msaaTable.add(labelFactor).padLeft(8);
        msaaSamples.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                labelFactor.setText(getMsaaSamples() + " [Require restart]");
            }
        });

        return msaaTable;
    }

    private float getUIScaleDensity() {
        return RoundUtils.round(uiScaleDensity.getValue(), 2);
    }

    private int getMsaaSamples() {
        return (int) msaaSamples.getValue();
    }

    @Override
    public void translateSettingsToView() {
        autoSaving.setChecked(getSettings().autoSave);
        useOpenGL3.setChecked(getSettings().useOpenGL3);
        enablePlugins.setChecked(getSettings().enablePlugins);
        filterKeyMapping.setSelected(getSettings().keyBindingLayout);
        uiScaleDensity.setValue(getSettings().uiScaleDensity);
        msaaSamples.setValue(getSettings().msaaSamples);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().autoSave = autoSaving.isChecked();
        getSettings().useOpenGL3 = useOpenGL3.isChecked();
        getSettings().enablePlugins = enablePlugins.isChecked();
        getSettings().keyBindingLayout = filterKeyMapping.getSelected();
        getSettings().uiScaleDensity = getUIScaleDensity();
        getSettings().msaaSamples = getMsaaSamples();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().autoSave != autoSaving.isChecked()
                || getSettings().useOpenGL3 != useOpenGL3.isChecked()
                || getSettings().enablePlugins != enablePlugins.isChecked()
                || !getSettings().keyBindingLayout.equals(filterKeyMapping.getSelected())
                || getSettings().uiScaleDensity != getUIScaleDensity()
                || getSettings().msaaSamples != getMsaaSamples();
    }
}
