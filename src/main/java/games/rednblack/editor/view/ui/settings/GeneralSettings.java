package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import games.rednblack.puremvc.Facade;

import java.util.function.Supplier;

public class GeneralSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox autoSaving, useANGLEGLES2, failSafeException;
    private final VisCheckBox enablePlugins;
    private final VisSelectBox<String> filterKeyMapping;
    private final VisSlider uiScaleDensity, msaaSamples, fpsLimit;

    public GeneralSettings(Facade facade) {
        super("General", facade);

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);

        autoSaving = StandardWidgetsFactory.createSwitch();
        failSafeException = StandardWidgetsFactory.createSwitch();
        enablePlugins = StandardWidgetsFactory.createSwitch();
        useANGLEGLES2 = StandardWidgetsFactory.createSwitch();

        filterKeyMapping = StandardWidgetsFactory.createSelectBox(String.class);
        filterKeyMapping.setItems(settingsManager.getKeyMappingFiles());

        uiScaleDensity = StandardWidgetsFactory.createSlider(0.5f, 1.5f, 0.1f);
        msaaSamples = StandardWidgetsFactory.createSlider(0, 16, 1);
        fpsLimit = StandardWidgetsFactory.createSlider(0, 240, 10);

        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale();

        grid.section("Editor");
        grid.toggleWide("Save changes automatically [EXPERIMENTAL]", autoSaving);
        grid.toggleWide("Keep alive on exceptions [EXPERIMENTAL]", failSafeException);
        grid.row("Key mapping", filterKeyMapping);
        grid.sliderRow("UI scale density", uiScaleDensity,
                sliderValue(grid, uiScaleDensity, () -> getUIScaleDensity() + "x"));

        grid.section("Plugins");
        grid.toggleWide("Enable plugins", enablePlugins);

        grid.section("Performance");
        grid.sliderRow("MSAA samples", msaaSamples,
                sliderValue(grid, msaaSamples, () -> String.valueOf(getMsaaSamples())));
        grid.sliderRow("FPS limit", fpsLimit,
                sliderValue(grid, fpsLimit, () -> getFPSLimit() == 0 ? "Unlimited" : String.valueOf(getFPSLimit())));
        grid.toggleWide("Use ANGLE OpenGL ES 2 API", useANGLEGLES2);
    }

    /** The live value of a slider row, kept in sync while the slider is dragged. */
    private VisLabel sliderValue(PropertyGrid grid, VisSlider slider, Supplier<String> text) {
        VisLabel label = grid.valueLabel(text.get());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                label.setText(text.get());
            }
        });
        return label;
    }

    private float getUIScaleDensity() {
        return RoundUtils.round(uiScaleDensity.getValue(), 2);
    }

    private int getMsaaSamples() {
        return (int) msaaSamples.getValue();
    }

    private int getFPSLimit() {
        return (int) fpsLimit.getValue();
    }

    @Override
    public void translateSettingsToView() {
        autoSaving.setChecked(getSettings().autoSave);
        useANGLEGLES2.setChecked(getSettings().useANGLEGLES2);
        failSafeException.setChecked(getSettings().failSafeException);
        enablePlugins.setChecked(getSettings().enablePlugins);
        filterKeyMapping.setSelected(getSettings().keyBindingLayout);
        uiScaleDensity.setValue(getSettings().uiScaleDensity);
        msaaSamples.setValue(getSettings().msaaSamples);
        fpsLimit.setValue(getSettings().fpsLimit);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().autoSave = autoSaving.isChecked();
        getSettings().useANGLEGLES2 = useANGLEGLES2.isChecked();
        getSettings().failSafeException = failSafeException.isChecked();
        getSettings().enablePlugins = enablePlugins.isChecked();
        getSettings().keyBindingLayout = filterKeyMapping.getSelected();
        getSettings().uiScaleDensity = getUIScaleDensity();
        getSettings().msaaSamples = getMsaaSamples();
        getSettings().fpsLimit = getFPSLimit();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return getSettings().autoSave != autoSaving.isChecked()
                || getSettings().useANGLEGLES2 != useANGLEGLES2.isChecked()
                || getSettings().failSafeException != failSafeException.isChecked()
                || getSettings().enablePlugins != enablePlugins.isChecked()
                || !getSettings().keyBindingLayout.equals(filterKeyMapping.getSelected())
                || getSettings().uiScaleDensity != getUIScaleDensity()
                || getSettings().msaaSamples != getMsaaSamples()
                || getSettings().fpsLimit != getFPSLimit();
    }

    @Override
    public boolean requireRestart() {
        return getSettings().useANGLEGLES2 != useANGLEGLES2.isChecked()
                || getSettings().failSafeException != failSafeException.isChecked()
                || getSettings().enablePlugins != enablePlugins.isChecked()
                || !getSettings().keyBindingLayout.equals(filterKeyMapping.getSelected())
                || getSettings().msaaSamples != getMsaaSamples()
                || getSettings().fpsLimit != getFPSLimit();
    }
}
